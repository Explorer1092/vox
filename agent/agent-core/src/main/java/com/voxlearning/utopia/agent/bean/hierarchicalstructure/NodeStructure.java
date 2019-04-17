package com.voxlearning.utopia.agent.bean.hierarchicalstructure;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 层级结构的点
 * Created by yaguang.wang on 2016/10/21.
 */
@Getter
@Setter
@NoArgsConstructor
public class NodeStructure implements Serializable {
    private static final long serialVersionUID = -6490434544056744173L;

    private String id;
    private String pId;                 // pid =0 的为一次层
    private String name;
    private Integer tier;              // 层数
    private Boolean isSelected;        // 是否被选择
    private String type;               // 类型        必须与pType 联合使用
    private String pType;              // 父类的类型
    private List<NodeStructure> subNodes;

//    @Override
//    public String toString() {
//
//        String s = (StringUtils.isEmpty(this.name)? "" : this.name) + (this.tier == null ? "" : this.tier) +
//                (CollectionUtils.isEmpty(subNodes)? "" : subNodes);
//
//        return s;
//    }
}
