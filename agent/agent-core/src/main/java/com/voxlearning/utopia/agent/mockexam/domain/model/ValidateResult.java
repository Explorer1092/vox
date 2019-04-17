package com.voxlearning.utopia.agent.mockexam.domain.model;

import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 约束性校验结果
 *
 * @author xiaolei.li
 * @version 2018/8/18
 */
@Data
public class ValidateResult implements Serializable {

    /**
     * 是否正确
     */
    private boolean success;

    /**
     * 校验项
     */
    private List<Item> items;

    /**
     * 格式化输出结果
     *
     * @return 错误信息
     */
    public String format() {
        return StringUtils.join(
                items.stream().map(Item::getMessage).toArray(String[]::new),
                "/n");
    }

    /**
     * 校验项结果
     */
    @Data
    static class Item implements Serializable {
        private String propertyName;
        private Object propertyValue;
        private String message;
    }
}
