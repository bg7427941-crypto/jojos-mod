# JoJo's Mod

Mod de Minecraft inspirado en los Stands de JoJo's Bizarre Adventure. Código MIT;
modelos, texturas y sonidos del proyecto deben ser originales o tener una licencia compatible.

La Fase 2 incorpora un Stand de entrenamiento invocable, animado y con un ataque
básico, sobre los ítems y datos persistentes de la Fase 1. Todavía no hay obtención
por flecha, recetas ni loot adicional.

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

La flecha y el disco se apilan de uno en uno; los fragmentos, hasta 64. Por ahora
son ítems de prueba: usarlos no asigna un Stand. Se incluyen nombres en inglés y español.

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
El controlador rechaza IDs no registrados. La sesión activa es un attachment
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
En Minecraft 26.3 se ejecutan **13 tests en total: 12 del mod y uno de Minecraft**.

## Organización

- `registry`: ítems, entidades, definiciones y attachments.
- `stands`: datos, stats y controlador del ciclo de vida.
- `entities`: manifestación temporal del Stand.
- `abilities`: ataque básico autoritativo.
- `network`: intenciones cliente → servidor, sin aceptar objetivo ni daño del cliente.
- `client`: teclas y renderer, aislados del servidor dedicado.
- `commands`: asignación de prueba restringida a operadores.
- `src/gameTest`: mod de pruebas separado de la distribución.
- `assets/jojosmod`: traducciones, modelos y texturas.

Los paquetes `items` y `worldgen` se incorporarán cuando tengan lógica propia.

## Estado de validación en la nube

La compilación y los 13 GameTests han pasado en la nube. El log aportado por el
usuario también confirma compilación, los seis tests de Fase 1 y arranque del cliente
de esa fase en Windows. En Fase 2 se comprobó dentro del juego:

- Asignación con `/standtest assign` e invocación desde R.
- Renderizado del modelo de GeckoLib y seguimiento tras teletransportarse.
- Ataque desde Z: el aldeano de prueba pasó de 20 a 16 puntos de salud.
- Guardado, cierre completo del cliente y nueva invocación tras reabrir el mundo,
  sin repetir la asignación.

El cliente se cerró normalmente después de las comprobaciones.

Dominios adicionales requeridos por la compilación y el cliente:
`maven.neoforged.net`, `libraries.minecraft.net`, `piston-meta.mojang.com`,
`piston-data.mojang.com`, `resources.download.minecraft.net` y `dl.cloudsmith.io`.
