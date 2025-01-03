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
  private CompletableFuture<String> firstUserFuture;

  // make a static instance so set Constructor to private
  private BattleLobby() {
  }

  // get the static instance or create one if not made yet
  public static synchronized BattleLobby getInstance() {
    if (instance == null) {
      instance = new BattleLobby();
    }
    return instance;
  }

  public synchronized CompletableFuture<String> addUserToLobby(final User user) {
    // Should never happen
    if (currentPair.isFull()) {
      throw new IllegalStateException("Unexpected state: currentPair is full but not reset.");
    }

    final CompletableFuture<String> battleFuture = new CompletableFuture<>();

    // put the user into the pair
    if (currentPair.first == null) {
      currentPair.first = user;
      // Store the future for the first user
      firstUserFuture = battleFuture;
      return battleFuture.thenApply(result -> result);
    } else if (currentPair.second == null) {
      // when second user is found add pair to List
      currentPair.second = user;
      userPairs.add(currentPair);
    }

    // When full reset Pair and remove from List
    if (currentPair.isFull()) {
      final Pair<User> battlePair = currentPair;
      currentPair = new Pair<>(null, null);
      userPairs.remove(battlePair);

      try {
        // Submit the battle to the executor service
        final var battleExecutor = new BattleExecutor(battlePair.first, battlePair.second);
        final Future<String> battleResult = battleExecutorService.submit(battleExecutor);

        // When battle is complete, complete both futures with the result meaning both
        // users get the same battleLog first user just waits until second joins lobby
        CompletableFuture.supplyAsync(() -> {
          try {
            final String result = battleResult.get();
            firstUserFuture.complete(result);
            battleFuture.complete(result);
            return result;
          } catch (final Exception e) {
            final String errorMsg = e.getMessage();
            firstUserFuture.complete(errorMsg);
            battleFuture.complete(errorMsg);
            return errorMsg;
          }
        }, battleExecutorService);

        return battleFuture;
      } catch (final Exception e) {
        final String errorMsg = e.getMessage();
        firstUserFuture.complete(errorMsg);
        battleFuture.complete(errorMsg);
        return CompletableFuture.completedFuture(errorMsg);
      }
    }

    return battleFuture.thenApply(result -> result);
  }

  public void shutdown() {
    battleExecutorService.shutdown();
  }
}
