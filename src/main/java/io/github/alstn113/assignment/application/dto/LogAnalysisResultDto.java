package io.github.alstn113.assignment.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.alstn113.assignment.domain.analysis.Analysis;
import io.github.alstn113.assignment.domain.analysis.AnalysisStatus;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "로그 분석 결과")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LogAnalysisResultDto(
        @Schema(description = "분석 ID", example = "1") Long analysisId,
        @Schema(description = "분석 상태", example = "COMPLETED") AnalysisStatus status,
        @Schema(description = "에러 메시지 (FAILED 시)", example = "CSV 파일의 헤더가 기대값과 다릅니다") String errorMessage,
        @Schema(description = "분석 요약 정보 (COMPLETED 시)") Summary summary,
        @Schema(description = "파싱 에러 요약 (COMPLETED 시)") ParsingErrorSummary parsingErrors,
        @Schema(description = "상위 경로 목록 (COMPLETED 시)") List<PathCount> topPaths,
        @Schema(description = "상위 상태 코드 목록 (COMPLETED 시)") List<StatusCodeCount> topStatusCodes,
        @Schema(description = "상위 IP 목록 (COMPLETED 시)") List<IpCount> topIps) {

    @Schema(description = "요약 정보")
    public record Summary(
            @Schema(description = "전체 처리 개수 (정상 + 에러)", example = "182267") long totalCount,
            @Schema(description = "분석 성공 요청 수 (통계 기준)", example = "182265") long validCount,
            @Schema(description = "상태 코드 비율 분포") StatusCodeDistribution statusCodeDistribution) {
        public static Summary from(long totalLines, long validRequests, LogStatistics.StatusCodeStats stats) {
            return new Summary(totalLines, validRequests, LogAnalysisResultDto.StatusCodeDistribution.from(stats));
        }
    }

    @Schema(description = "상태 코드 비율 분포 (%) - 합계 100%")
    public record StatusCodeDistribution(
            @Schema(description = "2xx 성공 비율", example = "63.0") double success2xx,
            @Schema(description = "3xx 리다이렉트 비율", example = "2.7") double redirect3xx,
            @Schema(description = "4xx 클라이언트 에러 비율", example = "32.8") double clientError4xx,
            @Schema(description = "5xx 서버 에러 비율", example = "1.4") double serverError5xx) {
        public static StatusCodeDistribution from(LogStatistics.StatusCodeStats stats) {
            return new StatusCodeDistribution(
                    stats.success2xx(),
                    stats.redirect3xx(),
                    stats.clientError4xx(),
                    stats.serverError5xx());
        }
    }

    @Schema(description = "경로별 요청 수")
    public record PathCount(
            @Schema(description = "요청 경로", example = "/event/banner/mir2/popup") String path,
            @Schema(description = "요청 횟수", example = "26981") long count) {
        public static PathCount from(LogStatistics.PathCount pc) {
            return new PathCount(pc.path(), pc.count());
        }
    }

    @Schema(description = "상태 코드별 요청 수")
    public record StatusCodeCount(
            @Schema(description = "HTTP 상태 코드", example = "200") int statusCode,
            @Schema(description = "요청 횟수", example = "114352") long count) {
        public static StatusCodeCount from(LogStatistics.StatusCodeCount sc) {
            return new StatusCodeCount(sc.statusCode(), sc.count());
        }
    }

    @Schema(description = "IP별 요청 수 및 위치 정보")
    public record IpCount(
            @Schema(description = "IP 주소", example = "120.242.23.238") String ip,
            @Schema(description = "요청 횟수", example = "20916") long count,
            @Schema(description = "IP 위치 정보") IpInfo ipInfo) {
        public static IpCount from(LogStatistics.IpCount ic) {
            return new IpCount(ic.ip(), ic.count(), IpInfo.from(ic.ipInfo()));
        }
    }

    @Schema(description = "IP 지리 정보")
    public record IpInfo(
            @Schema(description = "국가", example = "CN") String country,
            @Schema(description = "지역", example = "Shanghai") String region,
            @Schema(description = "도시", example = "Shanghai") String city,
            @Schema(description = "조직/ISP", example = "AS9808 China Mobile Communications Group Co., Ltd.") String org) {
        public static IpInfo from(LogStatistics.IpInfo info) {
            return new IpInfo(info.country(), info.region(), info.city(), info.org());
        }
    }

    @Schema(description = "파싱 에러 요약")
    public record ParsingErrorSummary(
            @Schema(description = "파싱 실패 개수", example = "50") int errorCount,
            @Schema(description = "전체 대비 에러 비율 (%)", example = "0.5") double errorRate) {
        public static ParsingErrorSummary of(int errorCount, long totalLines) {
            double rate = totalLines > 0 ? (double) errorCount / totalLines * 100 : 0.0;
            return new ParsingErrorSummary(errorCount, Math.round(rate * 100.0) / 100.0);
        }
    }

    public static LogAnalysisResultDto from(Analysis analysis) {
        long totalLines = analysis.getTotalRequests() + analysis.getParsingErrors().errorCount();
        Summary summary = Summary.from(totalLines, analysis.getTotalRequests(), analysis.getStatusCodeDistribution());

        List<PathCount> topPaths = analysis.getTopPaths().stream()
                .map(PathCount::from)
                .toList();

        List<StatusCodeCount> topStatusCodes = analysis.getTopStatusCodes().stream()
                .map(StatusCodeCount::from)
                .toList();

        List<IpCount> topIps = analysis.getTopIps().stream()
                .map(IpCount::from)
                .toList();

        ParsingErrorSummary parsingErrorSummary = ParsingErrorSummary.of(
                analysis.getParsingErrors().errorCount(),
                totalLines);

        return new LogAnalysisResultDto(
                analysis.getId(),
                analysis.getStatus(),
                null,
                summary,
                parsingErrorSummary,
                topPaths,
                topStatusCodes,
                topIps);
    }

    public static LogAnalysisResultDto processing(Long analysisId, AnalysisStatus status) {
        return new LogAnalysisResultDto(
                analysisId,
                status,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public static LogAnalysisResultDto failed(Long analysisId, String errorMessage) {
        return new LogAnalysisResultDto(
                analysisId,
                AnalysisStatus.FAILED,
                errorMessage,
                null,
                null,
                null,
                null,
                null);
    }
}