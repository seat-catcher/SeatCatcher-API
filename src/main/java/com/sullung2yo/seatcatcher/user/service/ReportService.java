package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.user.domain.Report;
import com.sullung2yo.seatcatcher.user.dto.request.ReportRequest;

import java.util.List;

public interface ReportService {
    public List<Report> getAllReports();
    public void deleteReport(Long reportId);

//    public ReportResponse updateReport(Long reportId, ReportUpdateRequest request);
//    public ReportResponse getReportById(Long reportId);
//    public List<ReportResponse> getMyReports(ReportSearchCondition condition);
    public void createReport(ReportRequest request);
}
