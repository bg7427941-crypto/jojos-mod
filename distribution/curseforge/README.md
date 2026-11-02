# Preparar la ficha de CurseForge

Este directorio contiene textos para copiar, un icono original de 400×400 y una
captura real. No se ha creado ni enviado un proyecto desde tu cuenta. El nombre
**Stardust Resolve** es una propuesta para la ficha: comprueba disponibilidad en
el formulario y cámbialo si prefieres otro. El nombre interno del mod no se cambia.

## Crear el proyecto

Entra con tu cuenta en https://authors.curseforge.com/#/projects/create/choose-game
(o abre tu proyecto existente). Selecciona Minecraft → Mods.

- Nombre propuesto: **Stardust Resolve**; sin número de versión en el nombre.
- Summary: copia `summary.txt`.
- Description: copia `description.md` en modo Markdown; contiene el inglés primero.
- Logo: sube **icon.png**, PNG original de 400×400. `icon.svg` es su fuente editable.
- Categoría sugerida: Magic; adicionales Adventure and RPG y Mobs si aparecen.
- License: selecciona **Custom** y pega `license.txt`. Conserva los créditos de
  modelos y audio; no declares todos los recursos como MIT.
- Source: https://github.com/bg7427941-crypto/jojos-mod
- Issues: https://github.com/bg7427941-crypto/jojos-mod/issues
- Puedes subir `gallery/inventory.png` con el título/caption de `gallery/README.md`.

La descripción evita enlaces externos de descarga de archivos. Source/Issues son
los enlaces de código y soporte; GeckoLib se añade mediante Relations.

## Subir el archivo

En el proyecto: Files → Upload File. **Sube el JAR del mod, no el ZIP de este kit.**

| Campo | Valor |
| --- | --- |
| Archivo elegido por el propietario | jojosmod-0.14.1-alpha.1-anime-alpha.jar |
| Display name | Stardust Resolve 0.14.1-alpha.1 — Anime Audio |
| Release type | Alpha |
| Minecraft | 26.3, únicamente |
| Mod loader | NeoForge, únicamente |
| Java, si el formulario lo ofrece | 25 |
| Environment, si aparece | Client & Server, requerido en ambos |
| Changelog | Copia changelog.md |
| Related projects / Relations | GeckoLib → Required Dependency |

GeckoLib es el proyecto **388172**, slug **geckolib**:
https://www.curseforge.com/minecraft/mc-mods/geckolib
Usa la versión 5.5.7 de NeoForge/Minecraft 26.3; NeoForge probado: 26.3.0.95.
Los valores exactos también están en `project-fields.json`; es una hoja de datos
para rellenar el formulario, no una petición de API lista para enviar.

El JAR está en `releases/0.14.1-alpha.1/` del repositorio. En un ZIP local de este
kit se incluye en la raíz. Compara el SHA-256 con `SHA256SUMS.txt` antes de subirlo.

## Audio y revisión

La política Content Copyright exige licencia o permiso para recursos de terceros.
Los permisos de las grabaciones subyacentes no se han verificado. Los créditos
no sustituyen esos permisos y esta preparación no certifica la aprobación.
Se conserva la variante con anime que elegiste. La alternativa de efectos propios
se genera con `./gradlew publicAlphaDist`; no modifica la partida. Si subes ambas,
usa archivos del mismo proyecto, con nombres de variante claros.

Un proyecto nuevo con **solo archivos Alpha** puede aparecer en la web y aún no
sincronizarse con la app hasta tener una Beta/Release aprobada. Mantén Alpha
mientras describa el estado real de esta entrega.

El envío final se hace desde tu sesión de CurseForge y queda sujeto a moderación.
No necesitas compartir contraseñas ni claves API para la subida manual.

## Fuentes oficiales consultadas

- Crear proyecto: https://support.curseforge.com/support/solutions/articles/9000197241-creating-and-submitting-a-project
- Presentación/icono/licencia: https://support.curseforge.com/support/solutions/articles/9000199552-project-submission-guide-and-tips
- Moderación, idioma, copyright y enlaces: https://support.curseforge.com/support/solutions/articles/9000197279-project-and-modpack-moderation-policies
- Archivos Alpha, relaciones y subida: https://support.curseforge.com/support/solutions/articles/9000197242
