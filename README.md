# JoJo's Mod

Mod de Minecraft inspirado en los Stands de JoJo's Bizarre Adventure. Código MIT;
los recursos originales son MIT y los recursos de terceros mantienen sus licencias.
Consulta [créditos y condiciones de uso](THIRD_PARTY_NOTICES.md).

La meta es una experiencia completa de supervivencia y multijugador, con progresión,
habilidades fieles a JoJo y modelos y animaciones propios de cada Stand. La primera
versión pulida se centrará en **Parte 3: Stardust Crusaders**. La
[visión y criterios de entrega](docs/product-vision.md) guían las próximas fases.
El [plan de Parte 3](docs/part-three-release.md) propone el catálogo y el recorrido,
empezando por Star Platinum; el desarrollo específico de Whitesnake queda para una expansión posterior.
El estado descrito abajo corresponde al prototipo actual.

La **primera prueba de Hierophant Green, 0.11.0**, añade una segunda identidad
jugable de Parte 3. **R** lo invoca y **Z** lanza Emerald Splash: nueve esmeraldas
reales en tres pulsos, preparación de 0,3 s, alcance máximo de 14 bloques y
enfriamiento de 5 s. Su modelo y atlas nuevos, seis animaciones y cinco sonidos
originales acompañan la descarga. La [guía 0.11.0](docs/playtest-0.11.0.md) permite
probar puntería, paredes, progreso y juego con amigos. Cliente y servidor deben
usar **0.11.0, protocolo 9**.

La flecha Stardust sortea ahora **Star Platinum o Hierophant Green al 50 %**.
Las asignaciones existentes se conservan. El primer despertar de Hierophant Green
da +2 de voluntad una vez; causar daño de salud a tres hostiles distintos con
Emerald Splash da +1 y reduce su enfriamiento a 4 s desde la siguiente activación.
Los tentáculos, posesión y barrera de Hierophant Green siguen pendientes; consulta
su [ficha de diseño](docs/hierophant-green-design.md) para el alcance exacto.

La **progresión avanzada 0.10.0** añade dos desafíos de Star Platinum:
Star Finger con contacto a más de tres bloques e intercepción de flechas de
hostiles distintos. Cada desafío completo concede **+1 de voluntad una sola vez**
y mejora el enfriamiento de su habilidad: Star Finger de **6 a 5 s** e intercepción
de **3 a 2,5 s**. El panel y `/stand progress` muestran el avance y las reglas.
La [guía 0.10.0](docs/playtest-0.10.0.md) detalla la prueba de progresión y guardado.
La entrega 0.10.0 usaba el protocolo 8; la actual usa el 9.

La **integración de audio 0.9.3** añade `single_ora`, exclusivo de Z, y admite
`ora.ogg` en el [paquete de recursos local](docs/anime-audio.md). R, Z, X y V
usan voces que se sustituyen y cancelan por jugador. La [guía 0.9.3](docs/playtest-0.9.3.md)
propone la prueba de reproducción e interrupciones. Los ocho clips de prueba
añadidos manualmente en `main` se conservan e incluyen
en el JAR actual; los otros eventos mantienen los efectos sintetizados. Su
[procedencia y condiciones](THIRD_PARTY_NOTICES.md) se distinguen de los recursos
originales. La primera entrega 0.9.3 usaba el protocolo 7; la actual usa el 9.

La **revisión visual 0.9.2** adapta el modelo y el atlas del anime de Star Platinum
de **Roundabout**, por Hydraheads y colaboradores, a nuestro rig de GeckoLib.
Conserva las trece animaciones del proyecto y los tiempos del combate. Las
[vistas frontal, tres cuartos, lateral y trasera](art/star_platinum/preview.png)
permiten valorar su apariencia; la [guía 0.9.2](docs/playtest-0.9.2.md) propone
compararla dentro de Minecraft. Es una adaptación acreditada bajo la licencia
de Roundabout, conservada también en el JAR.
La **prueba jugable 0.9.0** añadió el **salto con Stand (J, voluntad 4)**,
con impulso desde suelo firme, amortiguación limitada y presentación original.
La 0.8.0 añadió intercepción precisa con **B** y voluntad 6,
con una ventana de reacción, animaciones, sonido y destellos propios.
La presentación introducida en 0.7.0 rehace Star Platinum con geometría, rig, UV, textura,
nueve animaciones y diez efectos sonoros originales. Se construye desde proyectos
nuevos, usando la obra y varios modelos como referencias visuales. La
[dirección artística](docs/art-direction.md) y las fuentes editables documentan
esa separación. Conserva Star Finger, los desafíos de voluntad, el panel y los
sprites propios de 0.6.0.
Sigue la [guía de salto 0.9.0](docs/playtest-0.9.0.md), la
[guía de intercepción 0.8.0](docs/playtest-0.8.0.md), la
[guía de presentación 0.7.0](docs/playtest-0.7.0.md), la
[guía de progresión](docs/playtest-0.6.0.md) y la [ficha de combate](docs/star-platinum-design.md).

Conserva Echoes ACT 1, Little Feet, sus flechas, los discos, el guardián y las recetas
anteriores. La parada temporal, los otros Crusaders y DIO siguen pendientes; esta
entrega no completa aún el recorrido de Parte 3. El diseño de Whitesnake se conserva
para una expansión posterior en [Fase 4](docs/phase-four-design.md).

## Star Platinum — prueba jugable

Usa `/give @s jojosmod:stardust_arrow` en un mundo de prueba con un personaje sin
Stand, y haz clic derecho apuntando al aire. En supervivencia se consume una flecha;
en creativo se conserva. La primera asignación concede **+2 de voluntad una sola
vez**, también tras guardar o morir. La voluntad inicial 1 pasa a 3, el requisito
fuerte por defecto para recuperar su disco. La flecha no sustituye otro Stand.

