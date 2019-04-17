/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.dubbo.proxy;

import com.alibaba.dubbo.rpc.service.GenericService;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.common.ICharset;
import com.voxlearning.alps.spi.monitor.FlightController;
import com.voxlearning.alps.spi.monitor.FlightLocation;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.monitor.StartPoint;
import com.voxlearning.com.alibaba.dubbo.config.ReferenceConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DubboProxyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(DubboProxyServerHandler.class);

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof FullHttpRequest) {
                FlightLocation location = FlightLocation.builder()
                        .type(DubboProxyServerHandler.class)
                        .method("handleHttpRequest")
                        .build();
                FlightController.startup(StartPoint.CTL, location);
                try {
                    handleHttpRequest(ctx, (FullHttpRequest) msg);
                } finally {
                    FlightController.shutdown();
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        long startTime = System.currentTimeMillis();

        FullHttpResponse response;
        byte[] returnContent;

        QueryStringDecoder decoder = new QueryStringDecoder(msg.uri());
        String serviceName = decoder.parameters().getOrDefault("service", Collections.emptyList())
                .stream().findFirst().orElse(null);
        String methodName = decoder.parameters().getOrDefault("method", Collections.emptyList())
                .stream().findFirst().orElse(null);
        String dubboGroup = decoder.parameters().getOrDefault("group", Collections.emptyList())
                .stream().findFirst().orElse(null);
        String dubboVersion = decoder.parameters().getOrDefault("version", Collections.emptyList())
                .stream().findFirst().orElse(null);

        String dubboGeneric = "fastjson";
        List<String> list = decoder.parameters().get("serialization");
        if (list != null && !list.isEmpty()) {
            //noinspection OptionalGetWithoutIsPresent
            dubboGeneric = list.stream().findFirst().get();
        }

        String info = "%s.%s[%s/%s/%s]";
        info = String.format(info, serviceName, methodName,
                dubboGroup, dubboVersion, dubboGeneric);
        FlightRecorder.dot(info);

        ReferenceConfig<GenericService> reference = ReferenceBuilder.build(
                dubboGroup, dubboGeneric, serviceName, dubboVersion);

        List<byte[]> args = new ArrayList<>();
        ByteBuf content = msg.content();
        while (true) {
            int n = content.bytesBefore((byte) 0x0A);    // search "\n"
            if (n >= 0) {
                ByteArrayOutputStream bs = new ByteArrayOutputStream(n);
                content.readBytes(bs, n);
                args.add(bs.toByteArray());
                content.skipBytes(1);   //skip "\n"
            } else {
                n = content.readableBytes();
                if (n > 0) {
                    ByteArrayOutputStream bs = new ByteArrayOutputStream(n);
                    content.readBytes(bs, n);
                    args.add(bs.toByteArray());
                }
                break;
            }
        }

        List<String> paramTypes = Collections.emptyList();
        if (decoder.parameters().containsKey("param_types")) {
            List<String> pl = decoder.parameters().getOrDefault("param_types", Collections.emptyList());
            if (pl != null && !pl.isEmpty()) {
                //noinspection OptionalGetWithoutIsPresent
                String j = pl.stream().findFirst().get();
                paramTypes = JsonUtils.fromJsonToList(j, String.class);
            }
        }

        try {
            GenericService genericService = reference.get();
            returnContent = (byte[]) genericService.$invoke(methodName,
                    paramTypes.toArray(new String[paramTypes.size()]),
                    args.toArray(new Object[args.size()]));

            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(returnContent));
        } catch (Exception ex) {
            FlightRecorder.dot(ex.getClass().getName());
            try (StringWriter w = new StringWriter();
                 PrintWriter p = new PrintWriter(w)) {
                ex.printStackTrace(p);
                returnContent = w.toString().getBytes(ICharset.DEFAULT_CHARSET);
            }
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY,
                    Unpooled.wrappedBuffer(returnContent));
        }

        long duration = System.currentTimeMillis() - startTime;
        response.headers().set("X-Duration", Long.toString(duration));

        if (!HttpHeaders.isKeepAlive(msg)) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, returnContent.length);
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            ctx.write(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("exceptionCaught", cause);
        ctx.close();
    }
}
