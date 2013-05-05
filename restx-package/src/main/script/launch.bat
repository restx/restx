:begin
@echo off

setlocal enabledelayedexpansion

java -Drestx.shell.home="%~dp0." -cp "%~dp0lib\*;%~dp0plugins\*;." restx.shell.RestxShell %*

endlocal