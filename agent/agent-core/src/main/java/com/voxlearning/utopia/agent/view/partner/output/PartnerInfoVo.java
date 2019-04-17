package com.voxlearning.utopia.agent.view.partner.output;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.agent.service.partner.model.LinkMan;
import com.voxlearning.utopia.agent.service.partner.model.Partner;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;

/**
 * @description: 合作伙伴详情视图
 * @author: kaibo.he
 * @create: 2019-04-02 16:10
 **/
@Data
public class PartnerInfoVo {
    private String homePhotoUrl;        //门头照片url
    private String name;                //机构名称
    private String regionName;          //地址
    private BaseInfo baseInfo;          //基本信息
    private LinkManInfo linkManInfo;    //联系人信息
    @Data
    @lombok.Builder
    public static class BaseInfo {
        private Long id;                //机构id
        private String type;            //机构类型
        private String createUserName;  //机构创建人
        private String createTime;      //创建时间

    }

    @Data
    @lombok.Builder
    public static class LinkManInfo {
        private String wxNickName;      //微信昵称
        private Integer orderNum;       //累计订单数
        private String phoneNum;        //手机号码
        private String bindTime;        //绑定时间
        private Long linkManId;         //联系人id
        private String headPortrait;        //头像
    }

    public static class Builder {
        public static PartnerInfoVo build(Partner partner, LinkMan linkMan) {
            PartnerInfoVo vo = new PartnerInfoVo();
            if (Objects.isNull(partner)) {
                return vo;
            }
            vo.setHomePhotoUrl(partner.getHomePhotoUrl());
            vo.setName(partner.getName());
            vo.setRegionName(partner.getAddress());

            BaseInfo baseInfo = BaseInfo.builder()
                    .id(partner.getId())
                    .type(partner.getType())
                    .createUserName(partner.getCreateUserName())
                    .createTime(partner.getCreateTime())
                    .build();
            vo.setBaseInfo(baseInfo);

            if (Objects.nonNull(linkMan)) {
                LinkManInfo linkManInfo = LinkManInfo.builder()
                        .bindTime(linkMan.getBindTime())
                        .linkManId(linkMan.getId())
                        .phoneNum(linkMan.getMobile())
                        .wxNickName(linkMan.getNickName())
                        .headPortrait(linkMan.getHeadPortrait())
                        .build();
                vo.setLinkManInfo(linkManInfo);
            }

            return vo;
        }
    }
}
