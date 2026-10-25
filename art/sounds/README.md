# Sonidos de Star Platinum

Siete clips editados a partir de efectos gratuitos de **Kenney (Kenney Vleugels)**,
con licencia **CC0-1.0**. Se pueden reutilizar, modificar y redistribuir, también en
proyectos comerciales. El crédito no es obligatorio bajo CC0; se conserva aquí
para documentar la procedencia. Estos archivos no contienen voces, música ni
sonidos extraídos del anime o de otros juegos de JoJo.

## Fuentes y prueba de licencia

Las páginas oficiales identifican sus paquetes como Creative Commons CC0 y ofrecen
descarga gratuita, con donación opcional:

| Paquete oficial | Archivos originales utilizados | Aviso original conservado |
| --- | --- | --- |
| [Impact Sounds 1.0](https://kenney.nl/assets/impact-sounds) | `impactPunch_heavy_000.ogg`, `impactPunch_medium_001.ogg`, `impactMetal_medium_000.ogg` | [kenney-impact-sounds-license.txt](kenney-impact-sounds-license.txt) |
| [RPG Audio](https://kenney.nl/assets/rpg-audio) | `knifeSlice.ogg` | [kenney-rpg-audio-license.txt](kenney-rpg-audio-license.txt) |
| [Sci-fi Sounds 1.0](https://kenney.nl/assets/sci-fi-sounds) | `forceField_000.ogg` | [kenney-sci-fi-sounds-license.txt](kenney-sci-fi-sounds-license.txt) |

La descarga utilizada es la copia pública de los paquetes y sus avisos originales
en [GeorgeQLe/assets-sfx-combat](https://github.com/GeorgeQLe/assets-sfx-combat/tree/9662dc1bed3b06ae753045915f9accfac044c7ce),
fijada al commit `9662dc1bed3b06ae753045915f9accfac044c7ce`. Se contrastó su licencia
con las tres páginas oficiales. El crédito del autor corresponde a Kenney; el
repositorio citado proporciona la copia de distribución. Los avisos originales
también se incluyen junto con los OGG en los recursos del JAR.

[provenance.json](provenance.json) registra la URL exacta de cada entrada, su
SHA-256, filtros aplicados, duración, formato, pico decodificado y SHA-256 final.
Los cinco archivos de entrada se comprobaron por hash antes de procesarlos.

## Edición y eventos

Los sonidos están en `src/main/resources/assets/jojosmod/sounds/star_platinum/`.
Son OGG Vorbis **mono a 48 kHz**, para que Minecraft pueda ubicarlos espacialmente.
Se recortaron, filtraron y suavizaron sus extremos; la desinvocación invierte un
fragmento de la energía de invocación. La normalización mide el archivo Vorbis ya
decodificado para corregir el sobreimpulso del códec: los picos finales quedan entre
−3,2 y −2,5 dBFS. Esto deja margen antes de aplicar el volumen del juego.

| Evento `jojosmod:` | Archivo | Duración aproximada | Edición principal |
| --- | --- | --- | --- |
| `star_platinum.summon` | `summon.ogg` | 0,65 s | Energía de `forceField_000`, filtrado y fundidos |
| `star_platinum.dismiss` | `dismiss.ogg` | 0,35 s | Energía recortada e invertida |
| `star_platinum.swing` | `swing.ogg` | 0,12 s | Fragmento de aire de `knifeSlice`, acelerado y filtrado |
| `star_platinum.impact` | `impact_1.ogg`, `impact_2.ogg` | 0,18 s cada uno | Dos golpes de puño con ataques distintos y cola recortada |
| `star_platinum.guard` | `guard.ogg` | 0,22 s | Energía corta con menos graves |
| `star_platinum.guard_block` | `guard_block.ogg` | 0,18 s | Impacto metálico corto |

`sounds.json` declara seis eventos, con dos variantes para los impactos y sin
bucles ni streaming. El swing dura menos que el intervalo de 0,15 s de la ráfaga,
para evitar una cola larga superpuesta en cada golpe. El volumen y el momento de
reproducción los decide la lógica del mod. El timbre y el balance final se deben
valorar en el juego con una salida de audio real.

## Volver a generar

Con Python 3, FFmpeg y FFprobe disponibles en `PATH`, desde la raíz del proyecto:

```sh
python art/sounds/build_star_platinum.py
```

El script descarga solo los cinco archivos necesarios desde URLs fijadas al
commit indicado, verifica sus SHA-256 y elimina su caché temporal al terminar.
Reproduce la edición y actualiza `provenance.json`. Para usar entradas ya descargadas
y trabajar sin red, conserva sus rutas relativas a cada paquete en una carpeta y usa:

```sh
python art/sounds/build_star_platinum.py --source-dir /ruta/a/los/paquetes
```

Por ejemplo, esa carpeta contendrá
`impact-sounds/Audio/impactPunch_heavy_000.ogg`. Si una entrada falta en la caché,
el script intenta descargarla. Los avisos de licencia originales se copian a los
recursos al generar los sonidos.
