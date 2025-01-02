package org.mtcg.utils.battle;

import org.mtcg.models.User;
import org.mtcg.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.*;

public class BattleLobby {
  private static BattleLobby instance;
  private final ExecutorService battleExecutorService = Executors.newFixedThreadPool(10);
  private final List<Pair<User>> userPairs = new ArrayList<>();
  private Pair<User> currentPair = new Pair<>(null, null);

  private BattleLobby() {
  }

  public static synchronized BattleLobby getInstance() {
    if (instance == null) {
      instance = new BattleLobby();
    }
    return instance;
  }

  public synchronized Future<String> addUserToLobby(User user) {
    if (currentPair.isFull()) {
      throw new IllegalStateException("Unexpected state: currentPair is full but not reset.");
    }

    if (currentPair.first == null) {
      currentPair.first = user;
    } else if (currentPair.second == null) {
      currentPair.second = user;
      userPairs.add(currentPair);
    }

    if (currentPair.isFull()) {
      Pair<User> battlePair = currentPair;
      currentPair = new Pair<>(null, null); // Reset for new users
      userPairs.remove(battlePair);

      try {
        // Submit the battle to the executor service
        BattleExecutor battleExecutor = new BattleExecutor(battlePair.first, battlePair.second);
        return battleExecutorService.submit(battleExecutor);
      } catch (Exception e) {
        return CompletableFuture.completedFuture(e.getMessage());
      }
    }

    return CompletableFuture.completedFuture("Waiting for an opponent...");
  }

  public void shutdown() {
    battleExecutorService.shutdown();
  }
}
