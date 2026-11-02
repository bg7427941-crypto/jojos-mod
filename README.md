# JoJo's Mod — alpha de Stardust Crusaders

**0.14.1-alpha.1 · Minecraft 26.3 · NeoForge 26.3.0.95 · GeckoLib 5.5.7 · Java 25**

Una alpha de supervivencia y multijugador inspirada en la Parte 3 de JoJo:
Star Platinum, Hierophant Green, Silver Chariot, Magician’s Red, Hermit Purple,
The Fool y The World. Incluye habilidades, modelos animados, voluntad y dominio
persistentes, guardianes en templos del desierto y un combate cooperativo contra
DIO con recompensas personales. Es un proyecto de fans sin afiliación oficial.

## Instalar y jugar

Usa el **JAR terminado en `-public-alpha.jar`** del paquete público. Instala
NeoForge y GeckoLib en cliente y servidor; todos deben usar la misma versión del
mod. **Protocolo 13**. Las dependencias se descargan por separado.

La [guía de instalación](distribution/INSTALL.md) explica perfiles, servidores,
actualizaciones y solución de problemas. Empieza con **`/stand journey`**;
**`/stand progress`** explica tu Stand y **`/stand claim`** reclama recompensas
reservadas. Busca flechas Stardust en cofres de templos del desierto nuevos.

| Tecla | Acción |
| --- | --- |
| R | Invocar o desinvocar |
| Z / X / C | Técnica principal / secundaria / utilidad |
| V | Técnica avanzada; Star Finger para Star Platinum |
| B / J | Intercepción / salto de Star Platinum |
| G | Parada temporal para usuarios compatibles |

Las teclas se pueden cambiar en Controles. La [guía jugable de 0.14.0](docs/playtest-0.14.0.md)
describe la campaña y reglas de combate que conserva esta alpha.

## Alcance y estado

La alpha ofrece el recorrido principal de Parte 3; el equilibrio, animaciones y
compatibilidad siguen en desarrollo. Las peleas activas con DIO se reinician al
descargar sus chunks o cerrar el servidor; los hitos y recompensas se guardan.
Echoes ACT 1 y Little Feet conservan sus prototipos y datos anteriores. Whitesnake
y otras partes son futuras expansiones. Consulta [limitaciones](distribution/KNOWN_ISSUES.md),
[cambios](distribution/RELEASE_NOTES.md) y la [auditoría](docs/implementation-audit.md).

El paquete público usa **43 efectos originales sintetizados**, incluyendo señales
de ataque en los espacios de voz; no incorpora grabaciones del anime. La versión
interna de pruebas conserva sus clips por separado. Los modelos adaptados de
Roundabout mantienen su autoría y condiciones; los originales y el código son MIT.
[Créditos públicos](distribution/PUBLIC_NOTICES.md) · [Avisos del repositorio](THIRD_PARTY_NOTICES.md).

## Construir y verificar

Con JDK 25 y Python 3.11 o posterior, desde la raíz:

```sh
./gradlew build runGameTestServer publicAlphaDist
python3 -m unittest discover -s tools/release -p 'test_*.py'
```

En Windows usa `gradlew.bat`. El proceso Gradle necesita Python en PATH; puedes
seleccionarlo con `-PpythonExecutable=python` o `-PpythonExecutable=py`.

El ZIP verificado está en `build/distributions/`; contiene JAR público, guía,
limitaciones, avisos y SHA-256. El JAR normal de `build/libs/` corresponde al audio
interno de pruebas; `runClient` conserva ese entorno. Distribuye el **público**.
El verificador revisa audio por hash, referencias de sonido, subtítulos,
versión/protocolo, dependencias, licencias y clases incluidas.

[Publicar una alpha](docs/public-alpha.md) · [Reportar un error](https://github.com/bg7427941-crypto/jojos-mod/issues/new/choose)
· [Contribuir](CONTRIBUTING.md) · [Historial de entregas](HISTORY.md).
