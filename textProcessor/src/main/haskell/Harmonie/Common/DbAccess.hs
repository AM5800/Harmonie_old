module Harmonie.Common.DbAccess where

import Harmonie.Common.Word
import Harmonie.Common.NormalizedString
import Harmonie.Common.TextPart

class DbAccess db where
    connect :: FilePath -> IO db
    disconnect :: db -> IO ()

    isIgnored :: db -> NormalizedString -> IO Bool
    ignore :: db -> NormalizedString -> IO ()

    getMeanings :: db -> WordId -> IO [String]

    getNormalForms :: db -> NormalizedString -> IO [WordId]
    addNormalForm :: db -> NormalizedString -> WordId -> IO ()

    addExample :: db -> WordId -> TextPart -> [String] -> [Range] -> IO ()

    addAffix
        :: db
        -> WordId -- Verb
        -> NormalizedString -- Preposition
        -> WordId -- Result word
        -> IO ()

    getAffix
        :: db
        -> WordId -- Verb
        -> NormalizedString -- Preposition
        -> IO (Maybe WordId)


