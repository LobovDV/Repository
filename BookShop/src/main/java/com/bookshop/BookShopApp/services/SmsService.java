package com.bookshop.BookShopApp.services;


//import com.twilio.Twilio;
//import com.twilio.exception.ApiException;
//import com.twilio.rest.api.v2010.account.Message;
//import com.twilio.type.PhoneNumber;
import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SmsService {

    //Twilio
//    @Value("${twilio.ACCOUNT_SID}")
//    private String ACCOUNT_SID;
//
//    @Value("${twilio.ACCOUNT_SID}")
//    private String AUTH_TOKEN;
//
//    @Value("${twilio.ACCOUNT_SID}")
//    private String TWILIO_NUMBER;

//    public String sendSecretCodeSms(String contact) {
//        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
//        String formattedContact = contact.replaceAll("[\\s+()-]", "");
//        String generatedCode = generateCode();
//        Message.creator(
//                new PhoneNumber(formattedContact),
//                new PhoneNumber(TWILIO_NUMBER),
//                "Your secret code is: " + generatedCode
//        ).create();
//        return generatedCode;
//    }

    //Vonage
    @Value("${vonage.API_KEY}")
    private String API_KEY;

    @Value("${vonage.API_SECRET}")
    private String API_SECRET;

    public String sendSecretCodeSms(String contact, String code) {
        VonageClient client = VonageClient.builder().apiKey(API_KEY).apiSecret(API_SECRET).build();
        String formattedContact = contact.replaceAll("[\\s+()-]", "");
        TextMessage message = new TextMessage("BookShop", formattedContact, "Security code: " + code + " valid 3 minutes");
        SmsSubmissionResponse response = client.getSmsClient().submitMessage(message);
        if (response.getMessages().get(0).getStatus() == MessageStatus.OK) {
            return code;
        } else {
            return "";
        }
    }

    public String generateCode() {
        //nnn nnn - pattern
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 6) {
            sb.append(random.nextInt(9));
        }
        sb.insert(3, " ");
        return sb.toString();
    }



}
