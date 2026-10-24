# Fuentes de Star Platinum

Arte original del proyecto, con licencia MIT igual que el código. Representación
en bloques inspirada en la apariencia de Star Platinum del anime Stardust Crusaders;
no contiene modelos, texturas, voces ni audio extraídos de otros juegos o de la serie.

- `star_platinum.bbmodel`: proyecto editable de Blockbench, jerarquía, cubos, textura
  incrustada y las ocho animaciones. Usa el complemento de animación de GeckoLib
  para revisar/exportar el formato del mod.
- `star_platinum-atlas.svg`: paleta editable de 128×128. Los materiales ocupan
  casillas de 32×32, compartidas por las piezas con el mismo material.

La geometría exportada tiene 24 huesos y 101 cubos; el atlas mide 128×128.

Recursos exportados:

| Recurso | Ruta desde `src/main/resources/assets/jojosmod/` |
| --- | --- |
| Geometría | `geckolib/models/entity/star_platinum.geo.json` |
| Animaciones | `geckolib/animations/entity/star_platinum.animation.json` |
| Atlas PNG | `textures/entity/star_platinum.png` |

Conserva nombres de huesos y animaciones al editar. El controlador usa `idle`,
`move`, `summon`, `dismiss`, `strike`, `barrage`, `guard` y `recover`, con prefijo
`animation.star_platinum.`. El contacto del golpe empieza a los 0,2 segundos;
los pulsos de ráfaga están separados por 0,15 segundos. Las unidades de geometría
son dieciseisavos de bloque. Revisa el resultado en juego desde ambas perspectivas:
una exportación válida no garantiza un encuadre o articulaciones satisfactorios.
