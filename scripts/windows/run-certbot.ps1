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

New-Item -ItemType Directory -Force -Path ".\certs" | Out-Null
Copy-Item "C:\Certbot\live\$Domain\*.pem" -Destination ".\certs\" -Force

icacls ".\certs" /inheritance:r
icacls ".\certs" /grant:r "$($env:USERNAME):(OI)(CI)F"

icacls ".\certs\privkey.pem" /inheritance:r
icacls ".\certs\privkey.pem" /grant:r "$($env:USERNAME):F"

icacls ".\certs\fullchain.pem" ".\certs\cert.pem" ".\certs\chain.pem" /grant:r "Everyone:R"