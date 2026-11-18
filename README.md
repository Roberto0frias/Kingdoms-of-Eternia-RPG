# Kingdoms of Eternia RPG — Fase 9

Mod Fabric modular para Minecraft Java 26.2 y Java 25. Aporta una base RPG medieval con personaje persistente, progresión, economía, profesiones, Reliquias de clase y una primera expedición a una ruina con laberinto y jefe.

## Estado actual: 0.9.1

| Sistema | Implementación actual |
|---|---|
| Personaje | Selección de cinco razas y cinco clases principales desde la interfaz con **P**. |
| Atributos y progresión | Seis atributos, experiencia y niveles; la base actual progresa hasta nivel 200. |
| Clases y talentos | Guerrero, Mago, Arquero, Asesino y Sacerdote; especializaciones y nodos de talentos con requisitos. |
| Habilidades | Habilidad inicial por clase, activación con **R**, enfriamiento y validación en servidor. |
| Reliquia de clase | Arma vinculada a propietario y clase, tres formas por clase, desbloqueos por talento/nivel/fusión y protección contra caída o almacenamiento. |
| Profesiones | Siete oficios con nivel independiente hasta 100 y experiencia por actividades compatibles. |
| Economía | Cartera contable en Cobre con conversión a Plata, Oro y Platino, recompensas por bajas y compras del catálogo del mod. |
| Misiones | Contratos de caza con objetivos, nivel mínimo y recompensas persistentes. |
| Mazmorra | Expedición: ruina de piedra con laberinto procedural, el Cartógrafo Iriel y Varkh, con barra de jefe y golpe de área telegrafiado. |

Las partidas guardan los datos RPG mediante attachments persistentes del jugador. El servidor valida selección, habilidades, compras, contratos y recompensas relevantes.

## Resumen de desarrollo

- **Fundación:** proyecto Fabric, datos persistentes del personaje, selección de raza/clase e interfaz inicial.
- **Progresión:** atributos, niveles, especializaciones, habilidades iniciales y talentos.
- **Economía y contratos:** cartera, recompensas por bajas, mercado del mod y contratos de caza.
- **Profesiones:** herrero, minero, leñador, agricultor, pescador, cazador y alquimista.
- **Reliquias:** armas ligadas a clase, selección de formas, fusión con armas compatibles y habilidades asociadas.
- **Mazmorra:** Ruina del Umbral, laberinto, NPC de expedición y jefe Varkh.
- **Versión 0.9.1:** sala del jefe despejada y conectada al laberinto, aviso previo del ataque y corrección del mensaje de ubicación.

Es una base de desarrollo incremental, no la totalidad del diseño final. Consulta [DISENO_MOD_RPG.md](DISENO_MOD_RPG.md) para el diseño y pendientes.

## Compilar

1. Instala JDK 25.
2. Abre esta carpeta en IntelliJ IDEA o ejecuta `./gradlew build` (Windows: `gradlew.bat build`). El proyecto incluye Gradle Wrapper 9.8.0; no hace falta instalar Gradle aparte.
3. El mod compilado aparece en `build/libs/kingdoms-of-eternia-rpg-0.9.1.jar`.
4. Instala Fabric Loader para Minecraft 26.2 y Fabric API en la carpeta `mods` del perfil. Este mod usa Fabric API y necesita ambas dependencias en juego/servidor.

## Instalar en el Launcher oficial (Windows)

1. Selecciona el perfil `fabric-loader-0.20.1-26.2` en Minecraft Launcher.
2. Copia `Kingdoms-of-Eternia-RPG-0.9.1.jar` y `fabric-api-0.161.0+26.2.jar` a `%APPDATA%\.minecraft\mods` (crea la carpeta si hace falta).
3. Inicia el perfil Fabric 26.2, entra a un mundo y pulsa **P**.

La instalación local de desarrollo probada usa Minecraft 26.2, Fabric Loader 0.20.1, Fabric API 0.161.0+26.2 y Java 25.

## Crear personaje

