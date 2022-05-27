package com.bookshop.BookShopApp.services;

import com.bookshop.BookShopApp.data.UserRepository;
import com.bookshop.BookShopApp.structure.enums.ContactType;
import com.bookshop.BookShopApp.structure.user.BookstoreUserDetails;
import com.bookshop.BookShopApp.structure.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BookstoreUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public BookstoreUserDetailsService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userRepository.findBookstoreUserByContact(s, ContactType.EMAIL.ordinal());
        if(user!=null){
            return new BookstoreUserDetails(user, s);
        }
        user = userRepository.findBookstoreUserByContact(s, ContactType.PHONE.ordinal());
        if(user!=null){
            return new BookstoreUserDetails(user, s);
        }
        throw new UsernameNotFoundException("user not found!");
    }
}

