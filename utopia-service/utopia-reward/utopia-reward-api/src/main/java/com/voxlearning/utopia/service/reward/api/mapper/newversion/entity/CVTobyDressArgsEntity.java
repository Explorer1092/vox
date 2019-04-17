package com.voxlearning.utopia.service.reward.api.mapper.newversion.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class CVTobyDressArgsEntity implements Serializable {
    private Long id;
    private Integer type;
    private Integer priceNumType;

    public enum CVType{
        DEFAULT(0),
        USE(1),
        CONVERT(2),
        ;
        private int type;
        CVType(int type) {
            this.type = type;
        }
        public int intValue() {
            return type;
        }
    }
}
