package com.github.milomarten.fracktail.core.dice.table.multi;

import com.github.milomarten.fracktail.core.dice.table.RandomlySelected;

import java.util.List;

public interface MultiRandomlySelected<T> extends RandomlySelected<T> {
    List<T> getMultiple(MultiRollParams params);
}
