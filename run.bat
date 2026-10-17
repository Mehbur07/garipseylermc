@echo off
rem Builds Garip Seyler MC, installs the jar into the "Garip Seyler Test" MehburMC instance and opens the launcher.
chcp 65001 >nul
cd /d "%~dp0"

set "MODS=%APPDATA%\MehburMC\game\mc\instances\garip-seyler-test-0b41b8\mods"
set "LAUNCHER=%APPDATA%\MehburMC\app\MehburMC Launcher.exe"

echo [1/3] Derleniyor...
call gradlew.bat build
if errorlevel 1 (
	echo Derleme basarisiz!
	pause
	exit /b 1
)

echo [2/3] Test profiline kopyalaniyor: %MODS%
if not exist "%MODS%" mkdir "%MODS%"
del /q "%MODS%\garipseylermc-*.jar" 2>nul
copy /y "build\libs\garipseylermc-*.jar" "%MODS%\" >nul
if not exist "%MODS%\fabric-api-*.jar" echo UYARI: Fabric API test profilinde yok! Modrinth'ten fabric-api 26.2 surumunu mods klasorune koy.
if not exist "%MODS%\geckolib-*.jar" (
	echo GeckoLib indiriliyor...
	curl -sL -o "%MODS%\geckolib-fabric-26.2-5.5.5.jar" "https://cdn.modrinth.com/data/8BmcQJ2H/versions/7gaQHok7/geckolib-fabric-26.2-5.5.5.jar"
)

echo [3/3] MehburMC Launcher aciliyor - "Garip Seyler Test" profilini sec.
start "" "%LAUNCHER%"
