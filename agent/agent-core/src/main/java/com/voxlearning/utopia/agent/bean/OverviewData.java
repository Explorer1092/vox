package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.agent.utils.MathUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OverviewData
 *
 * @author song.wang
 * @date 2017/4/14
 */
@Getter
@Setter
@UtopiaCacheRevision("20170509")
public class OverviewData implements Serializable {

    private static final long serialVersionUID = -2893532473983646462L;

    public static final String ID_TYPE_SCHOOL = "SCHOOL";
    public static final String ID_TYPE_GROUP = "GROUP";
    public static final String ID_TYPE_USER = "USER";
    public static final String ID_TYPE_REGION = "REGION";

    public static final String VIEW_TYPE_JUNIOR = "JUNIOR";
    public static final String VIEW_TYPE_MIDDLE = "MIDDLE";

    private Integer day;              //最新日期
    private Long id;                //数据记录的键
    private String idType;               //数据记录的类型 标记id 是部门ID ，学校ID, 用户ID
    private String name;               //数据记录的名称 部门名称， 学校名称， 用户名称等

    private int juniorStuScale;      //学生规模(小学)
    private int juniorRegStuNum;    // 注册学生数(小学)
    private int juniorAuthStuNum;   // 认证学生数(小学)
    private int juniorMonthRegStuNum;           //月新增注册学生数(小学)
    private int juniorMonthAuthStuNum;          //月新增认证学生数(小学)
    private int juniorDayRegStuNum;         //昨日新增注册学生数(小学)
    private int juniorDayAuthStuNum;        ///昨日新增认证学生数(小学)

    private int middleStuScale;      //学生规模(中学)
    private int middleRegStuNum;    // 注册学生数(中学)
    private int middleAuthStuNum;   // 认证学生数(中学)
    private int middleMonthRegStuNum;           //月新增注册学生数(中学)
    private int middleMonthAuthStuNum;          //月新增认证学生数(中学)
    private int middleDayRegStuNum;         //昨日新增注册学生数(中学)
    private int middleDayAuthStuNum;        ///昨日新增认证学生数(中学)

    public OverviewData(Integer day) {
        this.day = day;
    }

    public static String ck_id_type_day(Long id, String idType, Integer day) {
        return CacheKeyGenerator.generateCacheKey(OverviewData.class,
                new String[]{"id", "type", "day"},
                new Object[]{id, idType, day});
    }

    public OverviewData appendData(OverviewData data){
        if(data == null){
            return this;
        }

        // 小学数据
        this.juniorStuScale += data.getJuniorStuScale();
        this.juniorRegStuNum += data.getJuniorRegStuNum();
        this.juniorAuthStuNum += data.getJuniorAuthStuNum();
        this.juniorMonthRegStuNum += data.getJuniorMonthRegStuNum();
        this.juniorMonthAuthStuNum += data.getJuniorMonthAuthStuNum();
        this.juniorDayRegStuNum += data.getJuniorDayRegStuNum();
        this.juniorDayAuthStuNum += data.getJuniorDayAuthStuNum();

        // 中学数据
        this.middleStuScale += data.getMiddleStuScale();
        this.middleRegStuNum += data.getMiddleRegStuNum();
        this.middleAuthStuNum += data.getMiddleAuthStuNum();
        this.middleMonthRegStuNum += data.getMiddleMonthRegStuNum();
        this.middleMonthAuthStuNum += data.getMiddleMonthAuthStuNum();
        this.middleDayRegStuNum += data.getMiddleDayRegStuNum();
        this.middleDayAuthStuNum += data.getMiddleDayAuthStuNum();
        return this;
    }

    public List<OverviewViewData> generateViewDateList(){
        List<OverviewViewData> retList = new ArrayList<>();
        retList.add(generateViewData(VIEW_TYPE_JUNIOR));
        retList.add(generateViewData(VIEW_TYPE_MIDDLE));
        return retList;
    }

    public OverviewViewData generateViewData(String viewType) {
        OverviewViewData data = new OverviewViewData();
        data.setId(this.id);
        data.setIdType(this.idType);
        data.setName(this.name);
        data.setViewType(viewType);

        if (Objects.equals(viewType, OverviewData.VIEW_TYPE_JUNIOR)) {
            data.setViewName("小学");
            data.setStuScale(this.juniorStuScale);
            data.setRegStuNum(this.juniorRegStuNum);
            data.setAuthStuNum(this.juniorAuthStuNum);
            data.setMonthRegStuNum(this.juniorMonthRegStuNum);
            data.setMonthAuthStuNum(this.juniorMonthAuthStuNum);
            data.setDayRegStuNum(this.juniorDayRegStuNum);
            data.setDayAuthStuNum(this.juniorDayAuthStuNum);
            data.setAuthRate(MathUtils.doubleDivide(this.juniorAuthStuNum, this.juniorStuScale, 2, BigDecimal.ROUND_FLOOR));
        } else if (Objects.equals(viewType, OverviewData.VIEW_TYPE_MIDDLE)) {
            data.setViewName("中学");
            data.setStuScale(this.middleStuScale);
            data.setRegStuNum(this.middleRegStuNum);
            data.setAuthStuNum(this.middleAuthStuNum);
            data.setMonthRegStuNum(this.middleMonthRegStuNum);
            data.setMonthAuthStuNum(this.middleMonthAuthStuNum);
            data.setDayRegStuNum(this.middleDayRegStuNum);
            data.setDayAuthStuNum(this.middleDayAuthStuNum);
            data.setAuthRate(MathUtils.doubleDivide(this.middleAuthStuNum, this.middleStuScale, 2, BigDecimal.ROUND_FLOOR));
        }
        return data;
    }

}
