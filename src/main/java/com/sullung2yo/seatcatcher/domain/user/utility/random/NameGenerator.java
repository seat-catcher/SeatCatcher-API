package com.sullung2yo.seatcatcher.domain.user.utility.random;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@NoArgsConstructor
public class NameGenerator {

    private static final List<String> adjectives = List.of(
            "빠른", "어두운", "조용한", "복잡한", "늦어진",
            "좁은", "흔들린", "쾌적한", "정확한", "붐비는",
            "차가운", "고요한", "비좁은", "느긋한", "젊은",
            "지친", "빠른", "불편한", "낡은", "혼잡한",
            "떨리는", "기다린", "길어진", "익숙한", "초록색",
            "답답한", "쾌속열차", "부드러운", "무거운", "뜨거운",
            "멈춘", "복잡한", "시끄러운", "짧은", "늦은",
            "분주한", "졸린", "환한", "불 꺼진", "떨리는",
            "낯선", "고장난", "조용한", "지연된", "서두른",
            "어색한", "좁은", "느려진", "끊어진"
    );

    private static final List<String> nouns = List.of(
            "열차", "터널", "승강장", "환승역", "도착",
            "출입문", "손잡이", "좌석", "시각", "출구",
            "바닥", "플랫폼", "통로", "지도", "이동",
            "기관사", "승객들", "전동차", "위치", "객차",
            "시간", "출근길", "열차", "정차", "풍경",
            "노선", "통행", "진입", "출발", "문소리",
            "승강장", "운행", "환승통로", "안내방송", "도착예정",
            "환승", "아침", "야근길", "조명", "차량",
            "문닫힘", "역이름", "에스컬레이터", "칸막이", "열차",
            "이동", "시선", "통로", "속도", "무선망"
    );

    public String generateRandomName() {
        /*
         * ThreadLocalRandom : 쓰레드마다 독립적으로 Random 인스턴스를 갖고 있음 -> 락 없이 빠르게 랜덤값 생성 가능
         */
        int adjectives_index = ThreadLocalRandom.current().nextInt(adjectives.size());
        int nouns_index = ThreadLocalRandom.current().nextInt(nouns.size());

        String nickname = adjectives.get(adjectives_index) + " " + nouns.get(nouns_index);
        log.debug("랜덤으로 생성된 이름: {}", nickname);
        return nickname;
    }
}
