package org.mtcg.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mtcg.models.Trade;

public class TradingDbAccess {
  private static final Logger logger = Logger.getLogger(TradingDbAccess.class.getName());

  public List<Trade> getTradingDeals() {
    final var tradeList = new ArrayList<Trade>();
    try (final var connection = DbConnection.getConnection()) {
      final String sql = "Select * FROM trading_deals";
      try (final var Stmt = connection.prepareStatement(sql)) {
        try (final var result = Stmt.executeQuery()) {
          while (result.next()) {
            tradeList.add(new Trade(
                (UUID) result.getObject("id"),
                (UUID) result.getObject("card_id"),
                result.getString("required_card_type"),
                result.getDouble("min_damage"),
                (UUID) result.getObject("user_id")));
          }
        }
      }
    } catch (SQLException e) {
      logger.severe("Failed to list Trading Deals: " + e.getMessage());
      return null;
    }

    return tradeList;
  }

  public boolean insertDeal(Trade trade) throws SQLException {
    try (final var connection = DbConnection.getConnection()) {
      final String insertSQL = "INSERT INTO trading_deals (id, user_id, card_id, required_card_type, min_damage) VALUES (?, ?, ?, ?, ?)";
      try (final var stmt = connection.prepareStatement(insertSQL)) {
        stmt.setObject(1, trade.getId());
        stmt.setObject(2, trade.getUserId());
        stmt.setObject(3, trade.getCardId());
        stmt.setObject(4, trade.getRequiredType());
        stmt.setObject(5, trade.getMinDamage());
        final int userAffectedRows = stmt.executeUpdate();
        if (userAffectedRows == 0) {
          throw new SQLException("Failed to insert user record into the database.");
        } else {
          return true;
        }
      }
    } catch (final SQLException e) {
      if ("23505".equals(e.getSQLState())) {
        throw new SQLException("Conflict: A deal with this deal ID already exists.", e);
      } else {
        logger.severe("Failed to configure Deck: " + e.getMessage());
        return false;
      }
    }
  }

  public boolean deleteDeal(UUID tradeId) {
    final String deleteDealSQL = "DELETE FROM trading_deals WHERE id = ?";
    try (final var connection = DbConnection.getConnection();
        final var stmt = connection.prepareStatement(deleteDealSQL)) {
      stmt.setObject(1, tradeId);
      int rowsAffected = stmt.executeUpdate();

      return rowsAffected > 0;
    } catch (SQLException e) {
      logger.severe("Failed to delete trading deal with ID " + tradeId + ": " + e.getMessage());
      return false;
    }
  }

  public Trade getTradeFromId(UUID tradeId) throws SQLException {
    try (final var connection = DbConnection.getConnection()) {
      final String sql = "SELECT * FROM trading_deals WHERE id = ?";
      final var preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setObject(1, tradeId);

      try (final var result = preparedStatement.executeQuery()) {
        if (result.next()) {
          logger.info("Trade Deal retrieved successfully: " + tradeId);
          final var trade = new Trade(
              (UUID) result.getObject("id"),
              (UUID) result.getObject("card_id"),
              result.getString("required_card_type"),
              result.getDouble("min_damage"),
              (UUID) result.getObject("user_id"));
          return trade;
        } else {
          logger.warning("Trade with ID: " + tradeId + " not found.");
        }
      }
    } catch (final SQLException e) {
      logger.log(Level.SEVERE, "SQL error while retrieving Trade Deal: " + tradeId, e);
    }
    return null;
  }

  public boolean completeTrade(UUID tradeId, UUID id, UUID cardId) {
    return false;
  }

}
