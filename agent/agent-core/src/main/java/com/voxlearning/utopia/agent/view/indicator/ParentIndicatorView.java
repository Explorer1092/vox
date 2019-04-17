package com.voxlearning.utopia.agent.view.indicator;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ParentIndicatorView
 *
 * @author deliang.che
 * @since  2019/3/17
 */
@Getter
@Setter
public class ParentIndicatorView implements Serializable {

    private static final long serialVersionUID = -1483942014497820819L;
    private Long id;
    private Integer idType;  // ID 类型， 标记ID 是部门ID, 用户ID 还是学校ID
    private Integer schoolLevel;      // 1 ：小学；2 ：初中；4 ：高中；24： 初高中
    private String name;    // 名称(部门名称、用户名、 学校名)

    private Integer viewType; // 业绩类型
    private String viewName;  // 业绩类型名称

    private	int	tmBindParentStuNum;	                //当月绑定家长的学生数
    private	int	lmBindParentStuNum;	                //上月绑定家长的学生数
    private	int	pdBindParentStuNum;	                //昨日绑定家长的学生数

    private	int	tmBindStuParentNum;	                //当月绑定学生的家长数
    private	int	lmBindStuParentNum;	                //上月绑定学生的家长数
    private	int	pdBindStuParentNum;	                //昨日绑定学生的家长数

    private	int	bindParentStuNum;	                //绑定家长的学生数
    private	int	bindStuParentNum;	                //累计注册家长
    private	int	regStuNum;	                        //注册学生
    private	Double	parentPermeateRate;	                //家长渗透率
    private	int	tmLoginGte1BindStuParentNum;        //本月登录1次及以上且绑定学生的家长数（MAU）
    private	int	pdLoginGte1BindStuParentNum;        //昨日登录1次及以上且绑定学生的家长数（MAU）

    private	int	tmLoginGte3BindStuParentNum;        //本月登录3次及以上且绑定学生的家长数
    private	int	pdLoginGte3BindStuParentNum;        //昨日登录3次及以上且绑定学生的家长数

    private	int	tmBackFlowParentNum;	        //当月回流活跃3次
    private	int	tmIncParentNum;	                //当月新增活跃3次
    private	int	pdIncParentNum;	                //昨日新增活跃3次

    private	int	tmParentStuActiveSettlementNum;	    //当月家长学生双活结算
    private	int	pdParentStuActiveSettlementNum;	    //昨日家长学生双活结算

    private	int	tmNewParentOldStuActiveSettlementNum;	//当月老学生新家长双活结算
    private	int	tmNewParentNewStuActiveSettlementNum;	//当月新学生新家长双活结算
    private	int	tmNewParentActiveSettlementNum;	    //当月新家长双活结算

    private double perCapitaNum;   //人均数

    public Map<String, Object> generateIndicatorDataMap() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", this.id);
        dataMap.put("idType", this.idType);
        dataMap.put("schoolLevel", this.schoolLevel);
        dataMap.put("name", this.name);
        dataMap.put("viewType", this.viewType);
        dataMap.put("viewName", this.viewName);

        Integer idType = SafeConverter.toInt(this.idType);

        if (viewType == ParentIndicatorViewSupport.PARENT_BIND_TM){
            dataMap.put("tmBindStuParentNum",this.tmBindStuParentNum);
            dataMap.put("tmBindParentStuNum",this.tmBindParentStuNum);
            if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
                dataMap.put("perCapitaNum",this.perCapitaNum);
            }
        }else if (viewType == ParentIndicatorViewSupport.PARENT_BIND_PD){
            dataMap.put("pdBindStuParentNum",this.pdBindStuParentNum);
            dataMap.put("pdBindParentStuNum",this.pdBindParentStuNum);
            if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
                dataMap.put("perCapitaNum",this.perCapitaNum);
            }
        }else if (viewType == ParentIndicatorViewSupport.PARENT_ACTIVE_TM){
            dataMap.put("tmLoginGte1BindStuParentNum",this.tmLoginGte1BindStuParentNum);
            dataMap.put("tmLoginGte3BindStuParentNum",this.tmLoginGte3BindStuParentNum);
            dataMap.put("tmIncParentNum",this.tmIncParentNum);
            dataMap.put("tmParentStuActiveSettlementNum",this.tmParentStuActiveSettlementNum);
            if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
                dataMap.put("perCapitaNum",this.perCapitaNum);
            }
        }else if (viewType == ParentIndicatorViewSupport.PARENT_ACTIVE_PD){
            dataMap.put("pdLoginGte1BindStuParentNum",this.pdLoginGte1BindStuParentNum);
            dataMap.put("pdLoginGte3BindStuParentNum",this.pdLoginGte3BindStuParentNum);
            dataMap.put("pdIncParentNum",this.pdIncParentNum);
            dataMap.put("pdParentStuActiveSettlementNum",this.pdParentStuActiveSettlementNum);
            if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
                dataMap.put("perCapitaNum",this.perCapitaNum);
            }
        }
        return dataMap;
    }

}
