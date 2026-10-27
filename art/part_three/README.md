# Modelos y recursos de los cinco Stands nuevos de Parte 3

`build_assets.py` exporta cinco proyectos Blockbench editables, geometría GeckoLib,
sprites y 47 clips originales. `adapt_models.py` aplica a Silver Chariot,
Magician’s Red y The World el mismo flujo de conversión de Star Platinum:
formas/UV del modelo de Roundabout, atlas de Parte 3 fijado por revisión,
transformaciones de reposo horneadas y adaptación al rig de técnicas del mod.
No importa keyframes de Roundabout. Chariot conserva placas independientes y
un solo estoque 3D propio en lugar de los planos transparentes originales; los duplicados de ráfaga/kick y el estoque izquierdo se omiten.

Las fuentes y los atlas de esos tres modelos permanecen bajo **Roundabout License
v1.2**, con crédito a **Hydraheads y colaboradores**. La revisión, rutas y hashes
están en `upstream/sources.json`; la [licencia completa](../star_platinum/upstream/LICENSE-Roundabout.txt)
se distribuye también dentro del JAR. El atlas de cada modelo adaptado se copia
byte por byte (128 × 128). El Java exportado de Chariot se convierte en geometría,
no se incorpora como código de juego compilado.

Hermit Purple (lianas espinosas) y The Fool (cuadrúpedo de arena) mantienen geometría
y atlas de 512 originales. Todos los keyframes, los cinco sprites de núcleo,
los generadores y los **22 sonidos sintetizados mono/48 kHz** son originales MIT,
sin entradas de grabación. Los personajes pertenecen a Hirohiko Araki y sus titulares.

Cada subcarpeta incluye `.bbmodel`, atlas y previsualización de los recursos reales;
`overview.png` compara las cinco identidades. Abre el `.bbmodel` en Blockbench con
el soporte GeckoLib. El identificador de modelo coincide con el del recurso de juego.

```sh
python art/part_three/build_assets.py
python art/part_three/build_sounds.py
python art/part_three/preview.py
```

Requiere Pillow, NumPy para las vistas y FFmpeg/libvorbis para el audio. La versión
de FFmpeg y los hashes se guardan en `sound-provenance.json`; el codificador usa
opciones bitexact. `asset-provenance.json` registra autores, fuentes y hashes. Ambos
manifiestos se incluyen en el JAR bajo `META-INF/jojosmod`.

Los recursos originales se publican con MIT; los adaptados conservan la licencia
y atribución de Roundabout descritas arriba.
Las vistas del rasterizador son ayuda de desarrollo: la entrega requiere revisar
GeckoLib y las transiciones dentro de Minecraft. Las técnicas y sus adaptaciones
se explican en [el diseño](../../docs/part-three-stands-design.md).
