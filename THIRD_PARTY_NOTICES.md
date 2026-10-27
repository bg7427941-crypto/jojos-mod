# Autoría y dependencias

## Star Platinum — adaptación de Roundabout desde 0.9.2

La geometría, las UV y el atlas del anime de Star Platinum se adaptan de
[Roundabout](https://github.com/Hydraheads/RoundaboutMod), por **Hydraheads y
colaboradores**, con los créditos históricos a GenericName/GenericName72.
Se conserva la [licencia Roundabout v1.2](art/star_platinum/upstream/LICENSE-Roundabout.txt),
también incluida en la raíz del JAR como `LICENSE-Roundabout.txt`.

La licencia permite copiar, modificar y compartir, con crédito al autor, sin
reclamar la propiedad del mod original y respetando sus solicitudes sobre
distribución. Los recursos adaptados mantienen esas condiciones. No se presentan
como geometría, UV o píxeles originales ni se relicencian bajo la MIT del proyecto.

La revisión de origen es `f2390f1240e9b49444d6013e79bfda2038a542fb`.
El [manifiesto](art/star_platinum/asset-provenance.json) conserva URLs, hashes y
modificaciones: rotaciones de reposo aplanadas, mapeo al rig propio, divisiones en
muñeca/tobillo y dos dedos extensibles. Se omiten los keyframes y duplicados de
ráfaga de origen. El atlas anime 128×128 se copia byte por byte.

La adaptación técnica y las trece animaciones son desarrollo de JoJo's Mod y
tienen licencia MIT. Los catorce efectos sonoros sintetizados, ocho sprites de
ítems, partículas, HUD, habilidades y progresión siguen siendo del proyecto.
Los ocho clips de prueba añadidos manualmente al repositorio tras la primera
entrega 0.9.3 tienen la procedencia separada que se detalla a continuación.

Star Platinum y JoJo's Bizarre Adventure son creación de Hirohiko Araki. La
representación se inspira en Stardust Crusaders; no se reclama la creación del
personaje ni una afiliación oficial. La adaptación animada es de David Production.

Fuentes y manifiestos:

- [Modelo adaptado, atlas y animaciones](art/star_platinum/README.md).
- [Sonido sintetizado](art/sounds/original-audio.md).
- `META-INF/jojosmod/star-platinum-provenance.json` y `sound-original.json` en el JAR.

## Clips de audio de prueba añadidos manualmente

El commit `aba6b09` incorporó ocho grabaciones procesadas de la recopilación de
referencias de audio de Roundabout: invocación, ORA breve, inicio de ráfaga,
Star Finger, dos impactos, guardia y bloqueo. La versión actual conserva esos
archivos y su asignación de eventos sin modificarlos. Los clips no son síntesis
original ni se relicencian bajo MIT.

[El manifiesto de procedencia](art/sounds/collected-preview-provenance.json) conserva
los nombres de origen, hashes, enlaces a blobs concretos y los cortes y ajustes de
volumen. También se incluye como `META-INF/jojosmod/sound-collected-preview.json`
en el JAR. Los derechos de las grabaciones pertenecen a sus titulares; su edición
y los permisos de redistribución del audio del anime no se han verificado de
forma independiente. La licencia del código o del modelo no concede derechos
adicionales sobre esas grabaciones.

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
Las entregas 0.7.0–0.9.1 emplearon geometría y atlas propios; 0.9.2 utiliza la adaptación acreditada descrita arriba.
