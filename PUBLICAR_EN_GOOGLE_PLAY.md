# Publicar Phrasal Daily en Google Play

Revisión del 15 de octubre de 2026 frente a los requisitos actuales de Google Play. Actualizada el 16 de octubre para monetizar con anuncios de AdMob.

## 1. Lo que ya cumple el código

| Requisito | Estado |
|---|---|
| Nivel de API objetivo: desde el 31/08/2026, las apps nuevas deben apuntar a Android 16 (API 36) | ✅ `targetSdk = 36` y `compileSdk = 36` (AGP 8.10.1) |
| Formato Android App Bundle (.aab) obligatorio para apps nuevas | ✅ `./gradlew bundlePlayRelease`; GitHub lo genera solo cuando existe la clave de subida |
| Las apps de Play no pueden actualizarse fuera de Play | ✅ Versión `play` sin el aviso de actualización y sin `REQUEST_INSTALL_PACKAGES`. GitHub lo comprueba en cada compilación |
| Permiso de notificaciones (Android 13+) pedido en contexto | ✅ Se pide en la bienvenida y en Ajustes, y la app funciona sin él |
| Alarmas exactas | ✅ `SCHEDULE_EXACT_ALARM` solo hasta Android 12L; en versiones nuevas usa alarmas inexactas, sin declaración especial |
| Pantalla de borde a borde (obligatorio con API 35+) | ✅ `enableEdgeToEdge()` y márgenes del sistema respetados |
| Páginas de memoria de 16 KB | ✅ La app no tiene código nativo propio, y el SDK de AdMob es compatible |
| Política de privacidad pública | ✅ [PRIVACY.md](PRIVACY.md), enlazada también desde Ajustes > Acerca de |
| Anuncios con el SDK oficial de Google (AdMob) | ✅ Un banner fijo encima del menú inferior, nunca en notificaciones ni a pantalla completa |
| Consentimiento de anuncios en la UE (RGPD) | ✅ Formulario de consentimiento de Google (UMP) al abrir la app y opción para cambiarlo en Ajustes > Anuncios |
| Sin cuentas ni analítica propia | ✅ Lo único que comparte datos es AdMob (ver formulario abajo) |
| Contenido propio | ✅ Los 199 phrasal verbs y 113 false friends están escritos para esta app |
| Nombre | ✅ No he encontrado otra app llamada "Phrasal Daily", pero revísalo tú en Play antes de publicar |

## 2. Lo que tienes que hacer tú

1. **Crear la cuenta de desarrollador** en https://play.google.com/console (25 $ de pago único, con verificación de identidad).
2. **Prueba cerrada obligatoria**: si tu cuenta es personal y nueva, necesitas al menos **12 testers** con la app instalada durante **14 días seguidos** antes de poder pedir producción. Google revisa que de verdad la usen.
3. **Clave de subida**: cuando tengas la cuenta, dímelo y te genero la clave. Tú la guardas en GitHub como secretos `SIGNING_KEYSTORE_BASE64` y `SIGNING_PASSWORD`, y desde entonces cada compilación deja el `.aab` listo para subir (en Actions > el último build > Artifacts). Activa **Play App Signing** (viene por defecto).
4. **Rellenar la ficha** con los textos de abajo, el icono de 512×512, la imagen destacada de 1024×500 y al menos 2 capturas de pantalla del móvil.
5. **Anuncios (AdMob)**: la app será gratis y ganará dinero con anuncios. Pasos en el apartado 3.

> Si instalas la versión de Play, antes tendrás que desinstalar la de GitHub una vez, porque van firmadas con claves distintas.

## 3. Configurar AdMob (anuncios)

1. Entra en https://admob.google.com con la misma cuenta de Google y date de alta (es gratis).
2. **Apps > Añadir app** > Android. Si aún no está en Play, di que no está publicada; luego la vinculas.
3. Crea un **bloque de anuncios** de tipo **Banner**.
4. ✅ Hecho (16 oct): ID de aplicación `ca-app-pub-8041642304276543~9465612775` y bloque de banner `ca-app-pub-8041642304276543/8495401177`, ya puestos en la versión de Play (`gradle.properties`). No son secretos.
5. En **Privacidad y mensajes**, crea el mensaje de **RGPD** (consentimiento europeo) para la app y publícalo. Sin él, en España apenas se mostrarán anuncios.
6. En **Pagos**, rellena tus datos fiscales y bancarios. AdMob paga cada mes cuando superas el umbral mínimo de pago (en euros ronda los 70 €).
7. Cuando la app esté publicada en Play, en AdMob ve a **Aplicaciones > Phrasal Daily > Configuración de la aplicación** y pulsa **Vincular a una tienda de apps** para conectarla con su ficha. Así AdMob la revisa y empieza a servir anuncios con normalidad.
8. Después, publica un archivo `app-ads.txt` en la web de desarrollador que pongas en la ficha. AdMob te da el contenido; te lo puedo preparar.

> **Nunca pulses tus propios anuncios reales**: AdMob puede cerrarte la cuenta. Por eso la versión de GitHub que tienes en el móvil muestra siempre anuncios de prueba ("Test Ad"), que puedes tocar sin problema.

## 4. Formularios de Play Console

- **Seguridad de los datos** (por AdMob): la app **recoge y comparte** datos. Marca:
  - *Ubicación aproximada* (a partir de la IP).
  - *Interacciones en la app* (actividad de la app).
  - *Diagnósticos* y *otros datos de rendimiento de la app*.
  - *ID de dispositivo u otros identificadores* (ID de publicidad).
  - Finalidad: publicidad o marketing, analítica y prevención de fraude. Datos cifrados en tránsito: **sí**. El usuario puede pedir que se borren: no aplica, porque la app no tiene cuentas.
  - El progreso, el nombre y los ajustes se quedan en el móvil y no cuentan como recogidos.
- **Anuncios**: **sí, contiene anuncios**. Play Store mostrará la etiqueta "Contiene anuncios".
- **Acceso a la app**: toda la funcionalidad está disponible sin restricciones.
- **Público objetivo**: 13 años o más. Si incluyes menores de 13, se aplica la política de Familias y hay más requisitos.
- **Clasificación de contenido (IARC)**: categoría Educación; sin violencia, sexo, drogas, apuestas ni interacción entre usuarios. Algunos ejemplos usan expresiones coloquiales como *shut up*, así que responde con sinceridad a la pregunta de lenguaje malsonante leve.
- **Categoría**: Educación.

## 5. Textos de la ficha (listos para copiar)

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
> Gratis y sin registro: tu progreso se guarda en tu móvil.

## 6. A tener en cuenta

- **Requisito de API cada año**: Google sube el nivel de API obligatorio cada agosto. Para seguir publicando actualizaciones habrá que subir `targetSdk` (lo puedo hacer yo).
- **Verificación de desarrolladores**: desde 2027 Google exigirá en todo el mundo que las apps instaladas fuera de Play (como la descarga de GitHub) vengan de un desarrollador verificado. Con la cuenta de Play Console tendrás la verificación hecha; solo habrá que registrar también la versión de GitHub y su clave.
- **Ingresos**: Google Play no cobra comisión por los anuncios; los ingresos de AdMob van aparte y debes declararlos (consulta a un gestor).
- **SDK de anuncios**: uso el SDK clásico de Google (`play-services-ads`), que Google mantiene pero recomienda migrar con el tiempo al nuevo "Next-Gen SDK". Funciona igual; la migración se puede hacer más adelante.
