@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Android\Android Studio Narwhal\jbr"
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo JAVA_HOME not found: %JAVA_HOME%
  echo Edit build.bat / gradle.properties androidStudioPath to match your Android Studio install.
  exit /b 1
)
call "%~dp0gradlew.bat" buildPlugin %*
echo.
echo Plugin zip:
dir /b "%~dp0build\distributions\*.zip"
endlocal
