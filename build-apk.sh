#!/bin/bash
# ============================================
# ExamIA - Script de compilación APK
# Ejecutar: chmod +x build-apk.sh && ./build-apk.sh
# ============================================

set -e

ANDROID_HOME="$HOME/android-sdk"
CMDTOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "============================================"
echo "  ExamIA - Compilador APK para Android 12+"
echo "============================================"
echo ""

# 1. Verificar Java
echo "[1/6] Verificando Java..."
if ! command -v java &> /dev/null; then
    echo "  ERROR: Java no encontrado. Instala OpenJDK 17:"
    echo "  sudo dnf install java-17-openjdk-devel"
    exit 1
fi
java -version 2>&1 | head -1
echo "  OK"

# 2. Descargar Android SDK si no existe
echo "[2/6] Verificando Android SDK..."
if [ ! -d "$ANDROID_HOME/cmdline-tools" ]; then
    echo "  Descargando Android SDK command-line tools..."
    mkdir -p /tmp/android-sdk-dl
    curl -L --progress-bar -o /tmp/android-sdk-dl/cmdtools.zip "$CMDTOOLS_URL"
    
    echo "  Instalando SDK..."
    mkdir -p "$ANDROID_HOME/cmdline-tools"
    cd /tmp/android-sdk-dl
    unzip -q cmdtools.zip
    mv cmdline-tools "$ANDROID_HOME/cmdline-tools/latest"
    rm -rf /tmp/android-sdk-dl
    echo "  SDK instalado en $ANDROID_HOME"
else
    echo "  SDK ya instalado"
fi

export ANDROID_HOME="$ANDROID_HOME"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

# 3. Aceptar licencias e instalar componentes
echo "[3/6] Instalando componentes del SDK..."
yes | sdkmanager --licenses > /dev/null 2>&1 || true
sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools" 2>&1 | grep -v "Warning"

# 4. Configurar proyecto
echo "[4/6] Configurando proyecto..."
cd "$PROJECT_DIR"

# 5. Compilar APK
echo "[5/6] Compilando APK..."
chmod +x gradlew
./gradlew assembleDebug

# 6. Copiar APK resultante
echo "[6/6] Copiando APK..."
APK_PATH="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    cp "$APK_PATH" "$PROJECT_DIR/ExamIA.apk"
    echo ""
    echo "============================================"
    echo "  APK GENERADO EXITOSAMENTE!"
    echo "============================================"
    echo "  Ubicación: $PROJECT_DIR/ExamIA.apk"
    echo "  Tamaño: $(du -h "$PROJECT_DIR/ExamIA.apk" | cut -f1)"
    echo ""
    echo "  Para instalar en tu celular:"
    echo "  1. Transfiere el archivo ExamIA.apk"
    echo "  2. En Android, habilita 'Fuentes desconocidas'"
    echo "  3. Abre el archivo para instalar"
    echo "============================================"
else
    echo "  ERROR: No se generó el APK"
    exit 1
fi
