package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.core.util.MapUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

/**
 * @author Jia HuanYin
 * @since 2015/10/12
 */
@Getter
@Setter
public class WorkRecordSummary implements Serializable {
    private static final long serialVersionUID = -199454616286562561L;

    private Map<String, Long> summary;
    private Map<String, List<WorkRecordDetail>> detail;

    private transient Map<String, WorkRecordDetail> buffer;

    public WorkRecordSummary() {
        summary = new HashMap<>();
        detail = new LinkedHashMap<>();
        buffer = new HashMap<>();
    }

    public void increase(String group, String code, String name) {
        Long count = summary.get(group);
        summary.put(group, count == null ? 1 : ++count);

        List<WorkRecordDetail> details = detail.get(group);
        if (details == null) {
            details = new ArrayList<>();
            detail.put(group, details);
        }

        WorkRecordDetail iDetail = buffer.get(code);
        if (iDetail == null) {
            iDetail = new WorkRecordDetail(code, name, 1);
            details.add(iDetail);
            buffer.put(code, iDetail);
        } else {
            iDetail.setCount(iDetail.getCount() + 1);
        }
    }

    public void smartSort() {
        if (MapUtils.isNotEmpty(detail)) {
            for (List<WorkRecordDetail> details : detail.values()) {
                Collections.sort(details);
            }
        }
    }
}
