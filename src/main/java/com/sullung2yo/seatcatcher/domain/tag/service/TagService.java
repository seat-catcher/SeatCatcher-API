package com.sullung2yo.seatcatcher.domain.tag.service;

import com.sullung2yo.seatcatcher.domain.tag.entity.Tag;

import java.util.List;

public interface TagService {

    List<Tag> getTags();
    Tag getTagById(Long id);
}
