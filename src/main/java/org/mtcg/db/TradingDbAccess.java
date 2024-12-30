package org.mtcg.db;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.mtcg.models.Trade;

public class TradingDbAccess {

  public List<Trade> getTradingDeals() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getTradingDeals'");
  }

  public boolean insertDeal(Trade trade) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'insertDeal'");
  }

  public boolean deleteDeal(UUID tradeId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'deleteDeal'");
  }

  public Trade getTradeFromId(UUID tradeId) throws SQLException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getTradeFromId'");
  }

  public boolean completeTrade(UUID tradeId, UUID id, UUID cardId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'completeTrade'");
  }

}
