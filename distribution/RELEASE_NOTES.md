# 0.14.1-alpha.1 — primera alpha pública preparada

La campaña de Stardust Crusaders introducida en 0.14.0 permite despertar un
Stand, entrenar voluntad/dominio, encontrar un guardián natural, desafiar a DIO
con amigos y obtener recompensas personales. Esta entrega prepara ese recorrido
como descarga pública experimental.

## Incluido

- Siete Stands de Parte 3, modelos animados, sprites y técnicas avanzadas:
  tentáculos/barrera, estoque, detector de vida, Hamon, arena y cuchillos.
- DIO con fases, The World, avisos de ataques y parada temporal.
- Guardianes, avances, `/stand journey`, `/stand progress` y `/stand claim`;
  recompensas por participación y recuperación con inventario lleno.
- Paquete público separado con 43 efectos sintetizados originales. Los espacios
  de voz usan señales de acción; no contiene grabaciones recopiladas del anime.
- Descripción actual, dependencias, instalación, limitaciones, créditos,
  reporte de errores, checksum y verificación automática de distribución.

## Compatibilidad y validación

Minecraft **26.3**, NeoForge **26.3.0.95**, GeckoLib **5.5.7**, Java **25**.
Cliente y servidor: **0.14.1-alpha.1, protocolo 13**. Conserva datos de 0.14.0.
Consulta `KNOWN_ISSUES.md` antes de usar un mundo importante.

La campaña cuenta con 168 GameTests nativos y una prueba de dos clientes reales
automatizados por LAN con reconexión y recompensa única. La preparación pública
añade verificación del JAR y rechazo de audio desconocido/alterado, clases de QA,
créditos ausentes, mapa de sonidos incorrecto y protocolo obsoleto. La evaluación
humana de equilibrio y la escucha siguen siendo tareas de playtest.

El JAR público se instala y conecta a un servidor NeoForge dedicado limpio.
Se comprobaron invocación, controles, shader temporal y los 52 eventos sonoros en
cliente real; el lanzador del cliente es de desarrollo y carga el mod desde JAR.
La construcción repetida produjo JAR y ZIP idénticos byte por byte.
