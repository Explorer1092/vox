<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/jquery-swfupload/swfupload.2.5.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/jquery-swfupload/handlers.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/jquery-swfupload/swfupload.queue.js"></script>

<div id="uploadphotoBox" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>上传图片</h3>
    </div>
    <div class="modal-body">
        <div id="content" class="startUploadBox">
            <form>
                <div>
                    <span id="spanButtonPlaceholder"></span>
                </div>
            </form>
            <div id="divFileProgressContainer"></div>
            <div id="thumbnails"></div>
        </div>

        <div class="endUploadBox"  style="display: none;">
            <p class="b_right">
                <span class="text-success">图片上传成功</span>
            </p>
        </div>
    </div>
    <div class="text-center alert-block">
        单次最多上传1张图片
    </div>
</div>


<script type="text/javascript">
    function uploadComplete(file, serverData) {
        try {
            console.info(this.getStats().files_queued);
            if (this.getStats().files_queued > 0) {
                this.startResizedUpload(this.getFile(0).ID, 300, 300, SWFUpload.RESIZE_ENCODING.JPEG, 100,false);
            } else {
                var progress = new FileProgress(file,  this.customSettings.upload_target);
                progress.setComplete();
                progress.setStatus("全部上传完毕.");
                progress.toggleCancel(false);


                setTimeout(function(){
                    $('#uploadphotoBox').modal('hide');
                    progress.setStatus("");
                },500);
            }
        } catch (ex) {
            this.debug(ex);
        }
    }

    var divClass;
    $(document).click(function(e){
        divClass=$(e.target).attr('class');
    });



    function myUploadSuccessHandler (file, serverData){

        //todo
        console.info("divClass:"+divClass);
        var imgUrl = 'http://cdn-cnc.17zuoye.cn/public/skin/studentv3/images/publicbanner/familyAlbumUSACard.png';
        var jsonObj = eval('(' + serverData + ')');
        console.info(jsonObj);
        console.info(pic_index);
        if(jsonObj.success){
//            $('.'+divClass).attr('src',jsonObj.url).attr('data-file_name',jsonObj.fileName).closest('.'+divClass).show();
            var img_html='<img src="'+jsonObj.url+'" data-file_name="'+jsonObj.fileName+'">';
            console.info(img_html);
            $("#addBox"+pic_index).html(img_html);
        }else{
//            $('.'+divClass).closest('.'+divClass).hide();
        }

        /*try {
            var progress = new FileProgress(file,  this.customSettings.upload_target);
            var jsonObj = eval('(' + serverData + ')');
            console.info(jsonObj);
        } catch (ex) {
            this.debug(ex);
        }*/

    }

    var swfu ,uploadImageHrefUrl;
    window.onload = function () {
        swfu = new SWFUpload({
            // Backend Settings
            upload_url: "edituploadimage.vpage",
            file_post_name : "imgFile",

            //设置图片格式
            file_size_limit : "3 MB",  // 3 MB
            file_types : "*.jpeg;*.jpg;*.png;*.gif",
            file_types_description : "All Files",
            file_upload_limit : 100,
            file_queue_limit : 2,

            //调用方法
            swfupload_preload_handler : preLoad,
            swfupload_load_failed_handler : loadFailed,
            file_queued_handler : fileQueued,
            file_queue_error_handler : fileQueueError,
            file_dialog_complete_handler : fileDialogComplete,

            upload_progress_handler : uploadProgress,
            upload_error_handler : uploadError,
            upload_success_handler : myUploadSuccessHandler, //   uploadSuccess
            upload_complete_handler : uploadComplete,


            //按钮设置障碍
            button_image_url : "../../public/js/jquery-swfupload/swfupload/upload_photo_button.png",
            button_placeholder_id : "spanButtonPlaceholder",
            button_width: 118,
            button_height: 130,
            button_text: '<span class="textBtn">添加照片</span>',
            button_text_style : '.textBtn{ color:#016246; font-size: 14px; font-weight: bold; text-align:center; display: block; width: 117px; }',
            button_text_top_padding: 90,
            button_text_left_padding: 0,
            button_window_mode: SWFUpload.WINDOW_MODE.TRANSPARENT,
            button_cursor: SWFUpload.CURSOR.HAND,

            // Flash Settings
            flash_url : "../../public/js/jquery-swfupload/swfupload.2.2.swf",
            flash9_url : "../../public/js/jquery-swfupload/swfupload_fp9.swf",

            custom_settings : {
                upload_target : "divFileProgressContainer"
            },

            // Debug Settings
            debug: false
        });
    };
</script>

