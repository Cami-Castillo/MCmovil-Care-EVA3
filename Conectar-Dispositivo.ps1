param([string]$Serial="")
. "$PSScriptRoot/tools/Entorno.ps1"
$adb=Adb;$dispositivo=@();if($Serial){$dispositivo=@("-s",$Serial)}
& $adb @dispositivo reverse tcp:8081 tcp:8081
if($LASTEXITCODE -ne 0){throw "Conecta un telefono USB con depuracion o enciende un emulador. Si hay varios, usa -Serial."}
& $adb @dispositivo reverse tcp:8082 tcp:8082
if($LASTEXITCODE -ne 0){throw "No se pudo mapear 8082"}
Write-Host "Dispositivo conectado a catalogo 8081 y atenciones 8082."
