module Harmonie.Common.TextPart where

data Range = Range
    Int -- Start
    Int -- Length
        deriving (Show, Eq, Read)


data TextPart = TextPart
    String -- Text
    String -- Text Name
    Int -- Part Index
        deriving (Show, Eq)

