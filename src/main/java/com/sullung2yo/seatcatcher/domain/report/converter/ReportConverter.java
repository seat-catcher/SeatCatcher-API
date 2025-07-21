package com.sullung2yo.seatcatcher.domain.report.converter;

import com.sullung2yo.seatcatcher.domain.report.entity.Report;
import com.sullung2yo.seatcatcher.domain.report.dto.response.ReportResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReportConverter{
    public ReportResponse toReportResponse(Report report) {
        if (report.getReportUser() == null || report.getReportedUser() == null) {
            throw new IllegalArgumentException("신고자 혹은 피신고자 정보가 누락되었습니다.");
        }
        return ReportResponse.builder()
                .reportUserId(report.getReportUser().getId())
                .reportUserName(report.getReportUser().getName())
                .reportedUserId(report.getReportedUser().getId())
                .reportedUserName(report.getReportedUser().getName())
                .reason(report.getReason())
                .build();
    }

    public List<ReportResponse> toResponseList(List<Report> reports) {
        return reports.stream()
                .map(this::toReportResponse)
                .collect(Collectors.toList());
    }
}
