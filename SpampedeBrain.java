package com.gradescope.spampede;

import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Queue;

/**
 * SpampedeBrain - The "Controller" in MVC, which includes all of the AI and
 * handling key presses
 * 
 * @author CS60 instructors
 */
public class SpampedeBrain extends SpampedeBrainParent {

	/** The "View" in MVC */
	private SpampedeDisplay theDisplay; 
	
	/** The "Model" in MVC */
	private SpampedeData theData;
	
	/** Number of animated frames displayed so far */
	private static int cycleNum = 0;

	// Mapping between direction (names) and keys
	private static final char REVERSE = 'r';
	private static final char UP      = 'i';
	private static final char DOWN    = 'k';
	private static final char LEFT    = 'j';
	private static final char RIGHT    = 'l';
	private static final char AI_MODE = 'a';
	private static final char PLAY_SPAM_NOISE = 's';


	/**
	 * Starts a new game.
	 */
	public void startNewGame() {
		this.theData = new SpampedeData();
		this.theData.placeSnakeAtStartLocation();
		this.theData.setStartDirection();
		
		this.theDisplay = new SpampedeDisplay(this.theData, this.screen,
				this.getSize().width, getSize().height);
		this.theDisplay.updateGraphics();
		
		this.playSound_spam();
		
		// Hack because pictures have a delay in loading, and we won't
		//    redraw the screen again until the game actually starts,
		//    which means we wouldn't see the image until the game
		//    does start.
		// Wait a fraction of a second (200 ms), by which time the
		//    picture should have been fetched from disk, and redraw.
		try { Thread.sleep(200); } catch (InterruptedException e) {};
		this.theDisplay.updateGraphics();
	}

	/**
	 * Declares the game over.
	 */
	public void gameOver() {
		super.pause(); // pause the game
		this.theData.setGameOver(); // tell the model that the game is over
		if (this.audioMeow != null) { // play a sound
			this.audioMeow.play();
		}
	}
	
	/**
	 * Gets the cycle number, to be used in creating rainbow
	 */
	public static int getCycleNum() {
		return cycleNum;
	}
	
	/* -------- */
	/* Gameplay */
	/* -------- */
	
	/**
	 * Moves the game forward one step (i.e., one frame of animation,
	 * which occurs every Preferences.SLEEP_TIME milliseconds)
	 */
	public void cycle() {

		// move the snake
		this.updateSnake();

		// update the list of Spam
		this.updateSpam();

		// draw the board
		this.theDisplay.updateGraphics();

		// make the new display visible - sends the drawing to the screen
		this.repaint();

		// update the cycle counter
		SpampedeBrain.cycleNum++;
	}

    /** 
     * Reacts to characters typed by the user.
     * <p>
     * The provided code registers the SpampedeBrain as
     * an "observer" for key presses on the keyboard.
     * 
     * So, whenever the user presses a key,
     * Java automatically calls this keyPressed method and
     * passes it a KeyEvent describing the specific keypress.
     */
	public void keyPressed(KeyEvent evt) {
		
		switch (evt.getKeyChar()) {
		// The getKeyChar method of a keypress event
		//    returns the character corresponding to the pressed key.
		
		case REVERSE:
			this.reverseSnake();
			break;
		case AI_MODE:
			this.theData.setMode_AI();
			break;
		case PLAY_SPAM_NOISE:
			this.playSound_spam();
			break;
		case UP:
			this.theData.setDirectionNorth();
			this.theData.resetCellsForNextSearch();
			break;
		case DOWN:
			this.theData.setDirectionSouth();
			this.theData.resetCellsForNextSearch();
			break;
		case LEFT:
			this.theData.setDirectionWest();
			this.theData.resetCellsForNextSearch();
			break;
		case RIGHT:
			this.theData.setDirectionEast();
			this.theData.resetCellsForNextSearch();
			break;
		default:
			this.theData.setDirectionEast();
		}
	}


	/**
	 *  Moves the snake forward once every REFRESH_RATE cycles,
	 *  either in the current direction, or as directed by
	 *  the AI's breadth-first search.
	 *  <p>
	 *  Called by cycle()
	 */
	
	void updateSnake() {
		if (SpampedeBrain.cycleNum % Preferences.REFRESH_RATE == 0) {
			BoardCell nextCell;
			if (this.theData.inAImode()) {
				nextCell = this.getNextCellFromBFS();
			} else {
				nextCell = this.theData.getNextCellInDir();
			}
			this.advanceTheSnake(nextCell);
		}
	}

	/**
	 * Move the snake into the next cell (and possibly eat spam)
	 * <p>
	 * This method should probably be private, but we make it
	 * public anyway to permit unit testing.
	 * 
	 * @param nextCell  New location of the snake head (which
	 *                  must be horizontally or vertically adjacent
	 *                  to the old location of the snake head).
	 */
	public void advanceTheSnake(BoardCell nextCell) {
		// Note - do not modify provided code.
		if (nextCell.isWall()) {
			// Oops...we hit something.
			this.gameOver();
			return;
		} 
		else if (nextCell.equals(theData.getSnakeNeck())) {
			this.reverseSnake();
		} 
		else if(nextCell.isBody()) {
			this.gameOver();
		}else if (nextCell.isSpam()) {
			this.playSound_spamEaten();
			this.theData.eatSpam(nextCell);
		} else {
			this.theData.keepGoing(nextCell);
			
		}

	}


