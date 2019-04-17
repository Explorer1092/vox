package com.voxlearning.utopia.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public class AwardContext {

    /**
     * awardId : 46
     * num : 1
     * type : RC_PRODUCT 实物  HEAD_WEAR 头饰  INTEGRAL 学豆 "" 谢谢参与
     * disable : 1 禁用 0 启用
     * describe : 这里是奖品介绍
     * img : http://img.17zuoye.net/1.jpg
     * sort : 1
     */
    private Integer awardId;
    private Integer num;
    private Type type;
    private Integer disable;
    private String describe;
    private String img;
    private Integer sort;
    private Boolean big;
    private Boolean superBig;

    public enum Type {
        RC_PRODUCT, HEAD_WEAR, INTEGRAL, THANKS;

        public Type valueOfName(String string) {
            for (Type type : Type.values()) {
                if (Objects.equals(type.name(), string)) {
                    return type;
                }
            }
            return THANKS;
        }
    }

    public boolean isEnable() {
        return Objects.equals(disable, 0);
    }

}
