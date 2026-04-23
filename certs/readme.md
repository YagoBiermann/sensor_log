## To run on TSL, you have to generate the certificates with the following command: 

    docker run --rm \
      -v "$(pwd)/certs:/certs" \
      -v "$(pwd)/duckdns:/config" \
      infinityofspace/certbot_dns_duckdns:latest certonly \
      --non-interactive \
      --agree-tos \
      --email seu@email.com \
      --authenticator dns-duckdns \
      --dns-duckdns-credentials /config/duckdns.ini \
      -d "seu-dominio.duckdns.org"
##### don't forget to change with your email and domain

If you are on Windows, you might see permission errors while setting up the mosquitto to run on TSL.

The mosquitto container can't read the cert files because of permission deny

In order to fix it, copy the contents of the folder "certs/archive" into another folder that you have created, so you own the permissions of the files.

    mkdir certs/mosquitto
    cp certs/archive/yourdomain/*.pem certs/mosquitto
    chmod 600 certs/mosquitto

don't forget to update the *.env* file with your public **hostname**

