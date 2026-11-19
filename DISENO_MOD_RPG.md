# Kingdoms of Eternia RPG

Sistema RPG para Minecraft Java **26.2**, pensado para Fabric y Java 25. Transforma la supervivencia en progresión de personaje, aventuras, profesiones, gremios, economía y contenido de endgame. El repositorio está en etapa de diseño; esta especificación define el alcance funcional y la arquitectura inicial antes de implementar el mod.

## Estado de implementación

- **Fase 1 — completada:** proyecto Fabric, persistencia de raza/clase, creación de personaje con la tecla P.
- **Fase 2 — MVP completado:** XP por derrotar hostiles directamente, niveles 1–200, dos puntos por nivel, asignación de seis atributos, persistencia/sincronización y bonificaciones raciales de estadísticas. Vitalidad, Resistencia, Fuerza, Agilidad y Suerte afectan atributos vanilla.
- **Fase 3 — base de combate completada:** 15 especializaciones persistentes, desbloqueables al nivel 10, y una habilidad activa con cooldown por clase en R, con objetivo/daño/curación validados por el servidor.
- **Fase 4 — talentos y oficios base:** ahora 75 nodos (15 por clase) en tres sendas de cinco nodos y hasta tres rangos; se obtiene un punto cada cinco niveles. Siete profesiones persistentes hasta nivel 100, con XP para minería, tala, agricultura y caza.
- **Fase 5 — economía inicial:** cartera persistente en Cobre, conversión de denominaciones en interfaz P y recompensas moderadas por matar mobs hostiles directamente. Sin compras todavía.
- **Fase 6 — mercado RPG inicial:** catálogo de minería, agricultura y herrería; coste en Cobre y requisitos de nivel profesional comprobados por el servidor antes de cobrar y entregar. No modifica el comercio vanilla de aldeanos.
- **Fase 7 — contratos de caza:** misiones repetibles de zombis, esqueletos y arañas; progreso persistente, una activa por jugador, nivel mínimo, pago de XP/Cobre y descanso de 24 000 ticks. El servidor controla aceptación, objetivo y pago.
- **Fase 8 — Reliquias de clase:** objeto único enlazado a UUID y clase, protegido contra tirar/guardar; tres formas por clase, talento + nivel + arma compatible + resto de monstruo para fusionar; arma escala sus habilidades.
- **Fase 9 — Ruina del Umbral:** primera mazmorra con Cartógrafo NPC, guardián Varkh con barra y golpe de área anunciado.
- **Fase 10 — Maná, carga y mundo compartido:** maná persistente basado en Inteligencia, canalización de habilidades hasta dos segundos con más daño y coste, partículas de impacto, arco real para Arquero y flecha mágica si se queda sin munición. La Ruina pasa a worldgen raro compartido, mide 41×41 y termina en arena 7×7; hay cinco nodos por senda de talento y catalizadores de restos de monstruos para formas.
- **Siguiente:** probar la generación natural en mundo nuevo/servidor, agregar variantes de ruina, modelos/animaciones propios, NPC/jefes adicionales, gremios/ciudades, contratos de exploración y mobs élite escalados.

Alcance pendiente: habilidades raciales, habilidades activas auxiliares, sistemas de restos por familia de monstruo, árboles de talentos más grandes, escalado de mobs por zona, balance final de la curva y efectos propios por especialización.

## Decisiones de diseño

- El servidor autoritativo valida combate, progresión, inventario, moneda, misiones y permisos. El cliente renderiza interfaces y envía solicitudes validadas.
- Los sistemas se separan en módulos con API de eventos/datos para que clases, mobs, economía o misiones puedan evolucionar sin acoplarse a la interfaz.
- Configuración inicial por JSON/datapack siempre que sea seguro: curvas de XP, biomas, afijos, botín, misiones, comercio, recompensas y eventos.
- Guardado por UUID, con `schemaVersion`, valores por defecto y migraciones. Nunca usar nombres de jugador como identidad persistente.
- El diseño cubre el objetivo final; se implementa por hitos. Las cifras y multiplicadores de combate son parámetros iniciales sujetos a balance con pruebas de juego.

## 1. Razas

