# Grabaciones para ataques — 0.13.0

Integración de grabaciones solicitada explícitamente por el propietario del
proyecto. `sources.json` conserva el repositorio, revisión fija, archivo de origen,
Git blob, SHA-256 y duración. `upstream/` conserva los archivos preferidos para
editar; `build_audio.py` reproduce los diecisiete OGG de ejecución y escribe los
recortes, ganancias y hashes en `provenance.json`.

```sh
python art/anime_audio/build_audio.py
```

No se altera la velocidad ni el tono de las voces. Los efectos por pulso se
recortan a colas breves para evitar acumular frases. Las voces se cancelan por
propietario y las colas de recuperación normales pueden terminar.

Fuentes:

- [Roundabout por Hydraheads y colaboradores](https://github.com/Hydraheads/RoundaboutMod/tree/f2390f1240e9b49444d6013e79bfda2038a542fb): ORA, MUDA, Chariot, fuego y tiempo. Se conserva su licencia v1.2 y crédito.
- [Ripples of the Past por StandoByte y colaboradores](https://github.com/StandoByte/Ripples-of-the-Past/tree/92d6e4eb2e2bed82e74cba3aa63a3ca96b2979bc): llamadas de Kakyoin/Avdol y salida de esmeraldas. Los cuatro recursos independientes y sus ediciones conservan GPL-3.0; se incluye el texto de la licencia y sus originales.
- [JoJoStands-Sounds por AnotherGuy7 y colaboradores](https://github.com/AnotherGuy7/JoJoStands-Sounds/tree/592896c3da195070d231f55e30b381982c962662): llamada de Joseph para Hermit Purple. El repositorio no declara una licencia para la grabación; se registra su procedencia, sin atribuirle MIT.

Las grabaciones subyacentes conservan los derechos de sus respectivos titulares;
la licencia del código de un repositorio no establece derechos adicionales sobre
ellas. No se importó código ni animaciones de estos proyectos para este audio.
The Fool usa impactos de anime que ya estaban añadidos al proyecto, junto con
la arena original. No se le asignó una voz de otro personaje.
