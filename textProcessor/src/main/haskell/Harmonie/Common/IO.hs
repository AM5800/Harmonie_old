module Harmonie.Common.IO where

import System.IO

readFileUtf8 :: FilePath -> IO String
readFileUtf8 path = do
    h <- openFile path ReadMode
    hSetEncoding h utf8
    s <- hGetContents h
    return s

writeFileUtf8 :: FilePath -> String -> IO ()
writeFileUtf8 path text = do
    h <- openFile path WriteMode
    hSetEncoding h utf8
    hPutStr h text
    hClose h
