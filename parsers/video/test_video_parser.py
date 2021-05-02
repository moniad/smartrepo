import ntpath
import unittest
import sys, os
import pathlib as pl
import pika
import time


class VideoParserTestCase(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.path_input = sys.argv[1]
        cls.path_output = os.path.join(os.getcwd(), '..', '..', 'storage')
        cls.file_name = str(ntpath.basename(cls.path_input))
        cls.frames_folder = os.path.join(cls.path_output, cls.file_name, "frames")
        cls.audio_folder = os.path.join(cls.path_output, cls.file_name, "audio")

        host = "localhost"
        cls._connection = pika.BlockingConnection(
            pika.ConnectionParameters(host=host))
        cls.channel = cls._connection.channel()

        cls.channel.queue_declare(queue='mp4')
        cls.channel.basic_publish(exchange='', routing_key='mp4', body=cls.path_input)
        time.sleep(15)

    @classmethod
    def tearDownClass(cls):
        cls._connection.close()

    def test_audio_file_created(self):
        path = pl.Path(self.audio_folder)
        self.assertEqual((str(path), path.is_dir()), (str(path), True))
        self.assertTrue(os.listdir(self.audio_folder))

    def test_frames_created(self):
        path = pl.Path(self.frames_folder)
        self.assertEqual((str(path), path.is_dir()), (str(path), True))
        self.assertTrue(os.listdir(self.frames_folder))


if __name__ == '__main__':
    unittest.main(argv=['first-arg-is-ignored'], exit=False)
