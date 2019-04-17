package com.voxlearning.utopia.entity.activity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Summer on 2016/12/20.
 * 年度趣味报告
 */
@Setter
@Getter
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_interesting_report")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
@UtopiaCacheRevision("20161220")
@DocumentConnection(configName = "mongo-crm")
public class InterestingReport implements Serializable{

    private static final long serialVersionUID = 7724482541841208794L;
    @DocumentId private Long id;
    @DocumentField("user_name") private String userName;
    @DocumentField("create_date") private String createDate;                                        // 注册日期yyyy-mm-dd
    @DocumentField("diff_today") private String diffToday;                                          // 使用天数
    @DocumentField("eng_first_hw_date") private String engFirstHwDate;                              // 第一次英语作业时间 yyyy-mm-dd
    @DocumentField("math_first_hw_date") private String mathFirstHwDate;                            // 第一次数学作业时间 yyyy-mm-dd
    @DocumentField("json_stu_id") private String sameDateStudentIds;                                // 同时间完成作业的其他学生3个
    @DocumentField("voice_time") private String voiceTime;                                          // 语音时间：yyyy-MM-dd HH:mm
    @DocumentField("voice_url") private String voiceUrl;                                            // 语音URL
    @DocumentField("comment_time") private String commentTime;                                      // 第一条评语时间
    @DocumentField("tea_name") private String commentTeacherName;                                   // 第一条评语老师
    @DocumentField("comment") private String comment;                                               // 第一条评语内容
    @DocumentField("homework_num") private Integer homeworkNum90;                                   // 90分以上的作业次数 OR 老师布置作业总数
    @DocumentField("achievement_name") private String achievementName;                              // 获得的称号
    @DocumentField("json_bin") private String chartJson;                                            // 图表JSON
    @DocumentField("finish_hw_num") private Integer finishHwNum;                                    // 本学期完成作业次数
    @DocumentField("finish_rate") private Double finishRate;                                        // 本学期完成作业率
    @DocumentField("ranking") private Double ranking;                                               // 本学期超过百分之几
    @DocumentField("study_bean") private Integer studyBean;                                         // 学豆总数
    @DocumentField("bean") private Integer teacherBean;                                             // 老师发出的学豆总数
    @DocumentField("save_mins") private Integer saveMins;                                           // 老师节约时间
    @DocumentField("student_num") private Double studentNum;                                        // 老师完成作业人数

    @DocumentCreateTimestamp private Date createAt;
    @DocumentUpdateTimestamp private Date updateAt;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(InterestingReport.class, id);
    }

}
