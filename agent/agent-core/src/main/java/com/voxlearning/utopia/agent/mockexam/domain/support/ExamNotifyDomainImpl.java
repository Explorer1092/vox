package com.voxlearning.utopia.agent.mockexam.domain.support;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.mockexam.domain.ExamNotifyDomain;
import com.voxlearning.utopia.agent.mockexam.integration.StringUtil;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.email.api.EmailService;
import com.voxlearning.utopia.service.email.api.entities.PlainEmail;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知实现
 *
 * @author xiaolei.li
 * @version 2018/9/6
 */
@Service
public class ExamNotifyDomainImpl implements ExamNotifyDomain {

    @Resource
    List<Sender> senders;

    @Override
    public void send(Request request) {
        senders.parallelStream().forEach(i -> i.send(request));
    }

    /**
     * 发送接口
     */
    public interface Sender {

        /**
         * 发送
         *
         * @param request 请求
         */
        void send(Request request);
    }

    /**
     * email实现
     */
    @Slf4j
    @Service
    public static class SenderEmailImpl implements Sender {

        @ImportService(interfaceClass = EmailService.class)
        private EmailService emailService;

        @Inject
        private BaseOrgService orgService;

        @Override
        public void send(Request request) {
            try {
                if (request.getTypes().stream().anyMatch(i -> Request.Type.EMAIL == i)) {
                    List<String> emails = orgService.getUsers(request.getReceiver()).stream()
                            .map(AgentUser::getEmail)
                            .filter(StringUtils::isNotBlank)
                            .filter(p -> p.contains("@"))
                            .collect(Collectors.toList());
                    if(CollectionUtils.isEmpty(emails)){
                        return;
                    }
                    PlainEmail pe = new PlainEmail();
                    pe.setTo(String.join(";", emails));
                    pe.setSubject(request.getTitle());
                    pe.setBody(request.getMessage());
                    emailService.sendEmail(pe);
                    log.info("request = {}", request);
                }
            } catch (Exception e) {
                log.error("发送邮件通知失败,request = {}", request);
            }
        }
    }

    /**
     * 系统实现
     */
    @Slf4j
    @Service
    public static class SenderSystemImpl implements Sender {

        @Inject
        private AgentNotifyService agentNotifyService;

        @Override
        public void send(Request request) {
            try {
                if (request.getTypes().stream().anyMatch(i -> Request.Type.SYSTEM == i)) {
                    agentNotifyService.sendNotify(
                            AgentNotifyType.MOCK_EXAM_MESSAGE.getType(),
                            request.getTitle(),
                            request.getMessage(),
                            request.getReceiver()
                            , null);
                }
            } catch (Exception e) {
                log.error("发送系统通知失败,request = {}", request);
            }
        }
    }

    /**
     * 短信实现
     */
    @Slf4j
    @Service
    public static class SenderSMSImpl implements Sender {
        @Override
        public void send(Request request) {
            // do nothing
        }
    }

    /**
     * 短信实现
     */
    @Slf4j
    @Service
    public static class SenderPushImpl implements Sender {

        @Inject
        private AppMessageServiceClient appMessageServiceClient;

        @Override
        public void send(Request request) {
            try {
                if (request.getTypes().stream().anyMatch(i -> Request.Type.PUSH == i)) {
                    appMessageServiceClient.sendAppJpushMessageByIds(request.getMessage(), AppMessageSource.AGENT, request.getReceiver(), new HashMap<>());
                }
            } catch (Exception e) {
                log.error("发送Push消息失败,request = {}", request);
            }
        }
    }

}
