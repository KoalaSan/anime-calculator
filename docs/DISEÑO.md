# Identidad visual 🌸

## Concepto

Una calculadora que se sienta como un *key visual* de anime: fondo nocturno
con halos violetas, partículas flotantes, una mascota chibi en la cabecera y
superficies redondeadas tipo tarjeta. El objetivo es que sea usable a una mano
y reconocible al instante en el launcher.

## Paletas

El botón `🎨` de la cabecera rota entre tres temas. Cada tema define ocho tokens:
fondo superior, medio e inferior; acento principal y secundario; fondo de
dígitos, de operadores y de borrado.

| Tema | Fondo | Acento | Personalidad |
| --- | --- | --- | --- |
| ✿ Sakura | `#140F24` → `#2A1748` → `#100D1E` | `#FF6FA9` + `#9B6DFF` | Rosa clásico, el original |
| ✧ Hoshizora | `#08182B` → `#103A5E` → `#071320` | `#63D9FF` + `#4C9BFF` | Azul cielo nocturno |
| ☾ Yoru | `#130C28` → `#2C1A4E` → `#0B0818` | `#C68BFF` + `#7A5CFF` | Violeta intenso |

Los textos secundarios se derivan mezclando el acento secundario con blanco,
y los bordes de tarjetas se oscurecen para no competir con el contenido.

## Tipografía

Escala usada en `MainActivity.java` (sp):

| Nivel | Tamaño | Uso |
| --- | --- | --- |
| Título | 21, bold | Nombre de la app |
| Encabezado de sección | 17, bold | Progreso, notas, GitHub |
| Display | 46, bold | Resultado de la calculadora |
| Cuerpo | 14–15 | Botones, campos y resultados |
| Caption | 11–12 | Subtítulo, pie e información |

## Componentes

- **Botones calc**: cuadrícula 4×5, peso igual, radio 14 dp, `StateListDrawable`
  con aclarado del 18 % al pulsar. El `=` usa el acento principal en negrita.
- **Tarjetas**: radio 18 dp, relleno semitransparente y borde de acento apagado.
- **Campos**: radio 10 dp, borde de acento secundario.
- **Pestañas**: radio 20 dp; la activa se rellena con el color de operador.
- **Partículas**: 24 puntos con senos desfasados y cuatro estrellas de cuatro
  puntas, animadas con `ValueAnimator` infinito de 5,2 s.

## Accesibilidad

- Contraste de texto principal sobre fondo oscuro alto; los acentos claros se
  usan solo en textos grandes e iconos.
- Cada estado tiene texto además de color (p. ej. `⚠`, `✦`, mensajes de error).
- Botones de 44–64 dp de alto, por encima del mínimo táctil recomendado.

## Recursos

- `res/drawable/mascot.xml`: chibi vectorial (pelo rosa y violeta, flor de sakura).
- `res/drawable/ic_launcher.xml`: icono vectorial de 108 dp con calculadora y flor.
- `assets/banner.png`: banner 1200×630 generado por `tools/make_banner.py`.
- `docs/screenshots/*.png`: vistas previas ilustradas de `tools/make_previews.py`.
