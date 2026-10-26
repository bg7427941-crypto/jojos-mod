# Star Platinum — adaptación de Roundabout 0.9.2

La geometría, las UV y el atlas del anime proceden de **Roundabout**, por
**Hydraheads y colaboradores**. Se conservan los créditos históricos a
GenericName/GenericName72. La adaptación al rig y las trece animaciones son del
proyecto JoJo's Mod. Star Platinum es un personaje de Hirohiko Araki.

La [licencia de Roundabout v1.2](upstream/LICENSE-Roundabout.txt) permite copiar,
modificar y compartir, exige crédito y respeto a las solicitudes de distribución
del autor. Los recursos adaptados conservan esa licencia; no se presentan como
geometría o píxeles originales ni se relicencian bajo MIT. Las fuentes de nuestra
adaptación y nuestras animaciones sí tienen la licencia MIT del proyecto.

## Fuentes editables

- [star_platinum.bbmodel](star_platinum.bbmodel): proyecto **GeckoLib** con el rig,
  el atlas incrustado y las trece animaciones utilizadas en Minecraft.
- [upstream/star_platinum.bbmodel](upstream/star_platinum.bbmodel): proyecto de
  Roundabout sin modificar, formato Java Entity, fijado a un commit concreto.
- [upstream/anime.png](upstream/anime.png): atlas 128×128 de la variante del anime,
  conservado byte por byte. No son fotogramas ni grabaciones del anime.
- `build_roundabout.py`: convierte la geometría y UV del proyecto de origen.
- `model_export.py`: exportación común a JSON de GeckoLib y proyecto Blockbench.
- `original_animations.py`: poses propias y auditoría de contactos y separación.
- [asset-provenance.json](asset-provenance.json): URLs, commit, hashes, créditos y
  modificaciones. El JAR lo incluye como `META-INF/jojosmod/star-platinum-provenance.json`.
- `uv-layout.json`: caras exportadas y su UUID de procedencia.
- `preview_model.py`: vistas de desarrollo desde el JSON y atlas de ejecución.

Se usa la revisión `f2390f1240e9b49444d6013e79bfda2038a542fb` de
[Hydraheads/RoundaboutMod](https://github.com/Hydraheads/RoundaboutMod).
El generador comprueba los hashes Git del modelo, el atlas y la licencia antes de
escribir las exportaciones. Se ejecuta sin red, con Python 3, Pillow y NumPy:

```sh
python art/star_platinum/build_roundabout.py
python art/star_platinum/original_animations.py
python art/star_platinum/preview_model.py
python art/star_platinum/preview_model.py --pose guard --time .3 --output /tmp/guard.png
```

## Adaptación

Se conserva la silueta de Roundabout y se aplanan sus rotaciones de reposo del
cabello en transformaciones de cubos. Se mantienen las UV por cara, los reflejos,
las dilataciones y las piezas planas de tela. Los guantes se cortan en Y=14 y las
botas en Y=2, recortando proporcionalmente sus UV para articular muñecas y tobillos.
Las hombreras y la musculatura pertenecen a la geometría de origen.

Se excluyen las extremidades duplicadas para las ráfagas del proyecto de origen,
su dedo extensible único y sus keyframes. Se añaden nuestros dos dedos derechos
para Star Finger. Los 29 huesos conservan el contrato de las acciones: 79 cubos
y trece clips con sus tiempos de contacto originales. Los tres huesos de pañuelo
y dos laterales de faldón se reservan sin cubos; el collar rojo está pintado en el
atlas y las piezas frontal y trasera de faldón sí se articulan.

![Frente, tres cuartos, perfil y espalda del modelo adaptado](preview.png)

Esta imagen es un render de desarrollo, no una captura de Minecraft. Las vistas
utilizan el mismo modelo, UV y atlas que carga el juego. La
[guía de Blockbench](../../docs/blockbench-geckolib.md) explica cómo editar y exportar.
Regenerar sustituye los cambios hechos solo en el proyecto exportado: conserva
una copia antes de editar en Blockbench y traslada los cambios al conversor o a
una nueva fuente documentada antes de volver a ejecutar la generación.

## Animaciones y tiempos a 20 TPS

| Clip | Duración | Revisión |
| --- | --- | --- |
| `idle` | 2,4 s en bucle | Flotación, respiración y postura de combate |
| `move` | 1,2 s en bucle | Deslizamiento y movimiento de piernas/tela |
| `summon` | 0,4 s | Aparición y adopción de la postura |
| `dismiss` | 0,2 s | Retirada |
| `strike` | 0,6 s | Preparación, contacto a 0,2 s y recuperación |
| `barrage` | 1,5 s | Ocho contactos de 0,2 a 1,25 s, separados por 0,15 s |
| `guard` | 0,8 s en bucle | Antebrazos protegiendo el frente |
| `recover` | 0,22 s | Regreso a la postura de combate |
| `star_finger` | 0,9 s | Dos dedos derechos; extensión máxima a 0,4 s y retracción a 0,7 s |
| `intercept` | 0,6 s | Mano preparada de 0,1 a 0,5 s; ventana de servidor [0,1;0,5) |
| `intercept_catch` | 0,22 s | Cierre de mano y retroceso tras neutralizar una flecha |
| `leap` | 0,55 s | Impulso de brazos, piernas recogidas y equilibrio en el aire |
| `leap_land` | 0,25 s | Flexión de rodillas y cuerpo al aterrizar |

Todos los clips ocultan los dedos extensibles salvo Star Finger. Se mantienen
los catorce sonidos sintetizados del proyecto, sin importar audio de Roundabout.
Daño, proyectiles, progresión y sonidos siguen bajo autoridad del servidor.
