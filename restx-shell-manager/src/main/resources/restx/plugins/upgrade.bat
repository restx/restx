:begin
@echo off

setlocal enabledelayedexpansion

echo "upgrading RESTX...."

rmdir "%~dp0lib" /s /q

set cur=%cd%
cd "%~dp0."
jar xf upgrade.zip
cd %cur%

del "%~dp0upgrade.zip"

endlocal