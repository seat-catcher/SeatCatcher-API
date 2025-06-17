package com.sullung2yo.seatcatcher.user.dto;

import com.sullung2yo.seatcatcher.user.domain.UserTagType;
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
