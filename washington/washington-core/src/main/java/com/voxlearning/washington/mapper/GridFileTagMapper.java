/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.mapper;

import com.voxlearning.utopia.api.constant.GridFileType;
import com.voxlearning.utopia.api.constant.PaperType;
import com.voxlearning.utopia.service.business.api.entity.GridFileTag;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author RuiBao
 * @version 0.1
 * @serial
 * @since 13-5-8
 */
@Data
public class GridFileTagMapper implements Serializable {
    private static final long serialVersionUID = 3382733923996246077L;

    private GridFileType fileType;
    private String paperSubject;
    private String year;
    private String bookId;
    private String press;
    private String bookName;
    private PaperType paperType;
    private Long provinceCode;
    private String provinceName;
    private Long cityCode;
    private String cityName;
    private Long countyCode;
    private String countyName;
    private List<String> fileIdList;
    private String source; //来源
    private String knowledgePoint; //知识点
    private String bookLevel;   //书的册别 如：一年级(上）
    private Map<String, Object> extensionAttribute;  // extension attributes

    public GridFileTag toGridFileTag() {
        GridFileTag tag = new GridFileTag();
        tag.setFileType(fileType);
        tag.setPaperSubject(paperSubject);
        tag.setYear(year);
        tag.setBookId(bookId);
        tag.setPress(press);
        tag.setBookName(bookName);
        tag.setPaperType(paperType);
        tag.setProvinceCode(provinceCode);
        tag.setProvinceName(provinceName);
        tag.setCityCode(cityCode);
        tag.setCityName(cityName);
        tag.setCountyCode(countyCode);
        tag.setCountyName(countyName);
        tag.setSource(source);
        tag.setKnowledgePoint(knowledgePoint);
        tag.setBookLevel(bookLevel);
        tag.setExtensionAttribute(extensionAttribute);
        return tag;
    }

}