Selección al crear personaje; mostrar estadísticas y habilidad antes de confirmar. Cada personaje mantiene una raza. Cambiarla, si se habilita, requiere un objeto/servicio de reinicio y confirmación.

| Raza | Bonificación | Habilidad racial |
|---|---|---|
| Humano | +5% XP global, +5 Suerte, +5 Comercio | **Negociador:** descuento en compras de aldeanos |
| Elfo | +10 Agilidad, +10 Inteligencia | **Visión Natural:** mayor alcance efectivo de arco |
| Enano | +15 Resistencia, +10 Fuerza | **Maestro Herrero:** bonificación de calidad al fabricar |
| Orco | +20 Fuerza, +10 Vitalidad | **Furia de Guerra:** más daño al estar con poca vida |
| Tiefling | +15 Inteligencia, resistencia al fuego | **Llama Infernal:** ataque mágico de fuego |

Las bonificaciones de atributos se aplican al cálculo efectivo, sin inflar puntos libres. Definir límites: descuento máximo acumulado, tope de alcance/daño y resistencia al fuego compatible con otros efectos. Las habilidades raciales deben tener coste/enfriamiento o condición legible y estar validadas en servidor.

## 2. Atributos

Cada nivel de personaje entrega puntos distribuibles. Valores en pantalla incluyen base, inversión, raza, equipo y total. Fórmulas centralizadas y con límites para evitar combinaciones explotables.

| Atributo | Efectos |
|---|---|
| Vitalidad | Salud máxima y regeneración |
| Resistencia | Armadura, defensa y aguante |
| Fuerza | Daño cuerpo a cuerpo |
| Agilidad | Velocidad, esquiva y crítico |
| Inteligencia | Maná y poder mágico |
| Suerte | Botín, crítico, drops raros y encantamientos |

**Reglas de balance:** Suerte no genera duplicados de drops únicos de jefe ni incrementos ilimitados; comerciar/manufacturar no debe crear bucles de moneda; esquiva y crítico tienen tope; regeneración durante combate es menor que fuera de combate. Valores de rendimiento se calibran antes de lanzar endgame.

## 3. Clases principales y especializaciones

El jugador elige una clase principal. La especialización se desbloquea por nivel/quest y define variantes del árbol; permitir respec mediante coste para que una elección inicial no sea irreversible.

| Clase | Especializaciones |
|---|---|
| Guerrero | Caballero (escudos y defensa), Berserker (daño alto), Paladín (curación y luz) |
| Mago | Elemental, Nigromante, Invocador |
| Arquero | Francotirador, Explorador, Cazador de Bestias |
| Asesino | Sombras, Venenos, Cazador Nocturno |
| Sacerdote | Sanador, Templario, Exorcista |

Las habilidades activas usan entradas configurables, coste de maná/energía y enfriamiento. El servidor calcula el resultado; el cliente recibe estado, animación, sonido y mensajes. Entidades invocadas tienen límite, duración y dueño persistente para impedir acumulación infinita.

## 4. Profesiones

Siete líneas secundarias independientes, cada una con nivel máximo 100: Herrero, Minero, Leñador, Agricultor, Pescador, Cazador y Alquimista. Progresan con acciones legítimas verificadas y tienen recetas/beneficios por hitos. El progreso profesional no consume los puntos de clase.

- **Herrero:** armas únicas, equipo de calidad y forja de objetos legendarios.
- **Minero:** minerales raros, vetas/contratos por nivel.
- **Leñador:** materiales especiales y rendimiento de madera.
- **Agricultor:** cultivos avanzados, semillas y contratos.
- **Pescador:** peces legendarios y tesoros.
- **Cazador:** materiales únicos de criaturas y rastreo.
- **Alquimista:** pociones avanzadas, elixires y consumibles.

Evitar XP por colocar y romper repetidamente el mismo bloque, granjas de criaturas infinitas o repetir intercambios propios. Los límites y elegibilidad se configuran para servidores.

## 5. Árboles de habilidades

