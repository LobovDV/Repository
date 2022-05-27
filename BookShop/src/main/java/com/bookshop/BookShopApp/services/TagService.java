package com.bookshop.BookShopApp.services;

import com.bookshop.BookShopApp.data.TagRepository;
import com.bookshop.BookShopApp.structure.enums.TagType;
import com.bookshop.BookShopApp.structure.tag.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class TagService {

    private final TagRepository tagRepository;
    private static final int COUNT_BOOK_FOR_XS = 10;
    private static final int COUNT_BOOK_FOR_SM = 25;
    private static final int COUNT_BOOK_FOR_MD = 45;
    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Integer getTagBooksCount(Integer tagId) {
        return tagRepository.getTagBooksCount(tagId);
    }

    public void setTagBooksCount(Integer countBook, Integer tagId) {
        TagType type = TagType.Tag_sm;
        if (countBook < COUNT_BOOK_FOR_XS) {type = TagType.Tag_xs;}
        if ((countBook >= COUNT_BOOK_FOR_XS) & (countBook < COUNT_BOOK_FOR_SM)) {type = TagType.Tag_sm;}
        if ((countBook >= COUNT_BOOK_FOR_SM) & (countBook < COUNT_BOOK_FOR_MD)) {type = TagType.Tag_md;}
        if (countBook >= COUNT_BOOK_FOR_MD) {type = TagType.Tag_lg;}
        tagRepository.setTagBooksCount(countBook, type, tagId);
    }

    public void setAllTagsBooksCount() {
        if (getSumAllTagsCountBook() == 0) {
            List<Integer[]> tagsBookCountArray = tagRepository.getDataAllTagBooksCount();
            for (Integer[] item : tagsBookCountArray) {
                setTagBooksCount(item[1], item[0]);
            }
        }
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Tag getTagById(Integer tagId) {
        return tagRepository.findTagEntityById(tagId);
    }

    public  Integer getSumAllTagsCountBook() {
        Integer sum = tagRepository.getSumAllTagsCountBook();
        if (sum != null) {
            return sum;
        } else { return 0;}
    }
}
