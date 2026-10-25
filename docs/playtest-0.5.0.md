# Prueba de Star Platinum — 0.5.0

Esta entrega permite probar su primer combate y su modelo animado. Todavía no es
la versión completa de Parte 3: faltan otros Stands, encuentros y parada temporal.

## Instalar

Usa **Minecraft 26.3**, **NeoForge 26.3.0.95** y **GeckoLib 5.5.7**. Coloca
`jojosmod-0.5.0.jar` y el JAR de GeckoLib en `mods/`, sustituyendo el JAR anterior
de JoJo's Mod. Todos los participantes y el servidor necesitan las mismas versiones.
Para compilarlo desde el código, usa Java 25 y `./gradlew build`; el resultado queda
en `build/libs/jojosmod-0.5.0.jar`.

## Prueba rápida: 10 minutos

1. Crea un mundo de prueba en supervivencia con comandos y un personaje sin Stand.
2. Ejecuta `/give @s jojosmod:stardust_arrow` y úsala apuntando al aire. Debe indicar
   Star Platinum, consumir la flecha y mostrar el hito de +2 de voluntad.
3. Pulsa **R**. Revisa su aspecto en primera persona y pulsa **F5** para verlo desde
   fuera. Camina, gira, salta y vuelve a pulsar R para observar las transiciones.
4. Frente a una criatura, apunta a su cuerpo a menos de 3 bloques. **Z** prepara un
   golpe preciso; **X** lanza una ráfaga. Prueba a apartar la mira y a atacar con una
   pared entre ambos. La ráfaga tarda 5 segundos desde su inicio en poder repetirse.
5. **C** activa guardia durante hasta 2 segundos y otra pulsación la cancela. Enfrenta
   un ataque, luego prueba de espaldas. Debe proteger de frente y reducir tu velocidad
   solo mientras está activa. Se recupera a los 4 segundos desde su inicio.
6. Guarda, vuelve a entrar e invoca con R. El Stand y el hito deben conservarse;
   la manifestación no se guarda activa.

Las teclas se pueden reasignar en Controles → JoJo's Mod — Stands. Los tiempos
suponen 20 TPS. La [ficha de combate](star-platinum-design.md) detalla daño y límites.

Para explorar la obtención natural, abre cofres nuevos de templos del desierto:
cada cofre tiene un 20 % de probabilidad de flecha Stardust. No hay garantía de
encontrarla en el primer templo ni se rellenan cofres ya abiertos.

Si tu personaje ya tiene otro Stand, la flecha no lo sustituye. Para conservarlo,
usa el guardián y un disco vacío como explica el [README](../README.md#guardar-el-stand-con-el-guardián--prototipo-de-fase-4c).
El núcleo de Star Platinum está solo en creativo/comandos por ahora.

## Con un amigo

Usad las mismas versiones y un mundo de prueba compartido. Cada jugador necesita
su propia flecha. Observad mutuamente invocación, golpe, ráfaga y guardia, también
al alejaros y volver. Comprobad que cada uno controla únicamente su Stand y que
las reglas PvP y de fuego amigo del mundo se respetan.

## Qué necesito que valores

- ¿Se reconoce a Star Platinum? Indica si cabello, cara, proporciones o colores
  necesitan cambios, y si el modelo tapa demasiado la vista o atraviesa al jugador.
- ¿Se sienten bien el golpe y la ráfaga? Indica si cuesta apuntar o si la preparación,
  la recuperación y el cooldown resultan demasiado lentos o demasiado fuertes.
- ¿La guardia es clara y útil? Prueba sus límites y, si juegas con un amigo, pregunta
  si entiende cuándo estás atacando o defendiéndote.

Si encuentras un fallo, anota la acción, primera/tercera persona, modo de juego y si
ocurre solo o acompañado. Una captura o un vídeo corto ayudan a revisar el modelo.
