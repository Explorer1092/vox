package com.voxlearning.utopia.agent.view.partner.output;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.service.partner.model.Partner;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * @description: 合作伙伴列表vo
 * @author: kaibo.he
 * @create: 2019-04-02 15:25
 **/
@Data
public class PartnerListVo {
    private Long id;                    //id
    private String name;                // 名称
    private String regionName;          // 区域名称
    private String type;                //机构类别
    private Boolean hasLinkMan;         //关联联系人标志
    private String createUserName;      //创建人

    public static class Builder {
        public static PartnerListVo build(Partner partner) {
            PartnerListVo vo = new PartnerListVo();
            BeanUtils.copyProperties(partner, vo);
            if (CollectionUtils.isEmpty(partner.getLinkMans())) {
                vo.setHasLinkMan(Boolean.FALSE);
            } else {
                vo.setHasLinkMan(Boolean.TRUE);
            }
            vo.setRegionName(partner.getAddress());
            return vo;
        }
    }
}
