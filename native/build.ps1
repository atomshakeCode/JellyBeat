$ErrorActionPreference = 'Stop'
$compiler = Join-Path $env:WINDIR 'Microsoft.NET/Framework64/v4.0.30319/csc.exe'
$target = Join-Path $PSScriptRoot '../src/main/resources/native/windows-x64/jellybeat-capture.exe'
& $compiler /nologo /optimize+ /platform:x64 /target:winexe "/out:$target" (Join-Path $PSScriptRoot 'Capture.cs')
if ($LASTEXITCODE -ne 0) { throw 'WASAPI helper build failed' }
