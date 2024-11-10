package org.mtcg.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mtcg.db.PackageDbAccess;
import org.mtcg.db.UserDbAccess;
import org.mtcg.models.Card;
import org.mtcg.models.Card.CardType;
import org.mtcg.models.Card.Element;
import org.mtcg.models.Package;

@ExtendWith(MockitoExtension.class)
public class PackageControllerTest {
  @Mock
  private PackageDbAccess pkgDbAccess;

  @Mock
  private UserDbAccess userDbAccess;

  @InjectMocks
  private PackageController packageController;

  private UUID userId;

  @BeforeEach
  public void setUp() {
    userId = UUID.randomUUID();
  }

  public Card[] createCards() {
    return new Card[] {
        new Card(UUID.randomUUID(), "Ork", 50.0f, CardType.MONSTER, Element.NORMAL),
        new Card(UUID.randomUUID(), "Water Spell", 30.5f, CardType.SPELL, Element.WATER),
        new Card(UUID.randomUUID(), "Knight", 40.0f, CardType.MONSTER, Element.NORMAL),
        new Card(UUID.randomUUID(), "Fire Dragon", 45.0f, CardType.MONSTER, Element.FIRE),
        new Card(UUID.randomUUID(), "KnifeSpell", 35.0f, CardType.SPELL, Element.NORMAL)
    };
  }

  @Test
  void testAddPackage() {
    Package pkg = new Package(createCards(), userId);
    when(pkgDbAccess.addPackage(pkg)).thenReturn(true);
    boolean added = pkgDbAccess.addPackage(pkg);
    assertTrue(added);
    verify(pkgDbAccess, times(1)).addPackage(pkg);
  }
}
