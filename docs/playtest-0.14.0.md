# Prueba jugable 0.14.0 — recorrido de Stardust Crusaders

Minecraft 26.3, NeoForge 26.3.0.95, GeckoLib 5.5.7. Cliente y servidor deben usar
**0.14.0, protocolo 13**. Se conservan asignaciones, voluntad y dominio anteriores.

## Recorrido sin comandos de operador

1. Busca un templo del desierto. Sus cofres conservan el 20 % por cofre de añadir
   una flecha Stardust. La flecha ofrece los seis Crusaders al 1/6, sin sustituir
   un Stand que ya poseas. `/stand journey` explica el siguiente objetivo.
2. Aprende tu Stand con R/Z/X/C y `/stand progress`. Completa un desafío de dominio
   contra tres hostiles distintos; repetir el mismo objetivo no vuelve a contarlo.
3. Al visitar el interior de un templo real aparece un guardián en una superficie
   cercana. El juego anuncia sus coordenadas. Su identidad y el templo se guardan;
   no debe aparecer una copia al salir y volver, morir o reiniciar el servidor.
4. Interactúa sin disco vacío: el primer encuentro concede +1 de voluntad. Con
   el dominio de tu Stand, recibes **su núcleo una sola vez por Stand y jugador**.
   Un inventario lleno reserva la recompensa; libera una casilla y usa
   `/stand claim` o vuelve con el guardián. Los seis núcleos Crusader tienen así
   obtención de supervivencia y pueden intercambiarse con amigos.
5. De noche (13000–23000), agáchate e interactúa sin disco vacío para retar a DIO.
   Necesitas voluntad 4 y un desafío dominado del Stand actual. Requiere dificultad
   fácil, normal o difícil. El guardián se conserva como servicio neutral.
6. DIO anuncia golpes, cuchillos y la carga temporal. Tiene 140 PV para un jugador,
   +40 por participante cercano adicional al inicio, hasta cuatro jugadores. A
   media salud empieza a detener el tiempo; bajo un cuarto alarga la detención.
   Esquiva, toma cobertura y aprovecha las recuperaciones. No rompe construcciones.
7. Quienes hayan dañado a DIO o resistido sus golpes recientemente reciben su
   victoria personal; no se exige el último golpe. Se concede +2 de voluntad una
   sola vez y un núcleo de The World por jugador. Un participante desconectado
   recibe el hito al volver. Un inventario lleno conserva el derecho a reclamarlo.
8. Fabrica el disco de The World con su núcleo y un disco vacío. Para guardar tu
   Stand actual, entrega un disco vacío al guardián con confirmación agachado.
   Se mantiene el coste aprobado del disco: siete fragmentos, fragmento de eco y
   lingote de netherita. Equipar un disco no sustituye una asignación existente.

La fabricación, las flechas, los núcleos, el guardián y los hitos son adaptaciones
para Minecraft. No representan el despertar literal de cada personaje del manga.
Los guardianes antiguos colocados por comandos conservan el servicio anterior;
la campaña se ofrece en los guardianes nuevos vinculados a templos.

## Reintento y datos

Cada templo admite un nuevo desafío tras 60 segundos desde el anterior, y no
permite duplicar el jefe activo cercano. Si todos se alejan, DIO se retira tras
10 segundos; salir del área de 48 bloques también permite abandonar el combate.
El encuentro activo es temporal: descargar sus chunks o cerrar el servidor lo
reinicia. Las victorias, voluntad y reclamaciones ya completadas se conservan.
No hay coste de invocación ni pérdida de los discos que lleves al reintentar.

## Técnicas añadidas

| Stand | Control | Regla |
| --- | --- | --- |
| Hierophant Green | X | Tentáculo: contacto a 0,3 s, 4 de daño, alcance 10; ralentiza 65 % hasta 2 s. Se rompe al perder visión/distancia o interrumpir al usuario. Enfriamiento 6 s. |
| Hierophant Green | C | Barrera fija de tres hilos, ancho 6 y duración 10 s, delante del usuario. Reacciona una vez por objetivo, daño 6, hasta 8 objetivos. Mantenerse a 16 bloques; C cancela. Enfriamiento 13 s. Tras prepararla permite Emerald Splash. |
| Silver Chariot | V | Lanza el estoque: 9 de daño, alcance físico 12. Lo recupera a 1,75 s; antes no puede atacar con él. Enfriamiento 8 s. |
| Magician’s Red | V | Detector de vida: revela hasta 12 hostiles a 20 bloques durante 5 s, sin daño. Enfriamiento 9 s. |
| Hermit Purple | V | Conduce Hamon por la liana, alcance 7: 9 de daño a no muertos, 4 a otros. Enfriamiento 6 s. |
| The Fool | V | Cúpula móvil de arena durante 3 s, radio 3: reduce daño físico 40 % a usuario y aliados de equipo; también a jugadores si PvP está desactivado. Enfriamiento 12 s. |
| The World | V | Cinco cuchillos físicos en abanico, daño 3 por cuchillo, alcance 16. Enfriamiento 7 s. |

Las V exigen voluntad 4. Todos los contactos V ocurren a 0,4 s. Los proyectiles
respetan paredes y no son munición recogible. No se superponen acciones; el daño de
salud interrumpe ataques, conserva el enfriamiento y retira sus efectos. Las
protecciones de arena permanecen durante su duración. Las mejoras y el daño de
0.13.0 siguen vigentes. Los valores son balance del mod, separados del canon.

## Movimiento en tiempo ajeno

El propio usuario conserva toda su parada. Un usuario compatible de Star Platinum
(voluntad 6) puede moverse **1 s** en una parada ajena; The World (voluntad 4),
**1,5 s**. Derrotar a DIO añade **1 s** a ese presupuesto. DIO dispone de 1,5 s
para responder a paradas ajenas. Al agotarse, se congela durante lo que reste.
Salir y volver al área no renueva el presupuesto. Al finalizar, morir, retirarse
o desconectarse el usuario, se libera a los afectados y se restaura su velocidad.

## Prueba con amigos

- Dos personas deben poder obtener, guardar y recuperar Stands sin comandos.
- Participad ambos contra DIO, dejad que solo uno dé el último golpe y comprobad
  que los dos reciben el hito, voluntad y su propio núcleo, sin repetirse.
- Repetid con inventario lleno y con un participante desconectado al morir DIO.
- Probad cuchillos, barreras y tentáculos con equipos aliados y PvP desactivado.
- Comparad las poses y el audio desde el propietario y desde el otro cliente.
- Probad la parada de ambos usuarios, agotar movimiento ajeno, salir/entrar en
  el área y desconectarse en pleno efecto. Nadie debe quedar inmovilizado.
- Guardad y volved: guardianes, dominio, victorias y reclamaciones no deben duplicarse.

La escucha final de clips, el ritmo de progresión y el balance con personas son
parte de esta prueba manual. Los tests de servidor y las capturas del cliente
apoyan esa evaluación y no equivalen a una partida humana completa.
