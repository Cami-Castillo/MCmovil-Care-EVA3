# MCmovil Care — actualización de interfaz azul

Esta actualización se creó sobre el ZIP proporcionado, que contiene exclusivamente los módulos `android` y `logic`. No incluye los microservicios, la configuración local, bases H2, scripts de PowerShell ni llaves de firma. No es un proyecto integral autónomo. Conserva tu proyecto original y sus servicios.

## Instalación en Windows
1. Detén los microservicios y cierra Android Studio.
2. Haz una copia completa de la carpeta de proyecto actual, por ejemplo `MCmovilCare-EVA4-Revision-Respaldo`.
3. Extrae este ZIP a una carpeta temporal. Copia las carpetas `android` y `logic` dentro de la raíz del proyecto actual, donde ya están `services`, `tools`, los scripts `.ps1` y `pom.xml`. Acepta combinar carpetas y reemplazar los archivos coincidentes. No elimines las carpetas originales antes de copiar; conserva `local.properties`, configuración de firma y bases de datos.
4. Abre `android` en Android Studio y sincroniza Gradle. Usa el Pixel 7 Android 15 (API 35).
5. Desde la raíz ejecuta `./Ejecutar-Pruebas.ps1` (PowerShell usa también `.\Ejecutar-Pruebas.ps1`). Después, desde `android`, ejecuta `.\gradlew.bat :app:connectedDebugAndroidTest`. Se esperan 10 pruebas de interfaz: las 9 existentes y una adicional de feriado nacional. No se han ejecutado en el entorno de edición.
6. Inicia los servicios con `.\Iniciar-Servicios.ps1` desde la raíz. Restablece el túnel ADB para los puertos 8081 y 8082 en el emulador, y ejecuta la app.

## Cambios
- Tema azul claro y oscuro, superficies blancas en tema claro, encabezados, tarjetas, botones, indicadores de estado, navegación, login con visibilidad de contraseña.
- Inicio con texto contextual por rol y contador de atenciones no finalizadas para técnico; no incorpora filtrado por propietario.
- Agenda mensual navegable, fechas seleccionables, mantenciones reales del estado, feriados nacionales de la API y avisos regionales. Sugiere un día hábil de referencia; no reprograma automáticamente ni confirma horarios de atención. Muestra una advertencia si faltan datos del año o falla la consulta.
- Cotizaciones formateadas en pesos chilenos, historial visual y campos/formularios refinados. Se mantiene el logo original.
- No se modificó el módulo `logic`, ni la persistencia Room, el ViewModel, los endpoints ni dependencias Gradle. Los filtros del backend permanecen en el proyecto original.
- Se conservaron las nueve pruebas y se ajustaron desplazamientos para la nueva distribución; se añadió una prueba del aviso de feriado. No se modificaron los criterios de cobertura.

## Verificación pendiente
Este ZIP fue revisado estáticamente, no compilado ni ejecutado. Verifica login de los tres roles, permisos HTTP, CRUD, navegación, agenda (incluido cambio de año y falla de red), cobertura, pruebas instrumentadas y APK firmado antes de entregar. Los mockups originales son referencia estética, no reproducciones exactas de estas pantallas.
