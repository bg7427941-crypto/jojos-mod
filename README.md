# JoJo's Mod

Mod de Minecraft inspirado en los Stands de JoJo's Bizarre Adventure. Código MIT;
modelos, texturas y sonidos del proyecto deben ser originales o tener una licencia compatible.

La Fase 1 incorpora los ítems y la infraestructura de datos del jugador. Todavía no
hay Stands invocables, habilidades, recetas ni loot adicional.

## Requisitos

- Minecraft **26.3**, NeoForge **26.3.0.95**.
- **JDK 25** y el Gradle Wrapper incluido (**9.2.1**, con SHA-256 fijado).
- No se necesita una instalación global de Gradle.

La base adapta el MDK oficial de NeoForge; su aviso MIT está en `LICENSE-MDK.txt`.
GeckoLib se añadirá en la Fase 2, al introducir la primera entidad animada. Las APIs
nativas de NeoForge cubren la persistencia y la sincronización de esta fase.

## Compilar y ejecutar

Con Java 25 disponible en `JAVA_HOME` y `PATH`, desde la raíz del repositorio:

```sh
./gradlew build
./gradlew runGameTestServer
./gradlew runClient
```

`build` compila también el código de los GameTests; **no los ejecuta**.
`runGameTestServer` ejecuta los cinco casos descritos abajo. El JAR del mod se genera
en `build/libs/` y excluye el mod de pruebas.

El cliente requiere una sesión gráfica. En el entorno de nube preparado:

```sh
source /workspace/.tools/jojos-env.sh
xvfb-run -a ./gradlew runClient
```

Xvfb proporciona una pantalla virtual, sin una ventana visible en el navegador.
Para jugar y revisar visualmente los ítems, usa `runClient` en un equipo con escritorio.

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

Los tres PNG de `src/main/resources/assets/jojosmod/textures/item/` son **placeholders
originales de 16×16**: flecha dorada, fragmento violeta/plata y disco violeta/plata.
Se pueden sustituir conservando sus nombres y rutas. No contienen arte de la serie.

## Datos del jugador y pruebas

`StandData` guarda como máximo un identificador de Stand. `PlayerStands.get` permite
leerlo; `assign(ServerPlayer, Identifier)` y `clear(ServerPlayer)` se usan desde el
hilo del servidor. No se crean datos al leer un jugador sin Stand.

El attachment tiene codec de persistencia, copia al morir y sincronización con el
propietario. Cuando existan definiciones de Stands, la asignación deberá validarse
contra ese registro antes de conectarla a ítems o peticiones del cliente.

Los cinco GameTests comprueban:

1. Registro y tamaños de pila de los tres ítems.
2. Ausencia inicial, asignación única, sustitución y borrado.
3. Serialización y restauración del attachment en jugadores de prueba.
4. Conservación del dato mediante el mecanismo de copia al morir.
5. Codificación de red con y sin Stand asignado.

Los casos de copia y serialización ejercitan los mecanismos de NeoForge; no equivalen
a una muerte real ni a una desconexión/reconexión de un cliente. Esas comprobaciones
y la sincronización entre clientes siguen necesitando validación en juego.

## Organización

- `registry`: ítems, pestaña creativa y attachments.
- `stands`: dato inmutable y API de asignación.
- `src/gameTest`: mod de pruebas separado de la distribución.
- `assets/jojosmod`: traducciones, modelos y texturas.

Los paquetes `entities`, `items`, `abilities`, `worldgen`, `network` y `client`
se incorporarán cuando tengan código propio en las fases siguientes.

## Estado de validación en la nube

Java 25, Gradle, la configuración de tareas, la generación de metadatos y la pantalla
virtual están verificados. La compilación del mod y la ejecución de los cinco
GameTests, cliente y servidor están **pendientes**: el proxy devuelve HTTP 403 al
descargar NeoForm desde `maven.neoforged.net`.

Dominios adicionales requeridos por la compilación y el cliente:
`maven.neoforged.net`, `libraries.minecraft.net`, `piston-meta.mojang.com`,
`piston-data.mojang.com` y `resources.download.minecraft.net`.
La Fase 1 no se considera validada hasta completar esas comprobaciones.
