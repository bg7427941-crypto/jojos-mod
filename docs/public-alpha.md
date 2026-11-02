# Preparar y publicar una alpha

**Decisión:** el recorrido de Parte 3 es suficiente para una **alpha pública**.
No necesita otras partes, todos los poderes situacionales ni balance final.
0.14.1-alpha.1 prepara distribución, metadatos, documentación y verificación.
Esto no publica una ficha automáticamente; cada plataforma revisa sus normas.

| Requisito | Estado |
| --- | --- |
| Recorrido de supervivencia y cooperativo | Implementado en 0.14.0 |
| Persistencia y recompensas únicas | 168 GameTests y dos clientes automatizados por LAN |
| Separar grabaciones con permisos no verificados | JAR público con allowlist y hashes de 43 efectos propios |
| Créditos de modelos adaptados | Incluidos en JAR y ZIP |
| Instalación, protocolo y limitaciones | Documentación y ficha actualizadas |
| Reportes reproducibles | Formulario de GitHub Issues |
| Artefacto reproducible | `publicAlphaDist`, verificador y checksum |
| Validación continua | Workflow de compilación, GameTests y paquete público |
| Equilibrio, diversión y escucha | Evaluación humana durante la alpha |

## Construcción

Con JDK 25, Python 3.11+ y conexión para dependencias:

```sh
./gradlew build runGameTestServer publicAlphaDist
python3 -m unittest discover -s tools/release -p 'test_*.py'
```

En Windows usa `gradlew.bat` y selecciona Python si hace falta mediante
`-PpythonExecutable=python` o `-PpythonExecutable=py`.
El ZIP de `build/distributions/` contiene JAR público, instalación, límites,
notas, licencias, evidencia de verificación y `SHA256SUMS.txt`. Comprueba el JAR
extraído con `sha256sum -c SHA256SUMS.txt` o `Get-FileHash` en PowerShell.

`build` y `runClient` conservan el audio interno de pruebas. La tarea pública
elimina todos los sonidos del JAR base e incorpora solo los identificados como
originales en cuatro manifiestos, comprobando sus hashes. Añadir un evento con
audio no aprobado hace fallar la tarea hasta asignarle un efecto original.
No distribuye dependencias, mundos, credenciales ni clases de pruebas.

## Publicación manual

1. Comprueba instalación/volumen en tu PC y una partida con amigos en un mundo
   de prueba. Registra problemas de equilibrio como incidencias de la alpha.
2. En GitHub Releases, crea **`v0.14.1-alpha.1`**, elige el commit validado de `main`
   y marca **pre-release**. Adjunta el ZIP público o el JAR público con checksum.
   Usa `distribution/RELEASE_NOTES.md` como descripción.
3. En Modrinth/CurseForge selecciona **Alpha**, **NeoForge**, Minecraft **26.3**,
   cliente **y** servidor requeridos y **GeckoLib** obligatorio. Sube únicamente
   el JAR público; el ZIP documental sirve para GitHub o descarga manual.
4. Declara licencia mixta: código/recursos originales MIT y modelos adaptados bajo
   Roundabout v1.2. Enlaza los avisos; no marques todo como MIT. Describe el alcance
   de Parte 3 y el carácter de proyecto de fans.
5. Enlaza límites, instalación y GitHub Issues. No anuncies Stands en prototipo
   de otras partes como completos.

El workflow valida y guarda artefactos; no publica versiones automáticamente.
Las grabaciones internas se conservan y no se copian al paquete público. El
constructor de recursos personales sigue en `extras/anime-audio-pack/`; no
comparte ni descarga grabaciones automáticamente.

## Evidencia de esta preparación

- 168 GameTests nativos aprobados con la versión alpha y siete pruebas del
  verificador público, incluidas seis alteraciones que debe rechazar.
- JAR instalado en un servidor NeoForge dedicado limpio, con GeckoLib separado;
  arranque, conexión de cliente, guía, equipamiento e invocación comprobados.
- Cliente cargando el JAR público desde `mods/`, sin clases/recursos de desarrollo:
  diez modelos y diez conjuntos de animaciones cargados, R/Z/X/V/G ejercitados,
  shader temporal visible y 52 eventos sonoros aceptados sin archivos ausentes.
- JAR y ZIP idénticos byte por byte al repetir la construcción. El checksum de
  `SHA256SUMS.txt` corresponde al JAR extraído del ZIP.
- Los 68 archivos de audio del entorno interno permanecen byte por byte.

El cliente usa el lanzador de desarrollo para iniciar Minecraft, pero descubre
el mod empaquetado desde `mods/`. El servidor usa la instalación de producción.
La pantalla virtual requiere configuración local de QA; no se incluye en el mod.
El motor de audio no tiene salida física: estas pruebas verifican carga y
reproducción, y no sustituyen la escucha ni el balance humano.
