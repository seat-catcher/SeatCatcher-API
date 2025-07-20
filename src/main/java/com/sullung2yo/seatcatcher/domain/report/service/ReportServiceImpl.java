package com.sullung2yo.seatcatcher.domain.report.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.domain.report.converter.ReportConverter;
import com.sullung2yo.seatcatcher.domain.report.entity.Report;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.report.dto.request.ReportRequest;
import com.sullung2yo.seatcatcher.domain.report.dto.response.ReportResponse;
import com.sullung2yo.seatcatcher.domain.report.repository.ReportRepository;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
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
    public List<ReportResponse> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        List<ReportResponse> responses = reportConverter.toResponseList(reports);
        return responses;
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
                    .orElseThrow(() -> new UserException("해당 id를 가진 사용자를 찾을 수 없습니다. id : " + request.getReportUserId(), ErrorCode.USER_NOT_FOUND));
            report.setReportUser(reportUser);
        }
        if(request.getReportedUserId() != null){
            User reportedUser = userRepository.findById(request.getReportedUserId())
                    .orElseThrow(() -> new UserException("해당 id를 가진 사용자를 찾을 수 없습니다. id : " + request.getReportedUserId(), ErrorCode.USER_NOT_FOUND));
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
                .orElseThrow(() -> new UserException("해당 id를 가진 사용자를 찾을 수 없습니다. providerId : " + providerId, ErrorCode.USER_NOT_FOUND));

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
                .orElseThrow(() -> new UserException("해당 id를 가진 사용자를 찾을 수 없습니다. id : " + request.getReportUserId(), ErrorCode.USER_NOT_FOUND));

        User reportedUser = userRepository.findById(request.getReportedUserId())
                .orElseThrow(() -> new UserException("해당 id를 가진 사용자를 찾을 수 없습니다. id : " + request.getReportedUserId(), ErrorCode.USER_NOT_FOUND));

        Report report = Report.builder()
                .reportUser(reportUser)
                .reportedUser(reportedUser)
                .reason(request.getReason())
                .build();

        reportRepository.save(report);
    }
}
