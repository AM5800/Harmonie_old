del db\Harmonie.db
if exist db\Harmonie.db goto :fail

for /f %%d in ('dir /b Texts\') do (
	for /f %%f in ('dir /b Texts\%%d\hs') do (
		call runPart.bat %%d %%~nf
		if errorlevel 1 goto :fail
	)
)

copy db\Harmonie.db ..\harmonie\src\main\assets\Harmonie.db
goto :success

:fail
echo "Something went wrong"

:success

