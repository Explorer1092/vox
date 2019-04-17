package com.voxlearning.utopia.agent.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * PerformanceGroupType
 *
 * @author song.wang
 * @date 2018/2/9
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PerformanceGroupType {
    SA_25_25("SA类城市渗透25月活25"),
    SA_25_50("SA类城市渗透25月活50"),
    SA_25_75("SA类城市渗透25月活75"),
    SA_25_100("SA类城市渗透25月活100"),
    SA_50_25("SA类城市渗透50月活25"),
    SA_50_50("SA类城市渗透50月活50"),
    SA_50_75("SA类城市渗透50月活75"),
    SA_50_100("SA类城市渗透50月活100"),
    SA_75_25("SA类城市渗透75月活25"),
    SA_75_50("SA类城市渗透75月活50"),
    SA_75_75("SA类城市渗透75月活75"),
    SA_75_100("SA类城市渗透75月活100"),
    SA_100_25("SA类城市渗透100月活25"),
    SA_100_50("SA类城市渗透100月活50"),
    SA_100_75("SA类城市渗透100月活75"),
    SA_100_100("SA类城市渗透100月活100"),

    BC_25_25("BC类城市渗透25月活25"),
    BC_25_50("BC类城市渗透25月活50"),
    BC_25_75("BC类城市渗透25月活75"),
    BC_25_100("BC类城市渗透25月活100"),
    BC_50_25("BC类城市渗透50月活25"),
    BC_50_50("BC类城市渗透50月活50"),
    BC_50_75("BC类城市渗透50月活75"),
    BC_50_100("BC类城市渗透50月活100"),
    BC_75_25("BC类城市渗透75月活25"),
    BC_75_50("BC类城市渗透75月活50"),
    BC_75_75("BC类城市渗透75月活75"),
    BC_75_100("BC类城市渗透75月活100"),
    BC_100_25("BC类城市渗透100月活25"),
    BC_100_50("BC类城市渗透100月活50"),
    BC_100_75("BC类城市渗透100月活75"),
    BC_100_100("BC类城市渗透100月活100")
    ;

    private final String desc;

    private static final Map<String, PerformanceGroupType> descMap = new HashMap<>();
    private static final Map<String, PerformanceGroupType> nameMap = new HashMap<>();
    static {
        for (PerformanceGroupType performanceGroupType : PerformanceGroupType.values()) {
            descMap.put(performanceGroupType.getDesc(), performanceGroupType);
            nameMap.put(performanceGroupType.name(), performanceGroupType);
        }
    }

    public static PerformanceGroupType descOf(String desc){
        return descMap.get(desc);
    }

    public static PerformanceGroupType nameOf(String name){
        return nameMap.get(name);
    }

}
