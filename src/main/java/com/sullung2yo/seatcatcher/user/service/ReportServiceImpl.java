package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.user.converter.ReportConverter;
import com.sullung2yo.seatcatcher.user.domain.Report;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.dto.request.ReportRequest;
import com.sullung2yo.seatcatcher.user.dto.response.ReportResponse;
import com.sullung2yo.seatcatcher.user.repository.ReportRepository;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService{

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ReportConverter reportConverter;

    @Override
    public List<Report> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        return reports;
    }

    @Override
    public void deleteReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(()-> new IllegalArgumentException("id : " + reportId + "report를 찾을 수 없습니다."));

        reportRepository.delete(report);
    }

    @Override
    public ReportResponse updateReport(Long reportId, ReportRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(()-> new IllegalArgumentException("id : " + reportId + "report를 찾을 수 없습니다."));

        if(request.getReportUserId() != null){
            User reportUser = userRepository.findById(request.getReportUserId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            report.setReportUser(reportUser);
        }
        if(request.getReportedUserId() != null){
            User reportedUser = userRepository.findById(request.getReportedUserId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            report.setReportedUser(reportedUser);
        }
        if(request.getReason() != null){
            report.setReason(request.getReason());
        }

        ReportResponse response = reportConverter.toReportResponse(report);
        return response;
    }

    @Override
    public List<ReportResponse> getMyReport() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String providerId = authentication.getName();
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Report> reports = reportRepository.findAllByReportUser(user);
        return reportConverter.toResponseList(reports);
    }

    @Override
    public ReportResponse getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(()-> new IllegalArgumentException("id : " + reportId + "report를 찾을 수 없습니다."));

        ReportResponse response = reportConverter.toReportResponse(report);
        return response;
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
