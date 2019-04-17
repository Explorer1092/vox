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
import com.voxlearning.alps.annotation.meta.Subject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 智慧课堂题目库
 *
 * @author Maofeng Lu
 * @since 14-10-24 下午3:00
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-smartclazz")
@DocumentCollection(collection = "smartClazzQuestionLib")
public class SmartClazzQuestionLib implements Serializable {
    private static final long serialVersionUID = 4897421763161926978L;

    @DocumentId private String id;
    private Long creator;                           //创建者
    private Subject subject;                        //学科
    private String topicContent;                    //题目题干
    private String answer;                          //正确答案(存options中的KEY)
    private Map<String, String> options;            //选项(选项KEY用A,B,C,D表示)
    private String questionType;                    //题的类型：选择题、判断题
    @DocumentCreateTimestamp private Date createAt; //创建时间
    @DocumentUpdateTimestamp private Date updateAt; //更新时间
}
