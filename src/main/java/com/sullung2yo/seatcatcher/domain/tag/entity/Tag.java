package com.sullung2yo.seatcatcher.domain.tag.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sullung2yo.seatcatcher.domain.tag.enums.UserTagType;
import com.sullung2yo.seatcatcher.common.domain.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tag")
public class Tag extends BaseEntity {

    @JsonIgnore
    @OneToMany(mappedBy = "tag")
    @Builder.Default
    private Set<UserTag> userTag = new HashSet<>();

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'USERTAG_NULL'")
    @Builder.Default
    private UserTagType tagName = UserTagType.USERTAG_NULL;

}
