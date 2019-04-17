<@sugar.capsule js=["swfupload", "swfupload.handlers", "swfupload.queue"] />
<script id="t:上传照片" type="text/html">
    <div class="jqi-orange-box">
        <div class="jqi-orange-">
            <div class="jqi-orange-close">×</div>
            <div class="lead jqi-orange-title ">
                <div class="text_center">上传照片</div>
            </div>
            <div class="jqi-orange-message ">
                <div class="uploadContentModule">
                    <div class="containerModule">
                        <div class="fieldsetListBox">
                        <#--图片显示区-->
                            <div class="fieldset flash" id="fsUploadProgress"></div>
                        <#--初始上传按钮区-->
                            <div class="clear"></div>
                            <span id="spanButtonPlaceholder">一天只能上传一次！</span>
                            <div class="after-box" id="uploadMessageBox" style="display:none;">
                                <div class="fade"></div>
                                <div class="after" >
                                    <p class="signed"><!--message--></p>
                                    <div class="text_center sign_pay">
                                        <a href="javascript:void(0);" class="btn_mark btn_mark_well btn_mark_orange submitBtn">知道了</a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                <#--操作按钮区-->
                    <div class="bottomModule">
                        <div class="defaultInfo">
                            单次最多上传4张图片
                        </div>
                        <div class="uploadInfo"  style="display: none;">
                            <p class="b_left">成功上传<span id="uploadPhotoTotal">0</span>张照片（还能上传<span id="uploadPhotoNumber">4</span>张照片）</p>
                            <p class="b_right">
                                <a href="javascript:void(0);" id="uploadAddButton" class="upload_button"><span id="spanButtonPlaceholderMore">＋继续添加</span></a>
                                <a href="javascript:void(0);" id="uploadCancelButton" class="upload_button upload_gray_button" style="display: none;">取消</a>
                                <a href="javascript:void(0);" id="uploadFinishedButton" class="upload_button upload_blue_button">完成上传</a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>
