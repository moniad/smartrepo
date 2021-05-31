import ntpath
import os
import pika
import sys
import time
import unittest


class AudioConverterTestCase(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        cls.path_input = sys.argv[1]
        cls.path_output = os.path.join(os.getcwd(), '..', '..', 'storage')
        cls.accepted_formats = ["mp3", "aac", "flac", "ogg"]
        cls.file_name = str(ntpath.basename(cls.path_input))

        cls._connection = pika.BlockingConnection(
            pika.ConnectionParameters(host="localhost"))
        cls.channel = cls._connection.channel()

        for audio_format in cls.accepted_formats:
            cls.channel.queue_declare(queue=audio_format)
        for audio_format in cls.accepted_formats:
            cls.channel.basic_publish(exchange='',
                                  routing_key=audio_format,
                                  body=cls.path_input.encode('utf-8'),
                                  properties=pika.BasicProperties())
        time.sleep(10)

    @classmethod
    def tearDownClass(cls):
        cls._connection.close()

    def test_audio_file_created(self):
        self.assertTrue(os.listdir(self.path_input))


if __name__ == '__main__':
    unittest.main(argv=['first-arg-is-ignored'], exit=False)
