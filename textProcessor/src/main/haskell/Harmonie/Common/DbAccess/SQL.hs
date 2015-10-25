{-# LANGUAGE OverloadedStrings #-}

module Harmonie.Common.DbAccess.SQL
    (
        isIgnored,
        ignore,
        getNormalForms,
        addNormalForm,
        connect,
        disconnect,
        SQLiteSimpleDb
    ) where

import Database.SQLite.Simple
import Data.Int
import Control.Monad
import Data.List
import Data.Maybe
import System.FilePath
import qualified Data.Text as T

import Harmonie.Common.DbAccess
import Harmonie.Common.PartOfSpeech
import Harmonie.Common.NormalizedString
import Harmonie.Common.Word
import Harmonie.Common.IO
import Harmonie.Common.Trim
import Harmonie.Common.Split
import Harmonie.Common.TextPart


decodeMeanings :: String -> [String]
decodeMeanings = map trim . split (=='|')


encodeMeanings :: [String] -> String
encodeMeanings = unwords . intersperse "|" . map trim


encodeRanges :: [Range] -> String
encodeRanges = unwords . intersperse "|" . map (\(Range s l) -> show s ++ "," ++ show l)


tryGetId :: Connection -> WordId -> IO (Maybe Int64)
tryGetId conn (WordId nf Noun (Just gender)) = do
    qr <- query conn "SELECT id FROM germanWords WHERE word = ? AND partOfSpeech = ? AND gender = ?"
        (toString nf, show Noun, show gender) :: IO [Only Int64]
    return $ singleToMaybe . map fromOnly $ qr
tryGetId _ (WordId nf Noun Nothing) = error $ "Noun without a gender: " ++ (toString nf)
tryGetId conn (WordId nf pos Nothing) = do
    qr <- query conn "SELECT id FROM germanWords WHERE word = ? AND partOfSpeech = ?"
        (toString nf, show pos) :: IO [Only Int64]
    return $ singleToMaybe . map fromOnly $ qr
tryGetId _ (WordId nf _ (Just _)) = error $ "Non noun with gender: "++ (toString nf)


createId :: Connection -> WordId -> IO Int64
createId conn (WordId nf pos gender) = do
    execute conn "INSERT INTO germanWords(word, partOfSpeech, gender) VALUES(?, ?, ?)"
        (toString nf, show pos, fmap show gender)
    lastInsertRowId conn


getOrCreateId :: Connection -> WordId -> IO Int64
getOrCreateId conn wordId = do
    maybeId <- tryGetId conn wordId
    returnId maybeId where
        returnId Nothing = createId conn wordId
        returnId (Just dbId) = return dbId


singleToMaybe :: Show a => [a] -> Maybe a
singleToMaybe (a:[]) = Just a
singleToMaybe [] = Nothing
singleToMaybe (a:_) = error $ "Value is not single: " ++ show a


single :: Show a => [a] -> a
single (a:[]) = a
single (a:_) = error $ "Value is not single: " ++ show a
single [] = error "Empty list"


newtype SQLiteSimpleDb = SQLiteSimpleDb Connection

tc :: SQLiteSimpleDb -> Connection
tc (SQLiteSimpleDb conn) = conn


getOrCreatePartId :: Connection -> TextPart -> IO Int64
getOrCreatePartId conn (TextPart text name index) = do
    qr <- query conn "SELECT id, partText FROM textParts WHERE textId = ? AND partNumber = ?"
        (name, index) :: IO [(Int64, String)]
    let maybeResult = singleToMaybe qr
    if isNothing maybeResult then do
        execute conn "INSERT INTO textParts (partText, textId, partNumber) VALUES (?, ?, ?)"
            (text, name, index)
        lastInsertRowId conn
    else do
        let (savedId, savedText) = fromJust maybeResult
        when (savedText /= text) $ error $ "Text " ++ name ++ show index ++ "has changed. DB is inconsistent!"
        return savedId


getWordIdFromDbId :: Connection -> Int64 -> IO WordId
getWordIdFromDbId conn dbId = do
    qr <- query conn "SELECT word, partOfSpeech, gender FROM germanWords WHERE id = ?"
        (Only dbId) :: IO [(String, String, Maybe String)]
    let makeId (text, pos, gender) = WordId (toNormalizedString text) (read pos) (fmap read gender)
    return $ makeId $ single qr


instance DbAccess SQLiteSimpleDb where
    isIgnored conn nf = do
        qr <- query (tc conn) "SELECT * FROM ignoredWords WHERE ignoredWord = ?" (Only $ toString nf) :: IO [Only String]
        return $ not . null $ qr


    ignore conn src = execute (tc conn) "INSERT OR IGNORE INTO ignoredWords VALUES(?)" (Only $ toString src)


    connect folder = do
        let dbPath = combine folder "Harmonie.db"
        conn <- open dbPath
        let initSql = combine folder "init_db.sql"
        contents <- readFileUtf8 initSql
        let queries = map Query . T.lines . T.pack $ contents
        forM_ queries (execute_ conn)
        return $ SQLiteSimpleDb conn


    disconnect (SQLiteSimpleDb conn)= close conn


    getNormalForms conn baseForm = do
        let db = tc conn
        qr <- query db "SELECT wordId FROM germanNormalForms WHERE baseForm = ?"
            (Only $ toString baseForm) :: IO [Only Int64]
        mapM (getWordIdFromDbId db) $ map fromOnly qr


    addNormalForm conn bf wordId = do
        dbId <- getOrCreateId (tc conn) wordId
        execute (tc conn) "INSERT OR IGNORE INTO germanNormalForms(baseForm, wordId) VALUES (?, ?)" (toString bf, dbId)


    addExample conn wordId textPart meanings ranges = do
        let db = tc conn
        dbId <- getOrCreateId db wordId
        textId <- getOrCreatePartId db textPart
        execute db "INSERT OR IGNORE INTO germanExamples(partId, wordId, ranges, meanings) VALUES (?, ?, ?, ?)"
            (textId, dbId, encodeRanges ranges, encodeMeanings meanings)


    getMeanings conn wordId = do
        maybeDbId <- tryGetId (tc conn) wordId
        if isNothing maybeDbId then return []
        else do
            let dbId = fromJust maybeDbId
            qr <- query (tc conn) "SELECT meanings FROM germanExamples WHERE wordId = ?" (Only dbId) :: IO [Only String]
            let meanings = nub . concat . map decodeMeanings . map fromOnly $ qr
            return meanings

    addAffix conn verbId prep resultId = do
        let db = tc conn
        verbDbId <- getOrCreateId db verbId
        resultDbId <- getOrCreateId db resultId
        execute db "INSERT OR IGNORE INTO knownAffixes (verbId, prepositionId, wordId) VALUES (?, ?, ?)"
            (verbDbId, toString prep, resultDbId)


    getAffix conn verbId prep = do
        let db = tc conn
        maybeVerb <- tryGetId db verbId
        doRead db maybeVerb where
            doRead db (Just verb) = do
                qr <- query db "SELECT wordId FROM knownAffixes WHERE verbId = ? AND prepositionId = ?"
                    (verb, toString prep) :: IO [Only Int64]
                let singleQr = singleToMaybe qr
                if isNothing singleQr then return Nothing
                else do
                    let dbId = fromOnly $ fromJust singleQr
                    resultId <- getWordIdFromDbId db dbId
                    return (Just resultId)
            doRead _ _ = return Nothing



