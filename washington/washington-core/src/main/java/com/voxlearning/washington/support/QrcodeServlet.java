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

package com.voxlearning.washington.support;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;


/**
 * Simple barcode servlet.
 *
 * @version $Id: BarcodeServlet.java,v 1.8 2010/10/05 06:53:56 jmaerki Exp $
 */
public class QrcodeServlet extends HttpServlet {

    private static final long serialVersionUID = -1612711738060435089L;
    private static final int BLACK = -16777216;
    private static final int WHITE = -1;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // 二维码内容
            String content = req.getParameter("m");
            if (StringUtils.isBlank(content)) {
                resp.setContentType("text/plain;charset=UTF-8");
                resp.getOutputStream().write("二维码内容不能为空!".getBytes("utf-8"));
                resp.getOutputStream().close();
                return;
            }

            // 二维码大小,默认300*300
            int imgWidth = 300;
            int imgHeight = 300;

            String width = req.getParameter("w");
            String height = req.getParameter("h");
            if (!StringUtils.isBlank(width) && StringUtils.isNumeric(width)) {
                imgWidth = ConversionUtils.toInt(width);
            }
            if (!StringUtils.isBlank(height) && StringUtils.isNumeric(height)) {
                imgHeight = ConversionUtils.toInt(height);
            }

            BitMatrix byteMatrix;
            HashMap<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            byteMatrix = new MultiFormatWriter().encode(
                    new String(content.getBytes("UTF-8"), "ISO-8859-1"),
                    BarcodeFormat.QR_CODE,
                    imgWidth,
                    imgHeight,
                    hints);

            BufferedImage image = toBufferedImage(byteMatrix);
            resp.setContentType("image/png");
            ImageIO.write(image, "png", resp.getOutputStream());

        } catch (Exception e) {
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getOutputStream().write("生成二维码内容失败!".getBytes("utf-8"));
            resp.getOutputStream().close();
        }
    }

    private BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }

}
