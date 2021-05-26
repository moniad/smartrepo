FROM jjanzic/docker-python3-opencv
COPY requirements.txt req.txt
RUN pip install -r ./req.txt
ADD audio_converter.py .

RUN apt-get -y update \
&& apt-get -y upgrade \
&& apt-get install -y ffmpeg

ARG RABBITMQ_HOST
ENV HOST=$RABBITMQ_HOST

ENTRYPOINT python ./audio_converter.py $HOST