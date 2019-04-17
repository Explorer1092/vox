<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='17说' page_num=9>

<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>

<div class="span9">
    <legend>
        <strong>17说</strong>
    </legend>
    <div class="row-fluid">
        <div class="span8">
            <form id="publishForm" class="well form-horizontal" method="post" action="/opmanager/talk/saveaudio.vpage">
                <input type="hidden" name="audioId" value="${audioId!''}"/>
                    <legend>发布/编辑音频文稿</legend>
                    <div class="control-group">
                        <input type="hidden" name="audioId" value="${audioId!''}"/>
                        <label class="control-label">文稿名称：</label>
                        <div class="controls">
                            <input type="text" placeholder="请输入标题" maxlength="10" class="input" value="${title!''}" name="title"/>
                        </div>
                    </div>
                    <div id="container">
                        <#if contentList?exists>
                             <#list contentList as content>
                                <#switch content.type!0>
                                    <#case 1>
                        <div class="control-group">
                            <label class="control-label">图片资料：</label>
                            <div class="controls">
                                <i class="addIcon">上传图片（小于3M）</i>
                                <input class="fileUpBtn" type="file"
                                       accept="image/gif, image/jpeg, image/png, image/jpg"
                                       value="${content.content}"
                                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                                <img width="250" src="${content.content}" />
                                <input type="hidden" name="content" value="${content.content}"/>
                                <label class="col-xs help-block">删除</label>
                            </div>
                            <input type="hidden" name="duration" value="0"/>
                            <input type="hidden" name="audioType" value="1">
                        </div>
                                        <#break>
                                    <#case 2>
                        <div class="control-group">
                            <label class="control-label">文件资料：</label>
                            <div class="controls">
                                <textarea class="form-control span6"
                                      placeholder="请输入话题介绍"
                                      name="content" rows="3">${content.content}</textarea>
                                <label class="col-xs help-block">删除</label>
                            </div>
                            <input type="hidden" name="duration" value="0"/>
                            <input type="hidden" name="audioType" value="2">
                        </div>
                                        <#break>
                                    <#case 3>
                        <div class="control-group">
                            <label class="control-label">音频资料：</label>
                            <div class="controls">
                                时长：<input type="text" name="duration" value="${content.duration}" style="width: 60px;"/><br/>
                                <i class="addIcon">上传音频</i>
                                <input class="fileUpBtn" type="file"
                                       accept="audio/*"
                                       value="${content.content}"
                                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                                <input type="hidden" name="content" value="${content.content}"/>
                                <input type="hidden" name="audioType" value="3">
                                <label class="col-xs help-block">删除</label>
                            </div>
                        </div>
                                        <#break>
                                    <#default>
                                </#switch>
                             </#list>
                        </#if>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input data="${enable!''}" type="button" id="saveBtn" value="保存" class="btn btn-large btn-primary">
                        </div>
                    </div>
            </form>
        </div>
        <div class="span3">
            <fieldset>
                <legend>操作</legend>
                <div class="control-group">
                    <label class="control-label" id="insertImage">插入图片</label>
                </div>
                <div class="control-group">
                    <label class="control-label" id="insertVideo">插入音频</label>
                </div>
                <div class="control-group">
                    <label class="control-label" id="insertText">插入文本</label>
                </div>
            </fieldset>
        </div>
    </div>
