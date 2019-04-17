package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.lang.convert.ConversionUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by song.wang on 2016/4/18.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatisticsData {
    private Long schoolId;//学校ID
    private Integer clazzLevel;// 年级
    private Long clazzId;//班ID
    private Long groupId;//组ID
    private Long teacherId;//老师ID

    private String name;
    private Integer totalCount;//注册数
    private Integer usedCount;//使用数
    private Integer authedCount;//认证数
    private Integer hcaActiveCount;//本月高质量
    private Integer doubleSubjectCount;//双科认证数

    private Integer noDoubleSubject;//双科认证剩余数

    private Integer validAuthHwCount;//学生认证有效布置作业数
    private Integer validHcaHwCount;//本月高覆盖作业数
}
