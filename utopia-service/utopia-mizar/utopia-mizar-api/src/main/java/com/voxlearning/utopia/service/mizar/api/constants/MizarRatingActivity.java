package com.voxlearning.utopia.service.mizar.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by Summer Yang on 2016/9/6.
 * 评论收集活动
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MizarRatingActivity {
    Collect_1(100001),
    ADD_MIZAR_SHOP_LIKE(100002); // 常态机构点赞

    @Getter
    private final Integer id;
}
