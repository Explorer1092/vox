package com.voxlearning.utopia.admin.data;

import com.voxlearning.utopia.api.constant.CrmTaskRecordCategory;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jia HuanYin
 * @since 2015/11/12
 */
@Getter
@Setter
public class TaskRecordCounter implements Serializable, Comparable {
    private static final long serialVersionUID = 5694048240086001467L;

    public int count;
    public int base;
    public CrmTaskRecordCategory category;
    public List<TaskRecordCounter> children;

    public TaskRecordCounter(CrmTaskRecordCategory category) {
        this.category = category;
        this.children = new ArrayList<>();
    }

    public int increase() {
        return ++this.count;
    }

    public int moreBase() {
        return ++this.base;
    }

    public void addChild(TaskRecordCounter child) {
        this.children.add(child);
    }

    @Override
    public int compareTo(Object other) {
        if (other == null || !(other instanceof TaskRecordCounter)) {
            return -1;
        }
        TaskRecordCounter bean = (TaskRecordCounter) other;
        return bean.count - this.count;
    }
}
