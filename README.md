
# CookIes - Una App para Inspirar y Compartir Recetas

CookIes es una aplicación móvil diseñada para ayudar a los amantes de la cocina a descubrir, compartir y organizar recetas. Con una interfaz moderna y amigable, los usuarios pueden buscar inspiración, agregar sus recetas favoritas, calificar y comentar recetas de otros, y mucho más.

---

## Características

### Funcionalidades principales
1. **Explorar Recetas**:
    - Explora una lista curada de recetas con imágenes y detalles.
    - Filtra por tipo de cocina, tiempo de preparación o ingredientes.

2. **Gestión de Recetas**:
    - Crea, edita y guarda tus propias recetas.
    - Añade ingredientes personalizados y pasos detallados.

3. **Favoritos**:
    - Guarda recetas que te interesen en tu lista de favoritos para acceder fácilmente.

4. **Notificaciones**:
    - Recibe actualizaciones sobre tus recetas favoritas o nuevas recetas populares.

5. **Perfil de Usuario**:
    - Personaliza tu perfil con una foto y datos personales.
    - Cambia tu nombre, correo y contraseña.

6. **Modo Oscuro/Claro**:
    - Cambia entre temas para una experiencia visual personalizada.

---

## Tecnologías Utilizadas

1. **Framework**: Jetpack Compose - Interfaz declarativa moderna de Android.
2. **Backend en la Nube**: Firebase Firestore para almacenamiento y autenticación.
3. **Almacenamiento de Imágenes**: Integración con Firebase Storage para fotos de perfil y recetas.
4. **Autenticación**:
    - Autenticación por correo electrónico y contraseña.
    - Inicio de sesión con Google.
5. **Gestión de Estados**: Uso de `ViewModel` y `StateFlow` para un manejo reactivo y limpio del estado de la aplicación.
6. **Dependencias Clave**:
    - `Coil`: Para cargar imágenes de manera eficiente.
    - `Kotlin Coroutines`: Para manejar tareas asíncronas.
    - `Hilt`: Inyección de dependencias para simplificar la arquitectura del proyecto.

---

## Estructura del Proyecto

### 📂 `data`
Contiene las capas de datos como modelos, repositorios e integración con Firebase.
- **Modelos**:
    - Representan los datos principales: `Recipe`, `Notification`, `UserProfile`, etc.
- **Repositorios**:
    - `NotificationRepository`: Para manejar notificaciones.
    - `RecipeRepository`: Para gestionar las recetas y favoritos.
    - `UserRepository`: Para operaciones de perfil y autenticación.

### 📂 `ui`
Organiza las vistas de la aplicación en base a funcionalidades.
- **Autenticación**: `LoginView`, `SignUpView`, `ForgotPasswordView`.
- **Panel de Usuario**: `DashboardView`, `ProfileView`, `SettingsView`.
- **Recetas**: `RecipeDetailView`, `RecipeWizardView`.

### 📂 `components`
Componentes reutilizables y personalizados como:
- **Botones**: `PrimaryButton`.
- **Entradas**: `CustomTextField`.
- **Otros**: `SharedLoadingIndicator`, `SharedTopAppBar`.

### 📂 `theme`
Define la paleta de colores y estilos visuales:
- Compatibilidad con modo claro y oscuro.

---

## Configuración del Entorno de Desarrollo

### Requisitos
- **Android Studio Flamingo o superior**.
- **JDK 11 o superior**.
- **Firebase Console**:
    - Configurar Firestore, Storage y Authentication.
    - Descargar el archivo `google-services.json` y colocarlo en el directorio `app`.

### Pasos para Configurar
1. Clona este repositorio:
   ```bash
   git clone https://github.com/AlthausDev/05-Cook-Ies.git
   cd cookies
   ```
2. Abre el proyecto en Android Studio.
3. Configura las claves necesarias para Firebase.
4. Ejecuta el proyecto en un emulador o dispositivo físico.

---

## Uso

1. Abre la aplicación.
2. Regístrate o inicia sesión.
3. Explora recetas, crea tus propias recetas y comparte tus creaciones con la comunidad.

---

## Contribución

Si deseas contribuir, sigue estos pasos:
1. Haz un "fork" del repositorio.
2. Clona tu repositorio forkeado:
   ```bash
   git clone https://github.com/AlthausDev/05-Cook-Ies.git
   ```
3. Crea una nueva rama:
   ```bash
   git checkout -b nombre-de-tu-rama
   ```
4. Realiza tus cambios y asegúrate de que el código pase las pruebas.
5. Haz "commit" y sube tus cambios:
   ```bash
   git add .
   git commit -m "Descripción de los cambios"
   git push origin nombre-de-tu-rama
   ```
6. Abre un "Pull Request" en el repositorio original.

---

## Despliegue

Para desplegar la aplicación en un entorno de producción:
1. Configura Firebase Firestore y Storage con reglas de seguridad apropiadas.
2. Activa el servicio de autenticación con proveedores como Google.
3. Genera una versión "release" de la aplicación desde Android Studio.

---

## Licencia

Este proyecto está bajo la [Licencia Creative Commons 4.0](LICENSE-CC-BY-NC-4.0.md).

---

## Contacto

Si tienes alguna pregunta, sugerencia o problema, no dudes en contactarme a través de [samuelalthaus@gmail.com](mailto:samuelalthaus@gmail.com).

---

### ¡Gracias por contribuir y ser parte de la comunidad CookIes!
