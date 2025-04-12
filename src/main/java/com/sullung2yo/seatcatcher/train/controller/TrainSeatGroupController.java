package com.sullung2yo.seatcatcher.train.controller;

import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;
import com.sullung2yo.seatcatcher.train.dto.response.TrainSeatGroupResponse;
import com.sullung2yo.seatcatcher.train.service.TrainSeatGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/trains/{trainId}/cars/{carId}/seat-groups")
@Tag(name = "좌석 그룹 API", description = "좌석 그룹 관련 API")
public class TrainSeatGroupController {

    private final TrainSeatGroupService trainSeatGroupService;

    @GetMapping
    @Operation(
            summary = "량에 속한 모든 TrainSeatGroup들을 가져오는 API",
            description = "열차 id와 량 id를 이용하여 해당 량에 속한 모든 TrainSeatGroup 들을 가져옵니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 TrainSeatGroup들 반환",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainSeatGroupResponse.class))
                    )
            }
    )
    public ResponseEntity<List<TrainSeatGroupResponse>> getGroups(
            @PathVariable Long trainId,
            @PathVariable Long carId
    )
    {
        List<TrainSeatGroupResponse> responses = new ArrayList<>();

        List<TrainSeatGroup> records = trainSeatGroupService.findAllByTrainCarId(carId);
        for(TrainSeatGroup record : records) {
            responses.add(new TrainSeatGroupResponse(record));
        }

        return ResponseEntity.ok(responses);
    }


/*
    // TODO :: 나중에 개발자 전용 페이지를 만들게 될 때 구현해야 합니다!

    @GetMapping("/{seatGroupId}")
    public ResponseEntity<TrainSeatGroupResponse> getCertainGroup(
            @PathVariable Long trainId,
            @PathVariable Long carId,
            @PathVariable Long seatGroupId
    )
    {
        return ResponseEntity.ok(new TrainSeatGroupResponse(trainSeatGroupService.findBySeatGroupId(seatGroupId)));
    }

    @PostMapping
    public ResponseEntity<Void> createGroup(
            @PathVariable Long trainId,
            @PathVariable Long carId
            //@RequestBody
    )
    {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{seatGroupId}")
    public ResponseEntity<Void> updateGroup(
            @PathVariable Long trainId,
            @PathVariable Long carId,
            @PathVariable Long seatGroupId
            //@RequestBody
    )
    {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{seatGroupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable Long trainId,
            @PathVariable Long carId,
            @PathVariable Long seatGroupId
    )
    {
        return ResponseEntity.noContent().build();
    }
*/
}
