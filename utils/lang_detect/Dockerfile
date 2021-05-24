FROM golang:1.16.4-buster

WORKDIR /src
COPY . .
# RUN wget https://dl.google.com/go/go1.16.4.linux-amd64.tar.gz && tar -C /usr/local -xzf go1.16.4.linux-amd64.tar.gz

RUN go get -u github.com/abadojack/whatlanggo

ARG RABBITMQ_HOST
ENV HOST=$RABBITMQ_HOST

COPY main/golang/LanguageDetector.go .

ENTRYPOINT go rugit cn LanguageDetector.go
