@echo off
set partNumber=%2
set /A nextPartNumber=partNumber+1

call runPart.bat %1 %partNumber%
if not errorlevel 1 (
    copy db\Harmonie.db ..\harmonie\src\main\assets\Harmonie.db
    ..\textProcessor\dist\build\textProcessor\textProcessor.exe part %1 %nextPartNumber%
    if not errorlevel 1 ("C:\Program Files (x86)\Notepad++\notepad++.exe" Texts\%1\hs\%nextPartNumber%.hs)
)
