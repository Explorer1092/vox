package com.voxlearning.utopia.entity.activity.tuckerhomework;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class TuckerHomeworkInfo implements Serializable {
    private static final long serialVersionUID = 3156917583879214547L;

    private Date assignTime;          // 布置时间
    private String assignTimeStr;
    private Integer accomplishCount;  // 完成人数
    private Boolean accomplished;     // 是否完成

    public boolean accomplished() {
        return Boolean.TRUE.equals(accomplished);
    }
}
