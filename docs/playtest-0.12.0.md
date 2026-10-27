# Prueba de los siete Stands de Parte 3 — 0.12.0

Cliente y servidor: Minecraft 26.3, NeoForge 26.3.0.95, GeckoLib 5.5.7, mod 0.12.0,
**protocolo 10**. Compila con Java 25 (`./gradlew build` o `gradlew.bat build` en
Windows) y sustituye el JAR anterior por `build/libs/jojosmod-0.12.0.jar`.

Esta entrega añade Silver Chariot, Magician’s Red, Hermit Purple, The Fool y The
World. Star Platinum y Hierophant Green siguen disponibles. Consulta las
[técnicas, límites y adaptaciones](part-three-stands-design.md): son primeras
versiones jugables; el recorrido de DIO y los repertorios completos siguen pendientes.
Los sonidos nuevos son síntesis original. Se conservan los recursos anteriores.

## Elegir Stand de prueba

Usa un mundo de prueba con comandos. Para cada cambio:

```mcfunction
/standtest clear
/standtest willpower 3
/give @s jojosmod:stand_disc[jojosmod:stand_id="jojosmod:silver_chariot"]
```

Sustituye `silver_chariot` por `magicians_red`, `hermit_purple`, `the_fool` o
`the_world`. Equipa el disco con clic derecho al aire. El primer despertar concede
+2 de voluntad una vez: este atajo parte de 3 y deja 5, así que permite probar G
con The World. Para ensayar el recorrido natural, usa una flecha Stardust desde
voluntad inicial 1: despertar deja 3 y el dominio de tres hostiles distintos deja 4.
La flecha ofrece los seis Crusaders al 1/6; The World es por comandos/creativo hasta
implementar el encuentro de DIO. Las asignaciones existentes no se sortean otra vez.

R invoca; Z es ataque principal; X secundaria; C utilidad/defensa; G parada temporal.
V/B/J conservan las técnicas previas de Star Platinum. `/stand progress` explica el
conjunto seleccionado y su aprendizaje. Se respetan las teclas reasignadas.

## Técnicas que probar

| Stand | Prueba |
| --- | --- |
| Silver Chariot | Z contra un enemigo cercano; X debe dar seis estocadas. C retira placas durante cinco segundos, acelera Z y hace recibir un 25 % más de daño. |
| Magician’s Red | Z contra enemigos, paredes e inmunes al fuego. X ralentiza un blanco visible durante dos segundos. Recibe daño o pierde visión: la atadura debe desaparecer. C reduce daño durante dos segundos. No debe aparecer fuego en construcciones. |
| Hermit Purple | Z hasta cinco bloques. X apuntando a un bloque sólido a 2–12 bloques: debe tirar físicamente del usuario; aire o pared intermedia lo detienen. C informa del hostil más cercano hasta 16 bloques, incluso tras una pared. |
| The Fool | Z en cono: tres pulsos de arena. X protege dos segundos. C planea tres segundos y evita daño de caída mientras esté activo; después vuelve el riesgo de una nueva caída. |
| The World | Z a corta distancia, X diez impactos MUDA y C guardia frontal: desde atrás debe recibir daño normal. G, con voluntad 4, detiene el tiempo cercano tres segundos. |
| Star Platinum | Conserva todas sus técnicas anteriores. G con voluntad 6 detiene el tiempo cercano dos segundos. |

Comprueba preparación, contacto, recuperación y los enfriamientos. Valora tamaño,
silueta, atlas, manos/lianas, dirección del estoque, alas de arena, transiciones,
legibilidad del HUD y sonidos en primera/tercera persona. Los modelos editables se
encuentran en `art/part_three/<stand>/<stand>.bbmodel`.

## Parada temporal con amigos

1. Usad la misma versión 0.12.0 en todos los clientes y en el servidor.
2. Con PvP habilitado y sin compartir equipo, un rival dentro de 12 bloques debe
   quedar quieto sin poder atacar, usar objetos, romper/colocar o soltar ítems.
   Puede seguir mirando alrededor y la conexión debe mantenerse.
3. Aliados, creativo, espectadores y jugadores protegidos por PvP quedan exentos.
   Otro The World con voluntad 4 o Star Platinum con 6 también puede moverse.
4. Prueba mobs y flechas entrando en la esfera, también proyectiles del propio
   usuario. Al reanudar deben recuperar el movimiento; no deben quedar congelados.
5. Intenta G simultáneamente: no debe apilarse ni renovarse la duración. Reintenta
   durante el enfriamiento de 30 s: se rechaza.
6. Durante G, desinvoca, muere, desconecta, cambia de dimensión o sal del radio
   con el usuario: todos deben quedar liberados. Guarda y vuelve a abrir el mundo.
7. Comprueba que un enemigo fuera de la esfera y las máquinas/bloques siguen activos.

## Aprendizaje, almacenamiento y regresiones

- Daña con técnicas la salud de tres hostiles distintos: +1 de voluntad una vez y
  enfriamiento principal un 20 % menor desde la siguiente activación.
- Compartid enemigos: ambos avanzan por sus propias acciones. Pasivos, aliados,
  jugadores y daño totalmente absorbido no dan progreso.
- Muere, reconecta y almacena/recupera cada Stand con el guardián; identidad y
  aprendizaje deben conservarse sin duplicar recompensas ni discos.
- Repite Star Platinum y Emerald Splash para detectar regresiones. Prueba equipos,
  PvP deshabilitado y cambios de dimensión durante fuego, atadura, agarre y parada.

Esta guía pide valorar sensaciones, presentación, escucha y una partida con dos
clientes reales. Los GameTests comprueban las reglas de servidor y no sustituyen
esa sesión compartida.
