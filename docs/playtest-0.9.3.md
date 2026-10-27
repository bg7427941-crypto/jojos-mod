# Audio por ataque — prueba 0.9.3

Guía histórica de la primera entrega de audio. La rama actual conserva los clips
subidos manualmente después de esta entrega y añade la
[progresión 0.10.0](playtest-0.10.0.md), con protocolo 8.

Esta entrega añade el evento opcional `jojosmod:star_platinum.single_ora` para Z
y admite `ora.ogg` en el constructor del paquete de recursos. Mantiene el modelo
de Roundabout aprobado en 0.9.2, las habilidades y los catorce sonidos sintetizados
del JAR. Las grabaciones de la prueba local se entregan en un ZIP separado y
no forman parte del repositorio público ni del JAR.

## Instalación

1. Usa `build/libs/jojosmod-0.9.3.jar` en `mods`, retirando el JAR anterior.
2. Copia el ZIP de audio local, sin descomprimir, a `resourcepacks`.
3. Actívalo en Opciones → Paquetes de recursos por encima de otros packs que
   sustituyan sonidos del mod. Ajusta el volumen **Jugadores**.
4. Para jugar con amigos, cliente y servidor deben usar 0.9.3, **protocolo 7**.
   Cada oyente activa el ZIP en su cliente para escuchar la sustitución.

Minecraft 26.3, NeoForge 26.3.0.95 y GeckoLib 5.5.7 siguen siendo los requisitos.
La [guía del constructor](anime-audio.md) permite crear un pack parcial a partir
de clips locales sin cambiar el código del mod.

## Comprobar los sonidos

| Acción | Comprobación |
| --- | --- |
| R | La invocación suena una vez; desinvocar corta la voz del propietario |
| Z | Un ORA por ataque aceptado, también al aire; los intentos durante cooldown no añaden voces |
| X | La ráfaga usa su frase propia; no repite el ORA de Z en cada pulso |
| V | Star Finger usa su llamada propia y conserva el contacto a 0,4 s |
| Impacto | Sus dos variantes suenan al causar daño real; fallar no reproduce el impacto |
| C | La preparación y el bloqueo válido utilizan señales distintas |
| Interrumpir | Empezar otra voz o cancelar la acción corta la del mismo propietario |
| Dos jugadores | Cancelar al primero deja continuar la voz del segundo |

La prueba local usa ocho clips para siete eventos; los demás conservan el audio
del mod. Los clips son OGG Vorbis mono/48 kHz, con nivel ajustado y fundidos breves.
El ZIP incluye `SOURCES.json` con los hashes de procedencia y cortes, y `LEEME.txt`
con la instalación. No se presenta la selección como audio original del proyecto.

La llamada completa de Star Finger dura unos 2,53 s: puede terminar después de
la pose de 0,9 s. Una nueva voz del propietario la sustituye. Valora si esa cola
se siente bien al encadenar ataques. El ORA individual se extrajo de la ráfaga
recopilada; su corte también necesita valoración escuchándolo en tu equipo.

## Comportamiento sin pack

`single_ora` está vacío en el JAR base. El cliente lo reconoce y evita crear una
instancia de sonido cuando el pack no proporciona un clip; no utiliza un golpe
como voz de sustitución. Los efectos de aire, impacto y los otros sonidos siguen
funcionando. El evento nuevo no se comparte con X ni V.

## Verificación

La compilación pasa. Se verificaron las 84 pruebas existentes del servidor y
el nuevo caso de audio por propietario, repetido de forma focalizada. Este caso
comprueba el codec de todos los cues, un ORA por Z aceptado, ausencia de voces en
solicitudes rechazadas, señales distintas de X/V y cancelación con dos propietarios.
Los jugadores de esta prueba se registran en el mapa de entidades del mundo para
ejercer el envío real a observadores y al propietario.

Dos pruebas del constructor ejercen el ZIP parcial con ORA e impacto y el rechazo
de un archivo MP3 renombrado. El catálogo y el constructor coinciden en trece
eventos y catorce nombres de entrada. Se verificaron las ocho grabaciones del ZIP
local: decodificación completa, mono/48 kHz, correspondencia de eventos y hashes.
El JAR conserva los catorce OGG originales; los cambios públicos no incluyen
grabaciones recopiladas. La escucha en tu PC y la prueba con dos clientes reales
quedan pendientes de esta guía.
