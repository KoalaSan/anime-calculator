# Copia de seguridad y actualizaciones

GitHub es la fuente de verdad del proyecto: allí están el código, la
documentación y el APK de cada versión.

## Copia de seguridad

Cada cambio se sube con:

```bash
cd ~/.buddy/work/anime-calculator
bash backup.sh
```

El script:
1. Comprueba que exista `build/app.apk`.
2. Copia el APK a `apk/AnimeCalc-v<versionName>.apk` (histórico versionado).
3. Hace `git add -A`, crea un commit con la versión y hace push a `main`.

## Publicar una actualización

1. Sube `versionCode` y `versionName` en `app.properties` (versionCode siempre
   debe aumentar).
2. Actualiza `CURRENT_VERSION` en `MainActivity.java` y añade la entrada en
   `CHANGELOG.md`.
3. Compila con `build-apk.sh`.
4. Ejecuta `bash backup.sh`.
5. Crea el Release en GitHub con el tag `v<versionName>` y sube el APK:

```bash
gh release create v1.1 apk/AnimeCalc-v1.1.apk \
  --repo KoalaSan/anime-calculator \
  --title "Anime Calc v1.1" --notes "Descripción de los cambios"
```

## Cómo lo detecta la app

Dentro de la app, pestaña **🌸 Anime → Buscar actualización**, se consulta:

```
GET https://api.github.com/repos/KoalaSan/anime-calculator/releases/latest
```

Se compara `tag_name` con `CURRENT_VERSION`. Si son distintos se muestra un
diálogo con el enlace `html_url` del Release para descargar el APK nuevo.
Si no hay conexión, se muestra un aviso; la calculadora sigue funcionando
porque no depende de la red.

> Nota: el repositorio es público para que la descarga del APK no requiera
> iniciar sesión en GitHub.
