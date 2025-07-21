package com.sullung2yo.seatcatcher.domain.tag.entity;

import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.common.domain.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="user_tag")
public class UserTag extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 사용자

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag; // 태그

    public void setRelationships(User user, Tag tag) {
        this.user = user;
        this.tag = tag;

        user.getUserTag().add(this);
        tag.getUserTag().add(this);
    }
}
