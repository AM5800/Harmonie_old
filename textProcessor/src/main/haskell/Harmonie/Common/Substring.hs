module Harmonie.Common.Substring where

import Data.List

indexOf
    :: String -- Needle
    -> String -- Haystack
    -> Int
indexOf needle haystack = indexOf' 0 haystack where
    indexOf' _ [] = -1
    indexOf' i hs@(_:xs)
        | isPrefixOf needle hs = i
        | otherwise = indexOf' (i+1) xs

