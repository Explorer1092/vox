package com.voxlearning.washington.net.message.exam;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by tanguohong on 2015/7/28.
 */
@Data
public class QuestionResultMapper implements Serializable {

    private static final long serialVersionUID = -6493938845594034094L;
    private String examId;              //题ID
    private List<String> resourceIds;   //题目材料ID
    private List<List<String>> answer;  //答案
    private Long finishTime;            //完成时长
    private Map<String, String> additions;  // 扩展属性
}
