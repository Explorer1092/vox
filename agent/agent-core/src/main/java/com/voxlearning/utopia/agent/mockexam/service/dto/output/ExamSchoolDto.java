package com.voxlearning.utopia.agent.mockexam.service.dto.output;

import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.Data;

import java.io.Serializable;

/**
 * 学校传输对象
 *
 * @Author: peng.zhang
 * @Date: 2018/8/14 17:24
 */
@Data
public class ExamSchoolDto implements Serializable {

    /**
     * 学校ID
     */
    private Long id;

    /**
     * 学校名称
     */
    private String name;

    public static class Builder {
        public static ExamSchoolDto build(School school) {
            ExamSchoolDto dto = new ExamSchoolDto();
            dto.setId(school.getId());
            dto.setName(school.getCname());
            return dto;
        }
    }
}
