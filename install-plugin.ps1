# Builds the plugin jar and drops it in ~/.runelite/local-plugins, where run-runelite-local.ps1 picks it up.
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
.\gradlew.bat jar --console=plain
if ($LASTEXITCODE -ne 0) { throw 'build failed' }
$dest = Join-Path $env:USERPROFILE '.runelite\local-plugins'
New-Item -ItemType Directory -Force $dest | Out-Null
$jar = Get-ChildItem build\libs -Filter *.jar | Sort-Object LastWriteTime -Descending | Select-Object -First 1
Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $dest 'BossHelper.jar') -Force
Write-Host "Installed $($jar.Name) -> $dest\BossHelper.jar"
