package com.bookshop.BookShopApp.services;

import com.bookshop.BookShopApp.data.TransactionRepository;
import com.bookshop.BookShopApp.structure.payments.BalanceTransaction;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.Column;
import javax.transaction.Transactional;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.time.LocalDateTime;

@Service
@Transactional
public class PaymentService {

    @Value("${robokassa.merchant.login}")
    private String merchantLogin;

    @Value("${robokassa.pass.first.test}")
    private String firstTestPass;

    @Value("${robokassa.pass.second.test}}")
    private String secondTestPass;

    private final TransactionRepository transactionRepository;

    @Autowired
    public PaymentService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public String getPaymentUrl(Integer sum, Integer invId) throws NoSuchAlgorithmException {
        return "https://auth.robokassa.ru/Merchant/Index.aspx"+
                "?MerchantLogin="+merchantLogin+
                "&IndId="+invId+
                "&Culture=ru"+
                "&Encoding=utf-8"+
                "&OutSum="+sum+
                "&SignatureValue="+ getSignatureValue(sum, invId, 0)+
                "&IsTest=1";
    }

    public String getSignatureValue(Integer sum, Integer invId, int pwdType) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        String pwd = pwdType == 0 ? firstTestPass : secondTestPass;
        md.update((merchantLogin + ":" + sum + ":" + invId + ":" + pwd).getBytes());
        return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
    }

    public Integer addTransaction(Integer userId, Integer value, Integer bookId, String description, Integer confirmed, String referer) {
        BalanceTransaction transaction = new BalanceTransaction();
        transaction.setUserId(userId);
        transaction.setValue(value);
        transaction.setBookId(bookId);
        transaction.setDescription(description);
        transaction.setConfirmed(confirmed);
        transaction.setTime(LocalDateTime.now());
        transaction.setReferer(referer);
        transactionRepository.save(transaction);
        return transaction.getId();
    }

    public BalanceTransaction getNotConfirmedTransactionByUseridAndSum(Integer userId, Integer sum) {
        return transactionRepository.findNotConfirmedTransactionByUseridAndSum(userId, sum);
    }

    public void updateConfirmedTransaction(Integer transactionId) {
        transactionRepository.modifyConfirmedTransaction(transactionId);
    }

    public void removeTransaction(Integer transactionId) {
        transactionRepository.deleteBalanceTransactionById(transactionId);
    }
}
