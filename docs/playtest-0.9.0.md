# Prueba 0.9.0 — Salto con Stand

Requiere Minecraft 26.3, NeoForge 26.3.0.95 y GeckoLib 5.5.7.
Actualiza servidor y clientes al mod 0.9.0: el protocolo de red es 6.
Compila con `./gradlew build`; el archivo es `build/libs/jojosmod-0.9.0.jar`.

## Desbloqueo y uso

Con voluntad inicial 1, despertar Star Platinum concede +2. Completar cualquiera
de los desafíos de precisión, ráfaga o guardia añade 1: con **voluntad 4** se
desbloquea el **salto con Stand**, asignado a **J**. El panel y `/stand progress`
muestran la tecla configurada. Star Finger sigue requiriendo 5 y la intercepción 6.
Las partidas anteriores que ya tengan voluntad 4 reciben acceso automáticamente.

Invoca con R, pisa suelo firme, deja espacio sobre la cabeza y pulsa J.
El impulso es hacia arriba y hacia la dirección horizontal de tu mirada. La
inclinación de la cámara no cambia su fuerza. El cooldown es de **80 ticks, 4 s**
a 20 TPS. No consume voluntad. Puedes utilizar otras acciones después del impulso;
el salto necesita que estés libre de otra acción al iniciarlo.

La referencia es el uso de la fuerza del Stand para impulsarse durante los combates
de Parte 3. La tecla, los límites y el requisito de voluntad son balance del mod.

## Aterrizaje y límites

El primer aterrizaje del salto puede descontar **hasta cuatro bloques** de la
distancia que Minecraft usa para calcular daño por caída. Esto permite aterrizar
en un suelo de altura similar; bajar desde un precipicio todavía puede causar daño.
La protección se consume en ese aterrizaje y dura como máximo **60 ticks, 3 s**.

Desinvocar después de impulsarte conserva esa amortiguación limitada. Perder la
asignación de Star Platinum, cambiar de dimensión, morir, recibir daño de salud,
entrar en agua/lava, trepar, montar o activar vuelo/élitros termina la protección.
El cooldown se conserva. Los siguientes aterrizajes siguen el cálculo habitual.

Se necesita apoyo físico bajo los pies y espacio sobre la cabeza. No se inicia en
el aire, agua, lava, escaleras de mano/enredaderas, monturas, vuelo o élitros.
Un intento rechazado no aplica movimiento ni consume cooldown. El salto es un
impulso único; no permite encadenar impulsos en el aire.

## Prueba del jugador

1. Desde voluntad 3, completa un desafío y comprueba el aviso y el panel al llegar a 4.
   Para revisar solo la función, un operador puede usar `/standtest willpower 4`.
2. Prueba J en una zona plana y despejada. Valora distancia, altura, control al caer,
   pose del Stand, tela, sonidos de salida y aterrizaje, y destellos.
3. Mira hacia arriba, al suelo y a los lados: la dirección horizontal cambia; la fuerza
   vertical debe mantenerse. Prueba saltos entre plataformas de altura similar.
4. Repite J en el aire y durante el cooldown. Prueba también bajo un techo bajo y en
   agua: los intentos rechazados deben dejarte en movimiento normal.
5. Desde una caída moderada y segura, comprueba que la amortiguación es limitada.
   Después realiza otra caída normal: no debe conservar la protección anterior.
6. Desinvoca en el aire y comprueba el primer aterrizaje. Repite recibiendo un golpe
   o perdiendo el Stand: la protección debe terminar.
7. Con un amigo, saltad en direcciones distintas y alternad ataques en el aire.
   Cada jugador debe conservar su propio impulso, cooldown y aterrizaje.

La altura/distancia dependen de colisiones y de la física de Minecraft. Las pruebas
automáticas acompañan la revisión; la sensación de movimiento y la latencia necesitan
valoración en una partida real.

## Comprobaciones realizadas

La compilación y los **84 GameTests** pasan. Diez casos cubren el salto, incluidos
la amortiguación de una sola caída, el cooldown por propietario y una regresión
del aterrizaje especulativo del servidor antes de recibir el movimiento real.

En el cliente de desarrollo, J desde suelo plano alcanzó unos 4,4 bloques de altura
y 5,4 de desplazamiento, conservando 20/20 de salud. Saltar desde una plataforma
diez bloques más alta que el suelo de llegada dejó 13/20. El panel mostró voluntad
4 y el desbloqueo de J; no aparecieron avisos de recursos ausentes ni rechazos
de movimiento del servidor.

La comprobación con dos clientes no se ha repetido para esta versión. La salida
de audio en nube es virtual: volumen, timbre, fluidez y balance necesitan tu revisión.