<script type="text/javascript">
    var swfLoad, swfLoadOne;
    var uploadImageHrefUrl = "<@app.avatar href='/'/>";

    $(function(){
        var popupUploadPhotoBox = $("#popupUploadPhotoBox");
        var popupUploadPhotoBoxClose = popupUploadPhotoBox.find(".jqi-orange-close");
        var uploadPhotoButton = $("#uploadPhotoButton");
        var uploadFinishedButton = $("#uploadFinishedButton");
        var uploadCancelButton = $("#uploadCancelButton");
        var progressWrapper = $(".progressWrapper");
        var progressDelete = $(".progressDelete");
        var uploadDayType = ${(uploaded?string)!false};

        //点击上传照片 - 打开弹出框
        uploadPhotoButton.on("click", function(){
            var $this = $(this);
            if($this.hasClass("isShow")){
                document.getElementById("popupUploadPhotoBox").innerHTML = "";
                $this.removeClass("isShow");
                return false;
            }else{
                $this.addClass("isShow");
            }

            popupUploadPhotoBox.html(template("t:上传照片", {}));

            $(document).on("click",function(e){
                if($(e.target).closest("#popupUploadPhotoBox").length == 0 && $(e.target).closest("#uploadPhotoButton").length == 0){
                    deleteMethod(imageAllUrl);
                }
            });

            if(uploadDayType){
                return false;
            }

            setTimeout(function(){
                var swfSettings = {
                    //Post 设置
                    upload_url: "/uploadfile/clazzjournalphoto.vpage",
                    file_post_name : "filedata",

                    //设置图片格式
                    file_size_limit : "3 MB",  // 1 MB
                    file_types : "*.jpeg;*.jpg;*.png;*.gif",
                    file_types_description : "JPG Images; PNG Image; GIF Image",
                    file_upload_limit : 100,
                    file_queue_limit : 4,

                    //调用方法
                    swfupload_preload_handler : preLoad,
                    swfupload_load_failed_handler : loadFailed,
                    file_queued_handler : fileQueued,
                    file_queue_error_handler : fileQueueError,
                    file_dialog_complete_handler : fileDialogComplete,
                    upload_start_handler : uploadStart,
                    upload_progress_handler : uploadProgress,
                    upload_error_handler : uploadError,
                    upload_success_handler : uploadSuccess,
                    upload_complete_handler : uploadComplete,

                    //按钮设置障碍
                    button_image_url : "<@app.link href="public/plugin/jquery-swfupload/swfupload/upload_photo_button.png"/>",
                    button_placeholder_id : "spanButtonPlaceholder",
                    button_width: 118,
                    button_height: 130,
                    button_text: '<span class="textBtn">添加照片</span>',
                    button_text_style : '.textBtn{ color:#016246; font-size: 14px; font-weight: bold; text-align:center; display: block; width: 117px; }',
                    button_text_top_padding: 90,
                    button_text_left_padding: 0,
                    button_window_mode: SWFUpload.WINDOW_MODE.TRANSPARENT,
                    button_cursor: SWFUpload.CURSOR.HAND,

                    // Flash 设置
                    flash_url : "<@app.link href="public/plugin/jquery-swfupload/swfupload.2.2.swf"/>",
                    flash9_url : "<@app.link href="public/plugin/jquery-swfupload/swfupload_fp9.swf"/>",

                    //图片大小设置
                    custom_settings : {
                        progressTarget : "fsUploadProgress",
                        cancelButtonId : "uploadCancelButton",
                        thumbnail_height: 600,
                        thumbnail_width: 600,
                        thumbnail_quality: 100
                    },

                    // Debug Settings
                    debug: false
                };

                var swfSettingsOne = {
                    //按钮设置障碍
                    button_image_url : "<@app.link href="public/skin/student/images/myclass/upload_gray_button.png"/>",
                    button_placeholder_id : "spanButtonPlaceholderMore",
                    button_width: 118,
                    button_height: 33,
                    button_text: '<span class="textBtn">+添加照片</span>',
                    button_text_style : '.textBtn{ color:#666666; font-size: 14px; font-weight: bold; text-align:center; display: block; width: 118px;background-color: #cccccc;}',
                    button_text_top_padding: 7,
                    button_text_left_padding: 3
                };

                swfLoad = new SWFUpload(swfSettings);

                //继承swfSettings
                swfLoadOne = new SWFUpload($.extend(swfSettings, swfSettingsOne));
            }, 100);
        });

        //完成上传
        uploadFinishedButton.live("click", function(){
            var $this = $(this);
            if($this.hasClass("frozen") || imageAllUrl.length < 1){
                //alert("正在上传中哦！");
                uploadMessage("正在上传中哦！")
                return false;
            }

            $this.addClass("frozen");
            $.post("/student/clazz/uploadphoto.vpage", {
                photos : imageAllUrl.join()
            }, function(data){
                if(data.success){
                    uploadDayType = true;
                    fileLimit = 0;
                    imageAllUrl = [];

                    //当前班级新鲜事所在标签
                    var target = $("[data-newtype].active").data('newtype');
                    $.get('/student/clazz/clazzlatestnews.vpage?type='+target.toUpperCase()+'&currentPage=1',function(data){
                        $("#"+target).html(template("t:班级动态", {
                            news : data.journalPage.content
                        }));
                    });
                    //在这里调用imageAllUrl已置空，所以不会进删除方法
                    deleteMethod(imageAllUrl);
                }else{
                    //提交失败
                    $this.removeClass("frozen");
                    uploadMessage("请关闭上传照片，重新打开上传。。。");
                }
            });
        });

        //取消上传
        uploadCancelButton.live("click", function(){
            deleteMethod(imageAllUrl);
        });

        //关闭框靠
        popupUploadPhotoBoxClose.live("click", function(){
            deleteMethod(imageAllUrl);
        });

        //删除照片;
        progressDelete.live("click", function(){
            var $this = $(this);
            var $fileName = $this.attr("data-id");

            if($this.hasClass("frozen")){
                uploadMessage("删除中...");
                return false;
            }

            $this.addClass("frozen");

            $("#uploadPhotoTotal").text(fileLimit-1);
            $("#uploadPhotoNumber").text(5 - fileLimit);

            deleteMethod($fileName, $this);
        });

        //照片经过事件
        progressWrapper.live("mouseenter", function(){
            $(this).find(".progressDelete").addClass("progressDelete_active");
        }).live("mouseleave", function(){
            $(this).find(".progressDelete").removeClass("progressDelete_active");
        });

        //删除方法与关闭上传照片窗口方法
        function deleteMethod($fileName, $this){
            $fileName = $this ? $fileName : $fileName.join();

            if(!$this){
                if(fileLimit > 0){
                    cancelQueue(swfLoad);
                    cancelQueue(swfLoadOne);
                    fileLimit = 0;
                }
                setTimeout(function(){
                    document.getElementById("popupUploadPhotoBox").innerHTML = "";
                    uploadPhotoButton.removeClass("isShow");
                }, 100);

                if($fileName.length == 0){
                    return false;
                }
            }

            //删除接口
            $.post("/uploadfile/deletephoto.vpage", {
                filename : $fileName
            }, function(data){
                if(data.success){
                    if($this){
                        imageAllUrl.splice($.inArray($fileName, imageAllUrl), 1);
                        $this.closest(".progressWrapper").remove();
                        fileLimit--;
                    }else{
                        imageAllUrl = [];
                    }
                    $("#uploadCancelButton").hide();
                    $("#uploadAddButton").css({ width : "auto", position: "static", overflow: "visible"});

                    if(fileLimit == 0){
                        $(".uploadContentModule .defaultInfo").show();
                        $(".uploadContentModule .uploadInfo").hide();
                    }
                }else{
                    alert(data.info);
                    if($this){
                        $this.addClass("frozen");
                    }
                }
            });
        }

        function uploadMessage(info){
            var uploadMessageBox = $("#uploadMessageBox");
            var signed = uploadMessageBox.find(".signed");
            var submitBtn = uploadMessageBox.find(".submitBtn");

            uploadMessageBox.show();

            signed.html(info);

            submitBtn.live("click", function(){
                uploadMessageBox.hide();
                signed.html("");
            });
        }
    });
</script>