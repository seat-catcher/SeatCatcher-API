package com.sullung2yo.seatcatcher.domain.train.service;

import com.sullung2yo.seatcatcher.domain.train.entity.TrainSeat;
import com.sullung2yo.seatcatcher.domain.train.entity.TrainSeatGroup;
import com.sullung2yo.seatcatcher.domain.train.entity.UserTrainSeat;
import com.sullung2yo.seatcatcher.domain.train.enums.SeatGroupType;
import com.sullung2yo.seatcatcher.domain.train.enums.SeatType;
import com.sullung2yo.seatcatcher.domain.train.dto.TrainCarDTO;
import com.sullung2yo.seatcatcher.domain.train.dto.response.SeatInfoResponse;
import com.sullung2yo.seatcatcher.domain.train.repository.TrainSeatGroupRepository;
import com.sullung2yo.seatcatcher.domain.train.utility.SeatStatusAssembler;
import com.sullung2yo.seatcatcher.domain.user.domain.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class TrainSeatGroupServiceImpl implements TrainSeatGroupService {

    private final TrainSeatGroupRepository trainSeatGroupRepository;
    private final SeatStatusAssembler seatStatusAssembler;
    private final UserTrainSeatService userTrainSeatService;

    /*
        trainCode를 통해서 해당 열차에 생성되어있는 모든 좌석 그룹 리스트를 반환하는 메서드
     */
    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public List<TrainSeatGroup> findAllByTrainCodeAndCarCode(String trainCode, String carCode) {
        return trainSeatGroupRepository.findAllByTrainCodeAndCarCode(trainCode, carCode);
    }

    @Override
    @Transactional
    public List<TrainSeatGroup> createGroupsOf(@NonNull String trainCode, @NonNull String carCode) {
        List<TrainSeatGroup> groups = new ArrayList<>();
        // 차량 코드에서 편성번호 추출하고, 편성번호에 따라 좌석 배치 타입을 구분해서 TrainSeatGroup 엔티티 리스트 생성
        List<SeatGroupType> types = this.seatGroupChecker(carCode)
                ? List.of(SeatGroupType.PRIORITY_A, SeatGroupType.NORMAL_A_14,
                SeatGroupType.NORMAL_B_14, SeatGroupType.NORMAL_C_14,
                SeatGroupType.PRIORITY_B)      // 37773 (true)
                : List.of(SeatGroupType.PRIORITY_A, SeatGroupType.NORMAL_A_12,
                SeatGroupType.NORMAL_B_12, SeatGroupType.NORMAL_C_12,
                SeatGroupType.PRIORITY_B);     // 36663 (false)

        types.forEach(t -> groups.add(createTrainSeatGroup(trainCode, carCode, t)));
        trainSeatGroupRepository.saveAll(groups);

        return groups;
    }


    /**
     * [
     *   {
     *     "trainCode": "1234",
     *     "carCode": "2001",
     *     "seatGroupType": "...",
     *     "seatStatus": [
     *       {
     *         "seatId": 1,
     *         "seatLocation": 0,
     *         "seatType": "...",
     *         "occupant": {
     *           "userId": 123123,
     *           "nickname": "asdfasdf",
     *           "getOffRemainingCount": 3
     *         }
     *       },
     *       {...}
     *     ]
     *   },
     *   {...}
     * ]
     * 좌석 그룹 정보를 통해서 좌석 상태 리스트를 생성하는 메서드
     * @param trainCode: 열차 코드
     * @param carCode: 차량 코드
     * @param trainSeatGroups: 좌석 그룹 리스트
     * @return 좌석 상태 리스트
     */
    @Override
    public List<SeatInfoResponse> createSeatInfoResponse(String trainCode, String carCode, List<TrainSeatGroup> trainSeatGroups) {
        return trainSeatGroups.stream()
                .map(group -> SeatInfoResponse.builder()
                        .trainCode(trainCode)
                        .carCode(carCode)
                        .seatGroupType(group.getSeatGroupType())
                        .seatStatus(seatStatusAssembler.assembleSeatResponse(group))
                        .build())
                .toList();
    }

    /**
     * TrainSeatGroup 객체를 생성하는 메서드
     * @param trainCode: 열차 코드
     * @param carCode: 차량 코드
     * @param groupType: 좌석 그룹 타입
     * @return TrainSeatGroup 객체
     */
    @Transactional
    public TrainSeatGroup createTrainSeatGroup(String trainCode, String carCode, SeatGroupType groupType){
        // trainCode, carCode, groupType 를 통해서 TrainSeatGroup 객체 생성
        TrainSeatGroup trainSeatGroup = TrainSeatGroup.builder()
                .trainCode(trainCode)
                .carCode(carCode)
                .trainSeat(new ArrayList<>())
                .seatGroupType(groupType)
                .build();
        List<TrainSeat> trainSeatList = new ArrayList<>();

        // SeatGroupType에 따라 좌석 개수 설정해서 TrainSeat 객체 생성
        for(int seatNumber = 0; seatNumber < groupType.getSeatCount(); seatNumber++) {
            SeatType seatType = null;

            // 노약자석의 경우에는 seatType 을 ELDERLY 로 설정
            if(trainSeatGroup.getSeatGroupType() == SeatGroupType.PRIORITY_A || trainSeatGroup.getSeatGroupType() == SeatGroupType.PRIORITY_B) {
                seatType = SeatType.PRIORITY;
            } else {
                seatType = SeatType.NORMAL; // 임산부 좌석은 고려하지 않고 일단 Normal 로 모두 설정하겠음. TODO :: 추후에 임산부 좌석이 고려되어야 할 경우 이 부분을 변경할 것.
            }

            // 좌석 엔티티 생성 후 리스트 저장
            TrainSeat trainSeat = TrainSeat.builder()
                    .trainSeatGroup(trainSeatGroup)
                    .seatLocation(seatNumber)
                    .seatType(seatType)
                    .build();
            trainSeatList.add(trainSeat);
        }
        trainSeatGroup.setTrainSeat(trainSeatList);
        return trainSeatGroupRepository.save(trainSeatGroup);
    }

    @Override
    public TrainCarDTO getSittingTrainCarInfo(User user) {
        try
        {
            // TODO :: 지금 상황에서는 잘 작동할거임. fetch 전략이 default 인 EAGER 이기 때문. 그러나 정책이 바뀌면 이 코드도 바뀌어야 함.

            UserTrainSeat sittingInfo = userTrainSeatService.findUserTrainSeatByUserId(user.getId());

            TrainSeat seat = sittingInfo.getTrainSeat();

            TrainSeatGroup group = seat.getTrainSeatGroup();

            return new TrainCarDTO(group.getTrainCode(), group.getCarCode());
        }
        catch(EntityNotFoundException e)
        {
            return null; // 유저가 앉아있지 않다면
        }
        catch(NullPointerException e)
        {
            String errorMessage = "Domain 에 대한 Fetch 전략 이슈로 인해 빈 객체를 참조하고 있습니다.";
            log.error(errorMessage, e);
            throw new NullPointerException(errorMessage);
        }
    }

    /**
     * 차량 코드를 통해 편성 번호를 구해서,
     * 편성번호에 따른 열차 차분을 파악하여 좌석 배치 타입을 알아내는 메서드
     * 2호선의 경우 1차분은 37773, 2,3,4,5차분은 편의상 36663으로 설정
     * 7호선의 경우 1,2,3,4차분은 37773, 5차분은 36663으로 설정
     * @param carCode: 차량 코드
     * @return true -> 37773 형태, false -> 36663 형태
     */
    private boolean seatGroupChecker(String carCode) {
        int identifier = Integer.parseInt(carCode.charAt(0) + carCode.substring(2)); // 열차 번호가 2429이면 4를 제외한 229 -> 편성번호
        // 36663
        return (201 <= identifier && identifier <= 205) || (718 <= identifier && identifier <= 772);
    }
}