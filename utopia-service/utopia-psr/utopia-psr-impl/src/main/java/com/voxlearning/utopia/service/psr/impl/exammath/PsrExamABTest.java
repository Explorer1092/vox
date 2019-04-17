package com.voxlearning.utopia.service.psr.impl.exammath;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.psr.entity.PsrExamContent;
import com.voxlearning.utopia.service.psr.entity.PsrExamItem;
import com.voxlearning.utopia.service.psr.entity.PsrUserHistoryEid;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnData;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionKnowledgePoint;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@Slf4j
@Named
public class PsrExamABTest implements Serializable {
    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private PsrExamEnData psrExamEnData;

    public PsrExamContent dealABTest(PsrExamContent retExamContent, PsrExamContext psrExamContext, Subject subject) {
        if (retExamContent == null) retExamContent = new PsrExamContent();
        if (psrExamContext == null) return retExamContent;

        // 限定教材,人教版三年级下:BK_10200001298249+苏教版三年级下：BK_10200001410679
        if (!psrExamContext.getBookId().equals("BK_10200001298249") && !psrExamContext.getBookId().equals("BK_10200001410679"))
            return retExamContent;

        /*
         * key : exammath_abtest
         * value : redmine_number\tA=qid1:qid2;B=qid1:qid2
         */
        String examABTestKey = "exammath_abtest";
        if (Subject.ENGLISH.equals(subject))
            examABTestKey = "examenglish_abtest";
        else if (!Subject.MATH.equals(subject))
            return retExamContent;

//        String in = "redmine_24563\tA=Q_10200744145981-1:Q_10201052653625-1:Q_10200352440663-2;B=Q_10200744145981-1:Q_10201052656487-1:Q_10200352440663-2;";
//        ekCouchbaseDao.setCouchbaseData(examABTestKey, in);

        String value = ekCouchbaseDao.getCouchbaseDataByKey(examABTestKey);
        if (StringUtils.isBlank(value))
            return retExamContent;

        Map<String/*A\B\C*/, List<String/*qid*/>> taskMap = getABTestTask(value);
        if (MapUtils.isEmpty(taskMap))
            return retExamContent;

        // AB test 只有一组数据时,与不推荐词组数据做对比
        if (taskMap.size() == 1) {
            if (psrExamContext.getUserId() % 2L == 0L) {
                for (String key : taskMap.keySet()) {
                    return initExamContent(key, taskMap.get(key), psrExamContext, subject);
                }
            } else
                return retExamContent;
        }

        // 多组AB test 数据,则互为对比
        Integer taskSize = taskMap.size();
        Long index = psrExamContext.getUserId() % taskSize.longValue();
        Long i = 0L;
        for (String key : taskMap.keySet()) {
            if (index.equals(i++))
                return initExamContent(key, taskMap.get(key), psrExamContext, subject);
        }

        return retExamContent;
    }

    private PsrExamContent initExamContent(String algo, List<String> qids, PsrExamContext psrExamContext,  Subject subject) {
        PsrExamContent retExamContent = new PsrExamContent();
        if (StringUtils.isBlank(algo) || CollectionUtils.isEmpty(qids))
            return retExamContent;

        PsrUserHistoryEid psrUserHistoryEid = psrExamEnData.getPsrUserHistoryEid(psrExamContext, subject);
        if (psrUserHistoryEid == null)
            psrUserHistoryEid = new PsrUserHistoryEid();
        if (psrUserHistoryEid.getEidExaminationMap().containsKey(algo))
            return retExamContent;

        for (String qid : qids) {
            if (retExamContent.getEids().contains(qid))
                continue;
            NewQuestion question = questionLoaderClient.loadQuestionIncludeDisabled(qid);
            if (question == null)
                continue;
            List<NewQuestionKnowledgePoint> kps = question.getKnowledgePointsNew();
            if (CollectionUtils.isEmpty(kps))
                continue;

            NewQuestionKnowledgePoint kp = kps.get(0);
            if (kp == null)
                continue;

            PsrExamItem item = new PsrExamItem();
            item.setEk(kp.getId());
            item.setEt(question.getContentTypeId().toString());
            item.setEid(qid);
            item.setWeight(0.0D);
            item.setAlogv(algo);
            item.setPsrExamType(algo);
            retExamContent.getExamList().add(item);
            retExamContent.getEids().add(qid);
        }

        if (qids.size() == retExamContent.getExamList().size()) {
            retExamContent.setErrorContent("success");
            psrUserHistoryEid.getEidExaminationMap().put(algo, qids);
            psrExamContext.setPsrUserHistoryEid(psrUserHistoryEid);
        } else {
            retExamContent.getExamList().clear();
            retExamContent.getEids().clear();
        }

        return retExamContent;
    }

    private Map<String/*A\B\C*/, List<String/*qid*/>> getABTestTask(String line) {
        Map<String/*A\B\C*/, List<String/*qid*/>> retMap = new LinkedHashMap<>();

        if (StringUtils.isBlank(line))
            return retMap;
        String[] sArr = line.split("\t");
        if (sArr.length < 2)
            return retMap;

        String redmineNumber = sArr[0];
        String abString = sArr[1];
        if (StringUtils.isBlank(abString))
            return retMap;

        return getDetailTasks(redmineNumber, abString);
    }

    private Map<String/*A\B\C*/, List<String/*qid*/>> getDetailTasks(String redmineNumber, String line) {
        Map<String/*A\B\C*/, List<String/*qid*/>> taskMap = new LinkedHashMap<>();

        if (StringUtils.isBlank(redmineNumber) || StringUtils.isBlank(line))
            return taskMap;

        String[] typeArr= line.split(";");
        if (typeArr.length < 1)
            return taskMap;

        for (String item : typeArr) {
            if (StringUtils.isBlank(item))
                continue;
            String[] itemArr= item.split("=");
            if (itemArr.length < 2)
                continue;
            if (StringUtils.isBlank(itemArr[0]) || StringUtils.isBlank(itemArr[1]))
                continue;

            String typeId = redmineNumber + "_" + itemArr[0];   // redmine_24563_A
            List<String> qids = getQids(itemArr[1]);
            if (CollectionUtils.isEmpty(qids))
                continue;

            taskMap.put(typeId, qids);
        }

        return taskMap;
    }

    private List<String/*qid*/> getQids(String line) {
        List<String/*qid*/> retQids = new LinkedList<>();

        if (StringUtils.isBlank(line))
            return retQids;

        String[] qidArr = line.split(":");
        if (qidArr.length < 1)
            return retQids;

        for (String qid : qidArr) {
            if (StringUtils.isBlank(qid))
                continue;
            if (retQids.contains(qid))
                continue;
            retQids.add(qid);
        }

        return retQids;
    }

}
