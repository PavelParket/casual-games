# Local build prep: publishToMavenLocal + prepare build context.
#
# Usage:
#   .\deploy\build-local.ps1                  - full cycle
#   .\deploy\build-local.ps1 -SkipPublish     - skip publishToMavenLocal

param(
    [switch]$SkipPublish
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path $PSScriptRoot -Parent
$CommonUtilsDir = Join-Path $ProjectRoot "backend\common-utils"

function Write-Step($msg)
{
    Write-Host "[$( Get-Date -Format 'HH:mm:ss' )] $msg" -ForegroundColor Cyan
}
function Write-Ok($msg)
{
    Write-Host "  OK: $msg" -ForegroundColor Green
}
function Write-Warn($msg)
{
    Write-Host "  WARN: $msg" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "  casual-games - local build prep" -ForegroundColor White
Write-Host ""

# --- Step 1: publishToMavenLocal ----------------------------------------------
if (-not $SkipPublish)
{
    Write-Step "1/2  publishToMavenLocal (common-utils)..."
    Push-Location $CommonUtilsDir
    try
    {
        & .\gradlew.bat publishToMavenLocal -q
        if ($LASTEXITCODE -ne 0)
        {
            throw "publishToMavenLocal failed (exit $LASTEXITCODE)"
        }
        Write-Ok "common-utils published to local Maven"
    }
    finally
    {
        Pop-Location
    }
}
else
{
    Write-Warn "Step 1 skipped (-SkipPublish)"
}

# --- Step 2: prepare-build (copy artifacts to .m2-common-utils) --------------
Write-Step "2/2  Preparing build context..."
& "$PSScriptRoot\prepare-build.ps1"

Write-Host ""
Write-Host "  Done. Ready to commit and push." -ForegroundColor Green
Write-Host ""
