# Fase 4: núcleos, voluntad y discos

La [visión del mod](product-vision.md) incorpora la meta del usuario de una experiencia
completa, fiel a JoJo, con recursos visuales terminados, progresión divertida y juego
con amigos. El usuario eligió **Parte 3: Stardust Crusaders** para la primera versión;
el [plan de Parte 3](part-three-release.md) prioriza Star Platinum. El contenido
específico de Whitesnake se retoma en una expansión posterior de Stone Ocean.
Las decisiones aprobadas de este documento se conservan como diseño del mod completo.

El usuario aprobó el coste del disco vacío y el balance inicial de extracción hostil.
La **Fase 4A (0.4.0)** implementa el loot de fragmentos y la fabricación del disco vacío.
La **Fase 4B (0.4.1)** añade núcleos de Echoes ACT 1 y Little Feet, fabricación de
discos identificados y equipamiento con voluntad persistente. Los valores de voluntad
son provisionales; sus hitos y la extracción todavía no están programados.
La **Fase 4C — prototipo de servicio (0.4.2)** implementa el guardián persistente y
el almacenamiento voluntario. Su aparición natural, localización, combate y
recompensa de núcleo siguen pendientes.

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
es `jojosmod:empty_stand_disc`; `jojosmod:stand_disc` contiene la identidad mediante
el componente nativo `jojosmod:stand_id` desde Fase 4B. Los discos antiguos sin identidad
se conservan y no equipan ningún Stand. La receta del disco vacío se descubre al obtener
un fragmento; las recetas con núcleo, al obtener el núcleo correspondiente.

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
  La base provisional de Fase 4B empieza en 1, exige 1 para los dos Stands básicos y
  permite ajustar el requisito con configuración sincronizada del servidor. No consume
  voluntad al equipar ni modifica las reglas de la flecha. El comando de operador
  `/standtest willpower [valor]` permite probar el umbral; aún no otorga progreso natural.
- Condiciones, coste y presentación del servicio del jefe: intercambio o misión.
  El prototipo de Fase 4C usa únicamente el disco vacío como coste. El jugador debe
  agacharse e interactuar dentro de tres bloques y con visión directa; una interacción
  sin agacharse muestra antes un aviso. El disco se transforma en la misma mano, incluso
  en creativo, y se conserva la voluntad. No se exige espacio libre en el inventario.
- Cómo localizar al jefe y cuándo reaparece. Se propone que el servicio siga siendo
  accesible para otros jugadores después de una derrota.
  El NPC actual se invoca con `/summon jojosmod:disc_keeper`, persiste entre guardados
  y sirve a todos los jugadores. Es inmóvil y no recibe daño: aún no hay derrota ni loot.
- Otros núcleos, Stands disponibles y sus jefes asociados.
- Detalles del combate del jefe, de las habilidades de Whitesnake y de sus recursos visuales.

## Entregas propuestas

1. **Implementado en Fase 4A:** fragmentos en Ancient Cities y fabricación del disco vacío.
2. **Base implementada en Fase 4B:** discos con identidad, dos núcleos de prueba,
   voluntad persistente y equipamiento. Falta concretar los hitos y el balance final.
3. **Almacenamiento implementado en el prototipo de Fase 4C:** guardián persistente,
   consentimiento y transformación segura del disco. Faltan la aparición natural,
   localización y combate del jefe, y su recompensa de núcleo.
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
servidor. La transferencia voluntaria tiene pruebas de inventario, repetición,
aislamiento entre jugadores y guardado/carga. La extracción hostil todavía necesita
pruebas de concurrencia, interrupciones, enfriamientos y multijugador real.
