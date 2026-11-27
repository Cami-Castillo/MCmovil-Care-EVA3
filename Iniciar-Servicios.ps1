. "$PSScriptRoot/tools/Entorno.ps1"
Maven -Argumentos @("-pl","services/catalogo,services/atenciones","-am","package","-DskipTests")
$log=Join-Path $PSScriptRoot "logs";New-Item -ItemType Directory -Force $log | Out-Null
$iniciados=@()
foreach($servicio in @(@{Nombre="catalogo";Puerto=8081},@{Nombre="atenciones";Puerto=8082})){
 $nombre=$servicio.Nombre;$puerto=$servicio.Puerto
 if(Get-NetTCPConnection -LocalPort $puerto -State Listen -ErrorAction SilentlyContinue){throw "Puerto $puerto ocupado. No se detendra ningun programa ajeno. Usa Detener-Servicios.ps1 si es esta entrega."}
 $jar=Join-Path $PSScriptRoot "services/$nombre/target/$nombre-4.0.0.jar"
 $proceso=Start-Process -FilePath "$env:JAVA_HOME/bin/java.exe" -ArgumentList @("-Dserver.address=127.0.0.1","-jar",('"'+$jar+'"')) -WorkingDirectory "$PSScriptRoot/services/$nombre" -RedirectStandardOutput "$log/$nombre.log" -RedirectStandardError "$log/$nombre-error.log" -PassThru
 $iniciados+=@{Id=$proceso.Id;Jar=$jar;Nombre=$nombre}
 ConvertTo-Json -InputObject @($iniciados) | Set-Content "$log/procesos.json"
}
foreach($s in @(@{Nombre="catalogo";Puerto=8081},@{Nombre="atenciones";Puerto=8082})){
 $ok=$false
 for($i=0;$i -lt 40;$i++){try{$v=Invoke-RestMethod "http://localhost:$($s.Puerto)/api/salud" -TimeoutSec 2;if($v.estado -eq "OK"){$ok=$true;break}}catch{};Start-Sleep -Seconds 1}
 if(-not $ok){throw "No inicio $($s.Nombre). Revisa logs/$($s.Nombre)-error.log y logs/$($s.Nombre).log"}
 Write-Host "$($s.Nombre): OK en http://localhost:$($s.Puerto)/api/salud"
}
Write-Host "Ambos microservicios estan funcionando. Ahora ejecuta Conectar-Dispositivo.ps1."
