<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑课程" page_num=17>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑课程&nbsp;&nbsp;
        <a title="返回" href="javascript:window.history.back();" class="btn">
            <i class="icon-share-alt"></i> 返  回
        </a>
        <a title="保存" href="javascript:void(0);" class="btn btn-primary" id="save_info">
            <i class="icon-pencil icon-white"></i> 保  存
        </a>
        <#if course??>
            <div  style="float: right;">
                <a title="配置投放策略" href="${requestContext.webAppContextPath}/mizar/course/courseconfig.vpage?courseId=${course.id!''}" class="btn btn-info">
                    <i class="icon-th icon-white"></i> 配置投放策略
                </a>
            </div>
        </#if>
    </legend>
    <div class="row-fluid"><div class="span12"><div class="well">
        <form id="info_frm" name="info_frm" enctype="multipart/form-data" action="save.vpage" method="post">
            <input id="courseId" name="courseId" value="${courseId!}" type="hidden">
            <div class="form-horizontal">
                <div class="control-group">
                    <label class="col-sm-2 control-label">标题<span style="font-size: 12px;color: red;">*必填</span></label>
                    <div class="controls">
                        <input type="text" id="title" name="title" class="form-control input_txt" value="<#if course??>${course.title!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">副标题</label>
                    <div class="controls">
                        <textarea id="subTitle" name="subTitle" class="intro_small"  placeholder="请填写课程标题"><#if course??>${course.subTitle!}</#if></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">课程简介</label>
                    <div class="controls">
                        <textarea id="description" name="description" class="intro"  placeholder="请填写课程简介"><#if course??>${course.description}</#if></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">跳转URL</label>
                    <div class="controls">
                        <input type="text" id="redirectUrl" name="redirectUrl" class="form-control input_txt" value="<#if course??>${course.redirectUrl!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">是否置顶</label>
                    <div class="controls">
                        <label class="radio-inline" style="position:relative;top:4px;display: inline-block;margin-bottom: 0;vertical-align: middle;cursor: pointer;"><input type="radio" name="top" <#if (course.top)!false>checked="checked"</#if> style="position: relative;top:-3px;" /> 是</label>
                        <label class="radio-inline" style="position:relative;top:4px;display: inline-block;margin-left: 20px;margin-bottom: 0;vertical-align: middle;cursor: pointer;"><input type="radio" <#if course??><#if !course.top>checked="checked"</#if><#else>checked="checked"</#if> value="false" name="top" style="position: relative;top:-3px;" /> 否</label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">是否显示到首页</label>
                    <div class="controls">
                        <label class="radio-inline" style="position:relative;top:4px;display: inline-block;margin-bottom: 0;vertical-align: middle;cursor: pointer;"><input type="radio" name="indexShow" <#if (course.indexShow)!false>checked="checked"</#if> style="position: relative;top:-3px;" /> 是</label>
                        <label class="radio-inline" style="position:relative;top:4px;display: inline-block;margin-left: 20px;margin-bottom: 0;vertical-align: middle;cursor: pointer;"><input type="radio" <#if course??><#if !course.indexShow>checked="checked"</#if><#else>checked="checked"</#if> value="false" name="indexShow" style="position: relative;top:-3px;" /> 否</label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">状态</label>
                    <div class="controls">
                        <select name="status">
                            <#list status as s>
                                <option value="${s}" <#if s==(course.status)!>selected</#if>>
                                    <#switch s>
                                        <#case "ONLINE">
                                            上线
                                            <#break>
                                        <#case "OFFLINE">
                                            下线
                                            <#break>
                                        <#default>
                                    </#switch>
                                </option>
                            </#list>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">类别</label>
                    <div class="controls">
                        <select name="category">
                            <#list courseCategory as s>
                                <option value="${s.name()!}" <#if s.name()==(course.category)!>selected</#if>>${s.getDesc()!}</option>
                            </#list>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">活动ID（对应的通用导流活动ID）</label>
                    <div class="controls">
                        <input type="text" id="activityId" name="activityId" class="form-control input_txt" value="<#if course??>${course.activityId!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">主讲人<span style="font-size: 12px;color: red;">*必填</span></label>
                    <div class="controls">
                        <input type="text" id="keynoteSpeaker" name="keynoteSpeaker" class="form-control input_txt" value="<#if course??>${course.keynoteSpeaker!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">标签<span style="font-size: 12px;color: red;">*必填</span></label>
                    <div class="controls">
                        <textarea id="tags" name="tags" class="intro_small"
                                  placeholder="请填写标签,以逗号分隔"><#if course?? && course.tags?has_content><#list course.tags as s><#if s_index!=0>,</#if>${s}</#list></#if></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">适配年级</label>
                    <div class="controls">
                        <textarea id="clazzLevels" name="clazzLevels" class="intro_small"
                                  placeholder="请填写年级,以逗号分隔"><#if course?? && course.clazzLevels?has_content><#list course.clazzLevels as s><#if s_index!=0>,</#if>${s}</#list></#if></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">排序优先级</label>
                    <div class="controls">
                        <input type="text" id="priority" name="priority" class="form-control input_txt" value="<#if course??>${course.priority!}</#if>" /><span style="color: red;font-size: 14px;">数字越大，排序越靠前</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">课程价格<span style="font-size: 12px;color: red;">*必填</span></label>
                    <div class="controls">
                        <input type="text" id="price" name="price" class="form-control input_txt" value="<#if course??>${course.price!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">上课时间<span style="font-size: 12px;color: red;">*必填</span></label>
                    <div class="controls">
                        <input type="text" id="classTime" name="classTime" class="form-control input_txt" value="<#if course??>${course.classTime!}</#if>" />
                    </div>
                </div>
                <#if course??>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">主讲人头像<span style="font-size: 12px;color: red;">*必填</span></label>
                        <div class="controls">
                            <#if (course.speakerAvatar)?has_content>
                                <table class="table img_table">
                                    <tr>
                                        <td class="img_td">
                                            <div class="img_box">
                                                <span class="img_x_alt" id="del_avatar" data-file="${course.speakerAvatar!}"><img src="${requestContext.webAppContextPath}/public/img/x_alt.png"/></span>
                                                <img src="${course.speakerAvatar!}" />
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            <#else>
                                <a class="btn btn-info" href="javascript:void(0);" id="upload_btn_avatar" style="margin-bottom: 5px;"> <i class="icon-picture icon-white"></i> 上  传</a>
                            </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">背景图片</label>
                        <div class="controls">
                            <#if (course.background)?has_content>
                                <table class="table img_table">
                                    <tr>
                                        <td class="img_td">
                                            <div class="img_box">
                                                <span class="img_x_alt" id="del_background" data-file="${course.background!}"><img src="${requestContext.webAppContextPath}/public/img/x_alt.png"/></span>
                                                <img src="${course.background!}" />
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            <#else>
                                <a class="btn btn-info" href="javascript:void(0);" id="upload_btn_background" style="margin-bottom: 5px;"> <i class="icon-picture icon-white"></i> 上  传</a>
                            </#if>
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
                <textarea placeholder="请填写描述" id="uploadDesc" style="resize: none;"></textarea>
            </div>
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
            // 做数据校验
            var title = $("#title").val();
            if(title == ''){
                alert("请输入标题");
                return false;
            }
            var tags = $("#tags").val();
            if(tags == ''){
                alert("请输入标签");
                return false;
            }
            var keynoteSpeaker = $("#keynoteSpeaker").val();
            if(keynoteSpeaker == ''){
                alert("请输入主讲老师");
                return false;
            }
            var classTime = $("#classTime").val();
            if(classTime == ''){
                alert("请输入上课时间");
                return false;
            }
            var price = $("#price").val();
            if(price == ''){
                alert("请输入课程价格");
                return false;
            }
            $('#info_frm').ajaxSubmit({
                type: 'post',
                url: 'save.vpage',
                success: function (res) {
                    if (res.success) {
                        alert("保存成功");
                        window.location.href = "coursedetail.vpage?courseId=" + res.courseId;
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


        $("[id^='del_']").on('click', function() {
            if (!confirm("是否确认删除？")) {
                return false;
            }
            var $this = $(this);
            var field = $this.attr("id").substring("del_".length);
            var file = $(this).data("file");
            var courseId = $('#courseId').val();
            $.post('deletephoto.vpage', {courseId : courseId, field: field, file: file}, function(res) {
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
            $('#uploadDesc').val("");
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
            formData.append('courseId', $('#courseId').val());
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