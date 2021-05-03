import boto3
import json
import sys


class ImageRecognition:
    def __init__(self, path):
        with open('aws_credentials.json') as f:
            self.credentials = json.load(f)
        self.client = boto3.client('rekognition',
                                   region_name='us-east-1',
                                   aws_access_key_id=self.credentials['aws_access_key_id'],
                                   aws_secret_access_key=self.credentials['aws_secret_access_key'],
                                   aws_session_token=self.credentials['aws_session_token']
                                   )
        self.response = []
        self.pathIn = path

    def detect_image(self):
        with open(self.pathIn, 'rb') as image:
            self.response = self.client.detect_labels(Image={'Bytes': image.read()})

        with open("response.txt", 'w') as f:
            for label in self.response['Labels']:
                f.write(str(label['Name']) + '\n')


if __name__ == "__main__":
    """"
    Update credentials in aws_credentials.json
    Run: python image_recogniton.py pathInput
    
    """
    pathInput = sys.argv[1]

    recognizer = ImageRecognition(path=pathInput)
    recognizer.detect_image()

    # it is also possible to add detection confidence
    # print('Detected labels in ' + photo)
    # for label in response['Labels']:
    #     print(label['Name'] + ' : ' + str(label['Confidence']))
