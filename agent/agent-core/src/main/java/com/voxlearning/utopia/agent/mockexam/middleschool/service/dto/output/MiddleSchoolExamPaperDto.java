package com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.output;

import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.utopia.agent.mockexam.middleschool.domain.model.MiddleSchoolExamPaper;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.*;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2019-03-18 16:57
 **/
@Data
public class MiddleSchoolExamPaperDto implements Serializable{
    private String id;
    private String createedAt;
    private Integer examTimes;
    private Integer minutes;
    private Integer paperTag;
    private String paperTagText;
    private String title;
    private Integer totalNum;
    private Integer totalScore;
    private String books;
    private String regions;
    private String usageMonth;
    private String paperPreviewUrl;               // 试卷预览地址

    public static class Builder {
        public static MiddleSchoolExamPaperDto build(MiddleSchoolExamPaper paper) {
            MiddleSchoolExamPaperDto dto = new MiddleSchoolExamPaperDto();
            BeanUtils.copyProperties(paper, dto);
            dto.setBooks(Optional.ofNullable(paper.getBooks()).orElse(Collections.emptyList())
                    .stream()
                    .filter(book -> Objects.nonNull(book))
                    .map(book -> book.getName())
                    .reduce(new String(), (boos, item) -> boos.concat(item))
            );
            dto.setRegions(Optional.ofNullable(paper.getRegions()).orElse(Collections.emptyList())
                    .stream()
                    .filter(region -> Objects.nonNull(region))
                    .map(region -> region.getName())
                    .reduce(new String(), (item1, item2) -> item1.concat(",").concat(item2))

            );
            return dto;
        }
    }

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        String result = list.stream()
                .filter(item-> item != null)
                .map(item -> String.valueOf(item))
                .reduce(new String(), (item1, item2) -> item1.concat(",").concat(item2));
        System.out.println(result);
    }
}
