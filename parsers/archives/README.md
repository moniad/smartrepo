# ARCHIVE EXTRACTION

Extract files from .zip, .tar and .tar.gz archives.

## Running app
Module integrated with other components using docker-compose tool.
To run together with other components in app just type:
```
docker-compose up
```
from docker directory.

## Manual running of module

### Prerequisites
Required Erlang end Elixir installed.

### Setup
```
cd extract
mix deps.get
mix deps.compile
```

### Run
```
mix run extract.ex
```
