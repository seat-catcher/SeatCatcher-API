package com.sullung2yo.seatcatcher.user.converter;

import com.sullung2yo.seatcatcher.user.domain.Report;
import com.sullung2yo.seatcatcher.user.dto.response.ReportResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReportConverterImpl implements ReportConverter{
    @Override
    public ReportResponse toReportResponse(Report report) {
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
