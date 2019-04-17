package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.lang.calendar.DayRange;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author jiangpeng
 * @since 2017-12-28 下午12:01
 **/
@Data
public class PicListenBookPayInfo implements Serializable {
    private static final long serialVersionUID = 5873015321636748587L;


    private Long parentId;

    private DayRange dayRange;

    private ActiveStatus activeStatus;



    public enum ActiveStatus{
        active,  //在有效期内

        expired,  //已过期

        unpaid,   //未购买

        ;
    }


    public static PicListenBookPayInfo newInstance(Long parentId, DayRange dayRange){
        PicListenBookPayInfo picListenBookPayInfo = new PicListenBookPayInfo();
        picListenBookPayInfo.setParentId(parentId);
        if (dayRange == null){
            picListenBookPayInfo.setActiveStatus(ActiveStatus.unpaid);
            return picListenBookPayInfo;
        }
        picListenBookPayInfo.setDayRange(dayRange);
        if (dayRange.getEndDate().before(new Date()))
            picListenBookPayInfo.setActiveStatus(ActiveStatus.expired);
        else
            picListenBookPayInfo.setActiveStatus(ActiveStatus.active);
        return picListenBookPayInfo;

    }
}
