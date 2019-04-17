package com.voxlearning.utopia.service.psr.impl.examen;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.io.Serializable;

/**
 * psr 应试英语 实验调度
 * 1. 一共5道测试题
 * 2. 每道测试题挂载12道练习题(分成4组,每组3道题)
 * 3. 先依次推荐测试题n,做对n则做下一道测试题n1,没做对n则推荐相应的练习题m,m做完后推荐测试题n,归结说就是测试-练习-测试
 * 4. 按学生id的尾号,抽取2、4为A组,6、8为B组
 * 5. A组为: 测试-练习(12道题)-测试,B组为: 测试-联系(6道题)-测试
 * 6. 测试题出完为止
 */

@Slf4j
@Named
@Data
@Deprecated // 2015.08.10
public class PsrExamEnTestFiveEids implements Serializable {
// fixme 2015.09.01 之后暂不维护
/*
    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrExamEnController psrExamEnController;
    @Inject private PsrEnglishExamResultMemcachedDao psrEnglishExamResultMemcachedDao;
    @Inject private PsrExamEnGetData psrExamEnGetData;

    public PsrExamContent deal(PsrExamContext psrExamContext) {
        Date dtB = new Date();

        PsrExamContent retExamContent = new PsrExamContent();

        // 日志 及返回值
        if (ekCouchbaseDao == null || psrExamContext == null || psrExamEnController == null)
            return logContent(retExamContent, psrExamContext, "Can not connect databases,or...", dtB, "error");

        if (!isExam(psrExamContext))
            return logContent(retExamContent, null, "no need examTest", dtB, "info");

        // 获取该用户历史测验数据
        String strKey = "testfiveeidsuid_" + psrExamContext.getUserId();
        String strValue = ekCouchbaseDao.getCouchbaseDataByKey(strKey);
        List<ExamEnTestFiveEids> userTestInfos = decodeTestInfos(strValue);

        // 获取测试题及练习题数据
        String strEidsKey = "testfiveeids";
        String strEidsValue = ekCouchbaseDao.getCouchbaseDataByKey(strEidsKey);
        List<ExamEnTestFiveEids> testAndPracticeEids = decodeTestInfos(strEidsValue);
        if (testAndPracticeEids.size() <= 0)
            return logContent(retExamContent, null, "not found examTest eids from couchbase", dtB, "info");

        // 取出要测试的题或者练习的题,同时更新用户的测试题信息(推荐及掌握信息)
        List<ExamEnTestFiveEidItem> retItems = getTestEids(userTestInfos, testAndPracticeEids, psrExamContext);

        // 没有测验题则退出
        if (retItems.size() <= 0) {
            // [尾号1、3、5、7 用户的信息]
            strValue = encodeTestInfos(userTestInfos, true);
            if (!StringUtils.isEmpty(strValue)) {
                ekCouchbaseDao.setCouchbaseData(strKey, strValue);
            }
            return logContent(retExamContent, null, "not found examTest eids by userId", dtB, "info");
        }

        // 记录测验题或者练习题,防止psr补题的时候推重复的题
        if (psrExamContext.getRecommendEids() == null)
            psrExamContext.setRecommendEids(new ArrayList<String>());
        for (ExamEnTestFiveEidItem testItem : retItems) {
            if (!psrExamContext.getRecommendEids().contains(testItem.getEid()))
                psrExamContext.getRecommendEids().add(testItem.getEid());

            PsrExamItem examItem = new PsrExamItem();
            examItem.setEk(testItem.getEk());
            examItem.setEid(testItem.getEid());
            examItem.setEt(testItem.getEt());
            examItem.setAlogv(getExamTypeByEidStatus(testItem.getEidStatus()).getType());

            examItem.setPsrExamType("");
            retExamContent.getExamList().add(examItem);
        }

        // 用psr补充题目数量
        Integer eCount = psrExamContext.getECount();
        psrExamContext.setECount(eCount - retItems.size());
        boolean isWriteLog = psrExamContext.isWriteLog();
        psrExamContext.setWriteLog(false);
        PsrExamContent examContent = psrExamEnController.dealAll(psrExamContext);
        psrExamContext.setWriteLog(isWriteLog);
        if (examContent.isSuccess())
            retExamContent.addToExamList(examContent);
        psrExamContext.setECount(eCount);


        // 记录测验数据
        strValue = encodeTestInfos(userTestInfos, true);
        if (!StringUtils.isEmpty(strValue)) {
            ekCouchbaseDao.setCouchbaseData(strKey, strValue);
        }

        return logContent(retExamContent, psrExamContext, "success", dtB, "info");
    }

    public List<ExamEnTestFiveEidItem> getTestEids(List<ExamEnTestFiveEids> userTestInfos,
                                                   List<ExamEnTestFiveEids> testAndPracticeEids,
                                                   PsrExamContext psrExamContext) {
        List<ExamEnTestFiveEidItem> retList = new ArrayList<>();
        if (testAndPracticeEids.size() <= 0)
            return retList;

        // 首次来吧,推荐第一道测试题,剩下的psr补题
        if (userTestInfos.size() <= 0) {
            for (ExamEnTestFiveEids eids : testAndPracticeEids) {
                eids.getEidItem().setEidStatus(1);  // 本次测试题推荐状态
                userTestInfos.add(eids);
                retList.add(eids.getEidItem());
            }
            return retList;
        }

        // 获取该用户历史做题记录
        // fixme del
//        PsrUserHistoryEid psrUserHistoryEid = psrExamContext.getPsrUserHistoryEid();
//        if (psrUserHistoryEid == null)
//            psrUserHistoryEid = new PsrUserHistoryEid();
//        if (psrUserHistoryEid.getEidMasterInfoMap() == null)
//            psrUserHistoryEid.setPsrUserHistoryEid(ekCouchbaseDao.getCouchbaseUserHistory(psrExamContext.getUserId()));
//        if (psrUserHistoryEid.getEidPsrMap() == null
//                || psrUserHistoryEid.getEidExaminationMap() == null
//                || psrUserHistoryEid.getEidExaminationList() == null)
//            psrUserHistoryEid.setPsrUserHistoryEid(ekCouchbaseDao.getCouchbaseUserHistoryPsr(psrExamContext.getUserId()));
//        psrExamContext.setPsrUserHistoryEid(psrUserHistoryEid);

        PsrUserHistoryEid psrUserHistoryEid = psrExamEnGetData.getPsrUserHistoryEid(psrExamContext);

        // 取出当天 用户的做题数据
        // fixme del
//        List<QuestionResultLog> examResults = psrExamContext.getExamResults();
//        if (examResults == null) {
//            examResults = psrEnglishExamResultMemcachedDao.findByUserId(psrExamContext.getUserId());
//            psrExamContext.setExamResults(examResults);
//        }
        List<QuestionResultLog> examResults = psrExamEnGetData.getQuestionResultLog(psrExamContext);

        psrUserHistoryEid.formatEidMasterMapToStringEx(examResults);
        Map<String, KeyValuePair<Integer/ *master* /, Long/ *Date.getTime()* />> eidMasterInfoMap = psrUserHistoryEid.getEidMasterInfoMap();

        // 建索引
        Map<String, ExamEnTestFiveEids> testAndPracticeEidsMap = new HashMap<>();
        for (ExamEnTestFiveEids tmpEids : testAndPracticeEids) {
            if (testAndPracticeEidsMap.containsKey(tmpEids.getEidItem().getEid()))
                continue;
            testAndPracticeEidsMap.put(tmpEids.getEidItem().getEid(), tmpEids);
        }

        // 已经推荐过测试题,则推荐其练习题
        for (ExamEnTestFiveEids eidItem : userTestInfos) {
            TestEidStatus testEidStatus = getExamTypeByEidStatus(eidItem.getEidItem().getEidStatus());
            // 先更新用户测验数据,只更新该测试题第一次推荐后的反馈数据(是否做对)
            if (eidMasterInfoMap != null && testEidStatus.isTestEidRecommendBP()
                    && !testEidStatus.isTestEidFeedbackBP() && eidMasterInfoMap.containsKey(eidItem.getEidItem().getEid())) {
                KeyValuePair<Integer, Long> pair = eidMasterInfoMap.get(eidItem.getEidItem().getEid());
                testEidStatus.setTestEidFeedbackBP(true);
                testEidStatus.setTestEidRightBP(pair.getKey() == 1);

                eidItem.getEidItem().setEidStatus(eidItem.getEidItem().getEidStatus() | 2);
                if (testEidStatus.isTestEidRightBP())
                    eidItem.getEidItem().setEidStatus(eidItem.getEidItem().getEidStatus() | 4);
            }

            Integer type = testEidStatus.getRecommendType(psrExamContext.getUserId());
            Integer index  = testEidStatus.getPracticeEidIndex();
            int byteStatus = 8 << index;

            switch (type) {
                case 0:
                    // 该套测练题已经完成,不需要再测
                    break;
                case 1:
                    // 第二次推荐测试题
                    eidItem.getEidItem().setEidStatus(eidItem.getEidItem().getEidStatus() | 128);
                    retList.add(eidItem.getEidItem());
                    break;
                case 2:
                    // 尾号是1\3,5\7,只更新状态,不追加练习题,由外面的psr提供
                    eidItem.getEidItem().setEidStatus(eidItem.getEidItem().getEidStatus() | byteStatus);
                    break;
                case 3:
                    // 配置文件中没有练习题则跳过该测练
                    Map<String,ExamEnTestFiveEidItem> practiceEidsMap =
                            testAndPracticeEidsMap.containsKey(eidItem.getEidItem().getEid())
                                    ? testAndPracticeEidsMap.get(eidItem.getEidItem().getEid()).getPracticeEidsMap()
                                    : null;
                    // [尾号是2\4,6\8 推荐练习题]获取[index*3,index*3+3] 3个练习题
                    if (practiceEidsMap != null) {
                        int i = 0;
                        int eidCount = 0;
                        for (Map.Entry<String, ExamEnTestFiveEidItem> entry : practiceEidsMap.entrySet()) {
                            if (i++ >= index * 3) {
                                retList.add(entry.getValue());
                                eidCount++;
                            }
                            if (eidCount >= 3)
                                break;
                        }
                        eidItem.getEidItem().setEidStatus(eidItem.getEidItem().getEidStatus() | byteStatus);
                    }
                    break;
                case 4:
                    // 没有收到该测试题的反馈,处于等待状态
                    break;
                default:
                    break;
            }
        }

        // 外面 psr 补题
        return retList;
    }

    public TestEidStatus getExamTypeByEidStatus(Integer eidStatus) {
        TestEidStatus testEidStatus = new TestEidStatus();
        if (eidStatus == null)
            return testEidStatus;

        // int 按位取值,从最低位依次是
        // [0]:第一次推荐与否 0 没推荐 1 反之
        // [1]:第一次推荐后有无反馈 0 无反馈 1 反之
        // [2]:第一次推荐后正确与否 0 错误 1 反之
        // [3]:第一组练习题推荐与否 0 没推荐 1 反之
        // [4]:第二组练习题推荐与否 0 没推荐 1 反之
        // [5]:第三组练习题推荐与否 0 没推荐 1 反之
        // [5]:第四组练习题推荐与否 0 没推荐 1 反之
        // [7]:第二次推荐与否 0 没推荐 1 反之
        // 第一次推荐后并且做正确则不在推荐练习题
        // 第二次推荐后不再推荐

        boolean testEidRecommendBP = false; // false:本测验没有推荐过,true:本测验推荐过,testEidRecommendBeforPractice
        boolean testEidFeedbackBP = false;  // false:表示没有反馈结果,true:收到反馈结果
        boolean testEidRightBP = false;     // false:表示反馈结果是错误的(做错了),true:表示反馈结果是正确的(做对了)
        Integer practiceEidIndex = 0;       // 0:表示还没有推荐对应的练习题,1:表示推荐了第一组,2:表示推荐了第二组,3:表示推荐了第三组,4:表示推荐了第四组,一共四组练习题
        boolean testEidRecommendAP = false; // false:本测验没有推荐过,true:本测验推荐过,testEidRecommendAfterPractice

        testEidRecommendBP = (eidStatus & 1) == 1;
        testEidFeedbackBP = (eidStatus & 2) == 2;  // 是否收到反馈
        if (testEidFeedbackBP)
            testEidRightBP = (eidStatus & 4) == 4;
        if ((eidStatus & 8) == 8)
            practiceEidIndex = 1;
        if ((eidStatus & 16) == 16)
            practiceEidIndex = 2;
        if ((eidStatus & 32) == 32)
            practiceEidIndex = 3;
        if ((eidStatus & 64) == 64)
            practiceEidIndex = 4;
        testEidRecommendAP = (eidStatus & 128) == 128;

        //testBP_feed_right_practice1_testAP
        String strType;
        if (testEidRecommendBP) strType = "testBP";
        else strType = "notestBP";
        if (testEidFeedbackBP) strType += "_feed";
        else strType += "_nofeed";
        if (testEidRightBP) strType += "_right";
        else strType += "_wrong";
        strType += "_practice" + practiceEidIndex;
        if (testEidRecommendAP) strType += "_testAP";
        else strType += "_notestAP";

        testEidStatus.setTestEidRecommendBP(testEidRecommendBP);
        testEidStatus.setTestEidFeedbackBP(testEidFeedbackBP);
        testEidStatus.setTestEidRightBP(testEidRightBP);
        testEidStatus.setPracticeEidIndex(practiceEidIndex);
        testEidStatus.setTestEidRecommendAP(testEidRecommendAP);
        testEidStatus.setType(strType);

        return testEidStatus;
    }

    // ver \t testEid:status:ek:et;practiceEid:status:ek:et,practiceEid:status:ek:et \t testEid:status:ek:et;practiceEid:status:ek:et,practiceEid:status:ek:et
    public List<ExamEnTestFiveEids> decodeTestInfos(String strLine) {
        List<ExamEnTestFiveEids> retList = new ArrayList<>();
        if (StringUtils.isEmpty(strLine))
            return retList;

        String[] sLineArr = strLine.split("\t");
        if (sLineArr.length < 2)
            return retList;

        String ver = sLineArr[0];

        for (int i = 1; i < sLineArr.length; i++) {
            ExamEnTestFiveEids item = new ExamEnTestFiveEids();
            item.decode(sLineArr[i]);
            retList.add(item);
        }

        return retList;
    }


    public String encodeTestInfos(List<ExamEnTestFiveEids> infoList) {
        return encodeTestInfos(infoList, false);
    }

    public String encodeTestInfos(List<ExamEnTestFiveEids> infoList, boolean isHeader) {
        String retStr = "";
        if (infoList == null || infoList.size() <= 0)
            return retStr;

        for (ExamEnTestFiveEids info : infoList) {
            String strTmp = info.encode(isHeader);
            if (StringUtils.isEmpty(strTmp))
                continue;

            if (!StringUtils.isEmpty(retStr))
                retStr += "\t";

            retStr += strTmp;
        }

        String ver = "1";
        retStr = ver + "\t" + retStr;

        return retStr;
    }

    // 是否符合测验人群 根据uid末尾划分
    // 组1: 逻辑:测试-练习-测试 [2\4 : 分4次推12道练习题, 6\8 : 分2次推6道练习题]
    // 组2: 逻辑:测试-psr-测试 [1\3 : 分4次推12道psr题, 5\7 : 分2次推6道psr题]
    // 组1和组2区别: 组1中间推给定练习题,组2中间推psr题
    public boolean isExam(PsrExamContext psrExamContext) {
        if (psrExamContext == null)
            return false;
        long n = psrExamContext.getUserId() % 10;
        return (n >= 1 && n<= 8);
    }

    private PsrExamContent logContent(PsrExamContent retExamContent,
                                      PsrExamContext psrExamContext, String errorMsg,
                                      Date dtB, String logLevel) {
        if (retExamContent == null)
            retExamContent = new PsrExamContent();
        if (StringUtils.isEmpty(errorMsg))
            errorMsg = "success";

        retExamContent.setErrorContent(errorMsg);
        if (!errorMsg.equals("success"))
            retExamContent.getExamList().clear();

        if (psrExamContext != null && psrExamContext.isWriteLog()) {
            if (StringUtils.isEmpty(logLevel))
                logLevel = "info";
            Date dtE = new Date();
            Long uTAll = dtE.getTime() - dtB.getTime();
            String strLog = formatReturnLog(retExamContent, psrExamContext, uTAll);
            switch (logLevel) {
                case "info":
                    log.info(strLog);
                    break;
                case "error":
                    log.error(strLog);
                    break;
                case "warn":
                    log.warn(strLog);
                    break;
                default:
                    log.info(strLog);
            }
        }

        return retExamContent;
    }

    private String formatReturnLog(PsrExamContent retExamContent, PsrExamContext psrExamContext, Long spendTime) {
        String strLog = retExamContent.formatList("ExamTestFiveEids");
        strLog += "[product:" + psrExamContext.getProduct() + " uType:" + psrExamContext.getUType() + " userId:" + psrExamContext.getUserId().toString();
        strLog += " region:" + Integer.valueOf(psrExamContext.getRegionCode()).toString() + " book:" + psrExamContext.getBookId().toString() + " unit:" + psrExamContext.getUnitId().toString();
        strLog += " eCount:" + Integer.valueOf(psrExamContext.getECount()).toString();
        strLog += " minP:" + Float.valueOf(psrExamContext.getMinP()).toString() + " maxP:" + Float.valueOf(psrExamContext.getMaxP()).toString() + "]";
        strLog += "[TotalTime:" + spendTime.toString() + "]";

        return strLog;
    }
}

@Data
@Deprecated // 2015.08.10
class TestEidStatus implements Serializable {
    private boolean testEidRecommendBP = false; // false:本测验没有推荐过,true:本测验推荐过,testEidRecommendBeforPractice
    private boolean testEidFeedbackBP = false;  // false:表示没有反馈结果,true:收到反馈结果
    private boolean testEidRightBP = false;     // false:表示反馈结果是错误的(做错了),true:表示反馈结果是正确的(做对了)
    private Integer practiceEidIndex = 0;       // 0:表示还没有推荐对应的练习题,1:表示推荐了第一组,2:表示推荐了第二组,3:表示推荐了第三组,4:表示推荐了第四组,一共四组练习题
    private boolean testEidRecommendAP = false; // false:本测验没有推荐过,true:本测验推荐过,testEidRecommendAfterPractice
    private String type = "";

    // 0 : 不推荐, 1 : 推荐 测试题, 2 : [尾号:1\3\5\7]模拟推荐练习题(为了对比测试,按推荐练习题的流程走,测-psr,psr[psr,psr]-测), 3 : [尾号:2\4\6\8]推荐练习题(测-练,练[练,练]-测),4 : 暂停测试题及其练习题的推荐,直到得到用户的反馈(推荐给用户测试题但是没收到反馈,进入等待状态)
    public Integer getRecommendType(Long uid) {
        // 需要推荐(test or practice)
        if (testEidRecommendAP) // 第二次测试过,不在推荐,可以寻找下一个测试题
            return 0;
        if (!testEidFeedbackBP) // 第一次推荐了,但是没有反馈结果不推荐,暂停推荐测试题和练习题直到该测试题用户做过(得到用户的反馈后才进行后面的操作)
            return 4;
        if (testEidRightBP) // 第一次推荐并且做对了不推荐
            return 0;

        // 是否推荐练习题 practice
        Integer practiceType = isRecommendPracticeEid(uid);
        if (practiceType == 0)
            return 1;
        else if (practiceType == 1)
            return 2;
        else if (practiceType == 2)
            return 3;

        return 0;
    }

    // 0 : 不推荐, 1 : 表示推荐且uid尾号是1\3\5\7, 2 : 表示推荐且尾号是2\4\6\8
    public Integer isRecommendPracticeEid(Long uid) {
        Integer ret = 0;
        if (uid == null)
            return ret;

        // 按学生分组 2、4一组做12道练习题, 6、8一组做6道练习题
        Integer index = 0;
        Long n = uid % 10;
        if (n == 1 || n == 3 || n == 2 || n == 4)
            index = 4;                  // 4任务12道练习题
        if (n == 5 || n == 7 || n == 6 || n == 8)
            index = 2;                  // 2任务6道练习题

        // 练习题已经推完
        if (practiceEidIndex >= index)
            return ret;

        ret = 1;
        if (n == 2 || n == 4 || n == 6 || n == 8)
            ret = 2;

        return ret;
    }

*/
// fixme 2015.09.01 之后暂不维护

}
