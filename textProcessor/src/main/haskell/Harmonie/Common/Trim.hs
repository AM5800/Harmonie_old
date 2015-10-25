module Harmonie.Common.Trim where

import Data.Char

trim :: String -> String
trim = reverse . cleanup . reverse . cleanup where
    cleanup = dropWhile isSpace

