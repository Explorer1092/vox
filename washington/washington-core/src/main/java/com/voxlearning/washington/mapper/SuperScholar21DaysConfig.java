package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author malong
 * @since 2018/2/27
 */
@NoArgsConstructor
@Getter
@Setter
public class SuperScholar21DaysConfig implements Serializable{
    private static final long serialVersionUID = -4608911148236014453L;

    private List<Long> userIds;
    private String text;
    private String label;
    private String preHeatUrl;
    private String activityUrl;
    private String preEndDate;
}
