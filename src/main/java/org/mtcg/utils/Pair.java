package org.mtcg.utils;

public class Pair<F, S> {
  public F first;
  public S second;

  public Pair(F first, S second) {
    this.first = first;
    this.second = second;
  }

  public boolean isFull() {
    return (first != null && second != null);
  }

  @Override
  public String toString() {
    return "Pair{first=" + first + ", second=" + second + "}";
  }
}
