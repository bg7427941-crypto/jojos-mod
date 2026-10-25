# Sprites de ítems — arte original

Ocho iconos originales de **32×32 píxeles**, con transparencia RGBA, dibujados
para JoJo's Mod. Arte, SVG y generador con licencia **MIT**, igual que el código
original del proyecto. No se han copiado sprites del anime ni de otros mods.

Se conservan los identificadores de ítem, sus componentes y las reglas de
obtención. Cada ítem tiene ahora un modelo de inventario y una textura propios;
los discos y los núcleos dejan de compartir el icono de fragmento/disco provisional.
El modelo tridimensional de Star Platinum no se modifica.

| Ítem | Motivo visual |
| --- | --- |
| `stand_arrow` | Flecha dorada, asta cálida y pluma plateada |
| `stardust_arrow` | Estrella dorada, gema rojiza y asta violeta |
| `stand_disc_fragment` | Arco de disco roto, metal claro y borde violeta |
| `empty_stand_disc` | Disco plateado con brillo holográfico y agujero central |
| `stand_disc` | Disco violeta con aro dorado y marca luminosa |
| `star_platinum_core` | Cristal violeta en montura estelar dorada |
| `echoes_act_1_core` | Gema verde ovoide y ondas claras |
| `little_feet_core` | Cristal magenta y huella clara |

[preview.png](preview.png) muestra los iconos sobre fondos oscuro y claro, en ese
mismo orden. Los SVG tienen una cuadrícula de píxeles editables sin fuentes ni
recursos externos. Son fuentes de estos sprites; no son texturas de entidades.

## Regenerar

Con Python 3, desde la raíz del repositorio:

```sh
python art/items/build_sprites.py
```

Solo necesita la biblioteca estándar. El script dibuja sobre lienzos nuevos,
exporta SVG y PNG de manera determinista y actualiza los enlaces de los ocho
modelos/ítems. No lee ni edita imágenes de entrada. Para ajustar el arte, cambia
las formas y paleta del script; los PNG ya son de 32×32 y no necesitan reescalado.

Los recursos de ejecución están en `assets/jojosmod/textures/item/`,
`assets/jojosmod/models/item/` y `assets/jojosmod/items/` dentro de
`src/main/resources/`. Deben revisarse en inventario, mano y suelo para valorar
contraste, tamaño aparente y selección de objetos durante el juego.
