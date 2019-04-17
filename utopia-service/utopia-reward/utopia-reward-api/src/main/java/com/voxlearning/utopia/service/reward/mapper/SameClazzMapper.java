package com.voxlearning.utopia.service.reward.mapper;

import com.voxlearning.utopia.service.reward.api.mapper.CacheCollectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 同班教室 Mapper
 */
@Getter
@Setter
public class SameClazzMapper implements Serializable {
    private static final long serialVersionUID = -3784757236490499754L;

    private Long userId;
    private String type; // 学生 或 老师
    private List<ClazzMapper> clazzs;

    @Getter
    @Setter
    public static class ClazzMapper implements Serializable {
        private static final long serialVersionUID = -3784757236490499754L;

        private String clazzName;
        private Collection<CacheCollectMapper> collects;
    }
}