| Tecla | Acción |
| --- | --- |
| **R** | Invocar/desinvocar |
| **Z** | Golpe preciso: 6 puntos, preparación 0,2 s, cooldown 0,8 s |
| **X** | Ráfaga: 8 golpes de 1 punto, cooldown 5 s |
| **C** | Activar/cancelar guardia frontal: 60 % de reducción, máximo 2 s, cooldown 4 s |
| **V** | Star Finger: 4 puntos, alcance 6 bloques, preparación 0,4 s, cooldown 6 s (5 s al dominarlo); requiere 5 de voluntad |
| **B** | Intercepción precisa: neutraliza una flecha frontal entre 0,1 y menos de 0,5 s; cooldown 3 s (2,5 s al dominarla); requiere voluntad 6 |
| **J** | Salto con Stand: impulso desde el suelo; cooldown 4 s; requiere voluntad 4 |

El golpe preciso y la ráfaga alcanzan 3 bloques; Star Finger alcanza 6. Respetan
paredes y reglas PvP/equipos. La guardia
cubre 120 grados frente al usuario, reduce su velocidad un 45 % y no protege de
fuego, caídas ni ataques desde atrás. El daño de salud interrumpe los ataques.
No se pueden superponer acciones de combate. El salto es instantáneo y permite
atacar después del impulso. Todos los tiempos suponen 20 TPS y los cooldowns
se cuentan desde el inicio. Las siete teclas se pueden cambiar en Controles.

Después del despertar, completa desafíos de **precisión**, **ráfaga** y **guardia**.
Cada uno exige usar esa acción contra **3 enemigos hostiles distintos** y concede
**+1 de voluntad una sola vez**. Precisión y ráfaga deben causar daño de salud;
la guardia debe reducir daño físico frontal. Repetir el mismo enemigo no avanza
ese desafío; animales, jugadores y enemigos aliados tampoco cuentan. El progreso
se gana en supervivencia, se guarda al morir y no se reinicia al cambiar de Stand.
Con un desafío y la voluntad inicial se alcanza 4 y se desbloquea el salto con Stand.
Con dos desafíos y la voluntad inicial se alcanza 5 y se desbloquea Star Finger.
Los tres permiten alcanzar 6 y desbloquear la intercepción precisa. Cada jugador obtiene su progreso por sus propias
acciones, incluso contra enemigos compartidos. La voluntad no se consume.

Los dos desafíos avanzados requieren **3 enemigos hostiles distintos** por
categoría. Star Finger cuenta solo daño de salud cuyo punto de contacto esté a
**más de 3 bloques**; usarlo a corta distancia o absorber todo el daño no cuenta.
La intercepción cuenta el tirador hostil de una flecha atrapada correctamente;
flechas de jugadores, criaturas pasivas y dispensadores no dan progreso.
Cada desafío da +1 de voluntad y su reducción de enfriamiento desde la siguiente
activación. El enfriamiento que ya esté corriendo no se modifica. Completar los
cinco desafíos desde la voluntad inicial permite llegar a **8**, sin repetir
recompensas al morir, reconectar o recuperar el Stand.

El panel muestra voluntad y desafíos; **`/stand`** o **`/stand progress`** permiten
consultarlos sin permisos de operador. Los valores de daño, tiempos y requisitos
son decisiones de balance del mod.

Cada cofre nuevo de templo del desierto tiene un **20 %** de probabilidad de añadir
una flecha Stardust, conservando el loot vanilla. Esa flecha tiene su propio sorteo,
que contiene Star Platinum y Hierophant Green al 50 % cada uno; la flecha antigua mantiene Echoes/Little Feet.
El núcleo `jojosmod:star_platinum_core` está disponible en creativo/comandos y se
combina con un disco vacío. El guardián puede guardar y recuperar este Stand.

Las [fuentes editables](art/star_platinum/README.md) incluyen el proyecto adaptado
de Blockbench/GeckoLib, sus entradas de Roundabout fijadas a una revisión concreta,
el conversor reproducible y las curvas de las trece animaciones propias. El atlas
128×128 es el de la variante del anime de Roundabout, conservado sin repintar.
La [guía de Blockbench y GeckoLib](docs/blockbench-geckolib.md) describe la edición
visual del proyecto y la exportación de modelo, textura y animaciones.
Los [catorce efectos sintetizados](art/sounds/original-audio.md) son originales, mono
y espaciales, con subtítulos en inglés/español y volumen bajo Jugadores. La ráfaga
añade una capa de energía una vez al inicio, aire por pulso e impactos al acertar.
Invocación, ORA individual, ráfaga y Star Finger tienen un canal por jugador: una voz nueva
sustituye su señal anterior y una cancelación la interrumpe, conservando los canales
de los amigos. El [paquete de audio local](docs/anime-audio.md) permite personalizar
los eventos. Los ocho clips de prueba incorporados manualmente se documentan por separado;
no son audio original del proyecto.
La [recopilación de referencias del anime](docs/star-platinum-anime-audio-catalog.md)
organiza los candidatos para escuchar y los puntos de integración por ataque.
Los efectos usan partículas nativas y respetan los
ajustes del cliente; el aura está limitada a ocho partículas por segundo y Stand.

## Hierophant Green — Emerald Splash

La [ficha](docs/hierophant-green-design.md) distingue la referencia de Kakyoin y
el balance de esta primera entrega. La descarga sale tras seis ticks y emite tres
esmeraldas en cada uno de los ticks 6, 8 y 10. Cada esmeralda causa un punto antes
de armadura y respeta paredes, guardia frontal, PvP y equipos. El máximo teórico
es nueve puntos por descarga; el abanico y la puntería determinan cuántas aciertan.
No consume ni genera esmeraldas de inventario.

