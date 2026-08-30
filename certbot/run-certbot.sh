#!/bin/bash

DOMAIN=$1
EMAIL=$2

if [ -z "$DOMAIN" ]; then
  read -p "Domain: " DOMAIN
fi
 
if [ -z "$EMAIL" ]; then
  read -p "Email: " EMAIL
fi

certbot certonly --standalone --non-interactive --agree-tos --email "$EMAIL" -d "$DOMAIN"

mkdir -p ./certs
sudo chown -R 1883:1883 ./certs
chmod 700 ./certs
cp /etc/letsencrypt/live/$DOMAIN/*.pem ./certs/
chmod 600 ./certs/privkey.pem
chmod 644 ./certs/fullchain.pem ./certs/cert.pem ./certs/chain.pem