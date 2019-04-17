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

package com.voxlearning.utopia.service.business.api.entity;


import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by shiwei.liao on 2015/6/9.
 * <p>
 * o2o扫码提交答案的提交记录
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "WashingtonDatabase")
@DocumentCollection(collection = "o2o_answer_record")
public class ExamAnswerRecord implements Serializable {
    private static final long serialVersionUID = -2237934796851517786L;

    @DocumentId private String id;
    @DocumentField("uid") private String uid;           //user id
    @DocumentField("eid") private String examId;        //试卷id
    @DocumentField("iden") private Integer identity;    //选择的身份
    @DocumentField("ajson") private String answerJson;  //选择的答案json
    @DocumentField("stime") private Date startTime;     //开始时间
    @DocumentField("etime") private Date endTime;       //结束时间

}
