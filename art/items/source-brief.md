# Dirección del atlas

Herramienta: OpenAI image generation, con fondo transparente. Se generó un atlas
nuevo y después se corrigieron los márgenes y el agujero del disco vacío mediante
la misma herramienta. `source-atlas.png` conserva el resultado final de 1254×1254.
El exportador usa las dimensiones reales, sin asumir la resolución solicitada.

La petición original especificó una cuadrícula de cuatro columnas y cuatro filas,
14 iconos de pixel art nítidos, contorno violeta oscuro, reflejo superior izquierdo,
metales y gemas facetados, fondo transparente, sin textos, paneles, sombras ni aura.
Se pidió conservar cada objeto completo dentro de su celda, con las dos últimas
celdas vacías. Se dieron los motivos, colores y rasgos de cada ítem de la tabla
de README.md, en el mismo orden por filas.

La corrección pidió conservar los catorce diseños, colores, sombreado y orden,
reducir cada objeto al 72 % de su celda y dejar un margen transparente del 14 %
a cada lado. Se solicitó un agujero realmente transparente en el disco plateado,
y conservar el cristal jade en el disco violeta. El resultado fue revisado antes
de extraer los iconos a su resolución de ejecución.

La imagen maestra es la fuente de reconstrucción. Las operaciones posteriores
son exportación de formato: recorte por celda, ajuste con vecino más próximo,
transparencia binaria y centrado, sin repintado procedural del diseño.
