:begin
@echo off

setlocal enabledelayedexpansion

IF "%JAVA_OPTS%"=="" (
  SET JAVA_OPTS=
)
IF "%RESTX_OPTS%"=="" (
  SET RESTX_OPTS=
)

java %JAVA_OPTS% %RESTX_OPTS% -Drestx.shell.home="%~dp0." -cp "%~dp0lib\*;%~dp0plugins\*;." restx.shell.RestxShell %*

endlocal