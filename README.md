# 1. Nombre del proyecto

**MCmovil Care** — Evaluación Parcial 4 (Experiencia 3), DSY1105 Desarrollo de Aplicaciones Móviles.

MVP académico del seguimiento del ciclo de vida de equipos: app Android (Kotlin + Jetpack Compose) conectada a dos microservicios Spring Boot propios y a una API externa de feriados. Solo usa datos ficticios; no se conecta a Gestioo.

# 2. Integrantes

- Alejandrina Farias
- Camila Castillo


# 3. Funcionalidades

- Inicio con contadores calculados a partir de los registros y aviso de autorizaciones pendientes.
- Equipos: consulta para los tres roles; creación, edición y eliminación reservadas al administrador.
- Atenciones: crear, consultar, editar y eliminar; estados, cotización ficticia, autorización/rechazo, historial, informe, garantía y próxima mantención.
- Evidencias: captura con cámara o selección, vista previa, carga real al servicio de atenciones y consulta de la imagen guardada.
- Login obligatorio y roles validados en servidor. Perfil de contacto independiente, de almacenamiento local y con validación por campo.
- Preferencia local persistente para mostrar diagnósticos.
- Agenda con calendario interactivo y reprogramación de mantenciones, con feriados reales de Chile desde Nager.Date, vía Retrofit desde Android; cálculo de día hábil nacional de referencia y mantenciones ya registradas. Los feriados regionales se distinguen y no se tratan como nacionales.
- Room conserva copia de equipos/atenciones para consulta sin conexión. Sin conexión se deshabilitan los cambios remotos: no se afirma que exista una cola de sincronización. Perfil y preferencia se conservan localmente.
- Estados de carga, errores, reintento, validación por campo, confirmación de borrado y animación de detalles.

## Organización y arquitectura

- `android/app`: Compose/Material 3, navegación, adaptación al ancho disponible, CareViewModel, cámara, FileProvider y adaptador Room.
- `logic`: **lógica activa compartida**, utilizada por CareViewModel: modelos, validaciones, contratos Retrofit, RemoteCareRepository y CareController (estado y acciones asíncronas). No es un ejemplo independiente sin conexión con la app.
- `services/catalogo`: aplicación Spring Boot independiente y H2 propia de equipos.
- `services/atenciones`: aplicación Spring Boot independiente y H2 propia de atenciones/historial; archivos de evidencias en su volumen.
- `verificacion-logica`: ejecuta los mismos fuentes Kotlin de logic mediante Maven, con Kotest/MockK/coroutines-test y JaCoCo; no mantiene copias alternativas de la lógica.

Las bases están en `services/catalogo/datos` y `services/atenciones/datos`. No las borres si deseas conservar registros. Los tests usan otras bases en memoria. Las fotos están en `services/atenciones/datos/evidencias`.

## Acceso de demostración

Al abrir la app solo aparece **Iniciar sesión**. Inicia los dos servicios antes de ingresar:

| Usuario | Contraseña ficticia | Uso |
|---|---|---|
| `cliente` | `ClienteDemo26!` | Consulta equipos, crea y consulta solicitudes y responde cotizaciones. |
| `tecnico` | `TecnicoDemo26!` | Consulta equipos y actualiza estados, informes y evidencias de las atenciones. |
| `admin` | `AdminDemo26!` | Gestiona equipos y atenciones, incluyendo eliminación y autorización. |

Son cuentas de prueba preconfiguradas; no existe registro de usuarios ni administración de cuentas desde la app. Los roles operan sobre un conjunto compartido de datos ficticios (no hay aislamiento de múltiples clientes reales). No uses información real ni expongas esta demo públicamente. Puedes cambiar las contraseñas en el servidor con las variables MCCARE_CLAVE_CLIENTE, MCCARE_CLAVE_TECNICO y MCCARE_CLAVE_ADMIN.

Catálogo comprueba las credenciales y devuelve un token temporal. Retrofit lo envía a las dos API propias en `Authorization: Bearer ...`; nunca se adjunta al servicio externo de feriados. Ambos servicios bloquean el acceso anónimo a los datos. Los permisos se validan en el backend además de reflejarse en la interfaz. El token no se guarda en Room: si se cierra el proceso de la app o se reinicia Catálogo, hay que ingresar otra vez. La sesión del servidor vence en 8 horas o al recibir el cierre de sesión. Si cierras sin conexión, el cierre local es inmediato y el servidor conserva esa sesión hasta que venza.

# 4. Endpoints utilizados

Catálogo: `http://localhost:8081/`
- GET `/api/salud` (público)
- POST `/api/sesion`: recibe usuario/clave y responde token, usuario y rol.
- GET `/api/sesion`: identifica la sesión activa.
- DELETE `/api/sesion`: revoca la sesión.
- GET/POST `/api/equipos`
- GET/PUT/DELETE `/api/equipos/{id}`

Atenciones: `http://localhost:8082/`
- GET `/api/salud`
- GET/POST `/api/atenciones`
- GET/PUT/DELETE `/api/atenciones/{id}`
- PATCH `/api/atenciones/{id}/autorizacion`
- POST `/api/evidencias` (multipart, campo archivo)
- GET `/api/evidencias/{nombre}`
- GET `/internal/equipos/{id}/en-uso` (comprobación entre servicios)

