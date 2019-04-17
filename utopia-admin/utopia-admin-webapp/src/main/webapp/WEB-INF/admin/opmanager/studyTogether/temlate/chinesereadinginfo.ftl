<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='CRM' page_num=9>
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>
<#-- 使用已改过的 kindeditor-all.js-->
<script src="${requestContext.webAppContextPath}/public/js/kindeditor/kindeditor-all.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/echarts/echarts.min.js"></script>
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
            <#if template?? && template.id?has_content>编辑<#else >新增</#if>语文阅读模板
        <#else >
            模板详情-语文阅读
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
                            <input type="text" value="<#if template??>${template.title!''}</#if>" name="template_title" id="template_title" class="input">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>字数：</label>
                        <div class="controls">
                            <input type="text" value="<#if template??>${template.wordCount!''}</#if>" name="template_word_count" id="template_word_count" class="input">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>推荐阅读时长：</label>
                        <div class="controls">
                            <input type="text" value="<#if template??>${template.generalTimeString!''}</#if>" name="template_general_time_string" id="template_general_time_string" class="input">min
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>知识点数量：</label>
                        <div class="controls">
                            <input type="text" value="<#if template??>${template.knowledgeCount!''}</#if>" name="template_knowledge_count" id="template_knowledge_count" class="input" placeholder="正整数">个
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>上传封面：</label>
                        <div class="controls">
                            <input type="text" value="<#if template??>${template.coverImgUrl!''}</#if>" name="template_cover_img_url" id="template_cover_img_url" class="input" disabled="disabled">
                            <input class="upload_file" type="file" data-suffix="jpg#png">
                                <a class="btn btn-success preview"   data-href="<#if template?? && cdn_host??>${cdn_host!''}${template.coverImgUrl!''}"</#if>">预览</a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>课节导语：</label>
                        <div class="controls">
                            <textarea style="width: 300px;height: 100px;" name="template_introduction" id="template_introduction" maxlength="45" placeholder="45字以内"><#if template?? >${template.introduction!''}</#if></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">备注说明：</label>
                        <div class="controls">
                            <input type="text" value="<#if template??>${template.comment!''}</#if>" name="template_comment" id="template_comment" class="input">
                        </div>
                    </div>

                    <fieldset>
                        <legend class="field-title">学习模块</legend>
                        <label><strong>全文阅读：</strong></label>
                        <fieldset>
                            <div class="control-group">
                                <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>内容：</label>
                                <div class="controls">
                                    <label style="margin-top:  5px; ">
                                        <input type="radio" name="use_picture_book_or_expound_radio" value="1"
                                               <#if (picture_book_reading_content??)|| (!(picture_book_reading_content??) && !(famous_book_content??))>checked="checked"</#if>>绘本阅读
                                    </label>
                                    <label style="margin-top:  5px; ">
                                        <input type="radio" name="use_picture_book_or_expound_radio" value="2"<#if famous_book_content??>checked="checked"</#if>>名著阅读
                                    </label>
                                </div>
                            </div>
                            <div class="picture_book_read">
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块标题：</label>
                                    <div class="controls">
                                        <input type="text" value="<#if picture_book_reading_title??>${picture_book_reading_title!''}<#else >绘本阅读</#if>" name="picture_book_reading_title" id="picture_book_reading_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块内容：</label>
                                </div>
                                <input type="hidden" id="picture_book_reading_id" name="picture_book_reading_id" value="<#if picture_book_reading_content??>${picture_book_reading_content.id!''}</#if>">
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span
                                            style="color: red;font-size: 20px;">*</span>上传视频：</label>
                                    <div class="controls">
                                        <input type="text" value="<#if picture_book_reading_content??>${picture_book_reading_content.videoUrl!''}</#if>" name="picture_book_reading_content_video_url" id="picture_book_reading_content_video_url" class="input" disabled="disabled">
                                        <input class="upload_file" type="file" data-suffix="mp4">
                                        <a class="btn btn-success preview" data-href="<#if picture_book_reading_content?? && cdn_host??>${cdn_host!''}${picture_book_reading_content.videoUrl!''}</#if>">预览</a>
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>视频时长：</label>
                                    <div class="controls">
                                        <input type="text" value="<#if picture_book_reading_content??>${picture_book_reading_content.videoSeconds!0}</#if>" name="picture_book_reading_content_video_seconds" id="picture_book_reading_content_video_seconds" class="input">秒
                                    </div>
                                </div>
                            </div>
                            <div class="expound">
                                <input type="hidden" id="famous_book_content_id" name=famous_book_content_id" value="<#if famous_book_content??>${famous_book_content.id!''}</#if>">
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块标题：</label>
                                    <div class="controls">
                                        <input type="text" value="<#if famous_book_title??>${famous_book_title!''}<#else >名著阅读</#if>" name="famous_book_title" id="famous_book_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块内容：</label>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>标题：</label>
                                    <div class="controls">
                                        <input type="text" value="<#if famous_book_content??>${famous_book_content.title!''}</#if>" name="famous_book_content_title" id="famous_book_content_title" class="input">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span
                                            style="color: red;font-size: 20px;">*</span>作者：</label>
                                    <div class="controls">
                                        <input type="text" value="<#if famous_book_content??>${famous_book_content.author!''}</#if>" name="famous_book_content_author" id="famous_book_content_author" class="input">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span
                                            style="color: red;font-size: 20px;">*</span>背景图片：</label>
                                    <div class="controls">
                                        <input type="text" value="<#if famous_book_content??>${famous_book_content.backgroundImgUrl!''}</#if>" name="famous_book_content_background_img_url" id="famous_book_content_background_img_url" class="input" disabled="disabled">
                                        <input class="upload_file" type="file" data-suffix="jpg#png"><a class="btn btn-success preview"   data-href="<#if famous_book_content?? && cdn_host??>${cdn_host!''}${famous_book_content.backgroundImgUrl!''}"</#if>">预览</a>
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span
                                            style="color: red;font-size: 20px;">*</span>上传音频：</label>
                                    <div class="controls">
                                        <input type="text" value="<#if famous_book_content??>${famous_book_content.audioUrl!''}</#if>" name="famous_book_content_audio_url" id="famous_book_content_audio_url" class="input" disabled="disabled">
                                        <input class="upload_file" type="file" data-suffix="mp3"><a class="btn btn-success preview"   data-href="<#if famous_book_content?? && cdn_host??>${cdn_host!''}${famous_book_content.audioUrl!''}"</#if>">预览</a>
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span
                                            style="color: red;font-size: 20px;">*</span>音频时长：</label>
                                    <div class="controls">
                                        <input type="text" value="<#if famous_book_content??>${famous_book_content.audioSeconds!''}</#if>" name="famous_book_content_audio_seconds" id="famous_book_content_audio_seconds" class="input">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">文本内容：</label>
                                    <div class="controls">
                                        <textarea style="width: 300px;height: 100px;" name="famous_book_content_lrc_content" id="famous_book_content_lrc_content" placeholder="拼音紧跟文字之后。拼音前后用#包括"><#if famous_book_content??>${famous_book_content.lrcContent!''}</#if></textarea>
                                    </div>
                                </div>
                            </div>
                        </fieldset>
                        <label><strong>重点解析</strong></label>
                        <fieldset>
                            <div class="word_content">
                                <input type="hidden" name="word_content_id" id="word_content_id" value="<#if word_content??>${word_content.id!''}</#if>">
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块标题：</label>
                                    <div class="controls">
                                        <input type="text" value="<#if word_title??>${word_title!''}<#else >字词详解</#if>" name="word_title" id="word_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块内容：</label>
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
                                        <th><span style="color: red;font-size: 20px;">*</span>图片</th>
                                        <th><span style="color: red;font-size: 20px;">*</span>讲解音频</th>
                                        <th><span style="color: red;font-size: 20px;">*</span>字词及注音(字和拼音用#分割)</th>
                                        <th><span style="color: red;font-size: 20px;">*</span>字词释义</th>
                                        <th><span style="color: red;font-size: 20px;">*</span>例句文本</th>
                                        <th><span style="color: red;font-size: 20px;">*</span>展示顺序</th>
                                        <th><span style="color: red;font-size: 20px;">*</span>操作</th>
                                    </tr>
                                    </thead>
                                    <tbody id="word_list" class="batch_file_list">
                                <#if word_content?? && word_content.wordList?has_content && word_content.wordList?size gt 0>
                                    <#list word_content.wordList as word>
                                    <tr>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control" value="${word.imgUrl!''}" dir='rtl' disabled="disabled"/>
                                                <a class="btn btn-success preview" data-href="${cdn_host!''}${word.imgUrl!''}">预览</a>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control" value="${word.audioUrl!''}" dir='rtl' disabled="disabled"/>
                                                <a class="btn btn-success preview" data-href="${cdn_host!''}${word.audioUrl!''}">预览</a>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control"  placeholder="字和拼音用#分割" value="${word.word!''}"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control" style="width: 70px;" value="${word.paraphrase!''}"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <#assign word_example = word.example>
                                                <input type="text" class="form-control" style="width: 70px;" value="<#escape word_example as word_example?html>${word_example!''}"</#escape>/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control" style="width: 70px;" value="${word_index + 1}"/>
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
                            <div class="appreciate_content">
                                <input type="hidden" id="appreciate_content_id" name="appreciate_content_id" value="<#if appreciate_content??>${appreciate_content.id!''}</#if>">
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块标题：</label>
                                    <div class="controls">
                                        <input type="text" value="<#if appreciate_title??>${appreciate_title!''}<#else >重点赏析</#if>" name="appreciate_title" id="appreciate_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块内容：</label>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>主题图片：</label>
                                    <div class="controls">
                                        <input type="text" value="<#if appreciate_content??>${appreciate_content.themeImgUrl!''}</#if>" name="appreciate_content_theme_img_url" id="appreciate_content_theme_img_url" class="input" disabled="disabled">
                                        <input class="upload_file" type="file" data-suffix="jpg#png">
                                            <a class="btn btn-success preview"   data-href="<#if appreciate_content?? && cdn_host??>${cdn_host!''}${appreciate_content.themeImgUrl!''}"</#if>">预览</a>
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>上传音频：</label>
                                    <div class="controls">
                                        <input type="text" value="<#if appreciate_content??>${appreciate_content.audioUrl!''}</#if>" name="appreciate_content_audio_url" id="appreciate_content_audio_url" class="input" disabled="disabled">
                                        <input class="upload_file" type="file" data-suffix="mp3">
                                            <a class="btn btn-success preview"   data-href="<#if appreciate_content?? && cdn_host??>${cdn_host!''}${appreciate_content.audioUrl!''}"</#if>">预览</a>
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>音频时长：</label>
                                    <div class="controls">
                                        <input type="text" value="<#if appreciate_content??>${appreciate_content.audioSeconds!''}</#if>" name="appreciate_content_audio_seconds" id="appreciate_content_audio_seconds" class="input">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>文本内容：</label>
                                    <div class="controls">
                                        <textarea style="width: 300px;height: 100px;" name="appreciate_content_text" id="appreciate_content_text"><#if appreciate_content??>${appreciate_content.text!''}</#if></textarea>
                                    </div>
                                </div>
                            </div>
                        </fieldset>
                        <label><strong>思维导图：</strong></label>
                        <fieldset>
                            <div class="control-group">
                                <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块标题：</label>
                                <div class="controls">
                                    <input type="hidden" id="mind_map_content_id" name="mind_map_content_id" value="<#if mind_map_content??>${mind_map_content.id!''}</#if>">
                                    <input type="text" value="<#if mind_map_title??>${mind_map_title!''}<#else >思维导图</#if>" name="mind_map_title" id="mind_map_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块内容：</label>
                            </div>
                            <div class="control-group">
                                <input type="hidden" id="mind_map_question_id" name="mind_map_question_id" value="<#if mind_map_content?? && mind_map_content.questionIds?has_content && mind_map_content.questionIds?size gt 0>${(mind_map_content.questionIds![])?join("#")}</#if>"/>
                                <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>批量新增思维导图内容：</label>
                                <div class="controls">
                                    <input type="text" value="<#if mind_map_content??>${mind_map_content.excelFileUrl!''}</#if>" name="mind_map_content_file_url" id="mind_map_content_file_url" class="input" disabled="disabled">
                                    <input class="upload_mind_map_excel" type="file" data-suffix="xls#xlsx">
                                    <a class="btn btn-success preview" data-href="<#if mind_map_content??>${cdn_host!''}${mind_map_content.excelFileUrl!''}<#else >${cdn_host!''}study_course/test/2018/09/14/语文阅读思维导图上传模板.xlsx</#if>"><span id="download_title"><#if  template?? && template.id?has_content>下载已上传文件<#else >下载模板</#if></span></a>
                                </div>
                                <div class="controls">
                                    <span style="color: grey;">注：每个级别每个文本框输入字数（含标点符号）限制为2～40</span>
                                </div>
                                <div class="controls">
                                    <div class="row-fluid" id="mindMap" style="width: 1000px; height:400px;"></div>
                                </div>
                            </div>
                        </fieldset>
                        <div class="control-group">
                            <span id="mind_map_error_message" style="color: red"></span>
                        </div>
                        <label><strong>拓展学习：</strong></label>
                        <fieldset>
                            <div class="follow_reading_content">
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块标题：</label>
                                    <div class="controls">
                                        <input type="hidden" id="follow_reading_content_id" name="follow_reading_content_id" value="<#if follow_reading_content??>${follow_reading_content.id!''}</#if>">
                                        <input type="text" value="<#if follow_reading_title??>${follow_reading_title!''}<#else >趣味阅读</#if>" name="follow_reading_title" id="follow_reading_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">模块内容：</label>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>题目ID：</label>
                                    <div class="controls">
                                        <textarea style="width: 300px;height: 100px;" name="follow_reading_content_question_ids" id="follow_reading_content_question_ids" placeholder="用#分割，排列顺序即为前端展示顺序"><#if follow_reading_content?? && follow_reading_content.questionIdList?has_content && follow_reading_content.questionIdList?size gt 0>${(follow_reading_content.questionIdList![])?join("#")}</#if></textarea>
                                    </div>
                                </div>
                            </div>
                            <div class="expand_practice_content">
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>模块标题：</label>
                                    <div class="controls">
                                        <input type="hidden" id="expand_practice_content_id" name="expand_practice_content_id" value="<#if expand_practice_content??>${expand_practice_content.id!''}</#if>">
                                        <input type="text" value="<#if expand_practice_title??>${expand_practice_title!''}<#else >拓展练习</#if>" name="expand_practice_title" id="expand_practice_title" class="input"><span style="color: grey;margin-left: 10px;">默认值可以修改，字数限制7个字以内</span>
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">模块内容：</label>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>题目ID：</label>
                                    <div class="controls">
                                        <textarea style="width: 300px;height: 100px;" name="expand_practice_content_question_ids" id="expand_practice_content_question_ids" placeholder="用#分割，排列顺序即为前端展示顺序"><#if expand_practice_content?? && expand_practice_content.questionIdList?has_content && expand_practice_content.questionIdList?size gt 0>${(expand_practice_content.questionIdList![])?join("#")}</#if></textarea>
                                    </div>
                                </div>
                            </div>


                        </fieldset>
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
        var pictureBookReading = "<#if (picture_book_reading_content??)|| (!(picture_book_reading_content??) && !(famous_book_content??))>true</#if>";
        if (pictureBookReading) {
            $(".picture_book_read").show();
            $(".expound").hide();
            //模块2联动
            $(".word_content").show();
            $(".appreciate_content").hide();
            //模块4联动
            $(".follow_reading_content").show();
            $(".expand_practice_content").hide();
        } else {
            $(".picture_book_read").hide();
            $(".expound").show();
            //模块2联动
            $(".word_content").hide();
            $(".appreciate_content").show();
            //模块4联动
            $(".follow_reading_content").hide();
            $(".expand_practice_content").show();
        }
        //模板课节目标-输入项切换
        $("input[name=use_picture_book_or_expound_radio]").change(function () {
            var value = $("input[name=use_picture_book_or_expound_radio]:checked").val();
            if (value === "1") {
                $(".picture_book_read").show();
                $(".expound").hide();
                //模块2联动
                $(".word_content").show();
                $(".appreciate_content").hide();
                //模块4联动
                $(".follow_reading_content").show();
                $(".expand_practice_content").hide();
            } else if (value === "2") {
                $(".picture_book_read").hide();
                $(".expound").show();
                //模块2联动
                $(".word_content").hide();
                $(".appreciate_content").show();
                //模块4联动
                $(".follow_reading_content").hide();
                $(".expand_practice_content").show();
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
                if (inputFiles.length % 2 != 0) {
                    alert("选择文件不匹配，必须是是一个图片对应一个音频，并且文件名称必须一样。请重新选择");
                    //重置成空。保证连续选择同一个文件能触发上传
                    $this.val("");
                    return;
                }
                var fileSize = 0;
                for (var i = 0; i < inputFiles.length; i++) {
                    var suffix = inputFiles[i].name.split('.').pop().toLowerCase();
                    if (acceptSuffix.indexOf(suffix) === -1) {
                        alert("仅支持以下文件格式" + acceptSuffix);
                        //重置成空。保证连续选择同一个文件能触发上传
                        $this.val("");
                        return;
                    }
                    formData.append('inputFiles', inputFiles[i], inputFiles[i].name);
                    fileSize += inputFiles[i].size;
                }
                //限制30M
                if (fileSize > 31457280) {
                    alert("您选择的文件大小超过30M");
                    //重置成空。保证连续选择同一个文件能触发上传
                    $this.val("");
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
                        //重置成空。保证连续选择同一个文件能触发上传
                        $this.val("");
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
                                //讲解音频
                                var audio_url = "<td><div class='input-group'><input type='text' class='form-control' dir='rtl' value='" + audio_file.fileName + "' disabled='disabled'/>" +
                                        "<a class='btn btn-success preview' data-href='" + audio_file.fileUrl + "'>预览</a></div></td>";
                                //字词及注音
                                var wordInput = "<td><div class='input-group'><input type='text' class='form-control'  value='' /></div></td>";
                                //单词释义
                                var paraphraseInput = "<td><div class='input-group'><input type='text' class='form-control' style='width: 70px;' value='' /></div></td>";
                                //例句文本
                                var exampleInput = "<td><div class='input-group'><input type='text' class='form-control' style='width: 70px;' value='' /></div></td>";
                                //排序
                                var indexInput = "<td><div class='input-group'><input type='text' class='form-control' style='width: 70px;' value='' /></div></td>";
                                //删除按钮
                                var deleteButton = "<td><div class='input-group'> <a class='btn btn-warning delete_sentence'>删除</a> </div> </td>"
                                var $appendDiv = $($this.closest("fieldset").find("tbody.batch_file_list"));
                                //字词详解上传
                                $appendDiv.append("<tr>" + img_url + audio_url + wordInput + paraphraseInput + exampleInput + indexInput + deleteButton + "</tr>");
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
                $.get("/opmanager/studyTogether/template/get_spu_name.vpage", {spu_id: spuId}, function (data) {
                    if (data.success) {
                        $("#spu_name").html(data.spu_name);
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

        //保存语文阅读模板
        $("#save_info_button").on("click", function () {
            //课节目标
            var regEx = /^[0-9]*$/;
            if (!regEx.test($("#template_knowledge_count").val().trim())) {
                alert("课节目标-知识点数量只能是数字");
                return;
            }
            if(!regEx.test($("#template_word_count").val().trim())){
                alert("课节目标-字数只能是数字");
                return;
            }

            var template_info = {
                id: $("#template_id").val(),
                spuId: $("#template_spu_id").val().trim(),
                name: $("#template_name").val().trim(),
                title: $("#template_title").val().trim(),
                wordCount: $("#template_word_count").val().trim(),
                generalTimeString: $("#template_general_time_string").val().trim(),
                knowledgeCount: $("#template_knowledge_count").val().trim(),
                coverImgUrl: $("#template_cover_img_url").val().replace(/\s+/g, ""),
                introduction: $("#template_introduction").val().trim(),
                comment: $("#template_comment").val().trim()
            };
            if (template_info.title.length > 25) {
                alert("课节目表标题不能超过25个字");
                return;
            }
            if (template_info.coverImgUrl.length <= 0) {
                alert("课节目表封面图片地址不能为空");
                return;
            }
            //绘本阅读
            var picture_book_reading_model_info = {
                id: $("#picture_book_reading_id").val(),
                picture_book_reading_title: $("#picture_book_reading_title").val().trim(),
                videoUrl: $("#picture_book_reading_content_video_url").val().replace(/\s+/g, ""),
                videoSeconds: $("#picture_book_reading_content_video_seconds").val().replace(/\s+/g, "")
            };
            //名著阅读
            var famous_book_model_info = {
                id: $("#famous_book_content_id").val(),
                famous_book_title: $("#famous_book_title").val().trim(),
                title: $("#famous_book_content_title").val().trim(),
                author: $("#famous_book_content_author").val().trim(),
                backgroundImgUrl: $("#famous_book_content_background_img_url").val().replace(/\s+/g, ""),
                audioUrl: $("#famous_book_content_audio_url").val().replace(/\s+/g, ""),
                audioSeconds: $("#famous_book_content_audio_seconds").val().replace(/\s+/g, ""),
                lrcContent: $("#famous_book_content_lrc_content").val().trim()
            };
            //字词详解
            var word_list = [];
            var word_audio_list_label = $("#word_list").find("tr");
            for (var i = 0; i < word_audio_list_label.length; i++) {
                var word_audio_info_input = $(word_audio_list_label[i]).find("input");
                var word_info = {
                    imgUrl: $(word_audio_info_input[0]).val().replace(/\s+/g, ""),
                    audioUrl: $(word_audio_info_input[1]).val().replace(/\s+/g, ""),
                    word: $(word_audio_info_input[2]).val().trim(),
                    paraphrase: $(word_audio_info_input[3]).val().trim(),
                    example: $(word_audio_info_input[4]).val().trim(),
                    rank: $(word_audio_info_input[5]).val().trim()
                };
                word_list.push(word_info);
            }
            var word_model_info = {
                id: $("#word_content_id").val(),
                word_title: $("#word_title").val().trim(),
                wordList: word_list
            };
            //重点赏析
            var appreciate_model_info = {
                id: $("#appreciate_content_id").val(),
                appreciate_title: $("#appreciate_title").val().trim(),
                themeImgUrl: $("#appreciate_content_theme_img_url").val().replace(/\s+/g, ""),
                audioUrl: $("#appreciate_content_audio_url").val().replace(/\s+/g, ""),
                audioSeconds: $("#appreciate_content_audio_seconds").val().replace(/\s+/g, ""),
                text: $("#appreciate_content_text").val().trim()
            };
            //思维导图
            var mind_map_question_ids = [];
            if ($("#mind_map_question_id").val().trim().length > 0) {
                mind_map_question_ids = $("#mind_map_question_id").val().replace(/\s+/g, "").split("#");
            }
            $.each(mind_map_question_ids, function (index, value) {
                if (!value) {
                    mind_map_question_ids.pop(index);
                }
            });
            var mind_map_model_info = {
                id: $("#mind_map_content_id").val(),
                mind_map_title: $("#mind_map_title").val().trim(),
                excelFileUrl: $("#mind_map_content_file_url").val().replace(/\s+/g, ""),
                questionIds: mind_map_question_ids,
                mindMap: mind_map
            };
            //趣味跟读
            var follow_reading_questionIds = [];
            if ($("#follow_reading_content_question_ids").val().trim().length > 0) {
                follow_reading_questionIds = $("#follow_reading_content_question_ids").val().replace(/\s+/g, "").split("#");
            }
            $.each(follow_reading_questionIds, function (index, value) {
                if (!value) {
                    follow_reading_questionIds.pop(index);
                }
            });
            var follow_reading_model_info = {
                id: $("#follow_reading_content_id").val(),
                follow_reading_title: $("#follow_reading_title").val().trim(),
                questionIdList: follow_reading_questionIds
            };
            //拓展练习
            var expand_practice_questionIds = [];
            if ($("#expand_practice_content_question_ids").val().trim().length > 0) {
                expand_practice_questionIds = $("#expand_practice_content_question_ids").val().replace(/\s+/g, "").split("#");
            }
            $.each(expand_practice_questionIds, function (index, value) {
                if (!value) {
                    expand_practice_questionIds.pop(index);
                }
            });
            var expand_practice_model_info = {
                id: $("#expand_practice_content_id").val(),
                expand_practice_title: $("#expand_practice_title").val().trim(),
                questionIdList: expand_practice_questionIds
            };

            //各种数据校验
            var use_picture_book = $("input[name=use_picture_book_or_expound_radio]:checked").val();
            if (use_picture_book === "1") {
                //绘本精读
                if (picture_book_reading_model_info.videoUrl.length <= 0) {
                    alert("绘本精读模块视频地址不能为空");
                    return;
                }
                if (picture_book_reading_model_info.picture_book_reading_title.length > 7) {
                    alert("绘本精读模块标题不能超过7个字");
                    return;
                }
                //字词详解
                if (word_model_info.word_title.length > 7) {
                    alert("字词详解模块标题不能超过7个字");
                    return;
                }
                if (word_model_info.wordList.length <= 0) {
                    alert("字词详解列表不能为空");
                    return;
                }
                //校验每个词
                var need_break = false;
                $.each(word_model_info.wordList, function (index, word) {
                    if (word.imgUrl.length <= 0) {
                        alert("字词详解图片地址不能为空");
                        need_break = true;
                        return false
                    }
                    if (word.audioUrl.length <= 0) {
                        alert("字词详解音频地址不能为空");
                        need_break = true;
                        return false
                    }
                    if (word.word.length <= 0) {
                        alert("字词详解字词及注音不能为空");
                        need_break = true;
                        return false
                    }
                    if (word.paraphrase.length <= 0) {
                        alert("字词详解字词释义不能为空");
                        need_break = true;
                        return false
                    }
                    if (word.example.length <= 0) {
                        alert("字词详解例句文本不能为空");
                        need_break = true;
                        return false
                    }
                });
                if (need_break) {
                    return;
                }
                if (follow_reading_model_info.follow_reading_title.length > 7) {
                    alert("趣味跟读模块标题不能超过7个字");
                    return;
                }
                if (follow_reading_model_info.questionIdList.length <= 0) {
                    alert("趣味跟读题目ID不能为空");
                    return;
                }
            } else if (use_picture_book === "2") {
                //名著阅读
                if (famous_book_model_info.audioUrl.length <= 0) {
                    alert("名著阅读模块视频地址不能为空");
                    return;
                }
                if (famous_book_model_info.backgroundImgUrl.length <= 0) {
                    alert("名著阅读模块背景图片地址不能为空");
                    return;
                }
                if (famous_book_model_info.famous_book_title.length > 7) {
                    alert("名著阅读模块标题不能超过7个字");
                    return;
                }
                //重点赏析
                if (appreciate_model_info.themeImgUrl.length <= 0) {
                    alert("重点赏析主题图片不能为空");
                    return
                }
                if (appreciate_model_info.audioUrl.length <= 0) {
                    alert("重点赏析音频地址不能为空");
                    return
                }
                if (appreciate_model_info.appreciate_title.length > 7) {
                    alert("重点赏析模块标题不能超过7个字");
                    return;
                }
                if (expand_practice_model_info.expand_practice_title.length > 7) {
                    alert("拓展练习模块标题不能超过7个字");
                    return;
                }
                if (expand_practice_model_info.questionIdList.length <= 0) {
                    alert("拓展练习题目ID不能为空");
                    return;
                }
            } else {
                alert("类型错误");
                return;
            }
            if (mind_map_model_info.mind_map_title.length > 7) {
                alert("思维导图模块标题不能超过7个字");
                return;
            }
            if (mind_map_model_info.excelFileUrl.length <= 0) {
                alert("思维导图内容不能为空");
                return;
            }
            
            console.log(template_info);
            console.log(picture_book_reading_model_info);
            console.log(famous_book_model_info);
            console.log(word_model_info);
            console.log(appreciate_model_info);
            console.log(mind_map_model_info);
            console.log(follow_reading_model_info);
            console.log(expand_practice_model_info);
            //保存
            var data = {
                template_info: JSON.stringify(template_info),
                mind_map_info: JSON.stringify(mind_map_model_info),
                use_picture_book: use_picture_book
            };
            if (use_picture_book === "1") {
                data.picture_book_reading_info = JSON.stringify(picture_book_reading_model_info);
                data.word_info = JSON.stringify(word_model_info);
                data.follow_reading_info = JSON.stringify(follow_reading_model_info);
            } else if (use_picture_book === "2") {
                data.famous_book_info = JSON.stringify(famous_book_model_info);
                data.appreciate_info = JSON.stringify(appreciate_model_info);
                data.expand_practice_info = JSON.stringify(expand_practice_model_info);
            } else {
                alert("类型错误");
                return;
            }
            console.log(data);
            $.post("save_chinese_reading_template.vpage", data, function (data) {
                if (data.success) {
                    alert("保存成功，模板ID为：" + data.id);
                    window.close();
                } else {
                    $("#save_error_message").html(data.info);
                }
            });
        });
        //思维导图插件
        var chart = echarts.init($("#mindMap")[0]);
        chart.setOption(option = {
            tooltip: {
                trigger: 'item',
                triggerOn: 'mousemove'
            },
            series: [
                {
                    type: 'tree',

                    data: [],

                    top: '1%',
                    left: '7%',
                    bottom: '1%',
                    right: '20%',

                    symbolSize: 7,

                    label: {
                        normal: {
                            position: 'left',
                            verticalAlign: 'middle',
                            align: 'right',
                            fontSize: 9
                        }
                    },

                    leaves: {
                        label: {
                            normal: {
                                position: 'right',
                                verticalAlign: 'middle',
                                align: 'left'
                            }
                        }
                    },

                    expandAndCollapse: true,
                    animationDuration: 550,
                    animationDurationUpdate: 750
                }
            ]
        });

        //把树状结构的字段名称改成插件所需要的字段名称
        function parseTree(children, current) {
            if (!$.isEmptyObject(children)) {
                for (var index in  children) {
                    var object = {};
                    object.name = children[index].content;
                    object.value = children[index].contentType;
                    if (!$.isEmptyObject(children[index].childrenMindMapList)) {
                        object = parseTree(children[index].childrenMindMapList, object);
                    }
                    var current_children = current.children;
                    if (current_children === undefined) {
                        current_children = [];
                    }
                    current_children.push(object);
                    current.children = current_children;
                }
                return current;
            }
        }
        //思维导图回显
        var mind_map = <#if mind_map_json??&&mind_map_json?has_content>JSON.parse(JSON.stringify(${mind_map_json}))<#else >{}</#if>;
        if (!$.isEmptyObject(mind_map)) {
            var root = {};
            root.name=mind_map.content;
            root.value=mind_map.contentType;
            root =  parseTree(mind_map.childrenMindMapList,root);
            chart.setOption({
                series: [{
                    data: [JSON.parse(JSON.stringify(root))]
                }]
            });
        } else {
            $("#mindMap").hide();
        }
        //重新上传思维导图excel
        $(".upload_mind_map_excel").on("change", function () {
            var $this = $(this);
            var ext = $this.val().split('.').pop().toLowerCase();
            if ($this.val() != '') {
                if ($.inArray(ext, ['xls', 'xlsx']) === -1) {
                    alert("仅支持以下格式【'xls', 'xlsx'】");
                    //重置成空。保证连续选择同一个文件能触发上传
                    $this.val("");
                    return false;
                }

                if ($this[0].files[0].name === "mindmap_template.xlsx" || $this[0].files[0].name === "mindmap_template.xls") {
                    alert("上传文件名称与模板名称不允许一致，请重命名后上传");
                    //重置成空。保证连续选择同一个文件能触发上传
                    $this.val("");
                    return;
                }
                var formData = new FormData();
                formData.append('source_file', $this[0].files[0]);
                //重置成空。保证连续选择同一个文件能触发上传
                $this.val("");
                $.ajax({
                    url: 'upload_mind_map_excel.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            mind_map = data.mapper;
                            $("#mind_map_question_id").val(data.questionIds.join("#"));
                            var root = {};
                            root.name=mind_map.content;
                            root.value=mind_map.contentType;
                            root = parseTree(mind_map.childrenMindMapList,root);
                            $($this.closest('.controls').find("input.input")).attr("value", data.fileName);
                            $($this.closest('.controls').find("a.btn-success")).attr("data-href", data.fileUrl);
                            chart.setOption({
                                series: [{
                                    data: [JSON.parse(JSON.stringify(root))]
                                }]
                            });
                            $("#mindMap").show();
                            $("#download_title").html("下载已上传文件");
                        } else {
                            $("#mind_map_error_message").html(data.info);
                        }
                    }
                });
            }
        });
    });
    function isChineseChar(str) {
        var reg = /[\u4E00-\u9FA5\uF900-\uFA2D]/;
        return reg.test(str);
    }
</script>
</@layout_default.page>