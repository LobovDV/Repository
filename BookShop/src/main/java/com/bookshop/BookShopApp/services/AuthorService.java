package com.bookshop.BookShopApp.services;
import com.bookshop.BookShopApp.data.AuthorRepository;
import com.bookshop.BookShopApp.structure.author.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthorService {

    private AuthorRepository authorRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }


    public Author getAuthorById(Integer authorId) {
        return authorRepository.findAuthorById(authorId);
    }

    public Map<String, List<Author>> getAuthorsMap() {
       List<Author> authors = authorRepository.findAll();
       return authors.stream().collect(Collectors.groupingBy((Author a) -> {
                return a.getName().substring(0,1);
               }));
    }

    public String getParagraphDescriptionByNum(Integer authorId, Integer paragraphNum) {
        Author author = getAuthorById(authorId);
        String description = author.getDescription();
        String[] listParagraphs = description.split("\\.");
        if (listParagraphs.length >= paragraphNum) {
            return listParagraphs[paragraphNum-1];
        } else return "";
    }

    public List<String> getListParagraphDescription(Integer authorId, Integer startNum) {
        Author author = getAuthorById(authorId);
        String description = author.getDescription();
        String[] listParagraphs = description.split("\\.");
        List<String> result = new ArrayList<>();
        result.addAll(Arrays.asList(listParagraphs).subList(startNum, listParagraphs.length-1));
        return result;
    }

}
