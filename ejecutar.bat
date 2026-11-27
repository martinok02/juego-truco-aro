@echo off
pushd "%~dp0"
if errorlevel 1 exit /b 1

call "%~dp0compilar.bat"
if errorlevel 1 (
    popd
    exit /b 1
)

set "JAVA_CMD=java"
if exist "C:\Program Files\Eclipse Adoptium\jdk-25.0.5.10002-hotspot\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Eclipse Adoptium\jdk-25.0.5.10002-hotspot\bin\java.exe"
)

if "%~1"=="" (
    "%JAVA_CMD%" -cp out juego.Main
) else (
    "%JAVA_CMD%" -cp out %1
)
set "RESULTADO=%ERRORLEVEL%"
popd
exit /b %RESULTADO%
