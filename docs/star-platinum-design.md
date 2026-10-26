# Star Platinum — prueba jugable 0.9.2

Combate base y primeros desafíos de progresión de Parte 3. La [ficha oficial de Jotaro](https://jojo-portal.com/en/anime/sc/character/01/)
sirve de referencia para la fuerza, precisión y ráfagas de puñetazos. La presentación
0.7.0 construye modelo, UV, atlas, animaciones y sonido desde fuentes propias;
la [dirección artística](art-direction.md) documenta sus referencias. Los valores siguientes son
decisiones de balance del mod, independientes de los parámetros del manga.

## Obtención y progreso

Una flecha Stardust aparece con un 20 % de probabilidad por cofre de templo del
desierto. El modificador añade una flecha y conserva el loot vanilla. Solo permite
despertar Star Platinum en esta entrega; usa un sorteo separado de la flecha antigua.
La flecha antigua sigue ofreciendo Echoes ACT 1 y Little Feet al 50 % cada uno.

Usar la flecha apuntando al aire exige no tener Stand. Se consume en supervivencia,
se conserva en creativo y no causa daño. La primera asignación de Star Platinum
otorga +2 de voluntad, hasta el límite global, y muestra las teclas configuradas.
El hito persiste al guardar y morir; cambiar, guardar o recuperar el Stand no lo
repite. Con voluntad inicial 1 se alcanza el requisito fuerte por defecto de 3.

El núcleo de Star Platinum está disponible en creativo/comandos. Un núcleo más un
disco vacío producen su disco identificado. El guardián existente permite guardar
y recuperar este Stand con las mismas reglas de voluntad. Todavía no hay obtención
natural del núcleo. Las flechas y los discos son adaptaciones de progresión del mod:
no representan literalmente cómo despertó Jotaro su Stand en la historia.

En supervivencia, precisión, ráfaga y guardia registran hasta tres UUID de enemigos
hostiles distintos por categoría. Un golpe debe causar daño real de salud; una
guardia debe reducir daño físico frontal positivo. Repetir un enemigo no vuelve
a contar para ese desafío. Cada categoría completa concede +1 una sola vez.
No cuentan jugadores, criaturas pasivas, enemigos aliados ni acciones de usuarios
creativos/espectadores. Los objetivos compartidos pueden dar progreso a cada
participante por sus propias acciones, sin exigir el golpe final.

Estos tres desafíos se conservan al guardar, morir, retirar o recuperar el Stand.
El formato anterior `{awakened: true}` carga con los nuevos contadores vacíos sin
repetir su premio. El panel y `/stand progress` muestran el avance al propietario.
Con voluntad 4 se desbloquea el salto con Stand, con 5 se desbloquea Star Finger: despertar desde 1 y completar dos desafíos
permite alcanzarla; el tercero lleva a 6 y desbloquea la intercepción precisa. No se consume voluntad.

## Acciones a 20 TPS

| Tecla predeterminada | Acción | Efecto sin armadura | Tiempos |
| --- | --- | --- | --- |
| R | Invocar/desinvocar | Una manifestación propia | 5 ticks entre cambios |
| Z | Golpe preciso | 6 puntos; alcance 3 bloques | Preparación 4 ticks; acción 12; cooldown 16 desde el inicio |
| X | Ráfaga | 8 golpes de 1 punto; alcance 3 bloques | Golpes en ticks 4, 7, 10, 13, 16, 19, 22 y 25; acción 30; cooldown 100 desde el inicio |
| C | Activar/cancelar guardia | Reduce un 60 % el daño físico frontal | Máximo 40 ticks; cooldown 80 desde el inicio |
| V | Star Finger (voluntad ≥5) | 4 puntos; alcance 6 bloques | Preparación 8 ticks; acción 18; cooldown 120 desde el inicio |
| B | Intercepción precisa (voluntad ≥6) | Neutraliza una flecha frontal | Preparación 2 ticks; ventana [2,10); acción 12; cooldown 60 desde el inicio |
| J | Salto con Stand (voluntad ≥4) | Impulso horizontal y vertical desde suelo firme | Instantáneo; cooldown 80 ticks; primer aterrizaje: hasta 4 bloques de amortiguación durante 60 ticks |

Las teclas se pueden cambiar en Controles. La guardia cubre un arco horizontal de
120 grados frente a la mirada del usuario. Afecta ataques de jugadores y criaturas
y proyectiles; no protege de daño por fuego, caída ni ataques desde atrás. Reduce
la velocidad de movimiento un 45 % mientras está activa. Cancelarla antes mantiene
su cooldown. Su duración y alcance son límites de jugabilidad.

No se pueden superponer acciones. Recibir daño real de salud interrumpe el golpe
preciso, la ráfaga, Star Finger o intercepción y conserva sus cooldowns. Cada impacto vuelve a comprobar la mirada,
el alcance, paredes, estado del objetivo, PvP y fuego amigo. La ráfaga usa un tipo
de daño específico para resolver sus pulsos sin que la inmunidad normal entre
golpes los descarte; respeta armadura e invulnerabilidad y no reinicia globalmente
la inmunidad del objetivo. No causa retroceso.

La manifestación sigue al propietario y se recoloca si supera 4 bloques. Se acerca
a su frente durante las acciones. Morir, desconectarse, cambiar de dimensión,
desinvocar o perder la asignación terminan las acciones. El modificador de guardia
se elimina al terminar. Al desinvocar, el combate se desactiva inmediatamente y
la animación dispone de 4 ticks para retirar el modelo. Los estados activos y sus
cooldowns son temporales; el Stand, la voluntad y los desafíos sí se guardan.

Star Finger extiende dos dedos, como en el combate con Dark Blue Moon, referido
en el [episodio 6 del anime](https://jojo-animation.com/sc/contents/story/06.html)
y la [referencia de ese arco](https://jojowiki.com/Dark_Blue_Moon_(story_arc)).
El daño, alcance de 6 bloques y requisito de voluntad son adaptaciones de balance.
No atraviesa paredes y usa las mismas reglas de PvP/equipos que los otros golpes.

## Intercepción precisa

B prepara una respuesta basada en la precisión de Star Platinum al interceptar
una bala al inicio de Parte 3. Usar flechas de Minecraft y exigir voluntad 6
son adaptaciones de balance. La [guía 0.8.0](playtest-0.8.0.md) detalla su ventana.
Acepta una flecha normal, con efectos o espectral en el cono de 120° de la mirada
entre los ticks 2 y 9. Cancela la colisión y elimina esa flecha, sin munición
ni recompensas. Las flechas con Perforación la atraviesan. No afecta tridentes ni otros proyectiles; descarta tiros propios,
aliados o protegidos por las reglas PvP. El acierto termina la acción; el fallo
termina a los 12 ticks y ambos conservan el cooldown de 60 ticks.

## Salto con Stand

J aplica un impulso inmediato al estar sobre apoyo físico, con espacio sobre la
cabeza y Star Platinum activo. La dirección horizontal sigue la mirada; su
velocidad horizontal y vertical son 0,85 bloques/tick. Requiere voluntad 4 y una
acción de combate libre al comenzar. Después se pueden usar ataques en el aire.

El primer aterrizaje descuenta hasta cuatro bloques de distancia de caída durante
un máximo de 60 ticks. No elimina el daño de un precipicio. Desinvocar mantiene
esta protección limitada; perder el Stand, cambiar de dimensión, recibir daño o
entrar en agua/lava, trepar, montar o volar la termina. El cooldown de 80 ticks
permanece independiente. La [guía 0.9.0](playtest-0.9.0.md) detalla los límites
y la prueba con amigos. Se inspira en impulsarse con la fuerza del Stand en Parte 3;
sus valores son adaptaciones de balance.

## Presentación y alcance pendiente

El modelo 0.9.2 adapta la geometría, UV y atlas del anime de Roundabout, por
Hydraheads y colaboradores, con su licencia y créditos. Tiene 79 cubos, 29 huesos
y un atlas de 128×128. Se conservan el rostro, cabello, hombreras y vestuario;
los guantes y botas se dividen en muñecas y tobillos para las acciones del mod.
La procedencia está en [las fuentes editables](../art/star_platinum/README.md). Sus trece
animaciones originales cubren reposo, movimiento, invocación, desinvocación, golpe,
ráfaga, guardia, recuperación, Star Finger, intercepción, su gesto de captura, salto y aterrizaje. Los dos dedos derechos extensibles
solo son visibles durante Star Finger. Hay aura violeta/dorada,
trayectorias de puños, destellos de impacto y arco frontal de guardia, con partículas
nativas. Los catorce efectos de audio son síntesis original: ruido con semillas,
resonadores y osciladores propios, en mono/48 kHz. Invocación, ráfaga y Star Finger
usan un canal independiente por propietario y se cortan al cancelar. No incluyen
grabaciones de voces. Las muestras de otras fuentes se pueden probar con un pack personal.
Consulta los [créditos y licencias](../THIRD_PARTY_NOTICES.md).
Las fuentes editables están en [art/star_platinum](../art/star_platinum/README.md).

La parada temporal, los otros Crusaders, DIO y el recorrido completo
de encuentros quedan pendientes. Esta entrega permite decidir sobre silueta,
encuadre, legibilidad y ritmo antes de ampliar el catálogo. El lanzamiento completo
mantiene los criterios del [plan de Parte 3](part-three-release.md), incluida la
validación con dos clientes reales en servidor dedicado.
