$ErrorActionPreference="Stop"
$script:Raiz=Split-Path $PSScriptRoot -Parent
function Seleccionar-Java {
 param([switch]$ParaGradle)
 $candidatos=@()
 if($env:JAVA_HOME){$candidatos+=Join-Path $env:JAVA_HOME "bin/javac.exe"}
 $bases=@("C:/Program Files/Java","C:/Program Files/Eclipse Adoptium","C:/Program Files/Android/Android Studio","$env:USERPROFILE/.jdks")
 foreach($base in $bases){if(Test-Path $base){$candidatos+=Get-ChildItem $base -Filter javac.exe -Recurse -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName}}
 foreach($c in $candidatos){if(Test-Path $c){$texto=(& $c -version 2>&1 | Out-String);if($texto -match 'javac (\d+)' -and [int]$Matches[1] -ge 21 -and (-not $ParaGradle -or [int]$Matches[1] -eq 21)){$env:JAVA_HOME=Split-Path (Split-Path $c -Parent) -Parent;Write-Host "JDK seleccionado: $env:JAVA_HOME";return}}}
 if($ParaGradle){throw "Para reconstruir con Gradle usa un JDK 21 completo y configura JAVA_HOME."}
 throw "Necesitas un JDK completo 21 o superior. Un acceso Oracle javapath no es el JDK."
}
function Maven([string[]]$Argumentos) {
 Seleccionar-Java
 $version="3.9.11";$base=Join-Path $env:LOCALAPPDATA "MCare-tools";$mvn=Join-Path $base "apache-maven-$version/bin/mvn.cmd"
 if(-not(Test-Path $mvn)){
  New-Item -ItemType Directory -Force $base | Out-Null
  $zip=Join-Path $base "maven.zip";$url="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$version/apache-maven-$version-bin.zip"
  Invoke-WebRequest $url -UseBasicParsing -OutFile $zip
  $hash=(Invoke-WebRequest "$url.sha512" -UseBasicParsing).Content.Trim().Split(' ')[0]
  if((Get-FileHash $zip -Algorithm SHA512).Hash -ine $hash){throw "SHA512 de Maven no coincide"}
  Expand-Archive $zip $base -Force;Remove-Item $zip
 }
 Push-Location $script:Raiz
 try{& $mvn @Argumentos;if($LASTEXITCODE -ne 0){throw "Maven devolvio un error. Revisa la salida."}}finally{Pop-Location}
}
function Adb {
 $cmd=Get-Command adb.exe -ErrorAction SilentlyContinue
 if($cmd){return $cmd.Source}
 foreach($base in @($env:ANDROID_HOME,$env:ANDROID_SDK_ROOT,"$env:LOCALAPPDATA/Android/Sdk")){
  if($base){$p=Join-Path $base "platform-tools/adb.exe";if(Test-Path $p){return $p}}
 }
 throw "No se encontro adb. Instala Platform Tools desde Android Studio > SDK Manager."
}
