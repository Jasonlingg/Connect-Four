// Jason Ling
// 12/20/2020
// 11 COMPSCI
// Description: Connect four game between strawberry and banana. The game continues until one player gets
// 4 in a row horizontally, vertically or diagonally.
// The game comes with sound effects for the following:
// 1. When a piece is dropped
// 2. When a winner is declared
// There is also background music, with an extra menu that called "settings" where player can choose between a few songs
package com.company;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.*;
import javax.swing.*;


public class Main extends JPanel implements ActionListener, MouseListener, KeyListener {
    static JFrame frame;
    final int BANANA = -1;
    final int STRAWBERRY = 1;
    final int EMPTY = 0;
    final int SQUARE_SIZE = 60;
    final int TOP_OFFSET = 42;
    final int BORDER_SIZE = 4;
    Clip drop, win, background1, background2, background3;
    int[][] board;
    int currentPlayer;
    int currentColumn;
    Image firstImage, secondImage;

    Timer timer;

    // For drawing images offScreen (prevents Flicker)
    // These variables keep track of an off screen image object and
    // its corresponding graphics object
    Image offScreenImage;
    Graphics offScreenBuffer;

    boolean gameOver;

    public Main() throws IOException, UnsupportedAudioFileException, LineUnavailableException, InterruptedException {
        // Setting the defaults for the panel
        setPreferredSize(new Dimension(7 * SQUARE_SIZE + 2 * BORDER_SIZE + 1, (6 + 1) * SQUARE_SIZE + TOP_OFFSET + BORDER_SIZE + 1));
        setLocation(100, 10);
        setBackground(new Color(200, 200, 200));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        board = new int[8][9];

        // Set up the Menu
        JMenuBar mainMenu = new JMenuBar();
        // Set up the Game MenuItems
        JMenuItem newOption, exitOption, subMenuOne, subMenuTwo, subMenuThree, subMenuFour;

        newOption = new JMenuItem("New");
        exitOption = new JMenuItem("Exit");

        // Set up the Game Menu
        JMenu gameMenu = new JMenu("Game");
        gameMenu.add(newOption);
        gameMenu.addSeparator();
        gameMenu.add(exitOption);

        // Settings Menu
        // My part of code is integrated into this block:
        JMenu settingsMenu = new JMenu("Settings");
        // in settings, there is a submenu for songs
        JMenu songMenu = new JMenu("Songs");
        // add to settingsMenu
        settingsMenu.add(songMenu);
        // Add the song choices into songMenu
        subMenuOne = new JMenuItem("Giorno's theme");
        subMenuTwo = new JMenuItem("Coconut Song");
        subMenuThree = new JMenuItem("Normal music");
        subMenuFour = new JMenuItem("No music :(");
        songMenu.add(subMenuOne);
        songMenu.add(subMenuTwo);
        songMenu.add(subMenuThree);
        songMenu.add(subMenuFour);
        // Set up the Menu Bar and add the above Menus
        mainMenu.add(gameMenu);
        mainMenu.add(settingsMenu);
        // Set the menu bar for this frame to mainMenu
        frame.setJMenuBar(mainMenu);

        // Use a media tracker to make sure all of the images are
        // loaded before we continue with the program
        MediaTracker tracker = new MediaTracker(this);
        firstImage = Toolkit.getDefaultToolkit().getImage("banana.gif");
        tracker.addImage(firstImage, 0);
        secondImage = Toolkit.getDefaultToolkit().getImage("strawberry.gif");
        tracker.addImage(secondImage, 1);

        //  Wait until all of the images are loaded
        tracker.waitForAll();
        // audio input stream
        AudioInputStream sound = AudioSystem.getAudioInputStream(new File("drop.wav"));
        drop = AudioSystem.getClip();
        drop.open(sound);
        sound = AudioSystem.getAudioInputStream(new File("winner.wav"));
        win = AudioSystem.getClip();
        win.open(sound);
        sound = AudioSystem.getAudioInputStream(new File("background1.wav"));
        background1 = AudioSystem.getClip();
        background1.open(sound);
        sound = AudioSystem.getAudioInputStream(new File("background2.wav"));
        background2 = AudioSystem.getClip();
        background2.open(sound);
        sound = AudioSystem.getAudioInputStream(new File("background3.wav"));
        background3 = AudioSystem.getClip();
        background3.open(sound);
        // Set up the icon image (Tracker not needed for the icon image)
        Image iconImage = Toolkit.getDefaultToolkit().getImage("banana.gif");
        frame.setIconImage(iconImage);

        // Start a new game and then make the window visible
        newGame();
        // Setting action comment and add listener to play the music when the menu is selected
        newOption.setActionCommand("New");
        newOption.addActionListener(this);
        exitOption.setActionCommand("Exit");
        exitOption.addActionListener(this);
        subMenuOne.setActionCommand("JoJo");
        subMenuOne.addActionListener(this);
        subMenuTwo.setActionCommand("Coconut Song");
        subMenuTwo.addActionListener(this);
        subMenuThree.setActionCommand("Normal music");
        subMenuThree.addActionListener(this);
        subMenuFour.setActionCommand("No music");
        subMenuFour.addActionListener(this);
        setFocusable(true); // Need this to set the focus to the panel in order to add the keyListener
        addKeyListener(this);

        addMouseListener(this);
        // Starting Music
        background3.setFramePosition(0); //<-- play sound file again from beginning
        background3.loop(Clip.LOOP_CONTINUOUSLY);

    } // Constructor


