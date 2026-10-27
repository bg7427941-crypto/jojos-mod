# Primera versión completa: Stardust Crusaders

## Dirección confirmada y propuesta de alcance

El usuario confirmó **Parte 3** como centro de la primera versión completa y pulida.
La meta combina fidelidad a JoJo, modelos y animaciones propios, progresión entretenida
y juego con amigos. Este plan desarrolla esa dirección; sus propuestas de catálogo,
recompensas y balance todavía deben concretarse durante la implementación.

La prueba jugable 0.5.0 implementó el combate base de Star Platinum. La 0.5.1
mejora la presentación con un modelo adaptado de Roundabout, ocho animaciones
ajustadas, audio CC0 y efectos visuales; sus [créditos](../THIRD_PARTY_NOTICES.md)
acompañan a los recursos. La [ficha de habilidades](star-platinum-design.md)
registra sus reglas. La 0.6.0 añade Star Finger, desafíos persistentes de voluntad,
panel de progreso y sprites de ítems; su [guía de prueba](playtest-0.6.0.md) permite evaluarlo.
La 0.7.0 rehace la presentación de Star Platinum con recursos originales y una
[guía específica](playtest-0.7.0.md), siguiendo la [dirección artística](art-direction.md).
La 0.8.0 añade [intercepción precisa](playtest-0.8.0.md), desbloqueada con voluntad 6.
La 0.9.0 añade [salto con Stand](playtest-0.9.0.md), desbloqueado con voluntad 4.
La 0.10.0 añade [dominio avanzado](playtest-0.10.0.md) de Star Finger e intercepción,
con desafíos personales y mejoras de enfriamiento persistentes.
La 0.11.0 añade la [primera prueba de Hierophant Green](playtest-0.11.0.md):
Emerald Splash, modelo/atlas originales, seis animaciones, audio sintetizado y
progreso personal. La flecha Stardust sortea ambos Stands al 50 %; su
[ficha](hierophant-green-design.md) deja explícito el repertorio todavía pendiente.
Este plan sigue describiendo el lanzamiento completo, que aún está en desarrollo.

