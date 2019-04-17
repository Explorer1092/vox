package com.voxlearning.utopia.admin.data;

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
public class TaskRecordReport implements Serializable {
    private static final long serialVersionUID = 4572098968884399037L;

    public int total;
    public List<TaskRecordCounter> detail;
    public List<TaskRecordCounter> summary;

    public TaskRecordReport(int total) {
        this.total = total;
        this.detail = new ArrayList<>();
        this.summary = new ArrayList<>();
    }
}
