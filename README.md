# (WIP) klasy na backendzie są na brudno, jutro przerobię trochę sposób działania fileSavera żeby lepiej pasował do rabbita

##Brief:
Implementacja komunikacji parserów z backendem przy użyciu rabbitmq.
Parsery i broker rabbita są stawiane w dockerach. Kontenery i backend używają
wspólnego folderu smartrepo/storage, gdzie backend będzie zapisywał pobrane pliki,
a parsery sobie je stamtąd wyciagały gry przyjdzie request na przeparsowanie
pliku o ścieżce określonej w requestcie. 

##Jak to teraz działa:    

Nie zgrywałem tego jeszcze z uploadem plików więc zrobiłem przykład na dwóch plikach które wrzuciłem do storage, sample.pdf i sample.txt.
Dodałem sobie do testu dwa endpointy /test/pdf i /test/txt, wywołanie powoduje wysłanie przez backend do odpowiednich kolejek
requestów na przeparsowanie odpowiednio sample.pdf i sample.txt. Parsery to ogarną i zwrócą na tymczasową
kolejkę backendu response w postaci stringa a on go wypisze. Taaaa dam

##Jak uruchomić:
(w folderze docker) stawiamy parsery i brokera rabbita:
```
docker-compose up
```
jeśli chcemy przebudować parsery jako takie to można tutaj dodać   
`--build`, a replikować parsery można przy użyciu `--scale`  
(np: `--scale txt_tika=2`)

Odpalamy backend

wchodzimy na `localhost:7777/test/pdf` albo `localhost:7777/test/txt`

na konsoli w któej odpaliliście docker-compose pojawią się printy z parserów, a Spring
wyprintuje stringi które dostał w odpowiedzi od parserów.