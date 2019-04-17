package com.voxlearning.utopia.service.psr.entity;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
 * errorContent：判断是否取题正确,如果正确则该值为success,否则 该值为 错误码
 * examList: 例子
 * List中Map的key列表{eid,ek,et,weight,algov}
 * 另：examList 可能为空
 */
@Data
public class PsrExamContent implements Serializable {

    private static final long serialVersionUID = 84755977609804365L;

    private String errorContent;
    private List<PsrExamItem> examList;
    private List<String> eids;

    public PsrExamContent() {
        examList = new ArrayList<>();
        eids = new ArrayList<>();
    }

    public boolean isSuccess() {
        return ((errorContent != null) && errorContent.equals("success"));
    }
    public void addToExamList(PsrExamContent examContent) {
        if (examContent == null || !examContent.isSuccess() || examContent.getExamList().size() <= 0) return;

        examList.addAll(examContent.getExamList());
    }

    public void addToExamList(PsrExamEnSimilarContent psrExamEnSimilarContent) {
        if (psrExamEnSimilarContent == null || !psrExamEnSimilarContent.isSuccess() || psrExamEnSimilarContent.getExamList().size() <= 0)
            return;

        List<PsrExamEnSimilarItem> tmpExamList = psrExamEnSimilarContent.getExamList();

        for (int i = 0; tmpExamList != null && i < tmpExamList.size(); i++) {
            // ek,eid,et,weight,alogV
            PsrExamItem item = new PsrExamItem();
            item.setEk(tmpExamList.get(i).getEk());
            item.setEid(tmpExamList.get(i).getEid());
            item.setEt(tmpExamList.get(i).getEt());
            item.setWeight(tmpExamList.get(i).getWeight());
            item.setAlogv(tmpExamList.get(i).getAlogv());
            item.setPsrExamType("similar");
            this.examList.add(item);
        }
    }

    public void setExamListByEkEidListContent(EkEidListContent ekEidListContent, int reqCount, String algov, String psrExamType) {
        if (ekEidListContent == null || ekEidListContent.isEkListNull() || ekEidListContent.getEkList().size() == 0)
            return;

        List<String> aboveLevelEidList = null;

        setExamListByEkEidListContentFilter(ekEidListContent, aboveLevelEidList, reqCount, algov, psrExamType);
    }

    /*
     * ekEidListContent : 本教材 知识点对应题目列表
     * rightLevelEidList: 符合要求的题目列表,超纲需要过滤掉
     * eCount : 返回结果 条数
     */
    public void setExamListByEkEidListContentFilter(EkEidListContent ekEidListContent, List<String> rightLevelEidList, int reqCount, String algov, String psrExamType) {
        if (ekEidListContent == null || ekEidListContent.isEkListNull() || ekEidListContent.getEkList().size() == 0)
            return;

        if (StringUtils.isEmpty(algov)) algov = "p003";// 默认

        int count = 0;
        for (int i = 0; i < ekEidListContent.getEkList().size() && count < reqCount; i++) {
            EkToEidContent ekToEidContent = ekEidListContent.getEkList().get(i);
            if (ekToEidContent == null) continue;

            List<EidItem> eidList = ekToEidContent.getEidList();

            if (eidList == null) continue;

            for (int j = 0; j < eidList.size() && count < reqCount; j++) {
                // 如果 rightLevelEidList == null,则不进行 超纲判断
                if (rightLevelEidList != null && !rightLevelEidList.contains(eidList.get(j).getEid())) continue;
                if (eids.contains(eidList.get(j).getEid()))
                    continue;
                PsrExamItem psrExamItem = new PsrExamItem();
                // ek 知识点
                psrExamItem.setEk(ekToEidContent.getEk());
                // eid 题目id
                psrExamItem.setEid(eidList.get(j).getEid());
                // et 题型
                psrExamItem.setEt(eidList.get(j).getEt());
                // 预估通过率 另外计算
                // irtTheta, irt-a, irt-b
                psrExamItem.setWeight(eidList.get(j).getPredictRate());
                // alogV
                //psrExamItem.alogv = "p001";  // personal 001 第一版  todo 全局
                psrExamItem.setAlogv(algov);// p003 irt新算法

                psrExamItem.setPsrExamType(psrExamType);
                examList.add(psrExamItem);
                eids.add(eidList.get(j).getEid());
                count++;
            }
        }
    }

    public String formatList() {
        return formatList("ExamEn");
    }

    public String formatList(String type) {
        String strOut = "[";
        if (StringUtils.isEmpty(type))
            strOut += "ExamEn";
        else
            strOut += type;
        strOut += ":return code:" + errorContent + "]";

        Integer count = 0;

        if (examList != null) count = examList.size();

        strOut += "[retcount:" + count.toString() + "]";

        for (int i = 0; examList != null && i < examList.size(); i++) {
            // ek,eid,et,weight,alogV
            strOut += "[ek:" + examList.get(i).getEk();
            strOut += " eid:" + examList.get(i).getEid();
            strOut += " et:" + examList.get(i).getEt();
            strOut += " weight:" + examList.get(i).getWeight();
            strOut += " alogv:" + examList.get(i).getAlogv();
            strOut += " psrexamtype:" + examList.get(i).getPsrExamType() + "]";
        }
        return strOut;
    }
}
