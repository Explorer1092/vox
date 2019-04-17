package com.voxlearning.utopia.agent.mockexam.service.dto;

import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 树形节点
 *
 * @author xiaolei.li
 * @version 2018/8/14
 */
@Data
public class TreeNode<T extends Serializable> implements Serializable {

    /**
     * 根节点id
     */
    public static final String ROOT_ID = "-1";

    /**
     * 键
     */
    protected String id;

    /**
     * 名称
     */
    protected String name;

    /**
     * 深度，从0开始
     */
    protected int depth;

    /**
     * 是否为叶子节点
     */
    protected boolean leaf;

    /**
     * 数据
     */
    protected T data;

    /**
     * 孩子节点
     */
    protected List<TreeNode<T>> children;

    /**
     * 附加孩子节点
     *
     * @param node 节点
     * @return 当前节点
     */
    public TreeNode<T> appendChild(TreeNode<T> node) {
        if (null == children) {
            children = Lists.newArrayList();
        }
        children.add(node);
        leaf = false;
        return this;
    }

    public static class Builder {

        /**
         * 构建一个根节点
         *
         * @return 根节点
         */
        public static TreeNode buildRoot() {
            TreeNode root = new TreeNode();
            root.id = ROOT_ID;
            root.name = "根节点";
            root.depth = 0;
            return root;
        }
    }

    /**
     * 是否是根节点
     *
     * @param node 节点
     * @return boolean值
     */
    public static boolean isRoot(TreeNode node) {
        return null != node && ROOT_ID.equals(node.getId());
    }
}
