package com.sullung2yo.seatcatcher.jwt;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@DataJpaTest // JPA 테스트 시 더 가볍고 빠르게 테스트 가능하다 -> @Entity 클래스만 스캔하므로 @Service, @Component, @Repository 등은 스캔 X
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaAuditing
public class RefreshTokenRepositoryTest {
    // TODO: 추후 구현 예정
}
