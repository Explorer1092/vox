package com.voxlearning.utopia.agent.service.partner.model;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.agent.service.partner.outerfetch.dto.LinkManHttpDto;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @description: 联系人模型
 * @author: kaibo.he
 * @create: 2019-04-02 21:20
 **/
@Data
public class LinkMan {
    private Long id;
    private String nickName;
    private String bindTime;
    private String mobile;
    private String headPortrait;
    private Integer orderNum;

    public static class Builder {
        public static List<LinkMan> build(LinkManHttpDto.ResponseDto dto) {
            List<LinkMan> linkMans = new ArrayList<>();
            if (Objects.isNull(dto) || MapUtils.isEmpty(dto.fetchDataMap())) {
                return linkMans;
            }

            dto.fetchDataMap().forEach((k, v) -> {
                if (CollectionUtils.isNotEmpty(v)) {
                    v.forEach(item -> {
                        LinkMan linkMan = new LinkMan();
                        BeanUtils.copyProperties(item, linkMan);
                        linkMans.add(linkMan);
                    });
                }
            });
            return linkMans;
        }
    }
}
