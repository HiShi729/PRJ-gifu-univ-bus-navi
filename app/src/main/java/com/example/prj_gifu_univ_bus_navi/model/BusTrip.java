package com.example.prj_gifu_univ_bus_navi.model;

import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class BusTrip {
    private final String id;
    private final String destination;
    private final BaseDayType baseDayType;
    private final String routeName;
    private final LocalTime hospitalDepartureTime;
    private final LocalTime yanagidoDepartureTime;
    private final LocalTime universityDepartureTime;
    private final LocalTime jrGifuArrivalTime;
    private final LocalTime meitetsuGifuArrivalTime;
    private final OperationRule operationRule;
    private final Integer operatingStartMonth;
    private final Integer operatingEndMonth;
    private final boolean mayBeArticulatedBus;
    private final String optionLabel;
    private final Map<String, LocalTime> stopTimes;

    public BusTrip(
        String id,
        String destination,
        BaseDayType baseDayType,
        String routeName,
        LocalTime hospitalDepartureTime,
        LocalTime yanagidoDepartureTime,
        LocalTime universityDepartureTime,
        LocalTime jrGifuArrivalTime,
        LocalTime meitetsuGifuArrivalTime,
        OperationRule operationRule,
        Integer operatingStartMonth,
        Integer operatingEndMonth,
        boolean mayBeArticulatedBus,
        String optionLabel,
        Map<String, LocalTime> stopTimes
    ) {
        this.id = id;
        this.destination = destination;
        this.baseDayType = baseDayType;
        this.routeName = routeName;
        this.hospitalDepartureTime = hospitalDepartureTime;
        this.yanagidoDepartureTime = yanagidoDepartureTime;
        this.universityDepartureTime = universityDepartureTime;
        this.jrGifuArrivalTime = jrGifuArrivalTime;
        this.meitetsuGifuArrivalTime = meitetsuGifuArrivalTime;
        this.operationRule = operationRule;
        this.operatingStartMonth = operatingStartMonth;
        this.operatingEndMonth = operatingEndMonth;
        this.mayBeArticulatedBus = mayBeArticulatedBus;
        this.optionLabel = optionLabel;
        this.stopTimes = Collections.unmodifiableMap(new LinkedHashMap<>(stopTimes));
    }

    public String getId() { return id; }
    public String getDestination() { return destination; }
    public BaseDayType getBaseDayType() { return baseDayType; }
    public String getRouteName() { return routeName; }
    public LocalTime getHospitalDepartureTime() { return hospitalDepartureTime; }
    public LocalTime getYanagidoDepartureTime() { return yanagidoDepartureTime; }
    public LocalTime getUniversityDepartureTime() { return universityDepartureTime; }
    public LocalTime getJrGifuArrivalTime() { return jrGifuArrivalTime; }
    public LocalTime getMeitetsuGifuArrivalTime() { return meitetsuGifuArrivalTime; }
    public OperationRule getOperationRule() { return operationRule; }
    public Integer getOperatingStartMonth() { return operatingStartMonth; }
    public Integer getOperatingEndMonth() { return operatingEndMonth; }
    public boolean isMayBeArticulatedBus() { return mayBeArticulatedBus; }
    public boolean getMayBeArticulatedBus() { return mayBeArticulatedBus; }
    public String getOptionLabel() { return optionLabel; }
    public Map<String, LocalTime> getStopTimes() { return stopTimes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusTrip)) return false;
        BusTrip busTrip = (BusTrip) o;
        return mayBeArticulatedBus == busTrip.mayBeArticulatedBus &&
            Objects.equals(id, busTrip.id) &&
            Objects.equals(destination, busTrip.destination) &&
            baseDayType == busTrip.baseDayType &&
            Objects.equals(routeName, busTrip.routeName) &&
            Objects.equals(hospitalDepartureTime, busTrip.hospitalDepartureTime) &&
            Objects.equals(yanagidoDepartureTime, busTrip.yanagidoDepartureTime) &&
            Objects.equals(universityDepartureTime, busTrip.universityDepartureTime) &&
            Objects.equals(jrGifuArrivalTime, busTrip.jrGifuArrivalTime) &&
            Objects.equals(meitetsuGifuArrivalTime, busTrip.meitetsuGifuArrivalTime) &&
            operationRule == busTrip.operationRule &&
            Objects.equals(operatingStartMonth, busTrip.operatingStartMonth) &&
            Objects.equals(operatingEndMonth, busTrip.operatingEndMonth) &&
            Objects.equals(optionLabel, busTrip.optionLabel) &&
            Objects.equals(stopTimes, busTrip.stopTimes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, destination, baseDayType, routeName, hospitalDepartureTime, yanagidoDepartureTime,
            universityDepartureTime, jrGifuArrivalTime, meitetsuGifuArrivalTime, operationRule,
            operatingStartMonth, operatingEndMonth, mayBeArticulatedBus, optionLabel, stopTimes);
    }

    @Override
    public String toString() {
        return "BusTrip{" +
            "id='" + id + '\'' +
            ", destination='" + destination + '\'' +
            ", baseDayType=" + baseDayType +
            ", routeName='" + routeName + '\'' +
            ", hospitalDepartureTime=" + hospitalDepartureTime +
            ", yanagidoDepartureTime=" + yanagidoDepartureTime +
            ", universityDepartureTime=" + universityDepartureTime +
            ", jrGifuArrivalTime=" + jrGifuArrivalTime +
            ", meitetsuGifuArrivalTime=" + meitetsuGifuArrivalTime +
            ", operationRule=" + operationRule +
            ", operatingStartMonth=" + operatingStartMonth +
            ", operatingEndMonth=" + operatingEndMonth +
            ", mayBeArticulatedBus=" + mayBeArticulatedBus +
            ", optionLabel='" + optionLabel + '\'' +
            ", stopTimes=" + stopTimes +
            '}';
    }
}
