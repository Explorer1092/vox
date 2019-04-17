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

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.api.constant.ApplicationRange;
import com.voxlearning.utopia.api.constant.MarketApplicationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 市场申请表
 * Created by dell on 2015/7/13.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "applicationFromMarket")
public class ApplicationFromMarket implements Serializable {
    private static final long serialVersionUID = 6851506514924973258L;

    @DocumentId private String id;
    @DocumentField("rt") private ApplicationRange rangeType;                //测评类型
    @DocumentField("bdt") private Date beginDateTime;                       //有效开始时间
    @DocumentField("edt") private Date endDateTime;                         //有效结束时间
    @DocumentField("fids") private List<Map<String, Object>> fileIds;       //文件列表(key : id ,fileName)
    private List<Integer> regions;                                          //开放区域
    private List<Long> schoolIds;                                           //开放学校
    private MarketApplicationStatus status;                                 //状态(待录入、录入中、录入完成)
    private String statusUpdator;                                           //上次状态更新者
    private Boolean enabled;                                                //记录有效状态,true:有效,false：无效
    private String accountName;                                             //创建者(agent用户账号)
    private String realName;                                                //创建者真实姓名
    @DocumentField("ct") @DocumentCreateTimestamp private Date createAt;    // 创建时间
    private String updator;                                                 //更新者
    @DocumentField("ut") @DocumentUpdateTimestamp private Date updateAt;    //更新时间
    @DocumentField("pid") private String pid;                               //绑定的试卷(对应试卷中的doc_id字段)

    @DocumentFieldIgnore
    public String getCreateDatetimeStr() {
        if (createAt == null) {
            return "";
        }
        return DateUtils.dateToString(createAt, "yyyy-MM-dd HH:mm:ss");
    }

    @DocumentFieldIgnore
    public String getBeginDateTimeStr() {
        if (beginDateTime == null) {
            return "";
        }
        return DateUtils.dateToString(beginDateTime, "yyyy-MM-dd HH:mm:ss");
    }

    @DocumentFieldIgnore
    public String getEndDateTimeStr() {
        if (endDateTime == null) {
            return "";
        }
        return DateUtils.dateToString(endDateTime, "yyyy-MM-dd HH:mm:ss");
    }
}
