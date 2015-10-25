import System.Directory
import System.FilePath.Windows
import Data.Char
import System.Console.ArgParser
import System.Console.ArgParser.Parser

import Harmonie.Common.DbAccess.SQL
import Harmonie.Common.DbAccess
import Harmonie.Common.IO
import Harmonie.Main.ProcessPartCommand

data Command = ProcessPart String Int deriving Show

cmdLineParser :: IO (CmdLnInterface Command)
cmdLineParser = mkSubParser
    [
        --("dump", mkDefaultApp (DumpExamples `parsedBy` optPos "examples.txt" "file name") "dump"),
        ("part", mkDefaultApp (ProcessPart `parsedBy` reqPos "text name" `andBy` reqPos "part number") "part")
    ]

run :: DbAccess db => db -> Command -> IO ()
run db (ProcessPart textName partNumber) = processPart db textName partNumber


main = do
    interface <- cmdLineParser
    runApp interface runWithDb where
        runWithDb cmd = do
            conn <- connect "..\\Data\\db" :: IO SQLiteSimpleDb
            run conn cmd
            disconnect conn