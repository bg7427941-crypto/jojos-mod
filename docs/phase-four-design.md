# Fase 4: núcleos, voluntad y discos

El usuario aprobó el coste del disco vacío y el balance inicial de extracción hostil.
La **Fase 4A (0.4.0)** implementa el loot de fragmentos y la fabricación del disco vacío.
Los núcleos, la voluntad, el jefe y la extracción todavía no están programados.

## Requisitos indicados por el usuario

- Los fragmentos de disco se obtienen en Ancient Cities, conservando su loot vanilla.
- Un núcleo o alma determina el Stand del disco fabricado. Puede ser recompensa de un jefe.
- La fuerza de voluntad del jugador limita qué Stands puede poseer.
- Un jefe raro ofrece un servicio para guardar el Stand del jugador en un disco vacío
  que este debe entregarle. Se conserva el Stand guardado, sin sustituirlo automáticamente.
- Retar y derrotar a ese jefe permite obtener el núcleo o alma relacionado con Whitesnake.
- Whitesnake puede robar el Stand de otro jugador sin su consentimiento mediante un disco vacío.
- El disco vacío debe ser difícil de obtener para limitar la frecuencia de los robos.

El diseño relaciona cada disco con su núcleo y el Stand que este identifica.
Las reglas aprobadas de la flecha de Fase 3 se mantienen.

## Obtención y fabricación — coste aprobado

Usar **núcleo de Stand** como nombre del material que identifica el Stand.

- Cofres principales de Ancient City: 25 % de probabilidad de añadir 1–2 fragmentos.
  Los cofres especiales de hielo conservan su tabla actual.
- Un disco vacío requiere **7 fragmentos de disco, 1 fragmento de eco y 1 lingote de
  netherita**. Cada fabricación produce un disco vacío, apilable de uno en uno.
- Receta de mesa de crafteo, con F = fragmento de disco, E = fragmento de eco y
  N = lingote de netherita:

  ```text
  F F F
  F E F
  F N F
  ```

- Un disco vacío + un núcleo específico producen un disco con el Stand de ese núcleo.
- Guardar o robar un Stand transforma un disco vacío en un disco lleno.
- Equipar un disco lleno requiere no tener otro Stand asignado y cumplir su requisito
  de voluntad. Al equiparlo, el disco se consume en supervivencia.

El loot, el disco vacío y su receta están implementados en Fase 4A. El ítem nuevo
es `jojosmod:empty_stand_disc`; el antiguo `jojosmod:stand_disc` sigue reservado para
el desarrollo de los discos con identidad. La receta se descubre al obtener un fragmento.
La fabricación con núcleo y el uso de discos llenos corresponden a entregas posteriores.

Los ingredientes vinculan el disco con Ancient Cities y con la obtención de netherita.
La rareza limita cuántos robos pueden financiarse; las condiciones de combate de abajo
permiten defenderse incluso frente a jugadores con muchos recursos.

## Extracción hostil con Whitesnake — balance inicial aprobado, pendiente de implementación

- Whitesnake debe estar invocado y el usuario debe sostener un disco vacío en la mano secundaria.
- Solo afecta a otro jugador vivo, conectado, con Stand asignado y en la misma dimensión.
- Respeta la configuración PvP del servidor y las reglas de combate entre equipos.
- La víctima debe tener como máximo el **25 % de su salud máxima** durante la canalización.
- Requiere mantenerse a **3 bloques o menos**, con línea de visión, durante **4 segundos**.
- Recibir daño, perder alcance o visión, atacar, desinvocar, cambiar el disco o la
  asignación, morir o desconectarse interrumpe la canalización.
- Al completarse: el disco vacío se transforma en un disco con el Stand de la víctima;
  la víctima pierde la asignación y su manifestación se desinvoca. El usuario conserva
  Whitesnake y no equipa automáticamente el Stand robado.
- Enfriamiento: **5 minutos tras completar una extracción** y **30 segundos tras una
  canalización interrumpida**. El enfriamiento se conserva al morir y reconectar.
- Una extracción fallida conserva el disco vacío y el Stand de la víctima.
- La víctima conserva su fuerza de voluntad. Puede recuperar su Stand usando el disco
  correspondiente o conseguir otro Stand mediante las vías normales.

El servidor debe validar y resolver la transferencia completa. Dos extracciones
simultáneas no pueden generar dos discos del mismo Stand. Ante inventario lleno,
la operación debe conservar una salida válida para el disco resultante; la mano
secundaria que contenía el disco vacío es el destino propuesto.

## Progresión y jefe — decisiones todavía abiertas

- Valores de voluntad, requisitos de cada Stand e hitos que la aumentan. Se propone
  progreso persistente, separado de la experiencia y con recompensas únicas por hito.
- Condiciones, coste y presentación del servicio del jefe: intercambio o misión.
- Cómo localizar al jefe y cuándo reaparece. Se propone que el servicio siga siendo
  accesible para otros jugadores después de una derrota.
- Otros núcleos, Stands disponibles y sus jefes asociados.
- Detalles del combate del jefe, de las habilidades de Whitesnake y de sus recursos visuales.

## Entregas propuestas

1. **Implementado en Fase 4A:** fragmentos en Ancient Cities y fabricación del disco vacío.
2. Discos con identidad, núcleos, voluntad y equipamiento; concretar primero la progresión.
3. Jefe localizable, almacenamiento voluntario y recompensa de núcleo.
4. Extracción hostil de Whitesnake y comprobación multijugador.

Cada entrega debe compilar, tener las pruebas pertinentes y publicarse en el repositorio.
Se usarán recursos visuales originales provisionales mientras no haya modelos finales.

## Reutilización y referencias

| Referencia | Licencia | Aplicación | Uso |
| --- | --- | --- | --- |
| NeoForge, dependencia actual | LGPL-2.1-only | Loot aditivo, attachments y comunicación con el servidor | Reutilizar las APIs existentes; `neoforge:add_table` ya está probado en Fase 3 |
| Crafteo y componentes de ítem de Minecraft | Código del juego sujeto a sus términos | Recetas y datos persistentes del Stand contenido en cada disco | APIs del juego; evitar un sistema de recetas o persistencia paralelo |
| Roundabout, referencia revisada durante la investigación inicial | Licencia propia | Concepto de discos de Stand | Inspiración conceptual; no incorporar código ni assets |

La base actual ya tiene asignación persistente, desinvocación y validación PvP en el
servidor. La transferencia a discos necesita pruebas específicas de inventario,
concurrencia, interrupción y persistencia antes de darse por terminada.
