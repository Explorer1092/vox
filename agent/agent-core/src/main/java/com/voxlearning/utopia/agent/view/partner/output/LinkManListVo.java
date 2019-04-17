package com.voxlearning.utopia.agent.view.partner.output;

import com.voxlearning.utopia.agent.service.partner.model.LinkMan;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Objects;
import java.util.Set;

/**
 * @description: 联系人列表视图
 * @author: kaibo.he
 * @create: 2019-04-02 21:38
 **/
@Data
public class LinkManListVo {
    private Long id;
    private String nickName;            //微信昵称
    private String bindTime;            //绑定时间
    private String mobile;              //手机号
    private String headPortrait;        //头像
    private Boolean currentLinkMan;

    public static class Builder {
        public static LinkManListVo build(Set<Long> ids, LinkMan linkMan) {
            LinkManListVo vo = new LinkManListVo();
            if (Objects.isNull(linkMan)) {
                return vo;
            }
            BeanUtils.copyProperties(linkMan, vo);
            if (ids.contains(linkMan.getId())) {
                vo.setCurrentLinkMan(Boolean.TRUE);
            } else {
                vo.setCurrentLinkMan(Boolean.FALSE);
            }
            return vo;
        }
    }
}
