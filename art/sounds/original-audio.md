# Audio original de Star Platinum

Diez efectos creados para este proyecto mediante síntesis y DSP en
[build_original.py](build_original.py). Las fuentes son ruido con semillas fijas,
osciladores matemáticos y resonadores amortiguados. No se emplean grabaciones,
muestras descargadas, voces, material del anime ni audio de otros mods.
El código del generador y los sonidos generados se distribuyen bajo la
[licencia MIT del proyecto](../../LICENSE).

Los archivos están en
`src/main/resources/assets/jojosmod/sounds/star_platinum/original/`.
Todos son **OGG Vorbis mono, 48 kHz, calidad 6**, aptos para posicionamiento
espacial en Minecraft. La normalización mide el pico después de decodificar
Vorbis y busca −4 dBFS. Los extremos tienen fundidos suaves y el generador exige
que el pico de los últimos 5 ms quede por debajo de −40 dBFS.

| Archivo | Duración | Diseño |
| --- | ---: | --- |
| `summon.ogg` | 0,740 s | Aire ascendente, llegada con presión grave y resonancias breves |
| `dismiss.ogg` | 0,320 s | Doble exhalación descendente y disipación grave |
| `swing_1.ogg` | 0,110 s | Arco de aire rápido con cuerpo medio |
| `swing_2.ogg` | 0,118 s | Variante más baja, con ataque temprano y remolino final |
| `impact_1.ogg` | 0,180 s | Golpe grave con crujido seco y cuerpo amortiguado |
| `impact_2.ogg` | 0,190 s | Contacto más compacto y pequeño traqueteo residual |
| `guard.ogg` | 0,320 s | Tensión de energía con dos impulsos y metal amortiguado |
| `guard_block.ogg` | 0,180 s | Contacto seco sobre metal, con apoyo grave |
| `barrage_start.ogg` | 1,380 s | Acumulación de fuerza seguida de ocho acentos de aire |
| `star_finger.ogg` | 0,820 s | Extensión elástica, aire deslizante y retroceso suave |

La ráfaga acentúa los instantes **0,20; 0,35; 0,50; 0,65; 0,80; 0,95; 1,10 y
1,25 s**, correspondientes a ocho golpes separados por 0,15 s. Cada swing termina
antes del siguiente golpe. Las capas tonales son cortas y amortiguadas; el aire,
el cuerpo y las resonancias tienen envolventes independientes.

[original-provenance.json](original-provenance.json) conserva la receta y semilla
de cada efecto, duración, formato, número de muestras, pico y RMS decodificados,
pico de la cola, ganancia aplicada y SHA-256 final. También identifica el
generador por SHA-256 y registra las versiones de Python y FFmpeg.

Desde la raíz del repositorio, con Python 3, FFmpeg y FFprobe en `PATH`:

```sh
python3 art/sounds/build_original.py
python3 art/sounds/build_original.py --check
```

La primera orden regenera los diez OGG y el manifiesto sin acceso a la red. La
segunda sintetiza todo de nuevo en un directorio temporal y compara cada archivo
y el manifiesto byte a byte con los incluidos en el repositorio. Las semillas,
longitudes y opciones del codificador son fijas; `-fflags +bitexact` se aplica a
la salida para fijar también el número de serie del contenedor Ogg. La igualdad
binaria se garantiza con la misma plataforma y las mismas versiones de Python y
FFmpeg; otro codificador puede producir bytes distintos.

Las verificaciones automáticas comprueban formato, duración, nivel, muestras
finitas y colas. El balance final de volumen, alcance y mezcla depende de los
eventos del juego y debe valorarse al jugar con sonido.
