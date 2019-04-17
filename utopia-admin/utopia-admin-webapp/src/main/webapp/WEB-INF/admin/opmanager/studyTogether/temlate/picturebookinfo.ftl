<#import "../../../layout_default.ftl" as layout_default />
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
            <#if template?? && template.id?has_content>编辑<#else >新增</#if>英语绘本模板
        <#else >
            模板详情英语绘本
        </#if>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <legend class="field-title">模板基础信息</legend>
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>SPU_ID：</label>
                        <div class="controls">
                            <input type="text" value="<#if template??>${template.spuId!''}</#if>" name="template_spu_id" id="template_spu_id" class="input">
                            <span id="spu_name"></span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模板名称：</label>
                        <div class="controls">
                            <input type="text" value="<#if template??>${template.name!''}</#if>" name="template_name" id="template_name" class="input" placeholder="50字以内">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">创建人：</label>
                        <div class="controls" style="margin-top: 5px;"><#if template??>${template.createUser!''}<#else >${admin_user!''}</#if></div>
                    </div>
                </fieldset>
            </form>
            <form class="well form-horizontal" style="background-color: #fff;">
                <input type="hidden" id="template_id" name="template_id" value="<#if template??>${template.id!''}</#if>">
                <legend class="field-title">课节目标</legend>
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>标题：</label>
                        <div class="controls">
                            <input type="text" value="<#if template??>${template.title!''}<#else >学习目标</#if>" name="template_title" id="template_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制4个字以内</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>目标描述：</label>
                        <div class="controls">
                            <input type="text" value="<#if template??>${template.goalDetail!''}</#if>" name="template_goal_detail" id="template_goal_detail" class="input">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>内容：</label>
                        <div class="controls">
                            <label style="margin-top:  5px; ">
                                <input type="radio" name="template_use_picture_book_id_radio"  value="1" <#if (template?? && template.pictureBookId?has_content)|| !(template??)>checked="checked"</#if>>绘本ID
                            </label>
                            <label style="margin-top:  5px; ">
                                <input type="radio" name="template_use_picture_book_id_radio"  value="2" <#if template?? &&  !(template.pictureBookId?has_content)>checked="checked"</#if>>文本+图片+音频
                            </label>
                        </div>
                    </div>
                    <div class="template_picture_book_info_use_id">
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>绘本ID：</label>
                            <div class="controls">
                                <input type="text" value="<#if template??>${template.pictureBookId!''}</#if>" name="template_picture_book_id" id="template_picture_book_id" class="input">
                            </div>
                        </div>
                    </div>
                    <div class="template_picture_book_info_input">
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>绘本封面：</label>
                            <div class="controls">
                                <input type="text" value="<#if template??>${template.coverImgUrl!''}</#if>" name="template_cover_img_url" id="template_cover_img_url" class="input" disabled="disabled">
                                <input class="upload_file" type="file" data-suffix="jpg#png">
                                    <a class="btn btn-success preview"   data-href="<#if template?? && cdn_host??>${cdn_host!''}${template.coverImgUrl!''}"</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>图片音频列表：</label>
                            <div class="controls">
                                <input class="upload_img_audio_batch" type="file" multiple="multiple" data-suffix="jpg#png#mp3"><span style="color: red">图片命名和音频命名需一致,且不能包含中文</span>
                            </div>
                        </div>
                        <table class="table table-bordered">
                            <thead>
                            <tr>
                                <th><span style="color: red;font-size: 20px;">*</span>图片列表</th>
                                <th><span style="color: red;font-size: 20px;">*</span>音频列表</th>
                                <th><span style="color: red;font-size: 20px;">*</span>音频时长/S</th>
                                <th><span style="color: red;font-size: 20px;">*</span>文本内容</th>
                                <th><span style="color: red;font-size: 20px;">*</span>展示顺序</th>
                            </tr>
                            </thead>
                            <tbody id="picture_book_sentence_file_list" class="batch_file_list">
                                <#if template?? && template.sentenceList?has_content && template.sentenceList?size gt 0>
                                    <#list template.sentenceList as sentence>
                                    <tr>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control"  value="${sentence.imgUrl!''}" dir='rtl' disabled="disabled"/>
                                                <a class="btn btn-success preview" data-href="${cdn_host!''}${sentence.imgUrl!''}">预览</a>
                                            </div>
                                        </td>
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
                                                <#assign template_signal_sentence_content = sentence.sentence/>
                                                <input type="text" class="form-control" value="<#escape template_signal_sentence_content as template_signal_sentence_content?html>${template_signal_sentence_content!''}</#escape>"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control" style="width: 70px;" value="${sentence_index +1}"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <a class="btn btn-warning delete_sentence">删除</a>
                                            </div>
                                        </td>
                                    </tr>
                                    </#list>
                                </#if>
                            </tbody>
                        </table>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">内容说明：</label>
                        <div class="controls">
                            <input type="text" value="<#if template??>${template.comment!''}</#if>" name="template_comment" id="template_comment" class="input">
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
                                <input type="text" value="绘本名师精讲"  disabled = "disabled" class="input">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块标题：</label>
                            <div class="controls">
                                <input type="hidden" id="expound_content_id" name="expound_content_id" value="<#if expound_content??>${expound_content.id!''}</#if>">
                                <input type="text" value="<#if expound_title??>${expound_title!''}<#else >绘本名师精讲</#if>" name="expound_title" id="expound_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块内容：</label>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>上传视频：</label>
                            <div class="controls">
                                <input type="text" value="<#if expound_content??>${expound_content.videoUrl!''}</#if>" name="expound_content_video_url" id="expound_content_video_url" class="input" disabled="disabled">
                                <input class="upload_file" type="file" data-suffix="mp4">
                                <a class="btn btn-success preview" data-href="<#if expound_content?? && cdn_host??>${cdn_host!''}${expound_content.videoUrl!''}</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>视频时长：</label>
                            <div class="controls">
                                <input type="text" value="<#if expound_content??>${expound_content.videoSeconds!0}</#if>" name="expound_content_video_seconds" id="expound_content_video_seconds" class="input">秒
                            </div>
                        </div>
                    </fieldset>
                    <label><strong>模块2：</strong></label>
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块类型：</label>
                            <div class="controls">
                                <input type="text" value="核心单词练习"  disabled = "disabled" class="input">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块标题：</label>
                            <div class="controls">
                                <input type="hidden" id="word_content_id" name="word_content_id" value="<#if word_content??>${word_content.id!''}</#if>">
                                <input type="text" value="<#if word_title??>${word_title!''}<#else >核心单词练习</#if>" name="word_title" id="word_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块内容：</label>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>题目ID列表：</label>
                            <div class="controls">
                                <textarea style="width: 300px;height: 100px;" name="word_questionIds" id="word_questionIds" placeholder="题目ID用#分割"><#if word_content?? && word_content.questionIds?has_content && word_content.questionIds?size gt 0>${(word_content.questionIds![])?join("#")}</#if></textarea>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>单词列表：</label>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>上传图片+音频：</label>
                            <div class="controls">
                                <input class="upload_img_audio_batch" type="file" multiple="multiple" data-suffix="jpg#png#mp3"><span style="color: red">图片命名和音频命名需一致,且不能包含中文</span>
                            </div>
                        </div>
                        <table class="table table-bordered">
                            <thead>
                            <tr>
                                <th><span style="color: red;font-size: 20px;">*</span>图片列表</th>
                                <th><span style="color: red;font-size: 20px;">*</span>音频列表</th>
                                <th><span style="color: red;font-size: 20px;">*</span>单词名称</th>
                                <th><span style="color: red;font-size: 20px;">*</span>单词音标</th>
                                <th><span style="color: red;font-size: 20px;">*</span>单词词性</th>
                                <th><span style="color: red;font-size: 20px;">*</span>单词释义</th>
                                <th><span style="color: red;font-size: 20px;">*</span>展示顺序</th>
                            </tr>
                            </thead>
                            <tbody id="word_file_list" class="batch_file_list">
                                <#if word_content?? && word_content.words?has_content && word_content.words?size gt 0>
                                    <#list word_content.words as word>
                                    <tr>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control"  value="${word.imgUrl!''}"  dir='rtl' disabled="disabled"/>
                                                <a class="btn btn-success preview" data-href="${cdn_host!''}${word.imgUrl!''}">预览</a>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control"  value="${word.audioUrl!''}"  dir='rtl' disabled="disabled"/>
                                                <a class="btn btn-success preview" data-href="${cdn_host!''}${word.audioUrl!''}">预览</a>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control" style="width: 70px;" value="${word.word!''}"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control" style="width: 70px;" value="${word.pronunciation!''}"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control" style="width: 70px;" value="${word.wordClass!''}"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control" style="width: 70px;" value="${word.paraphrase!''}"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control" style="width: 70px;" value="${word_index +1}"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <a class="btn btn-warning delete_sentence">删除</a>
                                            </div>
                                        </td>
                                    </tr>
                                    </#list>
                                </#if>

                            </tbody>
                        </table>
                    </fieldset>
                    <label><strong>模块3：</strong></label>
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块类型：</label>
                            <div class="controls">
                                <input type="text" value="重点句子跟读"  disabled = "disabled" class="input">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块标题：</label>
                            <div class="controls">
                                <input type="hidden" id="sentence_content_id" name="sentence_content_id" value="<#if sentence_content??>${sentence_content.id!''}</#if>">
                                <input type="text" value="<#if sentence_title??>${sentence_title!''}<#else >重点句子跟读</#if>" name="sentence_title" id="sentence_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块内容：</label>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>题目ID列表：</label>
                            <div class="controls">
                                <textarea style="width: 300px;height: 100px;" name="sentence_questionIds" id="sentence_questionIds" placeholder="题目ID用#分割"><#if sentence_content?? && sentence_content.questionIds?has_content && sentence_content.questionIds?size gt 0>${(sentence_content.questionIds![])?join("#")}</#if></textarea>
                            </div>
                        </div>
                    </fieldset>
                    <label><strong>模块4：</strong></label>
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块类型：</label>
                            <div class="controls">
                                <input type="text" value="每日绘本配音"  disabled = "disabled" class="input">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块标题：</label>
                            <div class="controls">
                                <input type="hidden" id="fun_content_id" name="fun_content_id" value="<#if fun_content??>${fun_content.id!''}</#if>">
                                <input type="text" value="<#if fun_title??>${fun_title!''}<#else >每日绘本配音</#if>" name="fun_title" id="fun_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName">模块内容：</label>
                        </div>
                        <div class="fun_picture_book_info_input">
                            <div class="control-group">
                                <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>绘本封面：</label>
                                <div class="controls">
                                    <input type="text" value="默认同课节目标一致" name="fun_cover_img_url" id="fun_cover_img_url" disabled="disabled" class="input">
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>图片音频列表：</label>
                                <div class="controls">
                                    <input type="text" value="默认同课节目标一致" name="fun_sentence_list" id="fun_sentence_list" disabled="disabled" class="input">
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>关键词数量：</label>
                                <div class="controls">
                                    <input type="text" value="<#if fun_content??>${fun_content.keyWordCount!''}</#if>" name="fun_content_key_word_count" id="fun_content_key_word_count" class="input">
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>句子数量：</label>
                                <div class="controls">
                                    <input type="text" value="<#if fun_content??>${fun_content.sentenceCount!''}</#if>" name="fun_content_sentence_count" id="fun_content_sentence_count" class="input">
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" for="productName"><span style="color: red;font-size: 15px;">*绘本推荐阅读时长</span>：</label>
                                <div class="controls">
                                    <input type="text" value="<#if fun_content??>${fun_content.generalTimeString!''}</#if>" name="fun_content_general_time_string" id="fun_content_general_time_string" class="input">min
                                </div>
                            </div>
                        </div>

                        <div class="fun_picture_book_info_use_id">
                            <div class="control-group">
                                <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>绘本ID：</label>
                                <div class="controls">
                                    <input type="text" value="默认同课节目标一致" name="fun_picture_book_id" id="fun_picture_book_id" disabled="disabled"  class="input">
                                </div>
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
        var pictureBookId = "<#if !(template??) ||template.pictureBookId?has_content>true</#if>";
        if (pictureBookId) {
            $(".template_picture_book_info_use_id").show();
            $(".template_picture_book_info_input").hide();
        } else {
            $(".template_picture_book_info_use_id").hide();
            $(".template_picture_book_info_input").show();
        }
        //模块4
        var funPictureBookId = "<#if !(fun_content??) ||fun_content.pictureBookId?has_content>true</#if>";
        if (funPictureBookId) {
            $(".fun_picture_book_info_use_id").show();
            $(".fun_picture_book_info_input").hide();
        } else {
            $(".fun_picture_book_info_use_id").hide();
            $(".fun_picture_book_info_input").show();
        }
        //模板课节目标-输入项切换
        $("input[name=template_use_picture_book_id_radio]").change(function () {
            var value = $("input[name=template_use_picture_book_id_radio]:checked").val();
            if (value === "1") {
                $(".template_picture_book_info_use_id").show();
                $(".template_picture_book_info_input").hide();
                //模块4联动
                $(".fun_picture_book_info_use_id").show();
                $(".fun_picture_book_info_input").hide();
            } else if (value === "2") {
                $(".template_picture_book_info_use_id").hide();
                $(".template_picture_book_info_input").show();
                //模块4联动
                $(".fun_picture_book_info_use_id").hide();
                $(".fun_picture_book_info_input").show();
            } else {
                alert("类型错误");
            }
        });

        //上传单个图片或者音频
        $(".upload_file").change(function () {
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
                    url: '/opmanager/studyTogether/template/upload_signal_file_to_oss.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        closeUploadModel();
                        if (data.success) {
                            $($this.closest('.controls').find("input.input")).attr("value", data.fileName);
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
                if(inputFiles.length%2 != 0){
                    alert("选择文件不匹配，必须是是一个图片对应一个音频，并且文件名称必须一样。请重新选择");
                    return;
                }
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
                    url: '/opmanager/studyTogether/template/batch_upload_file_to_oss_and_group.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        closeUploadModel();
                        if (data.success) {
                            var file_map = data.file_map;
                            for (var file_name in  file_map) {
                                var file_list = file_map[file_name];
                                var img_file = file_list[0];
                                var audio_file = file_list[1];
                                //图片
                                var img_url = "<td><div class='input-group'><input type='text' class='form-control' dir='rtl' value='" + img_file.fileName + "' disabled='disabled'/>" +
                                        "<a class='btn btn-success preview'  data-href='" + img_file.fileUrl + "'>预览</a></div></td>";
                                //音频
                                var audio_url = "<td><div class='input-group'><input type='text' class='form-control' dir='rtl' value='" + audio_file.fileName + "' disabled='disabled'/>" +
                                        "<a class='btn btn-success preview' data-href='" + audio_file.fileUrl + "'>预览</a></div></td>";
                                //音频时长
                                var secondsInput = "<td><div class='input-group'><input type='text' class='form-control' style='width: 70px;' value='' /></div></td>";
                                //句子
                                var sentenceInput = "<td><div class='input-group'><input type='text' class='form-control' value='' /></div></td>";
                                //单词名称
                                var wordInput = "<td><div class='input-group'><input type='text' class='form-control' style='width: 70px;' value='' /></div></td>";
                                //单词音标
                                var pronunciationInput = "<td><div class='input-group'><input type='text' class='form-control' style='width: 70px;' value='' /></div></td>";
                                //单词词性
                                var wordClassInput = "<td><div class='input-group'><input type='text' class='form-control' style='width: 70px;' value='' /></div></td>";
                                //单词释义
                                var paraphraseInput = "<td><div class='input-group'><input type='text' class='form-control' style='width: 70px;' value='' /></div></td>";
                                //排序
                                var indexInput = "<td><div class='input-group'><input type='text' class='form-control' style='width: 70px;' value='' /></div></td>";
                                //删除按钮
                                var deleteButton = "<td><div class='input-group'> <a class='btn btn-warning delete_sentence'>删除</a> </div> </td>"
                                var $appendDiv = $($this.closest("fieldset").find("tbody.batch_file_list"));
                                if ($appendDiv.attr("id") === "picture_book_sentence_file_list") {
                                    //学习目标的上传
                                    $appendDiv.append("<tr>" + img_url + audio_url + secondsInput + sentenceInput + indexInput + deleteButton + "</tr>");
                                } else if ($appendDiv.attr("id") === "word_file_list") {
                                    //核心单词练习上传
                                    $appendDiv.append("<tr>" + img_url + audio_url + wordInput + pronunciationInput + wordClassInput + paraphraseInput + indexInput + deleteButton + "</tr>");
                                }
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
           if(!link){
               alert("文件上传中，请稍后预览");
               return;
           }
            window.open(link);
        });

        $("#template_spu_id").blur(function () {
            var spuId = $("#template_spu_id").val();
            if(spuId){
                $.get("/opmanager/studyTogether/template/get_spu_name.vpage",{spu_id:spuId},function (data) {
                    if(data.success){
                        $("#spu_name").html(data.spu_name);
                    }else{
                        alert(spuId +"对应的SPU不存在");
                        $("#template_spu_id").val("");
                        $("#spu_name").html("");
                        return;
                    }
                });
            }else{
                $("#template_spu_id").val("");
                $("#spu_name").html("");
                return;
            }
        });

        //删除一组上传的文件
        $(document).on("click", ".delete_sentence", function () {
            $(this).closest("tr").remove();
        });

        //保存绘本模版
        $("#save_info_button").on("click", function () {
            //课节目标
            var use_picture_id = $("input[name=template_use_picture_book_id_radio]:checked").val();
            var template_picture_book_id = "";
            var template_cover_img_url = "";
            var template_sentence_list = [];
            if (use_picture_id === "1") {
                template_picture_book_id = $("#template_picture_book_id").val();
            } else if (use_picture_id === "2") {
                template_cover_img_url = $("#template_cover_img_url").val();
                var sentence_audio_list_label = $("#picture_book_sentence_file_list").find("tr");
                for (var template_sentence_index = 0; template_sentence_index < sentence_audio_list_label.length; template_sentence_index++) {
                    var sentence_audio_info_input = $(sentence_audio_list_label[template_sentence_index]).find("input");
                    if (sentence_audio_info_input.length != 5) {
                        alert("绘本课节图片/地址信息错误");
                        return;
                    }
                    if ($(sentence_audio_info_input[0]).val().replace(/\s+/g, "").length <= 0) {
                        alert("绘本课节图片地址错误");
                        return;
                    }
                    if ($(sentence_audio_info_input[1]).val().replace(/\s+/g, "").length <= 0) {
                        alert("绘本课节音频地址错误");
                        return;
                    }
                    if ($(sentence_audio_info_input[2]).val().replace(/\s+/g, "").length <= 0) {
                        alert("绘本课节音频时长错误");
                        return;
                    }
                    if ($(sentence_audio_info_input[3]).val().trim().length <= 0) {
                        alert("绘本课节音频句子内容错误");
                        return;
                    }
                    var sentence_info = {
                        imgUrl: $(sentence_audio_info_input[0]).val().replace(/\s+/g, ""),
                        audioUrl: $(sentence_audio_info_input[1]).val().replace(/\s+/g, ""),
                        audioSeconds: $(sentence_audio_info_input[2]).val().replace(/\s+/g, ""),
                        sentence: $(sentence_audio_info_input[3]).val().trim(),
                        rank: $(sentence_audio_info_input[4]).val().replace(/\s+/g, "")
                    };
                    template_sentence_list.push(sentence_info);
                }
                if (template_sentence_list.length <= 0) {
                    alert("模板句子音频不能为空");
                    return;
                }
            } else {
                alert("类型错误");
                return;
            }
            if ($("#template_title").val().trim().length > 4) {
                alert("模板课节标题不能超过4个字");
                return;
            }
            if ($("#template_name").val().trim().length > 50) {
                alert("模板名称不能超过50个字");
                return;
            }
            //绘本课节目标基本信息
            var template_info = {
                id: $("#template_id").val(),
                spuId: $("#template_spu_id").val().trim(),
                name: $("#template_name").val().trim(),
                title: $("#template_title").val().trim(),
                goalDetail: $("#template_goal_detail").val(),
                pictureBookId: template_picture_book_id,
                coverImgUrl: template_cover_img_url,
                sentenceList: template_sentence_list,
                comment: $("#template_comment").val().trim()
            };
            //绘本名师精讲
            if ($("#expound_title").val().trim().length > 7) {
                alert("名师精讲模块标题不能超过7个字");
                return;
            }
            var expound_model_info = {
                id: $("#expound_content_id").val(),
                expound_title: $("#expound_title").val().trim(),
                videoUrl: $("#expound_content_video_url").val().replace(/\s+/g, ""),
                videoSeconds: $("#expound_content_video_seconds").val().replace(/\s+/g, "")
            };
            //核心单词练习
            var word_questionIds = [];
            if ($("#word_questionIds").val().trim().length > 0) {
                word_questionIds = $("#word_questionIds").val().replace(/\s+/g, "").split("#");
            }
            $.each(word_questionIds,function (index, value) {
                if(!value){
                    word_questionIds.pop(index);
                }
            });
            if (word_questionIds.length <= 0) {
                alert("核心单词练习题目ID不能为空");
                return;
            }
            //单词列表
            var word_list = [];
            var word_audio_list_label = $("#word_file_list").find("tr");
            for (var i = 0; i < word_audio_list_label.length; i++) {
                var word_audio_info_input = $(word_audio_list_label[i]).find("input");
                if (word_audio_info_input.length != 7) {
                    alert("核心单词练习单词信息错误");
                    return;
                }
                if ($(word_audio_info_input[0]).val().replace(/\s+/g, "").length <= 0) {
                    alert("核心单词练习图片地址不能为空");
                    return;
                }
                if ($(word_audio_info_input[1]).val().replace(/\s+/g, "").length <= 0) {
                    alert("核心单词练习音频地址不能为空");
                    return;
                }
                if ($(word_audio_info_input[2]).val().trim().length <= 0) {
                    alert("核心单词练习单词名称不能为空");
                    return;
                }
                if ($(word_audio_info_input[3]).val().trim().length <= 0) {
                    alert("核心单词练习单词音标不能为空");
                    return;
                }
                if ($(word_audio_info_input[4]).val().trim().length <= 0) {
                    alert("单词词性不能为空");
                    return;
                }
                if ($(word_audio_info_input[5]).val().trim().length <= 0) {
                    alert("核心单词练习单词释义不能为空");
                    return;
                }
                var word_info = {
                    imgUrl: $(word_audio_info_input[0]).val().replace(/\s+/g, ""),
                    audioUrl: $(word_audio_info_input[1]).val().replace(/\s+/g, ""),
                    word: $(word_audio_info_input[2]).val().trim(),
                    pronunciation: $(word_audio_info_input[3]).val().trim(),
                    wordClass: $(word_audio_info_input[4]).val().trim(),
                    paraphrase: $(word_audio_info_input[5]).val().trim(),
                    rank: $(word_audio_info_input[6]).val().replace(/\s+/g, "")
                };
                word_list.push(word_info);
            }
            if (word_list.length <= 0) {
                alert("核心单词练习单词不能为空");
                return;
            }
            if ($("#word_title").val().trim().length > 7) {
                alert("核心单词练习模块标题不能超过7个字");
                return;
            }
            var word_model_info = {
                id: $("#word_content_id").val(),
                word_title: $("#word_title").val().trim(),
                questionIds: word_questionIds,
                words: word_list
            };
            //重点句子跟读
            var sentence_questionIds = [];
            if ($("#sentence_questionIds").val().trim().length > 0) {
                sentence_questionIds = $("#sentence_questionIds").val().trim().split("#");
            }
            $.each(sentence_questionIds,function (index, value) {
                if(!value){
                    sentence_questionIds.pop(index);
                }
            });
            if (sentence_questionIds.length <= 0) {
                alert("重点句子跟读题目ID不能为空");
                return;
            }
            if ($("#sentence_title").val().trim().length > 7) {
                alert("重点句子跟读模块标题不能超过7个字");
                return;
            }
            var sentence_model_info = {
                id: $("#sentence_content_id").val(),
                sentence_title: $("#sentence_title").val().trim(),
                questionIds: sentence_questionIds
            };
            //每日绘本配音
            //这三个字段无论如何都有
            var fun_key_word_count = 0;
            var fun_sentence_count = 0;
            var fun_general_time_string = "";

            var fun_picture_book_id = "";
            var fun_cover_img_url = "";

            var fun_sentence_list = [];
            if (use_picture_id == "1") {
                fun_picture_book_id = $("#template_picture_book_id").val().trim();
            } else if (use_picture_id == "2") {
                fun_sentence_list = template_sentence_list;
                if (fun_sentence_list.length <= 0) {
                    alert("核心单词练习单词不能为空");
                    return;
                }
                fun_cover_img_url = template_cover_img_url;
                fun_key_word_count = $("#fun_content_key_word_count").val().trim();
                fun_sentence_count = $("#fun_content_sentence_count").val().trim();
                fun_general_time_string = $("#fun_content_general_time_string").val().trim();
            } else {
                alert("类型错误");
                return;
            }
            if ($("#fun_title").val().trim().length > 7) {
                alert("每日绘本配音模块标题不能超过7个字");
                return;
            }
            var fun_model_info = {
                id: $("#fun_content_id").val(),
                fun_title: $("#fun_title").val().trim(),
                pictureBookId: fun_picture_book_id,
                coverImgUrl: fun_cover_img_url,
                keyWordCount: fun_key_word_count,
                sentenceCount: fun_sentence_count,
                generalTimeString: fun_general_time_string,
                sentenceList: fun_sentence_list
            };

            console.log(template_info);
            console.log(expound_model_info);
            console.log(word_model_info);
            console.log(sentence_model_info);
            console.log(fun_model_info);
            //保存
            $.post("save_picture_book_template.vpage", {
                template_info: JSON.stringify(template_info),
                expound_info: JSON.stringify(expound_model_info),
                word_info: JSON.stringify(word_model_info),
                sentence_info: JSON.stringify(sentence_model_info),
                fun_info: JSON.stringify(fun_model_info)
            }, function (data) {
                if (data.success) {
                    alert("保存成功，模板ID为：" + data.id);
                    window.close();
                } else {
                    $("#save_error_message").html(data.info);
                }
            });
        });
    });
    function isChineseChar(str) {
        var reg = /[\u4E00-\u9FA5\uF900-\uFA2D]/;
        return reg.test(str);
    }
</script>
</@layout_default.page>