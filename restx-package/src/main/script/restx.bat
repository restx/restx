:begin
@echo off

setlocal enabledelayedexpansion

:start
if exist "%~dp0upgrade.bat" (
    cmd /c "%~dp0upgrade.bat"
    del "%~dp0upgrade.bat"
)

call "%~dp0launch.bat"

if exist "%~dp0.restart" (
     del "%~dp0.restart"
     goto start
)

endlocal