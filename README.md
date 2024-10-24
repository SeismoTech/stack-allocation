Información sobre Stack Allocation / Scalar Replacement (SR)
======================================================================

Prontuario
----------------------------------------------------------------------

- La charla en [MadridJUG](madridjug/main.pdf).

- [Algo de bibliografía](bib).

- [Código con experimentos](code).

El resto de este documento son notas con un grado diverso de eleboración.


Conceptos
----------------------------------------------------------------------

No confundir scalar-replacement con stack-allocation.
https://github.com/microsoft/openjdk-proposals/blob/main/stack_allocation/Stack_Allocation_JEP.md
  - This proposal is not a replacement to speculative scalar replacement based on speculative escape analysis
  - This proposal will not change how escape analysis works in C2, e.g. introduce context sensitive escape analysis. 

Depende de escape-analysis.
Se controla con la opción `-XX:+DoEscapeAnalysis`.
Activado por defecto.

Depende de inlining:
la mejor forma de que un valor no escape es 
que se haya hecho inlining de todos sus usos:
constructor,
llamadas a método propios como target,
llamadas a métodos ajenos como valor.

Anotación jdk.internal.vm.annotation.ForceInline
Paquete no exportado de java.base.
Además, [según pone en su documentación](
https://github.com/openjdk/jdk13/blob/dcd4014cd8a6f49a564cbb95387ad01a80a20bed/src/java.base/share/classes/jdk/internal/vm/annotation/ForceInline.java#L43),
sólo se atiende si la ha cargado el bootstraping loader.
Así que no hay forma de falsear un .class para añadirle esa anotación.


Premature optimization
----------------------------------------------------------------------

La famosa frase *premature optimization is the root of all evil*,
no está claro si se debe a Knuth o a Hoare, pero realmente dice:

> We should forget about small efficiencies, say about 97% of the time:
> premature optimization is the root of all evil. Yet we should not pass up our
> opportunities in that critical 3%

No está claro que son *small efficiencies* ni que es *premature*.
Al parecer, Knuth también dijo

> In established engineering disciplines a 12% improvement, easily obtained, is
> never considered marginal and I believe the same viewpoint should prevail in
> software engineering

https://en.wikipedia.org/wiki/Program_optimization

En [The Fallacy of Premature Optimization](
https://ubiquity.acm.org/article.cfm?id=1513451),
([pdf](bib/TheFallacyOfPrematureOptimization.pdf))
se atempera las posibles intrepretaciones de *premature optimization*.
Curiosamente cita el contexto que la precede, pero no el que la sucede,
que a mi parecer es mucho más contundente.

En la [introducción de *Rico Mariani*]( 
https://learn.microsoft.com/en-us/previous-versions/msp-n-p/ff649527(v=pandp.10)
a la guía *Improving .NET Application Performance and Scalability*,
dice *Never give up your performance accidentally*.

Rob Pike tiene las siguientes 
[5 reglas](https://users.ece.utexas.edu/~adnan/pike.html):

Rob Pike's 5 Rules of Programming

- Rule 1. You can't tell where a program is going to spend its time. Bottlenecks
  occur in surprising places, so don't try to second guess and put in a speed
  hack until you've proven that's where the bottleneck is. 

- Rule 2. Measure. Don't tune for speed until you've measured, and even then
  don't unless one part of the code overwhelms the rest. 

- Rule 3. Fancy algorithms are slow when n is small, and n is usually
  small. Fancy algorithms have big constants. Until you know that n is
  frequently going to be big, don't get fancy. (Even if n does get big, use Rule
  2 first.)

- Rule 4. Fancy algorithms are buggier than simple ones, and they're much harder
  to implement. Use simple algorithms as well as simple data structures.

- Rule 5. Data dominates. If you've chosen the right data structures and
  organized things well, the algorithms will almost always be self-evident. Data
  structures, not algorithms, are central to programming.

Pike's rules 1 and 2 restate Tony Hoare's famous maxim "Premature optimization
is the root of all evil." Ken Thompson rephrased Pike's rules 3 and 4 as "When
in doubt, use brute force.". Rules 3 and 4 are instances of the design
philosophy KISS. Rule 5 was previously stated by Fred Brooks in The Mythical
Man-Month. Rule 5 is often shortened to "write stupid code that uses smart
objects".


Sobre cómo mostrar las decisiones de inlining
----------------------------------------------------------------------

Con estas opciones que consigue muy buena información:
```
-XX:+PrintCompilation
-XX:+UnlockDiagnosticVMOptions
-XX:+PrintInlining
```
La opción `-XX:+PrintInlining` hay que habilitarla con 
`-XX:+UnlockDiagnosticVMOptions`.
Si se olvida poner `-XX:+PrintCompilation`, 
igualmente `-XX:+PrintInlining` escribirá sus decisiones de inlining
pero faltará la información de a qué método se refiere.

Con JITWatch también es posible ver esta info.


Sobre cómo mostrar el código generado por el JIT
----------------------------------------------------------------------

[JITWatch](https://github.com/AdoptOpenJDK/jitwatch)

- [How to Show the Assembly Code Generated by the JVM](
https://www.beyondjava.net/show-assembly-code-generated-jvm)


Ejemplos
----------------------------------------------------------------------

### Hashing implementado como Kernel

Explicado en la presentación de [MadridJUG](madridjug/main.pdf).

### Recodificación de datos

Explicado en la presentación de [MadridJUG](madridjug/main.pdf).

### *Wide hierarchy* con *hard polymorphism*

Está trabajado como un ejemplo en [LaEspe](
https://github.com/SeismoTech/laespe/blob/main/src/jmh/java/org/seismotech/laespe/AutoWideHierarchyBenchmark.java)

### Parámetro discreto como enumeración o como análisis de casos

Un *parámetro discreto* se puede implementar
como una enumeración (o alternativas similares)
o un análisis de casos.
Como es un *parámetro*, en la práctica se da un solo caso.
Si se implementa como enumeración, el JIT termina
haciendo inline del valor que implementa dicho caso.
Por contra, con una alternativa, todo el código termina allí,
aunque sólo se hace inline del código contenido en la rama que se ejecuta
en la práctica.
La diferencia de tiempos medidas con pruebas simples en la línea de comando
es muy significativa: de 1:4 o 1:5.

Este ejemplo es más de inlining que de stack allocation.
De hecho, una enumeración es una jerarquía de singletons, que es de suponer
que la JVM pondrá en el heap apuntados desde un sitio fijo.


Referencias
----------------------------------------------------------------------

- [JVM Performance Comparison for JDK 21](
https://ionutbalosin.com/2024/02/jvm-performance-comparison-for-jdk-21/)
Un analisis del rendimiento de OpenJDK 21 enfrentando a
Hotspot C2, Oracle Graal y Community Graal.
Son todo microbenchmarks,
cada uno pensado para analizar lo buena que es la implementación de un cierto
tipo de optimización.
Tiene varios ejemplos para temas de inlining, que es la base para el SR.

- [Zing Falcon](
https://github.com/preames/public-notes/blob/master/falcon-compiler.rst)
Citan algunas presentaciones, en particular
*Control-flow sensitive escape analysis in Falcon JIT*.

- [JVM Anatomy Quark #18: Scalar Replacement](
https://shipilev.net/jvm/anatomy-quarks/18-scalar-replacement/)
Un ejemplo de cómo usar el *profiler* de GC de JMH para comprobar que
un código no está reservando memoria y poder deducir, indirectamente,
que la jvm ha hecho algún tipo de *stack allocation*.

- [Escape Analysis in the HotSpot JIT Compiler](
https://blogs.oracle.com/javamagazine/post/escape-analysis-in-the-hotspot-jit-compiler)

- [Current state of EA and its uses in the JVM](
bib/CurrentStateOfEscapeAnalyisAndItsUsesInTheJVM.pdf)
La presentación de un intento de añadir Stack Allocation a OpenJDK.
Lo hicieron sobre la versión 11, pero no parece que se haya mergeado.
