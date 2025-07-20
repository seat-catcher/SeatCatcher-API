package com.sullung2yo.seatcatcher.domain.report.repository;

import com.sullung2yo.seatcatcher.domain.report.entity.Report;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAllByReportUser(User user);
}
