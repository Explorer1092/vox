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

package com.voxlearning.washington.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.MathBook;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by libin on 14-3-25.
 */
public class MobileBookMapper implements Serializable {
    private static final long serialVersionUID = -5515659527430623306L;

    @Getter @Setter private Long id;
    @Getter @Setter private String cname;                          //中文名称
    @Getter @Setter private String ename;                          //英文名称
    @Getter @Setter private Date createDatetime;                   //创建时间
    @Getter @Setter private Date updateDatetime;                   //更新时间
    @Getter @Setter private Boolean disabled;                      //是否可用
    @Getter @Setter private String versions;                       //版本系列
    @Getter @Setter private String press;                          //出版社
    @Getter @Setter private Integer classLevel;                    //年级
    @Getter @Setter private Integer term;                          //学期
    @Getter @Setter private Integer openExam;                      //开放应试状态 null:未开放，0:未开放，1:开放
    @Getter @Setter private String imgUrl;                         //课本图片
    @Getter @Setter private String status;                         //课本状态 分为：online:上线、offline:下线
    @Getter @Setter private String bookType;

    public MobileBookMapper() {
    }

    public MobileBookMapper(Long id, String cname, String ename, Date createDatetime, Date updateDatetime, Boolean disabled, String versions, String press, Integer classLevel, Integer term, Integer openExam, String imgUrl, String status, String bookType) {
        this.id = id;
        this.cname = cname;
        this.ename = ename;
        this.createDatetime = createDatetime;
        this.updateDatetime = updateDatetime;
        this.disabled = disabled;
        this.versions = versions;
        this.press = press;
        this.classLevel = classLevel;
        this.term = term;
        this.openExam = openExam;
        this.imgUrl = imgUrl;
        this.status = status;
        this.bookType = bookType;
    }

    @JsonIgnore
    public static List<MobileBookMapper> mapperEnglish(String imgUrlPrefix, List<Book> books) {
        if (books == null) {
            return Collections.emptyList();
        }
        List<MobileBookMapper> result = new ArrayList<>();
        for (Book book : books) {
            MobileBookMapper mapper = new MobileBookMapper(book.getId(), book.getCname(), book.getEname(), book.getCreateTime(),
                    book.getUpdateTime(), book.getDisabled(), null, book.getPress(), book.getClassLevel(), book.getTermType(),
                    book.getOpenExam(), book.getImgUrl(), book.getStatus(), "english");
            if (StringUtils.isNotBlank(book.getImgUrl())) {
                mapper.setImgUrl(imgUrlPrefix + "/upload/" + book.getImgUrl());
            }
            result.add(mapper);
        }
        return result;
    }

    @JsonIgnore
    public static List<MobileBookMapper> mapperMath(String imgUrlPrefix, List<MathBook> books) {
        if (books == null) {
            return Collections.emptyList();
        }
        List<MobileBookMapper> result = new ArrayList<>();
        for (MathBook book : books) {
            MobileBookMapper mapper = new MobileBookMapper(book.getId(), book.getCname(), null, book.getCreateDatetime(),
                    book.getUpdateDatetime(), book.getDisabled(), book.getVersions(), book.getPress(), book.getClassLevel(), book.getTerm(),
                    book.getOpenExam(), book.getImgUrl(), book.getStatus(), "math");
            if (StringUtils.isNotBlank(book.getImgUrl())) {
                mapper.setImgUrl(imgUrlPrefix + "/upload/" + book.getImgUrl());
            }
            result.add(mapper);
        }
        return result;
    }


}
