package com.voxlearning.utopia.service.mizar.api.utils;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.mizar.api.constants.SpecialTopicPosition;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.SpecialTopic;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xiang.lv on 2016/10/18.
 *
 * @author xiang.lv
 * @date 2016/10/18   20:05
 */
final public class SpecialTopicUtil {
    /***
     *
     * @param count
     * @param position
     * @param specialTopicList     有效的
     * @return
     */
    public static  List<SpecialTopic> filterAndSort(int count,final SpecialTopicPosition position,List<SpecialTopic> specialTopicList) {
        if (CollectionUtils.isNotEmpty(specialTopicList)) {
            //过滤,排序
            specialTopicList = specialTopicList.stream().filter(o -> StringUtils.equalsIgnoreCase(position.name(),o.getPosition())).sorted(new Comparator<SpecialTopic>() {
                @Override
                public int compare(SpecialTopic o1, SpecialTopic o2) {
                    Integer orderIndex = SafeConverter.toInt(o1.getOrderIndex());
                    Integer orderIndexOther = SafeConverter.toInt(o2.getOrderIndex());
                    //大值在前面
                    return Integer.compare(orderIndexOther,orderIndex);
                }
            }).collect(Collectors.toList());
            if(specialTopicList.size() > count ){
                specialTopicList = specialTopicList.subList(0,count);
            }
            return specialTopicList;
        } else {
            return Collections.emptyList();
        }
    }
}
