# Audio del anime y paquete de recursos local

La [recopilación de referencias de Star Platinum](star-platinum-anime-audio-catalog.md)
reúne enlaces para escuchar invocación, ORA y Star Finger, escenas de Parte 3 y
la asignación por evento. Es un catálogo de investigación; no incluye grabaciones
descargadas ni sustituye los sonidos de esta versión.

La versión 0.9.3 incluye catorce [efectos originales sintetizados](../art/sounds/original-audio.md)
para el proyecto, siguiendo la dirección de crear su presentación desde cero.
No incluye grabaciones de voces del anime ni muestras extraídas de otros mods.
La plantilla de
`extras/anime-audio-pack/` permite sustituirlos en tu cliente con archivos que tengas
permiso para usar, sin incorporarlos al código ni al JAR. No incluye ni descarga
audio. Usar un pack local no concede derechos adicionales sobre sus grabaciones;
no publiques ni compartas un pack con ellas sin el permiso correspondiente.

## Preparar los clips

Crea una carpeta y coloca los archivos que quieras sustituir con estos nombres.
Puedes aportar solo uno; los demás eventos conservarán sus sonidos del mod.

| Archivo de entrada | Evento `jojosmod:` | Recomendación |
| --- | --- | --- |
| `summon.ogg` | `star_platinum.summon` | Invocación breve, hasta aproximadamente 0,8 s |
| `dismiss.ogg` | `star_platinum.dismiss` | Retirada breve, alrededor de 0,35 s |
| `swing.ogg` | `star_platinum.swing` | Aire o puñetazo de hasta 0,15 s; se reproduce por pulso |
| `ora.ogg` | `star_platinum.single_ora` | ORA exclusivo de Z; una vez al aceptar el ataque, alrededor de 0,3 s |
| `impact_1.ogg`, `impact_2.ogg` | `star_platinum.impact` | Una o dos variantes, alrededor de 0,2 s |
| `guard.ogg` | `star_platinum.guard` | Entrada en guardia, alrededor de 0,25 s |
| `guard_block.ogg` | `star_platinum.guard_block` | Impacto defensivo, alrededor de 0,2 s |
| `barrage_start.ogg` | `star_platinum.barrage_start` | Inicio de ráfaga: una sola vez al pulsar X; máximo recomendado 1,5 s |
| `star_finger.ogg` | `star_platinum.star_finger` | Señal breve de Star Finger |
| `intercept.ogg` | `star_platinum.intercept` | Preparación, aproximadamente 0,1 s |
| `intercept_catch.ogg` | `star_platinum.intercept_catch` | Acierto, aproximadamente 0,18 s |
| `leap.ogg` | `star_platinum.leap` | Impulso, aproximadamente 0,28 s |
| `leap_land.ogg` | `star_platinum.leap_land` | Aterrizaje, aproximadamente 0,2 s |

Para una frase o un grito continuo de ráfaga, usa `barrage_start.ogg`; ponerlo en
`swing.ogg` repetiría el clip en cada pulso y superpondría las voces. Ningún evento
del pack es un bucle. Una cola breve reduce el solapamiento al interrumpir acciones.

`ora.ogg` necesita la versión 0.9.3. Su evento está vacío en el mod base y solo
suena si un pack proporciona un clip. No se reutiliza durante X ni V y no añade
un golpe sonoro de sustitución cuando falta. R, Z, X y V comparten un canal por
propietario: una nueva voz sustituye la anterior de ese jugador. Desinvocar,
recibir daño que interrumpe la acción, morir o cambiar de dimensión la corta.
Un intento rechazado por cooldown o por otra acción activa no inicia otra voz.

La versión 0.9.3 usa **protocolo 7**: actualiza cliente y servidor para jugar juntos.
El ZIP sigue siendo un recurso de cliente y cada oyente debe activarlo en su instancia.

Minecraft necesita **OGG Vorbis** para estos archivos. Usa **mono** para que se sitúen
en el mundo y se atenúen con la distancia. Renombrar un MP3 a `.ogg` no lo convierte.
Con FFmpeg, por ejemplo, para convertir un clip de inicio de ráfaga:

```sh
ffmpeg -i mi-clip.wav -t 1.5 -ac 1 -ar 48000 -c:a libvorbis -q:a 5 barrage_start.ogg
```

Para un swing, sustituye `-t 1.5` por `-t 0.15` y el nombre de salida por `swing.ogg`.
Escucha el resultado y ajusta el volumen de origen si satura. El constructor
comprueba extensión, cabecera Ogg e identificación Vorbis; no decodifica el audio
completo ni verifica por sí solo su duración o volumen. Avisa si detecta estéreo.

## Construir e instalar

Desde la raíz del repositorio, con Python 3:

```sh
python extras/anime-audio-pack/build_pack.py --source-dir /ruta/mis-sonidos --output /ruta/JoJo-audio-local.zip
```

En PowerShell de Windows, por ejemplo:

```powershell
py .\extras\anime-audio-pack\build_pack.py --source-dir "C:\JoJo\mis-sonidos" --output "C:\JoJo\JoJo-audio-local.zip"
```

El ZIP no se sobrescribe por defecto. Elige otro nombre o añade `--overwrite` para
sustituirlo. Los nombres de OGG desconocidos y los formatos que necesitan conversión
se rechazan antes de crear el ZIP; otros documentos de la carpeta no se copian.
Si aportas solo `impact_2.ogg`, el evento de impacto usará únicamente esa variante.

1. Copia el ZIP a la carpeta `resourcepacks/` de tu instancia de Minecraft.
2. Entra en Opciones → Paquetes de recursos y actívalo por encima de otros packs
   que reemplacen el audio de JoJo's Mod.
3. Prueba R, Z, X, C y Star Finger. Ajusta **Jugadores** en Música y sonidos y activa
   subtítulos si quieres comprobar los avisos sin depender del volumen.
4. Para recuperar el audio incluido, desactiva el pack y aplica los cambios.

Comprueba primero Z al aire: debe emitir un ORA, también cuando no haya un
objetivo. Mantenerlo o pulsarlo durante el cooldown no debe superponer voces.
X mantiene su frase de ráfaga y V su llamada propia. Para la prueba de cancelación,
empieza una voz larga y desinvoca con R; después repítelo con dos jugadores y
cancela solo la del primero. La voz del segundo debe continuar.

La prueba local preparada durante 0.9.3 conserva la llamada completa de Star
Finger, de unos 2,53 s. La habilidad mantiene su contacto a 0,4 s y duración de
0,9 s; la voz puede continuar después de la pose y se corta si comienza otra voz
del mismo propietario. No se acelera la grabación ni se retrasa el daño.

Es un paquete de cliente: cada amigo elige instalarlo, y no cambia habilidades,
daño ni voluntad. No se envía automáticamente a otros jugadores.

La plantilla usa recursos **97.1**, comprobados en `version.json` del Minecraft
26.3 incluido en NeoForge 26.3.0.95. `pack.mcmeta` declara `min_format` y `max_format`
como `[97, 1]`; el formato de datos del servidor es distinto y no se utiliza aquí.
