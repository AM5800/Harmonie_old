{-# LANGUAGE DeriveDataTypeable #-}

module Harmonie.Common.Gender where

import Data.Typeable

data Gender = Masculine | Neuter | Feminine deriving (Show, Eq, Typeable, Read)
