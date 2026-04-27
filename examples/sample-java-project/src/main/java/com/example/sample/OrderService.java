package com.example.sample;

import java.util.List;

public class OrderService {

    public int countPayableOrders(List<Integer> orderAmounts) {
        int count = 0;
        for (Integer amount : orderAmounts) {
            if (amount != null && amount > 0) {
                count++;
            }
        }
        return count;
    }
}