Desinvocar, perder el Stand, recibir daño de salud, morir, desconectar o cambiar de
dimensión cancela pulsos pendientes y vuelo de esa descarga. El enfriamiento se
conserva al interrumpir. La recuperación normal deja terminar las esmeraldas ya
lanzadas. Se usan proyectiles nativos, con recorrido limitado a 14 bloques y vida
máxima de 25 ticks. Los aliados y jugadores protegidos no reciben daño ni bloquean
la trayectoria hacia otros enemigos válidos.

El desafío personal registra tres hostiles distintos que hayan perdido salud.
Cada enemigo cuenta una vez; pasivos, jugadores, aliados, creativo/espectador y
daño totalmente absorbido no dan progreso. Dos amigos pueden progresar contra
los mismos enemigos por sus propias acciones. El panel y `/stand progress`
muestran sus contadores y la bonificación de enfriamiento de 5 a 4 s.
Despertar, voluntad y dominio persisten al guardar, morir y recuperar un disco.

El núcleo de Hierophant Green permite fabricar su disco con un disco vacío, y el
guardián puede guardarlo y recuperarlo. El núcleo está en creativo/comandos; la
obtención de supervivencia en esta entrega usa la flecha Stardust. Las fuentes
[Blockbench/GeckoLib](art/hierophant_green/README.md) incluyen modelo, atlas,
animaciones, icono, sonidos y generadores nuevos del proyecto.

## Requisitos

- Minecraft **26.3**, NeoForge **26.3.0.95**.
- **JDK 25** y el Gradle Wrapper incluido (**9.2.1**, con SHA-256 fijado).
- **GeckoLib 5.5.7**, descargado automáticamente por Gradle y necesario también en el servidor.
- No se necesita una instalación global de Gradle.

La base adapta el MDK oficial de NeoForge; su aviso MIT está en `LICENSE-MDK.txt`.
GeckoLib tiene licencia MIT y gestiona modelo y animaciones. Las APIs nativas de
NeoForge cubren persistencia, sincronización y los paquetes de intención del cliente.

## Compilar y ejecutar

Con Java 25 disponible en `JAVA_HOME` y `PATH`, desde la raíz del repositorio:

```sh
./gradlew build
./gradlew runGameTestServer
./gradlew runClient
```

`build` compila también el código de los GameTests; **no los ejecuta**.
`runGameTestServer` ejecuta los casos descritos abajo. El JAR del mod se genera
en `build/libs/` y excluye el mod de pruebas.

El cliente requiere una sesión gráfica. En el entorno de nube preparado:

```sh
source /workspace/.tools/jojos-env.sh
bash /workspace/jojos-cloud-client.sh
```

Xvfb proporciona una pantalla virtual, sin una ventana visible en el navegador.
Para jugar y revisar visualmente los ítems, usa `runClient` en un equipo con escritorio.
El lanzador de nube usa EGL y `--no-daemon` para que Xvfb y el proceso del juego
compartan la sesión de memoria; esos ajustes no son necesarios en un escritorio normal.

El servidor dedicado se inicia con `./gradlew runServer`. Sus archivos quedan en
`run/server/`, separados de `run/client/` y `run/gametest/`. Si solicita aceptar la
EULA de Minecraft, léela y decide sobre `run/server/eula.txt` antes de reiniciarlo.

La configuración de Gradle del proyecto no contiene credenciales ni direcciones
de proxy. En una máquina con proxy, este debe configurarse localmente para Java/Gradle;
se mantiene la verificación TLS y de checksums.

## Probar los ítems

En un mundo creativo, abre la pestaña **JoJo's Mod**, o ejecuta con permisos:

```mcfunction
/give @s jojosmod:stand_arrow
/give @s jojosmod:stand_disc_fragment
/give @s jojosmod:empty_stand_disc
/give @s jojosmod:stand_disc
```

La flecha y ambos discos se apilan de uno en uno; los fragmentos, hasta 64.
Se incluyen nombres y mensajes en inglés y español.

## Fragmentos y disco vacío — Fase 4A

Cada cofre principal de Ancient City, con tabla `minecraft:chests/ancient_city`,
tiene un **25 %** de probabilidad de añadir **1–2 fragmentos de disco**. Se conserva
el loot vanilla, incluidos sus componentes de ítem y cantidades. Los cofres de hielo
(`minecraft:chests/ancient_city_ice_box`) conservan su loot y no reciben fragmentos.
Los cofres cuyo contenido ya se generó no se rellenan retroactivamente.

En una mesa de crafteo, coloca **7 fragmentos de disco**, **1 fragmento de eco** y
**1 lingote de netherita** para producir **un disco vacío**:

```text
F F F
F E F
F N F
```

F = fragmento de disco, E = fragmento de eco, N = lingote de netherita.
La receta aparece en el libro al obtener un fragmento de disco. Todos los ingredientes
se consumen al fabricar; no hay una receta alternativa solo con fragmentos.

Para probarla en un mundo con comandos, consigue una mesa y los materiales:

```mcfunction
/give @s minecraft:crafting_table
/give @s jojosmod:stand_disc_fragment 7
/give @s minecraft:echo_shard
/give @s minecraft:netherite_ingot
```

`jojosmod:empty_stand_disc` es el disco vacío nuevo. El ítem `jojosmod:stand_disc`
conserva su ID y no sustituye al disco vacío en ninguna receta; los ejemplares antiguos
sin identidad siguen sin equipar ningún Stand.
El disco vacío se combina con un núcleo para fabricar un disco con identidad (ver abajo).
El guardián de Fase 4C ya permite guardar el Stand actual. Su combate como jefe y el
robo con Whitesnake siguen pendientes.
Los discos vacío y cargado tienen sprites distintos de 32×32.

