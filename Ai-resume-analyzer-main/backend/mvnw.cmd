@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup script for Windows (only-script type).
@REM Downloads Apache Maven on first use, then delegates to it.
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "WRAPPER_PROPS=%SCRIPT_DIR%.mvn\wrapper\maven-wrapper.properties"

if not exist "%WRAPPER_PROPS%" (
  echo Cannot find %WRAPPER_PROPS%
  exit /b 1
)

for /f "tokens=1,* delims==" %%A in ('findstr /b "distributionUrl=" "%WRAPPER_PROPS%"') do set "DIST_URL=%%B"

if "%DIST_URL%"=="" (
  echo distributionUrl not set in maven-wrapper.properties
  exit /b 1
)

for %%F in ("%DIST_URL%") do set "ZIP_NAME=%%~nxF"
set "DIST_NAME=%ZIP_NAME:-bin.zip=%"
set "DIST_NAME=%DIST_NAME:.zip=%"

if "%MAVEN_USER_HOME%"=="" set "MAVEN_USER_HOME=%USERPROFILE%\.m2"
set "MVN_BASE=%MAVEN_USER_HOME%\wrapper\dists"
set "MAVEN_HOME=%MVN_BASE%\%DIST_NAME%"

if not exist "%MAVEN_HOME%\bin" (
  echo Downloading Apache Maven from %DIST_URL% ...
  if not exist "%MVN_BASE%" mkdir "%MVN_BASE%"
  powershell -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%MVN_BASE%\maven.zip'"
  powershell -Command "Expand-Archive -Path '%MVN_BASE%\maven.zip' -DestinationPath '%MVN_BASE%' -Force"
  for /d %%D in ("%MVN_BASE%\apache-maven-*") do move "%%D" "%MAVEN_HOME%" >nul
  del "%MVN_BASE%\maven.zip"
)

call "%MAVEN_HOME%\bin\mvn.cmd" %*
endlocal
