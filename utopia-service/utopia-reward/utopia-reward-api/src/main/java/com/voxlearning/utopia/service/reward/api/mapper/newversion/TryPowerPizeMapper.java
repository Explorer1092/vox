package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class TryPowerPizeMapper implements Serializable{

    private Boolean isPize;
    private String tip;
    private List<PrizeEntity> prizeList;

    @Getter
    @Setter
    @ToString
    public class PrizeEntity implements Serializable {
        private String name;
        private Integer type;
        private String pictuerUrl;
        private Long fragmentNum;
    }

//    public enum PrizeType {
//        FRAGMENT(1),
//        REAL_GOODS(2);
//        private int type;
//        PrizeType(int type) {
//            this.type = type;
//        }
//        public int intValue (){
//            return type;
//        }
//    }
}
