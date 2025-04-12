package com.sullung2yo.seatcatcher.user.converter;

import com.sullung2yo.seatcatcher.user.domain.Report;
import com.sullung2yo.seatcatcher.user.dto.response.ReportResponse;

import java.util.List;

public interface ReportConverter {
    ReportResponse toReportResponse(Report report);
    List<ReportResponse> toResponseList(List<Report> reports);
}
