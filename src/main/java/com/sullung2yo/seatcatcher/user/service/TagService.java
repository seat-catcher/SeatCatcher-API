package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.user.domain.Tag;

import java.util.List;
import java.util.Optional;

public interface TagService {

    List<Tag> getTags();
    Tag getTagById(Long id);
}
