package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.lang.convert.ConversionUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Jia HuanYin
 * @since 2016/3/24
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegionSchool implements Comparable, Serializable {
    private static final long serialVersionUID = 7520224302446632484L;

    public static final String REGION_TYPE = "REGION";
    public static final String SCHOOL_TYPE = "SCHOOL";
    public static final String SCHOOL_REGION_TYPE = "SCHOOL_REGION";
    public static final String GROUP_TYPE = "GROUP";

    private String key;
    private String name;
    private String type;
    private String note;
    private Integer stuAuthNum;

    public RegionSchool(String key, String name, String type) {
        this.key = key;
        this.name = name;
        this.type = type;
    }

    public RegionSchool(String key, String name, String type, String note) {
        this.key = key;
        this.name = name;
        this.type = type;
        this.note = note;
    }

    @Override
    public int compareTo(Object o) {
        if (o == null || !(o instanceof RegionSchool)) {
            return -1;
        }
        RegionSchool other = (RegionSchool) o;
        return ConversionUtils.toInt(other.stuAuthNum) - ConversionUtils.toInt(this.stuAuthNum);
    }
}
