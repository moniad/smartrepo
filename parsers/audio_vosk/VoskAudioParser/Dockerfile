FROM mcr.microsoft.com/dotnet/sdk:5.0 AS build
WORKDIR /source

COPY /scripts/load_models.sh /scripts/
RUN bash -c /scripts/load_models.sh

COPY . .
RUN dotnet publish -c Release -o /app


FROM mcr.microsoft.com/dotnet/runtime:5.0

ARG RABBITMQ_HOST
ENV HOST=$RABBITMQ_HOST

WORKDIR /app
COPY --from=build /app .

RUN apt update && apt install libatomic1

ENTRYPOINT dotnet VoskAudioParser.dll $HOST