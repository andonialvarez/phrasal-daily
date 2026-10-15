# Phrasal Daily

App Android para aprender un phrasal verb (y, si quieres, un false friend) cada día.

## Qué incluye

- **Palabra del día**: cómo se escribe, traducción, significado, pronunciación (IPA + aproximación en español + voz con TextToSpeech, normal y lenta) y una frase de ejemplo traducida.
- **Modo de contenido** en Ajustes: solo phrasal verb, solo false friend (con botón para cambiar el de hoy) o los dos cada día.
- **Recordatorio diario** a la hora que elijas, con frases motivadoras que van cambiando, usando tu nombre y tu racha. Si ese día ya has entrado, no te molesta.
- **Rachas y logros**: días seguidos, mejor racha, 21 logros (3, 7, 14, 30, 60, 100, 200 y 365 días, palabras aprendidas, repasos, madrugador, búho nocturno…).
- **Estadísticas**: días esta semana, este mes, total y calendario mensual.
- **Repaso**: test de opción múltiple con las palabras que ya has visto.
- **Biblioteca** con buscador, favoritos y "por aprender".
- **Perfil**: te saluda por tu nombre. Tema claro/oscuro, velocidad de voz y reinicio de progreso.

Contenido incluido: 199 phrasal verbs y 113 false friends (`app/src/main/assets/`). Todo se guarda en el móvil; no hay servidor ni cuenta.

## Instalar en el móvil y recibir actualizaciones automáticas

Cada push a `main` lanza GitHub Actions, que compila el APK y lo publica como *Release*.

1. Abre en el móvil https://github.com/andonialvarez/phrasal-daily/releases/latest/download/PhrasalDaily.apk y abre el archivo descargado.
2. Android te pedirá permitir instalar apps desde el navegador: acéptalo e instala.
3. Cada vez que abras la app, comprueba si hay una versión nueva en GitHub y te ofrece actualizarla (también desde Ajustes > Buscar actualizaciones).

## Compilar en local

Android Studio (Ladybug o posterior): abre la carpeta y pulsa Run. O desde terminal: `./gradlew assembleDebug`.

## Firma

Por defecto el APK se firma con `app/debug.keystore` (credenciales públicas estándar `android`/`androiddebugkey`, como hace la plantilla de React Native), para que cada versión se instale encima de la anterior. Si en el futuro quieres una clave privada, añade los secretos `SIGNING_KEYSTORE_BASE64` y `SIGNING_PASSWORD` en el repositorio y el workflow la usará (cambiar de clave obliga a desinstalar una vez).
