package com.sullung2yo.seatcatcher.train.controller;

import com.sullung2yo.seatcatcher.train.domain.Train;
import com.sullung2yo.seatcatcher.train.dto.response.TrainSeatGroupResponse;
import com.sullung2yo.seatcatcher.train.service.TrainSeatGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/trains/{trainCode}/cars/{carCode}/seat-groups")
@Tag(name = "좌석 그룹 API", description = "좌석 그룹 관련 API")
public class TrainSeatGroupController {

    private final TrainSeatGroupService trainSeatGroupService;

    @GetMapping
    @Operation(
            summary = "량에 속한 모든 TrainSeatGroup들을 가져오는 API",
            description = "열차 번호와 차량 번호를 이용하여 해당 차량에 속한 모든 TrainSeatGroup 들을 가져옵니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 TrainSeatGroup들 반환",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainSeatGroupResponse.ResponseList.class))
                    ),
                    @ApiResponse(
                            responseCode = "201",
                            description = "성공적으로 TrainSeatGroup들을 생성, 반환 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainSeatGroupResponse.ResponseList.class))
                    )
            }
    )
    public ResponseEntity<TrainSeatGroupResponse.ResponseList> getGroups(
            @PathVariable String trainCode,
            @PathVariable String carCode
    )
    {
        TrainSeatGroupResponse.ResponseList responses = new TrainSeatGroupResponse.ResponseList();

        try
        {
            List<Train> records = trainSeatGroupService.findByTrainCodeAndCarCode(trainCode, carCode);

            for(Train record : records) {
                responses.getItems().add(new TrainSeatGroupResponse.SingleResponse(record));
            }

            log.debug("성공적으로 TrainSeatGroup들 반환");
            return ResponseEntity.ok(responses);
        }
        catch(EntityNotFoundException e)
        {
            // 엔티티가 없는 경우. 에러를 뿌리는게 아니라 새로 생성해줘야 함.
            List<Train> records = trainSeatGroupService.createGroupsOf(trainCode, carCode);
            for(Train record : records) {
                responses.getItems().add(new TrainSeatGroupResponse.SingleResponse(record));
            }

            log.debug("성공적으로 TrainSeatGroup들을 생성, 반환 완료");
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        }
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
