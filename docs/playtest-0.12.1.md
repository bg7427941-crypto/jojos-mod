# Prueba de presentación — 0.12.1

Minecraft 26.3, NeoForge 26.3.0.95, GeckoLib 5.5.7. Actualiza **cliente y servidor**:
esta versión usa **protocolo 11** por la sincronización del efecto temporal.

## Detener el tiempo

1. Equipa The World con voluntad 4, o Star Platinum con voluntad 6. Invoca con R.
2. Pulsa **G**. Cada Stand tiene su propio sonido de activación y reanudación.
   Son cuatro efectos originales sintetizados; no son grabaciones del anime.
3. Debe expandirse una onda de partículas y una distorsión de color en pantalla.
   Durante la pausa, The World deja una paleta dorada y Star Platinum una violeta
   desaturada. La transición negativa inicial es breve, sin parpadeo repetido.
4. Comprueba que el color normal regrese al terminar (3 s / 2 s), desinvocar,
   morir, salir de la burbuja o cambiar de dimensión. El cooldown sigue siendo 30 s.
5. Repite con un amigo cerca: ambos deben ver el inicio y oír los sonidos.
   Un jugador fuera de los 12 bloques puede oír la activación cercana, pero no
   debe recibir el filtro de pantalla. Las inmunidades y reglas PvP son las de 0.12.0.

**Efectos de pantalla** controla la intensidad; a 0 se desactiva el filtro.
**Ocultar destellos de rayos** suprime la inversión de colores inicial.
El sonido usa la categoría **Jugadores**; hay subtítulos para ambos Stands.
La burbuja sigue pausando entidades locales, sin detener bloques ni máquinas.

## Hermit Purple

Invoca con R y observa las lianas con F5, tanto quieto como caminando, corriendo,
girando, agachándote, nadando y usando un objeto. Deben seguir cada brazo del
jugador inmediatamente. Comprueba también la mano vacía en primera persona y
skins de brazos normales y delgados. Las lianas deben desaparecer al desinvocar.

La entidad sigue existiendo para las habilidades y la sincronización; su antiguo
modelo separado se oculta. Las lianas visibles se dibujan en la capa de brazos
del jugador y comparten sus transformaciones, sin una segunda interpolación.

## Animaciones

Prueba reposo, desplazamiento, Z, X y C en Silver Chariot, Magician's Red,
The Fool y The World; prueba Emerald Splash en Hierophant Green. Ahora hay
poses completas de cuerpo, flexión de codos/rodillas, preparación, contacto,
retroceso y recuperación. Los cambios entre reposo y movimiento se suavizan
sin retrasar los golpes. Star Platinum conserva sus ataques e incorpora una
pose propia para G y la misma separación de transiciones.

Los tiempos de impacto, daño, progresión y cooldown de las habilidades se
mantienen. La [guía anterior](playtest-0.12.0.md) describe los kits completos.