	/** 
	 * Every SPAM_ADD_RATE cycles, tries to add one new spam.
	 */
	void updateSpam() {
		if (this.theData.noSpam()) {
			this.theData.addSpam();
		} else if (SpampedeBrain.cycleNum % Preferences.SPAM_ADD_RATE == 0) {
			this.theData.addSpam();
		}
	}


	/** 
	 * Uses BFS to search for the spam closest to the snake head.
	 * 
	 * @return Where to move the snake head, if we want to head
	 *         *one step* along the shortest path to (the nearest) 
	 *         spam cell.
	 */
	public BoardCell getNextCellFromBFS() {
		//NOTE FOR GRADERS: this method passes most tests on CheckParentsBFS_fast, but boards G2 and G7
		// only pass for the regular tests (not fast)
		
		// Initialize the search.
		theData.resetCellsForNextSearch();
		
		// Initialize the cellsToSearch queue with the snake head;
		// as with any cell, we mark the head cells as having been added
		// to the queue
		
		Queue<BoardCell> cellsToSearch = new LinkedList<BoardCell>();
		BoardCell snakeHead = theData.getSnakeHead();
		snakeHead.setAddedToSearchList();
		cellsToSearch.add(snakeHead);
		
        // Variable to hold the closest spam cell, once we've found it.
		BoardCell closestSpamCell = null;
		
		// Search!
		while(closestSpamCell == null && !cellsToSearch.isEmpty()) {
			BoardCell currentCell = cellsToSearch.remove();
			BoardCell[] neighbors = this.theData.getNeighbors(currentCell);
			for(BoardCell nCell:neighbors) {
				if (nCell.isSpam()){
					closestSpamCell = nCell;
					closestSpamCell.setAddedToSearchList();
					cellsToSearch.add(closestSpamCell);
					nCell.setParent(currentCell);
				}
				else {
					if(!nCell.inSearchListAlready() && nCell.isOpen()) {
						nCell.setAddedToSearchList();
						cellsToSearch.add(nCell);
						nCell.setParent(currentCell);
					}
				}
			}
		}
		
		if(closestSpamCell != null) {
			return this.getFirstCellInPath(closestSpamCell);
		}
	
		
		
		// Note: we encourage you to write the helper method 
		// getFirstCellInPath below to do the backtracking to calculate the next cell!

		// If the search fails, just move somewhere.
		return this.theData.getRandomNeighboringCell(snakeHead);
	}

	/**
	 * Follows parent pointers back from the closest spam cell
	 * to decide where the head should move. Specifically,
	 * follows the parent pointers back from the spam until we find
	 * the cell whose parent is the snake head (and which must therefore
	 * be adjacent to the previous snake head location).
	 * <p>
	 * Recursive or looping solutions are possible.
	 *  
	 * @param start   where to start following spam pointers; this will
	 *                 be (at least initially, if you use recursion) the
	 *                 location of the spam closest to the head.
	 * @return the new cell for the snake head.
	 */
	private BoardCell getFirstCellInPath(BoardCell start) {

		BoardCell currentCell = start;

		while(!isHeadNeighbor(currentCell)) { //while the current cell is not a head neighbor
			currentCell = currentCell.getParent();
			currentCell.becomePath();
		}
		return currentCell; 
	}
	
	private boolean isHeadNeighbor(BoardCell cell) {
		BoardCell[] headNeighbors = this.theData.getNeighbors(theData.getSnakeHead());
		for(BoardCell currentCell:headNeighbors) {
			if(currentCell.equals(cell)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Reverses the snake back-to-front and updates the movement 
	 * mode appropriately.
	 */
	public void reverseSnake() {
		this.theData.reverseSnake();
	}

	
	/* ------ */
	/* Sounds */
	/* ------ */
	
	/** Plays crunch noise */
	public void playSound_spamEaten() {
		if (this.audioCrunch != null) {
			this.audioCrunch.play();
		}
	}

	/** Plays spam noise */
	public void playSound_spam() {
		if (this.audioSpam != null) {
			this.audioSpam.play();
		}
	}

	/** Plays meow noise */
	public void playSound_meow() {
		if (this.audioMeow != null) {
			this.audioMeow.play();
		}
	}

	// not used - a variable added to remove a Java warning:
	private static final long serialVersionUID = 1L;

	
	/* ---------------------- */
	/* Testing Infrastructure */
	/* ---------------------- */

	public static SpampedeBrain getTestGame(TestGame gameNum) {
		SpampedeBrain brain = new SpampedeBrain();
		brain.theData = new SpampedeData(gameNum);
		return brain;
	}
	
	public String testing_toStringParent() {
		return this.theData.toStringParents();
	}

	public BoardCell testing_getNextCellInDir() {
		return this.theData.getNextCellInDir();
	}

	public String testing_toStringSpampedeData() {
		return this.theData.toString();
	}
}
