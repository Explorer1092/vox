<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑点评" page_num=17>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑点评&nbsp;&nbsp;
        <a title="返回" href="javascript:void(0);" class="btn" id="back_list">
            <i class="icon-share-alt"></i> 返  回
        </a>
        <a title="保存" href="javascript:void(0);" class="btn btn-primary" id="save_info">
            <i class="icon-pencil icon-white"></i> 保  存
        </a>
    </legend>
    <div class="row-fluid"><div class="span12"><div class="well">
        <form id="info_frm" name="info_frm" enctype="multipart/form-data" action="saverating.vpage" method="post">
            <input id="ratingId" name="ratingId" value="${ratingId!}" type="hidden">
            <div class="form-horizontal">
                <div class="control-group">
                    <label class="col-sm-2 control-label">机构ID</label>
                    <div class="controls">
                        <input type="text" id="shopId" name="shopId" class="form-control input_txt"
                            <#if new?? && !new> <#if rating??>value="${rating.shopId!}"</#if> <#else> <#if shopId??>value="${shopId!}"</#if> </#if>/>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">用户ID</label>
                    <div class="controls">
                        <input type="text" id="userId" name="userId" class="form-control input_txt" value="<#if rating??>${rating.userId!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">用户名称</label>
                    <div class="controls">
                        <input type="text" id="userName" name="userName" class="form-control input_txt" value="<#if rating??>${rating.userName!}</#if>" />
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label">点评内容</label>
                    <div class="controls">
                        <textarea id="ratingContent" name="ratingContent" class="intro_small"  placeholder="请填写"><#if rating??>${rating.ratingContent!}</#if></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">点评星级</label>
                    <div class="controls">
                        <select id="rating" name="rating">
                            <option <#if rating?? && rating.rating?? && rating.rating == 1>selected="selected"</#if> value="1">1星</option>
                            <option <#if rating?? && rating.rating?? && rating.rating == 2>selected="selected"</#if> value="2">2星</option>
                            <option <#if rating?? && rating.rating?? && rating.rating == 3>selected="selected"</#if> value="3">3星</option>
                            <option <#if rating?? && rating.rating?? && rating.rating == 4>selected="selected"</#if> value="4">4星</option>
                            <option <#if rating?? && rating.rating?? && rating.rating == 5>selected="selected"</#if> value="5">5星</option>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">评论时间戳</label>
                    <div class="controls">
                        <input type="text" id="ratingTime" name="ratingTime" class="form-control input_txt" value="<#if rating??>${rating.ratingTime!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">活动ID</label>
                    <div class="controls">
                        <input type="text" id="activityId" name="activityId" class="form-control input_txt" value="<#if rating??>${rating.activityId!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">是否优质点评</label>
                    <div class="controls">
                        <input type="checkbox" id="goodRating" name="goodRating" <#if rating?? && rating.goodRating!false> checked </#if>>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">费用</label>
                    <div class="controls">
                        <input type="text" id="cost" name="cost" class="form-control input_txt" value="<#if rating??>${rating.cost!}</#if>" />
                    </div>
                </div>
                <#if rating??>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">用户头像</label>
                        <div class="controls">
                            <#if (rating.userAvatar)?has_content>
                                <table class="table img_table">
                                    <tr>
                                        <td class="img_td">
                                            <div class="img_box">
                                                <span class="img_x_alt" id="del_avatar" data-file="${rating.userAvatar!}"><img src="${requestContext.webAppContextPath}/public/img/x_alt.png"/></span>
                                                <img src="${rating.userAvatar!}" />
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            <#else>
                                <a class="btn btn-info" href="javascript:void(0);" id="upload_btn_avatar" style="margin-bottom: 5px;"> <i class="icon-picture icon-white"></i> 上  传</a>
                            </#if>

                        </div>
                    </div>
                </#if>
                <#if rating??>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">评论图片</label>
                        <div class="controls">
                            <a class="btn btn-info" href="javascript:void(0);" id="upload_btn_banner" style="margin-bottom: 5px;"> <i class="icon-picture icon-white"></i> 上  传</a>
                            <table class="table img_table">
                                <#if rating?? && rating.photo?? && rating.photo?has_content>
                                    <#list rating.photo as banner>
                                        <#if banner_index%3== 0> <tr></#if>
                                        <td class="img_td">
                                            <div class="img_box">
                                                <span class="img_x_alt" id="del_banner" data-file="${banner!}"><img src="${requestContext.webAppContextPath}/public/img/x_alt.png"/></span>
                                                <#if banner?? && (banner?string?index_of("17zuoye") > -1)>
                                                    <img src="${banner!}@1o" />
                                                <#else>
                                                    <img src="${banner!}" />
                                                </#if>
                                            </div>
                                        </td>
                                        <#if banner_index%3==2 || !banner_has_next>
                                            <#if rating.photo?size % 3 == 0><td></td><td></td></#if>
                                            <#if rating.photo?size % 3 == 1><td></td></#if>
                                        </tr>
                                        </#if>
                                    </#list>
                                </#if>
                            </table>
                        </div>
                    </div>
                </#if>
            </div>
        </form>
    </div></div></div>
