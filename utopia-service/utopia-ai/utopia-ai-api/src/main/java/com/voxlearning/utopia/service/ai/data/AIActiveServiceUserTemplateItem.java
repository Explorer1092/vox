package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author guangqing
 * @since 2018/12/3
 */
@Getter
@Setter
public class AIActiveServiceUserTemplateItem implements Serializable{

    private String name;
    private String value;
    private String type;
    private int index;
    private boolean checkBox;

    public AIActiveServiceUserTemplateItem(String name, String value, String type, int index, boolean checkBox) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.index = index;
        this.checkBox = checkBox;
    }

    @Override
    public String toString() {
        return "AIActiveServiceUserTemplateItem{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", type='" + type + '\'' +
                ", index=" + index +
                ", checkBox=" + checkBox +
                '}';
    }
}
