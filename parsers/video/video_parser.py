import cv2
import sys
import moviepy.editor as mpe
import os
import pathlib
import ntpath
import pika
import shutil


class VideoParser:
    def __init__(self):
        # file parameters
        self.storagePath = pathlib.Path('../../storage')
        self.pathIn = None
        self.count = 0
        self.vidCap = None
        self.success, self.image = None, None
        self.video_formats = [".mp4", ".mov", ".wmv", ".avi", ".mpeg"]
        self.fileName = ""
        self.framesFolder = None
        self.audioFolder = None
        self.audioPath = None
        self.ext = ""

        if len(sys.argv) > 1:
            rabbit_host = sys.argv[1]
        else:
            rabbit_host = "localhost"

        # RabbitMQ initialization
        params = pika.ConnectionParameters(host=rabbit_host, port=5672,
                                           heartbeat=600,
                                           blocked_connection_timeout=300)
        self.connection = pika.BlockingConnection(params)
        self.channel = self.connection.channel()
        self.audio_channel = self.connection.channel()
        self.frame_channel = self.connection.channel()
        self.channel.basic_qos(prefetch_count=1)
        self.audio_channel.basic_qos(prefetch_count=1)
        self.frame_channel.basic_qos(prefetch_count=1)

        self.reply_to = ""

        for video_format in self.video_formats:
            ext = video_format.split(".")[-1]
            self.channel.queue_declare(queue=ext)
            self.channel.basic_consume(queue=ext,
                                       on_message_callback=self.callback,
                                       auto_ack=False)

        self.audio_result = self.audio_channel.queue_declare(queue='', exclusive=True)
        self.frame_result = self.frame_channel.queue_declare(queue='', exclusive=True)

        self.audio_callback_queue = self.audio_result.method.queue
        self.frame_callback_queue = self.frame_result.method.queue

        self.audio_channel.basic_consume(
            queue=self.audio_callback_queue,
            on_message_callback=self.on_audio_response,
            auto_ack=False)

        self.frame_channel.basic_consume(
            queue=self.frame_callback_queue,
            on_message_callback=self.on_frame_response,
            auto_ack=False)

        self.audio_response = ""
        self.frame_response = ""
        self.audio_parsed = False
        self.n_frames_parsed = 0

    def set_paths(self, video_path):
        self.pathIn = pathlib.Path(self.storagePath, video_path)
        self.vidCap = cv2.VideoCapture(str(self.pathIn))
        self.success, self.image = self.vidCap.read()  # check if file exists
        self.fileName = str(ntpath.basename(self.pathIn)).split(".")[0]
        self.framesFolder = pathlib.Path(self.storagePath, self.fileName, "frames")
        self.audioFolder = pathlib.Path(self.storagePath, self.fileName, "audio")

    def create_directories(self):
        # Creates temporary output directories for audio and frames
        try:
            os.mkdir(pathlib.Path(self.storagePath, self.fileName))
            os.mkdir(self.framesFolder)
            os.mkdir(self.audioFolder)
        except FileExistsError:
            print("File with this name has already been parsed.")

    def remove_directories(self):
        # removes temporary output directories for audio and frames
        shutil.rmtree(pathlib.Path(self.storagePath, self.fileName))

    def parse_video(self):
        # Separates video into frames
        while self.success:
            self.vidCap.set(cv2.CAP_PROP_POS_MSEC, (self.count * 1000))
            self.success, self.image = self.vidCap.read()
            if self.success:
                # save frame as JPEG file
                cv2.imwrite(os.path.join(self.framesFolder, "frame%d.jpg" % self.count), self.image)
            # count indicates number of frames per second:
            # count + 1 is 1 frame per second,
            # count + 2 is frame per two seconds
            self.count = self.count + 1

    def extract_audio(self):
        # Creates a moviepy clip and extracts audio
        video = mpe.VideoFileClip(str(self.pathIn))
        audio = video.audio
        self.audioPath = pathlib.Path(self.audioFolder, self.fileName+".wav")
        audio.write_audiofile(str(self.audioPath))

    def parse(self):
        # Checks if the file is a video file and invokes frame and audio extraction
        ext = self.pathIn.suffix
        if ext not in self.video_formats:
            print(f"Wrong file extension: {ext}", file=sys.stderr)
        else:
            self.create_directories()
            self.parse_video()
            self.extract_audio()

    def callback(self, ch, method, properties, body):
        self.audio_response = ""
        self.frame_response = ""
        self.audio_parsed = False
        self.n_frames_parsed = 0
        self.reply_to = properties.reply_to

        # parse received video
        print(f"Parsing file: {body.decode()}")
        self.set_paths(body.decode())
        self.parse()

        # send audio to audio parser and get results
        rel_audio_path = str(pathlib.Path(self.fileName, "audio", self.fileName+".wav"))
        print('Sending audio file to audio queue')
        self.audio_channel.basic_publish(
            exchange='',
            routing_key='wav',
            properties=pika.BasicProperties(
                reply_to=self.audio_callback_queue
            ),
            body=rel_audio_path.encode('utf-8'))

        # send frames to image parser and get results
        rel_frames_path = str(pathlib.Path(self.fileName, "frames"))
        print('Sending frames to image queue')
        for frame in os.listdir(self.framesFolder):
            self.frame_channel.basic_publish(
                exchange='',
                routing_key='jpg',
                properties=pika.BasicProperties(
                    reply_to=self.frame_callback_queue
                ),
                body=(rel_frames_path+"/"+frame).encode('utf-8'))

        ch.basic_ack(delivery_tag=method.delivery_tag)

    def on_audio_response(self, ch, method, properties, body):
        self.audio_response = body.decode()
        self.audio_parsed = True
        ch.basic_ack(delivery_tag=method.delivery_tag)
        self.send_reply()

    def on_frame_response(self, ch, method, properties, body):
        self.frame_response += body.decode()
        ch.basic_ack(delivery_tag=method.delivery_tag)
        self.n_frames_parsed += 1
        self.send_reply()

    def send_reply(self):
        if self.audio_parsed and self.n_frames_parsed == len(os.listdir(self.framesFolder)):
            print("Received response:")
            full_transcript = str(self.audio_response + self.frame_response)
            print(full_transcript)
            self.channel.basic_publish(
                exchange='',
                routing_key=self.reply_to,
                body=full_transcript.encode('utf-8'))
            self.remove_directories()


if __name__ == "__main__":

    parser = VideoParser()

    print(' [*] Waiting for messages.')
    parser.channel.start_consuming()
    parser.audio_channel.start_consuming()
    parser.frame_channel.start_consuming()