    // To handle normal menu items
    public void actionPerformed(ActionEvent event) {
        String eventName = event.getActionCommand();
        if (eventName.equals("New")) {
            newGame();
        } else if (eventName.equals("Exit")) {
            System.exit(0);
        } else if (eventName.equals("JoJo")) {
            // Stop other sounds to play the certain song
            background2.stop();
            background3.stop();
            background1.setFramePosition(0); //<-- play sound file again from beginning
            background1.loop(Clip.LOOP_CONTINUOUSLY);
        } else if (eventName.equals("Coconut Song")) {
            // Stop other sounds to play the certain song
            background1.stop();
            background3.stop();
            background2.setFramePosition(0); //<-- play sound file again from beginning
            background2.loop(Clip.LOOP_CONTINUOUSLY);
        } else if (eventName.equals("Normal music")) {
            // Stop other sounds to play the certain song
            background1.stop();
            background2.stop();
            background3.setFramePosition(0); //<-- play sound file again from beginning
            background3.loop(Clip.LOOP_CONTINUOUSLY);
        } else if (eventName.equals("No music")) {
            // stop all music
            background1.stop();
            background2.stop();
            background3.stop();
        }
    }


    public void newGame() {
        currentPlayer = BANANA;
        clearBoard(board);
        gameOver = false;
        currentColumn = 3;
        repaint();
        JOptionPane.showMessageDialog(this, "Welcome to Connect Four!! The objective of the game is to get four in a row on the board. Good Luck!!",
                "Introduction", JOptionPane.INFORMATION_MESSAGE);
    }

//------------YOUR CODE GOES HERE  ------------------//

    public void clearBoard(int[][] board) {
        // takes the board array and then resets all of its values to 0
        // only parameter is the board, it represents the connect 4 board
        // does not return anything because arrays pass by reference
        for (int i = 0; i < board.length; i++)
            for (int c = 0; c < board[0].length; c++)
                board[i][c] = 0;
    }


    public int findNextRow(int[][] board, int column) {
        // given the board array and the column, it is finds where the next piece will go when dropped down
        // it needs to know the column and the array to change
        // return the row's key which is an integer
        int row = 0;
        if (board[0][column] != 0) {
            row = -1;
            return row;
        }
        for (int i = board.length - 2; i >= 0; i--) {
            if (board[i][column] == 0) {
                row = i;
                break;
            }
        }
        return row;
    }


