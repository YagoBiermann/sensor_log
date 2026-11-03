#!/bin/bash
#Run this script to generate a new SSL certificate for the Mosquitto MQTT broker using Certbot
#The certificates will be stored in the ./certs/public directory

DOMAIN=$1
EMAIL=$2

if [ -z "$DOMAIN" ]; then
  read -p "Domain: " DOMAIN
fi
 
if [ -z "$EMAIL" ]; then
  read -p "Email: " EMAIL
fi

certbot certonly --standalone --non-interactive --agree-tos --email "$EMAIL" -d "$DOMAIN"

mkdir -p ./certs/public ./certs/private
sudo chown -R 1883:1883 ./certs/public
chmod 700 ./certs/public
cp /etc/letsencrypt/live/$DOMAIN/fullchain.pem ./certs/public/server-fullchain.crt
cp /etc/letsencrypt/live/$DOMAIN/cert.pem ./certs/public/server.crt
cp /etc/letsencrypt/live/$DOMAIN/chain.pem ./certs/public/server-chain.crt
cp /etc/letsencrypt/live/$DOMAIN/privkey.pem ./certs/private/server.key
chmod 600 ./certs/private/server.key
chmod 644 ./certs/public/server-fullchain.crt ./certs/public/server.crt ./certs/public/server-chain.crt