package com.voxlearning.utopia.agent.mockexam.dao.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperProcessStateNotify;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import static com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC;

/**
 * 考卷存储模型
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_MEXAM_PAPER")
@Data
public class ExamPaperEntity implements Serializable {

    @UtopiaSqlColumn(name = "ID", primaryKey = true, primaryKeyGeneratorType = AUTO_INC)
    private Long id;

    /**
     * 计划id
     */
    @UtopiaSqlColumn
    private Long processId;

    /**
     * 试卷ID
     */
    @UtopiaSqlColumn
    private String paperId;

    /**
     * 试卷名称
     */
    @UtopiaSqlColumn
    private String paperName;

    /**
     * 试卷来源
     */
    @UtopiaSqlColumn
    private String source;

    /**
     * 类型
     */
    @UtopiaSqlColumn
    private String type;

    /**
     * 所属区域
     */
    @UtopiaSqlColumn
    private String region;

    /**
     * 教材ID
     */
    @UtopiaSqlColumn
    private String bookId;

    /**
     * 教材名称
     */
    @UtopiaSqlColumn
    private String bookName;

    /**
     * 状态
     *
     * @see ExamPaperProcessStateNotify.Status
     */
    @UtopiaSqlColumn
    private String status;

    /**
     * 学科
     */
    @UtopiaSqlColumn
    private String subject;

    /**
     * 题数
     */
    @UtopiaSqlColumn
    private String topicNum;

    /**
     * 总分
     */
    @UtopiaSqlColumn
    private Integer totalScore;

    /**
     * 考试时间
     */
    @UtopiaSqlColumn
    private Integer examTimes;

    /**
     * 创建时间
     */
    @UtopiaSqlColumn
    @DocumentCreateTimestamp
    private Date createDatetime;

    /**
     * 最后更新时间
     */
    @UtopiaSqlColumn
    @DocumentUpdateTimestamp
    private Date updateDatetime;

    /**
     * 是否公开
     */
    @UtopiaSqlColumn
    private String isPublic;

    /**
     * 逻辑删除
     */
    @UtopiaSqlColumn
    private String disable;

    /**
     * 测评引用次数
     */
    @UtopiaSqlColumn
    private Integer planTimes;

    /**
     * 测评形式
     */
    @UtopiaSqlColumn
    private String planForm;

    /**
     * 生成缓存 key
     *
     * @param paperId
     * @return
     */
    public static String ck_paperId(String paperId) {
        return CacheKeyGenerator.generateCacheKey(ExamPaperEntity.class, "pid", paperId);
    }

}
