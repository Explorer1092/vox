<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='活动管理' page_num=19>
<style type="text/css">
    .radio input[type="radio"]{margin-left: 0}
    .upload_preview img{width: 80px;height: 120px;}
    #uploadForm div.uploader{display: none;}
    .upload_preview .upload_append_list{display: inline-block;margin-right: 5px;text-align: center;}
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i>添加/修改活动配置项</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <div class="form-horizontal">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">*活动形式</label>
                    <div class="controls">
                        <input type="radio" value="1" name="form" <#if extend?? && extend.form == 1>checked</#if> /><span>普通推广</span>
                        <input type="radio" value="2" name="form" <#if extend?? && extend.form == 2>checked</#if> /><span>链式推广</span>
                        <input type="radio" value="3" name="form" <#if extend?? && extend.form == 3>checked</#if> /><span>组团</span>
                        <input type="radio" value="4" name="form" <#if extend?? && extend.form == 4>checked</#if> /><span>礼品卡</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">*列表图片</label>
                    <div class="controls" id="iconUrlsWrap">
                        <input id="iconUrls" name="iconUrls" type="file" />
                        <#if extend?? && extend.iconUrls?? && extend.iconUrls?size gt 0>
                            <#list extend.iconUrls as list>
                            <img src="${list!}" class="upload_iconUrls" style="width: 100px;height:100px;display: block; "/>
                            </#list>
                        </#if>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">*前端展示链接</label>
                    <div class="controls">
                        <input id="linkUrl"
                               name="linkUrl"
                               class="js-postData input-xlarge focused js-needed"
                               type="text"
                               data-einfo="前端展示链接"
                               value="<#if extend?? && extend.linkUrl??>${extend.linkUrl!''}</#if>">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">*前端明细链接</label>
                    <div class="controls">
                        <input id="recordUrl"
                               name="recordUrl"
                               class="js-postData input-xlarge focused js-needed"
                               type="text"
                               data-einfo="前端明细链接"
                               value="<#if extend?? && extend.recordUrl??>${extend.recordUrl!''}</#if>">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">活动简介长链接</label>
                    <div class="controls">
                        <input id="introductionUrl"
                               name="introductionUrl"
                               class="js-postData input-xlarge focused js-needed"
                               type="text"
                               data-einfo="活动简介长链接"
                               value="<#if extend?? && extend.introductionUrl??>${extend.introductionUrl!''}</#if>">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">活动海报</label>
                    <div class="controls" id="posterUrlsWrap">
                        <input id="posterUrls" name="posterUrls" type="file" /><span style="color: #f00">点击图片可删除当前海报</span>
                        <div class="posterWrap">
                           <#if extend?? && extend.posterUrls?? && extend.posterUrls?size gt 0>
                            <#list extend.posterUrls as list>
                            <img src="${list!}" class="upload_posterUrls" style="width: 75px;height:134px;"/>
                            </#list>
                           </#if>
                        </div>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">二维码位置 x轴</label>
                    <div class="controls">
                        <input id="qrCodeX"
                               name="qrCodeX"
                               class="js-postData input-xlarge focused js-needed"
                               type="text"
                               data-einfo="二维码位置 x轴"
                               value="<#if extend?? && extend.qrCodeX??>${extend.qrCodeX!''}</#if>">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">二维码位置 y轴</label>
                    <div class="controls">
                        <input id="qrCodeY"
                               name="qrCodeY"
                               class="js-postData input-xlarge focused js-needed"
                               type="text"
                               data-einfo="二维码位置 y轴"
                               value="<#if extend?? && extend.qrCodeY??>${extend.qrCodeY!''}</#if>">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">推广文案</label>
                    <div class="controls">
                            <textarea id="slogan"
                                      name="slogan"
                                      style="width: 270px;resize:none"
                                      rows="5"
                                      class="postDate"><#if extend?? && extend.slogan??>${extend.slogan!}</#if></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">推广素材</label>
                    <div class="controls">
                        <input type="button" class="uploadImgBtn btn btn-primary" value="上传照片">
                        <form id="uploadForm">
                            <input id="fileImage" type="file" size="30" name="fileselect[]" multiple="" accept="image/jpeg,image/png,image/gif">
                            <div id="preview" class="upload_preview">
                                <#if extend?? && extend.materialUrls?? && extend.materialUrls?size gt 0>
                                    <#list extend.materialUrls as list>
                                    <div id="uploadList_${list_index}" class="resource_list" style="display: inline-block;">
                                        <p><img id="uploadImage_0" src="${list!''}" class="resource_image"><br>
                                            <a href="javascript:" class="resource_delete" title="删除" data-index="0">删除</a></p>
                                    </div>
                                    </#list>
                                </#if>
                            </div>
                            <div id="uploadInf" class="upload_inf"></div>
                            <input type="button" id="fileSubmit" class="upload_submit_btn btn btn-primary hidden" value="确认上传图片">
                        </form>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">需要参加的课程天数</label>
                    <div class="controls">
                        <input id="meetConditionDays"
                               name="meetConditionDays"
                               class="js-postData input-xlarge focused"
                               type="text"
                               value="<#if extend?? && extend.meetConditionDays??>${extend.meetConditionDays!''}</#if>">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">是否允许下多个订单</label>
                    <div class="controls">
                        <input type="radio" value="true" name="multipleOrderFlag" <#if extend?? && extend.multipleOrderFlag?? && extend.multipleOrderFlag>checked</#if> /><span>是</span>
                        <input type="radio" value="false" name="multipleOrderFlag" <#if extend?? && extend.multipleOrderFlag?? && !extend.multipleOrderFlag>checked</#if> /><span>否</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">是否赠送礼品</label>
                    <div class="controls">
                        <input type="radio" value="true" name="hasGift" <#if extend?? && extend.hasGift?? && extend.hasGift>checked</#if> /><span>是</span>
                        <input type="radio" value="false" name="hasGift" <#if extend?? && extend.hasGift?? && !extend.hasGift>checked</#if> /><span>否</span>
                    </div>
                </div>

                <div class="form-actions">
                    <button type="button" class="btn btn-primary submitBtn" data-info="0">取消</button>
                    <button type="button" class="btn btn-primary submitBtn" data-info="1">保存</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="${requestContext.webAppContextPath}/public/js/uploadImg/uploadFile.js?v=1"></script>
