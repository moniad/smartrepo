FROM golang:1.16.4-buster

WORKDIR /src
COPY . .

COPY src/main/golang/* ./

EXPOSE 10000
ENTRYPOINT go run LanguageDetection.go
