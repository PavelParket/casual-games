# Подготавливает common-utils для Docker-билда.
# Запускать ПЕРЕД docker build / docker compose build из корня проекта.
#
# Использование:
#   .\deploy\prepare-build.ps1

$ErrorActionPreference = "Stop"

$M2Source = "$env:USERPROFILE\.m2\repository\com\casualgames"
$Dest = Join-Path $PSScriptRoot "..\backend\.m2-common-utils"

if (-not (Test-Path $M2Source))
{
    Write-Error @"
Source not found: $M2Source

Run first (из backend\common-utils):
  .\gradlew publishToMavenLocal
"@
    exit 1
}

Write-Host "-> Cleaning $Dest"
if (Test-Path $Dest)
{
    Remove-Item -Recurse -Force $Dest
}
New-Item -ItemType Directory -Path $Dest | Out-Null

Write-Host "-> Copying common-utils artifacts from $M2Source"
Copy-Item -Path "$M2Source\*" -Destination $Dest -Recurse

Write-Host "OK. Ready to docker build."
