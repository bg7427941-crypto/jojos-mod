# Hierophant Green — primera prueba jugable, 0.11.0

Segundo Stand de la primera versión de Stardust Crusaders, con Noriaki Kakyoin
como usuario de referencia. Su identidad inicial es atacar a distancia con
Emerald Splash. Las técnicas de tentáculos, posesión y barrera quedan para otra
entrega; registrar este Stand no significa que su repertorio esté completo.

Referencias para contrastar la representación: el
[catálogo oficial del anime de Parte 3](https://jojo-portal.com/en/anime/sc/character/)
y la ficha secundaria [Hierophant Green](https://jojowiki.com/Hierophant_Green).
La representación parte de su humanoide verde con trama clara, máscara y placas,
con geometría, atlas, animaciones y efectos sonoros nuevos del proyecto.
Sus parámetros de referencia son poder C, velocidad B, alcance A, resistencia B,
precisión C y potencial D. El perfil de combate de Minecraft se define aparte.

## Recorrido

La flecha Stardust pasa a sortear Star Platinum o Hierophant Green con **50 % por
Stand**. Conserva su obtención en templos del desierto, consumo y rechazo si ya
hay un Stand asignado. La flecha antigua conserva Echoes ACT 1 y Little Feet.
Ninguna asignación existente se vuelve a sortear. Las flechas y discos siguen
siendo adaptaciones de progresión para Minecraft, no el despertar literal de Kakyoin.

El primer despertar de Hierophant Green concede +2 de voluntad una sola vez.
Un núcleo y un disco vacío fabrican su disco identificado; el núcleo se ofrece en
creativo y comandos, como el de Star Platinum. Su disco requiere 3 de voluntad
con la configuración fuerte predeterminada y funciona con el guardián existente.

## Emerald Splash — Z con el Stand invocado

- Preparación de **6 ticks (0,3 s)**, tres pulsos separados por 2 ticks y recuperación
  hasta el tick 18. Cada pulso lanza tres esmeraldas en abanico: **9 proyectiles**.
- Cada esmeralda causa **1 punto antes de armadura**, con un máximo teórico de
  9 por descarga si todas aciertan. Las esmeraldas atraviesan el intervalo normal
  de invulnerabilidad al daño, pero respetan armadura y guardia frontal.
- Alcance de recorrido por proyectil **14 bloques**, velocidad inicial 1,2
  bloques/tick y vida máxima de 25 ticks. Las paredes detienen el ataque.
- Enfriamiento de **100 ticks (5 s)** desde la activación; los fallos lo consumen.
  Solo se admite una descarga propia a la vez. La puntería se actualiza por pulso.
- No consume ni genera esmeraldas de inventario. Los proyectiles no se guardan.
- Un objetivo aliado, el usuario o un jugador protegido por PvP/equipos no recibe
  daño. Las esmeraldas pueden pasar junto a aliados para facilitar la cooperación.
- Desinvocar, perder el Stand, recibir daño de salud, morir, desconectar o cambiar
  de dimensión cancela los pulsos pendientes y las esmeraldas de esa descarga.
  La recuperación normal permite terminar el vuelo de las ya lanzadas.

Son valores de balance del mod; no equivalen a una conversión de los parámetros
del manga. La valoración del abanico, legibilidad, modelo y ritmo exige juego real.

## Voluntad y dominio

Causar daño de salud con Emerald Splash a **tres hostiles distintos** concede
**+1 de voluntad** y reduce el enfriamiento a **80 ticks (4 s)** desde la próxima
activación. Cada enemigo cuenta una vez para ese jugador; no se exige el golpe
final. Enemigos compartidos pueden servir a ambos participantes por sus propias
acciones. Jugadores, pasivos, aliados, creativo/espectador y daño completamente
absorbido no dan progreso. Despertar y dominio se conservan al guardar, morir y
recuperar el Stand. El progreso se sincroniza solo con su propietario.

La versión usa **protocolo 9**; cliente y servidor deben actualizarse juntos.
