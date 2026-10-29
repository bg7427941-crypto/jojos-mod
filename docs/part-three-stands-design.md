# Catálogo jugable de Parte 3 — 0.14.0

La 0.14.0 añade las [técnicas avanzadas y la campaña](playtest-0.14.0.md).
El [encuentro de DIO](dio-encounter-design.md) ya permite obtener The World jugando.
Los valores de daño y ráfagas de la primera tabla son históricos de 0.12.0;
la [tabla 0.13.0](playtest-0.13.0.md) y la [ampliación 0.14.0](playtest-0.14.0.md)
son la referencia del balance actual.

## Conjunto inicial — 0.12.0 (histórico)

Esta entrega añade los cinco Stands que faltaban del catálogo principal propuesto:
Silver Chariot (Polnareff), Magician’s Red (Avdol), Hermit Purple (Joseph), The Fool
(Iggy) y The World (DIO). Son primeros conjuntos jugables con tres técnicas,
progresión y recursos propios; no representan todos los movimientos de la obra.
El encuentro de DIO y su recorrido de supervivencia siguen pendientes.

Referencias de identidad: [catálogo oficial de Stardust Crusaders](https://jojo-portal.com/en/anime/sc/character/)
y fichas secundarias de [Silver Chariot](https://jojowiki.com/Silver_Chariot),
[Magician’s Red](https://jojowiki.com/Magician%27s_Red),
[Hermit Purple](https://jojowiki.com/Hermit_Purple), [The Fool](https://jojowiki.com/The_Fool)
y [The World](https://jojowiki.com/The_World). Estas fichas reúnen citas de escenas;
el mod no toma habilidades de otros videojuegos como prueba de canon.

## Obtención, aprendizaje y controles

La flecha Stardust ofrece seis Crusaders con igual probabilidad (1/6 cada uno):
Star Platinum, Hierophant Green, Silver Chariot, Magician’s Red, Hermit Purple y
The Fool. The World se ofrece por disco/núcleo en creativo/comandos hasta diseñar
el encuentro de DIO; no se presenta como recompensa de un jefe inexistente.
Los Stands ya asignados y la flecha Echoes/Little Feet se conservan.

R invoca; Z activa la técnica principal, X la secundaria y C la utilidad/defensa.
G detiene el tiempo cuando corresponde. Se respetan los controles reasignados.
Los nuevos Stands despiertan con +2 de voluntad una sola vez. Causar daño de salud
a tres hostiles distintos concede +1 y reduce un 20 % el enfriamiento principal
desde la próxima activación. Cada jugador tiene su propio progreso por Stand,
conservado al guardar, morir y almacenar/recuperar discos. No exige el golpe final;
jugadores, aliados, pasivos, creativo/espectador y absorción completa no cuentan.

Todos los nuevos discos requieren voluntad 3 con la configuración fuerte por
defecto. Núcleos y discos se integran con las recetas y el guardián existentes.
Estas reglas de obtención y aprendizaje son adaptaciones para Minecraft.

## Técnicas y límites

| Stand | Z | X | C |
| --- | --- | --- | --- |
| Silver Chariot | Estocada: preparación 3 ticks, daño 4, alcance 3,5, enfriamiento 14 ticks | Seis estocadas de daño 1, desde tick 3 cada 2 ticks; enfriamiento 100 | Quitar armadura durante 100 ticks: estocada cada 8 ticks y +25 % de daño recibido; enfriamiento 200 |
| Magician’s Red | Crossfire Hurricane: preparación 6 ticks, proyectil de daño de fuego 6, recorrido 16, velocidad inicial 0,9/tick, vida 30 ticks; enfriamiento 50 | Red Bind: objetivo visible hasta 10, daño de fuego 3 y reducción de velocidad 90 % desde tick 5 hasta tick 45; enfriamiento 120 | Guardia de fuego: reduce daño recibido 50 % durante 40 ticks; enfriamiento 180 |
| Hermit Purple | Látigo de espinas: preparación 6 ticks, daño 3, alcance 5; enfriamiento 24 | Agarre: bloque sólido a 2–12 de distancia; impulso físico máximo 0,85/tick durante 12 ticks, cancelado al obstruirse; enfriamiento 60 | Adivinación: nombre y coordenadas del hostil más cercano hasta 16; enfriamiento 180 |
| The Fool | Arena: preparación 6 ticks, tres pulsos de daño 1 separados por 2 ticks, cono de 6, hasta 8 blancos por pulso; enfriamiento 50 | Coraza de arena: reduce daño 65 % durante 40 ticks; enfriamiento 120 | Planeo: descenso limitado a 0,12/tick y sin daño de caída durante 60 ticks; enfriamiento 180 |
| The World | Golpe: preparación 4 ticks, daño 6, alcance 3; enfriamiento 20 | Diez golpes MUDA de daño 1, desde tick 3 cada 2 ticks; enfriamiento 100 | Guardia frontal: reduce daño 50 % durante 40 ticks, dentro del cono frontal; enfriamiento 180 |

20 ticks equivalen a un segundo a velocidad normal del servidor. Daños antes de
armadura/defensas; valores de Minecraft separados de los parámetros del manga.
Los ataques respetan paredes, equipos y PvP. Crossfire no prende bloques ni deja
quemaduras persistentes, para evitar daño indirecto a aliados o construcciones;
los inmunes al fuego conservan su inmunidad. No consume ni suelta cargas ígneas.
Red Bind no anula todas las acciones: ralentiza el movimiento; no es una prisión.
Su modificador propio se retira al finalizar, interrumpirse, perder visión/distancia,
cambiar de Stand/dimensión, morir o desconectarse, sin quitar efectos ajenos.

Desinvocar, cambiar de Stand o recibir daño de salud interrumpe ataques y sus
proyectiles pendientes, manteniendo los enfriamientos. Las defensas y el planeo
pueden resistir daño sin cancelarse; expiran por tiempo. La recuperación normal
permite terminar el vuelo de Crossfire. Las esmeraldas de Hierophant Green y las
habilidades previas de Star Platinum conservan sus reglas.

El agarre y el planeo no permiten atravesar paredes ni conceden vuelo permanente.
Terminado el planeo, una nueva caída vuelve a causar daño. Quitar armadura retira
las placas visuales de Chariot y acelera estocadas, sin conceder ataques automáticos.
Adivinación es una adaptación de la fotografía espiritual: un rastreo local de
hostiles, no una búsqueda global garantizada de DIO.

## Parada temporal — The World y Star Platinum avanzado

G exige Stand invocado y voluntad 4 para The World o 6 para Star Platinum.
The World detiene durante 60 ticks y Star Platinum durante 40; ambos tienen un
enfriamiento de 600 ticks. La esfera tiene radio 12, centrada donde se activó,
y admite hasta 256 seres vivos/proyectiles. Solo hay una esfera por dimensión;
las activaciones solapadas se rechazan y no renuevan su duración.

El servidor sigue funcionando. Las entidades afectadas dejan de ejecutar sus ticks;
los jugadores conservan conexión y confirmaciones de teletransporte, pero el servidor
rechaza movimiento, interacción, uso de ítems, acciones y clics de inventario.
El cliente vacía la entrada de movimiento y las intenciones de habilidades.
Entrar en el área durante la parada también afecta; las entidades ajenas al área,
los bloques y sus máquinas siguen sus ticks. No es una pausa global del mundo.

El usuario, aliados, espectadores, creativo y jugadores protegidos por equipos/PvP
quedan fuera. Otros usuarios de The World con voluntad 4 o Star Platinum con 6
pueden moverse: es una simplificación explícita del movimiento temporal de Parte 3,
sin presupuestos individuales de segundos en esta entrega. Los proyectiles propios
también se congelan. El daño de técnicas del usuario se aplica inmediatamente;
no se acumula una explosión de daño al reanudar.

Al terminar se libera la entrada del jugador y se restaura la velocidad previa de
cada entidad. No se cambian IA, gravedad ni efectos de poción, por lo que no quedan
estados permanentes que restaurar. Muerte, desconexión, desinvocación, cambio de
Stand/dimensión o salir del radio con el usuario terminan la esfera; al cerrar el
servidor se limpian las esferas. Los datos de congelación no se guardan en disco.

## Presentación y alcance pendiente

Los cinco proyectos incluyen fuentes editables y hashes. Chariot, Magician’s Red
y The World adaptan geometría/UV y atlas de anime de Roundabout siguiendo el mismo
flujo que Star Platinum, con créditos y licencia completos. Hermit Purple y The
Fool usan geometría/atlas propios. Los íconos y los keyframes de los cinco son
originales. Véase [art/part_three](../art/part_three/README.md).
Chariot usa armadura separada y estoque; Magician’s Red, cabeza aviar y garras;
Hermit Purple, lianas con espinas sin cuerpo humanoide; The Fool, cuerpo cuadrúpedo
con ruedas/máscara y alas de arena; The World, casco, corazones y depósitos dorsales.
Son modelos de prueba que necesitan valoración del jugador, no recursos finales.

Pendientes: proyectar el estoque y el repertorio completo de Chariot; formas/alcance
avanzado del fuego; conducción de Hamon y fotografía con cámaras; transformaciones
avanzadas y control ambiental de arena; cuchillos y encuentro de DIO; animación de
reanudar y presupuestos temporales individuales. Hierophant Green todavía necesita
barrera, tentáculos y posesión. Echoes/Little Feet y Whitesnake mantienen el alcance
anterior, fuera del catálogo de lanzamiento de Parte 3.

## Técnicas y obtención actuales — 0.14.0

Se implementan el estoque lanzado, los cuchillos, el detector de vida, conducción
de Hamon y una cúpula protectora de arena para el grupo. Hierophant Green añade
tentáculos y barrera fija de contraataque. Los usuarios compatibles, incluido DIO,
agotan su presupuesto al moverse en una parada ajena; salir y volver no lo renueva.
La [guía actual](playtest-0.14.0.md) fija valores, controles y cancelaciones.

El guardián entrega una vez el núcleo del Crusader que hayas dominado, por jugador
y Stand. DIO entrega el núcleo de The World a cada participante, también al volver
a conectarse, con reserva si el inventario está lleno. La posesión, fotografías
con cámaras, control ambiental extenso y otras escenas adicionales del repertorio
se conservan como propuestas posteriores y no bloquean el recorrido actual.
