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

package com.voxlearning.washington.support.upload;

import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.alps.spi.storage.StorageSystem;
import lombok.Cleanup;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Named
public class VoiceUploader extends SpringContainerSupport {

    private static final String VOICE_CONTENT_TYPE = "audio/mpeg";

    @StorageClientLocation(system = StorageSystem.GFS, storage = "fs-default")
    private StorageClient fsDefault;

    public String uploadVoiceFromHttpServletRequest(HttpServletRequest request, String filename) {
        if (request == null || StringUtils.isBlank(filename)) {
            logger.warn("Upload voice: http servlet request is null or filename is blank");
            return null;
        }
        byte[] voice = parseByteArrayFromHttpServletRequest(request);
        if (ArrayUtils.isEmpty(voice)) {
            logger.warn("Upload voice: cannot parse voice from http servlet request");
            return null;
        }

        try {
            @Cleanup ByteArrayInputStream inStream = new ByteArrayInputStream(voice);
            StorageMetadata metadata = new StorageMetadata();
            metadata.setContentType(VOICE_CONTENT_TYPE);
            fsDefault.upload(inStream, filename, null, metadata);
            return filename;
        } catch (Exception ex) {
            logger.warn("Upload voice: failed when writing into mongo gfs", ex);
            return null;
        }
    }

    private byte[] parseByteArrayFromHttpServletRequest(HttpServletRequest request) {
        byte[] content;
        try {
            @Cleanup InputStream inStream = request.getInputStream();
            content = IOUtils.toByteArray(inStream);
        } catch (Exception ex) {
            content = new byte[0];
        }
        if (content.length != 0 && Base64.isBase64(content)) {
            content = Base64.decodeBase64(content);
        }
        return content;
    }
}
