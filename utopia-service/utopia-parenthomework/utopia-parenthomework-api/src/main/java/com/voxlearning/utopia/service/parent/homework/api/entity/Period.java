package com.voxlearning.utopia.service.parent.homework.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 周期
 *
 * @author Wenlong Meng
 * @since Feb 25, 2019
 */
@Getter
@Setter
public class Period implements Serializable {
    //local variables
    private int index;
    private Date startTime;
    private Date endTime;
    public static Period NULL = new Period(0, null, null);

    /**
     * 构建周期
     *
     * @param index
     * @param startTime
     * @param endTime
     */
    Period(int index, Date startTime, Date endTime) {
        this.index = index;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * 日期d是否包含在该周期内
     *
     * @param d
     * @return
     */
    public boolean contain(Date d){
        return startTime != null && endTime != null && startTime.before(d) && endTime.after(d);
    }

}
