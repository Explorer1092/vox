package com.voxlearning.utopia.agent.mockexam.service.dto.output;

import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 教材传输模型
 *
 * @Author: peng.zhang
 * @Date: 2018/8/14 14:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDto implements Serializable {

    private String id;

    private String name;

    public static class Builder {
        public static BookDto build(NewBookProfile bookProfile) {
            return new BookDto(bookProfile.getId(), bookProfile.getName());
        }
    }
}
