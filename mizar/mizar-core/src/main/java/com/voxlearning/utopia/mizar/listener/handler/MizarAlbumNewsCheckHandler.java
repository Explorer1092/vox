package com.voxlearning.utopia.mizar.listener.handler;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.mizar.api.entity.notify.MizarNotify;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarNotifyServiceClient;
import com.voxlearning.utopia.service.vendor.api.CRMVendorService;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNews;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsAlbum;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author shiwei.liao
 * @since 2016-12-27
 */
@Named
public class MizarAlbumNewsCheckHandler extends SpringContainerSupport {
    @ImportService(interfaceClass = CRMVendorService.class)
    private CRMVendorService crmVendorService;
    @Inject
    private MizarNotifyServiceClient mizarNotifyServiceClient;
    @Inject
    private WorkFlowLoaderClient workFlowLoaderClient;


    public void handle(String mqmsg, Long recordId, String status) {
        if (StringUtils.isBlank(mqmsg) || recordId == null || StringUtils.isBlank(status)) {
            logger.warn("MizarAlbumNewsCheckHandler not handle error: mqmsg is {},recordId is {}", mqmsg, recordId);
            return;
        }

        //通过工作流的recordId拿到record的taskName,taskName存的是newsId
        String newsId = "";
        JxtNews jxtNews = null;
        String pushUser = "";
        String checkReason = "";
        Map<Long, WorkFlowRecord> workFlowRecordMap = workFlowLoaderClient.loadWorkFlowRecords(Collections.singleton(recordId));
        if (MapUtils.isNotEmpty(workFlowRecordMap)) {
            WorkFlowRecord workFlowRecord = workFlowRecordMap.get(recordId);
            if (workFlowRecord != null) {
                newsId = workFlowRecord.getTaskName();
                pushUser = workFlowRecord.getCreatorName();
                Map<Long, List<WorkFlowProcessHistory>> historyMap = workFlowLoaderClient.loadWorkFlowProcessHistoriesByWorkFlowId(Collections.singleton(workFlowRecord.getId()));
                if (MapUtils.isNotEmpty(historyMap)) {
                    List<WorkFlowProcessHistory> histories = historyMap.get(workFlowRecord.getId());
                    WorkFlowProcessHistory history = histories.stream().sorted((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime())).findFirst().orElse(null);
                    checkReason = history == null ? "" : history.getProcessNotes();
                }
            }
        }
        if (StringUtils.isNotBlank(newsId)) {
            jxtNews = crmVendorService.$loadJxtNews(newsId);
        }
        if (jxtNews != null) {
            if (StringUtils.equals(mqmsg, "agree_lv1")) {
                jxtNews.setOnline(Boolean.TRUE);
                jxtNews.setPushTime(new Date());
                jxtNews.setPushUser("mizar:" + pushUser);
                crmVendorService.$upsertJxtNews(jxtNews);
                generateAndSendMizarNotify(jxtNews, "agree", "");
            } else if (StringUtils.equals(mqmsg, "reject_lv1")) {
                jxtNews.setOnline(Boolean.FALSE);
                crmVendorService.$upsertJxtNews(jxtNews);
                generateAndSendMizarNotify(jxtNews, "reject", checkReason);
            }
        }

    }

    //这里应该需要添加一个向Mizar发通知的方法
    private MapMessage generateAndSendMizarNotify(JxtNews jxtNews, String status, String checkReason) {
        if (jxtNews != null && StringUtils.isNotBlank(jxtNews.getAlbumId())) {
            JxtNewsAlbum album = crmVendorService.$loadJxtNewsAlbum(jxtNews.getAlbumId());
            if (album != null) {
                MizarNotify mizarNotify = new MizarNotify();
                if (StringUtils.equals(status, "agree")) {
                    mizarNotify.setTitle("文章通过审核");
                    mizarNotify.setContent("您提交的《" + jxtNews.getTitle() + "》审核通过");
                } else if (StringUtils.equals(status, "reject")) {
                    mizarNotify.setTitle("文章被驳回");
                    mizarNotify.setContent("您提交的《" + jxtNews.getTitle() + "》审核被驳回,驳回原因：" + checkReason + "，请到文章管理编辑后重新提交");
                }
                mizarNotify.setType("ADMIN_NOTICE");
                return mizarNotifyServiceClient.sendNotify(mizarNotify, Collections.singleton(album.getMizarUserId()));
            }
        }
        return MapMessage.errorMessage();
    }
}
