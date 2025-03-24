#!/bin/bash

echo "=== Iniciando deploy do plugin ==="

SERVER_IP="181.215.45.238"
SERVER_PORT="22"
SERVER_USER="root"
SERVER_PASSWORD="devprime"
SERVER_PLUGIN_DIR="/home/minecraft/server/plugins/"
PLUGIN_NAME="PrimeLeagueX1"  # nome do plugin

# Verifica dependências
if ! command -v mvn &> /dev/null; then echo "Instale o Maven."; exit 1; fi
if ! command -v sshpass &> /dev/null; then echo "Instale o sshpass."; exit 1; fi

# Envia o .jar
JAR_FILE="target/${PLUGIN_NAME}.jar"
[ -f "$JAR_FILE" ] || { echo "Arquivo $JAR_FILE não encontrado."; exit 1; }

sshpass -p "$SERVER_PASSWORD" scp -P $SERVER_PORT "$JAR_FILE" $SERVER_USER@$SERVER_IP:$SERVER_PLUGIN_DIR/${PLUGIN_NAME}.jar || {
  echo "Erro ao copiar plugin."; exit 1;
}

# Recarrega o plugin
sshpass -p "$SERVER_PASSWORD" ssh -p $SERVER_PORT $SERVER_USER@$SERVER_IP "screen -r -X stuff 'plugman reload ${PLUGIN_NAME}\n'"

echo "✅ Deploy concluído com sucesso!" 