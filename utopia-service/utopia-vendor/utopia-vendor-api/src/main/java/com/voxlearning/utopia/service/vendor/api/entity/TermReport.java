package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

/**
 * @author malong
 * @since 2017/6/19
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-activity")
@DocumentCollection(collection = "vox_term_report")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TermReport implements CacheDimensionDocument{
    private static final long serialVersionUID = -4580975799268317792L;

    @DocumentId
    private String id;
    @DocumentField("parent_id")
    private Long parentId;
    @DocumentField("student_id")
    private Long studentId;
    /**
     * 学生完成作业次数
     */
    @DocumentField("stu_hwfinish_count")
    private Integer finishCount;
    /**
     * 学生作业完成率
     */
    @DocumentField("stu_hwfinish_rate")
    private Double finishRate;
    /**
     * 学生获得学霸次数
     */
    @DocumentField("stu_studymaster_count")
    private Integer studyMasterCount;
    /**
     * 学生做题数量
     */
    @DocumentField("stu_question_count")
    private Integer questionCount;
    /**
     * 学生做题正确率
     */
    @DocumentField("stu_right_cent")
    private Double questionRightCent;
    /**
     * 学生击败全班学生百分比
     */
    @DocumentField("stu_ranking")
    private Double rank;
    /**
     * 学生获得称号
     */
    @DocumentField("stu_title")
    private String studentTile;
    /**
     * 家长查看作业报告次数
     */
    @DocumentField("par_hwreport_num")
    private Integer parentCheckCount;
    /**
     * 家长获得称号
     */
    @DocumentField("par_title ")
    private String parentTitle;

    @Override
    public String[] generateCacheDimensions() {
        return new String[] {
                newCacheKey(new String[] {"PID", "SID"}, new Object[] {parentId, studentId})
        };
    }
}
