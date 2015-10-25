module Harmonie.Common.Word where

import Harmonie.Common.PartOfSpeech
import Harmonie.Common.Gender
import Harmonie.Common.NormalizedString


data WordId = WordId NormalizedString PartOfSpeech (Maybe Gender) deriving (Show, Eq)


createWordId :: String -> PartOfSpeech -> Maybe Gender -> WordId
createWordId nf pos gender = WordId (toNormalizedString nf) pos gender


data Word = Word WordId [String] deriving Show


getWordId :: Word -> WordId
getWordId (Word wordId _) = wordId


getWordMeanings :: Word -> [String]
getWordMeanings (Word _ meanings) = meanings
