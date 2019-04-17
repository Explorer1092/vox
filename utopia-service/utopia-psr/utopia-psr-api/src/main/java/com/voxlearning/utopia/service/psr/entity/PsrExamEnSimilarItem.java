package com.voxlearning.utopia.service.psr.entity;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class PsrExamEnSimilarItem implements Serializable {
    public PsrExamEnSimilarItem() {
        this.lastDate = new Date();
    }

    public PsrExamEnSimilarItem(String eid, String date, double w, double similarity, int sum, int right, String ek, String et, String alogv) {
        if (StringUtils.isEmpty(date)) this.lastDate = new Date();
        else {
            String[] sArr = date.split("-");
            if (sArr.length < 3) this.lastDate = new Date();
            else
                this.lastDate = new Date(NumberUtils.toInt(sArr[0]) - 1900,
                        NumberUtils.toInt(sArr[1]) - 1, NumberUtils.toInt(sArr[2]));
        }


        this.ek = ek;
        this.eid = eid;
        this.et = et;
        this.weight = w;
        this.alogv = alogv;
        this.sum = sum;
        this.rate = w;
        this.similarity = similarity;
    }

    public PsrExamEnSimilarItem(UserExamEnWrongItem item, String alogv) {
        if (item == null) return;

        this.eid = item.getEid();
        this.ek = item.getEk();
        this.et = item.getEt();
        this.lastDate = item.getDate();
        this.rate = item.getRate();
        this.weight = item.getRate();
        this.similarity = 0.0D;
        this.sum = item.getSumCount();
        this.alogv = alogv;
    }

    public PsrExamEnSimilarItem(ExamEnGlobalWrongItem item) {
        if (item == null) return;

        this.eid = item.getEid();
        this.ek = item.getEk();
        this.et = item.getEt();
        this.lastDate = new Date();
        this.rate = item.getRate();
        this.weight = item.getRate();
        this.similarity = 0.0D;
        this.sum = item.getSumCount();
        this.alogv = "fallibility";
    }


    private static final long serialVersionUID = 2936628975058605736L;
    private String ek;
    private String eid;
    private String et;
    private Double weight;
    private String alogv;
    private Integer sum;
    private Double rate;
    private Double similarity;
    private Date lastDate;
}
