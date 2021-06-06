FROM python:3.8

ARG RABBITMQ_HOST
ENV HOST=$RABBITMQ_HOST

COPY ./parser.py .

RUN python -m pip install pika --upgrade & python -m pip install zeep

ENTRYPOINT python parser.py $HOST
