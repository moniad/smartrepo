import os
import sys
import pathlib
import pika
import ntpath
from pydub import AudioSegment


class AudioConverter:
    def __init__(self):
        self.pathIn = None
        self.fileName = ""
        self.pathOut = pathlib.Path('storage')
        self.accepted_formats = [".mp3", ".aac", ".flac", ".ogg"]
        self.ext = ""

        if len(sys.argv) > 1:
            rabbit_host = sys.argv[1]
        else:
            rabbit_host = "localhost"

        # RabbitMQ initialization
        self.connection = pika.BlockingConnection(
            pika.ConnectionParameters(host=rabbit_host, port=5672))
        self.channel = self.connection.channel()

        self.wav_result = self.channel.queue_declare(queue='', exclusive=True)
        self.wav_callback_queue = self.wav_result.method.queue
        self.channel.basic_consume(
            queue=self.wav_callback_queue,
            on_message_callback=self.on_wav_response,
            auto_ack=True)
        self.wav_response = ""
        self.channel.basic_qos(prefetch_count=1)

        for audio_format in self.accepted_formats:
            ext = audio_format.split(".")[-1]
            self.channel.queue_declare(queue=ext)
            self.channel.basic_consume(queue=ext, on_message_callback=self.callback)

    def set_path_and_ext(self, received_path):
        self.pathIn = pathlib.Path(received_path)
        self.fileName = str(ntpath.basename(self.pathIn)).split(".")[0]
        self.ext = self.pathIn.suffix
        self.pathOut = pathlib.Path(self.pathOut, self.fileName+".wav")

    def create_tmp_file(self):
        if not os.path.exists(self.pathOut):
            with open(self.pathOut, 'a'):
                pass

    def delete_tmp_file(self):
        os.remove(self.pathOut)

    def callback(self, ch, method, properties, body):
        self.set_path_and_ext(body.decode())
        self.create_tmp_file()
        song = AudioSegment.from_file(str(self.pathIn), self.ext.split(".")[-1])
        song.export(self.pathOut, "wav")
        rel_audio_path = str(self.pathOut.relative_to(*self.pathOut.parts[:1]))
        self.channel.basic_publish(
            exchange='',
            routing_key='wav',
            properties=pika.BasicProperties(),
            body=rel_audio_path.encode('utf-8'))

        while len(self.wav_response) == 0:
            self.connection.process_data_events()

        ch.basic_publish(
            exchange='',
            routing_key=properties.reply_to,
            properties=pika.BasicProperties(),
            body=self.wav_response.encode('utf-8'))

        self.delete_tmp_file()
        ch.basic_ack(delivery_tag=method.delivery_tag)

    def on_wav_response(self, ch, method, properties, body):
        self.wav_response = body.decode()


if __name__ == "__main__":

    parser = AudioConverter()

    print(' [*] Waiting for messages.')
    parser.channel.start_consuming()
