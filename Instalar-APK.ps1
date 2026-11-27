param([string]$Serial="")
. "$PSScriptRoot/tools/Entorno.ps1"
& "$PSScriptRoot/Conectar-Dispositivo.ps1" -Serial $Serial
$adb=Adb;$opciones=@();if($Serial){$opciones=@("-s",$Serial)}
& $adb @opciones install -r "$PSScriptRoot/entrega/MCmovilCare-EVA4-release.apk"
if($LASTEXITCODE -ne 0){throw "Instalacion fallida. Revisa el mensaje de adb; no se borraron datos automaticamente."}
& $adb @opciones shell monkey -p com.example.appmcmovilcare_castillo_farias.eva4 1
