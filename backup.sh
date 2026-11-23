#!/bin/bash
# Copia de seguridad en GitHub: guarda el APK con su número de versión y sube todo.
# Uso: bash backup.sh
set -euo pipefail
cd "$(dirname "$0")"

VNAME=$(sed -n 's/^versionName=//p' app.properties | head -1)
VCODE=$(sed -n 's/^versionCode=//p' app.properties | head -1)
[ -f build/app.apk ] || { echo "No existe build/app.apk; compila primero." >&2; exit 1; }

mkdir -p apk
cp build/app.apk "apk/AnimeCalc-v${VNAME}.apk"
echo "APK guardado: apk/AnimeCalc-v${VNAME}.apk (versionCode ${VCODE})"

git add -A
if git diff --cached --quiet; then
  echo "Sin cambios nuevos que subir."
  exit 0
fi
git -c user.name="KoalaSan" -c user.email="38317850+KoalaSan@users.noreply.github.com" \
  commit -q -m "Copia de seguridad v${VNAME} (versionCode ${VCODE})"
git push -q origin main
echo "Subido a https://github.com/KoalaSan/anime-calculator"
