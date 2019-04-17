<!DOCTYPE html>
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <@sugar.capsule js=["jquery"] />
    <link href="http://bbs.0579.com.cn/css/default.css" rel="stylesheet" type="text/css" />
    <script type="text/javascript" src="/public/plugin/jquery-swfupload/swfupload.2.5.js"></script>
    <script type="text/javascript" src="/public/plugin/jquery-swfupload/handlers.js"></script>
    <script type="text/javascript">
        var swfu;

        window.onload = function () {
            swfu = new SWFUpload({
                //Post 设置
                upload_url: "/uploadfile/clazzjournalphoto.vpage",
                file_post_name : "filedata",

                //设置图片格式
                file_size_limit : "1 MB",  // 1 MB
                file_types : "*.jpeg;*.jpg;*.png;*.gif",
                file_types_description : "JPG Images; PNG Image; GIF Image",
                file_upload_limit : 9,
                file_queue_limit : 9,

                //调用方法
                swfupload_preload_handler : preLoad,
                swfupload_load_failed_handler : loadFailed,
                file_queued_handler : fileQueued,
                file_queue_error_handler : fileQueueError,
                //file_dialog_complete_handler : fileDialogComplete,
                upload_start_handler : uploadStart,
                upload_progress_handler : uploadProgress,
                upload_error_handler : uploadError,
                upload_success_handler : uploadSuccess,
                upload_complete_handler : uploadComplete,

                //按钮设置障碍
                button_image_url : "/public/plugin/jquery-swfupload/swfupload/upload_file_but.png",
                button_placeholder_id : "spanButtonPlaceholder",
                button_width: 142,
                button_height: 42,
                button_text: '<span class="textBtn">上传英语试卷</span>',
                button_text_style : '.textBtn{ color:#ffffff; font-size: 14px; font-weight: bold; text-align:center; display: block; width: 142px;}',
                button_text_top_padding: 12,
                button_text_left_padding: 1,
                button_window_mode: SWFUpload.WINDOW_MODE.TRANSPARENT,
                button_cursor: SWFUpload.CURSOR.HAND,

                // Flash 设置
                flash_url : "/public/plugin/jquery-swfupload/swfupload.2.2.swf",
                flash9_url : "/public/plugin/jquery-swfupload/swfupload_fp9.swf",

                //图片大小设置
                custom_settings : {
                    progressTarget : "fsUploadProgress",
                    cancelButtonId : "btnCancel",
                    thumbnail_height: 150,
                    thumbnail_width: 150,
                    thumbnail_quality: 100
                },

                // Debug Settings
                debug: false
            });
        };
    </script>

</head>
<body>
    <div id="content">
        <h2>Application Demo</h2>
        <p>This demo shows how SWFUpload can behave like an AJAX application.  Images are uploaded by SWFUpload and converted to thumbnails by the server.  Then some JavaScript is used to download and display the thumbnails without reloading the page.</p>
        <div class="fieldset flash" id="fsUploadProgress">
            <span class="legend">Upload Queue</span>
        </div>
        <form>
            <div>
                <span id="spanButtonPlaceholder"></span>
                <input type="button" value="Start Upload" onclick="swfu.startUpload();" style="margin-left: 2px; font-size: 8pt; height: 29px;" />
            </div>
        </form>

        <div id="thumbnails"></div>
    </div>
</body>
</html>
