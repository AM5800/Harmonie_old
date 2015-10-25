module Harmonie.Common.Replace where

import qualified Data.Text as T

-- TODO: rewrite or use library
replace :: String -> String -> String -> String
replace needle replacement haystack = T.unpack $ T.replace (T.pack needle) (T.pack replacement) (T.pack haystack)