El loot usa el mismo `neoforge:add_table` que las flechas. Sus recursos están en
`data/jojosmod/loot_modifiers/stand_fragments_in_ancient_cities.json` y
`data/jojosmod/loot_table/chests/stand_disc_fragments.json`. La receta y su descubrimiento
usan los sistemas nativos de Minecraft, en `recipe/empty_stand_disc.json` y
`advancement/recipes/misc/empty_stand_disc.json`. No se añaden dependencias.

## Núcleos, discos identificados y voluntad — Fase 4B

Un **disco vacío + un núcleo de Stand** producen **un disco con el Stand del núcleo**.
La receta no tiene forma y funciona tanto en el inventario 2×2 como en la mesa 3×3.
Consume ambos ingredientes y se descubre al obtener el núcleo correspondiente.
Los núcleos no se apilan y tienen iconos propios.

Esta entrega incluye núcleos de Echoes ACT 1, Little Feet, Star Platinum y Hierophant Green. Se obtienen en la pestaña
creativa o mediante comandos de operador; **todavía no tienen loot ni receta propia**,
porque su obtención mediante encuentros sigue pendiente:

```mcfunction
/give @s jojosmod:empty_stand_disc
/give @s jojosmod:echoes_act_1_core
/give @s jojosmod:little_feet_core
```

El resultado usa el ID existente `jojosmod:stand_disc` y el componente nativo
`jojosmod:stand_id`. El nombre muestra el Stand contenido y su identidad se conserva
al guardar y sincronizar el inventario. Los cuatro discos identificados también aparecen
en creativo. Para obtener uno directamente con comandos:

```mcfunction
/give @s jojosmod:stand_disc[jojosmod:stand_id="jojosmod:echoes_act_1"]
/give @s jojosmod:stand_disc[jojosmod:stand_id="jojosmod:little_feet"]
```

Haz clic derecho apuntando al aire con el disco en cualquiera de las manos. El servidor
exige un jugador vivo, que no sea espectador, sin ningún Stand asignado y con voluntad
suficiente. Se consume el disco en supervivencia y se conserva en creativo. Pulsa **R**
para invocar el Stand equipado. No se pierde salud ni se consume voluntad.
Si ya tienes un Stand, el disco se conserva y no sustituye la asignación ni la manifestación.
Los discos antiguos sin componente y los de identidad desconocida se rechazan intactos;
no se convierten gratuitamente en discos vacíos ni sirven como ingrediente sustituto.

La voluntad inicial es **1**, también para jugadores de partidas anteriores. Es un dato
persistente separado del Stand y de la experiencia: sobrevive al guardado y a la copia al
morir, y no se reinicia al cambiar o borrar la asignación. Solo se sincroniza al propietario.
Echoes ACT 1 y Little Feet requieren **1** por defecto. El administrador puede cambiar
`willpower.basicStandRequirement` en `config/jojosmod-synced.toml` del servidor o instancia.
NeoForge también admite la configuración específica del mundo en `serverconfig/`.
`strongStandRequirement = 3` se aplica a Star Platinum.
Estos valores son **provisionales**; Star Platinum ofrece +2 por despertar y
tres desafíos de entrenamiento de +1 cada uno.
Los comandos de prueba, restringidos a operadores, permiten consultar y ajustar el valor propio:

```mcfunction
/standtest willpower
/standtest willpower 0
/standtest willpower 1
```

Con voluntad 0 y el requisito por defecto, el uso del disco se rechaza sin consumirlo;
con voluntad 1, se puede equipar. El valor permitido va de 0 a 1 000 000.
Esta restricción se aplica a los discos; la flecha conserva las reglas aprobadas de Fase 3.
Bajar la voluntad o subir el requisito no elimina retroactivamente un Stand ya asignado.

## Guardar el Stand con el guardián — prototipo de Fase 4C

El **Guardián de discos** es un NPC persistente de servicio. Por ahora se crea con
permisos de operador; permanece donde aparece, no recibe daño ni ofrece recompensas
y reutiliza el modelo y la animación originales del maniquí. Su aparición natural,
localización, combate como jefe y recompensa de núcleo se implementarán después.

```mcfunction
/summon jojosmod:disc_keeper ~ ~ ~2
/give @s jojosmod:empty_stand_disc
```

Con un Stand asignado, acércate a **3 bloques o menos**, sin paredes entre ambos.
Sostén **un disco vacío** en cualquiera de las manos y haz clic derecho sobre el guardián:

1. Sin agacharte, muestra un aviso que explica que guardar el Stand lo retirará del personaje.
2. **Agáchate y vuelve a interactuar** para realizar la transferencia voluntaria.
3. El disco vacío se transforma, en esa misma mano, en un disco con tu Stand. La
   asignación se borra y la manifestación activa desaparece. El guardián sigue disponible.

El servicio también funciona con el Stand desinvocado y con el inventario lleno: no
necesita una casilla libre ni arroja el disco al suelo. Si el disco está en la mano
secundaria, lo usa aunque sostengas otro objeto en la principal. Si ambas manos
tienen un disco vacío, se usa el de la interacción recibida (normalmente, la principal).
Transforma el disco también en creativo; no conserva una copia adicional del disco vacío.
Su único coste provisional es el disco vacío entregado. No consume salud ni voluntad.

El servidor comprueba el estado del jugador y del guardián, la dimensión, el alcance,
la visión, la acción de agacharse, el ítem en la mano y la identidad del Stand antes
de modificar nada. Un jugador sin Stand, una identidad desconocida o un ítem
incorrecto conservan sus datos y objetos. El servicio solo guarda el Stand del
jugador que interactúa, y cada jugador puede utilizar al mismo guardián.

