package com.bookshop.BookShopApp.data;

import com.bookshop.BookShopApp.structure.payments.BalanceTransaction;

import java.util.List;

public class TransactionsPageDto {

    private int count;
    private List<BalanceTransaction> transactions;

    public TransactionsPageDto(List<BalanceTransaction> transactions) {
        this.count = transactions.size();
        this.transactions = transactions;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<BalanceTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<BalanceTransaction> transactions) {
        this.transactions = transactions;
    }
}
