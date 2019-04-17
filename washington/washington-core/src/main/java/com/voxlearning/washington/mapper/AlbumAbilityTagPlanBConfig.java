package com.voxlearning.washington.mapper;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 能力标签兜底方案
 * Created by jiang wei on 2017/5/17.
 */
@Data
public class AlbumAbilityTagPlanBConfig {
    private Map<Long, String> tagMap;
    private Integer subjectId;
    private List<Integer> clazzLevels;
    private List<String> albumIds;
}
