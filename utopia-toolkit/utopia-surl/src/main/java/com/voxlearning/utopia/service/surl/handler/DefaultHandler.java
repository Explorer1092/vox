package com.voxlearning.utopia.service.surl.handler;

import com.voxlearning.utopia.service.surl.module.monitor.HandlerCountManager;
import com.voxlearning.utopia.service.surl.module.monitor.HandlerType;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author xin.xin
 * @since 9/25/15
 */
public class DefaultHandler extends AbstractHandler {
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        HandlerCountManager.Companion.getInstance().increment(HandlerType.DEFAULT_HANDLER);

        //如果到这个handler了，那说明之前的handler都没处理，这里就返回错误信息了
        response.getWriter().write("无效的地址");
    }
}
