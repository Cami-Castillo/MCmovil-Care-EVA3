# Trazabilidad con el encargo EVA4

| Ítem | Implementación | Comprobación |
|---|---|---|
| Pantallas/formularios/flujos | Inicio, equipos, atención, edición, seguimiento, perfil/resumen, preferencias, agenda; estados de carga y error | Build Android, tests de UI y recorrido manual |
| Microservicios Spring Boot | Catálogo y Atenciones, procesos y H2 separados | Tests MockMvc + HTTP real entre procesos |
| Integración CRUD | GET/POST/PUT/DELETE desde formularios/listas; PATCH autorización y carga de imágenes | Pruebas de contratos y recorrido Android |
| API externa | Nager.Date desde Retrofit, pantalla Agenda | Consulta real y test de error aislado |
| 80% de lógica | JaCoCo con umbral automático en núcleo y servicios | Informes reales con alcance explícito |
| APK firmado | APK release y JKS de demo; verificación de certificado | apksigner y certificado público; instalación |
| GitHub/Trello | Workflow y planificación incluidos | La colaboración debe producirse en sus cuentas; no se fabrica |

No es una garantía de nota. La rúbrica se evalúa sobre su ejecución y defensa, y el caso completo incluye aspectos (administración dinámica de usuarios y aislamiento entre múltiples clientes, entre otros) que esta demo no implementa. Revisen estas limitaciones con el alcance definido por el equipo antes de entregar.

**Indicación adicional del profesor:** login antes de cualquier dato. Entrada en LoginScreen y protección de los endpoints de negocio y evidencias con AccesoFiltro.
