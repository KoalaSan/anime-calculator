# Instalación

## Desde el APK publicado (recomendado)

1. Abre https://github.com/KoalaSan/anime-calculator/releases
2. Descarga el APK de la última versión (por ejemplo `AnimeCalc-v1.0.apk`).
3. Abre el archivo descargado en el teléfono.
4. Si Android pide permiso, activa **Permitir desde esta fuente** para tu
   explorador o gestor de archivos.
5. Pulsa **Instalar** y confirma (a veces pide huella o PIN).

## Compilando desde el código

Necesitas la toolchain de Buddy en el propio teléfono:

```bash
bash ~/.buddy/work/.claude/skills/android-app/setup-toolchain.sh
bash ~/.buddy/work/.claude/skills/android-app/build-apk.sh ~/.buddy/work/anime-calculator
```

Después instala `~/.buddy/work/anime-calculator/build/app.apk` con el instalador
de Android. El usuario siempre debe pulsar **Instalar**; un asistente no puede
hacerlo por él.

## Actualización desde una versión anterior

El APK está firmado con la misma clave de depuración de la toolchain, así que
puedes instalar encima sin desinstalar: aparecerá **Actualizar** en vez de
**Instalar**. No se pierde nada porque la app no guarda datos persistentes.
