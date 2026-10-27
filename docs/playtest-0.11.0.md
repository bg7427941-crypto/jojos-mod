# Hierophant Green y Emerald Splash — prueba 0.11.0

Esta entrega permite probar un segundo estilo de combate de Parte 3 junto a Star
Platinum. Incluye Emerald Splash, modelo y textura originales, seis animaciones,
cinco sonidos sintetizados y dominio persistente. Tentáculos, posesión y barrera
siguen pendientes; el [diseño](hierophant-green-design.md) describe el alcance.

## Instalar

1. Compila con Java 25: `./gradlew build`, o `gradlew.bat build` en Windows.
2. Sustituye el JAR anterior por `build/libs/jojosmod-0.11.0.jar` en `mods`.
3. Usa Minecraft 26.3, NeoForge 26.3.0.95 y GeckoLib 5.5.7.
4. Cliente y servidor deben usar 0.11.0, **protocolo 9**. El JAR conserva también
   los ocho clips de Star Platinum subidos manualmente a `main`.

## Obtener y usar el Stand

En supervivencia, una flecha Stardust sortea Star Platinum o Hierophant Green con
50 % de probabilidad cada uno. Su aparición en cofres de templo del desierto sigue
siendo del 20 % por cofre nuevo. Las asignaciones existentes no cambian y la
flecha rechaza a quien ya tenga Stand. La flecha antigua conserva Echoes/Little Feet.

Para probar Hierophant Green de forma determinista en un mundo de prueba con
comandos, consigue su disco. Debes estar sin Stand y tener al menos 3 de voluntad
con la configuración predeterminada:

```mcfunction
/standtest clear
/standtest willpower 3
/give @s jojosmod:stand_disc[jojosmod:stand_id="jojosmod:hierophant_green"]
```

Equipa el disco con clic derecho al aire. El primer despertar suma +2 de voluntad
una sola vez; este atajo de prueba empieza en 3 y, por tanto, deja 5. El recorrido
natural desde voluntad inicial 1 pasa a 3 al despertar y a 4 al dominar la descarga.
El núcleo `jojosmod:hierophant_green_core` también está disponible para probar la
fabricación de su disco con un disco vacío.

| Tecla | Acción |
| --- | --- |
| R | Invocar/desinvocar Hierophant Green |
| Z | Emerald Splash: nueve esmeraldas en tres pulsos, preparación 0,3 s, enfriamiento 5 s |

X, C, V, B y J siguen siendo habilidades de Star Platinum. Emerald Splash respeta
la tecla configurada para el ataque principal del Stand si cambias los controles.

## Puntería, distancia y colisiones

- Prueba contra un husk o zombi a unos ocho bloques: debe perder salud y avanzar
  una vez el contador. El abanico tiene tres direcciones por pulso.
- Mantener o repetir Z durante la acción o el enfriamiento no debe duplicar la descarga.
- Dispara al aire: cada descarga debe lanzar como máximo nueve esmeraldas y
  desaparecer al recorrer 14 bloques o cumplir su vida máxima de 25 ticks.
- Repite con una pared entre ambos, también junto al brazo del Stand. No debe
  aparecer daño detrás de la pared ni un proyectil creado al otro lado de ella.
- Las esmeraldas son energía de la habilidad: no se consumen ni aparecen en el
  inventario, ni quedan como objetos recogibles tras chocar.
- El máximo teórico es nueve puntos antes de armadura si todas aciertan. Armadura,
  dispersión y guardia frontal pueden reducir ese resultado.

Valora especialmente la sensación de apuntar, el tamaño del abanico, cuánto
tapa la vista, la coincidencia entre manos/pulsos y si la descarga se distingue
claramente del combate cercano de Star Platinum.

## Interrupciones y dominio

1. Empieza Z y desinvoca o recibe daño de salud antes del primer pulso. La
   descarga y sus proyectiles deben cancelarse, conservando el enfriamiento.
2. Repite tras lanzar uno o dos pulsos. Las esmeraldas de esa descarga deben
   desaparecer y no deben salir pulsos posteriores.
3. Una recuperación normal deja terminar los proyectiles ya lanzados.
4. Daña la salud de tres hostiles distintos en supervivencia. Cada uno cuenta
   una vez; repetir el mismo, atacar pasivos/jugadores/aliados o absorber todo
   el daño no debe dar progreso.
5. Comprueba +1 de voluntad y la bonificación: desde la siguiente activación,
   el enfriamiento pasa de 5 a 4 s. El que ya esté corriendo conserva su duración.
6. Guarda/reentra, muere y recupera tu Stand. El despertar, la voluntad y el
   contador deben conservarse sin premios repetidos. Consulta `/stand progress`.

## Con amigos

- Usad Star Platinum y Hierophant Green juntos contra los mismos enemigos. Cada
  jugador debe avanzar por sus propias acciones, sin necesitar el golpe final.
- Comprueba que las esmeraldas no dañen a un compañero de equipo o un jugador
  protegido por PvP. Deben poder pasar junto a aliados hacia un enemigo válido.
- Con PvP permitido y jugadores rivales, prueba la guardia frontal de Star
  Platinum: debe reducir los impactos; disparar desde atrás evita esa guardia.
- Al morir, desconectar o cambiar de dimensión, los proyectiles anteriores no
  deben seguir activos ni dañando a otros jugadores.

El panel tiene una presentación jade propia. Revisa su legibilidad con tu escala
de interfaz y la presencia del modelo en primera/tercera persona. La escucha, el
ritmo, el balance y una partida real con dos clientes necesitan tu valoración.

## Comprobaciones realizadas

La compilación y el empaquetado se comprobaron con Java 25. Los 93 GameTests
anteriores pasaron en la ejecución general; los 13 casos nuevos de Hierophant
Green pasaron en una ejecución enfocada tras corregir el avance manual del reloj
de una fixture de prueba. Cubren vuelo, paredes, cancelaciones, equipos/PvP,
progresión individual, guardado, discos y defensa de Star Platinum.

También se abrió un cliente real: R invoca el modelo con su textura y panel, y Z
genera nueve proyectiles en los ticks 6, 8 y 10, daña al objetivo y recupera la
acción. Se revisaron las vistas frontal y posterior. Esto no sustituye escuchar
los efectos en tu equipo ni jugar con dos clientes reales.
