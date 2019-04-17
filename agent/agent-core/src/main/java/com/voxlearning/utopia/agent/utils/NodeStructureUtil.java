package com.voxlearning.utopia.agent.utils;

import com.couchbase.client.java.document.json.JsonObject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.LinkedMap;
import com.voxlearning.com.alibaba.dubbo.common.json.JSONObject;
import com.voxlearning.utopia.agent.bean.hierarchicalstructure.NodeStructure;
import com.voxlearning.utopia.agent.bean.hierarchicalstructure.TreeNode;
import com.voxlearning.utopia.service.wechat.api.constants.SourceType;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.http.converter.json.GsonFactoryBean;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 层级接点构造
 * Created by yaguang.wang on 2016/10/21.
 */
public class NodeStructureUtil {
    private static final Integer TIER = 6;  //超过6层感觉无意义，防止数据结构错误，导致的死递归

    public static MapMessage formatNode(List<NodeStructure> nodeStructureList) {
        return formatNode(nodeStructureList, null);
    }

    public static MapMessage formatNode(List<NodeStructure> nodeStructureList, String type) {
        List<NodeStructure> result = new ArrayList<>();
        MapMessage msg = MapMessage.successMessage();
        if (CollectionUtils.isEmpty(nodeStructureList)) {
            msg.add("nodeList", result);
            msg.add("tier", 0);
            return msg;
        }
        result = getNodeTree(nodeStructureList, "0", type, 1);
        List<Integer> tiers = nodeStructureList.stream().filter(p -> p.getTier() != null).map(NodeStructure::getTier).sorted((p1, p2) -> (p2 - p1)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tiers)) {
            msg.add("tier", 0);
        } else {
            msg.add("tier", tiers.get(0));
        }

        msg.add("nodeList", result);
        return msg;
    }

    private static List<NodeStructure> getNodeTree(List<NodeStructure> nodeStructureList, String pId, String type, Integer tier) {
        if (CollectionUtils.isEmpty(nodeStructureList)) {
            return Collections.emptyList();
        }
        if (tier >= TIER) {
            return Collections.emptyList();
        }

        List<NodeStructure> result = nodeStructureList.stream().filter(p -> Objects.equals(p.getPId() + p.getPType(), pId+type)).collect(Collectors.toList());
        result.forEach(p -> {
            p.setTier(tier);
            List<NodeStructure> clientNodes = getNodeTree(nodeStructureList, p.getId(), p.getType(), tier + 1);
            p.setSubNodes(clientNodes);
        });
        return result;
    }

//    public static Map<TreeNode, Map<TreeNode, List<TreeNode>>> generateCategory(List<? extends TreeNode> list, String pid){
//        if(CollectionUtils.isEmpty(list)){
//            return Collections.emptyMap();
//        }
//        Map<TreeNode, Map<TreeNode, List<TreeNode>>> treeMap = new LinkedMap<>();
//        list.stream().filter(p -> StringUtils.isBlank(p.getPid()) || StringUtils.equals(p.getPid(), pid)).forEach(p -> {
//            treeMap.put(p, new LinkedHashMap<>());
//            list.stream().filter(x -> p.isParent((TreeNode)x)).forEach(w -> treeMap.get(p).put(w, list.stream().filter(y -> w.isParent((TreeNode)y)).collect(Collectors.toList())));
//        });
//
//        return treeMap;
//    }


    public static List<TreeNode> generateNodeTree(List<TreeNode> list, String pid){
        if(CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        List<TreeNode> childList = list.stream().filter(p -> StringUtils.equals(p.getPid(), pid)).collect(Collectors.toList());
        childList.forEach(p -> p.setChildList(generateNodeTree(list, p.getId())));
        return childList;
    }

    public static void main(String[] args) {
        List<NodeStructure> result = new ArrayList<>();
        NodeStructure node = new NodeStructure();
        node.setId("1");
        node.setPId("2");
        NodeStructure node1 = new NodeStructure();
        node1.setId("2");
        node1.setPId("0");
        NodeStructure node2 = new NodeStructure();
        node2.setId("3");
        node2.setPId("0");
        NodeStructure node3 = new NodeStructure();
        node3.setId("5");
        node3.setPId("3");
        NodeStructure node4 = new NodeStructure();
        node4.setId("9");
        node4.setPId("6");
          /*NodeStructure node5 = new NodeStructure();
        node5.setId("1");
        node5.setPId("2");
        NodeStructure node6 = new NodeStructure();
        node6.setId("1");
        node6.setPId("2");
        NodeStructure node7 = new NodeStructure();
        node7.setId("1");
        node7.setPId("5");
        NodeStructure node8 = new NodeStructure();
        node8.setId("1");
        node8.setPId("2");
        NodeStructure node9 = new NodeStructure();
        node9.setId("1");
        node9.setPId("2");*/
        result.add(node);
        result.add(node1);
        result.add(node2);
        result.add(node3);
        /*  result.add(node4);
        result.add(node5);
        result.add(node6);
        result.add(node7);
        result.add(node8);
        result.add(node9);*/
//        System.out.println("---------------------------------");
//        System.out.println(formatNode(result).get("nodeList"));
//        System.out.println(formatNode(result).get("tier"));
//        System.out.println("---------------------------------");

        TreeNode t1 = new TreeNode();
        t1.setId("1");
        t1.setPid("0");

        TreeNode t2 = new TreeNode();
        t2.setId("2");
        t2.setPid("1");

        TreeNode t3 = new TreeNode();
        t3.setId("3");
        t3.setPid("1");

        TreeNode t4 = new TreeNode();
        t4.setId("4");
        t4.setPid("0");

        TreeNode t5 = new TreeNode();
        t5.setId("5");
        t5.setPid("4");

        TreeNode t6 = new TreeNode();
        t6.setId("6");
        t6.setPid("5");

        List<TreeNode> nodeList = new ArrayList<>();
        nodeList.add(t1);
        nodeList.add(t2);
        nodeList.add(t3);
        nodeList.add(t4);
        nodeList.add(t5);
        nodeList.add(t6);
        System.out.println(JsonUtils.toJson(t1));
//        Map<TreeNode, Map<TreeNode, List<TreeNode>>> map = generateCategory(nodeList, "0");
//        com.alibaba.fastjson.JSONObject.toJSON(map);
//        String s = JsonUtils.toJsonPretty(map);
//        System.out.println(s);
//        System.out.println(com.alibaba.fastjson.JSONObject.toJSON(map));


        List<TreeNode> treeNodes = generateNodeTree(nodeList, "0");
        System.out.println(JsonUtils.toJson(treeNodes));



    }
}
