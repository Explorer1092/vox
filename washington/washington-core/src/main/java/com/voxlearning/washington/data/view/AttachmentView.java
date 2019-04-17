package com.voxlearning.washington.data.view;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 附件信息
 *
 * @Author: peng.zhang
 * @Date: 2018/10/17
 */
@Data
public class AttachmentView {

    private String type;

    private String name;

    private String src;

    public static class Builder{
        public static List<AttachmentView> build(TeacherCourseware teacherCourseware){
            List<AttachmentView> attachmentViews = new ArrayList<>();
            if (StringUtils.isNotEmpty(teacherCourseware.getWordUrl())){
                AttachmentView view = new AttachmentView();
                String name = teacherCourseware.getWordName() == null ? "" : teacherCourseware.getWordName();
                String type = name.substring(name.lastIndexOf(".")+1,name.length());
                view.setType(type);
                view.setName(teacherCourseware.getWordName());
                view.setSrc(teacherCourseware.getWordUrl());
                attachmentViews.add(view);
            }
            if (StringUtils.isNotEmpty(teacherCourseware.getCoursewareFile())){
                AttachmentView view = new AttachmentView();
                String name = teacherCourseware.getCoursewareFileName() == null ? "" : teacherCourseware.getCoursewareFileName();
                String type = name.substring(name.lastIndexOf(".")+1,name.length());
                if (type.contains("ppt")){
                    view.setType(type);
                    view.setName(teacherCourseware.getCoursewareFileName());
                    view.setSrc(teacherCourseware.getCoursewareFile());
                    attachmentViews.add(view);
                }
            }
            if (StringUtils.isNotEmpty(teacherCourseware.getPptCoursewareFile())){
                AttachmentView view = new AttachmentView();
                String name = teacherCourseware.getPptCoursewareFileName() == null ? "" : teacherCourseware.getPptCoursewareFileName();
                String type = name.substring(name.lastIndexOf(".")+1,name.length());
                if (type.contains("ppt")){
                    view.setType(type);
                    view.setName(teacherCourseware.getPptCoursewareFileName());
                    view.setSrc(teacherCourseware.getPptCoursewareFile());
                    attachmentViews.add(view);
                }
            }
            // 详情里不展示图片了
//            if (CollectionUtils.isNotEmpty(teacherCourseware.getPicturePreview())){
//                for (Map<String,String> picture : teacherCourseware.getPicturePreview()){
//                    AttachmentView view = new AttachmentView();
//                    String name = picture.get("name");
//                    String type = name.substring(name.lastIndexOf(".")+1,name.length());
//                    view.setType(type);
//                    view.setName(picture.get("name"));
//                    view.setSrc(picture.get("url"));
//                    attachmentViews.add(view);
//                }
//            }
            return attachmentViews;
        }
    }

}
