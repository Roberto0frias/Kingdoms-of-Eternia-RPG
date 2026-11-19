# Kingdoms of Eternia RPG — Fase 10

Mod Fabric modular para Minecraft Java 26.2 y Java 25. Aporta una base RPG medieval con personaje persistente, progresión, economía, profesiones, Reliquias de clase y ruinas compartidas generadas al explorar el Overworld.

## Estado actual: 0.10.0

| Sistema | Implementación actual |
|---|---|
| Personaje | Selección de cinco razas y cinco clases principales desde la interfaz con **P**. |
| Atributos y progresión | Seis atributos, experiencia y niveles; la base actual progresa hasta nivel 200. Inteligencia aumenta el maná máximo. |
| Clases y talentos | Guerrero, Mago, Arquero, Asesino y Sacerdote; especializaciones y 75 nodos de talentos en tres sendas por clase. |
| Maná y habilidades | Maná persistente: máximo `50 + 5 × Inteligencia efectiva`, regeneración `2 + Inteligencia/20` por segundo. Mantén **R** para cargar la habilidad hasta 2 segundos; la carga completa llega a 2.5× potencia y aumenta el coste de maná. |
| Reliquia de clase | Arma vinculada al propietario/clase, tres formas por clase, fusión con arma compatible y resto de monstruo, protección contra caída/almacenamiento. El Arquero usa tensado real de arco. |
| Flecha Astral | Si el Arquero no lleva flechas, puede cargar el arco y gastar 9–16 de maná para disparar una flecha mágica con daño escalado por Agilidad y carga. |
| Profesiones | Siete oficios con nivel independiente hasta 100 y experiencia por actividades compatibles. |
| Economía | Cartera contable en Cobre con conversión a Plata, Oro y Platino, recompensas por bajas y compras del catálogo del mod. |
| Misiones | Contratos de caza con objetivos, nivel mínimo y recompensas persistentes. |
| Mazmorra | Estructura compartida generada naturalmente en terreno nuevo del Overworld: laberinto procedural de 41×41, cámara de jefe 7×7, Iriel y Varkh. |

Las partidas guardan los datos RPG mediante attachments persistentes del jugador. El servidor valida selección, habilidades, compras, contratos y recompensas relevantes.

## Resumen de desarrollo

- **Fundación:** proyecto Fabric, datos persistentes del personaje, selección de raza/clase e interfaz inicial.
- **Progresión:** atributos, niveles, especializaciones, habilidades iniciales y talentos.
- **Economía y contratos:** cartera, recompensas por bajas, mercado del mod y contratos de caza.
- **Profesiones:** herrero, minero, leñador, agricultor, pescador, cazador y alquimista.
- **Reliquias:** armas ligadas a clase, selección de formas, fusión con armas compatibles y habilidades asociadas.
- **Mazmorra:** Ruina del Umbral, laberinto, NPC de expedición y jefe Varkh.
- **Versión 0.9.1:** sala del jefe despejada y conectada al laberinto, aviso previo del ataque y corrección del mensaje de ubicación.
- **Versión 0.10.0:** maná persistente, carga de habilidades con partículas, arco funcional y Flecha Astral, senda de talentos ampliada a 5 nodos y ruina compartida como worldgen con arena final.

Es una base de desarrollo incremental, no la totalidad del diseño final. Consulta [DISENO_MOD_RPG.md](DISENO_MOD_RPG.md) para el diseño y pendientes.

## Compilar

1. Instala JDK 25.
2. Abre esta carpeta en IntelliJ IDEA o ejecuta `./gradlew build` (Windows: `gradlew.bat build`). El proyecto incluye Gradle Wrapper 9.8.0; no hace falta instalar Gradle aparte.
3. El mod compilado aparece en `build/libs/kingdoms-of-eternia-rpg-0.10.0.jar`.
4. Instala Fabric Loader para Minecraft 26.2 y Fabric API en la carpeta `mods` del perfil. Este mod usa Fabric API y necesita ambas dependencias en juego/servidor.

## Instalar en el Launcher oficial (Windows)

1. Selecciona el perfil `fabric-loader-0.20.1-26.2` en Minecraft Launcher.
2. Copia `Kingdoms-of-Eternia-RPG-0.10.0.jar` y `fabric-api-0.161.0+26.2.jar` a `%APPDATA%\.minecraft\mods` (crea la carpeta si hace falta).
3. Inicia el perfil Fabric 26.2, entra a un mundo y pulsa **P**.

