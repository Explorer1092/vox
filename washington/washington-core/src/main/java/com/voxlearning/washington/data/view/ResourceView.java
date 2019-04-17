package com.voxlearning.washington.data.view;

import com.voxlearning.utopia.service.campaign.api.entity.TeacherResourceRef;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.text.SimpleDateFormat;

/**
 * 资源视图
 *
 * @Author: peng.zhang
 * @Date: 2018/10/23
 */
@Data
public class ResourceView {

    private Long userId;
    private TeacherResourceRef.Type resourceType;
    private String resourceId;
    private String resourceName;
    private String url;
    private Boolean disabled;
    private String createTime;
    private Long id;
    private String updateTime;

    public static class Builder{
        public static ResourceView build(TeacherResourceRef resourceRefs){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            ResourceView resourceView = new ResourceView();
            BeanUtils.copyProperties(resourceRefs,resourceView);
            resourceView.setCreateTime(simpleDateFormat.format(resourceRefs.getCreateDatetime()));
            resourceView.setUpdateTime(simpleDateFormat.format(resourceRefs.getUpdateDatetime()));
            return resourceView;
        }
    }
}
