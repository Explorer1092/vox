package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.crm.api.constants.crm.CrmClueType;
import com.voxlearning.utopia.service.crm.api.entities.crm.CrmTeacherClue;
import com.voxlearning.utopia.service.crm.consumer.loader.crm.CrmTeacherClueLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.crm.CrmTeacherClueServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import org.apache.http.client.utils.URIBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CrmTeacherClueService
 *
 * @author song.wang
 * @date 2016/8/6
 */
@Named
public class CrmTeacherClueService extends AbstractAdminService {

    @Inject private CrmTeacherClueLoaderClient crmTeacherClueLoaderClient;
    @Inject private CrmTeacherClueServiceClient crmTeacherClueServiceClient;
    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;

    public MapMessage addTeacherClue(String creatorName, CrmClueType clueType, Collection<Long> teacherIds){
        if(CollectionUtils.isEmpty(teacherIds)){
            return MapMessage.errorMessage("老师列表为空");
        }
        MapMessage message = MapMessage.successMessage();

        // 过滤出达到条件但未认证的老师列表
        List<Long> validTeacherIds = teacherIds.stream().map(crmSummaryLoaderClient::loadTeacherSummary).filter(p -> p.getAuthCond1Reached() && p.getAuthCond2Reached() && p.getAuthCond3Reached() && p.getAutoAuthPostPoned()).map(CrmTeacherSummary::getTeacherId).collect(Collectors.toList());
        List<Long> invalidTeacherIds = teacherIds.stream().filter(p -> !validTeacherIds.contains(p)).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(invalidTeacherIds)){
            List<String> invalidStatusTeacherMessages = invalidTeacherIds.stream().map(teacherLoaderClient::loadTeacherDetail).filter(p -> p != null && p.getProfile() != null).map(p -> p.getProfile().getRealname() + "(" + p.getId() + ")老师认证状态不符合条件").collect(Collectors.toList());
            message.put("invalidStatusTeacherMessages", invalidStatusTeacherMessages);
        }
        teacherIds = validTeacherIds;
        if(CollectionUtils.isEmpty(teacherIds)){
            message.setSuccess(false);
            return message;
        }

        // 过滤出最近 7 天没有创建过线索的老师
        Date endDate = DateUtils.getTodayEnd();
        Date startDate = DateUtils.addDays(endDate, -7);
        List<CrmTeacherClue> existTeacherClueList = crmTeacherClueLoaderClient.findByTeacherIds(teacherIds, clueType, startDate, endDate);
        if(CollectionUtils.isNotEmpty(existTeacherClueList)){
            message.add("existTeacherMessages", existTeacherClueList.stream().map(p -> p.getTeacherName() + "(" + p.getTeacherId() + ")老师已存在此线索").collect(Collectors.toList()));
            List<Long> existTeacherIdList = existTeacherClueList.stream().map(CrmTeacherClue::getTeacherId).collect(Collectors.toList());
            teacherIds = teacherIds.stream().filter(p -> !existTeacherIdList.contains(p)).collect(Collectors.toList());
        }

        if(CollectionUtils.isEmpty(teacherIds)){
            message.setSuccess(false);
            return message;
        }

        // 过滤出有对应市场专员的老师

        List<Long> noAgentTeacherList = new ArrayList<>();
        teacherIds = teacherIds.stream().filter(p -> {
            boolean result = false;
            try {
                URIBuilder builder = new URIBuilder(super.getMarketingUrl() + "/crm/hasbusinessdeveloper.vpage");
                URI uri = builder.build();
                HttpRequestExecutor executor = HttpRequestExecutor.defaultInstance();
                AlpsHttpResponse response = executor.post(uri)
                        .addParameter("teacherId", String.valueOf(p))
                        .execute();
                if (response == null || response.hasHttpClientException() || response.getStatusCode() != 200) {
                    result = false;
                }else {
                    MapMessage resultMap = JsonUtils.fromJson(response.getResponseString(), MapMessage.class);
                    if (resultMap.isSuccess()) {
                        result = true;
                    } else {
                        result = false;
                    }
                }
            } catch (Exception e) {
                result = false;
            }
            if(!result){
                noAgentTeacherList.add(p);
            }
            return result;
        }).collect(Collectors.toList());

        if(CollectionUtils.isNotEmpty(noAgentTeacherList)){
            List<String> noAgentTeacherMessages = noAgentTeacherList.stream().map(teacherLoaderClient::loadTeacherDetail).filter(p -> p != null && p.getProfile() != null).map(p -> p.getProfile().getRealname() + "(" + p.getId() + ")老师无对应市场专员").collect(Collectors.toList());
            message.put("noAgentTeacherMessages", noAgentTeacherMessages);
        }

        if(CollectionUtils.isEmpty(teacherIds)){
            message.setSuccess(false);
            return message;
        }


        List<CrmTeacherClue> teacherClueList = new ArrayList<>();
        CrmTeacherClue teacherClue;
        for(Long teacherId : teacherIds){
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
            if(teacherDetail == null){
                continue;
            }
            teacherClue = new CrmTeacherClue();
            teacherClue.setType(clueType);
            teacherClue.setSchoolId(teacherDetail.getTeacherSchoolId());
            teacherClue.setSchoolName(teacherDetail.getTeacherSchoolName());
            teacherClue.setTeacherId(teacherDetail.getId());
            teacherClue.setTeacherName(teacherDetail.getProfile() == null ? "" : teacherDetail.getProfile().getRealname());
            teacherClue.setSubject(teacherDetail.getSubject().name());
            teacherClue.setCreator(creatorName);
            teacherClueList.add(teacherClue);
        }
        if(CollectionUtils.isNotEmpty(teacherClueList)){
            crmTeacherClueServiceClient.inserts(teacherClueList);
        }
        return message;
    }

    public List<CrmTeacherClue> findByType(CrmClueType type, Date startDate, Date endDate){
        return crmTeacherClueLoaderClient.findByType(type, startDate, endDate);
    }

}
