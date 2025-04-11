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
            "빠른열차", "어두운터널", "푸른전동차", "조용한승강장", "느린지하철",
            "좁은출구", "밝은스크린도어", "긴플랫폼", "복잡한노선도", "덥고복잡한환승역",
            "조용한플랫폼", "붐비는출입구", "낡은전동차", "쾌적한칸막이", "혼잡한종착역",
            "늦은열차", "젊은기관사", "좁은좌석", "빠른환승", "차가운손잡이",
            "흔들린출입문", "작은스크린도어", "쾌속열차", "늦은승차", "답답한터널",
            "편한좌석", "익숙한전철", "초록색노선", "분주한기점", "정확한도착",
            "기다린플랫폼", "졸린출근길", "혼잡한출퇴근", "떨리는기차길", "미끄러운바닥",
            "고요한출구", "바쁜환승통로", "쾌적한지하철", "느긋한열차", "어색한마주침",
            "복잡한지도", "세련된열차", "비좁은좌석", "불편한출구", "깊은지하역",
            "멈춘전동차", "혼잡한타는곳", "단정한차장", "시끄러운열차", "환한지하역"
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
