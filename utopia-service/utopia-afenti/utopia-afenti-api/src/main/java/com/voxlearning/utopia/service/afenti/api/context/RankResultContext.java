package com.voxlearning.utopia.service.afenti.api.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author peng.zhang.a
 * @since 16-8-18
 */
@Getter
@Setter
@RequiredArgsConstructor
public class RankResultContext {

    List<Map<String, Object>> rankList;
    Map<Long, Integer> userRankFlag;

}
