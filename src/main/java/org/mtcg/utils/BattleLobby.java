package org.mtcg.utils;

import org.mtcg.models.User;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.*;

public class BattleLobby {
  private static BattleLobby instance;
  private final ExecutorService battleExecutorService = Executors.newFixedThreadPool(10);
  private final List<Pair<User, User>> userPairs = new ArrayList<>();
  private Pair<User, User> currentPair = new Pair<>(null, null);

  private BattleLobby() {
  }

  public static synchronized BattleLobby getInstance() {
    if (instance == null) {
      instance = new BattleLobby();
    }
    return instance;
  }

  public synchronized Future<String> addUserToLobby(User user) throws InterruptedException {
    if (currentPair.isFull()) {
      Pair<User, User> battlePair = currentPair;
      currentPair = new Pair<>(null, null); // Reset for new users
      userPairs.remove(battlePair);

      // Submit the battle to the executor service
      BattleExecutor battleExecutor = new BattleExecutor(battlePair.first, battlePair.second);
      return battleExecutorService.submit(battleExecutor); // Returns a Future
    }

    // Add user to the current pair
    if (currentPair.first == null) {
      currentPair.first = user;
    } else if (currentPair.second == null) {
      currentPair.second = user;
      userPairs.add(currentPair); // Add pair to the list
    }

    return CompletableFuture.completedFuture("Waiting for an opponent...");
  }

  public void shutdown() {
    battleExecutorService.shutdown();
  }
}
