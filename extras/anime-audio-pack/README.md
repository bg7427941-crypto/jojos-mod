# Plantilla de audio local

Construye un paquete de recursos para JoJo's Mod con archivos que puedas usar.
Esta carpeta no contiene grabaciones del anime y el constructor no descarga audio.

Con Python 3, desde la raíz del repositorio:

```sh
python extras/anime-audio-pack/build_pack.py --source-dir /ruta/mis-sonidos --output /ruta/JoJo-audio-local.zip
```

Solo se sustituyen los eventos cuyos clips aportes. El ZIP no se sobrescribe salvo
que añadas `--overwrite`. Consulta [nombres, instalación, conversión y fuentes](../../docs/anime-audio.md).
