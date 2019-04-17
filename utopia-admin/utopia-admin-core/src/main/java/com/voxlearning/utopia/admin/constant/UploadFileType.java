package com.voxlearning.utopia.admin.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum UploadFileType {
    unsupported(""),
    image("gif,jpg,jpeg,png"),
    audio("mp3,wma,wav,mid"),
    video("mp4,webm,ogg"),
    doc("zip,ppt,pptx,rar,doc,docx,pdf"),
    file("htm,html,txt,zip,rar,gz,bz2");
    @Getter
    private final String exts;

    public List<String> getDotExts() {
        return Arrays.stream(exts.split(",")).map(e -> '.' + e).collect(Collectors.toList());
    }

    static Map<String, UploadFileType> stringUploadFileTypeMap = new HashMap<>();

    static {
        String[] exts = UploadFileType.image.exts.split(",");
        for (String ext : exts) {
            stringUploadFileTypeMap.put(ext, UploadFileType.image);
        }
        exts = UploadFileType.video.exts.split(",");
        for (String ext : exts) {
            stringUploadFileTypeMap.put(ext, UploadFileType.video);
        }
        exts = UploadFileType.file.exts.split(",");
        for (String ext : exts)
            stringUploadFileTypeMap.put(ext, UploadFileType.file);
        exts = UploadFileType.audio.exts.split(",");
        for (String ext : exts)
            stringUploadFileTypeMap.put(ext, UploadFileType.audio);
        exts = UploadFileType.doc.exts.split(",");
        for (String ext : exts)
            stringUploadFileTypeMap.put(ext, UploadFileType.doc);
    }

    public static UploadFileType of(String ext) {
        ext = ext.toLowerCase();
        if (stringUploadFileTypeMap.containsKey(ext))
            return stringUploadFileTypeMap.get(ext);
        else
            return UploadFileType.unsupported;
    }
}
