/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.psr.impl.util;

/**
 * Created by ChaoLi Lee on 14-7-7.
 */


// fixme 由 UserExamQuestionResultInfo 代替

@Deprecated
class Rate {
    int allCount;    // 该知识点 做题的数量
    int rightCount;  // 该知识点 做对题的数量
}

@Deprecated
public class ExamResultsInfo {
// fixme 2015.09.01 之后暂不维护
/*
    @Getter @Setter
    private Map<String, Boolean> eids;
    @Getter @Setter
    private Map<String, Rate> eks;

    public Rate getEksRate(String ek) {
        if (ek == null || eks == null || eks.size() <= 0)
            return null;

        return eks.get(ek);
    }

    public ExamResultsInfo() {
        //eids = new HashMap<>();
        //eks = new HashMap<>();
        eids = new LinkedHashMap<>();
        eks = new LinkedHashMap<>();
    }

    public int getEidAllCount(String ek) {
        if (ek == null || eks == null || eks.size() <= 0 || !eks.containsKey(ek))
            return 0;

        return eks.get(ek).allCount;
    }

    public double rightRate(String ek) {
        double rate = 0.0;

        if (ek == null || eks == null || eks.size() <= 0 || !eks.containsKey(ek))
            return rate;
        Rate tmpRate = eks.get(ek);
        if (tmpRate == null || tmpRate.allCount == 0)
            return rate;

        rate = tmpRate.rightCount / tmpRate.allCount;

        return (rate);
    }

    public Boolean isEidMaster(String eid) {
        if (eid == null || eids == null || eids.size() <= 0)
            return false;

        return (eids.get(eid));
    }

    public void setEksEidsInfo(List<ExamResult> examResults) {
        if (examResults == null || examResults.size() <= 0)
            return;

        for (ExamResult examResult : examResults) {
            Boolean master = examResult.getMaster();
            eids.put(examResult.getEid(), master);

            EmbeddedKnowledgePoint point = examResult.getPoint();
            setKnowledgePointToEks(point, examResult.getMaster());
        }
    }

    public void setEksEidsInfoEx(List<QuestionResultLog> questionResultLogs) {
        if (questionResultLogs == null || questionResultLogs.size() <= 0) {
            return;
        }
        //fixme tan
//        for (EnglishExamResult result : examResults) {
//            Boolean master = result.getAtag();
//            eids.put(result.getEid(), master);
//            EmbeddedKnowledgePoint point = JsonUtils.fromJson(result.getEk_point(), EmbeddedKnowledgePoint.class);
//            setKnowledgePointToEks(point, result.getAtag());
//            List<String> tags = result.getEk_tags();
//            if (tags == null) continue;
//            for (String tag : tags) {
//                if (!StringUtils.startsWith(tag, "word:")) continue;
//                String ek = tag.substring("word:".length(), tag.length());
//                if (!StringUtils.isNumeric(ek)) continue;
//                insertEks("word", ek, result.getAtag());
//            }
//        }
    }

    private void insertEks(String type, String ek, boolean master) {
        if (StringUtils.isEmpty(ek) || StringUtils.isEmpty(type))
            return;
        Rate rate = null;
        if (!eks.containsKey(type +"#"+ ek)) {
            rate = new Rate();
        } else {
            rate = eks.get(type +"#" + ek);
        }
        rate.allCount++;
        if (master)
            rate.rightCount++;
        eks.put(type +"#"+ ek, rate);
    }

    private void setKnowledgePointToEks(EmbeddedKnowledgePoint point, boolean master) {
        if (point == null)
            return;
        for (String word : point.getWords()) {
            insertEks("word", word, master);
        }
        for (String grammar : point.getGrammars()) {
            insertEks("grammar", grammar, master);
        }
        for (String topic : point.getTopics()) {
            insertEks("topic", topic, master);
        }
    }

*/
}
