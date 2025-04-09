package com.sullung2yo.seatcatcher.user.controller;

import com.sullung2yo.seatcatcher.config.exception.dto.ErrorResponse;
import com.sullung2yo.seatcatcher.user.domain.Report;
import com.sullung2yo.seatcatcher.user.dto.response.TokenResponse;
import com.sullung2yo.seatcatcher.user.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "신고 API", description = "User 신고 API 입니다.")
public class ReportController {

    private final ReportService reportService;
    @GetMapping("/")
    @Operation(
            summary = "신고 불러오기 API",
            description = "신고된 내용을 불러옵니다. Admin만 해당 작업을 수행할 수 있습니다. (현재 관리자 페이지는 디자인된게 없어 Report 도메인 그대로 반환합니다.)"

    )
    public ResponseEntity<?> getReports() {
        List<Report> response = reportService.getAllReports();
        return ResponseEntity.ok(response);
    }


}
