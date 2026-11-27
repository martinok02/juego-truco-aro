@echo off
set "JAVAC_CMD=javac"
if exist "C:\Program Files\Eclipse Adoptium\jdk-25.0.5.10002-hotspot\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\Eclipse Adoptium\jdk-25.0.5.10002-hotspot\bin\javac.exe"
)

if exist out rmdir /s /q out
mkdir out

rem Lista de fuentes con rutas RELATIVAS, entre comillas y con "/".
rem Asi javac no se rompe si la carpeta del proyecto tiene espacios
rem o tildes en el nombre (por ejemplo la carpeta "Segundo ano").
setlocal enabledelayedexpansion
set "BASE=%CD%\"
(for /r src %%f in (*.java) do (
    set "RUTA=%%f"
    set "RUTA=!RUTA:%BASE%=!"
    echo "!RUTA:\=/!"
)) > fuentes.txt
endlocal

"%JAVAC_CMD%" -encoding UTF-8 -d out @fuentes.txt
set "RESULTADO=%ERRORLEVEL%"
del fuentes.txt
if %RESULTADO% equ 0 (
    echo ========================================
    echo   COMPILACION EXITOSA DE TODOS LOS FUENTES
    echo ========================================
)
exit /b %RESULTADO%
