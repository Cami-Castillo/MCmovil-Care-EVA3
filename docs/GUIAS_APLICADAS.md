# Lo visto en clases, aplicado a MCmovil Care

| Guía o tema | Dónde buscarlo | Qué mostrar en la defensa |
|---|---|---|
| 7 · MVVM y colaboración | `android/app/src/main/java/com/example/appmcmovilcare_castillo_farias/viewmodel`, `logic/src/main/kotlin/com/mcmovilcare/logic`, `TRELLO.md` | La pantalla envía eventos al ViewModel. El controlador gestiona estado y el repositorio lee/escribe datos. Mostrar commits y tarjetas reales del equipo. |
| 8 · Compose y pantalla base | `ui/LoginScreen.kt`, `ui/CareApp.kt`, `ui/theme` dentro del paquete Android | Formulario de entrada, pantalla de inicio y tema Material 3. |
| 9 · Adaptabilidad | `ui/LoginScreen.kt` y `ui/CareApp.kt` | El formulario permite desplazar la pantalla con teclado visible y limita el ancho en pantallas grandes. |
| 10 · Navegación | `ui/CareApp.kt` | Scaffold, barra superior e inferior y NavHost. La navegación interna solo se crea después del login. |
| 11 · Formularios y validación | `ui/LoginScreen.kt`, `ui/CareApp.kt`, `logic/.../Validacion.kt` | Probar campos vacíos, correo incorrecto y una descripción válida. |
| 12 · Animación y estados | `ui/CareApp.kt`, `ui/LoginScreen.kt` | Indicador al esperar el servidor y animación al desplegar el historial. |
| 13 · Cámara | `ui/components/AdjuntoImagen.kt` y manifiesto Android | Tomar o seleccionar una foto ficticia; verla adjunta a una atención. |
| 14 · Retrofit y API REST | `logic/.../Api.kt`, `RemoteCareRepository.kt`, `services/` | Mostrar cómo se crean y consultan solicitudes; abrir Agenda para ver la API externa. |
| 15 · Pruebas | `logic/src/test`, `services/*/src/test`, `android/app/src/androidTest` | Validar que la app no entra sin credenciales, mostrar permisos y abrir el informe JaCoCo. |
| Coordinación backend y firma | `services/catalogo`, `services/atenciones`, `entrega/`, `firma-demo/` | Dos procesos y dos bases independientes. Mostrar el APK y el certificado público; entregar la llave y contraseñas solo por el canal acordado. |

`ui/` y `viewmodel/` son subcarpetas de `android/app/src/main/java/com/example/appmcmovilcare_castillo_farias/`. `logic/.../` se refiere a `logic/src/main/kotlin/com/mcmovilcare/logic/`.
