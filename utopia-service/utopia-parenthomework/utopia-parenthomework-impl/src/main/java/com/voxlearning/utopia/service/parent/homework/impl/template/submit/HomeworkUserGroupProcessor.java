package com.voxlearning.utopia.service.parent.homework.impl.template.submit;

import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.mapper.StudentInfo;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

/**
 * 学生班组信息处理
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-15
 */
@Named
public class HomeworkUserGroupProcessor implements HomeworkProcessor {

    //Local variables
    @Inject private GroupLoaderClient groupLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;

    //Logic
    /**
     * 学生班组信息
     *
     * @param hc args
     * @return result
     */
    public void process(HomeworkContext hc) {
        HomeworkResult hr = hc.getHomeworkResult();
        HomeworkParam hp = hc.getHomeworkParam();
        //已完成发送消息
        if(ObjectUtils.get(()->hr.getFinished(), Boolean.FALSE)){
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(hp.getStudentId());
            //获取学生班组
            Long groupId = ObjectUtils.get(()->{
                Group group = groupLoaderClient.getGroupLoader().loadGroupsByClazzId(studentDetail.getClazzId()).getUninterruptibly().stream().filter(g -> g.getSubject() != null && Objects.equals(g.getSubject().name(), hr.getSubject())).findFirst().orElse(null);
                return group.getId();
            },null);
            hc.setGroupId(groupId);
            StudentInfo studentInfo = new StudentInfo();
            studentInfo.setClazzId(studentDetail.getClazzId());
            studentInfo.setStudentName(studentDetail.fetchRealname());
            hc.setStudentInfo(studentInfo);
        }
    }

}
