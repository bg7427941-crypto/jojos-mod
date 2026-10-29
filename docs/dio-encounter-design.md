# DIO — encuentro de Parte 3, 0.14.0

Referencias de identidad: [DIO, catálogo oficial del anime](https://jojo-portal.com/anime/sc/character/07/)
y las fichas con referencias a escenas de [DIO](https://jojowiki.com/Dio_Brando)
y [The World](https://jojowiki.com/The_World). Se representan su forma humana de
Stardust Crusaders, ropa dorada, cabello rubio, acentos verdes, Stand de combate
cercano, cuchillos y detención temporal. No se incorporan poderes de otras
continuidades. Los modelos editables del jefe y el guardián están en `art/journey`.

## Acceso y espacio

El guardián neutral aparece al visitar un templo real del desierto. Su UUID y
localización se guardan por templo. No se borran ni sustituyen bloques. El
almacenamiento conserva el coste y la confirmación anteriores. El guardián es un
personaje de servicio original del mod; no se presenta como Pucci ni Whitesnake.

El desafío es voluntario: agacharse e interactuar sin disco vacío, con un Stand
de Parte 3, voluntad 4 y un desafío personal dominado. Solo se inicia de noche y
fuera de dificultad pacífica. Busca una superficie libre cercana, sin agua, lava
ni bloques encima. Cada templo guarda un reintento mínimo de 60 s. No añade otro
jefe si ya hay uno activo cerca.

El área útil es de 48 bloques. El combate activo es temporal: DIO se retira si no
hay objetivos durante 10 s, al salir del área, descargarse o cerrar el servidor.
Se limpian su proyección, voz, barra de salud y parada temporal. El guardián y los
hitos persistentes permanecen para reintentar. No se consumen objetos al desafiar.

## Patrones

- 140 PV, +40 por participante cercano adicional, máximo cuatro al inicio; armadura
  4 y resistencia moderada al retroceso. No destruye construcciones ni daña NPCs.
- Golpe cercano: aviso de 0,6 s, daño 7 a un objetivo visible a 3,5 bloques. Puede
  fallar si el objetivo se retira durante la preparación.
- Cuchillos: aviso de 1 s, cinco proyectiles físicos en abanico, daño 3 por impacto,
  recorrido máximo 16. Las paredes dan cobertura y las cargas no se recogen.
- Bajo media salud: preparación temporal de 1,4 s, parada de 2 s; bajo un cuarto,
  parada de 3 s. Reutilización temporal de 13 s y una recuperación de 0,6 s tras activarla: puede ejecutar un golpe durante la parada.
- The World sigue al jefe y presenta sus acciones. DIO puede responder durante
  1,5 s en una parada ajena y después queda congelado; la suya no consume ese
  presupuesto. No se superponen esferas temporales en una dimensión.
- DIO es no muerto y se quema al sol. La conducción de Hamon tiene su bonificación
  contra él. Las protecciones, armadura y reglas de daño normales siguen activas.

Los tiempos, área, salud y enfriamientos son balance de Minecraft. La parada es
local; no pausa red, servidor ni máquinas. Restaura las velocidades y permite
reanudar las entradas al finalizar. No almacena una explosión de daño diferido.

## Participación y recompensa

Se registra a quien haya causado daño de salud a DIO y a los objetivos de sus
golpes cercanos. Se admite participación reciente de hasta 60 s; estar cerca sin
participar no basta. No exige el último golpe y admite enemigos compartidos.

La victoria genera una entrega pendiente por UUID, persistida en los datos del
mundo. Al estar conectado o volver a entrar, cada jugador recibe el hito y +2 de
voluntad una sola vez. Su núcleo de The World se reclama una vez; un inventario
lleno mantiene el derecho, sin tirar objetos ni marcarlos como entregados. El
jugador puede reclamar con el guardián o `/stand claim`. La progresión y las
reclamaciones se conservan al morir y cambiar de Stand. No se reclama en creativo.

La [guía 0.14.0](playtest-0.14.0.md) incluye los casos cooperativos y de recuperación.
