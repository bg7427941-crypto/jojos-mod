# JoJo's Mod

Mod de Minecraft inspirado en los Stands de JoJo's Bizarre Adventure. Código MIT;
modelos, texturas y sonidos del proyecto deben ser originales o tener una licencia compatible.

La Fase 3 permite obtener **Echoes ACT 1** o **Little Feet** usando una Flecha de
Stand. Ambos se invocan, siguen al jugador y tienen un ataque básico con autoridad
del servidor. Las flechas aparecen como loot adicional en mazmorras vanilla.
Los discos y fragmentos siguen siendo ítems sin comportamiento; corresponden a la Fase 4.

El diseño ampliado de núcleos, voluntad y extracción con Whitesnake está en
[la propuesta de Fase 4](docs/phase-four-design.md), con los requisitos confirmados
y los valores de balance pendientes de aprobación.

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
/give @s jojosmod:stand_disc
```

La flecha y el disco se apilan de uno en uno; los fragmentos, hasta 64.
Se incluyen nombres y mensajes en inglés y español.

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

Los tres PNG de `src/main/resources/assets/jojosmod/textures/item/` son **placeholders
originales de 16×16**: flecha dorada, fragmento violeta/plata y disco violeta/plata.
Se pueden sustituir conservando sus nombres y rutas. No contienen arte de la serie.

## Datos del jugador y pruebas

`StandData` guarda como máximo un identificador de Stand. `PlayerStands.get` permite
leerlo; `assign(ServerPlayer, Identifier)` y `clear(ServerPlayer)` se usan desde el
hilo del servidor. No se crean datos al leer un jugador sin Stand.

El attachment tiene codec de persistencia, copia al morir y sincronización con el
propietario. `ModStands` contiene las definiciones registradas en código, con seis
stats de 1 a 5; poder, velocidad y alcance determinan los valores del prototipo.
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
respawn de un cliente real y una sesión con dos clientes remotos no se han probado
manualmente; la copia al morir y el aislamiento entre propietarios sí tienen GameTests.

Los siete GameTests de Fase 2 comprueban rechazo de asignaciones inválidas,
invocación única y cooldown, aislamiento por propietario, seguimiento y rango,
daño y recuperación del cooldown, bloqueo por paredes y limpieza del ciclo de vida.
Los siete GameTests de Fase 3 ejercitan ambos resultados de la flecha con semillas
fijas, consumo en supervivencia, rechazo de asignaciones existentes y usuarios no
válidos, creativo/mano secundaria, guardado/copia al morir y combate con estadísticas
de cada definición. El caso de loot compara el resultado modificado con el original
para 256 semillas y comprueba que una tabla ajena no reciba flechas.
En Minecraft 26.3 se ejecutan **20 tests en total: 19 del mod y uno de Minecraft**.

## Organización

- `registry`: ítems, entidades, definiciones y attachments.
- `items`: obtención por flecha desde el servidor.
- `stands`: datos, stats y controlador del ciclo de vida.
- `entities`: manifestación temporal del Stand.
- `abilities`: ataque básico autoritativo.
- `network`: intenciones cliente → servidor, sin aceptar objetivo ni daño del cliente.
- `client`: teclas y renderer, aislados del servidor dedicado.
- `commands`: asignación de prueba restringida a operadores.
- `src/gameTest`: mod de pruebas separado de la distribución.
- `assets/jojosmod`: traducciones, modelos y texturas.
- `data/jojosmod`: tabla adicional y modificador de loot de mazmorras.

El paquete `worldgen` se incorporará cuando tenga lógica propia; la inyección de
loot actual solo necesita recursos de datos.

## Estado de validación en la nube

La compilación y los 20 GameTests han pasado en la nube. El log aportado por el
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
Los clientes se cerraron normalmente después de las comprobaciones.

Dominios adicionales requeridos por la compilación y el cliente:
`maven.neoforged.net`, `libraries.minecraft.net`, `piston-meta.mojang.com`,
`piston-data.mojang.com`, `resources.download.minecraft.net` y `dl.cloudsmith.io`.
