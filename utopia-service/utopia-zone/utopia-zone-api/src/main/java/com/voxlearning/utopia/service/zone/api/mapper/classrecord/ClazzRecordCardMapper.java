package com.voxlearning.utopia.service.zone.api.mapper.classrecord;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.zone.api.constant.ClazzRecordCardStatusEnum;
import com.voxlearning.utopia.service.zone.api.constant.ClazzRecordTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Clazz Record Card Mapper
 * Created by alex on 2017/3/1.
 * 非作业类 均使用超类
 */
@Getter
@Setter
public class ClazzRecordCardMapper implements Serializable {
    private static final long serialVersionUID = 3219258011755952352L;

    private ClazzRecordTypeEnum recordTypeEnum;
    private ClazzRecordCardStatusEnum statusEnum;
    /**
     * {ClazzRecordTypeEnum , ClazzRecordCardStatusEnum}
     * {学霸类型 , 未检查时}       = 完成作业人数
     * {满分类型 , }              = 满分作业次数
     * {专注类型 , 最新作业未提交} = 完成作业人数
     * {明察类型 , }              = 爆料次数
     * {友爱类型 , }              = 点赞数
     * {装扮类型 , }              = 装扮数
     */
    private Integer count;
    private String image;           // 学生头像
    private String headWear;        // 学生头饰
    private Integer score;          // 得分
    private Long time;              // 做题时长
    private String homeworkId;      // 作业id
    private Boolean notExist;       // 学霸不存在 当完成作业人数不满8人的时候 true
    private Integer likeCount;      // 点赞数
    private List<Long> hasGot;      // 获得记录人数  notice:作业类，进行中 表示 已经做完作业的同学
    private Long groupId;           // 分组id


    // 作业类｛学霸、专注｝ 状态：ING   part begin
    private Integer total;          // 总共需要多少人完成  同一个group的同学数
    private Set<String> finished;   // 完成作业的人
    private boolean finishedSelf;   // 是否完成当前作业
    // 作业类｛学霸、专注｝ 状态：ING   part end

    private Subject subject;        // 作业科目
    private String studentName;     // 学生姓名
    private String startDate;       // MM月dd日

    public ClazzRecordCardMapper() {
    }

    public ClazzRecordCardMapper(ClazzRecordTypeEnum recordTypeEnum) {
        this.recordTypeEnum = recordTypeEnum;
        this.statusEnum = ClazzRecordCardStatusEnum.UNLOCK;
    }

    public ClazzRecordCardMapper(ClazzRecordTypeEnum recordTypeEnum, ClazzRecordCardStatusEnum statusEnum) {
        this.recordTypeEnum = recordTypeEnum;
        this.statusEnum = statusEnum;
    }

    public void updateSelfFinished(String userId) {
        finishedSelf = CollectionUtils.isNotEmpty(finished) && finished.contains(userId);
    }

}
