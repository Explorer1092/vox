package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PsrMathLessonPersistence {

    private Long lessonId;
    private String cname;
    /**
     * Point 分两种,type=CALCULATE是计算类的知识点 , type=SPECIAL
     * 目前小学数学应用 只推 计算类 point 即 POINT_TYPE=CALCULATE
     */
    private List<PsrMathPointPersistence> points;

    public PsrMathLessonPersistence() {
        points = new ArrayList<>();
    }

    // 调试使用
    public String formatToString() {
        String retStr = "[lessonId:" + lessonId.toString() + " cname:" + cname;
        for (PsrMathPointPersistence point : points) {
            retStr += point.formatToString();
        }

        retStr += "]";

        return retStr;
    }
}