<script type="text/javascript">
$(function () {

    var postData = {};
    var activityId = getQuery('activityId');
    $('.submitBtn').on('click',function () {
        var info = $(this).data('info');
        if(info == 0){
            window.history.back();
        }else{

            var form = $('input[name="form"]:checked').val();
            var linkUrl = $('input[name="linkUrl"]').val().trim();
            var recordUrl = $('input[name="recordUrl"]').val().trim();
            var introductionUrl = $('input[name="introductionUrl"]').val().trim();
            var iconUrls = $('input[name="iconUrls"]').val().trim();
            var posterUrls = $('input[name="posterUrls"]').val().trim();
            var qrCodeX = $('input[name="qrCodeX"]').val().trim();
            var qrCodeY = $('input[name="qrCodeY"]').val().trim();
            if(!form){
                layer.alert('请选择活动形式');
                return;
            }
            if(!linkUrl){
                layer.alert('请填写前端展示链接');
                return;
            }
            if(!recordUrl){
                layer.alert('请填写前端明细链接');
                return;
            }
            // if(!introductionUrl){
            //     layer.alert('请填写活动简介长链接');
            //     return;
            // }
            // if(!qrCodeX){
            //     layer.alert('二维码x轴位置');
            //     return;
            // }
            // if(!qrCodeX){
            //     layer.alert('二维码y轴位置');
            //     return;
            // }
            if(!iconUrls && !$('.upload_iconUrls').attr('src')){
                layer.alert('请上传列表图片');
                return;
            }
            // if(!posterUrls && !$('.upload_posterUrls').attr('src')){
            //     layer.alert('请上传活动海报');
            //     return;
            // }


            //上传封面
            if($('.upload_iconUrls').attr('src') && $('.upload_iconUrls').attr('src').indexOf('https://') == -1){
                var formData = new FormData();
                var file = $('#iconUrls')[0].files[0];
                formData.append('file', file);
                formData.append('file_size', file.size);
                formData.append('file_type', file.type);
                $.ajax({
                    url: '/file/upload.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,  // 告诉jQuery不要去处理发送的数据
                    contentType : false,
                    success: function (res) {
                        if(res.success){
                            postData.iconUrls = res.fileUrl;
                            $('.upload_iconUrls').attr('src',res.fileUrl);
                        }
                    }
                });
            }else{
                postData.iconUrls = $('.upload_iconUrls').attr('src');
            }

            // //上传海报
            // if($('.upload_posterUrls').attr('src') && $('.upload_posterUrls').attr('src').indexOf('https://') == -1){
            //     var formData = new FormData();
            //     var file = $('#posterUrls')[0].files[0];
            //     formData.append('file', file);
            //     formData.append('file_size', file.size);
            //     formData.append('file_type', file.type);
            //     $.ajax({
            //         url: '/file/upload.vpage',
            //         type: 'POST',
            //         data: formData,
            //         processData: false,  // 告诉jQuery不要去处理发送的数据
            //         contentType : false,
            //         success: function (res) {
            //             if(res.success){
            //                 postData.posterUrls = res.fileUrl;
            //                 $('.upload_posterUrls').attr('src',res.fileUrl);
            //             }
            //         }
            //     });
            // }else{
            //     postData.posterUrls = $('.upload_posterUrls').attr('src');
            // }
            var upload_posterUrls = $('.upload_posterUrls');
            var imgArr = [];
            if(upload_posterUrls.length > 0){
                upload_posterUrls.each(function (i,item) {
                    imgArr.push($(item).attr('src'));
                });
            }
            postData.posterUrls = imgArr.toString();//活动海报

            postData.activityId = activityId;
            postData.form = form;
            postData.linkUrl = linkUrl;
            postData.recordUrl = recordUrl;
            postData.introductionUrl = introductionUrl;
            postData.qrCodeX = qrCodeX;
            postData.qrCodeY = qrCodeY;
            postData.slogan = $("#slogan").val().trim();
            postData.meetConditionDays = $('input[name="meetConditionDays"]').val().trim();
            postData.multipleOrderFlag = $('input[name="multipleOrderFlag"]:checked').val();
            postData.hasGift = $('input[name="hasGift"]:checked').val();
            if($('.upload_append_list').length > 0){
                //上传推广素材
                $('#fileSubmit').trigger('click');
            }else{
                var resource_list = $('.resource_list');
                var urls = [];
                resource_list.each(function (i,item) {
                    urls.push($(item).find('.resource_image').attr('src'));
                });
                postData.materialUrls = urls.toString();// 推广素材
                setTimeout(function () {
                    var index = layer.load(1, {
                        shade: [0.1,'#fff'] //0.1透明度的白色背景
                    });
                    $.post('update_extend.vpage',postData,function (res) {
                        layer.close(index);
                        if(res.success){
                            layer.alert('保存成功',function () {
                                window.history.back();
                            });
                        }else{
                            layer.alert(res.info);
                        }
                    });
                },2000);
            }
        }
    });

    //删除原来的素材
    $('.resource_delete').on('click',function () {
        $(this).parents('.resource_list').remove();
    });

    var iconUrls = undefined;
    //选择封面
    $("#iconUrls").on('change',function (e) {
        iconUrls = e.target.files;
        var img = $("#iconUrls").val();
        if (blankString(img)) {
            layer.alert("请选择封面！");
            $('#iconUrlsWrap img').remove();
            return;
        }
        var fileParts = img.split(".");
        var fileExt = fileParts.length < 2 ? null : fileParts[fileParts.length - 1].toLowerCase();
        if (fileExt !== "jpg" && fileExt !== "jpeg" && fileExt !== "png") {
            $("#iconUrls").val('');
            $('#iconUrlsWrap .filename').text('No file selected');
            layer.alert("请上传正确格式(jpg、jpeg、png)的图片！");
            return false;
        }

        var reader = new FileReader();
        reader.onload = function(e) {
            var data = e.target.result;
            var image = new Image();
            var width = 0;
            var height = 0;
            image.onload=function(){
                width = image.width;
                height = image.height;
                // if(width!=170||height!=170){
                //     layer.alert("请上传170*170的图片！");
                //     return false;
                // }else{
                    var img = '<img src="' + e.target.result + '" class="upload_iconUrls" style="width: 100px;height:100px;display: block; "/>';
                    $('#iconUrlsWrap img').remove();
                    $('#iconUrlsWrap').append(img);
                //}
            };
            image.src= data;

        };
        reader.readAsDataURL(e.target.files[0]);
    });

    var posterUrls = undefined;
    //选择海报
    $("#posterUrls").on('change',function (e) {
        posterUrls = e.target.files;
        var img = $("#posterUrls").val();
        if (blankString(img)) {
            layer.alert("请选择海报！");
            $('#posterUrlsWrap img').remove();
            return;
        }
        var fileParts = img.split(".");
        var fileExt = fileParts.length < 2 ? null : fileParts[fileParts.length - 1].toLowerCase();
        if (fileExt !== "jpg" && fileExt !== "jpeg" && fileExt !== "png") {
            $("#posterUrls").val('');
            $('#posterUrlsWrap .filename').text('No file selected');
            layer.alert("请上传正确格式(jpg、jpeg、png)的图片！");
            return false;
        }

        var formData = new FormData();
        var file = $('#posterUrls')[0].files[0];
        formData.append('file', file);
        formData.append('file_size', file.size);
        formData.append('file_type', file.type);
        $.ajax({
            url: '/file/upload.vpage',
            type: 'POST',
            data: formData,
            processData: false,  // 告诉jQuery不要去处理发送的数据
            contentType : false,
            success: function (res) {
                if(res.success){
                    // postData.posterUrls = res.fileUrl;
                    $('.posterWrap').append('<img src='+ res.fileUrl +' class="upload_posterUrls" style="width: 75px;height:134px;margin-right: 5px;"/>');
                }
            }
        });
        // var reader = new FileReader();
        // reader.onload = function(e) {
        //     var data = e.target.result;
        //     var image = new Image();
        //     var width = 0;
        //     var height = 0;
        //     image.onload=function(){
        //         width = image.width;
        //         height = image.height;
        //         // if(width!=750||height!=1334){
        //         //     layer.alert("请上传750*1334的图片！");
        //         //     return false;
        //         // }else{
        //             var img = '<img src="' + e.target.result + '" class="upload_posterUrls" style="width: 75px;height:134px;display: block; "/>';
        //             $('#posterUrlsWrap img').remove();
        //             $('#posterUrlsWrap').append(img);
        //         //}
        //     };
        //     image.src= data;
        //
        // };
        // reader.readAsDataURL(e.target.files[0]);
    });
    //删除海报
    $(document).on('click','.upload_posterUrls',function () {
        var $this = $(this);
        var index = layer.confirm('确认删除该海报？', {
            btn: ['确认','取消'] //按钮
        }, function(){
            $this.remove();
            layer.close(index);
        }, function(){
            layer.close(index);
        });

    });

    // 上传图片按钮
    $(document).on('click','.uploadImgBtn',function () {
        $('#fileImage').trigger('click');
    });
    setTimeout(function () {
        var params = {
            fileInput: $("#fileImage").get(0),
            upButton: $("#fileSubmit").get(0),
            url:  "/file/multiple_file_upload.vpage",
            onSelect: function(files) {
                if(files.length > 10){
                    layer.alert('最多选择十张！');
                    files.forEach(function(i,index){
                        if(index > ($('.upload_append_list').length - 1)){
                            UPLOADFILE.funDeleteFile(i);
                        }
                    });
                    return;
                }
                var html = '', i = 0;
                var funAppendImage = function() {
                    file = files[i];
                    if (file) {
                        var reader = new FileReader();
                        reader.onload = function(e) {
                            html = html + '<div id="uploadList_'+ i +'" class="upload_append_list"><p>'+
                                    '<img id="uploadImage_' + i + '" src="' + e.target.result + '" class="upload_image" /><br />' +
                                    '<a href="javascript:" class="upload_delete" title="删除" data-index="'+ i +'">删除</a></p>'+
                                    '</div>';

                            i++;
                            funAppendImage();
                        }
                        reader.readAsDataURL(file);
                    } else {
                        $("#preview").html(html);
                        if (html) {
                            //删除方法
                            $(".upload_delete").click(function() {
                                UPLOADFILE.funDeleteFile(files[parseInt($(this).attr("data-index"))]);
                                return false;
                            });
                        }
                    }
                };
                funAppendImage();
            },
            onDelete: function(file) {
                $("#uploadList_" + file.index).remove();
            },
            onSuccess: function(res) {
                postData.materialUrls = res.imageUrlList.toString();// 推广素材
                setTimeout(function () {
                    var index = layer.load(1, {
                        shade: [0.1,'#fff'] //0.1透明度的白色背景
                    });
                    $.post('update_extend.vpage',postData,function (res) {
                        layer.close(index);
                        if(res.success){
                            layer.alert('保存成功',function () {
                                window.history.back();
                            });
                        }else{
                            layer.alert(res.info);
                        }
                    });
                },2000);
            }
        };
        UPLOADFILE = $.extend(UPLOADFILE, params);
        UPLOADFILE.init();
    },30);

});
</script>
</@layout_default.page>
