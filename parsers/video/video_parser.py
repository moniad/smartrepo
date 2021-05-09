import cv2
import sys
import moviepy.editor as mpe
import os
import ntpath
import pika


audio_channel = None
frame_channel = None


class VideoParser:
    def __init__(self, video_path):
        self.pathIn = video_path
        self.pathOut = os.path.join(os.getcwd(), '..', '..', 'storage')
        self.count = 0
        self.vidcap = cv2.VideoCapture(self.pathIn)
        self.success, self.image = self.vidcap.read()  # check if file exists
        self.video_formats = ["mp4", "mov", "wmv", "avi", "mpeg"]
        self.fileName = str(ntpath.basename(video_path))
        self.framesFolder = os.path.join(self.pathOut, self.fileName, "frames")
        self.audioFolder = os.path.join(self.pathOut, self.fileName, "audio")
        self.audioPath = ""

    def create_directories(self):
        # Creates temporary output directories for audio and frames
        try:
            os.mkdir(os.path.join(self.pathOut, self.fileName))
            os.mkdir(self.framesFolder)
            os.mkdir(self.audioFolder)
        except FileExistsError:
            print("File with this name has already been parsed.")

    def parse_video(self):
        # Separates video into frames
        while self.success:
            self.vidcap.set(cv2.CAP_PROP_POS_MSEC, (self.count * 1000))
            self.success, self.image = self.vidcap.read()
            if self.success:
                # save frame as JPEG file
                cv2.imwrite(os.path.join(self.framesFolder, "frame%d.jpg" % self.count), self.image)
            # count indicates number of frames per second:
            # count + 1 is 1 frame per second,
            # count + 2 is frame per two seconds
            self.count = self.count + 1

    def extract_audio(self):
        # Creates a moviepy clip and extracts audio
        video = mpe.VideoFileClip(self.pathIn)
        audio = video.audio
        self.audioPath = os.path.join(self.audioFolder, "audio.wav")
        audio.write_audiofile(self.audioPath)

    def parse(self):
        # Checks if the file is a video file and invokes frame and audio extraction
        ext = self.pathIn.split(".")[-1]
        if ext not in self.video_formats:
            print(f"Wrong file extension: {ext}", file=sys.stderr)
        else:
            self.create_directories()
            self.parse_video()
            self.extract_audio()


def callback(ch, method, properties, body):
    print(f"Received path: {body.decode()}")
    parser = VideoParser(video_path=body.decode())
    parser.parse()
    ch.basic_ack(delivery_tag=method.delivery_tag)

    audio_channel.basic_publish(exchange='wav', routing_key='', body=parser.audioPath)
    # TODO add when image parser is available
    # for frame in os.listdir(parser.framesFolder):
    #     frame_path = os.path.join(parser.framesFolder, frame)
    #     frame_channel.basic_publish(exchange='jpg', routing_key='', body=frame_path)


if __name__ == "__main__":

    host = "localhost"

    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host=host))
    video_channel = connection.channel()
    audio_channel = connection.channel()
    frame_channel = connection.channel()

    video_channel.queue_declare(queue='video', durable=True)
    audio_channel.queue_declare(queue='wav', durable=True)
    frame_channel.queue_declare(queue='jpg', durable=True)

    video_channel.basic_qos(prefetch_count=1)
    video_channel.basic_consume(queue='video', on_message_callback=callback)

    print(' [*] Waiting for messages.')
    video_channel.start_consuming()
