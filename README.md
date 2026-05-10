# UrbanEye

UrbanEye es un MVP Android nativo escrito en Java para conductores. La app muestra alertas geolocalizadas colaborativas sobre un mapa Mapbox, usa Firebase para autenticación/Firestore en tiempo real y AdMob Rewarded Ads para entregar tokens.

## Stack

- Android Java + MVVM/Clean Architecture ligera.
- Mapbox Maps SDK para Android.
- Firebase Authentication, Firestore, Cloud Messaging y Functions.
- Google AdMob Rewarded Ads.
- AndroidX Lifecycle, Material Components, Retrofit, Gson, Glide y Room.

## Configuración local

1. Abre el repositorio con Android Studio.
2. Crea un proyecto Firebase y descarga `google-services.json`.
3. Copia `google-services.json` en `app/google-services.json`.
4. Define el token público de Mapbox en `~/.gradle/gradle.properties` o `gradle.properties` local:

   ```properties
   MAPBOX_ACCESS_TOKEN=pk.xxxxx
   ```

5. Durante desarrollo se usa el Ad Unit ID recompensado de prueba de Google. Reemplázalo por el ID real solo en builds de producción.

## MVP implementado

- Login y registro con Firebase Auth.
- Creación del documento `users/{uid}` con tokens, XP y reputación inicial.
- Pantalla principal con Mapbox centrado en ubicación actual.
- Observación en tiempo real de alertas activas en Firestore.
- Publicación de alertas verdes, amarillas y rojas con costos de tokens.
- Expiración base: verdes 15/30/60 minutos, amarillas 3 horas, rojas persistentes.
- Perfil con tokens, XP, reputación, logout y anuncios recompensados +20 tokens.
- Votación para confirmar, negar o reportar abuso.
- Reglas iniciales anti-abuso con remoción automática por umbrales.

## Colecciones Firestore

### users

```json
{
  "id": "",
  "username": "",
  "email": "",
  "photoUrl": "",
  "tokens": 20,
  "xp": 0,
  "level": 1,
  "reputation": 100,
  "reportsConfirmed": 0,
  "reportsRejected": 0,
  "createdAt": ""
}
```

### alerts

```json
{
  "id": "",
  "type": "RED",
  "title": "",
  "description": "",
  "latitude": 0,
  "longitude": 0,
  "createdBy": "",
  "createdAt": "",
  "expiresAt": "",
  "confirmations": 0,
  "denies": 0,
  "reports": 0,
  "status": "ACTIVE"
}
```

### votes

```json
{
  "id": "",
  "alertId": "",
  "userId": "",
  "type": "CONFIRM",
  "createdAt": ""
}
```