    public int checkForWinner(int[][] board, int lastRow, int lastColumn) {
        // takes the position of the last piece dropped down (its row and column) and checks each scenario if the player won
        // lastRow/column represents the pieces' position ofn the array "board"
        // returns win, which is an integer that decides if the player has won or not according to its value
        int win = board[lastRow][lastColumn];
        int left = 0, right = 0, up = 0, down = 0, upleft = 0, upright = 0, downleft = 0, downright = 0;
        for (int i = 1; i < 4; i++) {
            if (board[lastRow][lastColumn - i] == win)
                left++;
            else
                break;
        }
        for (int i = 1; i < 4; i++) {
            if (board[lastRow][lastColumn + i] == win)
                right++;
            else
                break;
        }
        if (left + right >= 3)
            return win;
        for (int i = 1; i < 4; i++) {
            if (board[lastRow + i][lastColumn] == win)
                down++;
            else
                break;
        }
        for (int i = 1; i < 4; i++) {
            if (board[lastRow - i][lastColumn] == win)
                up++;
            else
                break;
        }
        if (up + down >= 3)
            return win;

        for (int i = 1; i < 4; i++) {
            if (board[lastRow + i][lastColumn - i] == win)
                downleft++;
            else
                break;
        }
        for (int i = 1; i < 4; i++) {
            if (board[lastRow - i][lastColumn + i] == win)
                upright++;
            else
                break;
        }

        if (downleft + upright >= 3)
            return win;

        for (int i = 1; i < 4; i++) {
            if (board[lastRow - i][lastColumn - i] == win)
                upleft++;
            else
                break;
        }
        for (int i = 1; i < 4; i++) {
            if (board[lastRow + i][lastColumn + i] == win)
                downright++;
            else
                break;
        }
        if (downright + upleft >= 3)
            return win;
        else
            return 0;
    }

//----------------------------------------------------//


