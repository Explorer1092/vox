<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/datepicker/WdatePicker.js"></script>
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
        <span style="color: #00a0e9">通知管理/</span>添加|编辑通知
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存通知"/>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="rewardForm" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <input id="noticeId" name="noticeId" value="${noticeId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">
                        <#-- 通知ID-->
                        <#if noticeId?has_content>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">通知ID</label>
                            <div class="controls">
                                <input type="text" id="noticeId" name="noticeId" class="form-control" value="${noticeId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        </#if>

                        <#-- 课程ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="skuId" name="skuId" class="form-control js-postData" value="${content.skuId!''}" style="width: 336px"/>
                                <span id="skuName"></span>
                            </div>
                        </div>

                        <#-- 通知类型 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">通知类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="type" name="type" style="width: 350px;" class="js-postData">
                                <option value="">--请选择通知类型--</option>
                                <#if types??>
                                    <#list types as type>
                                        <option <#if content?? && content.type??><#if content.type == type> selected="selected"</#if></#if> value = ${type!}>
                                            <#if type?? && type == 1>只弹一次<#elseif type?? && type == 2>时间间隔</#if>
                                        </option>
                                    </#list>
                                </#if>
                                </select>
                            </div>
                        </div>

                        <#-- 间隔天数 -->
                        <div id="interval_day_id" class="control-group" hidden>
                            <label class="col-sm-2 control-label">间隔天数 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="intervalDay" name="intervalDay" class="form-control js-postData" value="${content.intervalDay!''}" placeholder="自然数" style="width: 336px"/>
                            </div>
                        </div>

                        <#-- 通知图片 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">通知图片 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="picUrl" name="picUrl" class="form-control js-postData input" value="${content.picUrl!''}" style="width: 336px"/>
                                <input id="upload_file" class="upload_file" type="file" data-suffix="jpg#png#jpeg">
                                <a class="btn btn-success preview"   data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.picUrl!''}"</#if>">预览</a>
                            </div>
                        </div>

                        <#-- 文本内容 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">文本内容 <span style="color: red">*</span></label>
                            <div class="controls">
                                <textarea id="content" name="content" class="form-control js-postData" style="width:336px;" placeholder="纯文本">${(content.content)!}</textarea>
                            </div>
                        </div>

                        <#-- 跳转链接 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">跳转链接 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="jumpUrl" name="jumpUrl" class="form-control js-postData" value="${content.jumpUrl!''}" style="width: 336px"/>
                            </div>
                        </div>

                        <#-- 开始日期 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">开始日期 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="startDate" name="startDate" class="form-control js-postData" value="<#if content??>${content.startDate!''}</#if>" style="width: 336px;" onclick="WdatePicker({dateFmt: 'yyyy-MM-dd HH:mm:ss'});" autocomplete="OFF"/>
                            </div>
                        </div>

                        <#-- 结束日期 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">结束日期 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="endDate" name="endDate" class="form-control js-postData" value="<#if content??>${content.endDate!''}</#if>" style="width: 336px;" onclick="WdatePicker({dateFmt: 'yyyy-MM-dd HH:mm:ss'});" autocomplete="OFF"/>
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
        if (value === '1') {
            $("#interval_day_id").hide();
        } else if (value === '2') {
            $("#interval_day_id").show();
        } else {
            $("#interval_day_id").hide();
        }

        //类型-间隔天数切换
        $("#type").change(function () {
            var value = $("#type").find("option:selected").val();
            if (value === '1') {
                $("#interval_day_id").hide();
            } else if (value === '2') {
                $("#interval_day_id").show();
            } else {
                $("#interval_day_id").hide();
            }
        });

        //上传图片
        $("#upload_file").change(function () {
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

        //sku name 检测
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
        var num_reg = /^[0-9]*$/;
        var validateForm = function () {
            var msg = "";
            if($('#skuId').val() === ''){
                msg += "课程ID为空！\n";
            }
            if($('#type').val() === ''){
                msg += "通知类型为空！\n";
            }
            if($('#type').val() === '2') {
                if ($('#intervalDay').val() === '' || !$('#intervalDay').val().match(num_reg)) {
                    msg += "间隔天数不是自然数！\n";
                }
            }
            if($('#picUrl').val() === ''){
                msg += "通知图片为空！\n";
            }
            if($('#content').val() === ''){
                msg += "文本内容为空！\n";
            }
            if($('#jumpUrl').val() === ''){
                msg += "跳转链接为空！\n";
            }
            if($('#startDate').val() === ''){
                msg += "开始日期为空！\n";
            }
            if($('#endDate').val() === ''){
                msg += "结束日期为空！\n";
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

