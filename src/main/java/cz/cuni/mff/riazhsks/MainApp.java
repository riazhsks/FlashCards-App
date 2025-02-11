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
 * Import necessary Java libraries for UI, file handling, database connection, 
 * and other utilities required for the flashcard application.
 */
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import javax.swing.*;     

/**
 * MainApp is the class responsible for creating the user interface
 * and handling user interactions with the application.
 */
public class MainApp {

    private static JFrame frame; 
    public static boolean showingQuestion;
    private static int numberOfCards;
    
    private JButton[] buttons;
    private  JButton[] qbuttons;
    private JPanel panel;
    private JButton addButton;
    private JButton removeButton;
    private  JButton removeAllButton;
    private  JLabel lastBest;
    private  Connection connection;
    public  JButton nextButton;
    public JButton prevButton;
    public  JButton flipButton;
    private JButton quizModeButton; 
    private  int best;
    private int total_best;

    public static  int currentCardIndex;
    public static List<Flashcard> flashcards;
    public static JTextArea cardTextArea;
    

    /**
     * Constructor that initializes the flashcards, sets up the database,
     * creates the user interface, and loads existing flashcards.
     */
    public MainApp() {
        flashcards = new ArrayList<>();
        currentCardIndex = 0;
        showingQuestion = true;
        initializeDatabase();
        studyMode();
        setButtonsResponses();
        DBconnection.loadFlashcards();
        updateCard(cardTextArea);
        numberOfCards = flashcards.size();
    }

    /**
     * Establishes a connection to the database.
     * Exits if the connection cannot be established.
     */ 
    private void initializeDatabase() {
        DBconnection.initializeDatabase();
        connection = DBconnection.getConnection();
        if (connection == null) {
            System.err.println("Database connection could not be established");
            System.exit(1);
        }
    }

