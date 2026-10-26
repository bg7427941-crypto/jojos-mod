# Sonido original — 0.9.0

Los catorce efectos actuales de Star Platinum se crean desde cero por síntesis y DSP.
No se utilizan grabaciones de otros mods, voces de actores ni fragmentos del anime.
La [ficha de audio original](original-audio.md) describe cada efecto, sus fuentes
matemáticas, duraciones y verificación. El código y los sonidos están bajo MIT.

```sh
python art/sounds/build_original.py
python art/sounds/build_original.py --check
```

Los eventos del juego apuntan a `sounds/star_platinum/original/`. Invocación,
inicio de ráfaga y Star Finger utilizan un canal independiente por propietario:
un evento nuevo reemplaza el anterior de ese jugador y las cancelaciones lo
interrumpen. Las frases o efectos de un amigo conservan su propio canal.

El [paquete de audio personalizable](../../docs/anime-audio.md) permite probar
otras muestras en el cliente. El mod no necesita ese paquete para sonar.

Las muestras Kenney usadas en 0.5.1–0.6.0 se retiraron de los recursos actuales.
La mezcla local de grabaciones de JoJo de 0.6.1 se conserva en su rama de pruebas;
no forma parte de esta entrega. La procedencia histórica está en
[historial de recursos](../../docs/history/asset-provenance.md).
