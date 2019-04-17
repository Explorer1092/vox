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

package com.voxlearning.utopia.agent.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ApplicationRange;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dell on 2015/7/13.
 */
@Data
public class OralMapper implements Serializable {

    private static final long serialVersionUID = 3953835003355190274L;
    private String id;
    private String beginDate;
    private String startHour;
    private String startMin;
    private String endDate;
    private String endHour;
    private String endMin;
    private String correctStopDate;
    private String correctStopHour;
    private String correctStopMin;
    private String resultIssueDate;
    private String resultIssueHour;
    private String resultIssueMin;
    private String rangeType;
    private String rangeTypeName;
    private List<String> regions;
    private List<String> schoolIds;
    private String fileId;
    private String fileName;
    private String status;
    private String statusName;
    private String creator;
    private String creatorName;
    private String createTimeStr;  //创建时间,格式：yyyy-MM-dd hh:mm:ss
    private boolean self;

    @JsonIgnore
    public Date convertBeginDateToDate(){
        String date = StringUtils.join(beginDate," ",startHour,":",startMin,":","00");
        return DateUtils.stringToDate(date,DateUtils.FORMAT_SQL_DATETIME);
    }

    @JsonIgnore
    public Date convertEndDateToDate(){
        String date = StringUtils.join(endDate," ",endHour,":",endMin,":","00");
        return DateUtils.stringToDate(date,DateUtils.FORMAT_SQL_DATETIME);
    }

    @JsonIgnore
    public Date convertCorrectStopToDate(){
        String date = StringUtils.join(correctStopDate," ",correctStopHour,":",correctStopMin,":","00");
        return DateUtils.stringToDate(date,DateUtils.FORMAT_SQL_DATETIME);
    }

    @JsonIgnore
    public Date convertResultIssueToDate(){
        String date = StringUtils.join(resultIssueDate," ",resultIssueHour,":",resultIssueMin,":","00");
        return DateUtils.stringToDate(date,DateUtils.FORMAT_SQL_DATETIME);
    }

    @JsonIgnore
    public List<Integer> convertRegionsToList(){
        List<Integer> list = new LinkedList<>();
        if(CollectionUtils.isEmpty(regions)){
            return list;
        }
        for(String code : regions){
            int regionCode = ConversionUtils.toInt(code);
            if(regionCode > 0){
                list.add(regionCode);
            }
        }
        return list;
    }

    @JsonIgnore
    public List<Long> convertSchoolsToList(){
        List<Long> list = new LinkedList<>();
        if(CollectionUtils.isEmpty(schoolIds)){return list;}
        for(String schoolId : schoolIds){
            list.add(ConversionUtils.toLong(schoolId));
        }
        return list;
    }

    @JsonIgnore
    public ApplicationRange convertRangeType(){
        return ApplicationRange.valueOf(rangeType);
    }

    public MapMessage dateFieldNonNullValidate(){
        if(convertBeginDateToDate() == null){
            return MapMessage.errorMessage("开始时间不能为空");
        }
        if(convertEndDateToDate() == null){
            return MapMessage.errorMessage("结束时间不能为空");
        }
        if(convertCorrectStopToDate() == null){
            return MapMessage.errorMessage("老师批改时间不能为空");
        }
        if(convertResultIssueToDate() == null){
            return MapMessage.errorMessage("发布时间不能为空");
        }
        return MapMessage.successMessage();
    }

}
