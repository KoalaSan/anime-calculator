# Verificación en dispositivo

Comprobado el 2026-11-23 en el teléfono con Android (pantalla 1260x2800 px),
usando la pantalla de fondo de Buddy.

| Prueba | Resultado observado |
| --- | --- |
| Instalación del APK v1.1 | `com.buddyapps.anicalc`, versionCode 2, confirmada por el usuario |
| Interfaz de la calculadora | Rejilla 4x5 completa, título, historial, display y pie de página sin solapes |
| Operación 7 + 3 = | Display `10`, historial `7 + 3 =` |
| Pestaña 🌸 Anime | Panel con progreso, notas, actualizaciones y enlace a GitHub visible |
| Progreso (12 totales, 5 vistos, 24 min) | `Vistos: 2 h 0 min`, `Restante: 2 h 48 min`, `Progreso: 41,7%` |
| Promedio (8.5, 9, 7.5) | `Promedio: 8,33 (3 notas)` con mensaje de ánimo |
| Buscar actualización | Diálogo `Ya tienes la última versión (1.1) ✿` (consulta a GitHub Releases OK) |
| Release pública | `v1.1` es la última, con `AnimeCalc-v1.1.apk` como asset |

Notas: los decimales se muestran con coma por el locale del dispositivo.
La comprobación de actualizaciones necesita red; si falla, muestra un aviso y
la calculadora sigue funcionando sin conexión.
