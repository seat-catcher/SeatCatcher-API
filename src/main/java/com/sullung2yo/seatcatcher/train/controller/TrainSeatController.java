package com.sullung2yo.seatcatcher.train.controller;

import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import com.sullung2yo.seatcatcher.train.dto.request.TrainSeatRequest;
import com.sullung2yo.seatcatcher.train.dto.response.CascadeTrainSeatResponse;
import com.sullung2yo.seatcatcher.train.dto.response.TrainSeatResponse;
import com.sullung2yo.seatcatcher.train.service.TrainSeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/trains/{trainId}/cars/{carId}/seat-groups/{seatGroupId}/seats")
@Tag(name = "좌석 API", description = "좌석 관련 API")
public class TrainSeatController {

    private final TrainSeatService trainSeatService;

    @GetMapping
    @Operation(
        summary = "어떤 그룹에 속한 좌석들의 끌어올 수 있는 모든 정보를 끌어오는 API",
        description = "열차 id와 량 id, 그룹 id를 이용하여 좌석 정보, 착석 유저 정보, 해당 유저의 경로 정보를 모두 가져옵니다.",
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "성공적으로 TrainSeat들 Cascade하게 반환 완료",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CascadeTrainSeatResponse.class))
                ),
                @ApiResponse(
                        responseCode = "204",
                        description = "그룹에 속한 좌석이 없음."
                )
        }
    )
    public ResponseEntity<List<CascadeTrainSeatResponse>> getSeatsCascade(
            @PathVariable Long trainId,
            @PathVariable Long carId,
            @PathVariable Long seatGroupId
    )
    {
        List<CascadeTrainSeatResponse> responses = new ArrayList<>();

        try
        {
            List<TrainSeat> records = trainSeatService.findAllBySeatGroupId(seatGroupId);
            for(TrainSeat record : records) {
                responses.add(new CascadeTrainSeatResponse(record));
            }

            for(CascadeTrainSeatResponse response : responses) {
                //UserTrainSeatService 에 서비스를 요청해서 좌석에 앉아 있는 유저가 있는지 확인하고
                //만약 존재한다면
                // UserService 에 서비스를 요청해서 해당 유저를 id 를 통해 검색해서 필요한 내용 채워 넣을 것.
                // PathHistory 에 서비스를 요청해서 해당 유저 id 를 통해 modified_at 이 제일 최근인걸 가져와서 필요한 내용 채워 넣을 것.
            }

            return ResponseEntity.ok(responses);
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.noContent().build();
        }
    }

    @PatchMapping("/{seatId}")
    @Operation(
            summary = "특정 좌석의 정보를 수정하는 API",
            description = "열차 id와 량 id, 그룹 id와 좌석 id를 이용하여 해당 좌석의 정보를 변경합니다.\n" +
                    "변경할 수 있는 정보는 좌석의 위치 정보, 좌석의 타입, 찜 수입니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 좌석 정보입니다. 일부 필드만 담아주셔도 됩니다.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TrainSeatRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 변경 완료"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "해당 좌석을 찾을 수 없음"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 데이터"
                    )
            }
    )
    public ResponseEntity<Void> updateSeat(
            @PathVariable Long trainId,
            @PathVariable Long carId,
            @PathVariable Long seatGroupId,
            @PathVariable Long seatId,
            @RequestBody TrainSeatRequest trainSeatRequest
    )
    {
        try
        {
            trainSeatService.update(seatId, trainSeatRequest);
            return ResponseEntity.ok().build();
        }
        catch(EntityNotFoundException e)
        {
            return ResponseEntity.notFound().build();
        }
    }
}