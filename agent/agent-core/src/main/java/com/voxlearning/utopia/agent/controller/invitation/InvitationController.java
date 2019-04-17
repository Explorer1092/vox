package com.voxlearning.utopia.agent.controller.invitation;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentInvitation;
import com.voxlearning.utopia.agent.service.invitation.AgentInvitationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.inject.Inject;
import java.nio.charset.Charset;

/**
 * 邀请函处理类
 *
 * @author deliang.che
 * @date 2018-04-13
 */
@Controller
@RequestMapping("/invitation")
@Slf4j
public class InvitationController extends AbstractAgentController {
    @Inject
    private AgentInvitationService agentInvitationService;

    /**
     * 新增邀请函
     * @return
     */
    @RequestMapping("/add_invitation.vpage")
    @ResponseBody
    public MapMessage addInvitation() {
        String name = requestString("name");
        String tel = requestString("tel");
        String city = requestString("city");
        String company = requestString("company");
        String position = requestString("position");
        if (null != name && name.length() > 50){
            return MapMessage.errorMessage("姓名不能超过50个文字");
        }
        if (null != tel && tel.length() > 30){
            return MapMessage.errorMessage("电话不能超过30个文字");
        }
        if (null != city && city.length() > 50){
            return MapMessage.errorMessage("城市不能超过50个文字");
        }
        if (null != company && company.length() > 50){
            return MapMessage.errorMessage("公司不能超过50个文字");
        }
        if (null != position && position.length() > 50){
            return MapMessage.errorMessage("职位不能超过50个文字");
        }
        AgentInvitation agentInvitation = new AgentInvitation();
        agentInvitation.setName(name);
        agentInvitation.setTel(tel);
        agentInvitation.setCity(city);
        agentInvitation.setCompany(company);
        agentInvitation.setPosition(position);
        agentInvitationService.createInvitation(agentInvitation);
        return MapMessage.successMessage();
    }

    /**
     *  活动类的微信分享用
     */
    @RequestMapping(value = "/getpwxjsapiconfig.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getParentJsapiConfig() {
        String sourceUrl = getRequestString("url");
        if (StringUtils.isBlank(sourceUrl)) {
            return MapMessage.errorMessage("parameter url is required!");
        }

        String type = getRequestString("t");
        if (StringUtils.isBlank(type)) {
            type = "0";
        }

        String url = "http://wechat.17zuoye.com/others/getjsapiconfig.vpage";
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url)
                .contentCharset(Charset.forName("UTF-8"))
                .addParameter("url", sourceUrl)
                .addParameter("t", type)
                .execute();

        MapMessage result = JsonUtils.fromJson(response.getResponseString(), MapMessage.class);
        if (result == null) {
            return MapMessage.errorMessage("parameter url is required!");
        }

        return result;
    }
}
