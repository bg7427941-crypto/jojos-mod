# Prueba de combate y presentación — 0.13.0

Minecraft 26.3, NeoForge 26.3.0.95 y GeckoLib 5.5.7. Actualiza **cliente y
servidor** juntos: **protocolo 12**.

## Modelo y animaciones

Hierophant Green sustituye el modelo provisional por una figura basada en las
proporciones del jugador: 79 cubos, máscara blanca segmentada, ojos amarillos
con ranuras, entramado de esmeralda y bandas que envuelven torso y extremidades.
La malla, la textura y las poses son originales y el proyecto editable está en
`art/hierophant_green/hierophant_green.bbmodel`.

Los ataques Z humanoides duran ahora **1 s**, Emerald Splash **1,6 s** y Star
Finger **1,35 s**. Las ráfagas duran **1,85 s** para Chariot y **2,2 s** para Star
Platinum/The World. La finalización normal del servidor permite terminar el
movimiento de recuperación. Recibir daño que interrumpe, desinvocar o cambiar
Stand sigue cancelando la acción. Un ataque nuevo puede sustituir la recuperación
visual; no se alargaron los cooldown ni los primeros contactos para hacerla esperar.

Prueba Z al aire, espera sin pulsar otra tecla y observa la recuperación. Después
prueba X completo, interrúmpelo con R y repite con otro amigo atacando a tu lado.
Las lianas de Hermit Purple siguen usando las poses reales del jugador.

## Daño base

Un corazón equivale a dos puntos de vida. Estas cifras son anteriores a armadura,
resistencia y defensa del objetivo; las reglas de equipos y PvP se conservan.

| Stand / habilidad | Antes | Ahora |
| --- | ---: | ---: |
| Star Platinum Z | 6 | 9 |
| ORA X, ráfaga completa | 8 × 1 = 8 | 12 × 1,5 = 18 |
| Star Finger V | 4 | 7 |
| The World Z | 6 | 9 |
| MUDA X, ráfaga completa | 10 × 1 = 10 | 12 × 1,5 = 18 |
| Silver Chariot Z | 4 | 6 |
| Chariot X, ráfaga completa | 6 × 1 = 6 | 10 × 1,2 = 12 |
| Crossfire Hurricane Z | 6 | 10 |
| Red Bind X | 3 | 5 |
| Hermit Purple Z | 3 | 5 |
| The Fool Z, tres pulsos | 3 × 1 = 3 | 3 × 2 = 6 por objetivo |
| Emerald Splash, por proyectil | 1 | 3 |

Emerald Splash mantiene nueve proyectiles en abanico: el daño depende de cuántos
alcancen al objetivo, con un máximo teórico de 27. No todos los proyectiles van
a acertar a un enemigo lejano. El abanico ahora apunta al impacto real de la
mira del servidor, corrigiendo los fallos a corta distancia causados por la
separación entre el hombro del Stand y la cámara. La defensa y la movilidad no reciben daño añadido.

## Audio por acción

Los clips se convierten a OGG Vorbis mono de 48 kHz, con ganancia limitada y colas
suavizadas. Las voces usan un canal por propietario: se reproducen una vez al
aceptar la acción y una voz nueva sustituye la anterior de ese jugador. Los
intentos rechazados no vuelven a dispararlas y cancelar a un jugador no corta a otro.

| Stand | Asignación |
| --- | --- |
| Star Platinum | ORA de Z, frase ORA completa más larga para X, llamada de Star Finger y detención temporal |
| The World | MUDA de Z, ráfaga MUDA propia para X, activación y reanudación temporal |
| Hierophant Green | Kakyoin llama Emerald Splash una vez; cada lanzamiento tiene su efecto breve |
| Magician's Red | Avdol llama Crossfire Hurricane en Z y Red Bind en X; disparo e impacto de fuego separados |
| Silver Chariot | Grito de estocada para Z, ráfaga de Polnareff en X, metal al impactar y sonido de liberación de armadura en C |
| Hermit Purple | Joseph llama Hermit Purple al usar Z o la liana de X |
| The Fool | Impactos del anime combinados con la capa original de arena; conserva efectos de arena para coraza y planeo, sin voz humana añadida |

Hay subtítulos para las llamadas y efectos. Ajusta **Jugadores** en Música y
sonidos. Comprueba que X no repita una frase por cada golpe, que Red Bind no use
la llamada de Crossfire y que Z de Hierophant Green diga Emerald Splash.

Las fuentes, revisiones, recortes, ganancias y hashes están en
[`art/anime_audio/`](../art/anime_audio/README.md). El audio recopilado conserva
sus términos de origen; no se presenta como síntesis original del proyecto.
