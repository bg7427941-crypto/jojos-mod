# Star Platinum — prueba jugable 0.5.0

Descarga [jojosmod-0.5.0.jar](https://github.com/bg7427941-crypto/jojos-mod/raw/refs/heads/playtest/0.5.0/jojosmod-0.5.0.jar) y colócalo en `mods/`, sustituyendo el JAR anterior de JoJo's Mod.

Necesitas Minecraft **26.3**, NeoForge **26.3.0.95** y GeckoLib **5.5.7**. Todos los clientes y el servidor deben usar las mismas versiones. No se incluye GeckoLib dentro de este archivo.

En un mundo de prueba con comandos y un personaje sin Stand:

```mcfunction
/give @s jojosmod:stardust_arrow
```

Usa la flecha apuntando al aire. **R** invoca; **Z** golpe preciso; **X** ráfaga; **C** activa/cancela guardia frontal. Las teclas se pueden reasignar.

Revisa el modelo en primera/tercera persona y el ritmo de las acciones. Si puedes, observa los Stands con un amigo. La [guía de 10 minutos](https://github.com/bg7427941-crypto/jojos-mod/blob/447d378983e003415477c433557c1751d2219a9e/docs/playtest-0.5.0.md) detalla las comprobaciones y el feedback que buscamos.

Incluye modelo original y ocho animaciones. Compilación y **53 GameTests** pasados; se verificaron golpe, ráfaga y una sesión LAN con dos clientes de desarrollo, incluido el modelo y la guardia desde el observador. Servidor dedicado, muerte/reconexión compartida y balance de grupo siguen pendientes de prueba manual.

Esta es una entrega para valorar jugabilidad y arte. La parada temporal, otros Crusaders, DIO y el recorrido completo de Parte 3 siguen en desarrollo.

Código fuente: [447d378983e003415477c433557c1751d2219a9e](https://github.com/bg7427941-crypto/jojos-mod/commit/447d378983e003415477c433557c1751d2219a9e).
Árbol verificado: `8de38035b45ae6c23841a056c754274ae301b0cd`.
SHA-256 del JAR: `35ef4f0d2770f13c173e2e025f560b314bffb5c94903aa02af445f7dd195a115`.

El JAR incluye las licencias MIT del proyecto y del MDK. Los recursos visuales editables están en el código fuente.
