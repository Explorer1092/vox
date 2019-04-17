package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author shiwe.liao
 * @since 2016/4/15
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum  ParentAppJxtExtTabTypeToWeb {
    //url 一定要加http 不然客户端有问题
    //type =0 。返回前端直接返回type与ParentAppJxtExtTabTypeToNative区分开。
    //0 直接跳web.非0根据ParentAppJxtExtTabTypeToNative跳具体的原生
    EDU_NEWS(0,1,"教育资讯","教育部发布新改革方案","/public/skin/parentMobile/images/dynamic/zixun.png","",0,"",Boolean.FALSE),
    NEW_EXAM(0,2,"在线模考","最新考试时间3月28日","","",0,"",Boolean.FALSE),
    ASSESS(0,2,"作业习惯测评","5分钟了解孩子作业习惯，限时免费","/public/skin/parentMobile/images/dynamic/assess.png","http://www.17zyw.cn/EFzM73?module=m_ry1q2y17&op=o_3gI7z4lt",1,"",Boolean.TRUE),
    VH_HOMEWORK(0,2,"假期作业","完成假期作业，领取学豆奖励","/public/skin/parentMobile/images/dynamic/holiday.png","/parentMobile/homework/vhindex.vpage",0,"",Boolean.FALSE);

    private final int type;
    private final int rank;
    private final String tabName;
    private final String tabExtInfo;
    private final String tabIcon;
    private final String url;
    private final Integer urlType;//链接类型：0站内。1站外
    private final String version;
    private final Boolean online;

    public static List<ParentAppJxtExtTabTypeToWeb> onlineTypeList() {
        List<ParentAppJxtExtTabTypeToWeb> list = new ArrayList<>();
        for (ParentAppJxtExtTabTypeToWeb type : ParentAppJxtExtTabTypeToWeb.values()) {
            if (Boolean.TRUE == type.getOnline()) {
                list.add(type);
            }
        }
        Collections.sort(list, (o1, o2) -> o2.getRank() - o1.getRank());
        return list;
    }
}
