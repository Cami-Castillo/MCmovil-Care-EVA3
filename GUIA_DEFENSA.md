# Explicación breve para la defensa

“MCmovil Care centraliza equipos y atenciones del cliente en un MVP académico. Usamos Kotlin, Compose y Material 3. CareViewModel integra el ciclo de vida Android y delega la lógica al CareController del módulo logic, que expone StateFlow. El repositorio utiliza Retrofit para los dos microservicios Spring Boot. Catálogo tiene su propia base de equipos; Atenciones conserva solicitudes, historial y evidencias en otra base. Room mantiene una copia local de consulta. Agenda utiliza una tercera API externa, Nager.Date, para mostrar feriados chilenos sin modificar datos del negocio.”

- **Ruta:** identifica el destino; NavHost registra pantallas y NavController administra la navegación en ui/CareApp.kt.
- **Scaffold:** organiza barra superior, navegación inferior y contenido. Se aplica su padding al contenedor.
- **CRUD:** GET consulta, POST crea, PUT reemplaza los campos editables, DELETE elimina. PATCH autorización cambia una parte de la atención.
- **Validación doble:** Validacion.kt evita solicitudes inválidas desde la app; los DTO y servicios backend vuelven a comprobar reglas. El cliente no es la única barrera.
- **Persistencia:** dos H2 en archivo para el servidor y Room en el dispositivo. Reiniciar el backend no borra las bases en archivo.
- **Cobertura:** se mide con JaCoCo, no contando tests. Abre el informe HTML real y explica sus exclusiones y alcance. No digas 100% de toda la aplicación si el reporte es del núcleo lógico.
- **API externa:** no es uno de nuestros microservicios ni una lista inventada; Retrofit consulta Nager.Date y muestra su respuesta en Agenda.
- **Firma:** el applicationId identifica el paquete y el certificado permite verificar la firma y compatibilidad de actualizaciones. Keystore es el almacén, alias identifica la entrada y las contraseñas protegen almacén y clave.
- **Límites:** cuentas preconfiguradas y datos ficticios compartidos, sin administración dinámica de cuentas; sin sincronización de escrituras offline ni integración Gestioo. APK local requiere servicios y adb reverse. No presentes estos límites como funciones implementadas.

Modificaciones para practicar: cambiar una regla en logic/Validacion.kt; agregar un estado coherente en cliente y DTO backend; cambiar la etiqueta de un botón sin tocar su callback; comprobar el endpoint invocado por RemoteCareRepository; cambiar una URL en los parámetros de generación, no en cada pantalla.


## ¿Cómo funciona el inicio de sesión?

1. `MainActivity.kt` abre `AccesoApp`. Si no hay sesión, solo compone `LoginScreen.kt`.
2. `LoginViewModel.kt` conecta el formulario con `LoginController.kt`. El controlador usa Retrofit y emite un StateFlow con carga, error o sesión.
3. `SesionController.java` de Catálogo valida las credenciales y genera un token temporal. `AccesoFiltro.java` exige una sesión antes de servir datos. Atenciones verifica el token con Catálogo.
4. Al entrar, aparece la navegación interna. Al salir se elimina de la memoria el ViewModel de negocio y vuelve el login. Room usa claves separadas por usuario y no almacena el token.
5. Demuestra permisos: cliente crea la solicitud, técnico cotiza, cliente aprueba. Cambiar la interfaz por sí sola no concede permisos.
