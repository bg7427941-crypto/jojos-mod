# Star Platinum — presentación 0.5.1

Esta prueba aplica tus comentarios sobre el modelo, las animaciones y la ausencia
de efectos sonoros. Conserva las habilidades, daño, alcance, duración y cooldowns
de 0.5.0 que ya probaste.

## Cambios para valorar

- Modelo y textura anime adaptados de Roundabout, con crédito y licencia propios.
  El rig incluye cabello, hombreras, músculos, manos y vestuario con más detalle.
- Ocho animaciones ajustadas: reposo, desplazamiento, invocación, retirada, golpe,
  ráfaga, guardia y recuperación. El golpe sigue contactando a los 0,2 segundos;
  los ocho pulsos de ráfaga mantienen sus tiempos anteriores.
- Sonidos breves de invocación, retirada, puño, impacto y guardia; dos variantes de
  impacto. Proceden de paquetes gratuitos CC0 de Kenney. No hay voces del anime.
- Aura violeta/dorada sutil, trayectoria de puños, destellos de impacto y un arco
  frontal de guardia. Usan partículas nativas y respetan el ajuste de partículas.

Consulta los [créditos y licencias](../THIRD_PARTY_NOTICES.md) y las
[fuentes editables](../art/star_platinum/README.md).

## Instalar y probar

Usa Minecraft **26.3**, NeoForge **26.3.0.95** y GeckoLib **5.5.7**. Sustituye el
JAR anterior por `jojosmod-0.5.1.jar`. Instala la misma versión en todos los clientes
y el servidor. Desde el repositorio, con Java 25: `gradlew.bat runClient` en Windows
o `./gradlew runClient` en Linux/macOS. `build` genera el JAR en `build/libs/`.

Puedes usar tu Star Platinum ya obtenido. Si necesitas uno en un mundo de prueba
con comandos y un personaje sin Stand: `/give @s jojosmod:stardust_arrow` y clic
derecho apuntando al aire. La [guía de combate 0.5.0](playtest-0.5.0.md) sigue vigente.

1. Pulsa **R** y gira alrededor con **F5**. Revisa proporciones, cabello, cara,
   textura y transiciones. Camina, gira y salta para comprobar las articulaciones.
2. Prueba **Z** y **X** frente a un objetivo y luego en el aire. El sonido de
   movimiento del puño debe oírse en ambos casos; el impacto solo cuando golpea.
3. Usa **C**, cancela y vuelve a intentarlo al recuperar el cooldown. Comprueba
   si la pose, el arco y el sonido hacen que la defensa se entienda.
4. Revisa primera persona: modelo, puños y partículas deben permitir apuntar.
   En Opciones → Música y sonidos ajusta **Jugadores** para estos efectos; activa
   subtítulos si quieres comprobar los avisos sin depender del audio.
5. Si puedes, pide a un amigo que observe las acciones: ambos deben ver los
   efectos y oír los sonidos cercanos. Al detener una ráfaga no debe quedar un bucle.

Necesito tu valoración sobre el modelo elegido, la fluidez y fuerza de las
animaciones, el volumen de la ráfaga y la visibilidad en primera persona. Si una
parte atraviesa al jugador o tapa demasiado, indica la acción y la perspectiva.
