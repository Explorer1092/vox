package com.voxlearning.utopia.mapper.rstaff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

/**
 * @author changyuan.liu
 * @since 2015/5/21
 */
@Data
@AllArgsConstructor
public class RSOralPaperReportWeakQuestionMapper implements Serializable {
    private static final long serialVersionUID = 1529496189402103198L;

    private String id;  // question id
    private String snapshot;    // 快照
    private double score;   // 老师打分
}
