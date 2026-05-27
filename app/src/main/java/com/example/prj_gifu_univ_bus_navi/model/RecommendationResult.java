package com.example.prj_gifu_univ_bus_navi.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class RecommendationResult {
    private final BusStopCandidate recommendedCandidate;
    private final List<BusStopCandidate> allCandidates;
    private final String message;

    public RecommendationResult(BusStopCandidate recommendedCandidate, List<BusStopCandidate> allCandidates, String message) {
        this.recommendedCandidate = recommendedCandidate;
        this.allCandidates = Collections.unmodifiableList(new ArrayList<>(allCandidates));
        this.message = message;
    }

    public BusStopCandidate getRecommendedCandidate() { return recommendedCandidate; }
    public List<BusStopCandidate> getAllCandidates() { return allCandidates; }
    public String getMessage() { return message; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecommendationResult)) return false;
        RecommendationResult that = (RecommendationResult) o;
        return Objects.equals(recommendedCandidate, that.recommendedCandidate) &&
            Objects.equals(allCandidates, that.allCandidates) &&
            Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recommendedCandidate, allCandidates, message);
    }

    @Override
    public String toString() {
        return "RecommendationResult{" +
            "recommendedCandidate=" + recommendedCandidate +
            ", allCandidates=" + allCandidates +
            ", message='" + message + '\'' +
            '}';
    }
}
