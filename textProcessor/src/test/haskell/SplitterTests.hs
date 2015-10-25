module SplitterTests where

import Test.Tasty
import Test.Tasty.HUnit
import Harmonie.Common.Split


splitterTests :: TestTree
splitterTests = testGroup "SplitterTests"
    [
        testCase "text" $ testSplitter "text" ["text"],
        testCase "one  two" $ testSplitter "one  two" ["one", "two"],
        testCase "one, two" $ testSplitter "one, two" ["one", ",",  "two"],
        testCase "one!! two" $ testSplitter "one!! two" ["one", "!", "!", "two"],
        testCase "one ! two" $ testSplitter "one ! two" ["one", "!", "two"],
        testCase "one!" $ testSplitter "one!" ["one", "!"],
        testCase "!" $ testSplitter "!" ["!"],
        testCase "dash-dash" $ testSplitter "dash-dash" ["dash-dash"],
        testCase "this.is.sparta." $ testSplitter "this.is.sparta." ["this.is.sparta", "."],
        testCase "\"Quotes\"" $ testSplitter "\"Quotes\"" ["Quotes"],
        testCase "(Brackets)" $ testSplitter "(Brackets)" ["Brackets"]
    ]


testSplitter :: String -> [String] -> Assertion
testSplitter text expected = (map fst . splitPart $ text) @?= expected

