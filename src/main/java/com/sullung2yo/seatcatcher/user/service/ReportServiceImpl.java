package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.config.exception.ErrorCode;
import com.sullung2yo.seatcatcher.config.exception.TokenException;
import com.sullung2yo.seatcatcher.user.domain.Report;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.dto.request.ReportRequest;
import com.sullung2yo.seatcatcher.user.repository.ReportRepository;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService{

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    @Override
    public List<Report> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        return reports;
    }

    @Override
    public void deleteReport(Long reportId) {

    }

    @Override
    public void createReport(ReportRequest request) {

        User reportUser = userRepository.findById(request.getReportUserId())
                .orElseThrow(() -> new IllegalArgumentException("id : "+ request.getReportUserId() + "사용자를 찾을 수 없습니다."));

        User reportedUser = userRepository.findById(request.getReportedUserId())
                .orElseThrow(() -> new IllegalArgumentException("id : "+ request.getReportUserId() + "사용자를 찾을 수 없습니다."));

        Report report = Report.builder()
                .reportUser(reportUser)
                .reportedUser(reportedUser)
                .reason(request.getReason())
                .build();

        reportRepository.save(report);
    }
}
