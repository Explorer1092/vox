package com.voxlearning.utopia.mapper;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tanguohong on 14-7-7.
 */
@Data
public class ReadingDraftPageTemp implements Serializable {

    private static final long serialVersionUID = -3388770059520650377L;
    private Integer pageNum = 1;
    private String pageLayout = "pt";
    private List<Map<String,Object>> keyWords; // 本页重点词，key:英文词内容 value:词说明
    private List<Map<String,Object>> keySentences ;
    private List<Map<String,Object>> keySentencesAnalysis ;  // 本页重点句子, key:英文句子内容 value:句子说明
    private String picUri;
    private List<ReadingDraftSentenceTemp> readingSentences = new ArrayList<>(); //句子


}
