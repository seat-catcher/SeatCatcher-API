package com.sullung2yo.seatcatcher.train.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TrainArrivalState {
    STATE_ENTERING(0), // 진입
    STATE_ARRIVED(1), // 도착
    STATE_DEPARTED(2), // 출발
    STATE_NOT_FOUND(404), // 출발 이후 사라짐
    STATE_DRIVING(99), // 운행중
    STATE_PRVSTTN_ENTERING(4), // 전역 진입
    STATE_PRVSTTN_ARRIVED(5), // 전역 도착
    STATE_PRVSTTN_DEPARTED(3); // 전역 출발
    private final int stateCode;

    public static TrainArrivalState getState(int stateCode) {
        for (TrainArrivalState state : TrainArrivalState.values()) {
            if (state.getStateCode() == stateCode) {
                return state;
            }
        }
        return null;
    }

    /*
        정책 :
            if state == 전역 도착 or 전역 출발
                "어떤 역에서 다른 역까지 얼마나 걸리는지에 대한 Service 호출"
                 그 후 해당 시간을 확인하고, 만약 현재 PathHistory 의 expectedArrivalTime 과
                 너무 차이가 난다면 (1분 이상) 이를 조정해줌. 아님 말고.
            else if state == 진입
                 expectedArrivalTime 을 대충 1분 뒤로 잡아놓을 것.
            else if state == 도착 or 출발 or 출발이후사라짐(200)
                 도착했으니까 자동하차처리를 하면 됨.

            else if state == 전역 진입
                애매함... 만약 "expected arrival time" 을 통해 계산한 "남은 시간(초)" 값이
                "어떤 역에서 다른 역까지 얼마나 걸리는지에 대한 Service" 로 얻은 남은 시간(초)랑 비교했을 때
                    if 두 번째로 얻은 값보다 첫 번째로 얻은 값이 작다?
                        말도 안 된다! 수정해야 함.
                    if 1분 이상 차이가 난다?
                        이러면 또 수정해야 함. 너무 늦음.
                    else
                        그냥 둔다.
             else if state == 운행중
                반드시 Msg2 부분에서 숫자를 추출할 수 있을 거고, 그게 분 단위로 얼마 남았는지를 나타냄.
                그걸 "expected arrival time" 을 통해 계산한 "남은 시간(초)" 값과 비교해서
                오차 범위가 너무 크다면 수정. 그게 아니라면 냅둠.


            필요한 인터페이스 :
                start , dest 가 주어졌을 때 상행인지 하행인지 판단해주는 인터페이스
                역 , 상행하행여부가 주어졌을 때 전역을 알려주는 인터페이스
    */
}
