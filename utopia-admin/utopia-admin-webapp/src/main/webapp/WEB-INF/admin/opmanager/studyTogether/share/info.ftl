<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        <span style="color: #00a0e9">分享管理/</span>分享详情
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="rewardForm" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <div class="form-horizontal">

                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享ID</label>
                            <div class="controls">
                                <input type="text" id="shareId" name="shareId" class="form-control" value="${shareId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="skuId" name="skuId" class="form-control js-postData" value="${content.skuId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="hidden" id="type" name="type" class="form-control js-postData" value="${content.type!''}" style="width: 336px" disabled/>
                                <input type="text" id="type1" name="type1" class="form-control js-postData"
                                       value="<#if content?? && content.type == "default">电子书<#elseif content?? && content.type == "rank">排行榜<<#elseif content?? && content.type == "report">课程报告<#elseif content?? && content.type == "diploma">毕业证书</#if>"
                                       style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div id="ebook_id" class="control-group" hidden>
                            <label class="col-sm-2 control-label">分享图片 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="ebookPicUrl" name="ebookPicUrl" class="form-control js-postData input" value="${content.ebookPicUrl!''}" style="width: 336px" disabled/>
                                <a class="btn btn-success preview" data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.ebookPicUrl!''}"</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享标题 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="title" name="title" class="form-control js-postData" value='${content.title!""}' style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享ICON <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="iconUrl" name="iconUrl" class="form-control js-postData input" value="${content.iconUrl!''}" style="width: 336px" disabled/>
                                <a class="btn btn-success preview"   data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.iconUrl!''}"</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享内容 <span style="color: red">*</span></label>
                            <div class="controls">
                                <textarea id="content" name="content" disabled class="form-control js-postData" style="width:336px;" placeholder="纯文本">${(content.content)!}</textarea>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">备注说明 </label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text" value="<#if content??>${content.remark!''}</#if>" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者 </label>
                            <div class="controls">
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="${content.createUser!''}" style="width: 336px" disabled/>
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
        var value = $("#type").val();
        if (value === 'default') {
            $("#ebook_id").show();
        } else {
            $("#ebook_id").hide();
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

