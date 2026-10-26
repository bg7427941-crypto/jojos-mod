# Modelado con Blockbench y GeckoLib

El mod ya utiliza **GeckoLib 5.5.7**. Su [proyecto de Blockbench](../art/star_platinum/star_platinum.bbmodel)
tiene formato `geckolib_model`, 144 cubos, 29 huesos, atlas incrustado de 256×256
y trece animaciones. El renderer utiliza `DefaultedEntityGeoModel` para cargarlos.

Blockbench sirve para editar forma, UV, pintura, pivotes y curvas; GeckoLib
reproduce el resultado en Minecraft. La calidad depende del trabajo artístico,
no solo de elegir la herramienta. Esta guía prepara la edición visual del modelo
existente; no introduce una revisión nueva de su geometría.

## Abrir y editar

1. Instala [Blockbench](https://www.blockbench.net/) y el plugin GeckoLib desde
   Archivo → Plugins. Consulta la [guía oficial de GeckoLib 5](https://wiki.geckolib.com/docs/geckolib5/making-models/blockbench-plugin-usage/).
2. Abre `art/star_platinum/star_platinum.bbmodel` y conserva una copia de trabajo.
   El formato ya es GeckoLib; no hace falta convertirlo a Java Entity. La textura
   está incrustada en el proyecto.
3. Edita piezas y pivotes en Model, textura en Paint y curvas en Animate.
   Comprueba frente, perfil y espalda, tanto en reposo como durante las acciones.
4. Conserva los nombres de los huesos y los trece nombres completos
   `animation.star_platinum.<clip>`. Los UUID vinculan los animadores con sus grupos;
   renombrar o sustituir elementos exige revisar esas referencias.
5. Guarda el `.bbmodel` y las exportaciones finales juntos. El JSON de ejecución
   por sí solo no conserva toda la información de edición.

| Zona | Revisión artística |
| --- | --- |
| Rostro | Ceño, mandíbula, ojos y contraste legibles a distancia de combate |
| Silueta | Hombros amplios, cintura y espacio entre brazos, torso y hombreras |
| Manos | Puño, nudillos y pulgar sin atravesar pecho ni antebrazos |
| Articulaciones | Pivotes de codo, muñeca y rodilla coherentes en guardia y ráfaga |
| Tela y cabello | Pañuelo, faldón y mechones que acompañen la acción sin taparla |
| Movimiento | Anticipación, contacto, recuperación y transición hacia reposo |

La [dirección artística](art-direction.md) conserva las referencias de Parte 3 y
de varios modelos. El diseño, la textura y las curvas del proyecto siguen siendo propios.

## Sincronización con el combate

Mover un keyframe no cambia el daño: el servidor decide los contactos a 20 TPS.

| Clip | Duración | Instante que debe leerse en la pose |
| --- | --- | --- |
| `strike` | 0,60 s | Contacto a 0,20 s |
| `barrage` | 1,50 s | Contactos: 0,20; 0,35; 0,50; 0,65; 0,80; 0,95; 1,10; 1,25 s |
| `star_finger` | 0,90 s | Extensión máxima a 0,40 s; retracción a 0,70 s |
| `intercept` | 0,60 s | Mano preparada de 0,10 a menos de 0,50 s |
| `intercept_catch` | 0,22 s | Captura disparada por el acierto real |
| `leap` | 0,55 s | Impulso al inicio |
| `leap_land` | 0,25 s | Flexión disparada por el aterrizaje real |

Conserva también `idle`, `move`, `summon`, `dismiss`, `guard` y `recover`.
Los dedos extensibles permanecen ocultos fuera de Star Finger. El audio se
sincroniza desde los eventos del servidor; añadirlo también en la línea de tiempo
lo duplicaría. La [recopilación sonora](star-platinum-anime-audio-catalog.md)
relaciona las señales con esos eventos.

## Exportar al mod

La [documentación de exportación](https://wiki.geckolib.com/docs/geckolib5/making-models/exporting-the-files/)
indica Archivo → Exportar → **Export GeckoLib Model** y **Export GeckoLib Animations**.
La textura se guarda con **Save As** desde su panel. Los destinos son:

| Recurso | Ruta desde la raíz |
| --- | --- |
| Modelo | `src/main/resources/assets/jojosmod/geckolib/models/entity/star_platinum.geo.json` |
| Animaciones | `src/main/resources/assets/jojosmod/geckolib/animations/entity/star_platinum.animation.json` |
| Textura | `src/main/resources/assets/jojosmod/textures/entity/star_platinum.png` |
| Fuente editable | `art/star_platinum/star_platinum.bbmodel` |

Estas rutas siguen la [organización de GeckoLib 5](https://wiki.geckolib.com/docs/geckolib5/making-models/placing-the-files/).
El generador `build_original.py` reconstruye esos archivos desde Python:
**regenerar sobrescribe las ediciones hechas solo en Blockbench**. Conserva la
copia editada y sus exportaciones, y traslada los cambios a las fuentes antes de
volver a generar. Actualiza la procedencia y los hashes tras aprobar la revisión.

La exportación actual ya compensa los ejes entre Blockbench y GeckoLib. No añadas
otra inversión manual de X sin comprobar el juego: cambiaría la dirección de las poses.

Compila con `./gradlew build` y prueba con `./gradlew runClient`. Revisa R, Z, X,
C, V, B y J desde varias cámaras: contactos, ropa, dedos, textura y vuelta al reposo
al cancelar. Después, otro cliente debe observar las mismas poses. La [guía 0.9.0](playtest-0.9.0.md)
describe esa prueba. Verificar el formato no sustituye abrir el editor y valorar el resultado.
