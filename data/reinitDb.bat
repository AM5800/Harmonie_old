@echo off

del db\Harmonie.db
if exist db\Harmonie.db goto :cantDelete

for /f %%d in ('dir /b Texts\') do (
	for /f %%f in ('dir /b Texts\%%d\hs') do (
        echo Processing %%d\%%~nf...
		call runPart.bat %%d %%~nf
		if errorlevel 1 goto :partError
	)
)

copy db\Harmonie.db ..\harmonie\src\main\assets\Harmonie.db
echo Database created successfully
goto :end

:cantDelete
eche Error deleting database
goto :end

:partError
echo Error processing part
goto :end

:end

