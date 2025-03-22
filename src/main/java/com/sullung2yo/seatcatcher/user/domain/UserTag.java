package com.sullung2yo.seatcatcher.user.domain;

import com.sullung2yo.seatcatcher.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="user_tag")
public class UserTag extends BaseEntity {

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'USERTAG_NULL'")
    @Builder.Default
    private UserTagType type = UserTagType.USERTAG_NULL;

}
