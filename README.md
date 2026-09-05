# CookIes

Aplicación Android para descubrir, guardar, crear y compartir recetas, desarrollada con **Kotlin, Jetpack Compose y Firebase**.

## Funcionalidades principales

- Explorar recetas y consultar sus detalles.
- Crear y editar recetas propias.
- Guardar favoritos.
- Gestionar perfil de usuario.
- Autenticación por correo y Google.
- Notificaciones y datos almacenados en Firebase.
- Interfaz con soporte para tema claro y oscuro.

## Stack

- Kotlin
- Jetpack Compose
- ViewModel + StateFlow
- Hilt
- Kotlin Coroutines
- Coil
- Firebase Authentication
- Cloud Firestore
- Firebase Storage

## Arquitectura

La aplicación separa datos, UI, componentes reutilizables y tema visual. Los repositorios encapsulan el acceso a Firebase y los `ViewModel` mantienen el estado que consume la interfaz Compose.

El proyecto sirve especialmente como referencia de un flujo Android moderno basado en estado observable, inyección de dependencias y servicios cloud.

## Desarrollo

Requisitos principales:

- Android Studio
- JDK compatible con el proyecto
- Un proyecto Firebase con Authentication, Firestore y Storage configurados
- `google-services.json` dentro del módulo `app`

Clona el repositorio y ábrelo desde Android Studio:

```bash
git clone https://github.com/AlthausDev/05-Cook-Ies.git
```

Las credenciales y archivos de configuración privados de Firebase no deben compartirse fuera del entorno de desarrollo correspondiente.

## Estado

Proyecto de formación conservado como referencia de arquitectura Android moderna y servicios Firebase.

## Licencia

Publicado bajo **Creative Commons Attribution-NonCommercial 4.0 International**. Consulta [`LICENSE.md`](LICENSE.md).

Si reutilizas contenido original del proyecto, conserva la atribución a **Sam Althaus / AlthausDev** conforme a los términos de la licencia.
