package com.voxlearning.washington.data.view;

import lombok.Data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Author: peng.zhang
 * @Date: 2018/10/17
 */
@Data
public class LabelView {

    private String labelName;

    private Integer labelNum;

    public static class Builder{
        public static List<LabelView> build(Map<String,Integer> labelInfo){
            List<LabelView> labelViewList = new ArrayList<>();
            Iterator<Map.Entry<String, Integer>> entries = labelInfo.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Integer> entry = entries.next();
                LabelView labelView = new LabelView();
                labelView.setLabelName(entry.getKey());
                labelView.setLabelNum(entry.getValue());
                labelViewList.add(labelView);
            }
            return labelViewList;
        }
    }
}