Cada clase tiene 3 ramas. Especificación final por rama: **30 talentos, de los cuales 5 son habilidades activas y 10 pasivas**; el resto pueden ser mejoras, talentos de ruta y modificadores de estilo. Para cinco clases, esto significa al menos 450 talentos. Es un alcance de contenido por temporadas/hitos, no un requisito para el primer prototipo.

Ejemplo Guerrero:

- **Defensa:** Escudo Divino, Muralla Humana, Provocación y talentos defensivos.
- **Combate:** Golpe Poderoso, Torbellino, Ejecución y talentos ofensivos.
- **Maestro de Armas:** especialización en espadas, hachas, martillos y mejoras asociadas.

Cada nodo declara identificador estable, rama, rangos, coste, prerequisitos, nivel mínimo, icono, texto y efecto. Separar definiciones de contenido de la lógica de desbloqueo. Al quitar talento, recalcular pasivos y cancelar/actualizar estados activos relacionados.

## 6. Nivel y experiencia

Nivel máximo de personaje: **200**.

| Tramo | Título |
|---|---|
| 1–50 | Principiante |
| 51–100 | Veterano |
| 101–150 | Maestro |
| 151–200 | Legendario |

La curva exacta de XP se mantiene configurable; el nivel 200 debe exigir juego de endgame sin obligar a farmear una sola actividad. Fuentes: combate elegible, misiones, exploración y eventos. Separar XP de personaje, clase y profesiones. Registrar XP total para migraciones/rebalance; otorgar puntos al subir nivel de forma idempotente (no duplicarlos al cargar datos).

## 7. Escalado por bioma y encuentro

| Zona | Rango de nivel previsto |
|---|---:|
| Bosque | 1–20 |
| Cuevas | 20–40 |
| Nether | 70–120 |
| End | 130–200 |

Definir tablas de zona configurables por bioma/dimensión y permitir tramos intermedios para el mundo normal. El nivel de encuentro considera zona, profundidad, progreso/dificultad y jugadores cercanos, no solo nivel de quien golpea. Mostrar nivel y rango en nombre/barra. No alterar entidades nombradas, jefes de otros mods o mobs de evento ajenos sin opción explícita.

## 8. Rangos de mobs, afijos y botín

| Rango | Color | Perfil de poder | Contenido |
|---|---|---|---|
| Normal | Blanco | Base escalada | XP y botín normal |
| Élite | Azul | Objetivo de balance: alrededor de 3× | 1–2 afijos y drops mejorados |
| Campeón | Morado | Objetivo de balance: alrededor de 10× | Habilidades propias y botín raro |
| Mini Boss | Naranja | Encuentro regional diseñado | Mecánicas y drop temático |
| Boss | Rojo | Encuentro/evento especial | Fases, botín único y participación |

Los múltiplos 3×/10× son presupuesto global del encuentro, no multiplicador indiscriminado de todos los atributos: repartir presupuesto entre salud, daño, defensa, afijos y mecánicas. Un campeón diez veces más fuerte sin telegráficos puede volverse injusto o inviable; limitar daño por golpe y anunciar ataques fuertes.

Afijos iniciales: Brutal, Rápido, Vampírico; extender con listas por zona. Cada afijo altera pocas propiedades, tiene reglas de incompatibilidad y puede explicarse en tooltip. Botín único con elegibilidad/recompensa personal para participantes que contribuyeron; evitar que Suerte cree copias infinitas.

## 9. Mini bosses

Encuentros regionales con ubicación/condición de aparición configurable, identidad visual, ataques telegráficos, enfriamientos y botín garantizado o por probabilidad controlada.

| Mini boss | Drop distintivo |
|---|---|
| Rey Esqueleto | Corona Maldita |
| Araña Reina | Seda Legendaria |
| Gólem Ancestro | Núcleo Antiguo |

## 10. Jefes mundiales

Invocación por eventos, calendario del mundo o condiciones. El servidor controla una instancia de encuentro, fases y estado; impedir aparición duplicada al reiniciar o con varios jugadores. Escalar vida según participación activa con límites y dar recompensa personal proporcional a contribución mínima.

| Jefe | Nivel |
|---|---:|
| Dragón Carmesí | 150 |
| Rey Lich | 180 |
| Titán del Vacío | 200 |

