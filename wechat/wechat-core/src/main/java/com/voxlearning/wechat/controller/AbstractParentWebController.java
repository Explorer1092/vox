package com.voxlearning.wechat.controller;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.*;

/**
 * Created by xinxin on 31/12/2015.
 */
public class AbstractParentWebController extends AbstractController {




    protected Map<String, Object> mapChildInfo(User child) {
        if (null == child) {
            return Collections.emptyMap();
        }

        Map<String, Object> info = new HashMap<>();
        info.put("id", child.getId());
        info.put("name", child.getProfile().getRealname());
        info.put("img", child.getProfile().getImgUrl());
        return info;
    }

    protected List<Map<String, Object>> mapChildInfos(List<User> children) {
        if (CollectionUtils.isEmpty(children)) return new ArrayList<>();
        List<Map<String, Object>> infos = new ArrayList<>();
        children.stream().forEach(t -> infos.add(mapChildInfo(t)));

        return infos;
    }

    protected String callNameAvailable(Long parentId) {
        List<StudentParentRef> refList = parentLoaderClient.loadParentStudentRefs(parentId);
        if(CollectionUtils.isNotEmpty(refList)){
            StudentParentRef ref = refList.stream().filter(r-> StringUtils.isBlank(r.getCallName())).findAny().orElse(null);
            if(ref != null){
                return "/parent/ucenter/selectparentwithstudentid.vpage?studentId=" + ref.getStudentId();
            }
        }
        return "";
    }

    @Override
    protected void log(Map<String, String> map) {
        map.put("app", WECHAT_LOG_APP_PARENT);
        map.put("source", WECHAT_LOG_SOURCE_PARENT);
        super.log(map);
    }
}
