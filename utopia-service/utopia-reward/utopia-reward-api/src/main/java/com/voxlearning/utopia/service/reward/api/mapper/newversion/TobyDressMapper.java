package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class TobyDressMapper implements Serializable {

    private List<TobyDress> tobyDressList;

    @Getter
    @Setter
    @ToString
    public class TobyDress implements Serializable{
        private Long id;
        private String url;
        private String name;
        private Integer type;
    }

    public enum TobyDressType {
        image(1), countenance(2), props(3), accessory(4),
        ;
        private int type;
        TobyDressType(int type) {
            this.type = type;
        }

        public int intValue () {
            return type;
        }
    }
}
