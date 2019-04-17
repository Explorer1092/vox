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

package com.voxlearning.utopia.service.psr.entity;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

@Data
public class PsrUserHistoryEid implements Serializable {

    private static final long serialVersionUID = -5321184623097222017L;

    private Long userId;
    // 该用户当天做题记录
    private Map<String/*docId不带版本号*/, KeyValuePair<Integer/*master*/, Long/*Date.getTime()*/>> eidMasterInfoMap;
    // 最近7天psr推荐的eids
    private Map<String/*docId不带版本号*/, Long/*Date.getTime()*/> eidPsrMap;
    // 方便格式化数入库
    private Map<String/*试卷ID*/, List<String>/*任务ID*/> eidExaminationMap;
    // 方便查询 该用户历史测验数据，一张试卷分多个任务，同最近7天数据存入一条couchbase中
    private List<String/*试卷ID_任务ID*/> eidExaminationList;

    private boolean isEidMasterInfoMapInit;
    private boolean isEidPsrMapInit;

    private String getDocIdByEid(String eid) {
        if (StringUtils.isEmpty(eid))
            return eid;
        if (eid.contains("-"))
            return (eid.substring(0, eid.indexOf("-")));
        else if (eid.contains("."))
            return (eid.substring(0, eid.indexOf(".")));
        return eid;
    }

    public PsrUserHistoryEid() {
        isEidMasterInfoMapInit = false;
        isEidPsrMapInit = false;
        eidMasterInfoMap = new HashMap<>();
        eidPsrMap = new HashMap<>();
        eidExaminationMap = new HashMap<>();
        eidExaminationList = new ArrayList<>();
    }

    // return true : 不推荐
    public boolean isMasterByEid(String eid) {
        if (StringUtils.isEmpty(eid) || eidMasterInfoMap == null) return false;

        String docId = getDocIdByEid(eid);
        if (!eidMasterInfoMap.containsKey(docId)) return false;

        // 最近一个月历史上做"对"的题不推
        return (eidMasterInfoMap.get(docId).getKey() > 0);
    }

    // return true : 不推荐
    public boolean isDone(String eid) {
        if (StringUtils.isEmpty(eid) || eidMasterInfoMap == null) return false;

        String docId = getDocIdByEid(eid);
        if (eidMasterInfoMap.containsKey(docId)) return true;// 最近一个月历史上做"过"的题不推

        return false;
    }

    /*
     * 没有推荐过 return false
     * 推荐过 时间间隔 < n 天 return true(不推荐)
     * 推荐过 时间间隔 >= n 天 return false (推荐)
     */
    public boolean isPsrByEid(String eid, Integer eidPsrDays) {
        if (StringUtils.isEmpty(eid) || eidPsrMap == null) return false;

        String docId = getDocIdByEid(eid);
        Date now = new Date();
        if (eidPsrMap.containsKey(docId))
            return (now.getTime() - eidPsrMap.get(docId) < eidPsrDays * 86400 * 1000);

        return false;
    }

    public void setPsrUserHistoryEid(PsrUserHistoryEid psrUser) {
        if (psrUser == null)
            return;
        if (this.userId == null)
            this.userId = psrUser.getUserId();
        if (!MapUtils.isEmpty(psrUser.getEidPsrMap()))
            this.eidPsrMap = psrUser.getEidPsrMap();
        if (!MapUtils.isEmpty(psrUser.getEidMasterInfoMap()))
            this.eidMasterInfoMap = psrUser.getEidMasterInfoMap();
        if (!MapUtils.isEmpty(psrUser.getEidExaminationMap()))
            this.eidExaminationMap = psrUser.getEidExaminationMap();
        if (!CollectionUtils.isEmpty(psrUser.getEidExaminationList()))
            this.eidExaminationList = psrUser.getEidExaminationList();
    }

    public String formatEidMasterMapToString() {
        if (eidMasterInfoMap == null) eidMasterInfoMap = new HashMap<>();
        String ver = "2"; // 2015-03-31 add time

        String retStr = ver + "\t";

        boolean isFirst = true;
        for (Map.Entry<String, KeyValuePair<Integer, Long>> entry : eidMasterInfoMap.entrySet()) {
            if (!isFirst) retStr += ";";
            retStr += entry.getKey() + ":" + entry.getValue().getKey().toString() + ":" + entry.getValue().getValue().toString();
            isFirst = false;
        }

        return retStr;
    }

    /*
     * 数据格式如下:
     * 1. 最近7天推过的数据,ver=1 or ver=0, eg 51dae4e1a31092fb6698e2f5:1;51cbe43da310006f56e80ad8:0
     * 2. ver\t最近7天推过的数据\texamination,ver=2,中间'\t'分割, eg 2\t51dae4e1a31092fb6698e2f5:1;51cbe43da310006f56e80ad8:0\t51dae4e1a31092fb6698e2f5:1:2:3;51cbe43da310006f56e80ad8:1
     */
    public String formatEidPsrMapToString(PsrExamContent retExamContent) {
        if (retExamContent == null
                || retExamContent.getExamList() == null
                || retExamContent.getExamList().size() == 0)
            return null;

        if (eidPsrMap == null) eidPsrMap = new HashMap<>();

        String ver = "2";// 2014-11-27,加入examination数据
        String retStr = "";
        Date now = new Date();

        for (PsrExamItem item : retExamContent.getExamList()) {
            String docId = getDocIdByEid(item.getEid());
            eidPsrMap.put(docId, now.getTime());
        }

        String psrEids = "";
        for (Map.Entry<String, Long> entry : eidPsrMap.entrySet()) {
            if (now.getTime() - entry.getValue() >= 7 * 86400 * 1000) continue;
            if (!StringUtils.isEmpty(psrEids)) psrEids += ";";
            psrEids += entry.getKey() + ":" + entry.getValue().toString();
        }

        String psrExamination = "";
        if (eidExaminationMap != null && eidExaminationMap.size() > 0) {
            for (Map.Entry<String, List<String>> entry : eidExaminationMap.entrySet()) {
                if (!StringUtils.isEmpty(psrExamination)) psrExamination += ";";
                psrExamination += entry.getKey();
                for (String taskId : entry.getValue()) {
                    psrExamination += ":" + taskId;
                }
            }
        }

        retStr = ver + "\t" + psrEids + "\t" + psrExamination;
        return retStr;
    }

    public String formatEidPsrMapToString() {
        if (eidPsrMap == null) eidPsrMap = new HashMap<>();

        String ver = "2";// 2014-11-27,加入examination数据
        String retStr = "";
        Date now = new Date();

        String psrEids = "";
        for (Map.Entry<String, Long> entry : eidPsrMap.entrySet()) {
            if (now.getTime() - entry.getValue() >= 7 * 86400 * 1000) continue;
            if (!StringUtils.isEmpty(psrEids)) psrEids += ";";
            psrEids += entry.getKey() + ":" + entry.getValue().toString();
        }

        String psrExamination = "";
        if (eidExaminationMap != null && eidExaminationMap.size() > 0) {
            for (Map.Entry<String, List<String>> entry : eidExaminationMap.entrySet()) {
                if (!StringUtils.isEmpty(psrExamination)) psrExamination += ";";
                psrExamination += entry.getKey();
                for (String taskId : entry.getValue()) {
                    psrExamination += ":" + taskId;
                }
            }
        }

        retStr = ver + "\t" + psrEids + "\t" + psrExamination;
        return retStr;
    }
}