    public void handleAction(int x, int y) {
        if (gameOver) {
            JOptionPane.showMessageDialog(this, "Please Select Game...New to start a new game",
                    "Game Over", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int column = (x - BORDER_SIZE) / SQUARE_SIZE + 1;
        int row = findNextRow(board, column);
        if (row <= 0) {
            JOptionPane.showMessageDialog(this, "Please Select another Column",
                    "Column is Full", JOptionPane.WARNING_MESSAGE);
            return;
        }

        animatePiece(currentPlayer, column, row);
        board[row][column] = currentPlayer;

        int winner = checkForWinner(board, row, column);

        if (winner == BANANA) {
            win.setFramePosition(0); //<-- play sound file again from beginning
            win.start();
            gameOver = true;
            repaint();
            JOptionPane.showMessageDialog(this, "Banana Wins!!!",
                    "GAME OVER", JOptionPane.INFORMATION_MESSAGE);

        } else if (winner == STRAWBERRY) {
            win.setFramePosition(0); //<-- play sound file again from beginning
            win.start();
            gameOver = true;
            repaint();
            JOptionPane.showMessageDialog(this, "Strawberry Wins!!!",
                    "GAME OVER", JOptionPane.INFORMATION_MESSAGE);
        } else
            // Switch to the other player
            currentPlayer *= -1;
        currentColumn = 3;

        repaint();
    }


    // MouseListener methods
    public void mouseClicked(MouseEvent e) {
        int x, y;
        x = e.getX();
        y = e.getY();
        drop.setFramePosition(0); //<-- play sound file again from beginning
        drop.start();
        handleAction(x, y);
    }


    public void mouseReleased(MouseEvent e) {
    }


    public void mouseEntered(MouseEvent e) {
    }


    public void mouseExited(MouseEvent e) {
    }


    public void mousePressed(MouseEvent e) {
    }


    //KeyListener methods
    public void keyPressed(KeyEvent kp) {
        if (kp.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (currentColumn < 6)
                currentColumn++;
        } else if (kp.getKeyCode() == KeyEvent.VK_DOWN) {
            handleAction((currentColumn) * SQUARE_SIZE + BORDER_SIZE, 0);
            drop.setFramePosition(0); //<-- play sound file again from beginning
            drop.start();
        } else if (kp.getKeyCode() == KeyEvent.VK_LEFT) {
            if (currentColumn > 0)
                currentColumn--;
        } else
            return;
        repaint();
    }


    public void keyReleased(KeyEvent e) {
    }


    public void keyTyped(KeyEvent e) {
    }


    public void animatePiece(int player, int column, int finalRow) {
        Graphics g = getGraphics();

        // Find the x and y positions for each row and column
        int xPos = (4 - 1) * SQUARE_SIZE + BORDER_SIZE;
        int yPos = TOP_OFFSET + 0 * SQUARE_SIZE;
        offScreenBuffer.clearRect(xPos, yPos, SQUARE_SIZE, SQUARE_SIZE);
        for (double row = 0; row < finalRow; row += 0.10) {
            // Find the x and y positions for each row and column
            xPos = (column - 1) * SQUARE_SIZE + BORDER_SIZE;
            yPos = (int) (TOP_OFFSET + row * SQUARE_SIZE);
            // Redraw the grid for this column
            for (int gridRow = 1; gridRow <= 6; gridRow++) {
                // Draw the squares
                offScreenBuffer.setColor(Color.blue);
                offScreenBuffer.drawRect((column - 1) * SQUARE_SIZE + BORDER_SIZE,
                        TOP_OFFSET + gridRow * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }

            // Draw each piece, depending on the value in board
            if (player == BANANA) {
                offScreenBuffer.drawImage(firstImage, xPos, yPos, SQUARE_SIZE, SQUARE_SIZE, this);
            } else if (player == STRAWBERRY) {
                offScreenBuffer.drawImage(secondImage, xPos, yPos, SQUARE_SIZE, SQUARE_SIZE, this);
            }
            // Transfer the offScreenBuffer to the screen
            g.drawImage(offScreenImage, 0, 0, this);
            delay(3);
            offScreenBuffer.clearRect(xPos + 1, yPos + 1, SQUARE_SIZE - 2, SQUARE_SIZE - 2);
        }
    }


    // Avoid flickering -- smoother graphics
    public void update(Graphics g) {
        paint(g);
    }


    public void paintComponent(Graphics g) {

        // Set up the offscreen buffer the first time paint() is called
        if (offScreenBuffer == null) {
            offScreenImage = createImage(this.getWidth(), this.getHeight());
            offScreenBuffer = offScreenImage.getGraphics();
        }

        // All of the drawing is done to an off screen buffer which is
        // then copied to the screen.  This will prevent flickering
        // Clear the offScreenBuffer first
        offScreenBuffer.clearRect(0, 0, this.getWidth(), this.getHeight());

        // Redraw the board with current pieces
        for (int row = 1; row <= 6; row++)
            for (int column = 1; column <= 7; column++) {
                // Find the x and y positions for each row and column
                int xPos = (column - 1) * SQUARE_SIZE + BORDER_SIZE;
                int yPos = TOP_OFFSET + row * SQUARE_SIZE;

                // Draw the squares
                offScreenBuffer.setColor(Color.blue);
                offScreenBuffer.drawRect(xPos, yPos, SQUARE_SIZE, SQUARE_SIZE);

                // Draw each piece, depending on the value in board
                if (board[row][column] == BANANA)
                    offScreenBuffer.drawImage(firstImage, xPos, yPos, SQUARE_SIZE, SQUARE_SIZE, this);
                else if (board[row][column] == STRAWBERRY)
                    offScreenBuffer.drawImage(secondImage, xPos, yPos, SQUARE_SIZE, SQUARE_SIZE, this);
            }

        // Draw next player
        if (!gameOver)
            if (currentPlayer == BANANA)
                offScreenBuffer.drawImage(firstImage, currentColumn * SQUARE_SIZE + BORDER_SIZE, TOP_OFFSET, SQUARE_SIZE, SQUARE_SIZE, this);
            else
                offScreenBuffer.drawImage(secondImage, currentColumn * SQUARE_SIZE + BORDER_SIZE, TOP_OFFSET, SQUARE_SIZE, SQUARE_SIZE, this);

        // Transfer the offScreenBuffer to the screen
        g.drawImage(offScreenImage, 0, 0, this);

    }


    /**
     * Purpose: To delay the given number of milliseconds
     *
     * @param milliSec The number of milliseconds to delay
     */
    private void delay(int milliSec) {
        try {
            Thread.sleep(milliSec);
        } catch (InterruptedException e) {
        }
    }


    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
        frame = new JFrame("Connect Four");
        Main myPanel = new Main();

        frame.add(myPanel);
        frame.pack();
        frame.setVisible(true);

    } // main method
} // ConnectFourWorking class
