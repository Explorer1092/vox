package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

/**
 * @author guangqing
 * @since 2018/11/30
 */
@Getter
@Setter
public class AIActiveServiceTemplateItem {

    private String name;
    private String value;
    private String type;
    private int index;
    private boolean checkBox;

    public AIActiveServiceTemplateItem(String name, String value, String type, int index) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.index = index;
    }

    @Override
    public String toString() {
        return "AIActiveServiceTemplateItem{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", type='" + type + '\'' +
                ", index=" + index +
                '}';
    }
}
