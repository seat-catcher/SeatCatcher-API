package com.sullung2yo.seatcatcher.domain.report.service;

import com.sullung2yo.seatcatcher.domain.report.dto.request.ReportRequest;
import com.sullung2yo.seatcatcher.domain.report.dto.response.ReportResponse;

import java.util.List;

public interface ReportService {
    public List<ReportResponse> getAllReports();
    public void deleteReport(Long reportId);


    public ReportResponse updateReport(Long reportId, ReportRequest request);

    public List<ReportResponse> getMyReport();
    public ReportResponse getReportById(Long reportId);
    public void createReport(ReportRequest request);
}
