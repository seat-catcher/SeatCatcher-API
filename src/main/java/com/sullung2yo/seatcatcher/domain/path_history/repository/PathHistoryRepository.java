package com.sullung2yo.seatcatcher.domain.path_history.repository;

import com.sullung2yo.seatcatcher.domain.path_history.entity.PathHistory;
import com.sullung2yo.seatcatcher.domain.subway_station.entity.SubwayStation;
import com.sullung2yo.seatcatcher.domain.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PathHistoryRepository extends JpaRepository<PathHistory, Long> {
    @Query("""
    SELECT ph
    FROM PathHistory ph
    WHERE ph.user = :user
      AND (:lastPathId IS NULL OR ph.id < :lastPathId)
    ORDER BY ph.id DESC
""")
    List<PathHistory> findScrollByUserAndCursor(
            @Param("user") User user,
            @Param("lastPathId") Long lastPathId,
            Pageable pageable
    );

    // TODO :: 이게 이렇게 막 써놓고 파란색 글씨 뜨니까 되는 것 같기는 한데 일단 테스트 코드 나중에 만들어서 검증해야 함.
    Optional<PathHistory> findTopByUserIdOrderByUpdatedAtDesc(long userId);
    
    @Query("SELECT ph.endStation FROM PathHistory ph WHERE ph.user = :user")
    SubwayStation findEndStationByUser(User user);
}
