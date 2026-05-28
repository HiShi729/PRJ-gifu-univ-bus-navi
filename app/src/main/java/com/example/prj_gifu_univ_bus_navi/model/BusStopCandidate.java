package com.example.prj_gifu_univ_bus_navi.model;

import java.time.LocalTime;
import java.util.Objects;

public final class BusStopCandidate {
    private final BusStopId busStopId;
    private final String busStopName;
    private final String tripId;
    private final LocalTime departureTime;
    private final int travelMinutes;
    private final LocalTime arrivalTimeAtBusStop;
    private final int remainingMinutes;
    private final boolean canCatch;
    private final String routeName;
    private final String destinationBusStopName;
    private final String actualArrivalBusStopName;
    private final LocalTime destinationArrivalTime;
    private final boolean mayBeArticulatedBus;
    private final String optionLabel;
    private final String reason;

    public BusStopCandidate(
        BusStopId busStopId,
        String busStopName,
        String tripId,
        LocalTime departureTime,
        int travelMinutes,
        LocalTime arrivalTimeAtBusStop,
        int remainingMinutes,
        boolean canCatch,
        String routeName,
        String destinationBusStopName,
        String actualArrivalBusStopName,
        LocalTime destinationArrivalTime,
        boolean mayBeArticulatedBus,
        String optionLabel,
        String reason
    ) {
        this.busStopId = busStopId;
        this.busStopName = busStopName;
        this.tripId = tripId;
        this.departureTime = departureTime;
        this.travelMinutes = travelMinutes;
        this.arrivalTimeAtBusStop = arrivalTimeAtBusStop;
        this.remainingMinutes = remainingMinutes;
        this.canCatch = canCatch;
        this.routeName = routeName;
        this.destinationBusStopName = destinationBusStopName;
        this.actualArrivalBusStopName = actualArrivalBusStopName;
        this.destinationArrivalTime = destinationArrivalTime;
        this.mayBeArticulatedBus = mayBeArticulatedBus;
        this.optionLabel = optionLabel;
        this.reason = reason;
    }

    public BusStopId getBusStopId() { return busStopId; }
    public String getBusStopName() { return busStopName; }
    public String getTripId() { return tripId; }
    public LocalTime getDepartureTime() { return departureTime; }
    public int getTravelMinutes() { return travelMinutes; }
    public LocalTime getArrivalTimeAtBusStop() { return arrivalTimeAtBusStop; }
    public int getRemainingMinutes() { return remainingMinutes; }
    public boolean isCanCatch() { return canCatch; }
    public boolean getCanCatch() { return canCatch; }
    public String getRouteName() { return routeName; }
    public String getDestinationBusStopName() { return destinationBusStopName; }
    public String getActualArrivalBusStopName() { return actualArrivalBusStopName; }
    public LocalTime getDestinationArrivalTime() { return destinationArrivalTime; }
    public boolean isMayBeArticulatedBus() { return mayBeArticulatedBus; }
    public boolean getMayBeArticulatedBus() { return mayBeArticulatedBus; }
    public String getOptionLabel() { return optionLabel; }
    public String getReason() { return reason; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusStopCandidate)) return false;
        BusStopCandidate that = (BusStopCandidate) o;
        return travelMinutes == that.travelMinutes &&
            remainingMinutes == that.remainingMinutes &&
            canCatch == that.canCatch &&
            mayBeArticulatedBus == that.mayBeArticulatedBus &&
            busStopId == that.busStopId &&
            Objects.equals(busStopName, that.busStopName) &&
            Objects.equals(tripId, that.tripId) &&
            Objects.equals(departureTime, that.departureTime) &&
            Objects.equals(arrivalTimeAtBusStop, that.arrivalTimeAtBusStop) &&
            Objects.equals(routeName, that.routeName) &&
            Objects.equals(destinationBusStopName, that.destinationBusStopName) &&
            Objects.equals(actualArrivalBusStopName, that.actualArrivalBusStopName) &&
            Objects.equals(destinationArrivalTime, that.destinationArrivalTime) &&
            Objects.equals(optionLabel, that.optionLabel) &&
            Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(busStopId, busStopName, tripId, departureTime, travelMinutes, arrivalTimeAtBusStop,
            remainingMinutes, canCatch, routeName, destinationBusStopName, actualArrivalBusStopName,
            destinationArrivalTime, mayBeArticulatedBus, optionLabel, reason);
    }

    @Override
    public String toString() {
        return "BusStopCandidate{" +
            "busStopId=" + busStopId +
            ", busStopName='" + busStopName + '\'' +
            ", tripId='" + tripId + '\'' +
            ", departureTime=" + departureTime +
            ", travelMinutes=" + travelMinutes +
            ", arrivalTimeAtBusStop=" + arrivalTimeAtBusStop +
            ", remainingMinutes=" + remainingMinutes +
            ", canCatch=" + canCatch +
            ", routeName='" + routeName + '\'' +
            ", destinationBusStopName='" + destinationBusStopName + '\'' +
            ", actualArrivalBusStopName='" + actualArrivalBusStopName + '\'' +
            ", destinationArrivalTime=" + destinationArrivalTime +
            ", mayBeArticulatedBus=" + mayBeArticulatedBus +
            ", optionLabel='" + optionLabel + '\'' +
            ", reason='" + reason + '\'' +
            '}';
    }
}
