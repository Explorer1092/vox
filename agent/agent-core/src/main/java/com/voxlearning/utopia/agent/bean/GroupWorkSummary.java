package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jia HuanYin
 * @since 2015/11/6
 */

@Getter
@Setter
public class GroupWorkSummary implements Serializable {
    private static final long serialVersionUID = -5538599498305496663L;

    private String code;
    private String name;
    private Map<String, Integer> summary;
    private List<AgentGroup> children;

    public GroupWorkSummary(String code, String name) {
        this.code = code;
        this.name = name;
        summary = new HashMap<>();
    }

    public GroupWorkSummary(String code, String name, List<AgentGroup> children) {
        this.code = code;
        this.name = name;
        this.children = children;
        summary = new HashMap<>();
    }

    public void increase(String group) {
        Integer count = summary.get(group);
        summary.put(group, count == null ? 1 : ++count);
    }
}
