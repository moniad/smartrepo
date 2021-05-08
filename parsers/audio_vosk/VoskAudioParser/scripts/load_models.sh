#!/bin/bash

apt-get update
apt-get install unzip

mkdir /app
mkdir /app/models
cd /app/models

for i in vosk-model-small-en-us-0.15,model_en \
	 vosk-model-small-cn-0.3,model_cn \
	 vosk-model-small-ru-0.15,model_ru \
	 vosk-model-small-fr-pguyot-0.3,model_fr \
	 vosk-model-small-de-0.15,model_de \
	 vosk-model-small-es-0.3,model_es \
	 vosk-model-small-pt-0.3,model_pt \
	 vosk-model-small-tr-0.3,model_tr \
	 vosk-model-small-vn-0.3,model_vn \
	 vosk-model-small-it-0.4,model_it \
	 vosk-model-small-ca-0.4,model_ca \
	 vosk-model-small-fa-0.4,model_fa;
do 
    IFS=',' read model dir <<< "${i}"
    echo "${model}" and "${dir}"
    wget -q http://alphacephei.com/vosk/models/${model}.zip
    unzip ${model}.zip
    mv ${model} ${dir}
    rm -rf ${model}.zip
done