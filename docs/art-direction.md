# Identidad visual propia

El primer conjunto pulido se centra en Stardust Crusaders. La obra de JoJo define
el personaje; el modelado, la textura, las poses, los sonidos y la progresión se
crean para este proyecto. Comparar varios mods ayuda a valorar legibilidad y
presentación, sin convertir sus recursos en nuestra plantilla.

## Star Platinum en 0.8.0

| Elemento | Construcción actual |
| --- | --- |
| Modelo | 144 cubos nuevos y 29 huesos, con articulaciones de codo, muñeca, rodilla, tela y cabello |
| Textura | Atlas de 256×256 creado desde una imagen vacía, con UV y pixel art propios |
| Animaciones | Once secuencias nuevas, sincronizadas con los contactos del servidor |
| Sonido | Doce clips sintetizados con osciladores, ruido y resonadores; sin grabaciones externas |
| Sprites | Ocho ítems originales de 32×32, con sus fuentes SVG y generador |
| Partículas y HUD | Composición propia sobre las APIs de Minecraft |
| Habilidades y voluntad | Implementación del proyecto sobre Minecraft, NeoForge y GeckoLib |

Las fuentes editables y las recetas están en [arte de Star Platinum](../art/star_platinum/README.md)
y [síntesis de audio](../art/sounds/original-audio.md). La geometría, las UV, los
píxeles y los keyframes se generan sin leer modelos, texturas ni animaciones ajenos.
El proyecto Blockbench incluye el atlas y todas las secuencias.

Se mantienen los [créditos históricos](history/asset-provenance.md) de los recursos
usados en versiones anteriores. Sus modelos y muestras ya no forman parte de la
presentación de Star Platinum incluida en 0.7.0.

## Referencias consultadas

| Fuente | Uso en esta entrega |
| --- | --- |
| [Portal oficial de Stardust Crusaders](https://jojo-portal.com/en/anime/sc/character/01/) | Identidad y contexto del personaje |
| [Comparación visual del episodio 23](https://jojocomparisons.github.io/jojo-sc-23/) y [ficha de apariencia](https://jojowiki.com/Star_Platinum#Appearance) | Contrastar la paleta del anime y los detalles del rostro y vestuario |
| [Star Platinum de JoJo's Bizarre Survival](https://github.com/Novarch129/JoJo-s-Bizarre-Survival/blob/master/images/star_platinum.png) | Comparar silueta, hombreras y lectura de un personaje cúbico |
| [Presentación de Ripples of the Past](https://github.com/StandoByte/Ripples-of-the-Past/blob/1.16.5/src/main/resources/logo.png) | Comparar proporciones y densidad de detalle en Minecraft |
| [Roundabout](https://github.com/Hydraheads/RoundaboutMod) | Referencia anterior de calidad y respuesta visual; sus recursos se sustituyen en esta entrega |
| Minecraft | Escala, cámara, distancia de lectura y lenguaje de píxeles |

Se contrastó la paleta con fotogramas del anime reproducidos en la comparación:
piel violeta, sombras faciales moradas, cabello negro, ojos cian, corona dorada
con joya plateada, pañuelo rojo, guantes y botas oscuros, faldón y rodilleras claros.
No se incorporan imágenes de esas páginas al atlas. La consulta de referencias
publicadas no equivale a haber jugado esos mods ni prueba compatibilidad con 26.3.

Las proporciones buscan hombros anchos, cintura estrecha, musculatura legible y
cabello escalonado. Las hombreras se construyen con capas; las manos tienen
nudillos y pulgar. Star Finger extiende dos dedos desde una mano articulada.
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

La [prueba 0.7.0](playtest-0.7.0.md) revisa rostro, articulaciones, transición entre
poses, lectura de impactos, visibilidad en primera persona y presentación para los
amigos. Los renders de desarrollo ayudan a detectar errores, pero la calidad del
movimiento y del timbre se valora dentro del juego y con una salida de audio real.

Los próximos Stands partirán también de una ficha canónica, boceto, rig, UV,
textura y animaciones nuevos. Los controles, balance y progresión se decidirán
mediante pruebas del proyecto.
