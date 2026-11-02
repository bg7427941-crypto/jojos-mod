# Instalar JoJo's Mod 0.14.1-alpha.1

## Requisitos

| Componente | Versión de esta alpha |
| --- | --- |
| Minecraft Java Edition | 26.3, exactamente |
| Java | 25 |
| NeoForge | 26.3.0.95, versión probada |
| GeckoLib para NeoForge/Minecraft 26.3 | 5.5.7, versión probada |
| JoJo's Mod | 0.14.1-alpha.1, JAR `-public-alpha.jar` |
| Protocolo del mod | 13 |

El mismo JAR sirve para cliente y servidor dedicado. Cada jugador necesita el
mod y GeckoLib. No funciona en Fabric ni Forge; no mezcles archivos de distintas
versiones o varios JAR de JoJo's Mod en una misma carpeta.

## Cliente

1. Crea un perfil de Minecraft 26.3 con el instalador oficial de
   [NeoForge](https://neoforged.net/). Configura Java 25 en el lanzador.
2. Descarga [GeckoLib](https://modrinth.com/mod/geckolib) 5.5.7 para **NeoForge y 26.3**.
   Copia su JAR a `mods/` del perfil.
3. Descomprime el ZIP de esta alpha. Copia **solo el JAR `-public-alpha.jar`** a
   la misma carpeta `mods/`; los documentos pueden guardarse fuera.
4. Arranca el perfil y comprueba la versión del mod en la lista de mods.
5. Crea un mundo de supervivencia o entra en un servidor con la misma alpha.
   Escribe `/stand journey` y `/stand progress` para empezar.

Una flecha Stardust puede aparecer en los cofres nuevos de templos del desierto
(20 % por cofre); no reemplaza un Stand existente. Usa R/Z/X/C, y V al alcanzar
la voluntad necesaria. B/J son técnicas de Star Platinum y G detiene el tiempo
para usuarios compatibles. Se pueden reasignar en Opciones → Controles.

## Servidor y amigos

1. Instala un servidor NeoForge 26.3.0.95 mediante su instalador oficial, con
   Java 25. Revisa y acepta el EULA de Minecraft antes de arrancarlo.
2. Pon los JAR de GeckoLib y JoJo's Mod público en su carpeta `mods/`.
3. Inicia con el script generado por NeoForge. Mantén los archivos de jugadores,
   mundo y configuración entre sesiones para conservar la progresión.
4. Añade a los jugadores con `whitelist add Nombre` en la consola del servidor:
   Minecraft 26.3 activa la lista permitida por defecto. Conserva la autenticación
   normal de Minecraft en un servidor publicado.
5. Todos los clientes deben instalar las mismas versiones. Para LAN, el anfitrión
   puede abrir su mundo desde el menú de pausa y los demás conectarse normalmente.

La campaña requiere dificultad fácil, normal o difícil para desafiar a DIO. Cada
participante que contribuya puede recibir su propia recompensa; no depende del
último golpe. `/stand claim` recupera recompensas reservadas por inventario lleno.
El avance se conserva al morir y reconectar.

## Actualizar y ajustar

Cierra cliente y servidor y copia el mundo completo antes de actualizar. Retira
el JAR anterior y sustituye el de todos los jugadores y servidor a la vez.
0.14.1-alpha.1 conserva los datos de 0.14.0; volver a versiones más antiguas no
está validado. Los cofres ya abiertos no regeneran su loot, aunque todavía puede
aparecer el guardián al visitar el templo.

El audio público usa efectos sintetizados. Los paquetes de recursos personales
pueden sustituir eventos en el cliente; no se descargan ni envían grabaciones a
los amigos. Activa subtítulos o ajusta Jugadores en Música y sonidos.
El shader temporal respeta la intensidad de efectos de pantalla de Minecraft;
ponla a 0 para desactivarlo. Respeta también Ocultar destellos.

## Si algo falla

- No arranca: revisa Java 25, Minecraft/NeoForge/GeckoLib correctos y JAR duplicados.
- Rechazo al conectar: compara la alpha y las dependencias de cliente/servidor;
  protocolo 13 no es compatible con entregas antiguas de protocolo 12.
- No funciona una tecla: comprueba conflictos en Controles y `/stand progress`;
  algunas técnicas requieren voluntad y no se pueden superponer.
- No aparece una flecha: su probabilidad no garantiza que salga en cada templo.
- Sin recompensa visible: libera un hueco y ejecuta `/stand claim`.
- Problemas gráficos: prueba primero sin shaders ni otros mods. Guarda
  `logs/latest.log` y, si existe, el archivo de `crash-reports/`.

Reporta versión, pasos y logs en
[GitHub Issues](https://github.com/bg7427941-crypto/jojos-mod/issues/new/choose).
