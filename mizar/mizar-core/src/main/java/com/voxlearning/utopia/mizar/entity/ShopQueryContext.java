package com.voxlearning.utopia.mizar.entity;

import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于机构查询的参数
 * Created by Yuechen.Wang on 2016/11/29.
 */
@Getter
@Setter
public class ShopQueryContext extends MizarQueryContext {

    private static final long serialVersionUID = -843023668469579512L;

    private String shopName;           // 机构名称
    private Boolean cooperator;        // 是否合作机构
    private Boolean vip;               // 是否VIP

    public ShopQueryContext(Pageable pageable) {
        super(pageable);
    }

    public Map<String, Object> toParamMap() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("vip", vip);
        paramMap.put("cooperator", cooperator);
        paramMap.put("shopName", shopName);
        return paramMap;
    }
}
