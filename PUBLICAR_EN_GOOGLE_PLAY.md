# Publicar Phrasal Daily en Google Play

Revisión del 15 de octubre de 2026 frente a los requisitos actuales de Google Play.

## 1. Lo que ya cumple el código

| Requisito | Estado |
|---|---|
| Nivel de API objetivo: desde el 31/08/2026, las apps nuevas deben apuntar a Android 16 (API 36) | ✅ `targetSdk = 36` y `compileSdk = 36` (AGP 8.10.1) |
| Formato Android App Bundle (.aab) obligatorio para apps nuevas | ✅ `./gradlew bundlePlayRelease`; GitHub lo genera solo cuando existe la clave de subida |
| Las apps de Play no pueden actualizarse fuera de Play | ✅ Versión `play` sin el aviso de actualización, sin `INTERNET` y sin `REQUEST_INSTALL_PACKAGES`. GitHub lo comprueba en cada compilación |
| Permiso de notificaciones (Android 13+) pedido en contexto | ✅ Se pide en la bienvenida y en Ajustes, y la app funciona sin él |
| Alarmas exactas | ✅ `SCHEDULE_EXACT_ALARM` solo hasta Android 12L; en versiones nuevas usa alarmas inexactas, sin declaración especial |
| Pantalla de borde a borde (obligatorio con API 35+) | ✅ `enableEdgeToEdge()` y márgenes del sistema respetados |
| Páginas de memoria de 16 KB | ✅ No usa código nativo |
| Política de privacidad pública | ✅ [PRIVACY.md](PRIVACY.md), enlazada también desde Ajustes > Acerca de |
| Sin anuncios, cuentas, analítica ni SDK de terceros | ✅ Formulario de seguridad de datos sencillo (ver abajo) |
| Contenido propio | ✅ Los 199 phrasal verbs y 113 false friends están escritos para esta app |
| Nombre | ✅ No he encontrado otra app llamada "Phrasal Daily", pero revísalo tú en Play antes de publicar |

## 2. Lo que tienes que hacer tú

1. **Crear la cuenta de desarrollador** en https://play.google.com/console (25 $ de pago único, con verificación de identidad).
2. **Prueba cerrada obligatoria**: si tu cuenta es personal y nueva, necesitas al menos **12 testers** con la app instalada durante **14 días seguidos** antes de poder pedir producción. Google revisa que de verdad la usen.
3. **Clave de subida**: cuando tengas la cuenta, dímelo y te genero la clave. Tú la guardas en GitHub como secretos `SIGNING_KEYSTORE_BASE64` y `SIGNING_PASSWORD`, y desde entonces cada compilación deja el `.aab` listo para subir (en Actions > el último build > Artifacts). Activa **Play App Signing** (viene por defecto).
4. **Rellenar la ficha** con los textos de abajo, el icono de 512×512, la imagen destacada de 1024×500 y al menos 2 capturas de pantalla del móvil.
5. **Para venderla**: crea un perfil de pagos en Play Console y pon el precio. Google cobra el IVA en España por ti y se queda una comisión del 15 % de los primeros 1 M$ al año. Ojo: si la publicas gratis, después ya no podrás ponerle precio.

> Si instalas la versión de Play, antes tendrás que desinstalar la de GitHub una vez, porque van firmadas con claves distintas.

## 3. Formularios de Play Console

- **Seguridad de los datos**: "No recogemos ni compartimos datos de usuario". Los datos se guardan solo en el dispositivo, así que no cuentan como recogidos.
- **Anuncios**: no contiene anuncios.
- **Acceso a la app**: toda la funcionalidad está disponible sin restricciones.
- **Público objetivo**: 13 años o más. Si incluyes menores de 13, se aplica la política de Familias y hay más requisitos.
- **Clasificación de contenido (IARC)**: categoría Educación; sin violencia, sexo, drogas, apuestas ni interacción entre usuarios. Algunos ejemplos usan expresiones coloquiales como *shut up*, así que responde con sinceridad a la pregunta de lenguaje malsonante leve.
- **Categoría**: Educación.

## 4. Textos de la ficha (listos para copiar)

**Nombre (máx. 30):** Phrasal Daily: inglés diario

**Descripción breve (máx. 80):** Un phrasal verb al día con pronunciación, ejemplos, rachas y recordatorios.

**Descripción completa:**

> Aprende inglés de verdad, un día cada vez. Phrasal Daily te enseña cada día un phrasal verb nuevo: cómo se escribe, qué significa, cómo se pronuncia y una frase de ejemplo para usarlo.
>
> ¿Te cuestan los false friends? Activa también una palabra trampa diaria, de esas que parecen lo que no son ("actually" no es "actualmente").
>
> • Palabra del día con traducción, significado y ejemplo
> • Pronunciación con fonética, guía en español y voz en inglés (normal y lenta)
> • Recordatorio diario a tu hora, con frases motivadoras que cambian cada día
> • Rachas de días seguidos y más de 20 logros por desbloquear
> • Estadísticas semanales, mensuales y calendario de estudio
> • Repasos tipo test para que no se te olvide nada
> • Biblioteca con buscador y favoritos
> • Te llama por tu nombre, con tema claro u oscuro
>
> Sin anuncios, sin registro y sin recoger tus datos: todo se guarda en tu móvil.

## 5. A tener en cuenta

- **Requisito de API cada año**: Google sube el nivel de API obligatorio cada agosto. Para seguir publicando actualizaciones habrá que subir `targetSdk` (lo puedo hacer yo).
- **Verificación de desarrolladores**: desde 2027 Google exigirá en todo el mundo que las apps instaladas fuera de Play (como la descarga de GitHub) vengan de un desarrollador verificado. Con la cuenta de Play Console tendrás la verificación hecha; solo habrá que registrar también la versión de GitHub y su clave.
