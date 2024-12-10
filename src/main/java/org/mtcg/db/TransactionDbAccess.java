package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

public class TransactionDbAccess {
  private static Logger logger = Logger.getLogger(TransactionDbAccess.class.getName());

  public boolean buyPackage(UUID id) {
    // TODO:
    // Select Random Package from DB which has no FK for transactions table
    // Create a Stack Object with id = UUID.randomUUID()
    // userId and Cards in an ArrayList
    // Insert into Stack table the stack ID and its userId
    // make Batch for inserting StackID and cardIds in stack_cards
    // Set package is bought to true OR

    logger.info("Attempting to buy a package");
    Connection connection = null;
    try {
      connection = DbConnection.getConnection();
      connection.setAutoCommit(false);

      final String randomPkg = "Select * from packages ";
      var pkgStmt = connection.prepareStatement(randomPkg);

    } catch (SQLException e) {
      logger.severe("Failed to buy Package: " + e.getMessage());
    }
    throw new UnsupportedOperationException("Unimplemented method 'buyPackage'");
  }

}
