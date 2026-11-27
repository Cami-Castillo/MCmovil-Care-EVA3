param([string]$CatalogoUrl="http://127.0.0.1:8081/",[string]$AtencionesUrl="http://127.0.0.1:8082/")
. "$PSScriptRoot/tools/Entorno.ps1"
foreach($url in @($CatalogoUrl,$AtencionesUrl)){
 if(-not $url.EndsWith("/")){throw "Las URL deben terminar en /"}
 if(-not($url.StartsWith("https://") -or $url.StartsWith("http://127.0.0.1:") -or $url.StartsWith("http://10.0.2.2:"))){throw "Usa HTTPS salvo la demostracion local."}
}
& "$PSScriptRoot/Ejecutar-Pruebas.ps1"
Seleccionar-Java -ParaGradle
Push-Location "$PSScriptRoot/android"
try{
 & .\gradlew.bat assembleRelease "-PCATALOGO_URL=$CatalogoUrl" "-PATENCIONES_URL=$AtencionesUrl"
 if($LASTEXITCODE -ne 0){throw "Fallo release"}
 Copy-Item app/build/outputs/apk/release/app-release.apk "$PSScriptRoot/entrega/MCmovilCare-EVA4-release.apk" -Force
}finally{Pop-Location}
Write-Host "Release generada. Instalala y comprueba todos los flujos."
