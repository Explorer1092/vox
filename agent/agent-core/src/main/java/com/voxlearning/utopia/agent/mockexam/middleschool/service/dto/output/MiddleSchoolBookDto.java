package com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.output;

import com.voxlearning.utopia.agent.mockexam.middleschool.domain.model.MiddleSchoolBook;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @description: 中学教材传输对象
 * @author: kaibo.he
 * @create: 2019-03-19 18:35
 **/
@Data
@Accessors(chain = true)
@Builder
public class MiddleSchoolBookDto implements Serializable{
    private String id;
    private String name;

    public static class Builder {
        public static MiddleSchoolBookDto build(MiddleSchoolBook book) {
            return MiddleSchoolBookDto.builder().id(book.getId()).name(book.getName()).build();
        }
    }
}
