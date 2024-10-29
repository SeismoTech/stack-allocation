# Stack allocation y otros monstruos

Casi todas las JVM actuales tienen alguna forma de *stack allocation*,
particularmente una variante llamada *scalar replacement*.
Esta optimización se suele presentar como una forma de mejorar el rendimiento
de nuestro programa, sobre todo al reducir la cantidad de basura generada.
Pero aprovechada adecuadamente sirve para mucho más:
para construir programas mejores
(mejor diseñados, más fáciles de testear, más adaptables, ...)
sin afectar (casi) al rendimiento.
Tras una explicación de los fundamentos del stack allocation, 
pasaremos a ver algunos ejemplos reales como guía de la presentación.
Y eso nos llevará a hablar de otras optimizaciones, de cómo se conjugan,
de dónde están sus límites y de algún truco para saltar parte de esos límites.
