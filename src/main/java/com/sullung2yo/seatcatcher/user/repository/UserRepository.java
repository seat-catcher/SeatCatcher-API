package com.sullung2yo.seatcatcher.user.repository;

import com.sullung2yo.seatcatcher.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 주어진 이메일 주소에 해당하는 User 엔티티를 Optional로 감싸 반환합니다.
     * 입력된 이메일과 일치하는 사용자가 존재하는 경우 해당 User 엔티티를,
     * 존재하지 않을 경우 빈 Optional을 반환합니다.
     *
     * @param email 사용자 이메일 주소
     * @return 주어진 이메일에 해당하는 User 엔티티를 Optional로 감싼 값
     */
    Optional<User> findByEmail(String email);

    /**
     * 제공자 ID를 통해 User 엔티티를 조회합니다.
     * 주어진 providerId에 해당하는 User 엔티티를 검색하며,
     * 존재하는 경우 해당 User를 Optional로 감싸 반환하고, 그렇지 않은 경우 빈 Optional을 반환합니다.
     *
     * @param providerId 제공자 ID
     * @return providerId와 일치하는 User 엔티티를 포함하는 Optional, 존재하지 않으면 빈 Optional
     */
    Optional<User> findByProviderId(String providerId);
}
