### RabbitMQ + Docker demo

Oficjalny obraz RabbitMQ - https://hub.docker.com/_/rabbitmq  

#### Docker - instalacja:<br />
* https://docs.docker.com/engine/install/
* https://docs.docker.com/compose/install/

#### Uruchamianie

1. Z użyciem Docker Compose <br />
    * Uruchomienie: <br />
    `docker-compose up`
    * Zatrzymanie aplikacji - CTRL+C
    * Usunięcie danych: <br />
    `docker-compose down -v`
    * Przebudowanie obrazów: <br />
    `docker-compose build`
    * Usunięcie obrazów: <br />
    `docker image rm smartrepo_producer smartrepo_consumer rabbitmq:3`
    
    _Komendy `docker-compose` należy wykonywać z poziomu głównego katalogu._
    
2. Lokalnie + RabbitMQ w Dockerze
    * Uruchomienie serwera RabbitMQ w kontenerze: <br />
    `docker run -d -p 5672:5672 --name smartrepo-rabbitmq-local rabbitmq:3`
    * Zatrzymanie: <br />
    `docker stop smartrepo-rabbitmq-local`
    * Uruchomienie ponownie: <br />
    `docker start smartrepo-rabbitmq-local`
    * Usunięcie: <br />
    `docker rm smartrepo-rabbitmq-local`
      