La creación/edición de atención consulta el catálogo. El borrado de equipo consulta las referencias de atenciones; devuelve 409 si hay referencias y 503 si no puede comprobarlas. No se simulan dos servicios dentro de una única base: son dos procesos y dos almacenes. Esta demostración no implementa transacciones distribuidas ni coordinación de carreras concurrentes entre procesos.

API externa: `https://date.nager.at/api/v3/PublicHolidays/{año}/CL`. Se consulta directamente con Retrofit desde la pantalla Agenda. No guarda ni reemplaza datos del negocio. Si falla, las otras pantallas siguen funcionando; el cálculo advierte cuando solo está excluyendo fines de semana. No planifiques años distintos del que muestra la consulta.

# 5. Pasos para ejecutar

1. Extrae el ZIP COMPLETO; no copies solo android. La raíz debe mostrar android, logic, services y pom.xml. Las carpetas firma-demo y entrega pueden requerir creación si se clona el repositorio público.
2. En esa raíz, escribe `powershell` en la barra de direcciones del Explorador. Ejecuta `.\Iniciar-Servicios.ps1`. Descarga Maven si falta y selecciona un JDK completo >=21; evita el acceso de Oracle javapath. No cambia la configuración global del PC. Los puertos son **8081 y 8082**, no 8080.
3. Abre `http://localhost:8081/api/salud` y `http://localhost:8082/api/salud`. Ambos deben devolver OK.
4. Enciende un emulador Android API 26 o superior, o conecta un teléfono con depuración USB. Ejecuta `.\Conectar-Dispositivo.ps1` para establecer ambos puertos; ejecuta la app desde Android Studio. Para instalar el APK firmado, usa `.\Instalar-APK.ps1`. Es necesario repetir el reverse tras desconectar el teléfono/emulador.
5. Si quieres editar el código, abre **la carpeta android** en Android Studio, no la raíz de entrega. Espera el Sync y ejecuta app. Debug tiene un applicationId diferente para poder coexistir con release y con la entrega anterior.
6. Al terminar: `.\Detener-Servicios.ps1` detiene solo los procesos registrados por este proyecto.

Si PowerShell bloquea scripts y la política del instituto lo permite, usa `Set-ExecutionPolicy -Scope Process Bypass` solo en esa ventana. No hace falta ejecutarlo si ya están permitidos. Si una directiva institucional lo impide, no se modifica esa directiva: ejecuta los comandos Maven y Java equivalentes desde el IDE/terminal autorizado.

## Requisitos técnicos

- Backend: JDK completo 21 o superior. Para probar o reconstruir Android desde Gradle usa JDK 21 (o el JBR 21 de Android Studio) y configura JAVA_HOME. Un JDK 25 puede servir para iniciar el backend y probar el APK ya firmado.
- Android: Gradle 8.13, AGP 8.13.2, Kotlin 2.2.21, compileSdk/targetSdk 35, minSdk 26. Se abandonaron los números futuros del ZIP anterior para poder comprobar la compilación.
- Android Studio con SDK Platform 35, Build Tools 35.0.0 y Platform Tools. El APK release de esta revisión fue generado y su firma verificada.
- Internet para la API externa y las descargas iniciales. Los microservicios locales no requieren internet una vez construidos.

## Prueba manual de todos los flujos

1. Abre la app: solo debe aparecer el login. Prueba una contraseña inválida y luego entra como administrador. Registra tus datos ficticios de contacto y comprueba el resumen.
2. Registra un equipo (nombre, marca, modelo, serie, estado y diagnóstico).
3. Edítalo y comprueba el cambio tras Actualizar.
4. Cierra sesión e ingresa como cliente. Crea una atención para ese equipo. Prueba primero descripción corta y correo inválido.
5. Adjunta una foto ficticia pequeña; guarda y abre los detalles para comprobarla desde el servidor.
6. Pulsa **Salir** y entra como técnico. En **Editar atención**, cambia el estado a Cotizada y agrega el monto y el informe. Guarda.
7. Cierra la sesión técnica y vuelve a entrar como cliente. Autoriza desde la tarjeta. Verifica el historial y el contador de pendientes.
8. Ingresa como técnico o administrador. Edita estado, garantía y próxima mantención. Consulta Agenda y sus feriados.
9. Cambia la preferencia de diagnósticos; cierra y abre la app.
10. Detén y reinicia ambos servicios; vuelve a entrar. Los registros e historial deben permanecer.
11. Ingresa como administrador. Prueba eliminar el equipo con atención: debe impedirse. Elimina atención y luego equipo.
12. Después de haber cargado datos, detén servicios y pulsa Actualizar: debe mostrar copia local y bloquear edición. Enciéndelos y reintenta.

La eliminación de atenciones no borra inmediatamente las imágenes huérfanas del servidor. La demo limita archivos a 5 MiB y valida que sean imágenes; no incluye una política de limpieza de archivos ni un almacenamiento cloud de evidencias.

## Tests y cobertura

