package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author Jia HuanYin
 * @since 2015/8/19
 */
@Getter
@Setter
@NoArgsConstructor
public class RegionMeetingBean implements Comparable, Serializable {
    private static final long serialVersionUID = -8160186529375113960L;

    private Integer regionCode;
    private String regionName;
    private int meetingCount;
    private List<RegionMeetingBean> children;

    public RegionMeetingBean(Integer regionCode, String regionName) {
        this.regionCode = regionCode;
        this.regionName = regionName;
    }

    public void increase() {
        this.meetingCount++;
    }

    @Override
    public int compareTo(Object other) {
        if (other == null || !(other instanceof RegionMeetingBean)) {
            return -1;
        }
        RegionMeetingBean bean = (RegionMeetingBean) other;
        return bean.meetingCount - this.meetingCount;
    }
}
