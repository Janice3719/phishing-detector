package com.example.phishingdetector.dto;

public class ScanStatsDto {
    private long totalScans;
    private long dangerousCount;
    private long warningCount;
    private long safeCount;

    public ScanStatsDto() {
    }

    public ScanStatsDto(long totalScans, long dangerousCount, long warningCount, long safeCount) {
        this.totalScans = totalScans;
        this.dangerousCount = dangerousCount;
        this.warningCount = warningCount;
        this.safeCount = safeCount;
    }

    public long getTotalScans() {
        return totalScans;
    }

    public void setTotalScans(long totalScans) {
        this.totalScans = totalScans;
    }

    public long getDangerousCount() {
        return dangerousCount;
    }

    public void setDangerousCount(long dangerousCount) {
        this.dangerousCount = dangerousCount;
    }

    public long getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(long warningCount) {
        this.warningCount = warningCount;
    }

    public long getSafeCount() {
        return safeCount;
    }

    public void setSafeCount(long safeCount) {
        this.safeCount = safeCount;
    }
}