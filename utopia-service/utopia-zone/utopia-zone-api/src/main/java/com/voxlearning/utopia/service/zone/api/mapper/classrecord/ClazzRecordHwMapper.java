package com.voxlearning.utopia.service.zone.api.mapper.classrecord;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/3/3
 * Time: 16:16
 * 作业记录
 */
@Setter
@Getter
public class ClazzRecordHwMapper implements Serializable {
    private static final long serialVersionUID = 4512087178863765258L;

    // homework id
    private String homeworkId;
    private Date createTime;
    //是否完成
    private Boolean finish;
    //是否补做
    private Boolean repair;
    //是否提升
    private Boolean improved;
    //是否全班最高
    private Boolean highest;
    //是否全班最快
    private Boolean fastest;
    //得分
    private Integer score;
    //做题时间
    private Long time;
    //中断时间
    private Long breakTime;

    private Subject subject;

    public boolean getFullMarks() {
        return score != null && score >= 100;
    }

    public static String ck_user_homework(Long userId, String homeworkId) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordHwMapper.class,
                new String[]{"U", "HW"},
                new Object[]{userId, homeworkId});
    }

}
