package com.bookshop.BookShopApp.controllers;

import com.bookshop.BookShopApp.services.GenreService;
import com.bookshop.BookShopApp.structure.genre.Genre;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class GenresRestApiController {

    private final GenreService genreService;

    @Autowired
    public GenresRestApiController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping("/api/genres")
    @ApiOperation("operation to get list genres by parentId, parameter parentId - Integer")
    @ResponseBody
    public Map<Integer, List<Genre>> genresByParentId(@RequestParam("parentId") Integer parentId){
        return genreService.getMapGenreEntitiesByParentId(parentId);
    }
}
