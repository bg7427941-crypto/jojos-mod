# Referencias sonoras de Star Platinum — Parte 3

Consulta: 26 de octubre de 2026. Objetivo: reunir señales del anime japonés de
Stardust Crusaders y asignarlas a los ataques, usando varias fuentes de referencia.

Esta entrega es un **catálogo de enlaces y puntos de integración**, con
[datos estructurados](../art/sounds/anime-reference-catalog.json). No es un paquete
de grabaciones: no se han descargado ni incorporado nuevos OGG al JAR. Se revisaron
las páginas y sus metadatos; la edición exacta, la limpieza del audio y sus puntos
de corte necesitan comprobarse escuchando cada grabación. Las duraciones indicadas
son objetivos del mod, no mediciones de los enlaces.

## Referencias para escuchar

| Señal | Referencia | Qué revisar |
| --- | --- | --- |
| Invocación | [Star Platinum Summon Sound Effect, Shortdragonfire](https://tuna.voicemod.net/sound/92279f39-0b9a-4982-ad65-05609880cc9c) | Separar la llamada del personaje del efecto de aparición; confirmar la edición de Parte 3 |
| ORA individual | [Star Platinum Single ORA, wantcookies](https://www.myinstants.com/en/instant/star-platinum-single-ora-14465/) | Una exclamación limpia, sin música ni remates añadidos |
| Ráfaga ORA | [Star Platinum Ora Ora Ora, YourNormalApexx](https://www.myinstants.com/en/instant/star-platinum-ora-ora-ora-1587/) | Cadencia y posibilidad de una frase breve que termine limpiamente |
| Otra ráfaga | [StarPlatinumOra.wav, JoJo Wiki](https://jojowiki.com/File:StarPlatinumOra.wav) | La página del archivo no identifica un episodio de origen; no dar su edición por verificada |
| Star Finger | [Jojo_star finger](https://instantsbutton.com/id/sound/jojo-star-finger) | La página identifica la llamada; contrastarla con el episodio 6 |
| Aire, impactos y energía | [Star Platinum: Part 3 — Stand Sound Profiles, Jojo's Bizarre Sound Design](https://www.youtube.com/watch?v=1QQsrGRpgpY) | Recopilación localizada por metadatos; verificar cada escena antes de elegir un corte |

El «Part 3» del título de una recopilación puede indicar la entrega del autor;
por sí solo no verifica que todos sus clips sean de Stardust Crusaders. No se
dan minutos exactos sin comprobarlos. Los soundboards son candidatos para escuchar,
no fuentes con edición y permiso de redistribución ya confirmados.

## Escenas para contrastar

Las fichas oficiales ayudan a localizar las escenas en una copia autorizada;
son contexto narrativo, no bancos de efectos separados:

- [Episodio 1](https://jojo-animation.com/sc/contents/story/01.html): aparición inicial y precisión; revisar la captura de la bala.
- [Episodio 6, Dark Blue Moon](https://jojo-animation.com/sc/contents/story/06.html): llamada y extensión de Star Finger.
- [Episodio 17, Lovers, segunda parte](https://jojo-animation.com/sc/contents/story/17.html): identidad de la ráfaga; la del mod dura 1,5 s.
- [Episodio 47, DIO's World, tercera parte](https://jojo-animation.com/sc/contents/story/47.html): intercambio de golpes entre Star Platinum y The World.
- [Episodio 48](https://jojo-animation.com/sc/contents/story/48.html): referencia para la futura parada temporal de Star Platinum.

Confirmar audio japonés de la adaptación de Parte 3. OVA, videojuegos, otras partes
y voces de DIO se catalogarían como variantes distintas. La parada temporal aún
no tiene habilidad ni evento sonoro en el mod.

## Asignación a los eventos actuales

El prefijo de eventos es `jojosmod:star_platinum.`. Los OGG son los nombres de
entrada que reconoce el constructor del paquete local.

| Uso | Evento | Entrada | Disparo y objetivo |
| --- | --- | --- | --- |
| R, invocar | `summon` | `summon.ogg` | Una vez al invocar; unos 0,4–0,8 s |
| R, retirar | `dismiss` | `dismiss.ogg` | Al desinvocar; unos 0,2–0,35 s |
| Z, ORA individual | `single_ora` | `ora.ogg` | Una vez al aceptar Z, unos 0,3 s; vacío sin pack |
| Z/X/V, aire | `swing` | `swing.ogg` | Por contacto previsto; hasta unos 0,15 s, sin frase larga |
| Z/X/V, impacto | `impact` | `impact_1.ogg`, `impact_2.ogg` | Al acertar; variantes de unos 0,2 s |
| X, voz de ráfaga | `barrage_start` | `barrage_start.ogg` | Una vez al iniciar; hasta 1,5 s para sus ocho golpes |
| C, preparación | `guard` | `guard.ogg` | Al entrar en guardia; unos 0,25–0,32 s |
| C, bloqueo | `guard_block` | `guard_block.ogg` | Al bloquear daño válido; unos 0,2 s |
| V, Star Finger | `star_finger` | `star_finger.ogg` | Desde el inicio; contacto a 0,4 s y acción de 0,9 s |
| B, preparación | `intercept` | `intercept.ogg` | Unos 0,1 s; no implica acierto |
| B, captura | `intercept_catch` | `intercept_catch.ogg` | Al neutralizar la flecha; unos 0,18 s |
| J, impulso | `leap` | `leap.ogg` | Al despegar; unos 0,28 s |
| J, apoyo | `leap_land` | `leap_land.ogg` | Al aterrizar; unos 0,2 s |

Desde **0.9.3**, el ORA individual tiene el evento propio `single_ora` y el
constructor admite `ora.ogg`. `swing` sigue compartido por Z, X y V y se reserva
para aire breve. La parada temporal todavía no tiene evento ni `time_stop.ogg`.

X hace contacto a 0,20; 0,35; 0,50; 0,65; 0,80; 0,95; 1,10 y 1,25 s. La frase va
en `barrage_start`; los sonidos por pulso deben ser breves. R, Z, X y V usan un canal
por propietario: una señal nueva lo sustituye y la cancelación lo corta, sin
interrumpir al otro jugador.

Una llamada completa de Star Finger puede durar más que su acción de 0,9 s. La
prueba local conserva una frase de unos 2,53 s y permite su cola después de la
pose; una nueva voz o cancelación del propietario la interrumpe. Su sincronización
requiere valoración escuchándola en el juego. No se acelera ni cambia el daño para acomodarla. Las acciones
de impulso, guardia y precisión pueden usar efectos breves sin una frase para cada una.

## Estado y preparación

No se encontró en las fuentes consultadas una autorización explícita para
redistribuir estas grabaciones en el mod. JoJo Wiki identifica su WAV como
[material sin licencia de reutilización](https://jojowiki.com/File:StarPlatinumOra.wav)
mostrado bajo su criterio de uso ilustrativo. Un botón de descarga no establece
por sí solo ese permiso. Los enlaces quedan como referencias; la selección de
grabaciones para distribución está pendiente.

Los clips autorizados se preparan en **OGG Vorbis mono, 48 kHz**, con fundidos
breves y nivel consistente. El [constructor existente](anime-audio.md) permite
probarlos por evento sin cambiar el JAR. El audio de ataques se dispara desde el
servidor, no también desde la animación de Blockbench.

La primera selección útil sería invocación, ráfaga y Star Finger; después, impactos
y defensas. La prueba con sonido real debe comprobar cancelaciones, solapamiento
entre golpes y canales independientes para dos propietarios.
