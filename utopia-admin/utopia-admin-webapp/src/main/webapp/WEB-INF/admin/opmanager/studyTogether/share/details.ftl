<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<style>
    .layer {
        background:#020516;
        width:100%;
        height:100%;
        opacity:0.4;
        filter:alpha(opacity=40);
        position:fixed;
        left:0;
        top:0; z-index:1000;
        display:none;
    }
    .loading{
        width:38px;
        height:38px;
        background:url(/public/img/loading.gif) no-repeat;
        position:fixed;
        left:50%;
        top:50%;
        margin-left:-16px;
        margin-top:-16px;
        z-index:4000;
        display:none;
    }
</style>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        <span style="color: #00a0e9">分享管理/</span>添加|编辑分享
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存分享"/>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="rewardForm" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <input id="shareId_hi" name="shareId" value="${shareId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">

                        <#if noticeId?has_content>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享ID</label>
                            <div class="controls">
                                <input type="text" id="shareId" name="shareId" class="form-control" value="${shareId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        </#if>

                        <#-- 课程ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="skuId" name="skuId" <#if content?? && content.skuId??>disabled</#if> class="form-control js-postData" value="${content.skuId!''}" style="width: 336px"/>
                                <span id="skuName"></span>
                            </div>
                        </div>

                        <#-- 分享类型 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="type" name="type" style="width: 350px;" class="form-control js-postData" <#if content?? && content.type??>disabled</#if>>
                                    <option value="diploma" <#if content?? && content.type?? && content.type == 'diploma'>selected</#if>>毕业证书</option>
                                    <option value="default" <#if content?? && content.type?? && content.type == 'default'>selected</#if>>电子书</option>
                                    <option value="rank" <#if content?? && content.type?? && content.type == 'rank'>selected</#if>>排行榜</option>
                                    <option value="report" <#if content?? && content.type?? && content.type == 'report'>selected</#if>>课程报告</option>
                                </select>
                            </div>
                        </div>

                        <#-- 分享图片 -->
                        <div id="ebook_id" class="control-group" hidden>
                            <label class="col-sm-2 control-label">分享图片 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="ebookPicUrl" name="ebookPicUrl" class="form-control js-postData input" value="${content.ebookPicUrl!''}" style="width: 336px"/>
                                <input class="upload_file" type="file" data-suffix="jpg#png#jpeg">
                                <a class="btn btn-success preview" data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.ebookPicUrl!''}"</#if>">预览</a>
                            </div>
                        </div>

                        <#-- 分享标题 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享标题 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="title" name="title" class="form-control js-postData" value='${content.title!""}' placeholder='纯文本' style="width: 336px"/>
                                <span style="color: red">毕业证类型标题填写格式：一起学古诗训练营:[real_name]顺利毕业,荣获"[title]"称号</span>
                            </div>
                        </div>

                        <#-- 分享ICON -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享ICON <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="iconUrl" name="iconUrl" class="form-control js-postData input" value="${content.iconUrl!''}" style="width: 336px"/>
                                <input class="upload_file" type="file" data-suffix="jpg#png#jpeg">
                                <a class="btn btn-success preview"   data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.iconUrl!''}"</#if>">预览</a>
                            </div>
                        </div>

                        <#-- 分享内容 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享内容 <span style="color: red">*</span></label>
                            <div class="controls">
                                <textarea id="content" name="content" class="form-control js-postData" style="width:336px;" placeholder="纯文本">${(content.content)!}</textarea>
                            </div>
                        </div>

                        <#-- 备注说明 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">备注说明 </label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text" value="<#if content??>${content.remark!''}</#if>" style="width: 336px"/>
                            </div>
                        </div>

                        <#-- 创建者 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者 </label>
                            <div class="controls">
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="${createUser!''}" style="width: 336px" readonly/>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<div class="layer loading_layer" id="loading_layer"></div>
<div class="loading" id="loading"></div>

