# Word Notebook

A console-based Java application for building and tracking a personal vocabulary list.

## Features
- Add nouns and verbs
- List and search saved words
- View meanings and example sentences
- Mark entries as NEW or LEARNED
- Generate a learning progress report
- Persist entries locally in `data/words.txt`

## Requirements
- JDK 17 or later

## Run
Windows: run `run.bat` from the project root.
macOS/Linux: run `chmod +x run.sh && ./run.sh`.

## Structure
- `Main.java`: console menu and user interaction
- `WordService.java`: vocabulary operations and reports
- `FileManager.java`: local file persistence
- `WordEntry.java`: abstract vocabulary entry
- `NounEntry.java` and `VerbEntry.java`: concrete entry types

The application creates `data/words.txt` and `data/report.txt` as needed. These local data files are excluded from Git to avoid publishing personal vocabulary. Each saved record uses `id|type|word|meaning|example1|example2|status`.