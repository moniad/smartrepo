FROM python:3.8

COPY requirements.txt req.txt
RUN pip install -r ./req.txt
ADD aws-parser.py .
ADD aws_credentials.json .
ARG RABBITMQ_HOST
ENV HOST=$RABBITMQ_HOST

ENTRYPOINT python ./aws-parser.py $HOST