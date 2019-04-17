package com.voxlearning.utopia.agent.utils;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 *
 * @author song.wang
 * @date 2018/8/22
 */
public class AgentSchoolLevelUtils {

    /**
     * 生成组合的schoolLevel值， 按照集合中值的大小排序后拼接在一起
     * @param schoolLevels list
     * @return 组合后的schoolLevel,  0 : 未知
     */
    public static Integer generateCompositeSchoolLevel(Collection<Integer> schoolLevels){
        if(CollectionUtils.isEmpty(schoolLevels)){
            return 0;
        }
        List<Integer> targetSchoolLevelList = schoolLevels.stream().filter(level -> level != null && SchoolLevel.safeParse(level, null) != null).collect(Collectors.toList());
        Collections.sort(targetSchoolLevelList);
        return SafeConverter.toInt(StringUtils.join(targetSchoolLevelList, ""));
    }

    public static List<Integer> fetchFromCompositeSchoolLevel(Integer compositeSchoolLevel){
        List<Integer> result = new ArrayList<>();
        if(compositeSchoolLevel != null){
            String str = String.valueOf(compositeSchoolLevel);
            for(int i = 0; i< str.length(); i++){
                Integer level = SafeConverter.toInt(str.charAt(i));
                if(SchoolLevel.safeParse(level, null) != null){
                    result.add(level);
                }
            }
        }
        return result;
    }

}
