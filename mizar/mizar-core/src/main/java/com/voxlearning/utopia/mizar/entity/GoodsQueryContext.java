package com.voxlearning.utopia.mizar.entity;

import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于品牌查询的参数
 * Created by Yuechen.Wang on 2016/11/29.
 */
@Getter
@Setter
public class GoodsQueryContext extends MizarQueryContext {

    private static final long serialVersionUID = -3240814192896410212L;

    private String token;                    // 课程ID或名称
    private String status;                   // 课程状态
    private String shopToken;                // 机构ID或名称
    private Map<String, MizarShop> shopMap;  // 课程关联机构（调用回传）

    public GoodsQueryContext(Pageable pageable) {
        super(pageable);
    }

    public Map<String, Object> toParamMap() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("token", token);
        paramMap.put("status", status);
        paramMap.put("shopToken", shopToken);
        return paramMap;
    }

}
