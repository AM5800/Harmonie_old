module Harmonie.Main.Paths where

import System.FilePath
import System.Environment


findRootDirectory :: IO FilePath
findRootDirectory = do
    exec <- getExecutablePath
    let parts = splitPath exec
    let result = take (length parts - 5) parts
    return $ joinPath result


getAssetsDirectory :: IO FilePath
getAssetsDirectory = do
    root <- findRootDirectory
    return $ combine root "androidApp\\src\\main\\assets"


getTextPartOutputPath :: String -> Int -> String -> IO FilePath
getTextPartOutputPath textName partIndex ext = do
    root <- findRootDirectory
    let partName = show partIndex ++ "." ++ ext
    let result = combine root "data\\Texts\\" ++ textName ++ "\\hs\\" ++ partName
    return result

getAssetTextPartPath :: String -> Int -> String -> IO FilePath
getAssetTextPartPath text number ext = do
    assetsDir <- getAssetsDirectory
    let partName = show number ++ "." ++ ext
    let result = combine assetsDir "exercises\\texts\\" ++ text ++ "\\" ++ partName
    return result