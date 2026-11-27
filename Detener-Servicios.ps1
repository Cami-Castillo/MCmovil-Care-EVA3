$ErrorActionPreference="Stop"
$archivo="$PSScriptRoot/logs/procesos.json"
if(-not(Test-Path $archivo)){Write-Host "No hay procesos registrados por esta entrega.";exit}
foreach($s in @(Get-Content $archivo -Raw | ConvertFrom-Json)){
 $p=Get-CimInstance Win32_Process -Filter "ProcessId=$($s.Id)" -ErrorAction SilentlyContinue
 if($p -and $p.CommandLine -like "*$($s.Jar)*"){Stop-Process -Id $s.Id;Write-Host "Detenido $($s.Nombre)"}
}
Remove-Item $archivo
