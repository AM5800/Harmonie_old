{-# LANGUAGE DeriveDataTypeable #-}

module Harmonie.Common.PartOfSpeech where

import Data.Typeable

data PartOfSpeech = Noun | Verb | Adverb | Adjective | Conjunction | Article | Pronoun | Preposition | NonWord
    deriving (Show, Eq, Typeable, Read)

