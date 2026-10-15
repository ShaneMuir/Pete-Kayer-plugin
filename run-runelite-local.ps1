# Builds the loader, then starts the official RuneLite client with every jar in ~/.runelite/local-plugins registered.
# No developer mode. Close any other RuneLite / 1Up client first (one login at a time).
param([switch]$Foreground)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$runelite = Join-Path $env:USERPROFILE '.runelite'
$repo = Join-Path $runelite 'repository2'
$java = Join-Path $env:LOCALAPPDATA 'RuneLite\jre\bin\java.exe'
$javac = (Get-Command javac -ErrorAction Stop).Source
$out = Join-Path $root 'loader\build'

# Newest version of each client dependency in the launcher's download folder.
$pattern = '^(?<n>.+?)-(?<v>\d+(\.\d+)*)(?<s>.*)\.jar$'
$latest = @{}
foreach ($f in Get-ChildItem $repo -Filter *.jar) {
	if ($f.Name -notmatch $pattern) { continue }
	$key = $Matches.n + $Matches.s
	$ver = [version]($Matches.v + $(if ($Matches.v -notmatch '\.') { '.0' } else { '' }))
	if (-not $latest.ContainsKey($key) -or $ver -gt $latest[$key].Ver) {
		$latest[$key] = [pscustomobject]@{ Ver = $ver; Path = $f.FullName }
	}
}
$classpath = ($latest.Values | ForEach-Object { $_.Path })

New-Item -ItemType Directory -Force $out | Out-Null
& $javac -d $out -cp ($classpath -join ';') --release 11 (Join-Path $root 'loader\src\com\bosshelper\loader\LocalPluginLoader.java')
if ($LASTEXITCODE -ne 0) { throw 'loader compile failed' }

# loadBuiltin insists assertions are on; enabling them for just that class satisfies it without slowing the client.
$argList = @('-Xmx1536m', '-Xss2m', '-ea:net.runelite.client.externalplugins.ExternalPluginManager', '-cp', (($classpath + $out) -join ';'), 'com.bosshelper.loader.LocalPluginLoader') + $args
if ($Foreground) {
	& $java @argList
} else {
	Start-Process -FilePath $java -ArgumentList $argList -WindowStyle Hidden
	Write-Host 'RuneLite starting. Logs: ~\.runelite\logs\client.log'
}
