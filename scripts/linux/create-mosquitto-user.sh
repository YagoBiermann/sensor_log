#!/bin/bash
# This script creates a new user for the Mosquitto MQTT broker and stores the credentials in a password file with proper hashing.


USER=$1
PASS=$2

if [[ "$(basename "$(pwd)")" != "sensor_log" ]]; then
  echo "Please run this script from the sensor_log directory."
  exit 1
fi
if [ -z "$USER" ]; then
  read -p "user: " USER
fi

if [ -z "$PASS" ]; then
  read -s -p "password: " PASS
  echo
fi

touch ./mosquitto.passwd

docker run --rm -it \
  -v $(pwd)/mosquitto.passwd:/mosquitto/config/mosquitto.passwd \
  eclipse-mosquitto \
  mosquitto_passwd -b /mosquitto/config/mosquitto.passwd "$USER" "$PASS"