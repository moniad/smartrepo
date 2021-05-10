FROM bitwalker/alpine-elixir-phoenix:latest

WORKDIR /app

COPY extract/mix.exs .
COPY extract/mix.lock .
COPY extract/extract.ex .

ARG RABBITMQ_HOST
ENV HOST=$RABBITMQ_HOST

ENTRYPOINT mix deps.get && mix deps.compile && mix run extract.ex $HOST

