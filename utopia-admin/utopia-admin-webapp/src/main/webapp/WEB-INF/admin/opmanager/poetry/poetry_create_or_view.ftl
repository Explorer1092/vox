<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='CRM' page_num=9>
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>
<#-- 使用已改过的 kindeditor-all.js-->
<script src="${requestContext.webAppContextPath}/public/js/kindeditor/kindeditor-all.js"></script>
<style>
    .uploading {
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
    .field-title {
        font-weight: bold;
    }

</style>

<div id="main_container" class="span9">
    <legend>
        <#if  edit?? && edit == 1>
            <#if poetryMission?? && poetryMission.id?has_content>编辑<#else >新增</#if>语文古诗模板
        <#else >
            模板详情-语文古诗
        </#if>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <legend class="field-title">模板基础信息</legend>
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="productName">描述：</label>
                        <div class="controls">
                            <input type="text" value="<#if poetryMission??>${poetryMission.description!''}</#if>" name="description" id="description" class="input" placeholder="50字以内">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">创建人：</label>
                        <div class="controls" style="margin-top: 5px;">
                            <input type="text" disabled="disabled" value="<#if poetryMission??>${poetryMission.createUserId!''}<#else >${admin_user!''}</#if>" name="createUserId" id="createUserId">
                        </div>
                    </div>
                </fieldset>
            </form>
            <form class="well form-horizontal" style="background-color: #fff;">
                <input type="hidden" id="template_id" name="template_id" value="<#if poetryMission??>${poetryMission.id!''}</#if>">
                <legend class="field-title">课节目标</legend>
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>标题：</label>
                        <div class="controls">
                            <input type="text" value="<#if poetryMission??>${poetryMission.title!''}</#if>" name="title" id="title" class="input">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>作者：</label>
                        <div class="controls">
                            <input type="text" value="<#if poetryMission??>${poetryMission.author!''}</#if>" name="author" id="author" class="input">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>目标描述：</label>
                        <div class="controls">
                            <input type="text" value="<#if poetryMission??>${poetryMission.goalDetail!''}</#if>" name="goalDetail" id="goalDetail" class="input">
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>上传音频：</label>
                        <div class="controls">
                            <input type="text" value="<#if poetryMission??>${poetryMission.audioUrl!''}</#if>" name="audioUrl" id="audioUrl" class="input" disabled="disabled">
                            <input class="upload_audio" type="file" data-suffix = "mp3">
                                <a class="btn btn-success preview" data-href="<#if poetryMission?? && cdn_host??>${cdn_host!''}${poetryMission.audioUrl!''}"</#if>">预览</a>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>音频时长：</label>
                        <div class="controls">
                            <input type="text" value="<#if poetryMission??>${poetryMission.audioSeconds!''}</#if>" name="audioSeconds" id="audioSeconds" class="input">秒
                        </div>
                    </div>


                    <div class="control-group" id="template_content_list">
                        <label class="control-label"><span style="color: red;font-size: 20px;">*</span>文本内容：</label>
                        <#if poetryMission?? && poetryMission.contentList?has_content && poetryMission.contentList?size gt 0>
                            <#list poetryMission.contentList as content>
                                <div class="controls singleItemBox" style="margin-top: 5px;">
                                    <input type="text" name="content" value="<#escape content as content?html>${content!''}</#escape>" class="input nameCtn">
                                    <#if content_index = 0>
                                        <input type="button" value="添加" class="btn btn-primary" id="addContentRow"> <br/>
                                    <#else>
                                        <input type="button" value="删除" class="btn thisDelete"
                                               data-val="${content_index}"><br/>
                                    </#if>
                                </div>
                            </#list>
                        <#else>
                            <div class="controls singleItemBox" style="margin-top: 5px;">
                                <input type="text" name="content" value="" class="input nameCtn">
                                <input type="button" value="添加" class="btn btn-primary" id="addContentRow">
                            </div>
                        </#if>
                        <div id="newTemplateContent"></div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">内容说明：</label>
                        <div class="controls">
                            <input type="text" value="<#if poetryMission??>${poetryMission.comment!''}</#if>" name="comment" id="comment" class="input">
                        </div>
                    </div>
                </fieldset>

                <fieldset>
                    <legend class="field-title">学习模块</legend>
                    <label><strong>模块1：</strong></label>
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块类型：</label>
                            <div class="controls">
                                <input type="text" value="名师讲解"  disabled = "disabled" class="input">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块标题：</label>
                            <div class="controls">
                                <input type="text" value="<#if expound_title??>${expound_title!''}<#else >名师讲解</#if>" name="expound_title" id="expound_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块内容：</label>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName">子标题：</label>
                            <div class="controls">
                                <input type="text" value="<#if expound_content??>${expound_content.subTitle!''}</#if>" name="expound_content_sub_title" id="expound_content_sub_title" class="input">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>上传背景图：</label>
                            <div class="controls">
                                <input type="text" value="<#if expound_content??>${expound_content.backgroundImgUrl!''}</#if>" name="expound_content_background_img_url" id="expound_content_background_img_url" class="input" disabled="disabled">
                                <input class="upload_audio" type="file" data-suffix = "jpg#png">
                                <a class="btn btn-success preview" data-href="<#if expound_content?? && cdn_host??>${cdn_host!''}${expound_content.backgroundImgUrl!''}</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>上传音频：</label>
                            <div class="controls">
                                <input type="text" value="<#if expound_content??>${expound_content.audioUrl!''}</#if>" name="expound_content_audio_url" id="expound_content_audio_url" class="input" disabled="disabled">
                                <input class="upload_audio" type="file" data-suffix = "mp3">
                                <a class="btn btn-success preview" data-href="<#if expound_content?? && cdn_host??>${cdn_host!''}${expound_content.audioUrl!''}</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>音频时长：</label>
                            <div class="controls">
                                <input type="text" value="<#if expound_content??>${expound_content.audioSeconds!0}</#if>" name="expound_content_audio_seconds" id="expound_content_audio_seconds" class="input">秒
                            </div>
                        </div>
                    </fieldset>
                    <label><strong>模块2：</strong></label>
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块类型：</label>
                            <div class="controls">
                                <input type="text" value="名句赏析"  disabled = "disabled" class="input">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块标题：</label>
                            <div class="controls">
                                <input type="text" value="<#if appreciate_title??>${appreciate_title!''}<#else >名句赏析</#if>" name="appreciate_title" id="appreciate_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块内容：</label>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName">子标题：</label>
                            <div class="controls">
                                <input type="text" value="<#if appreciate_content??>${appreciate_content.subTitle!''}</#if>" name="appreciate_content_sub_title" id="appreciate_content_sub_title" class="input">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>上传背景图：</label>
                            <div class="controls">
                                <input type="text" value="<#if appreciate_content??>${appreciate_content.backgroundImgUrl!''}</#if>" name="appreciate_content_background_img_url" id="appreciate_content_background_img_url" class="input" disabled="disabled">
                                <input class="upload_audio" type="file" data-suffix = "jpg#png">
                                <a class="btn btn-success preview" data-href="<#if appreciate_content?? && cdn_host??>${cdn_host!''}${appreciate_content.backgroundImgUrl!''}</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>上传音频：</label>
                            <div class="controls">
                                <input type="text" value="<#if appreciate_content??>${appreciate_content.audioUrl!''}</#if>" name="appreciate_content_audio_url" id="appreciate_content_audio_url" class="input" disabled="disabled">
                                <input class="upload_audio" type="file" data-suffix = "mp3">
                                <a class="btn btn-success preview" data-href="<#if appreciate_content?? && cdn_host??>${cdn_host!''}${appreciate_content.audioUrl!''}</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>音频时长：</label>
                            <div class="controls">
                                <input type="text" value="<#if appreciate_content??>${appreciate_content.audioSeconds!0}</#if>" name="appreciate_content_audio_seconds" id="appreciate_content_audio_seconds" class="input">秒
                            </div>
                        </div>
                    </fieldset>
                    <label><strong>模块3：</strong></label>
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块类型：</label>
                            <div class="controls">
                                <input type="text" value="每日朗读"  disabled = "disabled" class="input">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块标题：</label>
                            <div class="controls">
                                <input type="text" value="<#if recite_title??>${recite_title!''}<#else >每日朗诵</#if>" name="recite_title" id="recite_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块内容：</label>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>文本音频列表：</label>
                            <div class="controls">
                                <input class="upload_audio_batch"  type="file" multiple="multiple" data-suffix = "mp3">
                            </div>
                        </div>
                        <table class="table table-bordered">
                            <thead>
                            <tr>
                                <th><span style="color: red;font-size: 20px;">*</span>音频列表</th>
                                <th><span style="color: red;font-size: 20px;">*</span>音频时长</th>
                                <th><span style="color: red;font-size: 20px;">*</span>句子内容</th>
                                <th><span style="color: red;font-size: 20px;">*</span>展示顺序</th>
                            </tr>
                            </thead>
                            <tbody id="recite_file_list">
                                <#if recite_content?? && recite_content.sentenceList?has_content && recite_content.sentenceList?size gt 0>
                                    <#list recite_content.sentenceList as sentence>
                                    <tr>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control"  value="${sentence.audioUrl!''}" dir='rtl' disabled="disabled"/>
                                                <a class="btn btn-success preview" data-href="${cdn_host!''}${sentence.audioUrl!''}">预览</a>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control" style="width: 70px;" value="${sentence.audioSeconds!0}"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <#assign recite_signal_content = sentence.sentence>
                                                <input type="text" class="form-control" value="<#escape recite_signal_content as recite_signal_content?html>${recite_signal_content!''}</#escape>"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control" style="width: 70px;" value="${sentence_index +1}"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <a class="btn btn-warning delete_recite_audio">删除</a>
                                            </div>
                                        </td>
                                    </tr>
                                    </#list>
                                </#if>

                            </tbody>
                        </table>
                    </fieldset>
                    <label><strong>模块4：</strong></label>
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块类型：</label>
                            <div class="controls">
                                <input type="text" value="趣味练习"  disabled = "disabled" class="input">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块标题：</label>
                            <div class="controls">
                                <input type="text" value="<#if fun_title??>${fun_title!''}<#else >趣味练习</#if>" name="fun_title" id="fun_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块内容：</label>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>题目ID：</label>
                            <div class="controls">
                                <textarea style="width: 300px;height: 100px;" name="fun_questionIds" id="fun_questionIds" placeholder="题目ID用#分割"><#if fun_content?? && fun_content.questionIds?has_content && fun_content.questionIds?size gt 0>${(fun_content.questionIds![])?join("#")}</#if></textarea>
                            </div>
                        </div>
                    </fieldset>
                </fieldset>
                <div class="control-group" >
                    <span id="save_error_message" style="color: red"></span>
                </div>
                <#if edit?? && edit == 1>
                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="save_info_button" value="提交" class="btn btn-large btn-primary">
                        </div>
                    </div>
                </#if>
            </form>
        </div>
    </div>
</div>
<div class="uploading" id="uploading"></div>
<div class="loading" id="loading"></div>
<script type="text/html" id="T:AUDIO_TABLE_ITEM_TEMPLATE">
    <% for (var i = 0; i < file_list.length; i++) { %>
    <% var file = file_list[i]; %>
    <tr>
        <td>
            <div class='input-group'>
                <input type='text' class='form-control' dir='rtl' value='<%=file.fileName%>' disabled='disabled'/>
                <a class='btn btn-success preview' data-href='<%=file.fileUrl%>'>预览</a>
            </div>
        </td>
        <td><div class='input-group'><input type='text' class='form-control' value='' /></div></td>
        <td><div class='input-group'><input type='text' class='form-control' style='width: 70px;' value='' /></div></td>
        <td>
            <div class="input-group">
                <input type="text" class="form-control" style="width: 70px;" value=""/>
            </div>
        </td>
        <td><div class='input-group'> <a class='btn btn-warning delete_recite_audio'>删除</a> </div> </td>
    </tr>
    <% } %>
</script>
<script type="text/javascript">
    $(function () {
        function openUploadModel() {
            $("#uploading").show();
            $("#loading").show();
        }

        function closeUploadModel() {
            $("#uploading").hide();
            $("#loading").hide();
        }
        //上传单个图片或者音频
        $(".upload_audio").change(function () {
            var $this = $(this);
            var suffix = $this.val().split('.').pop().toLowerCase();
            if ($this.val() && $this.val().trim()) {
                var acceptSuffix = $this.attr("data-suffix").split("#");
                if(acceptSuffix.indexOf(suffix) === -1){
                    alert("仅支持以下文件格式" + acceptSuffix);
                    return;
                }
                //限制30M
                if ($this[0].files[0].size > 31457280) {
                    alert("您选择的文件大小超过30M");
                    return;
                }
                var formData = new FormData();
                formData.append('inputFile', $this[0].files[0]);
                openUploadModel();
                $.ajax({
                    url: '/opmanager/poetry/upload_signal_file_to_oss.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        closeUploadModel();
                        if (data.success) {
                            $($this.closest('.controls').find("input.input")).attr("value",data.fileName);
                            $($this.closest('.controls').find("a.btn-success")).attr("data-href",data.fileUrl);
                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });

        //批量上传音频
        $(".upload_audio_batch").change(function () {
            var $this = $(this);
            var acceptSuffix = $this.attr("data-suffix").split("#");
            if ($this.val() && $this.val().trim()) {
                var formData = new FormData();
                var inputFiles = $this[0].files;
                var fileSize = 0;
                for (var i = 0; i < inputFiles.length; i++) {
                    var suffix = inputFiles[i].name.split('.').pop().toLowerCase();
                    if (acceptSuffix.indexOf(suffix) === -1) {
                        alert("仅支持以下文件格式" + acceptSuffix);
                        return;
                    }
                    formData.append('inputFiles', inputFiles[i], inputFiles[i].name);
                    fileSize += inputFiles[i].size;
                }
                //限制30M
                if (fileSize > 31457280) {
                    alert("您选择的文件大小超过30M");
                    return;
                }
                openUploadModel();
                $.ajax({
                    url: '/opmanager/poetry/batch_upload_file_to_oss.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        closeUploadModel();
                        if (data.success) {
                            $("#recite_file_list").append(template("T:AUDIO_TABLE_ITEM_TEMPLATE",{
                                file_list : data.file_list
                            }));
                        } else {
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

        //添加一行正文
        var singleItemBox = 1;
        $(document).on("click", "#addContentRow", function () {
            var newTemplateContent = $("#newTemplateContent");
            var _html = '<div class="controls singleItemBox" style="margin-top: 5px;">' +
                    '<input type="text" name="content" value="" class="input nameCtn">' +
                    ' <input type="button" value="删除" class="btn thisDelete" data-val="' + (singleItemBox++) + '">' +
                    '</div>';
            newTemplateContent.append(_html);
        });

        //删除一行正文
        $(document).on("click", "#template_content_list .thisDelete", function () {
            var $this = $(this);
            singleItemBox--;
            $this.closest(".singleItemBox").remove();
        });


        //删除一个每日朗读得语音
        $(document).on("click", ".delete_recite_audio", function () {
            $(this).closest("tr").remove();
        });

        //保存古诗模版
        $("#save_info_button").on("click", function () {

            //古诗基本信息-正文句子列表
            var template_content_list = [];
            var contentInput = $("#template_content_list").find("input[name=content]");
            for (var i = 0; i < contentInput.length; i++) {
                if ($(contentInput[i]).val().trim().length > 0) {
                    template_content_list.push($(contentInput[i]).val().trim());
                }
            }
            if (template_content_list.length <= 0) {
                alert("古诗正文不能为空");
                return;
            }
            //名师精讲
            if ($("#expound_title").val().trim().length > 7) {
                alert("名师精讲模块标题不能超过7个字");
                return;
            }
            //名句赏析
            if ($("#appreciate_title").val().trim().length > 7) {
                alert("名句赏析模块标题不能超过7个字");
                return;
            }
            //每日朗读-音频列表
            var audio_list_label = $("#recite_file_list").find("tr");
            var audio_list = [];
            for (var ii = 0; ii < audio_list_label.length; ii++) {
                var audio_info_input = $(audio_list_label[ii]).find("input");
                if (audio_info_input.length !== 4) {
                    alert("每日朗读音频信息错误");
                    return;
                }
                if ($(audio_info_input[0]).val().replace(/\s+/g, "").length <= 0) {
                    alert("每日朗读音频地址错误");
                    return;
                }
                if ($(audio_info_input[1]).val().replace(/\s+/g, "").length <= 0) {
                    alert("每日朗读音频时长错误");
                    return;
                }
                if ($(audio_info_input[2]).val().trim().length <= 0) {
                    alert("每日朗读音频句子内容错误");
                    return;
                }
                var audio_info = {
                    audioUrl: $(audio_info_input[0]).val().replace(/\s+/g, ""),
                    audioSeconds: $(audio_info_input[1]).val().replace(/\s+/g, ""),
                    sentence: $(audio_info_input[2]).val().trim(),
                    rank: $(audio_info_input[3]).val().replace(/\s+/g, "")
                };
                audio_list.push(audio_info);
            }
            if (audio_list.length <= 0) {
                alert("每日朗读句子列表不能为空");
                return;
            }
            //每日朗读
            if ($("#recite_title").val().trim().length > 7) {
                alert("每日朗读模块标题不能超过7个字");
                return;
            }



            //趣味练习
            var questionIds = [];
            if ($("#fun_questionIds").val().trim().length > 0) {
                questionIds = $("#fun_questionIds").val().replace(/\s+/g, "").split("#");
            }
            $.each(questionIds,function (index, value) {
                if(!value){
                    questionIds.pop(index);
                }
            });
            if (questionIds.length === 0) {
                alert("趣味练习题目ID不能为空");
                return;
            }
            if ($("#fun_title").val().trim().length > 7) {
                alert("趣味练习模块标题不能超过7个字");
                return;
            }

            //保存
            var poetry_info = {
                id : $("#template_id").val(),
                title : $("#title").val().trim(),
                author : $("#author").val().trim(),
                goalDetail : $("#goalDetail").val().trim(),
                audioUrl : $("#audioUrl").val().trim(),
                audioSeconds : $("#audioSeconds").val().trim(),
                comment : $("#comment").val().trim(),
                description : $("#description").val().trim(),
                contentList : template_content_list,
                createUserId : $("#createUserId").val().trim(),
                models : {
                    EXPOUND : {
                        modelType : "EXPOUND",
                        modelTitle : $("#expound_title").val().trim(),
                        expoundContent : {
                            subTitle : $("#expound_content_sub_title").val().trim(),
                            backgroundImgUrl : $("#expound_content_background_img_url").val().trim().replace(/\s+/g, ""),
                            audioUrl : $("#expound_content_audio_url").val().trim().replace(/\s+/g, ""),
                            audioSeconds : $("#expound_content_audio_seconds").val().trim()
                        }
                    },
                    APPRECIATE : {
                        modelType : "APPRECIATE",
                        modelTitle : $("#appreciate_title").val().trim(),
                        appreciateContent : {
                            subTitle : $("#appreciate_content_sub_title").val().trim(),
                            backgroundImgUrl : $("#appreciate_content_background_img_url").val().trim().replace(/\s+/g, ""),
                            audioUrl : $("#appreciate_content_audio_url").val().trim().replace(/\s+/g, ""),
                            audioSeconds : $("#appreciate_content_audio_seconds").val().trim()
                        }
                    },
                    RECITE : {
                        modelType : "RECITE",
                        modelTitle : $("#recite_title").val().trim(),
                        reciteContent : {
                            sentenceList : audio_list
                        }
                    },
                    FUN : {
                        modelType : "FUN",
                        modelTitle : $("#fun_title").val().trim(),
                        funContent : {
                            questionIds : questionIds
                        }
                    }
                }
            };
            $.post("/opmanager/poetry/save/ancient_poetry_mission.vpage", {
                poetry_info: JSON.stringify(poetry_info)
            }, function (data) {
                if (data.success) {
                    alert("保存成功，模板ID为：" + data.id);
                    window.close();
                } else {
                    alert(data.info);
                    $("#save_error_message").html(data.info);
                }
            });
        });
    });
</script>
</@layout_default.page>