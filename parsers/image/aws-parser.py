import boto3
import json
import sys
import pika
import pathlib

from PIL import Image

import pytesseract
import logging
from confluent_kafka import Producer


class ImageRecognition:
    def __init__(self):
        logging.basicConfig(format='%(asctime)s %(levelname)s - %(message)s', level=logging.INFO)

        self.credentials_path = pathlib.Path('aws_credentials.json')
        with open(self.credentials_path) as f:
            self.credentials = json.load(f)
        self.client = boto3.client('rekognition',
                                   region_name='us-east-1',
                                   aws_access_key_id=self.credentials['aws_access_key_id'],
                                   aws_secret_access_key=self.credentials['aws_secret_access_key'],
                                   aws_session_token=self.credentials['aws_session_token']
                                   )

        self.pathIn = ''
        self.content = ''

        if len(sys.argv) > 1:
            rabbit_host = sys.argv[1]
        else:
            rabbit_host = "localhost"

        # RabbitMQ initialization
        self.connection = pika.BlockingConnection(pika.ConnectionParameters(host=rabbit_host, port=5672))
        self.image_channel = self.connection.channel()
        self.image_channel.basic_qos(prefetch_count=1)

        self.image_channel.queue_declare(queue="jpg")
        self.image_channel.basic_consume(queue="jpg", on_message_callback=self.callback)

        self.image_channel.queue_declare(queue="png")
        self.image_channel.basic_consume(queue="png", on_message_callback=self.callback)

        conf = {'bootstrap.servers': rabbit_host+":9092"}
        self.producer = Producer(conf)

    def detect_image(self):
        with open(self.pathIn, 'rb') as image:
            try:
                resp = self.client.detect_labels(Image={'Bytes': image.read()})
                for label in resp['Labels']:
                    if label['Name'] is not None and label['Name'] != '' and label['Name'] != []:
                        self.content += _convert_to_str(label['Name']) + ' '
            except:
                logging.error("No response from AWS")

            ocr_result = pytesseract.image_to_string(Image.open(self.pathIn))
            self.content += _convert_to_str(ocr_result) + ' '

    def callback(self, ch, method, properties, body):
        # run aws recognition
        tmp_path = str(body.decode())
        logging.info("Parsing file: " + tmp_path)
        self.producer.produce("parsers", key="image", value=f"Started parsing file {tmp_path}")
        self.pathIn = pathlib.Path('../../storage', tmp_path)
        self.detect_image()

        logging.info("Content: " + str(self.content))
        self.producer.produce("parsers", key="video", value=f"Finished parsing file {tmp_path}")

        ch.basic_publish(
            exchange='',
            routing_key=properties.reply_to,
            properties=pika.BasicProperties(),
            body=str(self.content))
        ch.basic_ack(delivery_tag=method.delivery_tag)

        self.content = ''


def _convert_to_str(s):
    new = ''
    for x in s:
        new += x

    return new


if __name__ == "__main__":
    logging.info("Image parser started")
    recognizer = ImageRecognition()
    recognizer.image_channel.start_consuming()