<script type="text/javascript">
    $(function () {

        var value = $("#type").find("option:selected").val();
        if (value === 'default') {
            $("#ebook_id").show();
        } else {
            $("#ebook_id").hide();
        }

        //类型-间隔天数切换
        $("#type").change(function () {
            var value = $("#type").find("option:selected").val();
            if (value === 'default') {
                $("#ebook_id").show();
            } else {
                $("#ebook_id").hide();
            }
        });

        //上传图片
        $(".upload_file").change(function () {
            showLoadingFn();
            var $this = $(this);
            var suffix = $this.val().split('.').pop().toLowerCase();
            if ($this.val() != '') {
                var acceptSuffix = new String($this.attr("data-suffix")).split("#");
                if (acceptSuffix.indexOf(suffix) === -1) {
                    alert("仅支持以下文件格式" + acceptSuffix);
                    hideLoadingFn();
                    return;
                }
                var formData = new FormData();
                formData.append('inputFile', $this[0].files[0]);
                $.ajax({
                    url: '/opmanager/studytogether/common/upload.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        hideLoadingFn();
                        if (data.success) {
                            $($this.closest('.controls').find("input.input")).attr("value", data.fileName);
                            $($this.closest('.controls').find("a.btn-success")).attr("data-href", data.fileUrl);
                        } else {
                            hideLoadingFn();
                            alert("上传失败");
                        }
                    }
                });
            }
        });

        //文件预览
        $(document).on("click", "a.preview", function () {
            var link = $(this).attr("data-href");
            if (!link) {
                alert("文件上传中，请稍后预览");
                return;
            }
            window.open(link);
        });

        $("#skuId").blur(function () {
            var skuId = $("#skuId").val();
            if (skuId) {
                $.get("/opmanager/studytogether/common/sku_name.vpage", {skuId: skuId}, function (data) {
                    if (!data.success) {
                        alert(skuId + "对应的SKU不存在");
                        $("#skuId").val('');
                    }
                });
            }
        });

        //验证表单
        var validateForm = function () {
            var msg = "";
            if($('#skuId').val() === ''){
                msg += "课程ID为空！\n";
            }
            if($('#type').val() === ''){
                msg += "分享类型为空！\n";
            }
            if($('#title').val() === ''){
                msg += "分享标题为空！\n";
            }
            if($('#type').val() === 'default' && $('#ebookPicUrl').val() === '') {
                msg += "分享图片为空！\n";
            }
            if($('#content').val() === ''){
                msg += "分享内容为空！\n";
            }
            if (msg.length > 0) {
                alert(msg);
                return false;
            }
            return true;
        };

        //保存提交
        $(document).on("click",'#save_ad_btn',function () {
            if(validateForm()){
                var flag = true;
                var type = $("#type").val();
                var skuId = $("#skuId").val();
                var shareId_hi = $("#shareId_hi").val();
                $.ajaxSettings.async = false;
                if (shareId_hi === "") {
                    if (type && skuId) {
                        $.get("check_id.vpage", {skuId: skuId, type: type}, function (data) {
                            if (!data.success) {
                                alert("此分享已经存在，请更改课程ID或者类型后重新尝试");
                                $("#skuId").val('');
                                $("#type").val('');
                                flag = false;
                            }
                        });
                    }
                    if (!flag) {
                        return;
                    }
                }
                $.ajaxSettings.async = true;
                var post = {};
                $(".js-postData").each(function(i,item){
                    post[item.name] = $(item).val();
                });
                $.post('save.vpage',post,function (res) {
                    if(res.success){
                        alert("保存成功");
                        location.href= 'index.vpage';
                    }else{
                        alert("保存失败");
                    }
                });
            }
        });

    });

    function showLoadingFn() {
        $('#loading_layer').show();
        $('#loading').show();
    }

    function hideLoadingFn() {
        $('#loading_layer').hide();
        $('#loading').hide();
    }

</script>
</@layout_default.page>

