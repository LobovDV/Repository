package com.bookshop.BookShopApp.data;

import com.bookshop.BookShopApp.structure.book.Book;
import com.bookshop.BookShopApp.structure.payments.BalanceTransaction;
import com.bookshop.BookShopApp.structure.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<BalanceTransaction, Integer> {

    List<BalanceTransaction> findBalanceTransactionByUserId(Integer userId);

    @Modifying
    void deleteBalanceTransactionById(Integer TransactionId);

    @Query(value = "SELECT * FROM balance_transaction WHERE user_id = ?1 and value = ?2 and confirmed = 0", nativeQuery = true)
    BalanceTransaction findNotConfirmedTransactionByUseridAndSum(Integer userId, Integer sum);

    @Modifying
    @Query(value = "update balance_transaction set confirmed = 1 where id = ?1", nativeQuery = true)
    void modifyConfirmedTransaction(Integer transactionId);

    @Query(value =  "SELECT * FROM balance_transaction where user_id = ?1 order by time DESC ", nativeQuery = true)
    Page<BalanceTransaction> findTransactionsByUserIdDesc(Integer userId, Pageable nextPage);

}
