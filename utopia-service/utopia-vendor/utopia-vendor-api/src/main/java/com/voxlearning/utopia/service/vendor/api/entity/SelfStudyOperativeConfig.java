package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.calendar.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * Created by jiangpeng on 16/8/4.
 */
@Setter
@Getter
public class SelfStudyOperativeConfig {

    private String id;

    private String toolId;

    private String startDate;

    private String endDate;

    private String createDate;

    private String text;

    private List<Integer> clazzLevelList;


    public Date toStartDate() {
        return DateUtils.stringToDate(startDate, "yyyy-MM-dd HH:mm:ss");
    }

    public Date toEndDate() {
        return DateUtils.stringToDate(endDate, "yyyy-MM-dd HH:mm:ss");
    }

    public Date toCreateDate() {
        return DateUtils.stringToDate(createDate, "yyyy-MM-dd HH:mm:ss");
    }

//    public static void main(String[] args) {
//        SelfStudyOperativeConfig t = new SelfStudyOperativeConfig();
//        t.setId("op_1");
//        t.setText("随声听更新啦");
//        t.setToolId("1");
//        t.setCreateDate(new Date());
//        t.setStartDate(new Date());
//        t.setEndDate(new Date());
//        List<Integer> clazzLevelList = Arrays.asList(1,2,3,4,5,6);
//        t.setClazzLevelList(clazzLevelList);
//        System.out.println(JSON.toJSON(t));
//    }

}
