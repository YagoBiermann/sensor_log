ui = true

storage "file" {
  path = "/vault/data"
}

listener "tcp" {
  address = "[::]:8200"
  tls_disable = "false"
  tls_cert_file = "/vault/tls/vault-server.crt"
  tls_key_file  = "/vault/tls/vault-server.key"
  tls_client_ca_file = "/vault/tls/ca.crt"
}

api_addr = "https://vault:8200"

disable_mlock = true