# Modelo y animaciones de Star Platinum — 0.6.0

El modelo, el atlas anime y la pose base de guardia proceden de **Roundabout**, por
**Hydraheads y su equipo**, con contribuciones de modelado de **GenericName / GenericName72**.
Estos recursos mantienen la **Roundabout License v1.2**; no se reclaman como arte
original de este proyecto ni se relicencian MIT. Los [créditos y fuentes exactas](../../THIRD_PARTY_NOTICES.md)
y la [licencia completa](../../third_party/roundabout/LICENSE.txt) acompañan al código y al JAR.

Se adaptó el proyecto `star_platinum.bbmodel` y el atlas `anime.png` de la revisión
`cdd6dc35aead479aef4cebce873c1e532c0eccf3`. Se retiraron brazos/piernas auxiliares de
ráfagas. En 0.6.0 se adapta el dedo extensible a dos dedos para Star Finger.
El rig exportado tiene **58 huesos y 71 cubos**, con atlas **128×128**; el cuerpo
aprobado de 57 huesos y 69 cubos permanece igual.
La textura detallada sustituye la paleta de casillas de la prueba 0.5.0.

## Editar y exportar

`star_platinum.bbmodel` es el proyecto editable para Blockbench con el complemento
GeckoLib: incluye jerarquía, pivotes, cubos, UV por cara, atlas incrustado y nueve
animaciones. Conserva nombres de huesos y animaciones al editar.

Con Python 3, desde la raíz del repositorio:

```sh
python art/star_platinum/export_roundabout.py
```

El script exporta el proyecto editable actual, sin descargar archivos ni requerir
el mod Roundabout. Conserva UV orientadas, pivotes, rotaciones e inflación. Para
repetir la adaptación inicial desde los dos originales fijados en los créditos:

```sh
python art/star_platinum/export_roundabout.py --import-upstream /ruta/star_platinum.bbmodel --texture /ruta/anime.png
```

Esa operación sustituye el proyecto editable por la adaptación inicial. Comprueba
los hashes de los originales antes de hacerlo. El código del exportador es propio;
sus convenciones de coordenadas/UV se contrastaron con el exportador oficial de
Blockbench, cuya revisión está indicada en el script.

| Recurso | Ruta desde `src/main/resources/assets/jojosmod/` |
| --- | --- |
| Geometría | `geckolib/models/entity/star_platinum.geo.json` |
| Animaciones | `geckolib/animations/entity/star_platinum.animation.json` |
| Atlas PNG | `textures/entity/star_platinum.png` |

## Movimiento y tiempos

Las curvas de reposo, movimiento, invocación, retirada, golpe, ráfaga y recuperación
se ajustaron para este mod, sobre el rig adaptado. La guardia adapta la pose `Block`
del proyecto original. Las curvas suaves se exportan como muestras lineales para
mantener los instantes de contacto y evitar diferencias entre interpoladores.

Los nombres usan el prefijo `animation.star_platinum.` y los sufijos `idle`,
`move`, `summon`, `dismiss`, `strike`, `barrage`, `guard`, `recover` y `star_finger`. El contacto del
golpe sigue a los **0,2 s**; los ocho pulsos de ráfaga caen en **0,2 / 0,35 / 0,5 /
0,65 / 0,8 / 0,95 / 1,1 / 1,25 s**. La pose de recuperación evita levantar de nuevo
los brazos al finalizar una acción. Los sonidos y partículas se disparan desde el
servidor, fuera del archivo de animaciones, también para los observadores.
Star Finger dura **0,9 s**, extiende los dos dedos en **0,4 s** y los retrae antes
de finalizar. Los otros ocho clips ocultan explícitamente el hueso `finger`.

Revisa el resultado en el juego desde primera y tercera persona. La validez del
formato y de las UV no sustituye la valoración visual del jugador.
