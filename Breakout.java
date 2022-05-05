package com.shpp.p2p.cs.svasilyev.assignment4;

import acm.graphics.*;
import com.shpp.cs.a.graphics.WindowProgram;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Classic game "Breakout" with changing of difficult in process of game
 */
public class Breakout extends WindowProgram {
    /**
     * Width and height of application window in pixels
     */
    public static final int APPLICATION_WIDTH = 400;
    public static final int APPLICATION_HEIGHT = 600;


    /**
     * Offset of the paddle up from the bottom
     */
    private static final int PADDLE_Y_OFFSET = 30;

    /**
     * Number of bricks per row
     */
    private static final int NBRICKS_PER_ROW = 10;

    /**
     * Number of rows of bricks
     */
    private static final int NBRICK_ROWS = 10;

    /**
     * Separation between bricks
     */
    private static final int BRICK_SEP = 4;

    /**
     * Width of a brick
     */
    private static final int BRICK_WIDTH = (APPLICATION_WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /**
     * Height of a brick
     */
    private static final int BRICK_HEIGHT = 8;

    /**
     * Radius of the ball in pixels
     */
    private static final int BALL_RADIUS = 10;

    /**
     * Offset of the top brick row from the top
     */
    private static final int BRICK_Y_OFFSET = 70;

    /**
     * Number of turns
     */
    private static final int NTURNS = 3;

    /**
     * Width and height of rocket
     */
    private static final int ROCKET_WIDTH = 60;
    private static final int ROCKET_HEIGHT = 10;


    GRect rocket;
    GOval ball;
    GRect[][] bricks;
    GLabel livesLabel;
    int countOfBricks;

    // define status of game: pause or run
    boolean gameStart;

    //speed in y and x coordinate
    double vx = 1;
    double vy = 1;
    // lives available
    int lives = NTURNS;
    // count how many bricks and bricks of what color are destroyed
    int count = 0;
    // time of pause between movement of ball, which decrease when difficult is growing
    double pauseTime = 10;

    public void run() {
        /*
        creating world and circumstances
         */
        rocket = createRocket();
        ball = createBall();
        int countOfBricks = createBricks();
        livesLabel = getLives();
        addMouseListeners();

        /*
        process of game
         */
        gamePause();
        // count so big because specific of calculation, see updateDifficult method
        // score when all bricks are broken
        while (lives != 0 && countOfBricks > 0) {
            runTheBall();
            positionateBallOnRocket();
            //println(count);
        }
        removeAll();
        printEnd();
    }

    /**
     * method which finish the program and messages user he won, or he lost.
     */
    private void printEnd() {
        GLabel endOfGame;
        if (lives == 0) endOfGame = new GLabel("Game over! You lose :(");
        else endOfGame = new GLabel("Congratulations! You won!");
        endOfGame.setFont("Helvetica-30");
        add(endOfGame, (getWidth() - endOfGame.getWidth()) / 2, (getHeight() - endOfGame.getHeight()) / 2);
    }

    /**
     * method which creates and updates lives in process of game
     *
     * @return Glabel with current value of lives
     */
    private GLabel getLives() {
        GLabel livesLabel = new GLabel("Lives: " + lives);
        livesLabel.setFont("Helvetica-20");
        add(livesLabel, 0, getHeight() - livesLabel.getDescent());
        return livesLabel;
    }

    /**
     * creates matrix of bricks with different colors
     */
    private int createBricks() {
        bricks = new GRect[NBRICK_ROWS][NBRICKS_PER_ROW];
        for (int i = 0; i < NBRICK_ROWS; i++)
            createRow(i);
        return bricks.length * bricks[0].length;
    }

    /**
     * creates one row of bricks
     *
     * @param rowIndex height of row
     */
    private void createRow(int rowIndex) {
        for (int i = 0; i < NBRICKS_PER_ROW; i++) {
            createBrick(rowIndex, i);
        }

    }

    /**
     * method creates one brick in matrix
     *
     * @param rowIndex      height of row
     * @param positionInRow where locates brick in this row
     */
    private void createBrick(int rowIndex, int positionInRow) {
        bricks[rowIndex][positionInRow] = new GRect((BRICK_WIDTH + BRICK_SEP) * positionInRow + BRICK_SEP,
                BRICK_Y_OFFSET + (BRICK_HEIGHT + BRICK_SEP) * rowIndex + BRICK_SEP, BRICK_WIDTH, BRICK_HEIGHT);

        bricks[rowIndex][positionInRow].setFilled(true);
        bricks[rowIndex][positionInRow].setColor(chooseColor(rowIndex));
        add(bricks[rowIndex][positionInRow]);
    }

    /**
     * choose correct color for this brick depends on its row
     *
     * @param rowIndex height of row
     * @return color which correct for this row
     */
    private Color chooseColor(int rowIndex) {
        int colorIndex = rowIndex / 2; // every 2 rows have same color
        Color color;
        if (colorIndex == 0) color = Color.RED;
        else if (colorIndex == 1) color = Color.ORANGE;
        else if (colorIndex == 2) color = Color.YELLOW;
        else if (colorIndex == 3) color = Color.GREEN;
        else color = Color.CYAN;

        return color;
    }


    /**
     * pause before game with welcome phrase
     */
    private void gamePause() {
        gameStart = false;
        GLabel welcome = new GLabel("Press left button of mouse to CRUSH!");
        welcome.setFont("Helvetica-20");

        // repeat appearance of welcome until game stats
        while (!gameStart) {
            add(welcome, getWidth() / 2 - welcome.getWidth() / 2, getHeight() / 2);
            pause(1000);
            remove(welcome);
            pause(100);
        }
    }

    /**
     * creates ball for game and return it
     *
     * @return ready to use ball for game
     */
    private GOval createBall() {
        // locating ball on the rocket
        GOval ball = new GOval(rocket.getX() + rocket.getWidth() / 2 - BALL_RADIUS / 2,
                rocket.getY() - rocket.getHeight(), BALL_RADIUS, BALL_RADIUS);
        ball.setFilled(true);
        add(ball);
        return ball;
    }

    /**
     * click listener which starts game
     *
     * @param e mouse listener
     */
    public void mouseClicked(MouseEvent e) {
        gameStart = true;
    }

    /**
     * method which make all main actions in game
     */
    private void runTheBall() {
        while (gameStart) {
            ball.move(vx, vy);
            analyzeEvents();
            pause(pauseTime);
        }
    }

    /**
     * analyze events when ball face with something
     */
    private void analyzeEvents() {

        GRectangle bounds = ball.getBounds();

        // situation when ball face with right or left border of window
        if (bounds.getX() + bounds.getWidth() > getWidth() || bounds.getX() < 0) vx = -vx;
        // upper border of window
        else if (bounds.getY() < 0) vy = -vy;
        //low border of window - player fail
        else if (bounds.getY() + bounds.getHeight() > getHeight()) {
            gameStart = false;
            lives--;
            remove(livesLabel);
            livesLabel = getLives();
            add(livesLabel);
        }
        // facing with rocket
        else if (bounds.intersects(rocket.getBounds())) {
            // case when ball face with corner of rocket
            if (bounds.getY() + bounds.getHeight() > rocket.getY()) {
                vx = -vx;
            }
            vy = -vy;
        }
        // facing with lives label
        else if (bounds.intersects(livesLabel.getBounds()));
        // check is brick
        else checkIsBrick();
    }

    /**
     * Check is ball facing with brick if yes - delete brick and change direction of move
     */
    private void checkIsBrick() {
        /*
        4 scenarios of facing with 4 corners of circle with bricks with remove of brick, update difficult
        and realistic changing direction after facing
         */

        if (getElementAt(ball.getX(), ball.getY()) != null) {
            // temp is copy of brick which faced with ball, we need it for characteristics of brick
            GObject temp = getElementAt(ball.getX(), ball.getY());
            if (temp.getY() + temp.getHeight() > ball.getY()) vy = -vy;
            else if (temp.getY() + temp.getHeight() < ball.getY()) vx = -vx;
            else {
                vx = -vx;
                vy = -vy;
            }
            updateDifficult(temp);
            remove(temp);
            countOfBricks--;
        }
        else if (getElementAt(ball.getX() + ball.getWidth(), ball.getY()) != null) {
            GObject temp = getElementAt(ball.getX() + ball.getWidth(), ball.getY());
            if (temp.getY() + temp.getHeight() > ball.getY()) vx = -vx;
            else if (temp.getY() + temp.getHeight() < ball.getY()) vy = -vy;
            else {
                vx = -vx;
                vy = -vy;
            }
            updateDifficult(temp);
            remove(temp);
            countOfBricks--;
        }
        else if ((getElementAt(ball.getX(), ball.getY() + ball.getHeight()) != null)) {
            GObject temp = getElementAt(ball.getX(), ball.getY() + ball.getHeight());
            if (temp.getY() < ball.getY()) vx = -vx;
            else if (temp.getY() > ball.getY()) vy = -vy;
            else {
                vx = -vx;
                vy = -vy;
            }
            updateDifficult(temp);
            remove(temp);
            countOfBricks--;
        }
        else if ((getElementAt(ball.getX() + ball.getWidth(), ball.getY() + ball.getHeight()) != null)) {
            GObject temp = getElementAt(ball.getX() + ball.getWidth(), ball.getY() + ball.getHeight());
            if (temp.getY() + temp.getHeight() > ball.getY()) vx = -vx;
            else if (temp.getY() + temp.getHeight() < ball.getY()) vy = -vy;
            else {
                vx = -vx;
                vy = -vy;
            }
            updateDifficult(temp);
            remove(temp);
            countOfBricks--;
        }
    }

    /**
     * Method calculates how many bricks are destroyed and check moments when difficult must be increased.
     * Big numbers made with purpose of increasing difficult when first ball of some color was hit,
     * no matter how many bricks have already been broken.
     * Total score number - 7310420
     * So this method has two purposes - update difficult and update total score to check when game is won
     * @param temp  is copy of brick which faced with ball, we need it for characteristics of brick
     */
    private void updateDifficult(GObject temp) {
        if (temp.getColor() == Color.RED) count += 350000;
        else if (temp.getColor() == Color.ORANGE) count += 15000;
        else if (temp.getColor() == Color.YELLOW) count += 500;
        else if (temp.getColor() == Color.GREEN) count += 20;
        else if (temp.getColor() == Color.CYAN) count++;

        if (count >= 350000) rocket.setSize(ROCKET_WIDTH / 2, ROCKET_HEIGHT);
        else if (count >= 15000) pauseTime = 2;
        else if (count > 500) pauseTime = 4;
        else if (count > 20) pauseTime = 6;
        else if (count > 1) pauseTime = 8;
    }

    /**
     * mouse listener to move rocket
     * @param e
     */
    public void mouseMoved(MouseEvent e) {
        moveRocket(e);
        if (!gameStart) positionateBallOnRocket();
    }

    /**
     * position on rocket when game not started
     */
    private void positionateBallOnRocket() {
        ball.setLocation(rocket.getX() + rocket.getWidth() / 2 - ball.getWidth() / 2,
                rocket.getY() - rocket.getHeight() - 1);
    }


    /**
     * method which holds rocket in borders of window and moves it follows the cursor of mouse
     * @param e motion listener
     */
    private void moveRocket(MouseEvent e) {
        if (e.getX() > getWidth() - rocket.getWidth())
            rocket.setLocation(getWidth() - rocket.getWidth(), getHeight() - PADDLE_Y_OFFSET);
        else rocket.setLocation(e.getX(), getHeight() - PADDLE_Y_OFFSET);
    }

    /**
     * creating rocket and add it to center of window
     * @return ready to use rocket
     */
    private GRect createRocket() {
        GRect rocket = new GRect(getWidth() / 2 - ROCKET_WIDTH / 2,
                getHeight() - PADDLE_Y_OFFSET, ROCKET_WIDTH, ROCKET_HEIGHT);
        rocket.setFilled(true);
        add(rocket);
        return rocket;
    }
}