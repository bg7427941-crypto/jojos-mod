# Modelos originales del recorrido — 0.14.0

DIO usa proporciones humanoides de jugador, cabello rubio esculpido, ropa dorada,
camiseta negra y los acentos verdes de Parte 3. El guardián neutral tiene túnica,
capucha y medallón propios: es un personaje de servicio original, sin atribuirle
la identidad de Pucci. No se usan geometría, texturas ni keyframes de otros mods.

Cada carpeta conserva el proyecto Blockbench/GeckoLib, atlas de 256×256 y vistas
frontal, tres cuartos, lateral y trasera. Las animaciones de juego incluyen reposo,
marcha, ataque, cuchillos y preparación temporal. The World del jefe utiliza la
adaptación ya acreditada en `art/part_three`; no se presenta como recurso original.

```sh
python art/journey/build_assets.py
python art/journey/preview.py
```

El generador construye lienzos de atlas y cubos nuevos, reutilizando únicamente
los conversores MIT del proyecto. Requiere Pillow; las vistas usan NumPy. Los hashes
de recursos reales se incluyen en `asset-provenance.json` y en el JAR como
`META-INF/jojosmod/journey-provenance.json`. Código, geometría, atlas y keyframes
nuevos se distribuyen bajo MIT. DIO y JoJo pertenecen a Hirohiko Araki y sus titulares.
