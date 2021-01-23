package com.gradescope.spampede;

import java.awt.Color;
import java.awt.Font;

/**
 * Preferences - to avoid "magic numbers" within other code all unchanging
 * variables should be stored here!
 * 
 * @author CS60 instructors
 */
public final class Preferences {
	// Timing:
	public static final int REFRESH_RATE = 2;
	public static final int SPAM_ADD_RATE = 25;
	public static final int SLEEP_TIME = 30; // milliseconds between updates
	// Sizing:
	public static final int NUM_CELLS_WIDE = 50;
	public static final int NUM_CELLS_TALL = 30;
	public static final int CELL_SIZE = 10;
	private static final int SPACE_FOR_BUTTONS = 190;
	public static final int GAMEBOARDHEIGHT = NUM_CELLS_TALL * CELL_SIZE
			+ SPACE_FOR_BUTTONS;
	// Colors:
	public static final Color COLOR_BACKGROUND = Color.WHITE;
	//public static final Color COLOR_WALL = Color.RED; // wall color is actually rotating rainbow colors. See get cell color in BoardCell class.
	public static final Color COLOR_SPAM = Color.RED;
	public static final Color COLOR_OPEN = Color.WHITE;
	public static final Color COLOR_HEAD = Color.DARK_GRAY;
	public static final Color COLOR_BODY = Color.GRAY;
	public static final Color COLOR_PATH = new Color(255,126,126); // light red color
	
	// Text display - Title:
	public static final int TITLE_X = 100;
	public static final int TITLE_Y = 40;
	public static final Font TITLE_FONT = new Font("Helvetica", Font.PLAIN, 30);	
	public static final Color TITLE_COLOR = Color.RED;
	public static final String TITLE = "Spampede";
	// Text display - Game Over
	public static final int GAME_OVER_X = 150;
	public static final int GAME_OVER_Y = 200;
	public static final Font GAME_OVER_FONT = new Font("Helvetica", Font.PLAIN, 60);	
	public static final Color GAME_OVER_COLOR = Color.RED;
	public static final String GAME_OVER_TEXT = "Game Over";
	// Text display - Score
	public static final int SCORE_X = 300;
	public static final int SCORE_Y = 40;
	public static final Font SCORE_FONT = new Font("Helvetica", Font.PLAIN, 20);	
	public static final Color SCORE_COLOR = Color.RED;

	


}
