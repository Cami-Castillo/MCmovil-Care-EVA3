# Verificación de MCmovil Care con login

| Módulo | Pruebas correctas | Líneas lógicas cubiertas | Cobertura |
|---|---:|---:|---:|
| Lógica Android (incluye login) | 65 | 255/265 | 96.23% |
| Catálogo | 14 | 119/124 | 95.97% |
| Atenciones | 24 | 148/156 | 94.87% |

**103 pruebas ejecutadas sin fallos** con Maven. El umbral automático es 80%. La lógica de la app también pasó el control Gradle incluido en la compilación release. El informe mide validadores, estado, login, repositorio y lógica de backend. No equivale a cobertura de toda la interfaz ni de los adaptadores de Android.

- Debug, APK de pruebas instrumentadas y APK release compilados. Las 9 pruebas de interfaz se compilaron. No se acredita una ejecución completa y exitosa de la suite en dispositivo. Ejecutar en emulador o teléfono con Ejecutar-Pruebas.ps1 -ConInterfaz.
- APK firmado verificado por apksigner. Ver entrega/verificacion-firma.txt. Se instaló y abrió el APK release en un emulador Android API 29. La pantalla inicial de login se puede ver en evidencias/pantalla-login-emulador.png.
- Se iniciaron los dos JARs de Spring Boot y dos bases H2 en archivos separados. En HTTP se verificaron credenciales válidas e inválidas, 401 sin sesión (equipos, atenciones y fotos), 403 por rol, solicitud del cliente, cotización del técnico y aprobación del cliente. También se verificó el conflicto al borrar un equipo asociado, la revocación del token, la persistencia tras reiniciar los servidores y la eliminación de los registros.
- API externa Nager.Date: la respuesta para Chile/2026 se comprobó al preparar la integración. La pantalla Agenda la consulta con Retrofit y no envía el token interno.

Se incluyen informes en evidencias/cobertura y resultados de las pruebas. Ejecutar el recorrido del README en el equipo de la defensa, con el emulador o teléfono que se vaya a usar, y completar la colaboración real en GitHub y Trello. Las cuentas son ficticias y preconfiguradas; no hay administración dinámica de cuentas ni separación de registros de varios clientes.
