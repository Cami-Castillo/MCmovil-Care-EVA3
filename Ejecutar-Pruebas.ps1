param([switch]$ConInterfaz)
. "$PSScriptRoot/tools/Entorno.ps1"
Maven -Argumentos @("verify")
Seleccionar-Java -ParaGradle
Push-Location "$PSScriptRoot/android"
try{
 & .\gradlew.bat :logic:jacocoTestCoverageVerification
 if($LASTEXITCODE -ne 0){throw "Fallaron los tests o el umbral de cobertura 80%."}
 if($ConInterfaz){& .\gradlew.bat connectedDebugAndroidTest;if($LASTEXITCODE -ne 0){throw "Fallaron las pruebas de pantalla"}}
}finally{Pop-Location}
Write-Host "Revisa los informes reales en target/site/jacoco y logic/build/reports/jacoco."
