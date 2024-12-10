package org.mtcg.models;

import java.util.UUID;

import lombok.Getter;

@Getter
public class Transaction {
  private final UUID Id;
  private final UUID user_Id;
  private final UUID package_Id;

  public Transaction(UUID id, UUID user_Id, UUID package_Id) {
    Id = id;
    this.user_Id = user_Id;
    this.package_Id = package_Id;
  }
}
