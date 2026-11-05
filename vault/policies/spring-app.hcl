path "pki_int/sign/device" {
  capabilities = ["create", "update"]
}

path "pki_int/issue/broker" {
  capabilities = ["create", "update"]
}

path "pki_int/cert/ca_chain" {
  capabilities = ["read"]
}

path "pki_int/revoke" {
  capabilities = ["create", "update"]
}