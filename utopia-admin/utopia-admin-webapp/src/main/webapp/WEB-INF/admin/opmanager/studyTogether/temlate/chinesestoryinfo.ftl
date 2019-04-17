<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='CRM' page_num=9>
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    .uploading {
        background: #020516;
        width: 100%;
        height: 100%;
        opacity: 0.4;
        filter: alpha(opacity=40);
        position: fixed;
        left: 0;
        top: 0;
        z-index: 1000;
        display: none;
    }

    .loading {
        width: 38px;
        height: 38px;
        background: url(/public/img/loading.gif) no-repeat;
        position: fixed;
        left: 50%;
        top: 50%;
        margin-left: -16px;
        margin-top: -16px;
        z-index: 4000;
        display: none;
    }

    .field-title {
        font-weight: bold;
    }
</style>

<div id="main_container" class="span9">
    <legend>
        <#if  edit?? && edit == 1>
            <#if template?? && template.id?has_content>编辑<#else >新增</#if>语文故事模板
        <#else >
            模板详情-语文故事
        </#if>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <legend class="field-title">模板基础信息</legend>
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span
                                    style="color: red;font-size: 20px;">*</span>SPU_ID：</label>
                        <div class="controls">
                            <input type="text" value="<#if template??>${template.spuId!''}</#if>" name="template_spu_id"
                                   id="template_spu_id" class="input">
                            <span id="spu_name"></span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span
                                    style="color: red;font-size: 20px;">*</span>模板名称：</label>
                        <div class="controls">
                            <input type="text" value="<#if template??>${template.name!''}</#if>" name="template_name"
                                   id="template_name" class="input" placeholder="50字以内">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">创建人：</label>
                        <div class="controls"
                             style="margin-top: 5px;"><#if template??>${template.createUser!''}<#else >${admin_user!''}</#if></div>
                    </div>
                </fieldset>
            </form>
            <form class="well form-horizontal" style="background-color: #fff;">
                <input type="hidden" id="template_id" name="template_id"
                       value="<#if template??>${template.id!''}</#if>">
                <legend class="field-title">课节报告</legend>
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span
                                    style="color: red;font-size: 20px;">*</span>标题：</label>
                        <div class="controls">
                            <input type="text" value="<#if template??>${template.reportTitle!''}</#if>"
                                   name="template_title"
                                   id="template_title" class="input" maxlength="25" placeholder="25字以内">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span
                                    style="color: red;font-size: 20px;">*</span>上传图片：</label>
                        <div class="controls">
                            <input type="text" value="<#if template??>${template.coverImgUrl!''}</#if>"
                                   name="template_imgUrl" id="template_imgUrl" class="input" disabled="disabled">
                            <input class="upload_file" type="file" data-suffix="jpg#png">
                            <a class="btn btn-success preview"
                               data-href="<#if template?? && cdn_host??>${cdn_host!''}${template.coverImgUrl!''}</#if>">预览</a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span
                                    style="color: red;font-size: 20px;">*</span>报告描述：</label>
                        <div class="controls">
                            <textarea style="width: 300px;height: 100px;" name="template_introduction"
                                      id="template_introduction" maxlength="70"
                                      placeholder="70字以内"><#if template?? >${template.introduction!''}</#if></textarea>
                        </div>
                    </div>
                </fieldset>
                <fieldset class="all-model">
                    <legend class="field-title">学习模块</legend>
                    <div class="select_modal">
                        新增模块类型：<select id="modal_type" name="modal_type" style="width: 150px;">
                            <option value="" selected="selected">请选择</option>
                        </select>
                        <a id="add_modal_type" class="btn btn-primary">添加</a>
                    </div>
                    <div class="all-model-div">
                        <div id="CHINESESTORY" class="modal_content">
                            <input type="hidden" name="rank" readonly="readonly">
                            <input type="hidden" id="story_model_id" readonly="readonly"
                                   value="<#if story_content??>${story_content.id!''}<#else ></#if>">
                            <label>
                                <strong>故事：</strong>
                                <a class="btn btn-inverse del_modal">删除</a>
                                <a class="up" style="cursor: pointer">上移</a>
                                <a class="down" style="cursor: pointer">下移</a>
                            </label>
                            <fieldset>
                                <div class="story">
                                    <div class="control-group">
                                        <label class="control-label" for="productName"><span
                                                    style="color: red;font-size: 20px;">*</span>模块标题：</label>
                                        <div class="controls">
                                            <input type="text"
                                                   value="<#if story_title??>${story_title!''}<#else >故事</#if>"
                                                   name="story_title" id="story_title" class="input"><span
                                                    style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                                        </div>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label" for="productName"><span
                                                    style="color: red;font-size: 20px;">*</span>模块图片：</label>
                                        <div class="controls">
                                            <input type="text" value="<#if story_img??>${story_img!''}</#if>"
                                                   name="story_cover_img_url" id="story_cover_img_url" class="input"
                                                   disabled="disabled">
                                            <input class="upload_file" type="file" data-suffix="jpg#png">
                                            <a class="btn btn-success preview"
                                               data-href="<#if story_img?? && cdn_host??>${cdn_host!''}${story_img!''}</#if>">预览</a>
                                        </div>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label" for="productName"><span
                                                    style="color: red;font-size: 20px;">*</span>模块内容：</label>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label" for="productName"><span
                                                    style="color: red;font-size: 20px;">*</span>上传视频：</label>
                                        <div class="controls">
                                            <input type="text"
                                                   value="<#if story_content??>${story_content.videoUrl!''}</#if>"
                                                   name="story_content_video_url" id="story_content_video_url"
                                                   class="input"
                                                   disabled="disabled">
                                            <input class="upload_file" type="file" data-suffix="mp4">
                                            <a class="btn btn-success preview"
                                               data-href="<#if story_content?? && cdn_host??>${cdn_host!''}${story_content.videoUrl!''}</#if>">预览</a>
                                        </div>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label" for="productName"><span
                                                    style="color: red;font-size: 20px;">*</span>视频时长：</label>
                                        <div class="controls">
                                            <input type="text"
                                                   value="<#if story_content??>${story_content.videoSeconds!0}</#if>"
                                                   name="story_content_video_seconds" id="story_content_video_seconds"
                                                   class="input">秒
                                        </div>
                                    </div>
                                </div>
                            </fieldset>
                        </div>
                        <div id="CHINESESTORYKNOWLEDGE" class="modal_content">
                            <input type="hidden" name="rank" readonly="readonly">
                            <input type="hidden" id="knowledge_model_id" readonly="readonly"
                                   value="<#if knowledge_content??>${knowledge_content.id!''}<#else ></#if>">
                            <label>
                                <strong>知识点：</strong>
                                <a class="btn btn-inverse del_modal">删除</a>
                                <a class="up" style="cursor: pointer">上移</a>
                                <a class="down" style="cursor: pointer">下移</a>
                            </label>
                            <fieldset>
                                <div class="knowledge_points">
                                    <div class="control-group">
                                        <label class="control-label" for="productName"><span
                                                    style="color: red;font-size: 20px;">*</span>模块标题：</label>
                                        <div class="controls">
                                            <input type="text"
                                                   value="<#if knowledge_title??>${knowledge_title!''}<#else >知识点</#if>"
                                                   name="knowledge_points_title" id="knowledge_points_title"
                                                   class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                                        </div>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label" for="productName"><span
                                                    style="color: red;font-size: 20px;">*</span>模块图片：</label>
                                        <div class="controls">
                                            <input type="text" value="<#if knowledge_img??>${knowledge_img!''}</#if>"
                                                   name="knowledge_cover_img_url" id="knowledge_cover_img_url"
                                                   class="input"
                                                   disabled="disabled">
                                            <input class="upload_file" type="file" data-suffix="jpg#png">
                                            <a class="btn btn-success preview"
                                               data-href="<#if knowledge_img?? && cdn_host??>${cdn_host!''}${knowledge_img!''}</#if>">预览</a>
                                        </div>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label" for="productName"><span
                                                    style="color: red;font-size: 20px;">*</span>模块内容：</label>
                                        <a id="clone_knowledge_content" class="btn btn-small btn-warning">新增模块内容
                                        </a>
                                    </div>
                                    <div class="knowledge_points_content">
                                        <div class="control-group knowledge_title">
                                            <label class="control-label knowledge_title_label" for="productName"><span
                                                        style="color: red;font-size: 20px;">*</span>模块内容1：</label>
                                            <a name="del_knowledge_content" class="btn btn-small btn-danger">删除模块内容</a>
                                        </div>
                                        <div class="control-group">
                                            <label class="control-label" for="productName"><span
                                                        style="color: red;font-size: 20px;">*</span>背景图片：</label>
                                            <div class="controls">
                                                <input type="text" value=""
                                                       name="content_cover_img_url" class="input"
                                                       disabled="disabled">
                                                <input class="upload_file" type="file" data-suffix="jpg#png">
                                                <a class="btn btn-success preview img-preview"
                                                   data-href="">预览</a>
                                            </div>
                                        </div>
                                        <div class="control-group">
                                            <label class="control-label" for="productName"><span
                                                        style="color: red;font-size: 20px;">*</span>上传音频：</label>
                                            <div class="controls">
                                                <input type="text"
                                                       value=""
                                                       name="content_audio_url"
                                                       class="input" disabled="disabled">
                                                <input class="upload_file" type="file" data-suffix="mp3">
                                                <a class="btn btn-success preview audio-preview"
                                                   data-href="">预览</a>
                                            </div>
                                        </div>
                                        <div class="control-group">
                                            <label class="control-label" for="productName"><span
                                                        style="color: red;font-size: 20px;">*</span>音频时长：</label>
                                            <div class="controls">
                                                <input type="text"
                                                       value=""
                                                       name="content_audio_seconds"
                                                       class="input">秒
                                            </div>
                                        </div>
                                        <div class="control-group">
                                            <label class="control-label" for="productName"><span
                                                        style="color: red;font-size: 20px;">*</span>知识点标题：</label>
                                            <div class="controls">
                                                <input type="text"
                                                       value=""
                                                       name="content_title"
                                                       class="input" maxlength="10" placeholder="10字以内">
                                            </div>
                                        </div>
                                        <div class="control-group">
                                            <label class="control-label">文本内容：</label>
                                            <div class="controls">
                                                <label for="title" class="text_content">
                                                    <!-- 加载编辑器的容器 -->
                                                    <script id="content_container" type="text/plain"></script>
                                                </label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </fieldset>
                        </div>
                        <div id="CHINESESTORYEXPAND" class="modal_content">
                            <input type="hidden" name="rank" readonly="readonly">
                            <input type="hidden" id="expand_model_id" readonly="readonly"
                                   value="<#if expound_content??>${expound_content.id!''}<#else ></#if>">
                            <label>
                                <strong>拓展学习：</strong>
                                <a class="btn btn-inverse del_modal">删除</a>
                                <a class="up" style="cursor: pointer">上移</a>
                                <a class="down" style="cursor: pointer">下移</a>
                            </label>
                            <fieldset>
                                <div class="follow_reading_content">
                                    <div class="control-group">
                                        <label class="control-label" for="productName"><span
                                                    style="color: red;font-size: 20px;">*</span>模块标题：</label>
                                        <div class="controls">
                                            <input type="hidden" id="expand_study_id"
                                                   name="expand_study_id"
                                                   value="<#if expound_content??>${expound_content.id!''}</#if>">
                                            <input type="text"
                                                   value="<#if expound_title??>${expound_title!''}<#else >趣味练习</#if>"
                                                   name="expand_study_title" id="expand_study_title" class="input"><span
                                                    style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                                        </div>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label" for="productName"><span
                                                    style="color: red;font-size: 20px;">*</span>模块图片：</label>
                                        <div class="controls">
                                            <input type="text" value="<#if expound_img??>${expound_img!''}</#if>"
                                                   name="expand_cover_img_url" id="expand_cover_img_url" class="input"
                                                   disabled="disabled">
                                            <input class="upload_file" type="file" data-suffix="jpg#png">
                                            <a class="btn btn-success preview"
                                               data-href="<#if expound_img?? && cdn_host??>${cdn_host!''}${expound_img!''}</#if>">预览</a>
                                        </div>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label" for="productName">模块内容：</label>
                                    </div>
                                    <div class="controls">
                                        <label style="margin-top:  5px; ">
                                            <input type="radio" name="use_practice_or_read_radio" value="1"
                                                   <#if (picture_book_reading_content??)|| (!(picture_book_reading_content??) && !(famous_book_content??))>checked="checked"</#if>><span>趣味练习</span>
                                            <input type="radio" name="use_practice_or_read_radio" value="2"
                                                   <#if famous_book_content??>checked="checked"</#if>><span>朗读背诵</span>
                                        </label>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label" for="productName"><span
                                                    style="color: red;font-size: 20px;">*</span>题目ID：</label>
                                        <div class="controls">
                                        <textarea style="width: 300px;height: 100px;"
                                                  name="expand_content_question_ids"
                                                  id="expand_content_question_ids"
                                                  placeholder="用#分割，排列顺序即为前端展示顺序"><#if expound_content?? && expound_content.questionIds?has_content && expound_content.questionIds?size gt 0>${(expound_content.questionIds![])?join("#")}</#if></textarea>
                                        </div>
                                    </div>
                                </div>
                            </fieldset>
                        </div>
                    </div>
                </fieldset>
                <div class="control-group">
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
<script type="text/javascript">
    function openUploadModel() {
        $("#uploading").show();
        $("#loading").show();
    }

    function closeUploadModel() {
        $("#uploading").hide();
        $("#loading").hide();
    }

    $(function () {
        var detachObject = {};
        //用model_list来控制模块顺序和是否展示
        var allModels = ['CHINESESTORY', 'CHINESESTORYKNOWLEDGE', 'CHINESESTORYEXPAND'];
        var showModels = [];
        <#if model_rank_list??&&model_rank_list?size gt 0>
        <#list model_rank_list as content>
        var content_id = "${content!''}";
        showModels.push(content_id);
        </#list>
        </#if>
        if (showModels.length > 0) {
            $.each(allModels, function (i, val) {
                if ($.inArray(val, showModels) < 0) {
                    detachObject[val] = hideModel(val);
                }
            });
            var sort_modal = $(".modal_content").sort(function (model1, model2) {
                var model1_id = model1.id;
                var model2_id = model2.id;
                return showModels.indexOf(model1_id) > showModels.indexOf(model2_id) ? 1 : -1;
            });
            $(".all-model-div").empty().append(sort_modal);
        }
        //给ue记个数，添加删除内容的时候用
        var ueCount = 0;
        //把ue都放到这个list里
        var ue_list = [];
        var ue_content = [];
        if ($("#CHINESESTORYKNOWLEDGE").length !== 0) {
            var ue_first = UE.getEditor('content_container', {
                serverUrl: "ueditorcontroller.vpage",
                zIndex: 1040,
                fontsize: [12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34],
                toolbars: [[
                    'fullscreen', 'source', '|', 'undo', 'redo', '|',
                    'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                    'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                    'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                    'directionalityltr', 'directionalityrtl', 'indent', '|',
                    'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                    'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                    'simpleupload', 'pagebreak', '|',
                    'horizontal', 'date', 'time', 'spechars', '|', 'preview', 'searchreplace'
                ]]
            });
            ueCount += 1;
            ue_list.push(ue_first);
        }

        <#if knowledge_content??&&knowledge_content.knowledgePointList??&&knowledge_content.knowledgePointList?size gt 0>
        <#list knowledge_content.knowledgePointList as content>
        <#if (content_index?number == 0)>
        $(".knowledge_points_content input[name='content_cover_img_url']").val("${content.bgImgUrl!''}");
        $(".knowledge_points_content input[name='content_audio_url']").val("${content.audioUrl!''}");
        $(".knowledge_points_content input[name='content_audio_seconds']").val("${content.audioSeconds!''}");
        $(".knowledge_points_content .img-preview").attr("data-href", "${cdn_host!''}${content.bgImgUrl!''}");
        $(".knowledge_points_content .audio-preview").attr("data-href", "${cdn_host!''}${content.audioUrl!''}");
        $(".knowledge_points_content input[name='content_title']").val("${content.title!''}");
        <#if content??&&content.content??>
        ue_first.ready(function () {
            var firstContent = '${content.content!''}';
            if (firstContent) {
                ue_first.setContent(firstContent.replace(/\n/g, '<p><br/></p>'));
            }
        });
        </#if>
        <#else>
        var cloneDiv = $(".knowledge_points_content:first").clone();
        ueCount += 1;
        var initId = "content_container" + ueCount;
        cloneDiv.find("#content_container").each(function () {
            $(this).remove();
            cloneDiv.find(".text_content").each(function () {
                var script = document.createElement('script');
                script.type = 'text/plain';
                script.id = initId;
                $(this).append(script);
            });
        });
        cloneDiv.find("input,textarea").each(function () {
            $(this).val("");
        });
        cloneDiv.find("input[name='content_cover_img_url']").val("${content.bgImgUrl!''}");
        cloneDiv.find("input[name='content_audio_url']").val("${content.audioUrl!''}");
        cloneDiv.find("input[name='content_audio_seconds']").val("${content.audioSeconds!''}");
        cloneDiv.find(".img-preview").attr("data-href", "${cdn_host!''}${content.bgImgUrl!''}");
        cloneDiv.find(".audio-preview").attr("data-href", "${cdn_host!''}${content.audioUrl!''}");
        cloneDiv.find("input[name='content_title']").val("${content.title!''}");
        $(".knowledge_points").append(cloneDiv.clone(false));
        $(".knowledge_points_content").find(".knowledge_title_label").each(function (index) {
            index += 1;
            $(this).text("模块内容" + index + "：");
        });
        ue_list.push(initUeditor(initId));
        <#if content??&&content.content??>
        ue_content.push('${content.content!''}');
        </#if>
        </#if>
        </#list>
        </#if>

        $.each(ue_content, function (index, value) {
            ue_list[index + 1].ready(function () {
                ue_list[index + 1].setContent(value.replace(/\n/g, '<p><br/></p>'));
            });
        });

        //模板拓展学习-输入项切换
        $("input[name=use_practice_or_read_radio]").change(function () {
            var that = $("input[name=use_practice_or_read_radio]:checked");
            var text = that.next("span").text();
            $("#expand_study_title").val(text);
        });

        //上传单个图片或者音频
        $(document).on('change', '.upload_file', function () {
            var $this = $(this);
            var suffix = $this.val().split('.').pop().toLowerCase();
            if (isChineseChar($this.val())) {
                alert("文件名中不可包含中文！");
                //重置成空。保证连续选择同一个文件能触发上传
                $this.val("");
                return;
            }
            if ($this.val() != '') {
                var acceptSuffix = new String($this.attr("data-suffix")).split("#");
                if (acceptSuffix.indexOf(suffix) === -1) {
                    alert("仅支持以下文件格式" + acceptSuffix);
                    //重置成空。保证连续选择同一个文件能触发上传
                    $this.val("");
                    return;
                }
                //限制30M
                if ($this[0].files[0].size > 31457280) {
                    alert("您选择的文件大小超过30M");
                    //重置成空。保证连续选择同一个文件能触发上传
                    $this.val("");
                    return;
                }
                var formData = new FormData();
                formData.append('inputFile', $this[0].files[0]);
                openUploadModel();
                $.ajax({
                    url: '/opmanager/studyTogether/template/upload_signal_file_to_oss.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        //重置成空。保证连续选择同一个文件能触发上传
                        $this.val("");
                        closeUploadModel();
                        if (data.success) {
                            $($this.closest('.controls').find("input.input")).val(data.fileName);
                            $($this.closest('.controls').find("a.btn-success")).attr("data-href", data.fileUrl);
                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });

        //批量上传音频
        $(".upload_img_audio_batch").change(function () {
            var $this = $(this);
            var acceptSuffix = new String($this.attr("data-suffix")).split("#");
            if ($this.val() != '') {
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
                    url: '/opmanager/studyTogether/template/batch_upload_file_to_oss.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        closeUploadModel();
                        if (data.success) {
                            var file_list = data.file_list;
                            for (var i = 0; i < file_list.length; i++) {
                                var file = file_list[i];
                                var file_url = "<td><div class='input-group'><input type='text' class='form-control' dir='rtl' value='" + file.fileName + "' disabled='disabled'/>" +
                                    "<a class='btn btn-success preview' data-href='" + file.fileUrl + "'>预览</a></div></td>";
                                var secondsInput = "<td><div class='input-group'><input type='text' class='form-control' style='width: 70px;' value='' /></div></td>";
                                var sentenceInput = "<td><div class='input-group'><input type='text' class='form-control' value='' /></div></td>";
                                var indexInput = "<td><div class='input-group'><input type='text' class='form-control' style='width: 70px;' value='' /></div></td>";
                                var deleteButton = "<td><div class='input-group'> <a class='btn btn-warning delete_sentence'>删除</a> </div> </td>"
                                $("#recite_file_list").append("<tr>" + file_url + secondsInput + sentenceInput + indexInput + deleteButton + "</tr>");
                            }
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

        $("#template_spu_id").blur(function () {
            var spuId = $("#template_spu_id").val();
            if (spuId) {
                $.get("/opmanager/studytogether/common/spu_name.vpage", {spuId: spuId}, function (data) {
                    if (data.success) {
                        $("#spu_name").html(data.spuName);
                    } else {
                        alert(spuId + "对应的SPU不存在");
                        $("#template_spu_id").val("");
                        $("#spu_name").html("");
                        return;
                    }
                });
            } else {
                $("#template_spu_id").val("");
                $("#spu_name").html("");
                return;
            }
        });

        //删除一组上传的文件
        $(document).on("click", ".delete_sentence", function () {
            $(this).closest("tr").remove();
        });

        //复制知识点模块内容
        $(document).on('click', "#clone_knowledge_content", function () {
            var cloneDiv = $(".knowledge_points_content:first").clone();
            ueCount += 1;
            var initId = "content_container" + ueCount;
            cloneDiv.find("#content_container").each(function () {
                $(this).remove();
                cloneDiv.find(".text_content").each(function () {
                    var script = document.createElement('script');
                    script.type = 'text/plain';
                    script.id = initId;
                    $(this).append(script);
                });
            });
            cloneDiv.find("input,textarea").each(function () {
                $(this).val("");
            });
            $(".knowledge_points").append(cloneDiv.clone(false));
            $(".knowledge_points_content").find(".knowledge_title_label").each(function (index) {
                index += 1;
                $(this).text("模块内容" + index + "：");
            });
            var ue = initUeditor(initId);
            ue_list.push(ue);
        });

        //删除知识点模块内容
        $(document).on('click', "a[name='del_knowledge_content']", function () {
            var that = $(".knowledge_points_content");
            var length = that.length;
            if (length < 2) {
                alert("就剩一个了哦~~~~");
                return false;
            }
            $(this).parent().parent(".knowledge_points_content").remove();
            that.find(".knowledge_title_label").each(function (index) {
                index += 1;
                $(this).text("模块内容" + index + "：");
            });
        });

        //添加模块
        $("#add_modal_type").on('click', function () {
            var select = $("#modal_type");
            var modalVal = select.val();
            var realModal = detachObject[modalVal];
            if (realModal.attr("id") === "CHINESESTORYKNOWLEDGE") {
                ueCount += 1;
                var initId = "content_container";
                var ue = initUeditor(initId);
                ue_list.push(ue);
            }
            $(".all-model-div").append(realModal);
            // $("#" + modalVal).css("display", "block");
            select.find("option[value='" + modalVal + "']").remove();
        });

        //删除模块
        $(".all-model-div").on('click', ".del_modal", function () {
            var that = $(".modal_content:visible");
            var length = that.length;
            if (length < 2) {
                alert("就剩一个了哦~~~~");
                return false;
            }
            var modal = $(this).parent().parent();
            var detachModal = modal.detach();
            // modal.css("display", "none");
            var id = modal.attr("id");
            console.log(id);
            detachObject[id] = detachModal;
            var title = generateModalTitle(id);
            if (!title) {
                alert("模块删除错误，请刷新");
                return;
            }
            var html = '<option value="' + id + '">' + title + '</option>';
            $("#modal_type").append(html);
        });

        //保存语文故事模版
        $("#save_info_button").on("click", function () {

            if ($("#template_name").val().trim().length > 50) {
                alert("模板名称不能超过50个字");
                return;
            }

            var template_info = {
                id: $("#template_id").val(),
                spuId: $("#template_spu_id").val().trim(),
                name: $("#template_name").val().trim(),
                reportTitle: $("#template_title").val().trim(),
                coverImgUrl: $("#template_imgUrl").val().replace(/\s+/g, ""),
                introduction: $("#template_introduction").val().trim()
            };
            //故事
            var story_info = {};
            if ($("#CHINESESTORY").is(":visible")) {
                if ($("#story_title").val().trim().length > 7) {
                    alert("故事模块模块标题不能超过7个字");
                    return;
                }
                story_info = {
                    id: $("#story_model_id").val(),
                    story_title: $("#story_title").val().trim(),
                    coverImgUrl: $("#story_cover_img_url").val().replace(/\s+/g, ""),
                    videoUrl: $("#story_content_video_url").val().replace(/\s+/g, ""),
                    videoSeconds: $("#story_content_video_seconds").val().replace(/\s+/g, "")
                };
            }

            //知识点
            var knowledge_info = {};
            if ($("#CHINESESTORYKNOWLEDGE").is(":visible")) {
                if ($("#knowledge_points_title").val().trim().length > 7) {
                    alert("知识点模块标题不能超过7个字");
                    return;
                }
                var knowledge_point_list = [];
                $(".knowledge_points_content").each(function (index) {
                    var content_img = $(this).find('input[name="content_cover_img_url"]').val();
                    var content_audio = $(this).find('input[name="content_audio_url"]').val();
                    var content_audio_second = $(this).find('input[name="content_audio_seconds"]').val();
                    var content_title = $(this).find('input[name="content_title"]').val();
                    var content_text = ue_list[index].getContent();
                    var content = {
                        bgImgUrl: content_img,
                        audioUrl: content_audio,
                        audioSeconds: content_audio_second,
                        content: content_text,
                        title: content_title
                    };
                    knowledge_point_list.push(content);
                });
                knowledge_info = {
                    id: $("#knowledge_model_id").val(),
                    knowledge_title: $("#knowledge_points_title").val().trim(),
                    coverImgUrl: $("#knowledge_cover_img_url").val().replace(/\s+/g, ""),
                    knowledgePointList: knowledge_point_list
                };
            }
            //拓展学习
            var expand_info = {};
            if ($("#CHINESESTORYEXPAND").is(":visible")) {
                var questionIds = [];
                if ($("#expand_content_question_ids").val().trim().length > 0) {
                    questionIds = $("#expand_content_question_ids").val().replace(/\s+/g, "").split("#");
                }
                $.each(questionIds, function (index, value) {
                    if (!value) {
                        questionIds.pop(index);
                    }
                });
                if (questionIds.length === 0) {
                    alert("拓展学习题目ID不能为空");
                    return;
                }
                if ($("#expand_study_title").val().trim().length > 7) {
                    alert("拓展学习模块标题不能超过7个字");
                    return;
                }
                expand_info = {
                    id: $("#expand_model_id").val(),
                    expound_title: $("#expand_study_title").val().trim(),
                    coverImgUrl: $("#expand_cover_img_url").val().replace(/\s+/g, ""),
                    questionIds: questionIds
                };
            }
            var rank_info = [];
            $("input[name='rank']").each(function () {
                var modal_content_id = $(this).closest(".modal_content").attr("id");
                rank_info.push(modal_content_id);
            });
            console.log(template_info);
            console.log(story_info);
            console.log(knowledge_info);
            console.log(expand_info);
            console.log(rank_info);
            //保存
            $.post("save_chinese_story.vpage", {
                story_info: JSON.stringify(story_info),
                knowledge_info: JSON.stringify(knowledge_info),
                expand_info: JSON.stringify(expand_info),
                template_info: JSON.stringify(template_info),
                rank_info: JSON.stringify(rank_info)
                // recite_info: JSON.stringify(recite_info),
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


        //上移
        $(document).on('click', '.up', function () {
            var $tr = $(this).closest(".modal_content");
            var index = $tr.index(".modal_content");
            if (index !== 0) {
                $tr.fadeOut().fadeIn();
                $tr.prev().before($tr);
                sortTable();
            }
        });
        //下移
        // var len = $down.length;
        $(document).on('click', '.down', function () {
            var $tr = $(this).closest(".modal_content");
            // console.log($tr.index());
            $tr.fadeOut().fadeIn();
            $tr.next().after($tr);
            sortTable();
        });

    });


    function initUeditor(initId) {
        return UE.getEditor(initId, {
            serverUrl: "ueditorcontroller.vpage",
            zIndex: 1040,
            fontsize: [12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', '|',
                'horizontal', 'date', 'time', 'spechars', '|', 'preview', 'searchreplace'
            ]]
        });
    }

    function generateModalTitle(id) {
        if (id === 'CHINESESTORY') {
            return '故事';
        } else if (id === 'CHINESESTORYKNOWLEDGE') {
            return '知识点';
        } else if (id === 'CHINESESTORYEXPAND') {
            return '拓展学习';
        } else if (id === 'CHINESESTORYFOLLOWREAD') {
            return '句子跟读';
        } else {
            return '';
        }
    }


    function sortTable() {
        $(".modal_content").each(function (index) {
            $(this).find("input[name='rank']").val(index + 1);
        })
    }

    function hideModel(id) {
        var modal = $("#" + id);
        if (!modal) {
            alert("初始化错误，请刷新");
        }
        // modal.css("display", "none");
        var detachModal = modal.detach();
        console.log(id);
        var title = generateModalTitle(id);
        if (!title) {
            alert("初始化错误，请刷新");
            return;
        }
        var html = '<option value="' + id + '">' + title + '</option>';
        $("#modal_type").append(html);
        return detachModal;
    }

    function isChineseChar(str) {
        var reg = /[\u4E00-\u9FA5\uF900-\uFA2D]/;
        return reg.test(str);
    }
</script>
</@layout_default.page>