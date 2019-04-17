package com.voxlearning.utopia.agent.bean.hierarchicalstructure;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * TreeNode
 *
 * @author song.wang
 * @date 2017/6/7
 */
@Getter
@Setter
public class TreeNode implements Serializable{
    protected String id;
    protected String pid;                 // pid =0 的为一次层
    protected String name;

    protected List<TreeNode> childList;

    public boolean isParent(TreeNode node){
        return StringUtils.equals(this.id, node.getPid());
    }

//    public String toString(){
//        return "{\"id\":\"" + (StringUtils.isBlank(this.id)? "" : this.id) + "\", \"pid\":\"" + (StringUtils.isBlank(this.pid)? "" : this.pid) + "\", \"name\":\"" + (StringUtils.isBlank(this.name)? "" : this.name) + "\"}";
//    }
}
