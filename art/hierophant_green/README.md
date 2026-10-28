# Hierophant Green — fuentes originales

Modelo humanoide con proporciones de jugador, 79 cubos y 14 huesos. El atlas
512×512 dibuja la trama clara sobre verde jade, máscara, placas y cristal central.
Las seis animaciones son reposo, movimiento, invocación, desinvocación, recuperación
y Emerald Splash; sus tres pulsos visuales coinciden con 0,3, 0,4 y 0,5 segundos.

`hierophant_green.bbmodel` se abre en Blockbench con el formato GeckoLib. Incluye
la textura y las seis curvas editables. Los JSON de ejecución se exportan a
`src/main/resources/assets/jojosmod/geckolib/`. La textura de entidad es original;
el icono de núcleo se mantiene en [`art/items`](../items/README.md), y el generador
del modelo ya no lo sobrescribe. No se importan geometría, píxeles ni keyframes de Roundabout
o de Star Platinum. Se comparten únicamente los conversores técnicos MIT del proyecto.

```sh
python art/hierophant_green/build_assets.py
python art/hierophant_green/build_sounds.py
python art/hierophant_green/preview.py
python art/items/build_sprites.py
```

La generación visual requiere Pillow y NumPy para las vistas. El audio requiere
FFmpeg y FFprobe: cinco señales originales de cristal/energía, a partir de ruido
con semillas y osciladores, en OGG Vorbis mono/48 kHz. No hay grabaciones externas.
El generador usa flags de salida determinista; el manifiesto fija también la
versión de FFmpeg porque distintas versiones del codificador pueden cambiar los bytes.

[Reposo](preview.png) y [Emerald Splash](emerald-splash.png) muestran frente,
tres cuartos, lateral y espalda desde los recursos de ejecución reales. Estas
vistas apoyan la revisión y no sustituyen comprobar el render y las transiciones
dentro de Minecraft.

Los generadores y recursos originales son MIT. Hierophant Green y JoJo's Bizarre
Adventure pertenecen a Hirohiko Araki; la referencia de color es Stardust Crusaders.
[La ficha de diseño](../../docs/hierophant-green-design.md) distingue canon, balance
y habilidades todavía pendientes. Los dos manifiestos de procedencia se incluyen
también en el JAR.
