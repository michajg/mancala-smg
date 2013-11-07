package org.mancala.client.i18n;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages;

/**
 * Default Language Messages
 * 
 * @author Micha Guthmann
 */
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
@DefaultLocale("en")
public interface MancalaMessages extends Messages {

	@DefaultMessage("A new Game was added to your List of Games!")
	String newGameAdded();

	@DefaultMessage("Opponent: ")
	String opponent();

	@DefaultMessage("It is your Turn!")
	String itsYourTurn();

	@DefaultMessage("You play on the South side")
	String playOnSouthSide();

	@DefaultMessage("It is your Opponents Turn!")
	String opponentsTurn();

	@DefaultMessage("You play on the North side")
	String playOnNorthSide();

	@DefaultMessage("{0} made a move in game {1}. Your Game List was updated!")
	String opponentMadeMove(String opponentName, String matchId);

	@DefaultMessage("Waiting For Opponent")
	String waitForOpponent();

	@DefaultMessage("Invalid email address!")
	String invalidEmail();

	@DefaultMessage("An error occurred on the server!")
	String serverError();

	@DefaultMessage("{0} has not registered yet!")
	String opponentNotRegistered(String opponentId);

	@DefaultMessage("Error loading match!")
	String loadMatchError();

	@DefaultMessage("It is the Turn of {0}!")
	String turnOfOpponent(String opponentName);

	@DefaultMessage("Error while deleting match!")
	String deleteMatchError();

	@DefaultMessage("Start new or select a previous Game")
	String startNewGame();

	@DefaultMessage("--Select Match--")
	String selectMatch();

	@DefaultMessage("Your Turn")
	String yourTurn();

	@DefaultMessage("Their Turn")
	String theirTurn();

	@DefaultMessage("Game is over, it ended in a Tie")
	String tie();

	@DefaultMessage("Game is over, you won with {0} against {1} points")
	String youWon(String winnerScore, String loserScore);

	@DefaultMessage("Game is over, you lost with {0} against {1} points")
	String youLost(String loserScore, String winnerScore);

	@DefaultMessage("You can only choose one of your own pits with at least one seed when it is your turn!")
	String warnMessage();

	@DefaultMessage("{0} not found!")
	String opponent404(String opponentId);

	@DefaultMessage("New game because of error: ")
	String newGameBecauseError();

	@DefaultMessage("Game Over! The winner is {0} with a score of {1}.")
	String winnerIs(String winnerName, String winnerScore);

	@DefaultMessage("Play again")
	String playAgain();

	@DefaultMessage("Game against AI as North")
	String GameAiNorth();

	@DefaultMessage("Game against AI as South")
	String GameAiSouth();

	@DefaultMessage("Game against random real Person")
	String randomNewGame();

	@DefaultMessage("Opponents email address")
	String opponentsEmail();

	@DefaultMessage("Play!")
	String play();

	@DefaultMessage("Delete Game")
	String deleteGame();

	@DefaultMessage("AI makes a Move")
	String aiMakesMove();
}