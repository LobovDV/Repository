package com.bookshop.BookShopApp.controllers;

import com.bookshop.BookShopApp.data.BooksPageDto;
import com.bookshop.BookShopApp.data.TransactionsPageDto;
import com.bookshop.BookShopApp.errors.BookstoreApiWrongParameterException;
import com.bookshop.BookShopApp.services.BookService;
import com.bookshop.BookShopApp.services.PaymentService;
import com.bookshop.BookShopApp.services.UserRegister;
import com.bookshop.BookShopApp.services.UserService;
import com.bookshop.BookShopApp.structure.book.Book;
import com.bookshop.BookShopApp.structure.payments.BalanceTransaction;
import com.bookshop.BookShopApp.structure.user.User;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRegister userRegister;
    private final BookService bookService;
    private final UserService userService;

    @Autowired
    public PaymentController(PaymentService paymentService, UserRegister userRegister, BookService bookService, UserService userService) {
        this.paymentService = paymentService;
        this.userRegister = userRegister;
        this.bookService = bookService;
        this.userService = userService;
    }

    @PostMapping("/payment")
    @ResponseBody
    public Map<String, Object> handlePayment (@RequestBody Map<String, String> body, HttpServletRequest request) throws NoSuchAlgorithmException {
        String referer = request.getHeader("referer");
        Integer sum = Integer.parseInt(body.get("sum"));
        Map<String, Object> result = new HashMap<>();
        result.put("result", true);
        if (!userRegister.getAuthenticationStatus()) {
            result.put("url", "http://localhost:8085/signin");
            return result;
        }
        User user = userRegister.getCurrentUser();
        Integer userId = user.getId();

        if (referer.contains("cart")) {
            result.put("url", referer);
            if (user.getBalance() >= sum) {
                List<Book> currentUserBooks = bookService.getBooksByUserIdAndStatus(userId, "CART");
                for (Book book : currentUserBooks ) {
                    paymentService.addTransaction(userId, -sum, book.getId(), "Pay book " + book.getTitle(), 1, referer);
                    bookService.updateBookStatusByBookIdAndUserId(book.getId(), userId, "PAID");
                }
                userService.updateUserBalance(user.getBalance()-sum, userId);
                result.put("message", "Success paid!");
                return result;
            } else {
                result.put("result", false);
                result.put("message", "Not enough money. Refill balance!");
                return result;
            }
        }

        Integer transactionId = 0;
        if (sum > 0) {
            transactionId = paymentService.addTransaction(userId, sum, 0, "Refill balance", 0, referer);
        }
        //  String paymentUrl = paymentService.getPaymentUrl(sum, transactionId);
        //Заглушка приводящая сразу на метод обработки resultUrl Robokassa. Вернуть из робокассы не получится так как работает не на сайте а на localhost
        String paymentUrl = "http://localhost:8085/payment/result?OutSum="+sum+"&InvId="+transactionId+"&SignatureValue="+paymentService.getSignatureValue(sum,transactionId,1);
        result.put("url", paymentUrl);
        return result;
    }


    @GetMapping("/payment/result")
    public ModelAndView handlePaymentResult(@RequestParam("OutSum") Integer outSum, @RequestParam("InvId") Integer invId, @RequestParam("SignatureValue") String signatureValue, RedirectAttributes attributes) throws NoSuchAlgorithmException {
        User user = userRegister.getCurrentUser();
        Integer userId = user.getId();
        if (outSum == 0) {
            attributes.addFlashAttribute("payAttribute", "Value must be > 0");
            return new ModelAndView("redirect:/profile");
        }

        BalanceTransaction transaction = paymentService.getNotConfirmedTransactionByUseridAndSum(userId, outSum);
        if(transaction != null) {
            String correctSignatureValue = paymentService.getSignatureValue(transaction.getValue(), transaction.getId(), 1);
            if (correctSignatureValue.equals(signatureValue)) {
                paymentService.updateConfirmedTransaction(transaction.getId());
                userService.updateUserBalance(user.getBalance() + outSum, userId);
                attributes.addFlashAttribute("payAttribute", "Pay success");
                return new ModelAndView("redirect:/profile");
            } else {
                paymentService.removeTransaction(transaction.getId());
                attributes.addFlashAttribute("payAttribute", "Pay not correct");
            }
        } else { attributes.addFlashAttribute("payAttribute", "Pay not found"); }
        return new ModelAndView("redirect:/profile");
    }

    @GetMapping("/payment/fail")
    public ModelAndView handlePaymentFail(@RequestParam("InvId") Integer invId, RedirectAttributes attributes){
        attributes.addFlashAttribute("payAttribute", "Pay failed");
        return new ModelAndView("redirect:/profile");
    }


    @GetMapping("/api/transactions")
    @ApiOperation("operation to get user transactions list order time (descending), parameters userId, offset, limit - Integer")
    @ResponseBody
    public TransactionsPageDto getPopularBooksPage(@RequestParam("userId") Integer userId, @RequestParam("offset") Integer offset,
                                            @RequestParam("limit") Integer limit) throws BookstoreApiWrongParameterException, NumberFormatException {
        if ((offset == null) || (limit == null)){
            throw new BookstoreApiWrongParameterException("Wrong values passed to one or more parameters");
        } else {
            TransactionsPageDto transactionsPageDto = new TransactionsPageDto(userService.getPageOfTransactions(userId, offset, limit).getContent());
            int count = userService.getPageOfTransactions(userId, 0, 1000000000).getContent().size();
            transactionsPageDto.setCount(count);
            return transactionsPageDto;
        }
    }
}
