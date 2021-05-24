FROM jjanzic/docker-python3-opencv

COPY requirements.txt req.txt
RUN pip install -r ./req.txt
ADD video_parser.py .

ARG RABBITMQ_HOST
ENV HOST=$RABBITMQ_HOST

ENTRYPOINT python ./video_parser.py $HOST