package com.voxlearning.utopia.agent.tag;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.webmvc.module.runtime.WebConstants;
import com.voxlearning.utopia.agent.interceptor.AgentHttpRequestContext;
import lombok.Setter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.List;

/**
 * @author song.wang
 * @date 2018/5/23
 */
@Setter
public class PageElementTag extends SimpleTagSupport {

    private String elementCode;

    @Override
    public void doTag() throws IOException, JspException {
        super.doTag();
        PageContext pageContext = (PageContext) getJspContext();
        AgentHttpRequestContext requestContext = (AgentHttpRequestContext) pageContext.getRequest().getAttribute(WebConstants.REQUEST_ATTRIBUTE_DEFAULT_CONTEXT);
        List<String> pageElementCodes = requestContext.getCurrentUser().getPageElementCodes();
        if (CollectionUtils.isNotEmpty(pageElementCodes) && pageElementCodes.contains(elementCode)) {
            getJspBody().invoke(pageContext.getOut());
        }
    }

}
