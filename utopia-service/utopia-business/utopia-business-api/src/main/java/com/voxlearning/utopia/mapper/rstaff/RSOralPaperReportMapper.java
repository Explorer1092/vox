package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author changyuan.liu
 * @since 2015/5/21
 */
@Data
public class RSOralPaperReportMapper implements Serializable {

    private static final long serialVersionUID = 1731156374643116353L;

    private String id;
    private Long pushId;             // 教研员口语测验id
    private Long schoolId;          // 学校id
    private String schoolName;      // 学校名称
    private Integer studentCount;   // 完成人数
    private Integer totalCount;     // 应完成人数
    private Double avgMScore;       // 机器打分总分平均分
    private Double avgTScore;       // 人工打分总分平均分
    private Map<String, RSOralPaperReportPatternMapper> patterns;   // 题型数据
}