</div>
<div id="uploaderDialog" class="modal fade hide" style="width:550px; height: 300px;">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>上传图片</h3>
    </div>
    <div class="modal-body">
        <div style="float: left; width: 280px;">
            <div style="height: 200px; width: 280px;">
                <img id="imgSrc" src="" alt="预览" style="height: 200px; width: 280px;"/>
            </div>
        </div>
        <div style="float: right">
            <div style="display: block;">
                <a href="javascript:void(0);" class="uploader">
                    <input type="file" name="file" id="uploadFile" accept="image/*" onchange="previewImg(this)">选择素材
                </a>
            </div>
        </div>
        <input type="hidden" id="uploadField" value="photo">
    </div>
    <div class="modal-footer">
        <button title="确认上传" class="uploader" id="upload_confirm">
            <i class="icon-ok"></i>
        </button>
        <button class="uploader" data-dismiss="modal" aria-hidden="true"><i class="icon-trash"></i></button>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $('#info_frm').on('submit', function () {
            $('#info_frm').ajaxSubmit({
                type: 'post',
                url: 'saverating.vpage',
                success: function (res) {
                    if (res.success) {
                        alert("保存成功");
                        window.location.href = "info.vpage?ratingId=" + res.ratingId;
                    } else {
                        alert("保存失败:" + res.info);
                    }
                },
                error: function (msg) {
                    alert("保存失败！");
                }
            });
            return false;
        });

        $('#save_info').on('click', function () {
            if (confirm("是否确认保存？")) {
                $('#info_frm').submit();
            }
        });

        $('#back_list').on('click', function () {
            window.location.href = "${requestContext.webAppContextPath}/mizar/rating/index.vpage?shopId=" + $('#shopId').val();
        });

        $("[id^='del_']").on('click', function() {
            if (!confirm("是否确认删除？")) {
                return false;
            }
            var $this = $(this);
            var field = $this.attr("id").substring("del_".length);
            var file = $(this).data("file");
            var ratingId = $('#ratingId').val();
            $.post('deletephoto.vpage', {ratingId : ratingId, field: field, file: file}, function(res) {
                if (res.success) {
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });
        });

        $("[id^='upload_btn_']").on('click', function() {
            var filed = $(this).attr("id").substring("upload_btn_".length);
            $('#uploadFile').val("");
            $('#uploadField').val(filed);
            $('#imgSrc').attr("src", "");
            $('#uploaderDialog').modal("show");
        });

        $('#upload_confirm').on('click', function() {
            // 获取参数
            var field = $('#uploadField').val();
            // 拼formData
            var formData = new FormData();
            var file = $('#uploadFile')[0].files[0];
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);
            formData.append('field', field);
            formData.append('ratingId', $('#ratingId').val());
            // 发起请求
            $.ajax({
                url: 'uploadphoto.vpage' ,
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (res) {
                    if (res.success) {
                        alert("上传成功");
                        window.location.reload();
                    } else {
                        alert(res.info);
                    }
                }
            });
        });
    });

    function previewImg(file) {
        var prevDiv = $('#imgSrc');
        if (file.files && file.files[0]) {
            var reader = new FileReader();
            reader.onload = function (evt) {
                prevDiv.attr("src", evt.target.result);
            };
            reader.readAsDataURL(file.files[0]);
        }
        else {
            prevDiv.html('<img class="img" style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale,src=\'' + file.value + '\'">');
        }
    }
</script>
</@layout_default.page>