package com.voxlearning.utopia.agent.mockexam.middleschool.domain.model;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.utopia.agent.mockexam.integration.MiddleSchoolExamPaperClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 中学作业模型
 * @author: kaibo.he
 * @create: 2019-03-18 16:08
 **/
@Data
public class MiddleSchoolExamPaper implements Serializable{
    private String id;
    private String createedAt;
    private Integer examTimes;
    private Integer minutes;
    private Integer paperTag;
    private String paperTagText;
    private String title;
    private Integer totalNum;
    private Integer totalScore;
    private List<Book> books;
    private List<Region> regions;
    private String usageMonth;

    public static class Builder{
        public static MiddleSchoolExamPaper build(MiddleSchoolExamPaperClient.PaperInfo paperInfo) {
            MiddleSchoolExamPaper paper = new MiddleSchoolExamPaper();
            if (Objects.isNull(paperInfo)) {
                return paper;
            }
            paper.setId(paperInfo.get_id());
            paper.setCreateedAt(paperInfo.getCreated_at());
            paper.setPaperTagText(paperInfo.getPaper_tag_text());
            paper.setPaperTag(paperInfo.getPaper_tag());
            paper.setTitle(paperInfo.getTitle());
            paper.setTotalNum(paperInfo.getTotal_num());
            paper.setMinutes(paperInfo.getMinutes());
            paper.setExamTimes(paperInfo.getExam_times());
            paper.setTotalScore(paperInfo.getTotal_score());
            paper.setBooks(Optional.ofNullable(paperInfo.getBooks()).orElse(new ArrayList<>())
                    .stream()
                    .map(book -> Book.builder().id(book.getId()).name(book.getName()).build())
                    .collect(Collectors.toList())
            );
            paper.setRegions(Optional.ofNullable(paperInfo.getRegions()).orElse(Collections.emptyList())
                    .stream()
                    .map(region -> Region.builder().id(region.getId()).name(region.getName()).build())
                    .collect(Collectors.toList())
            );
            paper.setUsageMonth(paperInfo.getUsage_month());
            return paper;
        }
    }

    @Data
    @lombok.Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Book {
        private String id;
        private String name;
    }

    @Data
    @lombok.Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaperType {
        private Integer id;
        private String name;
    }

    @Data
    @lombok.Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Region {
        private Integer id;
        private String name;
    }
}
