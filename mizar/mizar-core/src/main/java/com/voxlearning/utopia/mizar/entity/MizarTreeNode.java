package com.voxlearning.utopia.mizar.entity;

import lombok.Getter;
import lombok.Setter;
import org.apache.xpath.operations.Bool;

import java.io.Serializable;
import java.util.List;

/**
 * 用于多级联动的数据结构
 * Created by Yuechen.Wang on 2016/11/29.
 */
@Getter
@Setter
public class MizarTreeNode implements Serializable{
    private static final long serialVersionUID = 6364860453289646824L;
    private String name; // 显示名称
    private String code; // 编码
    private String title; // 用于自动生成FancyTree
    private String key;      // 用于自动生成FancyTree
    private Boolean selected; // 用于自动生成FancyTree
    private List<MizarTreeNode> children; // 下级结构
}
