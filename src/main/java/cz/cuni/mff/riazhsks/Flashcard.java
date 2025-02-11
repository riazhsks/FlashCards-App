/**
 * Provides functionality for a flashcard study application.
 * 
 * This package contains classes responsible for managing a flashcard database, 
 * creating a user interface, and handling user interactions with the database.
 * 
 * MainApp class: Creates the user interface and handles user interaction with the database;
 * DBConnection class: Manages communication with an SQLite database, including table creation,
 * flashcard addition, removal, and loading;
 * Flashcard class: Represents individual flashcards with question, answer, number, and color.
 *
 * UI divided into two modes: 
 * study mode (adding, removing, displaying flashcards);
 * quiz mode (displaying flashcards, checking answers).
 * 
 * @author Sofiia Riazhskykh
 */
package cz.cuni.mff.riazhsks;

/**
 * Imports the Color class from the java.awt package
 * to represent colors in the user interface.
 */
import java.awt.Color;

/**
 * This class represents a flashcard used in a study application.
 * Flashcards consist of a question, an answer, a unique number ID, and a color.
 * 
 * Each flashcard object is immutable, meaning its values cannot be changed
 * after creation. 
 * 
 */
public class Flashcard {

    /** The question on the flashcard. */
    private final String question;

    /** The answer to the question on the flashcard. */
    private final String answer;

    /** The unique number id of the flashcard. */
    private final int number;

    /** The color associated with the flashcard. */
    private final Color color;

    /**
     * Constructor for a flashcard with the specified question, answer, 
     * number, and color.
     *
     * @param question the question displayed on the flashcard
     * @param answer the answer to the question on the flashcard
     * @param number the id number of the flashcard
     * @param color the color associated with the flashcard
     */
    public Flashcard(String question, String answer, int number, Color color) {
        this.question = question;
        this.answer = answer;
        this.number = number;
        this.color = color;
    }

    /**
     * Returns the question stored on the flashcard.
     *
     * @return the question text
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Returns the answer stored on the flashcard.
     *
     * @return the answer text
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Returns the unique id of the flashcard.
     *
     * @return the flashcard id
     */
    public int getNumber() {
        return number;
    }

    /**
     * Returns the color associated with the flashcard.
     *
     * @return the flashcard color
     */
    public Color getColor() {
        return color;
    }
}
