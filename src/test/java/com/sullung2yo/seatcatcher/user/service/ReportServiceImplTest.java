package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.user.domain.Report;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // JUnit 5에서 Mockito 사용을 위한 설정
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository; // 가짜 리포지토리

    @InjectMocks
    private ReportServiceImpl reportService; // 위의 Mock이 주입될 실제 테스트 대상

    private Report report1;
    private Report report2;
    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        //임이의 유저 생성
        userA = new User(null, null, null, null, null, null, null, null, null, null,null);
        userB = new User(null, null, null, null, null, null, null, null, null, null,null);

        //report 생성
        report1 = new Report(userA, userB, "유저 A가 B를 신고");
        report2 = new Report(userB, userA, "유저 B가 A를 신고");
    }

    @Test
    void getAllReports() {
        // given
        List<Report> mockReports = Arrays.asList(report1, report2);
        when(reportRepository.findAll()).thenReturn(mockReports); // Mock 객체의 동작 지정

        // when
        List<Report> result = reportService.getAllReports();

        // then
        assertThat(result).hasSize(1);
        assertThat(result).contains(report1, report2);
        verify(reportRepository, times(1)).findAll(); // findAll이 한 번 호출됐는지 검증
    }
}