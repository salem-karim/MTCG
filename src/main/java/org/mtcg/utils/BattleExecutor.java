package org.mtcg.utils;

import org.mtcg.models.Deck;
import org.mtcg.models.User;
import org.mtcg.db.DeckDbAccess;

import java.util.concurrent.Callable;

public class BattleExecutor implements Callable<String> {
  private final User user1;
  private final User user2;

  public BattleExecutor(User user1, User user2) {
    this.user1 = user1;
    this.user2 = user2;
  }

  @Override
  public String call() throws NullPointerException {
    // Battle logic here
    DeckDbAccess deckDbAccess = new DeckDbAccess();
    var deck1 = deckDbAccess.getDeckCards(deckDbAccess.getDeckId(user1.getId()));
    var deck2 = deckDbAccess.getDeckCards(deckDbAccess.getDeckId(user2.getId()));

    return performBattle(deck1, deck2); // Return battle log
  }

  private String performBattle(Deck deck1, Deck deck2) throws NullPointerException {
    if (deck1 == null || deck2 == null) {
      throw new NullPointerException("One of the user's decks are null");
    }
    // Implement battle logic
    return "Battle log placeholder";
  }
}
