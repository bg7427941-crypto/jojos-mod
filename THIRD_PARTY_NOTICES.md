# Autoría y dependencias

## Recursos originales de la entrega 0.9.1

La geometría, el rig, las UV, el atlas, las animaciones y los catorce efectos sonoros
actuales de Star Platinum se crean desde fuentes propias para este proyecto.
Los ocho sprites de ítems y los efectos de partículas también son desarrollo propio.
El código y estos recursos se distribuyen bajo la licencia MIT del proyecto.

La base del modelo 0.9.1 utiliza las proporciones del jugador clásico de Minecraft,
con detalles y textura propios para Star Platinum. Roundabout se consulta como
referencia de adaptación visual; no se importa su malla ni su atlas. La base se
construye localmente a partir de esas medidas, sin incorporar una skin de Minecraft.

Star Platinum y JoJo's Bizarre Adventure son creación de Hirohiko Araki. La
representación se inspira en Stardust Crusaders; no se reclama la creación del
personaje ni una afiliación oficial. La adaptación animada es de David Production.

Las referencias de apariencia y las comparaciones con otros mods están en
[docs/art-direction.md](docs/art-direction.md). No se incorporan sus imágenes,
geometrías, texturas, keyframes ni grabaciones a los recursos actuales.

Fuentes y manifiestos originales:

- [Modelo y textura](art/star_platinum/README.md).
- [Sonido sintetizado](art/sounds/original-audio.md).
- `META-INF/jojosmod/star-platinum-original.json` y `sound-original.json` en el JAR.

## Dependencias técnicas

La base adapta el MDK oficial de NeoForge; su aviso MIT se conserva en
[LICENSE-MDK.txt](LICENSE-MDK.txt). NeoForge mantiene su licencia LGPL-2.1 y
Minecraft sus condiciones propias. GeckoLib, con licencia MIT, se utiliza para
cargar y animar los modelos. Las dependencias mantienen sus licencias originales.

## Recursos de versiones anteriores

0.5.1–0.6.0 usaron un modelo/atlas adaptados de Roundabout y muestras de Kenney.
La rama local de pruebas 0.6.1 añadió grabaciones de JoJo recopiladas en Roundabout.
Esos recursos no se presentan como originales ni se relicencian retroactivamente.
Su procedencia y avisos históricos se conservan en
[docs/history/asset-provenance.md](docs/history/asset-provenance.md) y en el historial Git.
El JAR 0.9.1 utiliza el conjunto descrito arriba.
