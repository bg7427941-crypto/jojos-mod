# Dirección artística

El primer conjunto pulido se centra en Stardust Crusaders. Para 0.9.2 se adapta
el modelo y atlas de Roundabout, con crédito y su licencia, buscando una silueta
que encaje con Minecraft. Las habilidades, voluntad, animaciones y audio del
proyecto se conservan.

## Star Platinum en 0.9.2

| Elemento | Construcción actual |
| --- | --- |
| Modelo | Adaptación de Roundabout: 79 cubos, 29 huesos, muñecas y tobillos articulados |
| Textura | Atlas anime 128×128 de Roundabout, conservado byte por byte |
| Animaciones | Trece secuencias propias con los contactos del servidor |
| Sonido | Catorce clips sintetizados; sin grabaciones externas |
| Sprites | Ocho ítems originales de 32×32, con sus fuentes SVG y generador |
| Partículas y HUD | Composición propia sobre las APIs de Minecraft |
| Habilidades y voluntad | Implementación del proyecto sobre Minecraft, NeoForge y GeckoLib |

La procedencia y el conversor están en [arte de Star Platinum](../art/star_platinum/README.md).
La [licencia de Roundabout](../art/star_platinum/upstream/LICENSE-Roundabout.txt)
y los [créditos](../THIRD_PARTY_NOTICES.md) acompañan el recurso adaptado.
Se conservan rostro, cabello, hombreras y vestuario del modelo de origen, se
aplanan sus rotaciones de reposo y se recortan las UV en las nuevas articulaciones.
Los keyframes de origen no se importan; Star Finger usa dos dedos derechos propios.

Las [vistas de revisión](../art/star_platinum/preview.png) muestran frente, tres
cuartos, perfil y espalda. La [guía 0.9.2](playtest-0.9.2.md) propone valorar la
apariencia y las transiciones en el juego. Los [créditos históricos](history/asset-provenance.md)
registran las presentaciones anteriores, incluida la base de jugador propia 0.9.1.

## Referencias consultadas

| Fuente | Uso en esta entrega |
| --- | --- |
| [Portal oficial de Stardust Crusaders](https://jojo-portal.com/en/anime/sc/character/01/) | Identidad y contexto del personaje |
| [Comparación visual del episodio 23](https://jojocomparisons.github.io/jojo-sc-23/) y [ficha de apariencia](https://jojowiki.com/Star_Platinum#Appearance) | Contrastar la paleta del anime y los detalles del rostro y vestuario |
| [Star Platinum de JoJo's Bizarre Survival](https://github.com/Novarch129/JoJo-s-Bizarre-Survival/blob/master/images/star_platinum.png) | Comparar silueta, hombreras y lectura de un personaje cúbico |
| [Presentación de Ripples of the Past](https://github.com/StandoByte/Ripples-of-the-Past/blob/1.16.5/src/main/resources/logo.png) | Comparar proporciones y densidad de detalle en Minecraft |
| [Roundabout](https://github.com/Hydraheads/RoundaboutMod) | Fuente del modelo, UV y atlas anime adaptados, con licencia conservada |
| Minecraft | Escala, cámara, distancia de lectura y lenguaje de píxeles |

Se contrastó la paleta con fotogramas del anime reproducidos en la comparación:
piel violeta, sombras faciales moradas, cabello negro, ojos cian, corona dorada
con joya plateada, pañuelo rojo, guantes y botas oscuros, faldón y rodilleras claros.
El atlas actual se toma de la variante anime de Roundabout; no se incorporan
las imágenes de las otras páginas al atlas. La consulta de referencias
publicadas no equivale a haber jugado esos mods ni prueba compatibilidad con 26.3.

Las proporciones buscan hombros anchos, cintura estrecha, musculatura legible y
cabello escalonado. Las hombreras y guantes conservan el diseño de Roundabout y
se articulan en el rig del proyecto. Star Finger extiende dos dedos desde una mano articulada.
La guardia acerca los guantes al rostro manteniendo espacio entre las piezas.

## Ritmo y sonido

Z hace contacto a 0,2 s; X alterna ocho golpes desde 0,2 s cada 0,15 s; V alcanza
su extensión máxima a 0,4 s y la oculta a 0,7 s. Preparación, contacto y recuperación
se diseñan alrededor de esos tiempos. Tela, cabello y flotación acompañan la pose.

La paleta sonora combina llegada de energía, aire, impactos, tensión defensiva y
extensión elástica. Las señales largas de R, X y V se gestionan por propietario:
interrumpir una habilidad no corta la de otro jugador. Se conserva la posibilidad
de [personalizar los sonidos en el cliente](anime-audio.md).

## Criterios de revisión

La [prueba 0.9.2](playtest-0.9.2.md) revisa rostro, articulaciones, transición entre
poses, lectura de impactos, visibilidad en primera persona y presentación para los
amigos. Los renders de desarrollo ayudan a detectar errores, pero la calidad del
movimiento y del timbre se valora dentro del juego y con una salida de audio real.

Los próximos Stands partirán de una ficha canónica y una decisión documentada
sobre modelo, UV, textura y animaciones, con la procedencia correspondiente. Los controles, balance y progresión se decidirán
mediante pruebas del proyecto.

## Sprites de inventario 0.13.1

Los 14 ítems usan ahora [sprites RGBA64](../art/items/README.md) de arte nuevo
asistido por IA, con fuente maestra y SVG editables. Se unifican borde, iluminación
y escala; cada núcleo usa los rasgos y silueta de su Stand. El atlas de ítems es
independiente de las texturas de entidades y de las entradas de otros mods.
