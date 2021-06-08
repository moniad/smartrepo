import os
import sys
import pathlib
import pika
import ntpath
from pydub import AudioSegment


class AudioConverter:
    def __init__(self):
        self.storagePath = pathlib.Path('../../storage')
        self.pathIn = None
        self.fileName = ""
        self.pathOut = None
        self.accepted_formats = [".mp3", ".aac", ".flac", ".ogg"]
        self.ext = ""

        if len(sys.argv) > 1:
            rabbit_host = sys.argv[1]
        else:
            rabbit_host = "localhost"

        # RabbitMQ initialization
        params = pika.ConnectionParameters(host=rabbit_host, port=5672,
                                           heartbeat=600,
                                           blocked_connection_timeout=1000)
        self.connection = pika.BlockingConnection(params)
        self.channel = self.connection.channel()
        self.reply_to = ""

        self.wav_result = self.channel.queue_declare(queue='', exclusive=True)
        self.wav_callback_queue = self.wav_result.method.queue
        self.channel.basic_consume(
            queue=self.wav_callback_queue,
            on_message_callback=self.on_wav_response,
            auto_ack=False)
        self.wav_response = ""
        self.channel.basic_qos(prefetch_count=1)

        for audio_format in self.accepted_formats:
            ext = audio_format.split(".")[-1]
            self.channel.queue_declare(queue=ext)
            self.channel.basic_consume(queue=ext, on_message_callback=self.callback, auto_ack=False)

    def set_path_and_ext(self, received_path):
        self.pathIn = pathlib.Path(self.storagePath, received_path)
        self.fileName = str(ntpath.basename(self.pathIn)).split(".")[0]
        self.ext = self.pathIn.suffix
        self.pathOut = pathlib.Path(self.storagePath, self.fileName+".wav")

    def create_tmp_file(self):
        if not os.path.exists(self.pathOut):
            with open(self.pathOut, 'a'):
                pass

    def delete_tmp_file(self):
        os.remove(self.pathOut)

    def callback(self, ch, method, properties, body):
        self.set_path_and_ext(body.decode())
        self.create_tmp_file()
        self.reply_to = properties.reply_to

        print(f"Parsing file:{self.fileName+self.ext}")
        audio = AudioSegment.from_file(str(self.pathIn), self.ext.split(".")[-1])
        audio.export(self.pathOut, "wav")
        rel_audio_path = self.fileName+".wav"
        self.channel.basic_publish(
            exchange='',
            routing_key='wav',
            properties=pika.BasicProperties(reply_to=self.wav_callback_queue),
            body=rel_audio_path.encode('utf-8'))

        ch.basic_ack(delivery_tag=method.delivery_tag)

    def on_wav_response(self, ch, method, properties, body):
        self.wav_response = body.decode()
        print("Received result:")
        print(self.wav_response)
        ch.basic_ack(delivery_tag=method.delivery_tag)
        ch.basic_publish(
            exchange='',
            routing_key=self.reply_to,
            properties=pika.BasicProperties(),
            body=self.wav_response.encode('utf-8'))
        self.delete_tmp_file()


if __name__ == "__main__":

    parser = AudioConverter()

    print(' [*] Waiting for messages.')
    parser.channel.start_consuming()
