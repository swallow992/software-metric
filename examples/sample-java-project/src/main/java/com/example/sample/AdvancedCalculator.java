package com.example.sample;

import java.util.ArrayList;
import java.util.List;

public class AdvancedCalculator extends Calculator {

    private final List<Integer> history = new ArrayList<>();

    public int multiply(int left, int right) {
        int result = left * right;
        history.add(result);
        return result;
    }

    public int lastResult() {
        if (history.isEmpty()) {
            return 0;
        }
        return history.get(history.size() - 1);
    }
}
