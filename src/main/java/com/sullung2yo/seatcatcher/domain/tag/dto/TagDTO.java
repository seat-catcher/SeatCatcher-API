package com.sullung2yo.seatcatcher.domain.tag.dto;

import com.sullung2yo.seatcatcher.domain.tag.enums.UserTagType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TagDTO {

    List<UserTagType> tags;

}
