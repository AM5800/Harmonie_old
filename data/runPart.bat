set partNumber=%2
set /A nextPartNumber=partNumber+1

if not exist bin mkdir bin

ghc -o bin\%1_%2.exe Texts\%1\hs\%2.hs  -i"..\textProcessor\src\main\haskell" -odir bin -hidir bin -O -Werror -main-is %1_%2.main -prof -auto-all -caf-all -fforce-recomp
if not errorlevel 1 (
    .\bin\%1_%2.exe 1e6 +RTS -p
)