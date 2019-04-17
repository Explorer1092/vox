/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.business.api.entity;


import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Changyuan on 2015/1/19.
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-rstaff")
@DocumentCollection(collection = "rs_paper_analysis_report")
@DocumentIndexes({
        @DocumentIndex(def = "{'paperId':1,'schoolId':1}", background = true),
        @DocumentIndex(def = "{'ccode':1}", background = true),
        @DocumentIndex(def = "{'acode':1}", background = true)
})
public class RSPaperAnalysisReport implements Serializable {
    private static final long serialVersionUID = 1242827037836279790L;

    @DocumentId private String id;
    @DocumentCreateTimestamp private Date createAt;
    @DocumentUpdateTimestamp private Date updateAt;

    private String paperId;
    private String paperName;
    private Integer pcode;
    private Integer ccode;
    private Integer acode;
    private String areaName;
    private Long schoolId;
    private String schoolName;
    private Integer stuNum; // 评测人次
    private Integer finishNum;  // 完成人数
    private Integer questionNum;    // 总做题量
    private Integer correctNum; // 做对题量
    private Integer listeningScore; // 听力总得分
    private Integer writtenScore;   // 笔试总得分
    private List<String> weakPoints; // 薄弱点

    public RSPaperAnalysisReport initializeIfNecessary() {
        if (getStuNum() == null) setStuNum(0);
        if (getFinishNum() == null) setFinishNum(0);
        if (getQuestionNum() == null) setQuestionNum(0);
        if (getCorrectNum() == null) setCorrectNum(0);
        if (getListeningScore() == null) setListeningScore(0);
        if (getWrittenScore() == null) setWrittenScore(0);
        if (getWeakPoints() == null) setWeakPoints(new LinkedList<>());
        return this;
    }
}
