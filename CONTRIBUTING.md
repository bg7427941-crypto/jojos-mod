# Contribuir y reportar errores

Usa el formulario de GitHub Issues con versión, cliente/LAN/servidor, pasos,
Stand, resultado esperado/real, otros mods y `logs/latest.log` o crash report.
Elimina datos personales del log antes de compartirlo.

Con JDK 25 y Python 3.11+:

```sh
./gradlew build runGameTestServer publicAlphaDist
python3 -m unittest discover -s tools/release -p 'test_*.py'
```

Conserva datos de partidas, reglas de PvP/equipos, autoridad del servidor y
cooldowns al interrumpir. Comprueba servidor dedicado al tocar código compartido.
Documenta procedencia, autoría y condiciones de recursos externos; el paquete
público exige audio original aprobado. Las fuentes de modelos están en `art/`.