Entra en un mundo con el mod y pulsa **P**. Elige raza y clase y confirma; recibirás tu Reliquia vinculada. Empúñala y pulsa **R** para canalizar su habilidad. **Reliquia** muestra formas, talentos y armas de fusión; la Reliquia cambia de modelo y poder según la forma. En **Mazmorra**, abre una expedición. Se construirá una ruina de piedra a cierta distancia sobre terreno llano: habla con Iriel en la entrada, recorre el laberinto y derrota a Varkh en la cámara oriental. Su ataque de área se anuncia antes del impacto. Vuelve con Iriel para cobrar XP, Cobre y una Estrella del Nether. La expedición y el descanso se guardan por personaje. La Reliquia no puede tirarse ni guardarse en cofres.

## Estructura

- `core`: inicialización y perfil persistente del jugador.
- `core.network`: payload de selección cliente-servidor.
- `races`: catálogo de razas.
- `classes`: catálogo de clases y especializaciones.
- `attributes`: atributos RPG y sus identificadores.
- `progression`: XP, niveles, asignación de atributos y modificadores vanilla.
- `skills`: habilidad activa inicial de cada clase, cooldown y objetivos validados en servidor.
- `talents`: 45 nodos iniciales con hasta 3 rangos, prerequisitos y efectos pasivos.
- `professions`: niveles 1–100; XP inicial por minería, tala, cosecha madura y caza.
- `economy`: cartera persistente en Cobre, conversión de visualización a Plata, Oro y Platino, recompensas por bajas hostiles, catálogo RPG y compras validadas por servidor. Las ofertas se desbloquean por oficio.
- `quests`: contratos de caza persistentes, objetivo y nivel mínimo validado en servidor, una misión activa y recompensas con descanso de 24 000 ticks.
- `relics`: Reliquia de clase enlazada a jugador/clase, persistencia de formas, fusión de armas compatibles, selección de modelo y bloqueo de caída/contenedores mediante mixins.
- `dungeons`: generador procedural de laberinto medieval, sitio de terreno llano, jefe con barra/ataque telegráfico y Cartógrafo NPC con diálogo/recompensa de expedición.
- `ui`: entrada de cliente y pantalla de creación.

Vitalidad, Resistencia, Fuerza, Agilidad y Suerte aplican modificadores aditivos moderados a atributos vanilla. Inteligencia ya progresa y se usa en habilidades, y se conectará al maná cuando se implemente el sistema mágico. Habilidades raciales y árboles de talento siguen pendientes.

Habilidades de clase iniciales: Guerrero — Golpe Poderoso (objetivo cercano); Mago — Llama Arcana (ataque ígneo al objetivo señalado); Arquero — Disparo Certero (objetivo a distancia); Asesino — Golpe Sombrío (objetivo cercano); Sacerdote — Sanación Menor (se cura a sí mismo). Tienen enfriamiento y se calculan en servidor.

La base profesional ya guarda siete oficios hasta el nivel 100; hay XP inicial para minería, tala, cosecha madura y caza. El mercado actual es un catálogo propio y no reemplaza los intercambios vanilla. La Ruina del Umbral es una expedición generada por jugador, no una estructura que aparezca naturalmente en el mundo. El guardián usa el modelo de un Wither Skeleton con atributos, nombre, equipo y ataques propios, y el Cartógrafo es un aldeano vanilla con identidad y diálogo del mod. Faltan modelos/animaciones 3D propios, variedad de mazmorras, jefes y aldeanos adicionales, contratos de exploración/recolección, gremios y ciudades.

## Pendientes principales

- Escalado de nivel, rareza y afijos para mobs por zona/bioma.
- Maestría y ofertas progresivas persistentes para aldeanos con profesiones.
- Misiones de entrega, exploración y recolección, además de NPCs originales.
- Gremios con rangos, banco, misiones y funciones sociales.
- Estructuras naturales, variedad de mazmorras y más mini bosses/jefes.
- Modelos y animaciones 3D propios, interfaz ampliada, localización y balance multijugador.

No se ha completado una prueba exhaustiva dentro de un mundo real para la versión 0.9.1; la compilación Gradle sí terminó correctamente.
