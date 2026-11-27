# Revisión 4.2 — estructura y diseño

Base: versión EVA4 con login que ya se ejecutó en Windows; no el ZIP anterior sin backend. Guías 14 y 15: ui/screens, viewmodel, data/model, data/remote, repository. Los últimos tres paquetes viven en logic compartido para conservar las pruebas JVM. Se añadió RetrofitInstance y se separaron formularios y pantallas. Los servicios Java mantienen su estructura anterior para evitar romper el acceso entre clases. Se corrigió Map.of con motivo nulo en ambos manejadores.

## Validar antes de entregar
- Ejecutar servicios, conectar ADB y ejecutar Android debug. Probar los tres roles, CRUD, cotizaciones, fotos, caché, feriados.
- Ejecutar `Ejecutar-Pruebas.ps1 -ConInterfaz`; comprobar cobertura >=80%. La fábrica Retrofit nueva entra en la medición y puede reducir el porcentaje. No asumir el resultado anterior.
- Crear APK release firmado, verificar instalación, guardar evidencia actual. Completar número de equipo, README y Trello real. Hacer commits auténticos de cada integrante.
- Pendientes del caso: gestión dinámica de cuentas, aislamiento por propietario, notificaciones Android. No se han implementado en esta revisión. No usar datos reales.
- No modificar el repositorio evaluado después del plazo; usar copia local para practicar o solicitar permiso docente.
