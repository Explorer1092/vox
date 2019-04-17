<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="课节详情" page_num=9 jqueryVersion ="1.7.2">
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>

<div id="main_container" class="span9">

    <legend style="font-weight: 700;">
        <span style="color: #00a0e9">通知管理/</span>通知详情
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="courseNoticeForm" name="info_form" enctype="multipart/form-data" action="" method="post">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">通知ID </label>
                            <div class="controls">
                                <input type="text" id="noticeId" name="noticeId" class="form-control" value="${noticeId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="skuId" name="skuId" class="form-control js-postData" type="text" value="<#if content??>${content.skuId!''}</#if>" style="width: 336px;" disabled/>
                            </div>
                        </div>

                        <#-- 通知类型 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">通知类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="type" name="type" class="form-control js-postData" type="text" value="<#if content?? && content.type == 1>只弹一次<#elseif content?? && content.type == 2>时间间隔</#if>" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <#-- 间隔天数 -->
                        <div id="interval_day_id" class="control-group" hidden>
                            <label class="col-sm-2 control-label">间隔天数 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="intervalDay" name="intervalDay" class="form-control js-postData" value="${content.intervalDay!''}" placeholder="数值类型" style="width: 336px"/>
                            </div>
                        </div>
                        <#-- 通知图片 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">通知图片 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="picUrl" name="picUrl" class="form-control js-postData input" value="${content.picUrl!''}" style="width: 336px" disabled/>
                                <a class="btn btn-success preview"   data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.picUrl!''}"</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">文本内容 <span style="color: red">*</span></label>
                            <div class="controls">
                                <textarea id="content" name="content" class="form-control js-postData" style="width:336px;" disabled>${(content.content)!}</textarea>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">跳转链接 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="jumpUrl" name="jumpUrl" class="form-control js-postData" type="text" value="<#if content??>${content.jumpUrl!''}</#if>" style="width: 336px;" maxlength="30" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">开始日期 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="startDate" name="startDate" class="form-control js-postData" value="<#if content??>${content.startDate!''}</#if>" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">结束日期 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="endDate" name="endDate" class="form-control js-postData" value="<#if content??>${content.endDate!''}</#if>" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">备注说明 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text" value="<#if content??>${content.remark!''}</#if>" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="${content.createUser!''}" disabled/>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
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

        $(document).on("click", "a.preview", function () {
            var link = $(this).attr("data-href");
            if (!link) {
                alert("文件上传中，请稍后预览");
                return;
            }
            window.open(link);
        });
    });
</script>
</@layout_default.page>

