pragma encoding = utf8;
CREATE TABLE IF NOT EXISTS germanWords (id INTEGER PRIMARY KEY, word TEXT NOT NULL, partOfSpeech TEXT, gender TEXT)
CREATE UNIQUE INDEX IF NOT EXISTS germanWordsIndex ON germanWords (word, partOfSpeech, gender)
CREATE TABLE IF NOT EXISTS germanNormalForms (baseForm TEXT, wordId INTEGER)
CREATE UNIQUE INDEX IF NOT EXISTS germanNormalFormsIndex ON germanNormalForms (baseForm, wordId)
CREATE TABLE IF NOT EXISTS ignoredWords (ignoredWord TEXT PRIMARY KEY)
CREATE TABLE IF NOT EXISTS textParts (id INTEGER PRIMARY KEY, partText TEXT, textId TEXT, partNumber INTEGER)
CREATE UNIQUE INDEX IF NOT EXISTS textPartsIndex ON textParts (textId, partNumber)
CREATE TABLE IF NOT EXISTS texts (id TEXT PRIMARY KEY NOT NULL, languageId INTEGER, name TEXT)
CREATE TABLE IF NOT EXISTS germanExamples (partId INTEGER, wordId INTEGER, ranges TEXT, meanings TEXT)
CREATE UNIQUE INDEX IF NOT EXISTS germanExamplesIndex ON germanExamples (partId, wordId, ranges, meanings)
CREATE TABLE IF NOT EXISTS knownAffixes (verbId INTEGER, prepositionId INTEGER, wordId INTEGER)
CREATE UNIQUE INDEX IF NOT EXISTS knownAffixesIndex ON knownAffixes (verbId, prepositionId, wordId)
INSERT OR IGNORE INTO texts (id, languageId, name) VALUES("PG1", 0, "Technik, Gerätekunde, Aerodynamik")
INSERT OR IGNORE INTO texts (id, languageId, name) VALUES("PG2", 0, "Flugpraxis, Verhalten in besonderen Fällen, Menschliche Leistungsfähigkeit, Natur- und Umweltschutz")
INSERT OR IGNORE INTO texts (id, languageId, name) VALUES("PG3", 0, "Luftrecht")
INSERT OR IGNORE INTO texts (id, languageId, name) VALUES("PG4", 0, "Meteorologie")