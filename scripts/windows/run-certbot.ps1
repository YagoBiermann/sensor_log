param(
    [string]$Domain,
    [string]$Email
)

if ([string]::IsNullOrEmpty($Domain)) {
    $Domain = Read-Host "Domain"
}

if ([string]::IsNullOrEmpty($Email)) {
    $Email = Read-Host "Email"
}

certbot certonly --standalone --non-interactive --agree-tos --email $Email -d $Domain

New-Item -ItemType Directory -Force -Path ".\certs\public" | Out-Null
Copy-Item "C:\Certbot\live\$Domain\fullchain.pem" -Destination ".\certs\public\server-fullchain.crt" -Force
Copy-Item "C:\Certbot\live\$Domain\chain.pem" -Destination ".\certs\public\server-chain.crt" -Force
Copy-Item "C:\Certbot\live\$Domain\cert.pem" -Destination ".\certs\public\server.crt" -Force
New-Item -ItemType Directory -Force -Path ".\certs\private" | Out-Null
Copy-Item "C:\Certbot\live\$Domain\privkey.pem" -Destination ".\certs\private\server.key" -Force

icacls ".\certs\public" /inheritance:r
icacls ".\certs\public" /grant:r "$($env:USERNAME):(OI)(CI)F"

icacls ".\certs\private\server.key" /inheritance:r
icacls ".\certs\private\server.key" /grant:r "$($env:USERNAME):F"

icacls ".\certs\public\server-fullchain.crt" ".\certs\public\server.crt" ".\certs\public\server-chain.crt" /grant:r "Everyone:R"