FROM python:3.8

RUN apt-get update -y
RUN apt-get install -y python-pip python-dev build-essential
RUN apt update && apt install -y libsm6 libxext6
RUN apt-get -y install tesseract-ocr
#RUN apt-get install tesseract-ocr-all -y
RUN pip install pillow
RUN pip install pytesseract

COPY requirements.txt req.txt
RUN pip install -r ./req.txt
ADD aws-parser.py .
ADD aws_credentials.json .
ARG RABBITMQ_HOST
ENV HOST=$RABBITMQ_HOST

ENTRYPOINT python ./aws-parser.py $HOST