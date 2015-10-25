module Harmonie.Common.Split
    (
        split, splitPart
    ) where

import Data.List
import Data.Char

split :: (Char -> Bool) -> String -> [String]
split _ [] = []
split f xs = (fst spanned) : (split f . drop 1 . snd $ spanned) where
    spanned = break f xs


isTrimNShow :: Char -> Bool
isTrimNShow '!' = True
isTrimNShow ',' = True
isTrimNShow '.' = True
isTrimNShow '?' = True
isTrimNShow _ = False


splitPart :: String -> [(String, Int)]
splitPart text = processFoldResult . snd $ foldl' doFold ([], []) $ zip (text++[' ']) [1..] where
    doFold (currentWord, acc) ch
        | isSpace $ fst ch = ([], acc ++ [currentWord])
        | otherwise = (currentWord ++ [ch], acc)
    processFoldResult chars = concat . map processWord . filter (not . null) $ chars where
        processWord (x:[]) = [([fst x], snd x)]
        processWord ch@(_:_) = result where
            trimmed = reverse $ dropWhile (isPunctuation . fst) ch
            splitRight (c:cs)
                | isTrimNShow $ fst c = [[c]] ++ (splitRight cs)
                | isAlpha $ fst c = [c:cs]
                | otherwise = splitRight cs
            splitRight [] = []
            splitted = reverse . map reverse $ splitRight trimmed
            result = map (\w -> (map fst w, snd $ head w)) splitted
        processWord [] = error "Collection should not be empty"