El disco resultante permite recuperar el mismo Stand mediante su uso normal,
con los requisitos de voluntad de Fase 4B. También puedes obtener otro Stand con una
flecha mientras tu asignación esté vacía. Guardar y recuperar no reinicia la voluntad.
La transferencia usa el inventario y los datos nativos del jugador, sin un almacén
adicional en el NPC ni un paquete que acepte una identidad enviada por el cliente.

Para retirar un guardián de prueba cercano, un operador puede ejecutar:

```mcfunction
/kill @e[type=jojosmod:disc_keeper,sort=nearest,limit=1,distance=..5]
```

## Obtener un Stand con la flecha

1. Usa un jugador sin Stand. Para reiniciar una prueba, un operador puede ejecutar
   `/standtest clear`; `/standtest assign` asigna el maniquí y bloquearía la flecha.
2. Consigue una flecha en un cofre de mazmorra vanilla, o usa
   `/give @s jojosmod:stand_arrow` para probarla.
3. Con la flecha en cualquiera de las manos, haz clic derecho apuntando al aire.
   El servidor sortea **Echoes ACT 1 (50 %)** o **Little Feet (50 %)** y muestra su nombre.
4. Pulsa **R** para invocar/desinvocar y **Z** para golpear en la dirección de tu mirada.

En supervivencia se consume una flecha al obtener el Stand. En creativo se conserva.
No hay daño ni riesgo de muerte al usarla. Si ya tienes cualquier Stand asignado,
se rechaza el uso sin gastar la flecha ni cambiar la asignación, también en creativo.
La asignación persiste entre guardados y muertes; no se necesita repetir la flecha
al volver a entrar. La invocación activa es temporal: pulsa R de nuevo tras reconectar.

Los dos Stands usan **el mismo maniquí y las mismas animaciones provisionales
originales** de la Fase 2. No incluyen todavía sonido escrito, reducción de tamaño,
evoluciones ni otras habilidades propias de la serie. Sus valores son de balance
del prototipo, no una conversión literal de los parámetros del manga:

| Stand | Daño sin armadura | Cooldown | Separación máxima |
| --- | --- | --- | --- |
| Echoes ACT 1 | 2 puntos (1 corazón) | 20 ticks (1 s) | 8 bloques |
| Little Feet | 2 puntos (1 corazón) | 10 ticks (0,5 s) | 4 bloques |

Ambos golpes tienen alcance de 3 bloques, respetan paredes y las reglas PvP, y
consumen cooldown aunque fallen. El maniquí de entrenamiento queda fuera del sorteo.

Cada cofre de mazmorra con tabla `minecraft:chests/simple_dungeon` tiene un **10 %**
de probabilidad de añadir **una flecha**, independientemente del loot vanilla.
Se usa el modificador estándar `neoforge:add_table`, sin sustituir la tabla de
Minecraft ni añadir flechas a otras estructuras. No hay receta en esta fase.
Los cofres cuyo loot ya fue generado no se rellenan retroactivamente.

El modificador está en `data/jojosmod/loot_modifiers/stand_arrow_in_dungeons.json`
y la probabilidad en `data/jojosmod/loot_table/chests/stand_arrow.json`. Son recursos
reemplazables por datapacks. En 26.3 las condiciones usan `condition` singular y
el discriminador `type`; no se usa el antiguo listado `global_loot_modifiers.json`.

Referencias para la obtención por flecha y el loot:

