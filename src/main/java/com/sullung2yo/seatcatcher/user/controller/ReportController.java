package com.sullung2yo.seatcatcher.user.controller;

import com.sullung2yo.seatcatcher.config.exception.dto.ErrorResponse;
import com.sullung2yo.seatcatcher.user.domain.Report;
import com.sullung2yo.seatcatcher.user.dto.request.ReportRequest;
import com.sullung2yo.seatcatcher.user.dto.response.ReportResponse;
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
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<ReportResponse>> getReports() {
        List<ReportResponse> response = reportService.getAllReports();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{report_id}")
    @Operation(
            summary = "신고 삭제하기 API",
            description = "접수된 신고를 삭제합니다. reportId로 요청할 수 있습니다.)"

    )
    @ApiResponse(responseCode = "200", description = "신고 삭제 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "신고를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> deleteReport(@PathVariable("report_id") Long reportId) {
        reportService.deleteReport(reportId);
        return ResponseEntity.ok("Report가 삭제되었습니다.");
    }

    @PatchMapping("/{report_id}")
    @Operation(
            summary = "신고 수정하기 API",
            description = "접수된 신고 내용을 수정합니다. reportId로 요청할 수 있습니다.)"
    )
    @ApiResponse(responseCode = "200", description = "신고 수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "신고를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ReportResponse> patchReport(@PathVariable("report_id") Long reportId, @Valid @RequestBody ReportRequest request) {
        ReportResponse response = reportService.updateReport(reportId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{report_id}")
    @Operation(
            summary = "특정 report 가져오기 API",
            description = "특정 report를 가져옵니다. reportId로 요청할 수 있습니다.)"

    )
    @ApiResponse(responseCode = "200", description = "신고 가져오기 성공")
    @ApiResponse(responseCode = "404", description = "신고를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ReportResponse> getReport(@PathVariable("report_id") Long reportId) {
        ReportResponse response = reportService.getReportById(reportId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(
            summary = "사용자 report 가져오기 API",
            description = "사용자가 작성한 report를 가져옵니다.)"

    )
    @ApiResponse(responseCode = "200", description = "나의 신고 가져오기 성공")
    public ResponseEntity<List<ReportResponse>> getMyReports() {
        List<ReportResponse> responses = reportService.getMyReport();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/")
    @Operation(
            summary = "report 생성하기 API",
            description = "report를 새롭게 생성합니다.)"

    )
    @ApiResponse(responseCode = "200", description = "신고 생성하기 성공")
    public ResponseEntity<?> addReport(@Valid @RequestBody ReportRequest request) {
        reportService.createReport(request);
        return ResponseEntity.ok("Report가 생성되었습니다.");
    }

}
