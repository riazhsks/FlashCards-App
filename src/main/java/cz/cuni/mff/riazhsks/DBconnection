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
 * Import necessary Java libraries for UI, database connection, 
 * and other utilities.
 */
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;


/**
 * Handles communication with an SQLite database that stores flashcard information.
 * Provides methods for database initialization, table creation, 
 * adding, removing, and loading flashcards.
 */
public class DBconnection {

    /** Connection to the SQLite database. */
    private static Connection connection;

    /** Counter used for unit testing purposes. */
    public static int count;

    /**
     * Establishes a connection to the flashcards.db database.
     * If the database does not exist, it will be created.
     * 
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:flashcards.db");
            createTable();
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Returnes the database connection.
     *
     * @return the database connection
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * Creates the flashcards table in the database if it does not already exist.
     * The table includes columns for an ID, question, answer, number, and color.
     *  Database Schema:
     * - id INTEGER PRIMARY KEY AUTOINCREMENT
     * - question TEXT NOT NULL
     * - answer TEXT NOT NULL
     * - number INTEGER NOT NULL
     * - color INTEGER NOT NULL
     */
    public static void createTable() {
        try (PreparedStatement s = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS flashcards (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "question TEXT NOT NULL, " +
                "answer TEXT NOT NULL, " +
                "number INTEGER NOT NULL, " +
                "color INTEGER NOT NULL)")) 
            {
            s.executeUpdate();
            s.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

     /**
     * Adds a new flashcard to the database.
     *
     * @param question the text of the question
     * @param answer the text of the answer
     * @param number the id of the flashcard
     * @param color the color associated with the flashcard
     */
    public static void addFlashcard(String question, String answer, int number, Color color) {
        try {
            String q = "INSERT INTO flashcards (question, answer, number, color) VALUES (?, ?, ?, ?)";
            PreparedStatement s = connection.prepareStatement(q);
            s.setString(1, question);
            s.setString(2, answer);
            s.setInt(3, number);
            s.setInt(4, color.getRGB());
            s.executeUpdate();
            count++;
            s.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Opens a confirmation dialog and removes a flashcard from the database.
     *
     * @param flashcard the flashcard to remove
     */
    public static void removeFlashcard(Flashcard flashcard) {
        JDialog dialog = new JDialog();
        JPanel textPanel = new JPanel(new GridBagLayout());
        JTextArea label = new JTextArea("Do you want to remove this question?");
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setWrapStyleWord(true);
        label.setLineWrap(true);
        label.setOpaque(false); 
        label.setEditable(false);
        label.setFocusable(false);
        label.setBorder(BorderFactory.createEmptyBorder(60, 40,60,10));
        textPanel.add(label);

        JPanel buttonPanel = new JPanel();
        JButton yesButton = new JButton("Yes");
        JButton noButton = new JButton("No");
        buttonPanel.setLayout(new FlowLayout()); 
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setTitle("Confirm removal");
        dialog.getContentPane().add(label, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH); 
        dialog.setVisible(true);

        /**
         * Behaviour when the "Yes" button is clicked - delete the question 
         * from the database, update the card text area.
         */
        yesButton.addActionListener((ActionEvent e) -> {
            try {
                connection.setAutoCommit(false); 

                String deleteQuery = "DELETE FROM flashcards WHERE question = ? AND answer = ?";
                PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
                deleteStatement.setString(1, flashcard.getQuestion());
                deleteStatement.setString(2, flashcard.getAnswer());
                deleteStatement.executeUpdate();
                deleteStatement.close();
     
                String updateQuery = "UPDATE flashcards SET number = number - 1 WHERE number > ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setInt(1, flashcard.getNumber()); 
                updateStatement.executeUpdate();
                updateStatement.close();
        
                connection.commit();
                connection.setAutoCommit(true);
        
                loadFlashcards();
                if (MainApp.currentCardIndex >= MainApp.flashcards.size()) {
                    MainApp.currentCardIndex = MainApp.flashcards.size() - 1;
                }
                MainApp.currentCardIndex = Math.max(0, MainApp.currentCardIndex - 1);
                MainApp.updateCard(MainApp.cardTextArea);
                dialog.dispose();
                count--;
            } catch (SQLException ex) {
                try {
                    connection.rollback(); 
                } catch (SQLException rollbackEx) {
                    System.out.println(rollbackEx.getMessage());
                }
               System.out.println(ex.getMessage());
            }
        });

        /**
         * Behaviour when the "No" button is clicked - cancel deletion,
         * just close the dialog window.
         */
        noButton.addActionListener((ActionEvent e) -> {
            dialog.dispose();
        });
    }

    /**
     * Opens a confirmation dialog and removes all flashcards from the database.
     *
     * @param force if true deletion is executed without user confirmation
     * (used for clearing the database if needed before initializing UI)
     */
    public static void removeAll(boolean force) {
        JDialog dialog = new JDialog();
        JPanel textPanel = new JPanel(new GridBagLayout());
        JTextArea label = new JTextArea("Do you want to remove everything?");
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setWrapStyleWord(true);
        label.setLineWrap(true);
        label.setOpaque(false); 
        label.setEditable(false);
        label.setFocusable(false);
        label.setBorder(BorderFactory.createEmptyBorder(60, 40,60,20)); 
        textPanel.add(label);

        JPanel buttonPanel = new JPanel();
        JButton yesButton = new JButton("Yes");
        JButton noButton = new JButton("No");
        buttonPanel.setLayout(new FlowLayout()); 
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setTitle("Confirm removal");
        dialog.getContentPane().add(label, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH); 
        dialog.setVisible(true);

         /**
         * Behaviour when the "Yes" button is clicked - delete everything 
         * from the database, update the card text area.
         */
        yesButton.addActionListener((ActionEvent e) -> {
            String q = "DELETE FROM flashcards"; 
            try (Statement s = connection.createStatement()) {
                s.executeUpdate(q); 
                s.close();
                MainApp.flashcards = new ArrayList<>();
                MainApp.currentCardIndex = 0;
                MainApp.updateCard(MainApp.cardTextArea);
                dialog.dispose();
                
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            count = 0;
        });

        /**
         * Behaviour when the "No" button is clicked - cancel deletion,
         * just close the dialog window.
         */
        noButton.addActionListener((ActionEvent e) -> {
            dialog.dispose();
        });

        /**
         * In case there is a need to delete a database, but the user interface is not active yet
         * and the user cannot confirm the deletion, force the deletion.
         * This situation happens when the user chooses to work with a new database, instead of
         * continue working with the latest created one.
         */
        if (force) {
            yesButton.doClick();
        }
    }

    /**
     * Loads all flashcards from the database into the application's flashcard list.
     */
    public static void loadFlashcards() {
        if (connection == null) {
            System.out.println("Connection is not available");
            return;
        }
        MainApp.flashcards.clear();
        try (PreparedStatement s = connection.prepareStatement("SELECT * FROM flashcards")) {
            ResultSet set = s.executeQuery();
            while (set.next()) {
                String question = set.getString("question");
                String answer = set.getString("answer");
                int number = set.getInt("number");
                int rgb = set.getInt("color"); 
                Color color = new Color(rgb);
                MainApp.flashcards.add(new Flashcard(question, answer, number, color));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
