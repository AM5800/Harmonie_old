module Harmonie.Main.ProcessPartCommand where

import Data.List
import System.Directory
import System.FilePath.Windows
import qualified Data.Text as T

import Harmonie.Common.PartOfSpeech
import Harmonie.Common.DbAccess
import Harmonie.Common.NormalizedString
import Harmonie.Common.Split
import Harmonie.Common.Word
import Harmonie.Common.Replace
import Harmonie.Common.IO
import Harmonie.Common.Trim
import Harmonie.Main.Paths
import Harmonie.Common.TextPart


pattern :: String
pattern = "module #MODULE# where\n\
\import Harmonie.ReversePartProcessing\n\
\import Harmonie.Common.TextPart\n\
\import Harmonie.Common.Gender\n\n\
\#TEXT#\n\n#WORDS#\
\w n s = error $ \"Could not match word \" ++ s ++ \" with index \" ++ show n\n\n\
\main = processPart source w"


mkPart
    :: String -- Words
    -> String -- Text
    -> String -- Module name
    -> String
mkPart w source moduleName = replace "#MODULE#" moduleName $ replace "#TEXT#" source $ replace "#WORDS#" w pattern -- TODO: try template haskell


presentText :: TextPart -> String
presentText (TextPart text name index) = result where
    escapedText = replace "\"" "\\\"" text
    quoteLine line = concat  ["\\", line, "\\n\\"]
    quotedLines = map quoteLine $ lines escapedText
    result = "source = TextPart \"\\\n" ++ unlines quotedLines ++ "\\\" \"" ++ name ++ "\" " ++ show index


presentLeft :: String -> Int -> String
presentLeft word index = "w " ++ show index ++ " \"" ++ word ++ "\" = "


presentRight :: WordId -> [String] -> String
presentRight (WordId nf Noun (Just gender)) meanings =
    concat ["Noun \"", toString nf, "\" ", show gender, " ", presentMeanings meanings]
presentRight (WordId nf Adjective Nothing) meanings =
    concat ["Adjective \"", toString nf, "\" ", presentMeanings meanings]
presentRight (WordId _ Adverb Nothing) meanings =
    concat ["Adverb ", presentMeanings meanings]
presentRight (WordId _ Article Nothing) _ = "Article"
presentRight (WordId _ Preposition Nothing) _ = "Preposition"
presentRight (WordId _ Conjunction Nothing) _ = "Conjunction"
presentRight (WordId _ Pronoun Nothing) _ = "Pronoun"
presentRight (WordId nf pos Nothing) meanings =
    concat [show pos, " \"", toString nf, "\" ", presentMeanings meanings]
presentRight (WordId nf _ (Just _)) _ = error $ "word " ++ (toString nf) ++ " can not have gender"


data WordData = WordData Word | Ignore | Unknown deriving Show


presentMeanings :: [String] -> String
presentMeanings [] = []
presentMeanings meanings = concat ["[", concat $ intersperse ", " quotedMeanings, "]"] where
    quotedMeanings = map (\m -> concat ["\"", m, "\""]) meanings

-- TODO: refactor ASAP
getWords :: DbAccess db => db -> Int -> [String] -> IO [(String, Int, WordData)]
getWords _ _ [] = return []
getWords db n ((_:[]):xs) = getWords db n xs
getWords db n (x:xs) = do
    let nf = toNormalizedString x
    shouldIgnore <- isIgnored db nf
    if shouldIgnore then do
        rest <- getWords db (n+1) xs
        return $ (x, n, Ignore) : rest
    else do
        wordIds <- getNormalForms db nf
        if null wordIds then do
            rest <- getWords db (n+1) xs
            return $ (x, n, Unknown) : rest
        else do
            maybeWords <- mapM findWord wordIds
            let result = map mkResult maybeWords
            rest <- getWords db (n+1) xs
            return $ result ++ rest where
                mkResult maybeWord = (x, n, maybeWord)
                findWord wordId = fmap wordFromMeanings $ getMeanings db wordId where
                    wordFromMeanings meanings = WordData $ Word wordId meanings


presentWord :: (String, Int, WordData) -> String
presentWord (s, i, Unknown) = presentLeft s i ++ "Unknown"
presentWord (s, i, Ignore) = presentLeft s i ++ "Ignore"
presentWord (s, i, WordData (Word wordId meanings)) = (presentLeft s i) ++ presentRight wordId meanings


processPart' :: DbAccess db => db -> TextPart -> IO String
processPart' db textPart@(TextPart text _ _) = do
    splittedWords <- getWords db 1 . map fst . splitPart $ text
    let presented = map presentWord splittedWords
    let source = mkPart (unlines presented) (presentText textPart) $ makeModuleName textPart
    return source


makeModuleName :: TextPart -> String
makeModuleName (TextPart _ name index) = name ++ "_" ++ show index


processPart :: DbAccess db => db -> String -> Int -> IO ()
processPart db textName number = do
    outFn <- getTextPartOutputPath textName number "hs"
    outFnExists <- doesFileExist outFn

    if not outFnExists then do
        partText <- getTextPartText textName number
        processedContents <- processPart' db (TextPart partText textName number)
        writeFileUtf8 outFn $ processedContents
    else putStrLn $ "File already exists: " ++ outFn


getTextPartText :: String -> Int -> IO String
getTextPartText textName requiredPart = do
    currentDir <- getCurrentDirectory
    let fullTextPath = combine currentDir "..\\Data\\Texts\\" ++ textName ++ "\\full.txt"
    content <- readFileUtf8 fullTextPath
    let splitted = T.splitOn (T.pack "<br>") (T.pack content)
    let result = trim $ T.unpack $ head $ drop (requiredPart-1) splitted
    return result








