package com.sullung2yo.seatcatcher.common.exception.dto;

import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ErrorResponse {
    private String error;
    private String message;
}
