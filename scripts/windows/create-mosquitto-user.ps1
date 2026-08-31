# This script creates a new user for the Mosquitto MQTT broker and stores the credentials in a password file with proper hashing.

param(
    [string]$User,
    [string]$Pass
)

if ((Split-Path -Leaf $PWD) -ne "sensor_log") {
    Write-Host "Please run this script from the sensor_log directory." -ForegroundColor Red
    exit 1
}

while ([string]::IsNullOrEmpty($User)) {
    $User = Read-Host "type a username"
}

while ([string]::IsNullOrEmpty($Pass)) {
    $SecurePass = Read-Host "password" -AsSecureString
    $Pass = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecurePass)
    )
}

New-Item -ItemType File -Path .\mosquitto.passwd -Force | Out-Null

docker run --rm -it `
  -v "${PWD}:/mosquitto/config" `
  eclipse-mosquitto `
  sh -c "mosquitto_passwd -b /mosquitto/config/mosquitto.passwd '$User' '$Pass' && chmod 0700 /mosquitto/config/mosquitto.passwd"