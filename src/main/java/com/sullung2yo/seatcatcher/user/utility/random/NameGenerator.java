package com.sullung2yo.seatcatcher.user.utility.random;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@NoArgsConstructor
public class NameGenerator {

    private static final List<String> nicknames = List.of(
            "빠른 열차", "어두운 터널", "조용한 승강장", "복잡한 환승역", "늦어진 도착",
            "좁은 출입문", "흔들린 손잡이", "쾌적한 좌석", "정확한 시각", "붐비는 출구",
            "차가운 바닥", "고요한 플랫폼", "비좁은 통로", "복잡한 지도", "느긋한 이동",
            "젊은 기관사", "지친 승객들", "빠른 전동차", "불편한 위치", "낡은 객차",
            "혼잡한 시간", "떨리는 출근길", "기다린 열차", "길어진 정차", "익숙한 풍경",
            "초록색 노선", "답답한 통행", "쾌속열차 진입", "부드러운 출발", "무거운 문소리",
            "뜨거운 승강장", "멈춘 운행", "복잡한 환승통로", "시끄러운 안내방송", "짧은 도착예정",
            "늦은 환승", "분주한 아침", "졸린 야근길", "환한 조명", "불 꺼진 차량",
            "떨리는 문닫힘", "낯선 역이름", "고장난 에스컬레이터", "조용한 칸막이", "지연된 열차",
            "서두른 이동", "어색한 시선", "좁은 통로", "느려진 속도", "끊어진 무선망"
    );

    public String generateRandomName() {
        /*
         * ThreadLocalRandom : 쓰레드마다 독립적으로 Random 인스턴스를 갖고 있음 -> 락 없이 빠르게 랜덤값 생성 가능
         */
        int index = ThreadLocalRandom.current().nextInt(nicknames.size());
        log.debug("랜덤으로 생성된 이름: {}", nicknames.get(index));
        return nicknames.get(index);
    }
}