</div>
<script lang="javascript">

    var appendFunc = function (html) {
        $("#container").append(html);
        $("#container .help-block:last").click(function () {
            $(this).parents(".control-group").remove();
        })
    }

    var upload = function (object, callback) {
        if(object.val() != ''){
            var formData = new FormData();
            formData.append('inputFile', object[0].files[0]);
            $.ajax({
                url: 'upload.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    if (data.success) {
                        callback(data.path);
                        alert("上传成功");
                    } else {
                        alert("上传失败");
                    }
                }
            });
        }
    }

    $(document).ready(function () {
        $("#insertText").click(function () {
            var str = "<div class=\"control-group\">\n" +
                    "                            <label class=\"control-label\">文件资料：</label>\n" +
                    "                            <div class=\"controls\">\n" +
                    "                                <textarea class=\"form-control span10\"\n" +
                    "                                      placeholder=\"请输入文本资料\"\n" +
                    "                                      name=\"content\" rows=\"3\"></textarea>\n" +
                    "                                <label class=\"col-xs help-block\">删除</label>\n" +
                    "                            </div>\n" +
                    "                            <input type=\"hidden\" name=\"duration\" value=\"0\"/>\n" +
                    "                            <input type=\"hidden\" name=\"audioType\" value=\"2\">\n" +
                    "                        </div>";
            appendFunc(str);
        });
        $("#insertVideo").click(function () {
            var str = "<div class=\"control-group\">\n" +
                    "                            <label class=\"control-label\">音频资料：</label>\n" +
                    "                            <div class=\"controls\">\n" +
                    "                                时长：<input type=\"text\" name=\"duration\" value=\"0\" style=\"width: 60px;\"/><br/>\n" +
                    "                                <i class=\"addIcon\">上传音频</i>\n" +
                    "                                <input type=\"hidden\" name=\"audioType\" value=\"3\">\n" +
                    "                                <input data-type=\"video\"" +
                    "                                       class=\"fileUpBtn\" type=\"file\"\n accept=\"audio/*\"\n" +
                    "                                       value=\"\"\n" +
                    "                                       style=\"width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;\">\n" +
                    "                                <input type=\"text\" name=\"content\" value=\"\">\n" +
                    "                                <label class=\"col-xs help-block\">删除</label>\n" +
                    "                            </div>\n" +
                    "                        </div>";
            appendFunc(str);

            $("#container .fileUpBtn:last").change(function () {
                var container = $(this).parent();
                var content = container.find(":input[name='content']");
                upload($(this), function (path) {
                    content.val(path);
                });
            });

        });
        $("#insertImage").click(function () {
            var str = "<div class=\"control-group\">\n" +
                    "                            <label class=\"control-label\">图片资料：</label>\n" +
                    "                            <div class=\"controls\">\n" +
                    "                                <i class=\"addIcon\">上传图片（小于3M）</i>\n" +
                    "                                <input data-type=\"img\"" +
                    "                                       class=\"fileUpBtn\" type=\"file\"\n" +
                    "                                       accept=\"image/gif, image/jpeg, image/png, image/jpg\"\n" +
                    "                                       value=\"\"\n" +
                    "                                       style=\"width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;\">\n" +
                    "                                <img width=\"250\" src=\"\">\n" +
                    "                                <input type=\"hidden\" name=\"content\" value=\"\">\n" +
                    "                                <label class=\"col-xs help-block\">删除</label>\n" +
                    "                            </div>\n" +
                    "                            <input type=\"hidden\" name=\"duration\" value=\"0\"/>\n" +
                    "                            <input type=\"hidden\" name=\"audioType\" value=\"1\">\n" +
                    "                        </div>";
            appendFunc(str);
            $("#container .fileUpBtn:last").change(function () {
                var container = $(this).parent();
                var img = container.find("img");
                var content = container.find(":input[name='content']");
                upload($(this), function (path) {
                    img.attr("src", path);
                    content.val(path);
                });
            });
        });
        $("#saveBtn").click(function () {

            if($(":input[name='title']").val() == ''){
                alert('请输入文稿名称');
                return;
            }

            $("#publishForm").ajaxSubmit({
                url:"/opmanager/talk/audiosave.vpage",
                type:"post",
                dataType:"json",
                success:function(data){
                    if(data.success){
                        alert("保存成功");
                        document.location.href="/opmanager/talk/audiolist.vpage";
                    }else {
                        alert(data.info);
                    }
                },
                clearForm:false,
                resetForm:false
            });
        });


        if($("#saveBtn").attr("data") == 'cancel'){
            $("#saveBtn").attr("disabled", true);
        }

        $("#container .fileUpBtn[data-type='img']").change(function () {
            var container = $(this).parent();
            var img = container.find("img");
            var content = container.find(":input[name='content']");
            upload($(this), function (path) {
                img.attr("src", path);
                content.val(path);
            });
        });

        $("#container .fileUpBtn[data-type='video']").change(function () {
            var container = $(this).parent();
            var content = container.find(":input[name='content']");
            upload($(this), function (path) {
                content.val(path);
            });
        });

        $("#container .help-block").click(function () {
            $(this).parents(".control-group").remove();
        })
    })
</script>
</@layout_default.page>