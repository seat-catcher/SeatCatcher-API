package com.sullung2yo.seatcatcher.subway_station.controller;

import com.sullung2yo.seatcatcher.common.exception.dto.ErrorResponse;
import com.sullung2yo.seatcatcher.subway_station.dto.request.PathHistoryRequest;
import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.subway_station.service.PathHistoryService;
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

@Slf4j
@RestController
@RequestMapping("/path-histories")
@RequiredArgsConstructor
@Tag(name = "과거 경로 API", description = "과거 경로 API")
public class PathHistoryController {

    private final PathHistoryService pathHistoryService;

    @PostMapping("/")
    @Operation(
            summary = "path history 생성 API",
            description = "특정 path history를 생성합니다.)"

    )
    @ApiResponse(responseCode = "200", description = "path history 생성 성공")
    public ResponseEntity<?> postPathHistory(@Valid @RequestBody PathHistoryRequest request) {
        pathHistoryService.addPathHistory(request);
        return ResponseEntity.ok("pathHistory가 생성되었습니다.");
    }
    @GetMapping("/")
    @Operation(
            summary = "All path history 가져오기 API",
            description = "무한 스크롤 기반으로 모든 path history를 가져옵니다.)"

    )
    @ApiResponse(responseCode = "200", description = "All path history 가져오기 성공")
    public ResponseEntity<PathHistoryResponse.PathHistoryList> getAllPathHistory(@RequestParam(defaultValue = "10") int size, @RequestParam(required = false) Long cursor) {
        PathHistoryResponse.PathHistoryList response = pathHistoryService.getAllPathHistory(size, cursor);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{path_id}")
    @Operation(
            summary = "특정 path history 가져오기 API",
            description = "특정 path history를 가져옵니다.)"

    )
    @ApiResponse(responseCode = "200", description = "특정 path history 가져오기 성공")
    @ApiResponse(responseCode = "403", description = "user가 pathHistory에 접근할 권한이 없음")
    @ApiResponse(responseCode = "404", description = "user/pathHistory를 찾을 수 없음")
    public ResponseEntity<PathHistoryResponse.PathHistoryInfoResponse> getPathHistory(@PathVariable("path_id") Long pathId) {
        PathHistoryResponse.PathHistoryInfoResponse response = pathHistoryService.getPathHistory(pathId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{path_id}")
    @Operation(
            summary = "path history 삭제 API",
            description = "특정 path history를 삭제합니다. pathId로 요청할 수 있습니다.)"

    )
    @ApiResponse(responseCode = "200", description = "path history 삭제 성공")
    @ApiResponse(responseCode = "404", description = "path history를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "user가 pathHistory에 접근할 권한이 없음")
    public ResponseEntity<?> deletPathHistory(@PathVariable("path_id") Long pathtId) {

        return ResponseEntity.ok("해당 pathHistory를 삭제했습니다.");
    }

    @PatchMapping("/{path_id}")
    @Operation(
            summary = "path history 수정 API / admin 미완성",
            description = "특정 path history를 수정합니다. pathId로 요청할 수 있습니다.)"

    )
    @ApiResponse(responseCode = "200", description = "path history 수정 성공")
    @ApiResponse(responseCode = "404", description = "path history를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> patchPathHistory(@PathVariable("path_id") Long pathtId) {

//        return ResponseEntity.ok(response);
        return null;
    }
}
