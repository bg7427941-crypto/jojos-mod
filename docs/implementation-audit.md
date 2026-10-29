# Auditoría e implementación — Parte 3

Base revisada: 0.13.1; implementación: **0.14.0**. La primera versión completa acordada se centra en
Stardust Crusaders. El código real prevalece sobre las notas históricas de README.

| Estado | Carencia comprobada | Entrega que la resuelve |
| --- | --- | --- |
| Implementado | El guardián solo existe por comandos | Aparición persistente junto a templos del desierto y orientación en el juego |
| Implementado | No hay encuentro con DIO | Desafío voluntario, patrones anunciados, The World, cuchillos y parada temporal; reintento sin destruir el templo |
| Implementado | The World y los núcleos no se obtienen jugando | Victoria compartida y reclamación individual persistente del núcleo de The World |
| Implementado | No hay recorrido ni hitos de exploración/encuentro | Diario de viaje, avances y recompensas únicas de voluntad |
| Implementado | Hierophant Green solo tiene Emerald Splash | Tentáculo de sujeción y barrera de detección/contraataque |
| Implementado | Chariot no lanza su estoque; The World no lanza cuchillos | Técnicas avanzadas con proyectiles físicos y recuperación |
| Implementado | Falta utilidad avanzada de otros Crusaders | Detector de vida, conducción de Hamon y protección de arena para aliados |
| Implementado | Usuarios compatibles se mueven toda la parada ajena | Presupuesto temporal individual, sin renovarlo al salir/entrar |
| Prueba humana | Falta partida de principio a fin con dos personas | Prueba manual documentada, además de pruebas nativas de servidor/cliente |

## Criterios de implementación

Servidor autoritativo; paredes, equipos y PvP respetados; sin modificar bloques de
construcciones; costes de discos conservados; recompensas sin depender del golpe
final ni duplicarse al reconectar; enfriamientos conservados al interrumpir; efectos
temporales y modificadores retirados al terminar o cambiar de dimensión/Stand.
Los datos de partidas anteriores y sus desbloqueos se conservan.

Los encuentros y sus hitos son adaptaciones de supervivencia para Minecraft.
La representación de las técnicas se distingue del balance; no se añaden poderes
de otras continuidades a The World de Parte 3. La posesión de jugadores, el control
ambiental sin límites y el repertorio de cada escena del manga requieren diseño
propio; esta auditoría cierra el recorrido y las herramientas principales del catálogo.

## Expansiones conservadas

Echoes ACT 1 y Little Feet mantienen sus datos y el prototipo anterior. Sus poderes,
modelos y evoluciones completos, y Whitesnake con extracción hostil y el combate
específico del guardián, pertenecen a expansiones de otras partes. No se atribuyen
a la campaña de Stardust Crusaders ni se anuncian como funciones implementadas.

## Validación y límites de cierre

La suite nativa de servidor comprueba el combate, interrupciones, presupuesto
temporal, persistencia, recetas, almacenamiento, participantes, inventario lleno,
reclamaciones únicas y datos anteriores. La revisión de cliente comprueba modelos,
poses, proyectiles, efectos y orientación. La guía de prueba registra por separado
las comprobaciones automáticas y la partida humana necesaria para valorar balance.

Los encuentros activos se reinician al descargarse/cerrar el servidor; no guardan
combate parcial ni entregan recompensas sin victoria. Las victorias, voluntad,
localización del guardián y reclamaciones sí se guardan. DIO se quema con el sol
y pertenece a los no muertos, por lo que la técnica de Hamon aplica su bonificación.

### Evidencia de esta entrega

- **168 GameTests nativos aprobados**, incluidas las 132 comprobaciones anteriores
  y 36 nuevas de campaña, poderes, guardado, reclamación y tiempo detenido.
- Cliente real: tentáculo con 4 de daño, estoque con 9 y cuchillos en abanico con
  impactos físicos; barrera y modelos propios visibles; voz de DIO activa en el
  motor y shader temporal de inicio/fin comprobado.
- Guardián aparecido automáticamente al entrar en un templo generado real.
- **Dos clientes reales automatizados por LAN**: ambos contribuyen contra DIO,
  reciben su propia victoria y un núcleo cada uno. Tras reconectar el segundo
  cliente conserva la victoria y exactamente un núcleo.
- Reconstrucción idéntica de recursos, proyectos Blockbench y manifiestos desde
  las fuentes. Los 14 sprites de 0.13.1 conservan sus hashes. JAR íntegro, sin QA.

La evaluación de equilibrio y diversión con dos personas sigue siendo la prueba
humana de cierre. El controlador de audio del entorno es nulo: se verifica la
selección/reproducción en el motor, y la escucha corresponde a la prueba del jugador.
