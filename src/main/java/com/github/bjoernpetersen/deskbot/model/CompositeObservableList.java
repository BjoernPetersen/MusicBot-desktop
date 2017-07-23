package com.github.bjoernpetersen.deskbot.model;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.collections.WeakListChangeListener;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An immutable {@link ObservableList} implementation that is composed of two observable lists. Does
 * not implement optional operations like {@link #add(Object)}.
 */
@ParametersAreNonnullByDefault
public final class CompositeObservableList<T> extends ObservableListBase<T> {

  private final ObservableList<? extends T> list1;
  private final ObservableList<? extends T> list2;
  private final ListChangeListener<T> listener;

  /**
   * Creates an empty instance.
   */
  public CompositeObservableList() {
    this(FXCollections.emptyObservableList());
  }

  /**
   * Creates a composite list based on a single list. Convenience method for {@link
   * #CompositeObservableList(ObservableList, ObservableList) new CompositeObservableList(list,
   * Collections.emptyList())}.
   *
   * @param list the list to wrap
   */
  public CompositeObservableList(ObservableList<? extends T> list) {
    this(list, FXCollections.emptyObservableList());
  }

  /**
   * Creates an instance containing all elements of list1, followed by all elements of list2.
   *
   * @param list1 the first list
   * @param list2 the second list
   */
  public CompositeObservableList(ObservableList<? extends T> list1,
      ObservableList<? extends T> list2) {
    this.list1 = list1;
    this.list2 = list2;
    listener = this::fireChange;
    registerListener(this.list1);
    registerListener(this.list2);
  }

  /**
   * Creates a new composite list which is created as a union of this list and the specified other
   * list.
   *
   * @param other the other list
   * @return a new list composed of this and the other list
   */
  public CompositeObservableList<T> union(ObservableList<? extends T> other) {
    return new CompositeObservableList<>(this, other);
  }

  private void registerListener(ObservableList<? extends T> list) {
    list.addListener(new WeakListChangeListener<>(listener));
  }

  @Override
  public T get(int index) {
    if (index < list1.size()) {
      return list1.get(index);
    } else {
      return list2.get(index - list1.size());
    }
  }

  @Override
  public int size() {
    return list1.size() + list2.size();
  }
}