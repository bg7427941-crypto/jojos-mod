# Visión del mod y criterios de una versión completa

Este documento recoge la meta del usuario: un mod de JoJo completo, fiel a la obra,
con modelos y animaciones bien terminados, progresión entretenida y una experiencia
estable para jugar con amigos. Es el criterio para organizar las próximas entregas;
no describe funciones que ya estén implementadas.

## Primera versión: una parte concreta

El usuario eligió centrar la primera versión completa y pulida en **la Parte 3:
Stardust Crusaders**. Su catálogo de Stands, encuentros, ambientación y progresión
se organizará alrededor de esa parte. El [plan de lanzamiento de Parte 3](part-three-release.md)
distingue la decisión confirmada del catálogo propuesto y de las tareas pendientes.

La meta del mod completo conserva las mecánicas ya acordadas de flechas, voluntad,
núcleos, discos, almacenamiento voluntario y extracción hostil con Whitesnake.
Whitesnake y el desarrollo específico de Stone Ocean pasan a una expansión posterior.
Los sistemas de discos ya existentes se conservan, junto con sus ítems, identificadores
y datos guardados; su papel es una adaptación transversal del mod, no un elemento de
la historia de Parte 3. El primer recorrido de lanzamiento se centrará en Parte 3.

## Estado real de la base

La versión 0.4.2 es un prototipo funcional de sistemas. Tiene flechas, asignación
persistente, invocación, golpe básico, loot de fragmentos, crafteo de discos vacíos,
dos núcleos de prueba, equipamiento con voluntad y almacenamiento voluntario.
Sus 43 GameTests y la comprobación con un cliente verifican esos recorridos.

Echoes ACT 1 y Little Feet todavía comparten un maniquí y un golpe genérico.
Whitesnake no está implementado. En 0.6.0, Star Platinum añade Star Finger y tres
desafíos de voluntad que se completan con acciones propias en supervivencia;
los núcleos se obtienen con creativo/comandos y el guardián se invoca por comandos.
El juego con dos clientes remotos sigue pendiente. Estos puntos impiden considerar
terminada una experiencia de supervivencia o un Stand solo por estar registrado.

## Fidelidad a JoJo

Cada Stand tendrá una ficha antes de implementar sus poderes y sus recursos finales:

- Parte, usuario de referencia y capítulos o episodios que sustentan la habilidad.
- Apariencia, silueta, proporciones y versión de color elegida como referencia.
- Activación, objetivos válidos, alcance, persistencia, límites y formas de contrarrestarlo.
- Relación entre usuario y Stand: visibilidad, control, daño y excepciones propias.
- Evoluciones y condiciones narrativas, cuando correspondan.
- Adaptaciones para Minecraft, con su motivo y sus diferencias respecto a la obra.

Los parámetros del manga orientan la identidad del Stand. No se convierten
automáticamente en daño o bloques mediante una única fórmula para todos.
Las reglas de una habilidad y su presentación deben corresponderse: la animación,
el efecto y el resultado del servidor tienen que explicar la misma acción.

La revisión del prototipo ya identifica estas diferencias:

| Stand | Referencia de la obra | Trabajo que falta en el prototipo |
| --- | --- | --- |
| Echoes | ACT 1 aplica sonidos; sus ACT tienen habilidades y formas distintas | Poder sonoro y modelo no humanoide propios de ACT 1; diseño separado de las evoluciones |
| Little Feet | El corte inicia una reducción gradual del objetivo; la reducción del usuario tiene reglas distintas | Corte, cambio de tamaño, límites, restauración y presentación sincronizados |
| Whitesnake | Extrae discos de Stand y de memoria; también tiene capacidades de ilusión | Identidad y habilidades propias; separar el sistema de Stand del eventual sistema de memoria |

