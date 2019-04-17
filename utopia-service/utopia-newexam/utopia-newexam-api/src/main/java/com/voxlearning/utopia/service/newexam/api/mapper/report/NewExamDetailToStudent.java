package com.voxlearning.utopia.service.newexam.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class NewExamDetailToStudent implements Serializable {
    private static final long serialVersionUID = -1630789787709027638L;
    private String userName;
    private Long userId;
    private String paperName = "-";
    private String scoreStr = "缺考";
    private double score;//批改的话，取批改分数
    private String durationStr = "-";
    private int duration = Integer.MAX_VALUE;
    private String paperId;
    private boolean finished;//是否完成考试
    private Map<String,Object> parameters = new LinkedHashMap<>();
    private boolean begin;//是否开始考试
//    private List<String> partScore = new LinkedList<>();
    private boolean flag = true;
}
