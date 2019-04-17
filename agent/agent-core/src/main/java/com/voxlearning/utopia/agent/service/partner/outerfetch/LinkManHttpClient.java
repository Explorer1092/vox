package com.voxlearning.utopia.agent.service.partner.outerfetch;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.integration.ExamPaperClient;
import com.voxlearning.utopia.agent.mockexam.integration.StringUtil;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.service.partner.model.LinkMan;
import com.voxlearning.utopia.agent.service.partner.outerfetch.dto.LinkManHttpDto;
import org.springframework.http.HttpStatus;

import javax.inject.Named;
import java.util.List;

/**
 * @description: 联系人客户端
 * @author: kaibo.he
 * @create: 2019-04-02 19:00
 **/
@Named
public class LinkManHttpClient extends AbstractHttpClient{

    public MapMessage query(List<Long> userIds) {
        LinkManHttpDto.ResponseDto responseDto;
        LinkManHttpDto.ReqeustDto dto = LinkManHttpDto.ReqeustDto.Builder.build(userIds);
        POST post = build(Service.FANS_QUERY);
        post.addParameter(LinkManHttpDto.ReqeustDto.Builder.build(dto));

        AlpsHttpResponse response = post.execute();
        int status = response.getStatusCode();
        if (-1 == status) {
            return MapMessage.errorMessage("获取联系人列表失败:" + response.getHttpClientExceptionMessage());
        } else {
            HttpStatus httpStatus = HttpStatus.valueOf(status);
            switch (httpStatus) {
                case OK: {
                    String responseText = StringUtil.unicodeToString(response.getResponseString());
                    responseDto = JSON.parseObject(responseText, LinkManHttpDto.ResponseDto.class);
                    if (!responseDto.isSuccess()) {
                        return MapMessage.errorMessage("获取联系人列表失败!");                    }
                    break;
                }
                default: {
                    return MapMessage.errorMessage("获取联系人列表失败:" + response.getHttpClientExceptionMessage());                }
            }
        }
        return MapMessage.successMessage().add("data", LinkMan.Builder.build(responseDto));
    }
}
