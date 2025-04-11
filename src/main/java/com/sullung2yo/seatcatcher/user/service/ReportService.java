package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.user.domain.Report;
import com.sullung2yo.seatcatcher.user.dto.request.ReportRequest;
import com.sullung2yo.seatcatcher.user.dto.response.ReportResponse;

import java.util.List;

public interface ReportService {
    public List<Report> getAllReports();
    public void deleteReport(Long reportId);


    public ReportResponse updateReport(Long reportId, ReportRequest request);

    public List<ReportResponse> getMyReport();
    public ReportResponse getReportById(Long reportId);
    public void createReport(ReportRequest request);
}
