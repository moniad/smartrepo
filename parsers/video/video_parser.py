import cv2
import sys
import moviepy.editor as mpe
import os
import ntpath
import pika


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
        audio.write_audiofile(os.path.join(self.audioFolder, "audio.mp3"))

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


if __name__ == "__main__":

    host = "localhost"

    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host=host))
    channel = connection.channel()

    channel.queue_declare(queue='mp4')

    channel.basic_consume(queue='mp4', on_message_callback=callback, auto_ack=True)

    print(' [*] Waiting for messages.')
    channel.start_consuming()