Referencias secundarias consultadas: [Echoes](https://jojowiki.com/Echoes),
[Little Feet](https://jojowiki.com/Little_Feet) y
[Whitesnake](https://jojowiki.com/Whitesnake). Sus referencias a capítulos y episodios
sirven para localizar las escenas que habrá que contrastar al cerrar cada ficha.
Las mecánicas de otros videojuegos no se toman por sí solas como prueba de canon.

### Adaptaciones que ya forman parte del diseño

Los núcleos fabricables, el loot de Ancient Cities, el guardián original, la voluntad
numérica y los costes y tiempos de extracción son reglas de este mod. Se presentarán
como adaptaciones de juego, sin atribuirles un origen canónico inexistente.

Se mantienen las decisiones anteriores recogidas en
[el diseño de Fase 4](phase-four-design.md): el coste escaso del disco vacío, la flecha
sin daño ni riesgo de muerte y las condiciones de extracción de Whitesnake. La nueva
meta de fidelidad no cambia silenciosamente esas reglas aprobadas.

## Modelos, animaciones y presentación

Cada Stand del catálogo de lanzamiento necesita un modelo propio reconocible por
su silueta, proporciones y detalles, también a distancia y con la cámara de Minecraft.
Los modelos, texturas, efectos y sonidos se crearán para el proyecto o usarán recursos
con licencia compatible, conservando las fuentes editables y su procedencia.

La entrega visual de un Stand incluye:

- Modelo editable y geometría de juego con UV y textura revisadas.
- Invocación, reposo, desplazamiento y desinvocación coherentes con su forma.
- Preparación, ejecución y recuperación de cada ataque o habilidad.
- Transiciones entre estados sin saltos ni extremidades que atraviesen el cuerpo.
- Efectos y señales sonoras que permitan reconocer activación, impacto, interrupción y recuperación.
- Prueba en primera y tercera persona y desde otro cliente, con sombras y escala correctas.

El modelo de entrenamiento puede seguir sirviendo para depuración. Los Stands del
catálogo de lanzamiento se completarán con sus propios recursos y habilidades.
Una entrega de Stand debe incluir mecánica, presentación y validación juntas.

## Progresión divertida y comprensible

El recorrido debe poder jugarse en supervivencia: descubrir una vía de obtención,
conseguir un Stand, aprender a usarlo, explorar, superar desafíos y acceder a objetivos
más exigentes. La parte elegida dará contexto a esos encuentros y recompensas.

La voluntad tendrá hitos persistentes y variados, con recompensa única por hito.
El catálogo y los valores concretos de esos hitos siguen pendientes de diseño.
Repetir una acción trivial, morir o reconectar no debe duplicar sus recompensas.
El aprendizaje y la habilidad del jugador también deben abrir oportunidades de victoria.

Los objetivos y los requisitos estarán explicados dentro del juego. La exploración
debe aportar pistas para localizar encuentros y materiales; los jefes tendrán patrones
que se puedan aprender, recompensas útiles y una forma clara de volver a intentarlo.
El almacenamiento permite cambiar de estrategia conservando el Stand conseguido.

El balance deberá comprobar tanto la primera obtención como las sesiones posteriores:
tiempo real de exploración, utilidad de cada recompensa, repetición de tareas y opciones
para un amigo que llega más tarde. Los costes ya aprobados se mantienen como base;
el juego con personas aportará evidencia para proponer ajustes concretos.

## Jugar con amigos

El multijugador es un criterio de entrega desde el primer Stand completo:

- Servidor dedicado y al menos dos clientes reales, además de los GameTests.
- Identidad, modelo, animación y efectos consistentes para propietario y observadores.
- Poderes útiles para colaborar y con respuestas comprensibles en PvP.
- Respeto a PvP y equipos; la extracción hostil conserva las condiciones aprobadas.
- Recompensas y progreso individual definidos para encuentros compartidos.
- Guardado, muerte, cambio de dimensión, desconexión y reconexión durante habilidades.
- Pruebas con latencia y peticiones repetidas para detectar duplicaciones y estados bloqueados.

El robo de Stand debe mantener sus señales, interrupciones, enfriamientos y rutas
de recuperación. Las partidas con PvP desactivado conservan esa protección.
Las pruebas con FakePlayers no sustituyen la comprobación con dos clientes.

## Orden de las próximas entregas

| Entrega | Resultado jugable | Prueba de cierre |
| --- | --- | --- |
| Cerrar el primer catálogo de Parte 3 | Fichas de referencia, recorrido y alcance de la primera versión | Cada Stand tiene función, límites y relación con la progresión |
| Star Platinum terminado | Obtención, combate propio, modelo, animaciones y explicación en el juego | Recorrido de supervivencia y uso observado por otro jugador |
| Progresión y encuentro natural | Hitos de voluntad y encuentro localizable con recompensas | Dos jugadores pueden progresar sin comandos ni duplicar recompensas |
| Completar el catálogo de lanzamiento | Cada Stand aporta decisiones y posibilidades distintas | Mecánica, arte, animación y contrajuego validados por Stand |
| Encuentro de DIO y parada temporal | Desafío final coherente con Parte 3 y progresión de sus poderes | Interacción entre usuarios, objetivos y proyectiles con dos clientes |
| Candidato de lanzamiento | Recorrido completo, balance, rendimiento y documentación | Partida compartida de principio a fin y ausencia de fallos bloqueantes |

La aparición y el combate del guardián siguen siendo trabajo pendiente del diseño
anterior. Se retomarán con el desarrollo de discos y Whitesnake; el siguiente objetivo
principal es Star Platinum dentro del recorrido de Parte 3. El catálogo futuro puede
ampliarse a otras partes una vez cerrado y probado el primer lanzamiento.

## Cuándo se considera terminada la primera versión

La primera versión está completa cuando el catálogo y el recorrido acordados se
pueden jugar de principio a fin en supervivencia, sus Stands tienen identidad mecánica
y visual propia, y el progreso y los encuentros funcionan con amigos en un servidor
dedicado. El guardado y las transferencias deben conservar los datos y objetos
correctos, y la documentación debe distinguir el contenido disponible del pendiente.

Cada PR indicará qué mejora para el jugador, cómo se obtiene y se utiliza, qué referencia
de la obra sigue, qué adaptaciones incorpora y qué se probó realmente. Una compilación
correcta y los tests son necesarios; el cierre también requiere comprobar la experiencia
jugable y su presentación.
