# =============================================================================
# sim.ps1 - Run the AdvantageKit simulator, then report the newest sim log.
#
# Usage (from repo root):
#     powershell -File .\tools\sim.ps1
#     .\tools\sim.ps1
#
# The simulator launches AdvantageScope (a GUI). Close it to finish the run;
# the produced log is written to logs/sim/akit_*.wpilog.
# =============================================================================
$ErrorActionPreference = 'Stop'

# --- Repo root (one level up from the script) ---------------------------------
$scriptDir = Split-Path -Parent $PSCommandPath
$root      = Split-Path -Parent $scriptDir
Set-Location $root

# --- Required JDK (WPILib 2026) ----------------------------------------------
$env:JAVA_HOME = 'C:\Users\Public\wpilib\2026\jdk'

Write-Host ''
Write-Host '==> simulateJava (AdvantageKit simulator). Close the sim when done.' -ForegroundColor Cyan

cmd /c ".\\gradlew.bat simulateJava --console=plain"
Write-Host ''
Write-Host 'Sim exited with code:' -NoNewline
Write-Host $LASTEXITCODE -ForegroundColor Yellow

Write-Host '==> Newest sim log:' -ForegroundColor Cyan
$simDir = Join-Path $root 'logs\sim'
$log = Get-ChildItem $simDir -File -Filter '*.wpilog' -ErrorAction SilentlyContinue |
    Sort-Object -Property LastWriteTime -Descending | Select-Object -First 1

if ($null -ne $log) {
    Write-Host $log.FullName
    Write-Host ('  size: {0:N0} bytes, modified {1}' -f $log.Length, $log.LastWriteTime)
} else {
    Write-Host 'No logs\sim\*.wpilog found yet.' -ForegroundColor Magenta
}

exit 0