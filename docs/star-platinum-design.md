# Star Platinum — prueba jugable 0.5.0

Primera entrega del combate base de Parte 3. La [ficha oficial de Jotaro](https://jojo-portal.com/en/anime/sc/character/01/)
sirve de referencia para la fuerza, precisión y ráfagas de puñetazos. El modelo y
sus animaciones son originales, adaptados a Minecraft. Los valores siguientes son
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

## Acciones a 20 TPS

| Tecla predeterminada | Acción | Efecto sin armadura | Tiempos |
| --- | --- | --- | --- |
| R | Invocar/desinvocar | Una manifestación propia | 5 ticks entre cambios |
| Z | Golpe preciso | 6 puntos; alcance 3 bloques | Preparación 4 ticks; acción 12; cooldown 16 desde el inicio |
| X | Ráfaga | 8 golpes de 1 punto; alcance 3 bloques | Golpes en ticks 4, 7, 10, 13, 16, 19, 22 y 25; acción 30; cooldown 100 desde el inicio |
| C | Activar/cancelar guardia | Reduce un 60 % el daño físico frontal | Máximo 40 ticks; cooldown 80 desde el inicio |

Las teclas se pueden cambiar en Controles. La guardia cubre un arco horizontal de
120 grados frente a la mirada del usuario. Afecta ataques de jugadores y criaturas
y proyectiles; no protege de daño por fuego, caída ni ataques desde atrás. Reduce
la velocidad de movimiento un 45 % mientras está activa. Cancelarla antes mantiene
su cooldown. Su duración y alcance son límites de jugabilidad.

No se pueden superponer acciones. Recibir daño real de salud interrumpe el golpe
o la ráfaga y conserva sus cooldowns. Cada impacto vuelve a comprobar la mirada,
el alcance, paredes, estado del objetivo, PvP y fuego amigo. La ráfaga usa un tipo
de daño específico para resolver sus pulsos sin que la inmunidad normal entre
golpes los descarte; respeta armadura e invulnerabilidad y no reinicia globalmente
la inmunidad del objetivo. No causa retroceso.

La manifestación sigue al propietario y se recoloca si supera 4 bloques. Se acerca
a su frente durante las acciones. Morir, desconectarse, cambiar de dimensión,
desinvocar o perder la asignación terminan las acciones. El modificador de guardia
se elimina al terminar. Al desinvocar, el combate se desactiva inmediatamente y
la animación dispone de 4 ticks para retirar el modelo. Los estados activos y sus
cooldowns son temporales; el Stand y el hito de voluntad sí se guardan.

## Presentación y alcance pendiente

El modelo tiene cabello, cinta, gema, hombreras, guantes, puños articulados, botas,
bufanda y faldón. Sus ocho animaciones cubren reposo, movimiento, invocación,
desinvocación, golpe, ráfaga, guardia y recuperación. Los impactos usan partículas
CRIT y sonidos nativos de Minecraft; no se incluyen voces extraídas del anime.
Las fuentes editables están en [art/star_platinum](../art/star_platinum/README.md).

La parada temporal, Star Finger, los otros Crusaders, DIO y el recorrido completo
de encuentros quedan pendientes. Esta entrega permite decidir sobre silueta,
encuadre, legibilidad y ritmo antes de ampliar el catálogo. El lanzamiento completo
mantiene los criterios del [plan de Parte 3](part-three-release.md), incluida la
validación con dos clientes reales en servidor dedicado.