Resultados de las ejecuciones del 27/11/2026:

| Comprobación | Resultado |
|---|---|
| ControllerTest | 19/19 |
| LoginTest | 4/4 |
| RepositoryTest | 7/7 |
| ValidacionTest | 35/35 |
| Total lógica | 65/65, sin fallos ni errores |
| Pruebas instrumentadas en Pixel 7, Android 15 | 10/10 |
| Maven: Catálogo, Atenciones y verificación de lógica | BUILD SUCCESS |
| Control de cobertura JaCoCo | Aprobado |
| Acceso HTTP sin sesión | 2/2 (401) |
| Consultas y restricciones generales por rol | 13/13 (200 o 403 según operación) |

Las comprobaciones HTTP no constituyen una auditoría integral de seguridad ni verifican aislamiento entre clientes. La cobertura de lógica no equivale a cobertura de todo el código Android. Los informes originales permiten consultar los porcentajes exactos y las exclusiones.


`.\Ejecutar-Pruebas.ps1` ejecuta la suite Maven y el control de cobertura Gradle. Con emulador: `.\Ejecutar-Pruebas.ps1 -ConInterfaz`.

Herramientas: Kotest para lógica, MockK para dependencias reemplazadas, coroutines-test para asincronía y JUnit Jupiter/MockMvc para backend. Las pruebas de pantalla usan **JUnit 4 con ComposeTestRule**, como en la guía de Android. JUnit Jupiter se utiliza en el backend.

JaCoCo comprueba automáticamente mínimo **80% de líneas** en cada módulo lógico. Se incluyen controlador, validaciones y repositorio; los DTO/entidades sin reglas y puntos de arranque se excluyen de forma explícita. **La cifra de logic incluye LoginController pero no la interfaz Compose ni los adaptadores Android de Room/cámara**; esos componentes se comprueban con compilación, tests de UI y pruebas de integración/manuales. Ver `VERIFICACION.md` para los números realmente obtenidos, alcance y limitaciones. No se presenta número de tests como porcentaje de cobertura.

Informes Maven: `services/*/target/site/jacoco/index.html` y `verificacion-logica/target/site/jacoco/index.html`. Informe Gradle: `logic/build/reports/jacoco/test/html/index.html`. Guarda los nuevos XML y capturas en `evidencias`.

# 6. APK release y firma digital

1. Android Studio: Build > Generate Signed Bundle / APK > APK > Create new. Elige una llave JKS académica, alias y contraseñas propias; no las publiques.
2. Copia `android/keystore.properties.example` a `android/keystore.properties` y completa la ruta relativa a android, alias y contraseñas.
3. Ejecuta `./Generar-Release.ps1` desde la raíz. El script ejecuta pruebas y genera `entrega/MCmovilCare-EVA4-release.apk`.
4. Verifica la firma con `apksigner verify --print-certs -v`, guarda la salida y las capturas reales en evidencias. Instala con `./Instalar-APK.ps1`.
5. Entrega llave y contraseñas por canal privado acordado con el docente. No reutilices la llave académica en producción. Si instalaste una versión firmada con otra llave, desinstálala primero si no necesitas sus datos.

## Resultados verificados de la firma (27/11/2026)

- APK: `entrega/MCmovilCare-EVA4-release.apk`, versión 4.2.
- Llave académica: `firma-demo/mccare-academico.jks`, alias `mccare` (entrega privada).
- `apksigner verify --verbose --print-certs`: `Verifies`, esquema v2 válido, un firmante, RSA de 2048 bits.
- Huella SHA-256 del certificado, coincidente con la llave JKS: `d45a3b77e95473b83d2c55cdd17934bcc5a1e1b809fb89153546f518f7f2c2c3`.
- Instalación del APK en Pixel 7 (Android 15): `Success`. Inicio de actividad confirmado. La prueba funcional completa de release con servicios activos queda pendiente de confirmación.

**Evidencias pendientes de adjuntar:** capturas del APK, del archivo JKS (sin credenciales), de la verificación de firma, de los informes de pruebas y de Trello. No publicar la llave privada ni `keystore.properties`.

## Documentación adicional

`MATRIZ_RUBRICA.md`, `VERIFICACION.md`, `GUIA_DEFENSA.md`, `docs/ENDPOINTS.http`, `docs/GUIAS_APLICADAS.md`, `TRELLO.md`.

## Revisión de estructura y diseño
Los paquetes de lógica están en `logic/src/main/kotlin/com/mcmovilcare/logic/{data/model,data/remote,repository,domain}`; se mantienen en un módulo compartido para que las pruebas JVM ejecuten los mismos fuentes. Las pantallas Android están en `ui/screens`, navegación en `ui`, adaptador Room en `data`, ViewModels en `viewmodel`. Se mantiene el backend original por compatibilidad, con corrección del manejador de errores nulos.

El APK release fue generado, firmado y verificado el 27 de noviembre de 2026. Su instalación y apertura en el emulador fueron exitosas; falta confirmar el flujo completo conectado al backend después del último intento de arranque de Catálogo. Las cuentas demo siguen compartiendo registros; gestión dinámica de usuarios y notificaciones del sistema quedan pendientes.
