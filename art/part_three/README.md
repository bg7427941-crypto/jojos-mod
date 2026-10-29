# Modelos y recursos de los cinco Stands nuevos de Parte 3

`build_assets.py` exporta cinco proyectos Blockbench editables, geometría GeckoLib,
y 52 clips originales. Los sprites de inventario se mantienen en
[`art/items`](../items/README.md); este generador ya no los sobrescribe.
`adapt_models.py` aplica a Silver Chariot,
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
y atlas de 512 originales. Todos los keyframes,
los generadores y los **22 sonidos sintetizados mono/48 kHz** son originales MIT,
sin entradas de grabación. Los personajes pertenecen a Hirohiko Araki y sus titulares.

Cada subcarpeta incluye `.bbmodel`, atlas y previsualización de los recursos reales;
`overview.png` compara las cinco identidades. Abre el `.bbmodel` en Blockbench con
el soporte GeckoLib. El identificador de modelo coincide con el del recurso de juego.

```sh
python art/part_three/build_assets.py
python art/part_three/build_sounds.py
python art/part_three/preview.py
python art/items/build_sprites.py
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


## Presentación 0.12.1

`motion.py` crea poses originales de cuerpo completo con muestras suavizadas a
40 Hz. Conserva los contactos del servidor: Chariot a 0,15 s, The World a 0,20 s,
Magician's Red a 0,30 s, y los seis/diez contactos de sus ráfagas cada 0,10 s.
Hierophant Green comparte este autor de movimiento y mantiene sus pulsos a
0,30 / 0,40 / 0,50 s. Los proyectos Blockbench se regeneran con los mismos clips.
Star Platinum mantiene su autor independiente y suma una pose temporal de 0,70 s.

Las lianas visibles de Hermit Purple ahora se construyen como una capa del
modelo de jugador: `HermitPurpleLayer.java` contiene la malla local original,
con variantes para brazos normales y delgados, y reutiliza exactamente las
transformaciones de los brazos en primera y tercera persona. El proyecto
`hermit_purple.bbmodel` conserva el modelo independiente como fuente de arte;
ese modelo ya no se dibuja como entidad separada en el juego.

`python art/part_three/build_time_effects.py` genera cuatro efectos temporales
originales y `hermit_purple_arms.png`; su procedencia está en
`time-effects-provenance.json`. `build_sounds.py` conserva los veinte sonidos
base, y el generador temporal se ejecuta después para los cuatro sonidos nuevos.
El shader `time_stop.fsh` también es original. No se han importado animaciones,
sonidos ni shaders de Roundabout para estos cambios.
