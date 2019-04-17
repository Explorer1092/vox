package com.voxlearning.utopia.service.reward.mapper;

import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * 公益活动 - 排行榜记录
 */
@Getter
@Setter
@EqualsAndHashCode(of={"schoolId","clazzId"})
public class PGRankEntry implements Serializable{

    private static final long serialVersionUID = 8104017624394863320L;
    private static NumberFormat nf = new DecimalFormat("#,###"); // 学豆的格式

    private Long schoolId;
    private Long clazzId;
    private Long rank;
    private String name;
    private Long money;
    private Long finishNum;
    private String formatMoney;

    public Long addMoney(Long value){
        if(value == null) value = 0L;
        if(this.money == null) this.money = 0L;

        this.money = this.money + value;
        return this.money;
    }

    public Long addFinishNum(Long num){
        if(this.finishNum == null)
            this.finishNum = 0L;

        this.finishNum += SafeConverter.toLong(num);
        return this.finishNum;
    }

    public String getFormatMoney(){
        return nf.format(this.money);
    }

}
