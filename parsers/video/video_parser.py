import os
import sys

import moviepy.editor as mpe

video_formats = ["mp4", "mov", "wmv", "avi", "mpeg"]


def extract_audio(path):
    # Creates a moviepy clip and returns audio
    check_extension(path)
    video = mpe.VideoFileClip(path)
    return video.audio


def check_extension(path):
    # Checks if the file is a video file, otherwise prints to error
    split_path = path.split(".")
    ext = split_path[-1]
    if ext not in video_formats:
        print(f"Wrong file extension: {ext}", file=sys.stderr)


def main():
    # TODO replace with the path from queue
    cwd = os.getcwd()
    easy_path = os.path.join(cwd, "../../backend/src/main/resources/video-to-text-samples/english/easy/",
                             "Bill_Gates_480p_part.mp4")

    # extracting audio from video
    audio = extract_audio(easy_path)

    # writing audio file to temporary location
    audio.write_audiofile(os.path.join(cwd, "../../storage/easy.mp3"))


if __name__ == '__main__':
    main()
