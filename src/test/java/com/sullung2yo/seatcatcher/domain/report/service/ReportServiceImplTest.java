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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // JUnit 5에서 Mockito 사용을 위한 설정
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository; // 가짜 리포지토리

    @Mock
    private UserRepository userRepository;
    @Mock
    private ReportConverter reportConverter;

    @InjectMocks
    private ReportServiceImpl reportService; // 위의 Mock이 주입될 실제 테스트 대상

    private Report report1;
    private Report report2;
    private ReportResponse response1;
    private ReportResponse response2;
    private User userA;
    private User userB;

    private ReportRequest request;

    @BeforeEach
    void setUp() {
        //임이의 유저 생성
        userA = User.builder()
                .name("유저1")
                .build();
        userB = User.builder()
                .name("유저2")
                .build();

        report1 = Report.builder()
                .reportUser(userA)
                .reportedUser(userB)
                .reason("유저 A가 B를 신고")
                .build();
        report2 = Report.builder()
                .reportUser(userB)
                .reportedUser(userA)
                .reason("유저 B가 A를 신고")
                .build();

        response1 = ReportResponse.builder()
                .reportUserId(1L)
                .reportedUserId(2L)
                .reportUserName("유저1")
                .reportedUserName("유저2")
                .reason("유저 A가 B를 신고")
                .build();

        response2 = ReportResponse.builder()
                .reportUserId(2L)
                .reportedUserId(1L)
                .reportUserName("유저2")
                .reportedUserName("유저1")
                .reason("유저 B가 A를 신고")
                .build();


        //report 생성
        request = new ReportRequest(1L, 2L, "욕설 신고");
    }

    @Test
    void getAllReports() {
        // given
        List<Report> mockReports = Arrays.asList(report1, report2);
        when(reportRepository.findAll()).thenReturn(mockReports); // Mock 객체의 동작 지정

        // when
        when(reportConverter.toResponseList(mockReports)).thenReturn(List.of(response1, response2));
        List<ReportResponse> result = reportService.getAllReports();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).contains(response1, response2);
        verify(reportRepository, times(1)).findAll(); // findAll이 한 번 호출됐는지 검증
    }

    // TODO :: TEST 코드 작성 중

    @Test
    @DisplayName("성공적으로 신고가 생성되어 저장된다")
    void createReport_success() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(userA));
        when(userRepository.findById(2L)).thenReturn(Optional.of(userB));

        // when
        reportService.createReport(request);

        // then
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    @DisplayName("신고자를 찾을 수 없으면 예외가 발생한다")
    void createReport_whenReportUserNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());


        // when & then
        UserException exception = assertThrows(UserException.class,
                () -> reportService.createReport(request));

        assertTrue(exception.getMessage().contains("해당 id를 가진 사용자를 찾을 수 없습니다."));
        assertTrue(exception.getMessage().contains("id : 1"));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode()); // ErrorCode 검증
    }

    @Test
    @DisplayName("피신고자를 찾을 수 없으면 예외가 발생한다")
    void createReport_whenReportedUserNotFound_thenThrowsException() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(userA));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // when & then
        UserException exception = assertThrows(UserException.class,
                () -> reportService.createReport(request));

        assertTrue(exception.getMessage().contains("해당 id를 가진 사용자를 찾을 수 없습니다."));
        assertTrue(exception.getMessage().contains("id : 2"));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode()); // ErrorCode 검증
    }
}