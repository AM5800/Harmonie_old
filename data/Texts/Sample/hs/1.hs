module Sample_1 where
import Harmonie.ReversePartProcessing
import Harmonie.Common.TextPart
import Harmonie.Common.Gender

source = TextPart "\
\Die deutsche Sprache, gehört zur westgermanischen Gruppe des germanischen Zweiges der indogermanischen Sprachen.\n\
\\n\
\Deutsch ist die meistgesprochene Muttersprache in der Europäischen Union. Es wird im deutschen Sprachraum gesprochen, zu dem Deutschland, Österreich, die Deutschschweiz, Liechtenstein, Luxemburg, Ostbelgien, Südtirol, das Elsass und Lothringen gehören. Deutsch ist Minderheitensprache in einigen mittel- und außereuropäischen Ländern, Nationalsprache im afrikanischen Namibia und zählt zu den zehn wichtigsten Sprachen der Welt.\n\
\" "Sample" 1

w 1 "Die" = Article
w 2 "deutsche" = Adjective "deutsch" ["немецкий"]
w 3 "Sprache" = Noun "sprache" Feminine ["язык"]
w 4 "gehört" = Verb "gehören" ["относиться", "принадлежать"]
w 5 "zur" = Preposition
w 6 "westgermanischen" = Adjective "westgermanisch" ["западногерманский"]
w 7 "Gruppe" = Noun "gruppe" Feminine ["группа"]
w 8 "des" = Article
w 9 "germanischen" = Adjective "germanisch" ["германский"]
w 10 "Zweiges" = Noun "zweig" Masculine ["ветвь"]
w 11 "der" = Article
w 12 "indogermanischen" = Adjective "indogermanisch" ["индогерманский", "индоевропейский"]
w 13 "Sprachen" = Noun "sprache" Feminine ["язык"]
w 14 "Deutsch" = Noun "Deutsch" Neuter ["немецкий язык"]
w 15 "ist" = Ignore
w 16 "die" = Article
w 17 "meistgesprochene" = Unknown
w 18 "Muttersprache" = Noun "Muttersprache" Feminine ["родной язык"]
w 19 "in" = Preposition
w 20 "der" = Article
w 21 "Europäischen" = Adjective "europäisch" ["европейский"]
w 22 "Union" = Noun "Union" Feminine ["союз"]
w 23 "Es" = Pronoun
w 24 "wird" = Ignore
w 25 "im" = Preposition
w 26 "deutschen" = Adjective "deutsch" ["немецкий"]
w 27 "Sprachraum" = Noun "Sprachraum" Masculine ["область распространения языка"]
w 28 "gesprochen" = Verb "sprechen" ["говорить", "разговаривать"]
w 29 "zu" = Preposition
w 30 "dem" = Article
w 31 "Deutschland" = Noun "Deutschland" Neuter ["Германия"]
w 32 "Österreich" = Noun "Österreich" Neuter ["Австрия"]
w 33 "die" = Article
w 34 "Deutschschweiz" = Noun "Deutschschweiz" Feminine ["немецкоговорящая часть Швейцарии"]
w 35 "Liechtenstein" = Noun "Liechtenstein" Neuter ["Лихтенштейн"]
w 36 "Luxemburg" = Noun "Luxemburg" Neuter ["Люксембург"]
w 37 "Ostbelgien" = Noun "Ostbelgien" Neuter ["Восточная часть Бельгии"]
w 38 "Südtirol" = Noun "Südtirol" Neuter ["Южный Тироль (автономная провинция в Италии)"]
w 39 "das" = Article
w 40 "Elsass" = Noun "Elsass" Neuter ["Эльзас (историческая область на территории Франции)"]
w 41 "und" = Conjunction
w 42 "Lothringen" = Noun "Lothringen" Neuter ["Лотарингия (регион на северо-востоке Франции)"]
w 43 "gehören" = Verb "gehören" ["относиться", "принадлежать"]
w 44 "Deutsch" = Noun "Deutsch" Neuter ["немецкий язык"]
w 45 "ist" = Ignore
w 46 "Minderheitensprache" = Noun "Minderheitensprache" Feminine ["Язык национального меньшинства"]
w 47 "in" = Preposition
w 48 "einigen" = Pronoun
w 49 "mittel" = Noun "mittel" Neuter ["среднее"]
w 50 "und" = Conjunction
w 51 "außereuropäischen" = Adjective "außereuropäisch" ["неевропейский"]
w 52 "Ländern" = Noun "Land" Neuter ["страна"]
w 53 "Nationalsprache" = Noun "Nationalsprache" Feminine ["Национальный язык"]
w 54 "im" = Preposition
w 55 "afrikanischen" = Adjective "afrikanisch" ["африканский"]
w 56 "Namibia" = Noun "Namibia" Neuter ["Намибия"]
w 57 "und" = Conjunction
w 58 "zählt" = Verb "zählen" ["считать"]
w 59 "zu" = Preposition
w 60 "den" = Article
w 61 "zehn" = Ignore
w 62 "wichtigsten" = Adjective "wichtig" ["важный", "основной"]
w 63 "Sprachen" = Noun "sprache" Feminine ["язык"]
w 64 "der" = Article
w 65 "Welt" = Noun "welt" Feminine ["мир"]
w n s = error $ "Could not match word " ++ s ++ " with index " ++ show n

main = processPart source w