La instalación local de desarrollo probada usa Minecraft 26.2, Fabric Loader 0.20.1, Fabric API 0.161.0+26.2 y Java 25.

## Crear personaje

Entra en un mundo con el mod y pulsa **P**. Elige raza y clase; recibirás tu Reliquia vinculada. En el perfil consulta tu maná. Mantén **R** para cargar una habilidad y suelta para lanzarla; partículas acompañan el canalizado y el impacto. Si eres Arquero, usa la Reliquia como arco con flechas normales; si se te acaban, puedes gastar maná para una Flecha Astral. El árbol **Talentos** muestra las tres sendas de clase con cinco nodos cada una. Algunas formas piden nivel, talento, arma compatible y un resto de monstruo. La **Ruina del Umbral** no se abre desde el menú: aparece rara vez al generar terreno nuevo del Overworld, y todos los jugadores comparten el mismo laberinto de 41×41. Busca el arco tallado, encuentra a Iriel y derrota a Varkh en la arena 7×7; su golpe de área se anuncia antes del impacto. Reclama tu recompensa con Iriel. Los datos de recompensa y descanso se guardan por jugador, aunque la estructura y el jefe son compartidos.

## Estructura

- `core`: inicialización y perfil persistente del jugador.
- `core.network`: payload de selección cliente-servidor.
- `races`: catálogo de razas.
- `classes`: catálogo de clases y especializaciones.
- `attributes`: atributos RPG y sus identificadores.
- `progression`: XP, niveles, asignación de atributos y modificadores vanilla.
- `skills`: habilidad activa inicial de cada clase, cooldown y objetivos validados en servidor.
- `talents`: 75 nodos con hasta 3 rangos, organizados en 3 sendas por clase y con prerequisitos.
- `professions`: niveles 1–100; XP inicial por minería, tala, cosecha madura y caza.
- `economy`: cartera persistente en Cobre, conversión de visualización a Plata, Oro y Platino, recompensas por bajas hostiles, catálogo RPG y compras validadas por servidor. Las ofertas se desbloquean por oficio.
- `quests`: contratos de caza persistentes, objetivo y nivel mínimo validado en servidor, una misión activa y recompensas con descanso de 24 000 ticks.
- `relics`: Reliquia de clase enlazada a jugador/clase, persistencia de formas, fusión de armas compatibles, selección de modelo y bloqueo de caída/contenedores mediante mixins.
- `mana`: maná persistente calculado con Inteligencia efectiva, regeneración y coste de habilidades.
- `dungeons`: Feature de worldgen raro en Overworld, laberinto 41×41, arena 7×7, jefe con barra/ataque telegráfico y Cartógrafo NPC con recompensa compartida por estructura.
- `ui`: entrada de cliente y pantalla de creación.

Vitalidad, Resistencia, Fuerza, Agilidad y Suerte aplican modificadores aditivos moderados a atributos vanilla. Inteligencia determina el máximo de maná y participa en el escalado de habilidades. Las habilidades raciales siguen pendientes.

Habilidades de clase iniciales: Guerrero — Golpe Poderoso (objetivo cercano); Mago — Llama Arcana (ataque ígneo al objetivo señalado); Arquero — Disparo Certero (objetivo a distancia); Asesino — Golpe Sombrío (objetivo cercano); Sacerdote — Sanación Menor (se cura a sí mismo). Tienen enfriamiento y se calculan en servidor.

La Ruina del Umbral aparece solo en terreno nuevo generado después de instalar esta versión; Minecraft no añade automáticamente worldgen a chunks ya generados. El guardián usa el modelo de un Wither Skeleton con nombre, equipo y ataques propios; Iriel usa el modelo vanilla de aldeano. Las partículas son vanilla, no animaciones 3D personalizadas. Faltan modelos propios, más mazmorras/jefes y aldeanos, escalado/afijos de mobs, maestría comercial, gremios/ciudades y contratos de exploración/recolección.

## Pendientes principales

- Escalado de nivel, rareza y afijos para mobs por zona/bioma.
- Maestría y ofertas progresivas persistentes para aldeanos con profesiones.
- Misiones de entrega, exploración y recolección, además de NPCs originales.
- Gremios con rangos, banco, misiones y funciones sociales.
- Estructuras naturales, variedad de mazmorras y más mini bosses/jefes.
- Modelos y animaciones 3D propios, interfaz ampliada, localización y balance multijugador.

La compilación de fuentes para la versión 0.10.0 se verificó con `compileJava compileClientJava`. No se ha realizado una prueba interactiva de worldgen ni de combate dentro de un mundo para esta actualización.
