package com.voxlearning.utopia.service.reward.constant;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * @description: 对应库中高级特权商品和特级特权商品的id...
 * @author: kaibo.he
 * @create: 2018-11-07 20:07
 **/
public enum TagIdLogicRelation {
    TEACHER_PRIVILEGE_SENIOR(Long.valueOf(3), "高级"),
    TEACHER_PRIVILEGE_SUPER(Long.valueOf(4), "特级"),

    PUBLIC_GOOD(Long.valueOf(50), "公益"),
            ;
    @Getter
    private Long number;
    @Getter
    private String name;
    TagIdLogicRelation(Long number, String name) {
        this.name = name;
        this.number = number;
    }

    public Long getNumber() {
        return number;
    }

    public static String getNameByNumbers(Collection<Long> numbers){
        for(TagIdLogicRelation tagId : TagIdLogicRelation.values()){
            if(numbers.contains(tagId.getNumber())){
                return tagId.getName();
            }
        }
        return StringUtils.EMPTY;
    }
}