La propuesta de catálogo principal sigue a los Crusaders, con DIO como encuentro de
cierre. Los nombres y usuarios de referencia pueden contrastarse en el
[catálogo oficial del anime de Parte 3](https://jojo-portal.com/en/anime/sc/character/):

| Stand propuesto | Usuario de referencia | Lugar en el plan |
| --- | --- | --- |
| Star Platinum | Jotaro Kujo | Primera entrega completa de mecánica y presentación |
| Hierophant Green | Noriaki Kakyoin | Segunda identidad para comprobar variedad y cooperación |
| Silver Chariot | Jean Pierre Polnareff | Catálogo principal de Crusaders |
| Magician's Red | Muhammad Avdol | Catálogo principal de Crusaders |
| Hermit Purple | Joseph Joestar | Catálogo principal de Crusaders |
| The Fool | Iggy | Catálogo principal de Crusaders |
| The World | DIO | Poder del encuentro final; obtención por jugadores pendiente de diseño |

El orden después de Star Platinum se ajustará al coste real de las habilidades y
los recursos visuales. Otros antagonistas y encuentros de Parte 3 podrán formar parte
del recorrido; su selección y sus recompensas se definirán antes de programarlos.
No se considera cerrado un catálogo solo por registrar estos nombres.

## Primer objetivo: Star Platinum con identidad propia

La [ficha oficial de Jotaro](https://jojo-portal.com/en/anime/sc/character/01/) describe
un Stand humanoide de gran fuerza, velocidad y precisión, caracterizado por las ráfagas
de puñetazos. Esta será la base del primer conjunto jugable.

La primera entrega de Star Platinum reunirá:

- Obtención y requisitos explicados dentro del juego.
- Invocación y seguimiento con identidad persistente y validación del servidor.
- Golpe preciso, ráfaga de golpes y respuesta defensiva, con reglas y límites propios.
- Preparación, impacto y recuperación legibles para quien lo usa y quien lo observa.
- Modelo, textura, animaciones, efectos y señales sonoras específicos.
- Progresión de aprendizaje y prueba con dos clientes en un servidor dedicado.

El conjunto exacto de acciones, sus valores y la defensa se cerrarán en su ficha de
habilidades, distinguiendo la referencia de la obra del balance para Minecraft.
El parámetro de fuerza no debe convertir todos los Stands en variantes del mismo golpe.

La parada temporal de Star Platinum se descubre durante el enfrentamiento final con
DIO; se tratará como un desarrollo avanzado de la progresión. Véase la referencia
secundaria [Star Platinum](https://jojowiki.com/Star_Platinum), con sus citas de capítulos.
La primera entrega puede cerrar el combate base; el lanzamiento completo deberá cerrar
también el desarrollo avanzado acordado para Parte 3.

### Entrega visual

Se usará como referencia de estilo el anime de Stardust Crusaders, manteniendo una
representación original adaptada a Minecraft y una paleta consistente. El modelo
debe reconocer la constitución, el cabello, la cinta, las hombreras y los elementos
de vestuario de esa versión; las fuentes editables acompañarán a los recursos de juego.

La revisión incluye silueta, UV, escala, manos y puños, articulaciones y visibilidad
en primera y tercera persona. La ráfaga necesita movimiento y transiciones propios;
se comprobarán preparación, contacto, recuperación y su coincidencia con el daño del servidor.
El estado de reposo, la invocación y la desinvocación también forman parte de la entrega.
El proyecto nuevo de Star Platinum será la referencia de proceso para los siguientes
Stands: observar la obra y varias interpretaciones, definir el diseño y construir
geometría, UV, textura y keyframes propios.

## Progresión de la primera versión

El recorrido propuesto es descubrir el fenómeno de los Stands, obtener uno, aprender
su uso, encontrar pistas y encuentros, dominar sus herramientas y prepararse para DIO.
Cada etapa debe ofrecer una decisión, un desafío o una posibilidad nueva de juego.

Las flechas ya implementadas conservan sus reglas aprobadas. Su uso general para
obtener Stands es una adaptación del mod al universo de JoJo; no se presentará como
la forma idéntica en que despertó cada personaje de Stardust Crusaders. La tabla de
obtención de los futuros Stands de Parte 3 se definirá expresamente, conservando los
IDs y los datos de Echoes ACT 1 y Little Feet que ya existan en partidas anteriores.

La voluntad tiene cinco desafíos de Star Platinum y uno de Emerald Splash con recompensa
única y progreso persistente, explicados en la ficha y en el panel. Se completan
con acciones propias, también contra enemigos compartidos. La propuesta posterior
es combinar este aprendizaje con exploración y superación de encuentros; esos
hitos y sus recompensas de grupo siguen pendientes de diseño.

El encuentro de DIO necesitará pistas para localizarlo, preparación, patrones legibles,
una forma definida de reintento y recompensas coherentes para quienes participen.
La cantidad de jugadores no debe provocar que solo el golpe final tenga valor.
La victoria y los desbloqueos personales se guardarán sin recompensas duplicadas por
reconectar o repetir una petición al servidor.

## The World y la parada temporal

La [ficha oficial de DIO](https://jojo-portal.com/anime/sc/character/07/) vincula The World
con el combate cercano y la capacidad de detener el tiempo durante unos segundos.
Su implementación debe representar esa capacidad y las interacciones específicas
de otros usuarios compatibles; los detalles se contrastarán con las escenas de Parte 3.

El diseño debe resolver antes de implementarse:

- Inicio, duración, enfriamiento y comunicación visual y sonora del efecto.
- Qué usuarios, entidades y proyectiles quedan afectados y cómo se restauran.
- Cómo se resuelven daño y acciones realizadas durante la parada.
- Interacción entre The World y Star Platinum avanzado, incluidas activaciones solapadas.
- Desconexión, muerte, cambio de dimensión y objetivos que entran o salen del efecto.
- Reglas de PvP y equipos, con el servidor como autoridad.

El sistema mantendrá el servidor y las conexiones funcionando. El efecto se aplicará
a los objetivos definidos por el diseño, con límites explícitos y sin dejar entidades
bloqueadas al terminar. No se mezclarán habilidades o versiones alternativas de otras
continuidades bajo el nombre de The World de Parte 3.

## Lugar del contenido ya desarrollado

Echoes ACT 1 y Little Feet se mantienen como contenido del prototipo y con sus datos
compatibles. El sistema de discos, el servicio del guardián, el loot y el crafteo
existentes también se conservan como base del mod completo.

Whitesnake, el jefe asociado a su núcleo y la extracción hostil pertenecen al plan
posterior de Stone Ocean. Se conserva el balance de extracción ya aprobado en
[Fase 4](phase-four-design.md). Estos elementos no se atribuyen a la historia de Parte 3.
La planificación del lanzamiento deberá explicar claramente qué sistemas transversales
están disponibles y qué contenido pertenece a futuras expansiones.

## Prueba de cierre

Una versión candidata deberá permitir jugar su recorrido de supervivencia en un
servidor dedicado con al menos dos clientes reales. Cada Stand de su catálogo tendrá
modelo, animaciones, habilidades y límites propios; las recompensas y los datos
persistirán correctamente. Se comprobarán colaboración, PvP, muerte y reconexión.

El encuentro final y la parada temporal necesitarán pruebas específicas de objetivos
y proyectiles, restauración del estado y activaciones simultáneas. Se registrarán
también el tiempo de progresión, los problemas de comprensión y las tareas repetitivas
detectadas durante partidas compartidas. Los GameTests apoyan esa validación y no la sustituyen.
