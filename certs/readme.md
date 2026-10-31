## Certificate layout

```text
certs/
├── public/                 # Public certificates
├── private/                # Private keys and unseal secrets
├── bundles/                # PKCS12 and certificate metadata bundles
└── ca/                     # Certificate authorities and CA metadata
```

The Mosquitto server uses `public/server-fullchain.crt` and `private/server.key`.
The Spring Boot client uses `bundles/admin.p12`.

## Generate the public certificate

To run on TLS, generate the certificates with the following command:

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

If you are on Windows, you might see permission errors while setting up Mosquitto to run on TLS.

The mosquitto container can't read the cert files because of permission deny

In order to fix it, copy the contents of the folder "certs/archive" into another folder that you have created, so you own the permissions of the files.

    mkdir -p certs/public certs/private
    cp certs/archive/yourdomain/fullchain.pem certs/public/server-fullchain.crt
    cp certs/archive/yourdomain/cert.pem certs/public/server.crt
    cp certs/archive/yourdomain/chain.pem certs/public/server-chain.crt
    cp certs/archive/yourdomain/privkey.pem certs/private/server.key
    chmod 600 certs/private/server.key

don't forget to update the *.env* file with your public **hostname**
