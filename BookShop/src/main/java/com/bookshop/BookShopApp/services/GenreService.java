package com.bookshop.BookShopApp.services;

import com.bookshop.BookShopApp.data.GenreRepository;
import com.bookshop.BookShopApp.structure.genre.Genre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



@Service
@Transactional
public class GenreService {

    public class Pair {

        private Integer genreId;
        private List<Genre> genreList;

        public Pair(Integer genreId, List<Genre> genreList) {
            this.genreId = genreId;
            this.genreList = genreList;
        }
    }

    private GenreRepository genreRepository;

    @Autowired
    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public Map<Integer, List<Genre>> getGenresMap() {
        List<Genre> genres = genreRepository.findAll();
        return genres.stream().collect(Collectors.groupingBy((Genre g) -> {
            return g.getParentId();
        }));
    }

    public Integer getGenreBooksCount(Integer genreId) {
        return genreRepository.getGenreBooksCount(genreId);
    }

    public void setGenreBooksCount(Integer countBook, Integer genreId) {
        genreRepository.setGenreBooksCount(countBook, genreId);
    }

    public void setAllGenresBooksCount() {
        if (getSumAllGenreBooksCount() == 0) {
            List<Integer[]> genresBookCountArray = genreRepository.getDataAllGenreBooksCount();
            for (Integer[] item : genresBookCountArray) {
                setGenreBooksCount(item[1], item[0]);
            }
        }
    }

    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    public Genre getGenreById(Integer genreId) {
        return genreRepository.findGenreEntityById(genreId);
    }

    public List<Genre> getListGenreEntitiesByParentId(Integer parentId) {
        return genreRepository.findGenreEntityByParentIdOrderByName(parentId);
    }

    public Map<Integer, List<Genre>> getMapGenreEntitiesByParentId(Integer parentId) {
        List<Genre> genres = genreRepository.findGenreEntityByParentIdOrderByName(parentId);
        return genres.stream().collect(Collectors.groupingBy(Genre::getId));
    }

    public String getAllGenreString() {
        StringBuilder builder = new StringBuilder();
        List<Genre> genres = getListGenreEntitiesByParentId(0);
        builder.append("<div class=\"Tags Tags_genres\">\n");
        for (Genre item: genres) {
            builder.append(getGenreString(item, 0));
        }
        builder.append("</div>\n");
        return builder.toString();
    }

    public String getGenreString(Genre genre, int root) {
        StringBuilder builder = new StringBuilder();
        List<Genre> genres = getListGenreEntitiesByParentId(genre.getId());
        boolean flag = false;
        if ((genres.size() >  0) || (root == 0)) {
            builder.append("<div class=\"Tags\">\n");
            builder.append("<div class=\"Tags-title\">\n");
            builder.append("<div class=\"Tag\"><a href=\"/genre/" + genre.getId()+"\">" + genre.getName() + "<span class=\"undefined-amount\">" + " ("+ genre.getCount_book() + ")" + "</span></a></div>\n");
            builder.append("</div>");
            flag = true;
        } else {
            builder.append("<div class=\"Tag\"><a href=\"/genre/" + genre.getId()+"\">" + genre.getName() + "<span class=\"undefined-amount\">" + " ("+ genre.getCount_book() + ")" + "</span></a></div>\n");

        }

        for (Genre item: genres) {
            builder.append(getGenreString(item, 1));
        }
        if (flag) {builder.append("</div>\n");}

        return builder.toString();
    }

    public Integer getSumAllGenreBooksCount() {
        Integer sum = genreRepository.getSumAllGenreBooksCount();
        if (sum != null) {
            return sum;
        } else {return 0;}
    }


}
