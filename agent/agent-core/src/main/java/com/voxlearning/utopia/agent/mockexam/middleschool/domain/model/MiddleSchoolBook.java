package com.voxlearning.utopia.agent.mockexam.middleschool.domain.model;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.mockexam.integration.MiddleSchoolExamPaperClient;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 中学教材模型
 * @author: kaibo.he
 * @create: 2019-03-19 18:38
 **/
@Data
@Accessors(chain = true)
@Builder
public class MiddleSchoolBook implements Serializable{
    private String name;
    private String id;

    public static class Builder {
        public static List<MiddleSchoolBook> build(MiddleSchoolExamPaperClient.PaperSearchItemResponse response) {
            List<MiddleSchoolExamPaperClient.Book> books = response.getBooks();
            if (CollectionUtils.isEmpty(books)) {
                return new ArrayList<>();
            }
            return books
                    .stream()
                    .map(book -> MiddleSchoolBook.builder().id(book.getId()).name(book.getName()).build())
                    .collect(Collectors.toList());
        }
    }
}
