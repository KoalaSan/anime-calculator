# 🌸 Anime Calc

Calculadora Android con tema anime y herramientas para seguidores de anime.
Proyecto sin dependencias externas: Java 8 + Android framework, compilado con la
toolchain local de Buddy (sin Gradle ni Android Studio).

## Funciones

### 🧮 Calculadora
- Suma, resta, multiplicación y división con precedencia de operaciones en cadena.
- Decimal, signo (±), porcentaje (%) y borrado (C, ⌫).
- Línea de historial y control de división entre cero.
- Botones táctiles con vibración y diseño rosa/morado anime.

### 🌸 Herramientas anime
- **Progreso de anime**: a partir de episodios totales, vistos y minutos por
  episodio calcula tiempo visto, tiempo restante y porcentaje de progreso.
- **Promedio de notas**: acepta notas separadas por coma, punto y coma o espacio
  y devuelve el promedio con un mensaje de ánimo.
- **Actualizaciones**: consulta la API de GitHub Releases y avisa si hay una
  versión nueva, con enlace directo a la descarga.

## Datos del proyecto

| Campo | Valor |
| --- | --- |
| Paquete | `com.buddyapps.anicalc` |
| versionCode | 1 |
| versionName | 1.0 |
| minSdk | 26 (Android 8.0) |
| Repositorio | https://github.com/KoalaSan/anime-calculator |

## Estructura

```
AndroidManifest.xml                     Permisos e actividad lanzadora
app.properties                          Versión y SDK mínimo
java/com/buddyapps/anicalc/MainActivity.java   Toda la lógica y la UI en código
docs/INSTALACION.md                     Cómo compilar e instalar
docs/ACTUALIZACIONES.md                 Cómo funciona copia de seguridad y updates
CHANGELOG.md                            Historial de versiones
backup.sh                               Guarda el APK y sube el repo a GitHub
```

## Compilación

```bash
bash ~/.buddy/work/.claude/skills/android-app/setup-toolchain.sh   # solo la primera vez
bash ~/.buddy/work/.claude/skills/android-app/build-apk.sh ~/.buddy/work/anime-calculator
```

El APK queda en `build/app.apk`.

## Copia de seguridad

Todo el código, la documentación y el APK versionado se suben a GitHub.
Ver [docs/ACTUALIZACIONES.md](docs/ACTUALIZACIONES.md).
