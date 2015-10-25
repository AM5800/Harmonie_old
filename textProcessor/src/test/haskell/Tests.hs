import Test.Tasty
import Test.Tasty.HUnit

import SplitterTests
import TrimTests


main :: IO ()
main = defaultMain tests


tests :: TestTree
tests = testGroup "Tests" [ splitterTests, trimTests ]