## 11. Gremios

Nivel máximo 50. Funciones: crear gremio, banco compartido, chat privado, misiones y guerra de gremios. Rangos: Recluta, Miembro, Veterano, Oficial, Sub Líder y Líder. Permisos explícitos para invitar, expulsar, depósito/retiro, editar, iniciar guerra y transferir liderazgo. Auditar operaciones del banco y mantener saldo separado de jugadores.

Guerras/territorio requieren servidores con reglas explícitas: ventanas horarias, consentimiento del servidor, protección de construcciones, límites de recompensa y persistencia central. En mundo individual se puede desactivar ese módulo. Chat privado usa canales de servidor con permisos y saneamiento del contenido.

## 12. Economía: Corona Dorada

Moneda de cuenta con icono propio. Subdivisiones exactas:

`1 Platino = 100 Oro` · `1 Oro = 100 Plata` · `1 Plata = 100 Cobre`.

La unidad contable mínima es Cobre; usar entero de 64 bits y convertir para presentación. La cartera puede mostrarse como monedas denominadas; objetos-moneda físicos son solo cosméticos/canje validados, para evitar duplicación por clonación de inventario.

Fuentes: recompensas por matar mobs elegibles, misiones, contratos, eventos. Sumideros: compras, tarifas, respec, fabricación/servicios. Toda transacción se calcula y confirma en servidor de forma atómica; nunca usar precios o saldo que envíe el cliente. Limitar recompensas repetibles y auditar el historial económico.

## 13. Misiones y eventos

- **Gremio Aventurero:** caza, recolección y exploración.
- **Aldeanos:** entregas, protección y construcción.
- **Eventos:** invasiones y defensa de ciudades.

Estados de misión: disponible, aceptada, progreso, lista para entregar, completada y expirada/cancelada. Objetivos y recompensas declarativos. Entrega de objetos consume exactamente lo solicitado; confirmar una misión no duplica pago. Misiones compartidas definen contribución individual y grupal. Construcción solo se mide con criterios verificables de bloques/evento autorizados, no por una detección genérica de cualquier colocación.

## 14. Aldeanos evolutivos

Nivel 1–100 y maestría: Novato, Aprendiz, Experto, Maestro, Legendario. Progresan con intercambios válidos y misiones completadas, con enfriamiento y rendimientos decrecientes contra ciclos de compra-venta. Persisten por UUID de entidad y profesión.

Ejemplo Herrero:

| Nivel | Oferta desbloqueada |
|---:|---|
| 1 | Espada de hierro |
| 25 | Espada de acero |
| 50 | Espada épica |
| 75 | Espada heroica |
| 100 | Armamento legendario |

La interfaz RPG amplía el comercio vanilla; no debe borrar ofertas ni cambiar la profesión/mesa de trabajo original sin consentimiento/configuración. Las ofertas usan objetos registrados, costes, stock y reposición válidos. Adaptar requisitos por nivel y maestría. Definir los materiales Acero/ítems de rareza como registros incluidos en el mod o configurables; no referenciarlos si aún no existen.

## 15. Reputación de ciudades

Cada ciudad asentada registra **Fama, Honor y Prestigio**. En una primera versión, estos valores pueden ser reputación por facción/poblado registrado por el mod, sin inferir automáticamente límites de ciudad sobre todo el mundo. Recompensas: descuentos limitados, títulos y acceso a NPC especiales. Misiones de protección/entrega afectan reputación; crímenes/abandono pueden reducirla según reglas transparentes. Evitar que descuentos se acumulen sin límite con Negociador.

## 16. Interfaz

Tecla predeterminada **P**, configurable y sin conflicto forzado: si otra acción usa P, controles permite reasignar. Pantalla raíz con:

Personaje · Clases · Profesiones · Habilidades · Gremio · Reputación · Misiones · Economía.

Incluir tooltips, búsqueda/listas para contenido grande, navegación por teclado, escalado, localización y compatibilidad con pantallas pequeñas. Mostrar coste, requisitos, nivel, estado y consecuencias antes de confirmar compras, respec, retiro del banco o guerra. El cliente nunca decide el resultado de una acción económica/combate.

