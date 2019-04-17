package com.voxlearning.utopia.service.user.api.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 教学资源关联的任务
 * Created by haitian.gan on 2017/8/2.
 */
@Getter
@AllArgsConstructor
public enum TeachingResourceTask {

    @Deprecated
    NONE("无资源","所有老师、无资源",null),
    FREE("免费","免费",null),
    RECRUIT_NEW("15天内邀请一个老师注册并达成认证。被邀请老师注册时需填写您的手机或ID","15天内被邀请人认证",null),

    ASSIGN_WINTERVACATION("7天内至少为1个班布置寒假作业", "7天内至少为1个班布置寒假作业", null),
    ASSIGN_BASICREVIEW_TERMREVIEW("7天内至少为1个班布置期末复习", "7天内至少为1个班布置期末复习", null),

    PRO_SURVIVAL_1(
            "7天内至少为1个班布置并检查3次同科作业。检查时完成人数超过65%且不少于10人视为一次有效任务。",
            "7天1个班3次作业65%大于10名完成",
            "3,10,0.65"
    ),

    PRO_SURVIVAL_2(
            "7天内至少为1个班布置并检查3次同科作业。检查时完成人数超过70%且不少于10人视为一次有效任务。",
            "7天1个班3次作业70%大于10名完成",
            "3,10,0.7"
    ),

    PRO_SURVIVAL_3(
            "7天内至少为1个班布置并检查3次同科作业。检查时完成人数超过70%且不少于15人视为一次有效任务。",
            "7天1个班3次作业70%大于15名完成",
            "3,15,0.7"
    ),

    PRO_SURVIVAL_4(
            "7天内至少为1个班布置并检查3次同科作业。检查时完成人数超过77%且不少于15人视为一次有效任务。",
            "7天1个班3次作业77%大于15名完成",
            "3,15,0.77"
    ),


    PRO_SURVIVAL_5(
            "7天内至少为1个班布置并检查1次作业。检查时完成人数不少于10人视为一次有效任务。",
            "7天内1次作业大于10名完成",
            "1,10,0"
    ),
    @Deprecated
    PRO_SURVIVAL_6(
            "7天内至少为1个班布置并检查1次作业。检查时完成人数不少于10人视为一次有效任务。",
            "新注册未使用未认证，7天内1次作业大于10名完成",
            "1,10,0"
    ),
    @Deprecated
    PRO_SURVIVAL_7(
            "7天内至少为1个班布置并检查1次作业。检查时完成人数不少于10人视为一次有效任务。",
            "近1个月未使用未认证，7天内1次作业大于10名完成",
            "1,10,0"
    );

    private String desc;            // 这个是给老师看的书面描述
    private String configDesc;      // 这个是给配置的人看的
    private String conditionParam;  // 达标的参数

    public static TeachingResourceTask parse(String taskCode){
        try{
            return TeachingResourceTask.valueOf(taskCode);
        }catch(Exception e){
            return RECRUIT_NEW;
        }
    }
}
