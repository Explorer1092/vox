<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="课节详情" page_num=9 jqueryVersion ="1.7.2">
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>

<div id="main_container" class="span9">

    <legend style="font-weight: 700;">
        周奖励详情
        <a type="button" id="btn_cancel" href="wrindex.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="clazzFestivalInfoForm" name="info_form" enctype="multipart/form-data" action="" method="post">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">奖励ID</label>
                            <div class="controls">
                                <input type="text" id="rewardId" name="rewardId" class="form-control" value="${rewardId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">奖励名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="name" name="name" class="form-control js-postData" type="text" value="<#if content??>${content.name!''}</#if>" style="width: 336px;" maxlength="30" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">奖励图标 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="iconUrl" name="iconUrl" class="form-control js-postData input" value="${content.iconUrl!''}" style="width: 336px" disabled/>
                                    <a id="iconUrl" class="btn btn-success preview"   data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.iconUrl!''}"</#if>">预览</a>
                            </div>
                        </div>

                        <#-- 奖励类型 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">奖励类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="type" name="type" style="width: 350px;" class="js-postData" disabled>
                                <#if types??>
                                    <#list types as type>
                                        <option <#if content?? && content.type??><#if content.type == type> selected="selected"</#if></#if> value = ${type!}>
                                            <#if type?? && type == 2>视频<#elseif type?? && type == 3>学习币<#elseif type?? && type == 4>电子书<#elseif type?? && type == 5>周报告</#if>
                                        </option>
                                    </#list>
                                </#if>
                                </select>
                            </div>
                        </div>

                        <#-- 奖励内容：1图书 2视频 3学习币 4电子书 5周报告 6家长奖励 -->
                        <div id="ebook_id" class="control-group" hidden>
                            <label class="col-sm-2 control-label">电子书ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="ebookId" name="ebookId" class="form-control js-postData" value="<#if obj??>${obj.ebookId!''}</#if>" style="width: 336px" disabled/>
                                <span id="ebookName"></span>
                            </div>
                        </div>
                        <div id="video_id" class="control-group" hidden>
                            <label class="col-sm-2 control-label">上传视频 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="videoUrl" name="videoUrl" class="form-control js-postData input" value="<#if obj??>${obj.videoUrl!''}</#if>" style="width: 336px" disabled/>
                                <a class="btn btn-success preview" data-href="<#if obj?? && cdn_host??>${cdn_host!''}${obj.videoUrl!''}</#if>">预览</a>
                            </div>

                        </div>
                        <div id="coin_id" class="control-group" hidden>
                            <label class="col-sm-2 control-label">学习币类型ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="coinType" name="coinType" class="form-control js-postData" value="<#if obj??>${obj.coinType!''}</#if>" style="width: 336px" disabled/>
                                <span id="coinName"></span>
                            </div>
                        </div>
                        <div id="report_id" hidden>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">宝箱文物顺序 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input type="number" id="seq" name="seq" class="form-control js-postData" value="<#if obj??>${obj.seq!''}</#if>" style="width: 336px"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">报告标题 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input type="text" id="title" name="title" class="form-control js-postData" value="<#if obj??>${obj.title!''}</#if>" style="width: 336px"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">报告描述 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input type="text" id="desc" name="desc" class="form-control js-postData" value="<#if obj??>${obj.desc!''}</#if>" style="width: 336px"/>
                                </div>
                            </div>
                        </div>
                        <div id="reward_id" class="control-group" hidden>
                            <label class="col-sm-2 control-label">家长奖励类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="rewardType" name="rewardType" class="form-control js-postData" value="<#if obj??>${obj.type!''}</#if>" style="width: 336px" disabled/>
                                <span id="rewardName"></span>
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
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="${createUser!''}" readonly/>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<script>

    $(function () {

        //奖励内容切换
        var value = $("#type").find("option:selected").val();
        if (value === '2') {
            $("#ebook_id").hide();
            $("#video_id").show();
            $("#coin_id").hide();
            $("#report_id").hide();
            $("#reward_id").hide();
        } else if (value === '3') {
            $("#ebook_id").hide();
            $("#video_id").hide();
            $("#coin_id").show();
            $("#report_id").hide();
            $("#reward_id").hide();
        } else if (value === '4') {
            $("#ebook_id").show();
            $("#video_id").hide();
            $("#coin_id").hide();
            $("#report_id").hide();
            $("#reward_id").hide();
        } else if (value === '5') {
            $("#ebook_id").hide();
            $("#video_id").hide();
            $("#coin_id").hide();
            $("#reward_id").hide();
            $("#report_id").show();
        } else if (value === '6') {
            $("#ebook_id").hide();
            $("#video_id").hide();
            $("#coin_id").hide();
            $("#report_id").hide();
            $("#reward_id").show();
        } else {
            alert("类型错误");
        }

        //文件预览
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

