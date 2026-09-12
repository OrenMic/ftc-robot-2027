# =============================================================================
# verify.ps1 - Verification-first gate: Spotless + compile + tests.
#
# Usage (from repo root):
#     powershell -File .\tools\verify.ps1
#     .\tools\verify.ps1
#
# Exits non-zero if any step fails, so it can be used in hooks/CI.
# =============================================================================
$ErrorActionPreference = 'Stop'

# --- Repo root (one level up from the script, i.e. ./tools/../) --------------
$scriptDir = Split-Path -Parent $PSCommandPath
$root      = Split-Path -Parent $scriptDir
Set-Location $root

# --- Required JDK (WPILib 2026) ----------------------------------------------
$env:JAVA_HOME = 'C:\Users\Public\wpilib\2026\jdk'

Write-Host ''
Write-Host '==> verify: spotlessApply + compileJava + test' -ForegroundColor Cyan

cmd /c ".\\gradlew.bat spotlessApply compileJava test --console=plain"
if ($LASTEXITCODE -ne 0) {
    Write-Host ''
    Write-Host 'VERIFY FAILED (exit code below)' -ForegroundColor Red
    Write-Host $LASTEXITCODE -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host ''
Write-Host 'VERIFY PASSED' -ForegroundColor Green
exit 0