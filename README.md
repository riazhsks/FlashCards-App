# FlashCards-App
The FlashCards Application (Java)

Flashcards App - Overview
Version: 1.0
Author: Sofiia Riazhskykh

Overview
The FlashCards App is a Java-based application designed to help 
users study using flashcards stored in an SQLite database. 
It consists of two main modes:
Study Mode – Allows users to add, remove, and display flashcards.
Quiz Mode – Allows checking user's answers.

Project Structure
The application consists of the following classes:
MainApp – Handles the user interface and interaction with the database.
DBConnection – Manages communication with the SQLite database flashcards.db.
Flashcard – Represents a flashcard object, for which stores a question, an answer, a number, and a color.

The table flashcards.db includes:

id (INTEGER PRIMARY KEY AUTOINCREMENT) – Unique identifier for each flashcard.
question (TEXT NOT NULL) – The flashcard question.
answer (TEXT NOT NULL) – The flashcard answer.
number (INTEGER NOT NULL) – Order of the flashcard.
color (INTEGER NOT NULL) – Color associated with the flashcard.
