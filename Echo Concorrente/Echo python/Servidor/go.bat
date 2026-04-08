@echo off

set Command=python ServidorEcho.py --port 12347

echo %Command%
%Command%

pause