module TrimTests where

import Test.Tasty
import Test.Tasty.HUnit
import Harmonie.Common.Trim


trimTests :: TestTree
trimTests = testGroup "trimTests"
    [
        testCase "text" $ testTrim "text" "text",
        testCase ",text" $ testTrim ",text" "text",
        testCase " text " $ testTrim " text " "text",
        testCase " hello world " $ testTrim " hello world " "hello world"
    ]

testTrim :: String -> String -> Assertion
testTrim source expected = (trim source) @?= expected