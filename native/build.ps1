param([string]$OutputPath)
$ErrorActionPreference = 'Stop'
$compiler = Join-Path $env:WINDIR 'Microsoft.NET/Framework64/v4.0.30319/csc.exe'
if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $target = Join-Path $PSScriptRoot '../src/main/resources/native/windows-x64/jellybeat-capture.exe'
} elseif ([System.IO.Path]::IsPathRooted($OutputPath)) {
    $target = $OutputPath
} else {
    $target = Join-Path $PSScriptRoot $OutputPath
}
$target = [System.IO.Path]::GetFullPath($target)
New-Item -ItemType Directory -Force -Path (Split-Path $target) | Out-Null
& $compiler /nologo /optimize+ /platform:x64 /target:winexe "/out:$target" (Join-Path $PSScriptRoot 'Capture.cs')
if ($LASTEXITCODE -ne 0) { throw 'WASAPI helper build failed' }
Get-FileHash -LiteralPath $target -Algorithm SHA256 | Format-List
