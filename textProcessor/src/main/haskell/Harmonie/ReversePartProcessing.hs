module Harmonie.ReversePartProcessing where

import Control.Monad
import Data.Int
import Data.List
import Data.Maybe
import Data.Char
import qualified Data.IntMap as IntMap

import Harmonie.Common.DbAccess
import Harmonie.Common.DbAccess.SQL
import Harmonie.Common.Gender
import qualified Harmonie.Common.PartOfSpeech as H
import Harmonie.Common.NormalizedString
import Harmonie.Common.Word
import Harmonie.Common.Split
import Harmonie.Common.TextPart
import Harmonie.Common.Substring


data TextPartEntity =
    Unknown-- For later reprocessing
    | Ignore -- Ignore and add to ignore list
    | Skip -- Ignore but not add to ignore list
    | Preposition -- Ignored now, but may be useful in future
    | Article -- Ignored now, but may be useful in future
    | Conjunction -- Ignored now, but may be useful in future
    | Pronoun -- Ignored now, but may be useful in future
    | Noun String Gender [String]
    | Verb String [String]
    | Adjective String [String]
    | Adverb [String]
    | Compound [(String, TextPartEntity)]
    | AffixVerb String
    | AffixPreposition Int String [String]
    deriving Show


data WordData =
    WordData
        String -- Source form
        Int -- Word number
        Int -- Word index
        TextPartEntity
    deriving Show

-- shortenings----------------------------------
nounF :: String -> String -> TextPartEntity
nounF nf m = Noun nf Feminine [m]

nounM :: String -> String -> TextPartEntity
nounM nf m = Noun nf Masculine [m]

nounN :: String -> String -> TextPartEntity
nounN nf m = Noun nf Neuter [m]

nounF2 :: String ->String ->String -> TextPartEntity
nounF2 nf m1 m2 = Noun nf Feminine [m1, m2]

nounM2 :: String ->String ->String -> TextPartEntity
nounM2 nf m1 m2 = Noun nf Masculine [m1, m2]

nounN2 :: String ->String ->String -> TextPartEntity
nounN2 nf m1 m2 = Noun nf Neuter [m1, m2]
------------------------------------------------

processCompound
    :: String -- Source
    -> Int -- Word number
    -> Int -- Word index
    -> (String, TextPartEntity) -- CompoundPart
    -> WordData
processCompound src number index (partSrc, tpe) = WordData partSrc number (index + check startIndex) tpe where
    startIndex = indexOf partSrc src
    check (-1) = error $ "Part " ++ partSrc ++ " not found in compound: " ++ src
    check x = x


getWordDatas :: Int -> [(String, Int)] -> (Int -> String -> TextPartEntity) -> [WordData]
getWordDatas _ [] _ = []
getWordDatas n ((c:[], i):words) f = getWordDatas n words f -- ignore one char words
getWordDatas n ((src, index):words) f = do
    let part = f n src
    proceed part ++ getWordDatas (n + 1) words f where
        proceed (Compound xs) = map (processCompound src n index) xs
        proceed part = [WordData src n index part]


processPart :: TextPart -> (Int -> String -> TextPartEntity) -> IO ()
processPart tp@(TextPart text _ _) w = do
    let splitted = splitPart text
    db <- connect "db" :: IO SQLiteSimpleDb
    let wordDatas = getWordDatas 1 splitted w
    mapM_ (processWordData db tp) wordDatas
    processAffixes db tp wordDatas
    disconnect db


processExampleless :: DbAccess db => db -> String -> H.PartOfSpeech -> IO ()
processExampleless db src pos = do
    let nf = toNormalizedString src
    let wordId = WordId nf pos Nothing
    addNormalForm db nf wordId


process :: DbAccess db => db
    -> TextPart
    -> String -- Source form
    -> String -- Normal form
    -> Int -- Word index
    -> H.PartOfSpeech
    -> Maybe Gender
    -> [String] -- Meanings
    -> IO ()
process db textPart src normalForm index pos maybeGender meanings = do
    let nf = toNormalizedString normalForm
    let wordId = WordId nf pos maybeGender
    addNormalForm db (toNormalizedString src) wordId
    addNormalForm db nf wordId
    let wordLen = length src
    addExample db wordId textPart meanings [Range (index-1) wordLen]


processWordData :: DbAccess db => db -> TextPart -> WordData -> IO ()
processWordData db _ (WordData src _ _ Ignore) = ignore db $ toNormalizedString src
processWordData db _ (WordData _ _ _ Skip) = return ()
processWordData _ _ (WordData _ _ _ (Compound _)) = error "Compound not expected here"
processWordData db _ (WordData _ _ _ Unknown) = return ()
processWordData db _ (WordData _ _ _ (AffixVerb _)) = return ()
processWordData db _ (WordData _ _ _ (AffixPreposition _ _ _)) = return ()
processWordData db _ (WordData src _ _ Preposition) = processExampleless db src H.Preposition
processWordData db _ (WordData src _ _ Pronoun) = processExampleless db src H.Pronoun
processWordData db _ (WordData src _ _ Conjunction) = processExampleless db src H.Conjunction
processWordData db _ (WordData src _ _ Article) = processExampleless db src H.Article
processWordData db textPart (WordData src _ index (Noun nf gender meanings)) =
    process db textPart src nf index H.Noun (Just gender) meanings
processWordData db textPart (WordData src _ index (Verb nf meanings)) =
    process db textPart src nf index H.Verb Nothing meanings
processWordData db textPart (WordData src _ index (Adverb meanings)) =
    process db textPart src src index H.Adverb Nothing meanings
processWordData db textPart (WordData src _ index (Adjective nf meanings)) =
    process db textPart src nf index H.Adjective Nothing meanings


processAffixes :: DbAccess db => db -> TextPart -> [WordData] -> IO ()
processAffixes db textPart wordDatas = mapM_ processPreps wordDatas where
    processPreps (WordData src _ index (AffixPreposition verbIndex nf meanings)) = do
        verb <- findVerb verbIndex wordDatas
        let prep = toNormalizedString src
        let resultId = WordId (toNormalizedString nf) H.Verb Nothing
        addAffix db (fst verb) prep resultId
        addExample db resultId textPart meanings [snd verb, Range (index-1) (length src)] where
            findVerb n ((WordData verbSrc verbN verbIndex (AffixVerb verbNf)):xs)
                | n == verbN = do
                    let verbNf' = toNormalizedString verbNf
                    let verbSrc' = toNormalizedString verbSrc
                    let verbId = WordId verbNf' H.Verb Nothing
                    addNormalForm db verbSrc' verbId
                    addNormalForm db verbNf' verbId
                    return (verbId, Range (verbIndex-1) (length verbSrc))
                | otherwise = findVerb n xs
            findVerb n ((WordData _ _ _ _):xs) = findVerb n xs
            findVerb n [] = error $ "Affix verb not found. Index = " ++ show n
    processPreps _ = return ()




