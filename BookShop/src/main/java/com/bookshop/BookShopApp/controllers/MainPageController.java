package com.bookshop.BookShopApp.controllers;

import com.bookshop.BookShopApp.services.*;
import com.bookshop.BookShopApp.data.SearchWordDto;
import com.bookshop.BookShopApp.errors.EmptySearchException;
import com.bookshop.BookShopApp.structure.author.Author;
import com.bookshop.BookShopApp.structure.book.Book;
import com.bookshop.BookShopApp.structure.tag.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;



@Controller
public class MainPageController {

    private final BookService bookService;
    private final TagService tagService;
    private final AuthorService authorService;
    private final GenreService genreService;
    private final UserService userService;
    private final UserRegister userRegister;

    @Autowired
    public MainPageController(BookService bookService, TagService tagService, AuthorService authorService, GenreService genreService, UserService userService, UserRegister userRegister) {
        this.bookService = bookService;
        this.tagService = tagService;
        this.authorService = authorService;
        this.genreService = genreService;
        this.userService = userService;
        this.userRegister = userRegister;
    }

    @ModelAttribute("recommendedBooks")
    public List<Book> recommendedBooks(){
        return bookService.getPageOfRecommendedBooks(0, 20).getContent();
    }

    @ModelAttribute("recentBooks")
    public List<Book> recentBooks(){
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, -1);
        return bookService.getPageOfRecentBooks(calendar.getTime(), new Date(), 0, 20).getContent();
    }

    @ModelAttribute("popularBooks")
    public List<Book> popularBooks(){
        return bookService.getPageOfPopularBooks(0, 20).getContent();
    }

    @ModelAttribute("searchResults")
    public List<Book> searchResults() {
        return new ArrayList<>();
    }

    @ModelAttribute("searchCount")
    public int searchResultCount() {
        return 0;
    }

    @ModelAttribute("tags")
    public List<Tag> tags() {
        return tagService.getAllTags();
    }

    @ModelAttribute("authorsMap")
    public Map<String,List<Author>> authorsMap(){
        return authorService.getAuthorsMap();
    }

    @GetMapping("/")
    public String mainPage(@CookieValue(value = "bookShop", required = false) String bookShop, HttpServletResponse response) {

        userService.removeAnonymousUsersMoreThanThirtyDays();
        genreService.setAllGenresBooksCount();
        tagService.setAllTagsBooksCount();
        bookService.setAllReviewRatings();
        return "index";
    }

    @GetMapping("/about")
    public String aboutPage(){
        return "about";
    }

    @GetMapping("/faq")
    public String faqPage(){
        return "faq";
    }

    @GetMapping("/contacts")
    public String contactsPage(){
        return "contacts";
    }



    @GetMapping(value = {"/search", "/search/{searchWord}"})
    public String getSearchResult(@PathVariable(value = "searchWord", required = false) SearchWordDto searchWordDto,
                                  Model model) throws EmptySearchException {
        if(searchWordDto!=null){
            Page<Book> pages = bookService.getPageOfSearchResultBooks(searchWordDto.getExample(), 0, 20);
            model.addAttribute("searchWordDto", searchWordDto);
            model.addAttribute("searchResults", pages.getContent());
            model.addAttribute("searchCount", pages.getTotalElements());
            return "/search/index";
        }else {
            throw new EmptySearchException("Missing search word");
        }

    }

    @GetMapping("/books/popular")
    public String popularBookPage(){
        return "/books/popular";
    }

    @GetMapping("/books/recent")
    public String recentBookPage(){
        return "/books/recent";
    }


    @GetMapping(value= {"/tags","/tags/{tagId}"})
    public String booksByTagsPage(@PathVariable(value = "tagId", required = true) Integer tagId, Model model){
        Page<Book> pages = bookService.getPageOfBooksByTagId(tagId, 0, 20);
        model.addAttribute("tag", tagService.getTagById(tagId));
        model.addAttribute("booksByTagId", pages.getContent());
        return "/tags/index";
    }


    @GetMapping("/genres")
    public String genrePage(Model model){
        model.addAttribute("genresHtml", genreService.getAllGenreString());
        return "/genres/index";
    }

    @GetMapping("/genre/{genreId}")
    public String booksByGenrePage(@PathVariable(value = "genreId", required = true) Integer genreId, Model model)  {
        Page<Book> pages = bookService.getPageOfBooksByGenreId(genreId, 0, 20);
        model.addAttribute("genre", genreService.getGenreById(genreId));
        model.addAttribute("booksByGenreId", pages.getContent());
        return "/genres/slug";
    }


    @GetMapping("/authors")
    public String authorsPage(){
        return "/authors/index";
    }

    @GetMapping("/books/author/{authorId}")
    public String booksByAuthorPage(@PathVariable(value = "authorId", required = true) Integer authorId, Model model){
        Page<Book> pages = bookService.getPageOfBooksByAuthorId(authorId, 0, 20);
        model.addAttribute("booksCount", pages.getTotalElements());
        model.addAttribute("author", authorService.getAuthorById(authorId));
        model.addAttribute("p1", authorService.getParagraphDescriptionByNum(authorId, 1));
        model.addAttribute("p2", authorService.getParagraphDescriptionByNum(authorId, 2));
        model.addAttribute("paragraphs", authorService.getListParagraphDescription(authorId, 3));
        model.addAttribute("booksByAuthorId", pages.getContent());
        return "/authors/slug";
    }
}
