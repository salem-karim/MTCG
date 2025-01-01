package org.mtcg.utils;

public class Pair<T> {
  public T first;
  public T second;

  public Pair(T first, T second) {
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
