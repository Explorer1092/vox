package com.voxlearning.utopia.mizar.entity;

import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
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
public class BrandQueryContext extends MizarQueryContext {

    private static final long serialVersionUID = -6805490021408819239L;

    private String brandName;          // 品牌名称

    public BrandQueryContext(Pageable pageable) {
        super(pageable);
    }

    public Map<String, Object> toParamMap() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("brandName", brandName);
        return paramMap;
    }
}