| Referencia | Licencia | Aplicación | Uso en este proyecto |
| --- | --- | --- | --- |
| [Ripples of the Past](https://github.com/StandoByte/Ripples-of-the-Past/tree/1.16.5) | GPL-3.0 | Obtención de Stands con flechas | Inspiración; implementación propia, sin copiar código GPL al proyecto MIT |
| [NeoForge](https://github.com/neoforged/NeoForge) | LGPL-2.1 | Uso de ítems, datos y loot aditivo | Dependencia actual; se reutilizan sus APIs y `neoforge:add_table` |

Ripples usa Forge 1.16.5 y no es una dependencia compatible con este proyecto.
GeckoLib continúa como dependencia MIT para la manifestación animada compartida.

## Probar el Stand de entrenamiento

Arranca `./gradlew runClient` y entra en un mundo con trucos, o con permisos de
operador si pruebas un servidor multijugador. Ejecuta:

```mcfunction
/standtest assign
```

- **R** invoca o desinvoca tu Stand.
- **Z** realiza un golpe básico en la dirección de tu mirada.
- Ambas teclas se pueden cambiar en Controles, en la categoría **JoJo's Mod — Stands**.
- `/standtest clear` elimina la asignación de prueba y su entidad.

El servidor valida la asignación, el propietario, la línea de visión y el cooldown.
El golpe tiene alcance de **3 bloques**, daño base de **4 puntos** (2 corazones sin
armadura) y cooldown de **10 ticks** (0,5 s a 20 TPS), incluso si falla. Respeta las
reglas PvP del servidor. Invocar/desinvocar tiene un cooldown de 5 ticks y no reinicia
el del ataque.

La entidad flota cerca de su propietario y se recoloca si supera **8 bloques** de
separación. Desaparece al desinvocar, morir, desconectarse, cambiar de dimensión o
perder la asignación. La asignación sí se guarda: tras reconectar, vuelve a pulsar R.
Solo puede haber una manifestación por jugador.

El maniquí violeta/cian/plata, su textura y las animaciones idle/golpe son
**placeholders originales**. Los recursos están en `geckolib/models/entity/`,
`geckolib/animations/entity/` y `textures/entity/`, dentro de `assets/jojosmod/`.
Este prototipo no tiene salud ni refleja daño al propietario; esa mecánica y su
configuración se implementarán en una fase posterior. Los valores actuales sirven
para probar el sistema, no constituyen el balance final.

Los ocho sprites de `src/main/resources/assets/jojosmod/textures/item/` son
originales de **32×32**, con fuentes SVG y generador en [art/items](art/items/README.md).
Flechas, discos, fragmentos y cada núcleo tienen su propia identidad visual.

## Datos del jugador y pruebas

`StandData` guarda como máximo un identificador de Stand. `PlayerStands.get` permite
leerlo; `assign(ServerPlayer, Identifier)` y `clear(ServerPlayer)` se usan desde el
hilo del servidor. No se crean datos al leer un jugador sin Stand.

El attachment tiene codec de persistencia, copia al morir y sincronización con el
propietario. `ModStands` contiene las definiciones registradas en código, con seis
stats de 1 a 5; los valores antiguos se derivan de poder, velocidad y alcance. Star Platinum usa
un perfil de combate explícito, separado de los parámetros narrativos.
El controlador rechaza IDs no registrados. La identidad de cada manifestación se
sincroniza como dato de la entidad con los clientes que la observan, sin publicar
el attachment privado del propietario. La sesión activa es un attachment
exclusivo del servidor que no se guarda ni se copia al morir.

Los cinco GameTests de Fase 1 comprueban:

1. Registro y tamaños de pila de los tres ítems.
2. Ausencia inicial, asignación única, sustitución y borrado.
3. Serialización y restauración del attachment en jugadores de prueba.
4. Conservación del dato mediante el mecanismo de copia al morir.
5. Codificación de red con y sin Stand asignado.

Los casos de copia y serialización ejercitan los mecanismos de NeoForge; no equivalen
a una muerte real ni a una desconexión/reconexión de un cliente. Además se comprobó
manualmente la persistencia tras cerrar y volver a abrir el cliente. La muerte con
respawn de un cliente real sigue pendiente de prueba manual. La sesión LAN de dos
clientes de 0.5.0 se describe abajo; la copia al morir tiene GameTests.

Los siete GameTests de Fase 2 comprueban rechazo de asignaciones inválidas,
invocación única y cooldown, aislamiento por propietario, seguimiento y rango,
daño y recuperación del cooldown, bloqueo por paredes y limpieza del ciclo de vida.
Los siete GameTests de Fase 3 ejercitan ambos resultados de la flecha con semillas
fijas, consumo en supervivencia, rechazo de asignaciones existentes y usuarios no
válidos, creativo/mano secundaria, guardado/copia al morir y combate con estadísticas
de cada definición. El caso de loot compara el resultado modificado con el original
para 256 semillas y comprueba que una tabla ajena no reciba flechas.
Los cuatro GameTests de Fase 4A comprueban el registro del disco vacío, la receta
cargada y el consumo al recoger su resultado en una mesa real; rechazan ingredientes
ausentes o sustitutos baratos. Comparan el loot vanilla y modificado para 256 semillas,
comprueban las tablas excluidas y prueban el umbral exacto del 25 % con tiradas controladas.
La Fase 4B añade diez GameTests de recetas y consumo de núcleos, sustituciones inválidas,
equipamiento de ambas identidades, conservación de la asignación y manifestación,
usuarios y discos inválidos, creativo/mano secundaria, umbral de voluntad,
guardado y red de los ítems, y persistencia/copia al morir/sincronización de la voluntad.
Los nueve GameTests de Fase 4C prueban consentimiento y almacenamiento en supervivencia
y creativo, ingredientes incorrectos, asignaciones desconocidas, usuarios inválidos,
dimensión, alcance y paredes, inventario lleno y mano secundaria, guardado/carga
nativos del jugador y del guardián, recuperación del Stand y aislamiento entre jugadores.
La entrega Star Platinum añade diez GameTests de tiempos, daño, interrupciones,
guardia, PvP/equipos con controles positivos, paredes, alcance, loot, recetas,
almacenamiento y persistencia del hito. Esa primera entrega reunió **53 tests:
52 del mod y uno de Minecraft**; las entregas posteriores amplían la batería.

## Organización

- `registry`: ítems, entidades, definiciones y attachments.
- `items`: obtención por flecha y equipamiento de discos desde el servidor.
- `stands`: datos, stats, ciclo de vida y transferencia voluntaria a discos.
- `entities`: manifestación temporal del Stand y guardián persistente de servicio.
- `abilities`: rayos de ataque, acciones temporizadas y defensa resueltos en servidor.
- `network`: intenciones cliente → servidor, sin aceptar objetivo ni daño del cliente.
- `client`: teclas y renderer, aislados del servidor dedicado.
- `commands`: asignación y voluntad de prueba restringidas a operadores.
- `src/gameTest`: mod de pruebas separado de la distribución.
- `assets/jojosmod`: traducciones, modelos y texturas.
- `data/jojosmod`: loot adicional de mazmorras y Ancient Cities, recetas y descubrimiento.

El paquete `worldgen` se incorporará cuando tenga lógica propia; la inyección de
loot actual solo necesita recursos de datos.

## Estado de validación en la nube

La base anterior pasó compilación y 43 GameTests en la nube. El log aportado por el
usuario también confirma compilación, los seis tests de Fase 1 y arranque del cliente
de esa fase en Windows. En Fase 2 se comprobó dentro del juego:

- Asignación con `/standtest assign` e invocación desde R.
- Renderizado del modelo de GeckoLib y seguimiento tras teletransportarse.
- Ataque desde Z: el aldeano de prueba pasó de 20 a 16 puntos de salud.
- Guardado, cierre completo del cliente y nueva invocación tras reabrir el mundo,
  sin repetir la asignación.

En Fase 3 se comprobó manualmente con un cliente real:

- Obtención de Little Feet con clic derecho, consumo de la flecha en supervivencia
  y conservación de los 20 puntos de salud del jugador.
- Rechazo de una segunda flecha, con mensaje y conservación del ítem.
- Invocación y golpe desde las teclas: el aldeano pasó de 16 a 14 puntos de salud.
- Cierre completo del cliente, reapertura del mundo y nueva invocación con R,
  sin reasignar el Stand ni gastar otra flecha.

El loot, ambos resultados del sorteo y el modo creativo se validaron mediante
GameTests. La obtención abriendo un cofre natural de mazmorra y una sesión con dos
clientes remotos no se han probado manualmente.

En Fase 4A se comprobó con el cliente el descubrimiento de la receta al recibir
fragmentos, su aparición en el libro y la colocación automática de los ingredientes.
Al fabricar con 7 fragmentos, un eco y una netherita, la cuadrícula quedó vacía y
el inventario contenía exactamente un `jojosmod:empty_stand_disc`. Se verificaron
su nombre y su modelo provisional. El loot de Ancient City se comprobó con los
GameTests; no se abrió manualmente un cofre natural de esa estructura.
Los clientes se cerraron normalmente después de las comprobaciones.

En Fase 4B se comprobó con el cliente el nombre del disco identificado, el rechazo
por voluntad 0, el equipamiento de Echoes ACT 1 con voluntad 1 y su consumo en
supervivencia, y la invocación con R. Con el Stand activo, un disco de Little Feet
en la mano secundaria se rechazó sin consumirlo ni sustituir la manifestación.
Las recetas de ambos núcleos, los datos de ítem y la copia al morir de la voluntad
se verificaron mediante GameTests. La obtención natural de núcleos y los hitos
de voluntad siguen pendientes, al igual que la comprobación con dos clientes remotos.

En el prototipo de Fase 4C se comprobó con el cliente el renderizado y nombre del
guardián, el aviso sin agacharse y el almacenamiento de Echoes ACT 1 al agacharse.
El disco vacío de la mano principal se transformó en un disco con la identidad
`jojosmod:echoes_act_1`. Al usarlo apuntando al aire, se consumió y permitió recuperar
e invocar el mismo Stand con R. La voluntad permaneció en 7 durante todo el recorrido.
El inventario lleno, las dos manos, los usuarios inválidos y el guardado/carga nativo
del jugador y del NPC se comprobaron mediante GameTests. El aislamiento entre jugadores
se probó con FakePlayers; todavía no se ha hecho una sesión con dos clientes remotos.

En **0.5.0**, `build` y los **53 GameTests** han pasado. Se verificaron los JSON,
las referencias a huesos, los límites de UV y la correspondencia de las ocho
animaciones con el proyecto Blockbench. El JAR contiene los recursos finales y
excluye el mod de pruebas.

Con un cliente real se comprobó la obtención de Star Platinum y el hito de +2,
su invocación con R, el modelo frontal y el golpe con Z (objetivo de 20 a 14 de
salud). Tras restablecerlo, X lo dejó en 12. Con **dos clientes de desarrollo en
LAN**, ambos despertaron e invocaron Star Platinum; el segundo observó el modelo
y la guardia animada del primero, incluida la vuelta al reposo. La guardia también
mostró su aviso en primera persona. Se conservaron capturas de esta comprobación.

La dirección y reducción del daño de guardia, sus tiempos, interrupciones, paredes,
PvP/equipos, recetas, almacenamiento y persistencia se verificaron con GameTests.
La sesión LAN no sustituye una prueba de servidor dedicado ni valida el recorrido
completo en supervivencia. La muerte/reconexión con dos jugadores y el balance de
combate compartido siguen pendientes de prueba manual. La [guía 0.5.0](docs/playtest-0.5.0.md)
propone la revisión visual y de jugabilidad que debe hacer el jugador.

En **0.5.1**, la compilación y los **53 GameTests** han pasado de nuevo tras integrar
sonidos y partículas. La geometría y las animaciones se contrastaron con las
funciones del exportador oficial de Blockbench: 57 huesos, 69 cubos y 769 keyframes,
con UV orientadas y referencias válidas. El JAR contiene las siete muestras de
sonido y los avisos de licencias de terceros, y excluye el mod de pruebas.

Se abrió el cliente con el modelo nuevo: se revisaron reposo, invocación, guardia,
articulaciones, aura y subtítulos. El golpe dejó al objetivo en 14 de salud y la
ráfaga en 12 tras restablecer sus 20 puntos. El impacto mostró partículas y los
subtítulos de movimiento del puño e impacto. Los recursos de sonido se cargan sin
avisos de archivos o eventos ausentes. El volumen y el timbre final requieren
valoración con una salida de audio real; en la nube la salida es virtual. La
sesión compartida de 0.5.0 no se ha repetido con esta revisión visual; la [guía
0.5.1](docs/playtest-0.5.1.md) incluye la comprobación con un amigo.

En **0.6.0**, `build` y los **65 GameTests** han pasado: los doce casos nuevos
cubren Star Finger, premios únicos, enemigos compartidos, formato de guardado
anterior, copia al morir y sincronización. Se comprobaron JSON, idiomas, referencias
de audio, conservación del cuerpo y atlas, exportación reproducible y construcción
parcial del paquete de audio con rechazo de entradas inválidas y sobrescritura.

En el cliente, desde voluntad 3, precisión y ráfaga se completaron contra tres husks
distintos por categoría y concedieron +1 cada una. El panel mostró 3 → 4 → 5 y el
desbloqueo de V. Star Finger dejó a un objetivo situado a cinco bloques en 16 de
sus 20 puntos de salud. La revisión con dos clientes no se ha repetido para 0.6.0;
el balance de progresión y la claridad visual requieren valoración del jugador.
La [guía 0.6.0](docs/playtest-0.6.0.md) detalla esa prueba y el recorrido con un amigo.

En **0.7.0**, los **65 GameTests** vuelven a pasar con el canal de sonido por
propietario y el protocolo 4. La presentación se ha reconstruido desde cero:
144 cubos, 29 huesos, atlas de 256×256, nueve animaciones y diez sonidos sintetizados.
Se comprobaron UV y pivotes del proyecto Blockbench, atlas incrustado, hashes de
procedencia, regeneración idéntica y referencias de los ocho eventos sonoros.

La revisión nativa mostró una diferencia entre la guardia y su vista de desarrollo;
se corrigió la reflexión del eje X en la exportación y se comprobó de nuevo en
Minecraft. Se observaron guardia, ráfaga y extensión de dos dedos, con sus
subtítulos; Star Finger dejó al objetivo situado a cinco bloques en 16/20 de salud.
No se ha repetido la sesión de dos clientes con estos recursos. El aspecto, la
fluidez y el timbre final requieren la [valoración del jugador](docs/playtest-0.7.0.md)
con salida de audio real.

En **0.8.0**, `build` y los **74 GameTests** pasan, incluidos nueve casos de
intercepción, elegibilidad, límites exactos, Perforación y uso por dos propietarios.
La intercepción precisa se desbloquea con voluntad 6 y B. La ventana
neutraliza una flecha frontal sin Perforación entre los ticks 2 y 9, termina al
acertar y mantiene un cooldown de 60 ticks. Dos animaciones y dos sonidos nuevos
completan la señal de preparación y acierto. La [guía 0.8.0](docs/playtest-0.8.0.md)
explica su uso, contrajuego y prueba con amigos.

En el cliente real, B mostró la pose, el aviso y los subtítulos. Con flechas que
recorren la física del mundo, la intercepción frontal conservó 20/20 de salud;
un disparo desde atrás y otro fuera de la ventana causaron daño. La sesión de
dos clientes y la dificultad de reaccionar con latencia requieren valoración del jugador.

En **0.9.0**, `build` y los **84 GameTests** pasan, incluidos diez casos de salto:
elegibilidad, dirección, apoyo/techo, exclusión con combate, daño limitado de la
primera caída, limpieza, expiración y cooldowns independientes. Una regresión
reproduce el aterrizaje especulativo del servidor y comprueba que conserve la
amortiguación hasta la caída real. El salto con J se desbloquea con voluntad 4;
incluye dos animaciones y dos sonidos nuevos desde fuentes propias, conservando
las once animaciones y doce sonidos anteriores. El JAR reúne trece animaciones
y catorce muestras originales y excluye las herramientas y clases de prueba.

En el cliente real, J con voluntad 4 produjo un salto de unos 4,4 bloques de altura
y 5,4 de desplazamiento sobre suelo plano, conservando **20/20 de salud**. Al salir
desde una plataforma diez bloques más alta que el suelo de llegada, el aterrizaje
dejó **13/20**: la amortiguación no elimina el daño de caídas grandes. Se revisaron
la tecla, el panel y la carga de los recursos. La sesión con dos clientes no se ha
repetido para 0.9.0; la sensación de movimiento, la presentación y el audio con una
salida real quedan en la [guía de prueba](docs/playtest-0.9.0.md).

En **0.9.1**, `build` pasa y Star Platinum usa el cuerpo del jugador clásico:
cabeza 8×8×8, torso continuo 8×12×4 y extremidades 4×12×4, con 80 cubos, 29 huesos
y trece animaciones. Se ajustaron guardia, intercepción y la defensa de Star Finger
a los guantes y la cabeza mayores. Se comprobaron medidas, contactos, UV,
procedencia y regeneración idéntica; los catorce sonidos anteriores se conservan.
En el cliente se cargó el conjunto y se observó el modelo frontal junto al jugador.
La [guía 0.9.1](docs/playtest-0.9.1.md) propone la valoración visual y con un amigo;
la batería de GameTests no se repitió para esta revisión de recursos.

En **0.9.2**, `build` pasa con la adaptación acreditada del modelo y atlas de
Roundabout: 79 cubos, 29 huesos, atlas anime 128×128 y trece clips propios. Se
comprobaron la procedencia fijada, la regeneración idéntica, UV, contactos y
contenido del JAR. Las animaciones de ejecución y los catorce OGG son idénticos
a 0.9.1. En Minecraft se invocó con R y se observó el modelo frontal junto al
jugador. No se repitieron los GameTests para esta revisión de recursos. Las
[vistas](art/star_platinum/preview.png) y la [guía 0.9.2](docs/playtest-0.9.2.md)
conservan la revisión visual del modelo integrado en la rama principal.

En **0.9.3**, `build` pasa. Se verificaron las 84 pruebas anteriores del servidor
y el nuevo caso de voces por propietario en ejecución focalizada; también pasan
dos pruebas del constructor del ZIP. El ORA opcional pertenece solo a Z, no se
repite durante X/V y se cancela por propietario. Se comprobó que los catorce OGG
del JAR no cambian. El pack local de escucha incluye ocho clips para siete eventos
y conserva su procedencia fuera del repositorio público.

Dominios adicionales requeridos por la compilación y el cliente:
`maven.neoforged.net`, `libraries.minecraft.net`, `piston-meta.mojang.com`,
`piston-data.mojang.com`, `resources.download.minecraft.net` y `dl.cloudsmith.io`.