    /**
     * The main method initializes the application, asks the user about initial settings,
     * and displays the main application window.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp()); 
        String start = askUserStart();
        String type = askUserType();
        if (flashcards != null && start.equals("n")) {
            DBconnection.removeAll(true);
            numberOfCards = 0;
        } 
        if (type.equals("f")) {
            addFlashCards();
        }
        frame.setVisible(true);
    }

    /**
     * Asks the user to decide whether to create a new flashcard database
     * or continue working with the most recently created one.
     *
     * @return "n" to create a new database or "c" to continue with the existing one.
     */
    public static String askUserStart() {
        String answer = null; 
        System.out.println("Do you want to create a new database of flashcards, or to use the latest created one?");
        System.out.print("Type \"n\" for \"new\" or \"c\" for \"continue\": ");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            answer = scanner.next();
            if (answer.equalsIgnoreCase("n")) {
                    return answer;
            } else if (answer.equalsIgnoreCase("c")) {
                    return answer;
            } else {
                System.out.println("Incorrect input. Type \"n\" for \"new\" or \"c\" for \"continue\":");
            }
        }
        return answer;
    }

    /**
     * Asks the user to specify whether to load flashcards from a file or add them manually.
     *
     * @return "f" to load from a file, or "m" to add manually.
     */
    public static String askUserType() {
        String answer = null;   
        System.out.println("Do you want to download flashcards from the file, or to add them manually?");
        System.out.print("Type \"f\" for \"file\" or \"m\" for \"manually\": ");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            answer = scanner.next();
            if (answer.equalsIgnoreCase("f")) {
                return answer;
            } else if (answer.equalsIgnoreCase("m")) {
                return answer;
            } else {
                System.out.println("Incorrect input. Type \"f\" for \"file\" or \"m\" for \"manually\":");
            }
        }
        return answer;
    }

    /**
     * Gets a file as an input from user, processes the content,
     * and adds the flashcards to the database updates the UI.
     * 
     * @throws IOException 
     */
    public static void addFlashCards() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nNote that the question and answer fields are mandatory. Expected content of the file:\n"); 
        System.out.println("Question;Answer");
        System.out.println("What is the capital of Czech Republic?;Prague\n");
        System.out.println("Type the path to the file with flashcards: ");  
        Path pathToFile = null;
        while(scanner.hasNext()) {
            pathToFile = Path.of(scanner.next());
            try (BufferedReader br = new BufferedReader(new FileReader(pathToFile.toFile()))) {
                String line;
                int number = numberOfCards;
                while ((line = br.readLine()) != null && !line.isEmpty()) {
                    String[] fields = line.split(";");
                    if (fields.length != 2) {
                        throw new IOException("Invalid file content");
                    } 
                    String question = fields[0];
                    String answer = fields[1];
                    number++;
                    Color color = generateRandomColor();
                    DBconnection.addFlashcard(question.trim(), answer.trim(), number, color);
                }
            } catch (IOException e){
                System.out.println("Error reading the file.");
                System.exit(1);
            } 
            DBconnection.loadFlashcards();
            currentCardIndex = 0;
            updateCard(cardTextArea); 
            break;
        }
    }
    
    /** 
     *  Creates UI for study mode (frame, card area, buttons, score label).
     */
    private void studyMode() {
        frame = new JFrame("Flashcards App");
        frame.setSize(850, 600);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardTextArea = new JTextArea();
        cardTextArea.setEditable(false); 
        cardTextArea.setFont(new Font("Arial", Font.BOLD, 24));
        cardTextArea.setLineWrap(true); 
        cardTextArea.setWrapStyleWord(true); 
        cardTextArea.setBackground(Color.GRAY);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(cardTextArea, BorderLayout.CENTER); 

        nextButton = new JButton("Next");
        prevButton = new JButton("Previous");
        flipButton = new JButton("Flip");
        addButton = new JButton("Add Question");
        removeButton = new JButton("Delete Question");
        removeAllButton = new JButton("Delete All");
        quizModeButton = new JButton ("Quiz Mode");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 4, 10, 10)); 

        buttons = new JButton[]{nextButton, prevButton, flipButton, addButton, removeButton, removeAllButton, quizModeButton};
        for (JButton button : buttons) {
            button.setFont(new Font("Arial", Font.BOLD, 16));
            if(button.equals(prevButton)){
                button.setPreferredSize(new Dimension(140, 40));
            } else if(button.equals(addButton) || button.equals(removeAllButton)){
                button.setPreferredSize(new Dimension(180, 40));
            } else if (button.equals(removeButton)){
                button.setPreferredSize(new Dimension(200, 40));
            } else if (button.equals(quizModeButton)){
                button.setPreferredSize(new Dimension(210, 40));
            }  else {
             button.setPreferredSize(new Dimension(100, 40));    
            }
            buttonPanel.add(button);
        }   
        lastBest = new JLabel("Best score: " + best + "/" + total_best);
        lastBest.setFont(new Font("Arial", Font.BOLD, 18));
        buttonPanel.add(lastBest);
        panel.add(buttonPanel, BorderLayout.SOUTH);  
        frame.add(panel);  
        updateCard(cardTextArea);
    }

    
    /**
     * Generates a random color to be used for flashcards.
     *
     * @return A randomly generated Color object.
     */
    private static Color generateRandomColor() {
        Random rand = new Random();
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    /**
     * Enables all buttons in the application UI.
     */
    private void enableButtons(JButton[] buttons) {
        for (JButton button : buttons) {
            button.setEnabled(true);
        }   
    }

    /**
     * Disables all buttons in the application UI.
     */
    private void disableButtons(JButton[] buttons) {
        for (JButton button : buttons) {
            button.setEnabled(false);
        }   
    }

    /**
     * Sets the behavior of buttons when clicked.
     */
    private void setButtonsResponses() {
        /**
         * Moves to the next flashcard, if available.
         */
        nextButton.addActionListener((ActionEvent e) -> {
            if (currentCardIndex < flashcards.size() - 1) {
                currentCardIndex++;
                showingQuestion = true;
                updateCard(cardTextArea);
            } else {
                showWarningDialog(buttons);
            }
        });

        /**
         * Moves to the previous flashcard, if available.
         */
        prevButton.addActionListener((ActionEvent e) -> {
            if (currentCardIndex > 0) {
                currentCardIndex--;
                showingQuestion = true;
                updateCard(cardTextArea);
            } else {
                showWarningDialog(buttons);
            }
        });

        /**
         * Flips the current flashcard to show either the question or the answer.
         */
        flipButton.addActionListener((ActionEvent e) -> {
            showingQuestion = !showingQuestion;
            updateCard(cardTextArea);
        });

        /**
         * Opens a dialog window allowing the user to add a new flashcard
         * by entering a question and an answer.
         */
        addButton.addActionListener((ActionEvent e) -> {
            openAddFlashcardDialog();
        });

        /**
         * Removes the currently displayed flashcard from the database.
         */
        removeButton.addActionListener((ActionEvent e) -> {
            if (!flashcards.isEmpty()) {
                Flashcard currentCard = flashcards.get(currentCardIndex);
                DBconnection.removeFlashcard(currentCard);
                DBconnection.loadFlashcards();
            }
        });

        /**
         * Deletes all flashcards from the database.
         */
        removeAllButton.addActionListener((ActionEvent e) -> {
            if (!flashcards.isEmpty()) {
                DBconnection.removeAll(false);
            }
        });

        /**
         * Starts the quiz mode
         */
        quizModeButton.addActionListener((ActionEvent e) -> {
            quizMode();
        });
    }

    /**
     * Create UI for the quiz mode (frame, card area, buttons).
     */
    private void quizMode() {
        frame.setVisible(false);

        JTextArea quizTextArea = new JTextArea();
        quizTextArea.setEditable(false); 
        quizTextArea.setFont(new Font("Arial", Font.BOLD, 24));
        quizTextArea.setLineWrap(true); 
        quizTextArea.setWrapStyleWord(true); 
        quizTextArea.setBackground(Color.GRAY);
        currentCardIndex = 0;
        updateCard(quizTextArea);

        JFrame qframe = new JFrame("Quiz Mode");
        qframe.setSize(850, 600);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - qframe.getWidth()) / 2;
        int y = (screenSize.height - qframe.getHeight()) / 2;
        qframe.setLocation(x, y);
        qframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        JPanel qpanel = new JPanel();
        qpanel.setLayout(new BorderLayout());
        qpanel.add(quizTextArea, BorderLayout.CENTER);

        JButton next = new JButton("Next");
        JButton prev = new JButton("Previous");
        JButton checkAnswerButton = new JButton("Check Answer");
        JButton returnButton = new JButton("Return");
        
        JPanel quizButtonPanel = new JPanel(new FlowLayout());
        qbuttons = new JButton[]{next, prev, checkAnswerButton, returnButton};
        for (JButton button : qbuttons) {
            button.setFont(new Font("Arial", Font.BOLD, 16));
            if(button.equals(checkAnswerButton)){
                button.setPreferredSize(new Dimension(200, 40));
            } else {
                button.setPreferredSize(new Dimension(180, 40));
            } 
            quizButtonPanel.add(button);
        }   
        qpanel.add(quizButtonPanel, BorderLayout.SOUTH);
    
        int[] score = {0}; 
        int[] total = {0}; 
    
        JLabel scoreLabel = new JLabel("Score: " + score[0] + "/" + total[0]);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        qpanel.add(scoreLabel, BorderLayout.NORTH);

        /**
         * Moves to the next flashcard, if available.
         */
        next.addActionListener((ActionEvent e1) -> {
            if (currentCardIndex < flashcards.size() - 1) {
                currentCardIndex++;
                showingQuestion = true;
                updateCard(quizTextArea);
            } else {
                showWarningDialog(qbuttons);
            }
        });

        /**
         * Moves to the previous flashcard, if available.
         */
        prev.addActionListener((ActionEvent e1) -> {
            if (currentCardIndex > 0) {
                currentCardIndex--;
                showingQuestion = true;
                updateCard(quizTextArea);
            } else {
                showWarningDialog(qbuttons);
            }
        });

        checkAnswerButton.addActionListener((ActionEvent e2) -> {
            checkAnswer(score,total,qbuttons, scoreLabel);
        });
        
        /**
         * Displays a dialog with the final score and updates the best score. 
         * When the dialog is closed, the quiz frame is disposed,
         * and the main frame is visible.
         */
        returnButton.addActionListener((ActionEvent e3) -> {
            JDialog dialog = new JDialog(qframe, "Score", true);
            dialog.setSize(300, 150);
            dialog.setLocationRelativeTo(null);
            JPanel panel = new JPanel();
            JLabel lbl = new JLabel("Final score is " + score[0] + "/" + total[0]);
            if (score[0] > best) {
                best = score[0];
                total_best = total[0];
            }
            lbl.setFont(new Font("Arial", Font.BOLD, 18));
            panel.add(lbl);
            dialog.add(panel,BorderLayout.SOUTH);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    qframe.dispose(); 
                    frame.setVisible(true);  
                    lastBest.setText("Best Score: " + best + "/" + total_best);
                    currentCardIndex = 0;
                    updateCard(cardTextArea);
                }
            });
            dialog.setVisible(true);
        });
        qframe.add(qpanel);
        qframe.setVisible(true);
    }

    /**
     * Opens a dialog for the user to enter an answer to the current flashcard's question.
     * The answer is checked and feedback is provided.
     * 
     * @param score  user's correct answerы count
     * @param total  total number of questions answered
     * @param quizButtons quiz buttons
     * @param scoreLabel  current score label
     */
    private void checkAnswer(int[] score, int[] total, JButton[] quizButtons, JLabel scoreLabel) {
        if (!flashcards.isEmpty()) {
            Flashcard currentCard = flashcards.get(currentCardIndex);
            String correctAnswer = currentCard.getAnswer();
    
            JDialog dialog = new JDialog(frame, "Check Answer", true);
            dialog.setSize(450, 300);
            dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); 
    
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
            JLabel instructionLabel = new JLabel("Enter your answer:");
            instructionLabel.setFont(new Font("Arial", Font.BOLD, 18));
            instructionLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            mainPanel.add(instructionLabel);
    
            JTextField userInput = new JTextField();
            userInput.setFont(new Font("Arial", Font.BOLD, 18));
            userInput.setMaximumSize(new Dimension(400, 40)); 
            mainPanel.add(userInput);
    
            JPanel buttonPanel = new JPanel();
            JButton checkButton = new JButton("Check");
            checkButton.setPreferredSize(new Dimension(200, 40));
            buttonPanel.add(checkButton);
            mainPanel.add(buttonPanel);
    
            JTextArea resultText = new JTextArea(3, 30);
            resultText.setFont(new Font("Arial", Font.BOLD, 18));
            resultText.setEditable(false);
            resultText.setLineWrap(true);
            resultText.setWrapStyleWord(true);
    
            JScrollPane scrollPane = new JScrollPane(resultText);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setPreferredSize(new Dimension(400, 80)); 
            scrollPane.setMaximumSize(new Dimension(400, 100)); 
            mainPanel.add(scrollPane);
    
            checkButton.addActionListener((ActionEvent e1) -> {
                String userAnswer = userInput.getText().trim();
                if (userAnswer.equalsIgnoreCase(correctAnswer.trim())) {
                    resultText.setText("Correct!");
                    score[0]++;
                    total[0]++;
                    resultText.setForeground(Color.GREEN);
                } else {
                    resultText.setText("Incorrect! Correct answer: " + correctAnswer);
                    resultText.setForeground(Color.RED);
                    total[0]++;
                }
                for (JButton button : quizButtons) {
                    button.setEnabled(false);
                }  
                checkButton.setEnabled(false);
                resultText.repaint();
                scoreLabel.setText("Score: " + score[0] + "/" + total[0]);
                
            });
    
            dialog.add(mainPanel);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    for (JButton button : quizButtons) {
                        button.setEnabled(true);
                    } 
                }
            });
    
            dialog.setVisible(true); 
            
        } else {
            JOptionPane.showMessageDialog(frame, "No flashcards available.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Shows a warning if there are no more cards to display.
     */
    private void showWarningDialog(JButton[] buttons) {
        JDialog dialog = new JDialog(frame, "Warning", true);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(null);
        JPanel panel = new JPanel();
        disableButtons(buttons);
        JLabel lbl = new JLabel("No more flashcards");
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lbl);
        dialog.add(panel,BorderLayout.SOUTH);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                enableButtons(buttons);                
            }
        });
        dialog.setVisible(true);
    }

    /**
     * Opens a dialog window for adding a new flashcard.
     * The dialog allows the user to input a question and an answer.
     */
    private static void openAddFlashcardDialog() {
        JDialog dialog = new JDialog(frame, "Add Flashcard", true);
        dialog.setSize(430, 260);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        JPanel textPanel = new JPanel(new GridBagLayout());

        JTextArea questionLabel = new JTextArea("Enter the question:");
        
        questionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        questionLabel.setWrapStyleWord(true);
        questionLabel.setLineWrap(true);
        questionLabel.setOpaque(false); 
        questionLabel.setEditable(false);
        questionLabel.setFocusable(false);
        questionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10,0,10)); 
        textPanel.add(questionLabel);

        JTextArea answerLabel = new JTextArea("Enter the answer:");
        answerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        answerLabel.setWrapStyleWord(true);
        answerLabel.setLineWrap(true);
        answerLabel.setOpaque(false); 
        answerLabel.setEditable(false);
        answerLabel.setFocusable(false);
        answerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10,0,10)); 
        textPanel.add(answerLabel);
    
        JTextField questionField = new JTextField();
        questionField.setPreferredSize(new Dimension(350, 35));
        questionField.setFont(new Font("Arial", Font.BOLD, 18));
    
        JTextField answerField = new JTextField();
        answerField.setPreferredSize(new Dimension(350, 35));
        answerField.setFont(new Font("Arial", Font.BOLD, 18));
    
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
    
        panel1.add(questionLabel);
        panel1.add(questionField);
        panel1.add(answerLabel);
        panel1.add(answerField);
        panel1.add(buttonPanel);
        dialog.add(panel1);
    
        saveButton.addActionListener((ActionEvent e1) -> {
            String question = questionField.getText().trim();
            String answer = answerField.getText().trim();
            int number = flashcards.size() + 1;
            Color color = generateRandomColor();
            if (!question.isEmpty() && !answer.isEmpty()) {
                DBconnection.addFlashcard(question, answer, number, color);
                DBconnection.loadFlashcards();
                currentCardIndex = flashcards.size() - 1;
                updateCard(cardTextArea);
                dialog.dispose();
            }
        });
        cancelButton.addActionListener((ActionEvent e1) -> {
            dialog.dispose();
        });
        dialog.setVisible(true);
    }

    /**
     * Updates the flashcard displayed.
     * If no flashcards are available, notifies the user.
     * 
     * @param textArea The JTextArea (study or quiz mode) where the flashcard content will be displayed.
     */
    public static void updateCard(JTextArea textArea) {
        if (flashcards.isEmpty()) {
            textArea.setText("No flashcards available");
        } else {
            Flashcard currentCard = flashcards.get(currentCardIndex);
            int brightness = (int) (0.299 * currentCard.getColor().getRed() +
                                    0.587 * currentCard.getColor().getGreen() +
                                    0.114 * currentCard.getColor().getBlue());
            if (showingQuestion) {
                textArea.setText(" Flashcard number " + currentCard.getNumber() +  "\n\n Question:\n\n " + currentCard.getQuestion());
                textArea.setBackground(currentCard.getColor());
                if (brightness < 128) {
                    textArea.setForeground(Color.WHITE);
                } else{
                    textArea.setForeground(Color.BLACK);
                }
            } else {
                textArea.setText(" Flashcard number " + currentCard.getNumber() +"\n\n Answer:\n\n " + currentCard.getAnswer());
                textArea.setBackground(currentCard.getColor());
                if (brightness < 128) {
                    textArea.setForeground(Color.WHITE);
                } else {
                    textArea.setForeground(Color.BLACK);
                }
            }
        }
    }
}