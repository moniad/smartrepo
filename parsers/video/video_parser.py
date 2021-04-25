import cv2

import sys

import moviepy.editor as mpe

import os

import ntpath


class VideoParser:
    def __init__(self, videoPath):
        self.pathIn = videoPath
        self.pathOut = str(os.getcwd()) + '../../storage/'
        self.count = 0
        self.vidcap = cv2.VideoCapture(self.pathIn)
        self.success, self.image = self.vidcap.read() # check if file exists
        self.video_formats = ["mp4", "mov", "wmv", "avi", "mpeg"]
        self.fileName = str(ntpath.basename(videoPath))
        self.framesFolder = self.pathOut + self.fileName + "/frames/"
        self.audioFolder = self.pathOut + self.fileName + "/audio/"

    def create_directories(self):
        try:
            os.mkdir(self.pathOut + self.fileName)
            os.mkdir(self.framesFolder)
            os.mkdir(self.audioFolder)
        except:
            print("File with this name has already been parsed ")

    def parse_video(self):
        while self.success:
            self.vidcap.set(cv2.CAP_PROP_POS_MSEC, (self.count * 1000))  # added this line
            self.success, self.image = self.vidcap.read()
            if self.success:
                cv2.imwrite(self.framesFolder + "\\frame%d.jpg" % self.count, self.image)  # save frame as JPEG file
            # count indicates number of frames per second count +1 is 1 frame per second, count + 2 is frame per two seconds
            self. count = self.count + 1

    def extract_audio(self):
        # Creates a moviepy clip and returns audio

        video = mpe.VideoFileClip(self.pathIn)
        audio=video.audio
        audio.write_audiofile(self.audioFolder+"/audio.mp3")


    def check_extension(self):
        # Checks if the file is a video file, otherwise prints to error
        split_path = self.pathIn.split(".")
        ext = split_path[-1]
        if ext not in self.video_formats:
            print(f"Wrong file extension: {ext}", file=sys.stderr)
        else:
            self.create_directories()
            self.parse_video()
            self.extract_audio()


if __name__ == "__main__":
    # python VideoParser.py pathIn

    pathInput = sys.argv[1]

    parser = VideoParser(videoPath=pathInput)
    parser.check_extension()