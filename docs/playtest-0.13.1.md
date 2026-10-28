# Prueba visual 0.13.1 — ítems

Usa `jojosmod-0.13.1.jar` en Minecraft 26.3 con NeoForge 26.3.0.95 y GeckoLib 5.5.7.
El protocolo sigue siendo **12**, igual que 0.13.0. Para ver los sprites nuevos,
actualiza el cliente. Un servidor dedicado puede mantener 0.13.0.

1. Abre la pestaña creativa **JoJo's Mod**. Los 14 ítems deben tener iconos propios,
   sin cuadrados de fondo, texturas moradas de error ni fragmentos de otro icono.
2. Compara ambos tipos de flecha y los discos vacío/lleno. El agujero del disco
   plateado debe dejar ver el fondo; el disco violeta tiene una gema jade.
3. Coloca los nueve núcleos en inventario/barra rápida. Prueba distintas escalas
   de interfaz: identifica máscara de Hierophant Green, yelmo de Chariot, pájaro
   rojo, lianas moradas, rostro de arena, casco dorado y los otros tres núcleos.
4. Sostén flechas, discos y núcleos en primera y tercera persona. Comprueba borde,
   tamaño y reverso. Tira un objeto con Q: debe conservar la misma silueta en suelo.
5. Prueba tus paquetes de recursos habituales; uno que sustituya estas rutas
   puede mostrar sus propias texturas en vez de los sprites del mod.

También puedes obtener un ítem concreto con `/give @s jojosmod:hierophant_green_core`,
`/give @s jojosmod:stand_arrow` o los otros identificadores de la
[tabla de sprites](../art/items/README.md). Las recetas, componentes de discos,
asignación de Stand, daño, animaciones y audio conservan el comportamiento de 0.13.0.
