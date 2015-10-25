module Harmonie.Common.NormalizedString
    (
        NormalizedString,
        toNormalizedString,
        toString
    ) where

import Data.Char

import Harmonie.Common.Trim

newtype NormalizedString = NormalizedString String deriving (Show, Eq)

toNormalizedString :: String -> NormalizedString
toNormalizedString text = NormalizedString (trim . map toLower $ text)

toString :: NormalizedString -> String
toString (NormalizedString text) = text

