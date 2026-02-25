#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT_DIR="${ROOT_DIR}/build/tasks/_web-client_linkWasmJs"
HOST="${HOST:-127.0.0.1}"
PORT="${1:-9391}"

SKIKO_RUNTIME_BASE="${HOME}/.cache/JetBrains/Amper/.m2.cache/org/jetbrains/skiko/skiko-js-wasm-runtime"
SKIKO_JAR="$(find "${SKIKO_RUNTIME_BASE}" -maxdepth 2 -type f -name 'skiko-js-wasm-runtime-*.jar' | sort -V | tail -n 1)"

if [[ -z "${SKIKO_JAR}" ]]; then
  echo "ERROR: skiko runtime jar not found under ${SKIKO_RUNTIME_BASE}" >&2
  echo "Please run: ./amper task :web-client:linkWasmJs" >&2
  exit 1
fi

echo "Building web-client wasm artifacts..."
"${ROOT_DIR}/amper" task :web-client:linkWasmJs

echo "Preparing runtime assets in ${OUT_DIR}..."
mkdir -p "${OUT_DIR}"
unzip -jo "${SKIKO_JAR}" skiko.mjs skiko.wasm js-reexport-symbols.mjs -d "${OUT_DIR}" >/dev/null

cat > "${OUT_DIR}/index.html" <<'EOF'
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>web-client</title>
  <style>
    html, body { margin: 0; width: 100%; height: 100%; }
    body { overflow: hidden; background: #0e1014; }
  </style>
  <script type="importmap">
    {
      "imports": {
        "@js-joda/core": "https://esm.sh/@js-joda/core@5.6.5"
      }
    }
  </script>
</head>
<body>
<script type="module">
  import { _initialize } from './web-client.mjs';
  await _initialize();
</script>
</body>
</html>
EOF

echo "Serving ${OUT_DIR} at http://${HOST}:${PORT}"
cd "${OUT_DIR}"
python3 -m http.server "${PORT}" --bind "${HOST}"