## 17. Objetos y afijos RPG

Rarezas: Común, Poco Común, Raro, Épico, Legendario, Mítico y Divino. Afijos: Fuerte, Bendito, Maldito, Vampírico, Infernal y Celestial. Propiedades en datos/componentes, compatibles con inventario, encantamientos, reparación y guardado. Definir rangos numéricos por nivel/rareza y límites de afijos; mostrar comparación en tooltip. La calidad del Maestro Herrero varía dentro de un rango, no garantiza nivel de rareza por repetición de fabricación.

## 18. Endgame

- Mazmorras: Torres, Catacumbas y Ruinas.
- Raids para grupos de 5, 10 y 20 jugadores.
- Arenas PvP clasificadas.
- Guerras de gremios y control territorial.
- Jefes mundiales semanales.

Para PvP, desactivar o normalizar bonificaciones que produzcan ventajas imposibles de contrarrestar; colas, ranking y reglas dependen del servidor. Raids deben tener instancias/encuentros con escalado por tamaño, roles y reconexión. Contenido multijugador de gran escala (ranking global, guerra entre servidores) requeriría backend externo y es posterior al mod local.

## 19. Arquitectura modular

La siguiente estructura es la arquitectura de paquetes/módulos del mod Fabric. Se conserva un artefacto único al principio para facilitar instalación; módulos Gradle independientes solo cuando dependencias y ciclos lo justifiquen.

```text
kingdoms-of-eternia-rpg/
├── core-rpg/        # identidad de personaje, guardado, eventos, permisos, configuración
├── races/           # razas, rasgos y habilidades raciales
├── classes/         # clases, especializaciones, fórmulas de combate
├── skills/          # nodos, prerequisitos, activas/pasivas, enfriamientos
├── professions/     # siete progresiones y recetas
├── economy/         # Corona Dorada, transacciones, inventario contable
├── guilds/          # miembros, rangos, banco, chat, misiones
├── quests/          # contratos de gremio, aldeano y evento
├── mobs/            # escalado, rareza, afijos, XP y drops
├── bosses/          # mini bosses, jefes y encuentros
├── reputation/      # fama, honor, prestigio por ciudad/facción
├── dungeons/        # torres, catacumbas, ruinas y raids
├── world-events/    # invasiones, defensa y jefes programados
└── ui/              # pantallas, HUD, keybinds y sincronización visual
```

Dependencias de dominio recomendadas: `core-rpg` no depende de pantallas ni de módulos de contenido; economía no depende de UI; clases/skills comparten API de atributos/eventos; misiones emiten recompensas a economía/progresión mediante servicios; mobs/bosses usan progresión y recompensas sin depender de gremios. Usar registros/identificadores namespaced y datapacks para contenido extensible.

## 20. Fases de desarrollo

1. **Fundación:** proyecto Fabric 26.2, registro, configuración, persistencia/migración, personaje, selección de raza/clase y UI base.
2. **Progresión de combate:** seis atributos, XP/niveles 1–200, cinco clases/especializaciones, habilidades iniciales y árboles de datos.
3. **Economía y mundo:** Corona Dorada, recompensas de mobs, escalado por zona, élites/campeones, misiones y comercio/maestría de aldeanos.
4. **Profesiones y social:** siete profesiones, ciudades/reputación, gremios/banco/rangos/chat y contratos.
5. **Encuentros:** mini bosses, bosses mundiales, mazmorras y raids.
6. **Competitivo y vivo:** arena clasificada, guerras de gremios, eventos recurrentes, balance y compatibilidad.

Completar pruebas de guardado, reconexión, multijugador, sincronización, duplicación de monedas, recompensas repetibles, cambios de dimensión y migración antes de publicar cada fase.

## Referencias de plataforma

- [Documentación oficial de Fabric](https://docs.fabricmc.net/) (incluye selector de versión 26.2).
- [Fabric Develop](https://fabricmc.net/develop/) (versiones recomendadas de Loader, mappings y Fabric API por versión).
- [Guía oficial Fabric: entorno de desarrollo](https://docs.fabricmc.net/develop/getting-started/setting-up) (JDK 25 para Minecraft 26.2).
