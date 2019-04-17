<#import "../module.ftl" as module>
<@module.page
title="微课堂CRM"
pageJsFile={"micocourse" : "public/script/course/period"}
pageJs=["micocourse"]
leftMenu="课程管理"
>
<#include "bootstrapTemp.ftl">
<h3 class="title-h1">
    <#if !mode?has_content || mode != 'view'>查看<#else>编辑</#if>课时
</h3>
<div class="op-wrapper orders-wrapper clearfix">
    <form action="<#if period??>saveperiod.vpage<#else>appendperiod.vpage</#if>" method="POST" id="periodForm" class="" role="form">
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">课时名称</div>
                <input type="hidden" value="${courseId!0}" name="course">
                <input type="hidden" value="<#if period?has_content>${period.id!''}</#if>" name="period">
                <input type="text" class="v-select js-postData form-control" value="<#if period?has_content>${period.theme!''}</#if>" name="theme" maxlength="40" data-info="请填写课时名称" placeholder="请填写课时名称">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">课时时间</div>
                <input type="text" class="v-select js-postData form-control" style="border-bottom: none;" value="<#if period?has_content>${period.startTime?string("yyyy-MM-dd HH:mm")!''}</#if>" name="startTime" id="startTime" maxlength="20" readonly="readonly" data-info="请选择开始时间">
                <input type="text" class="v-select js-postData form-control" value="<#if period?has_content>${period.endTime?string("yyyy-MM-dd HH:mm")!''}</#if>" name="endTime" id="endTime" maxlength="20" readonly="readonly" data-info="请选择结束时间">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">课时价格</div>
                <input type="text" class="v-select js-postData form-control" value="<#if period?has_content>${period.price!}</#if>" name="price" id="price" maxlength="20" data-info="请填写课时价格" placeholder="请填写课时价格">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">课时视频</div>
                <input type="text" class="v-select js-postData" name="url" placeholder="请填写课时视频URL" value="<#if period?has_content && period.url?has_content>${period.url!}</#if>">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">按钮文本</div>
                <input type="text" class="v-select js-postData form-control" style="border-bottom: none;" value="<#if period?has_content && period.btnContent?has_content>${period.btnContent.BP!}</#if>" name="before" id="btn1" maxlength="20" data-info="请填写购买前自定义按钮文本" placeholder="请填写购买前自定义按钮文本">
                <input type="text" class="v-select js-postData form-control" value="<#if period?has_content && period.btnContent?has_content>${period.btnContent.AP!}</#if>" name="after" id="btn2" maxlength="20" data-info="请填写购买后自定义按钮文本" placeholder="请填写购买后自定义按钮文本">
            </div>
        </div>
        <div class="form-group" style="margin-bottom:0;">
            <div class="input-group">
                <div class="input-group-addon">直播地址</div>
                <input type="text" class="v-select js-postData" name="liveUrl" placeholder="请填写直播视频URL" value="<#if period?has_content && period.liveUrl?has_content>${period.liveUrl!}</#if>">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">回放地址</div>
                <input type="text" class="v-select js-postData" name="replayUrl" placeholder="请填写回放视频URL" value="<#if period?has_content && period.replayUrl?has_content>${period.replayUrl!}</#if>">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">欢拓课程</div>
                <input type="text" class="v-select js-postData form-control" name="tkCourse" placeholder="请填写需要跳转的欢拓课程ID" style="width: 85%;" value="<#if period?has_content && period.tkCourse?has_content>${period.tkCourse!}</#if>">
                <input type="text" class="v-select form-control" style="width: 15%;background: #eee;border-left: none; border-bottom: none;text-align: center;" value="课程自助 ID" disabled >

                <input name="talkfun" type="text" class="v-select form-control" placeholder="若不填写，将自动注册课程" value="<#if period?has_content && talkFun?has_content >${tkCourse!}</#if>" style="width: 85%;border-right: none;border-top:none;">
                <input id="talkFunBtn" type="text" class="v-select form-control" style="width: 15%;background: #eee;text-align: center;" value="课程默认 ID" readonly >
            </div>
        </div>
        <#if period?has_content && talkFun?has_content >
            <textarea id="talkFunVal" style="display: none;" disabled>${talkFun!}</textarea>
            <input type="text" style="display:none;" name="manual" value="${manual?c}">
        <#else>
            <input type="text" style="display:none;" name="manual" value="true">
        </#if>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">备注提示</div>
                <input type="text" class="v-select js-postData" name="tip" placeholder="请填写备注提示文字" value="<#if period?has_content && period.tip?has_content>${period.tip!}</#if>">
            </div>
        </div>
        <div class="form-group" style="margin-bottom:0;">
            <div class="input-group">
                <div class="input-group-addon">推广文字</div>
                <input type="text" class="v-select js-postData" name="spreadText" placeholder="请填写推广文字" value="<#if period?has_content && period.spreadText?has_content>${period.spreadText!}</#if>">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">推广链接</div>
                <input type="text" class="v-select js-postData" name="spreadUrl" placeholder="请填写推广链接" value="<#if period?has_content && period.spreadUrl?has_content>${period.spreadUrl!}</#if>">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">短信提醒</div>
                <div><span class="btn btn-default <#if period?has_content && period.smsNotify!true>active</#if> js-smsBtn" data-index="1">是</span><span class="btn btn-default <#if period?has_content && !period.smsNotify>active </#if>js-smsBtn" data-index="0">否</span></div>
                <input type="hidden" name="smsNotify" value="<#if period?has_content>${period.smsNotify?string!"true"}</#if>" id="smsNotify">
            </div>
        </div>
        <div class="from-group">
            <div class="input-group">
                <div class="input-group-addon">课程介绍</div>
                <div>
                    <script id="courseDetail" name="info" type="text/plain" style="height:260px; width:100%;"><#if period?has_content>${period.info!}</#if></script>
                </div>
            </div>
        </div>
        <div class="from-group" style="margin-top: 15px;">
            <div class="input-group">
                <div class="input-group-addon">课时图片</div>
                <input type="file" class="js-classPic" data-type="1" accept="image/gif,image/jpeg,image/jpg,image/png">
                <input type="hidden" id="classPic-1" class="js-postData" name="photo" value="<#if period?has_content && period.photo[0]?has_content>${period.photo[0]!}</#if>">
                <div id="imgDiv-1" >
                    <#if period?has_content && period.photo[0]?has_content>
                        <img src="${period.photo[0]!'#'}" style="width: 720px; height: 300px; ">
                    </#if>
                </div>
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">配套长期班URL</div>
                <input type="text" class="v-select js-postData" name="longClassUrl" placeholder="请填写配套长期班URL" value="<#if period?has_content && period.longClassUrl?has_content>${period.longClassUrl!}</#if>">
            </div>
        </div>
        <div class="from-group" style="margin-top: 15px;">
            <div class="input-group">
                <div class="input-group-addon">配套长期班图片</div>
                <input type="file" class="js-classPic" data-type="2" accept="image/gif,image/jpeg,image/jpg,image/png">
                <input type="hidden" id="classPic-2" class="js-postData" name="longClassPhoto" value="<#if period?has_content && (period.longClassPhoto)?has_content && period.longClassPhoto[0]?has_content>${period.longClassPhoto[0]!}</#if>">
                <div id="imgDiv-2" >
                    <#if period?has_content && (period.longClassPhoto)?has_content && period.longClassPhoto[0]?has_content>
                        <img src="${period.longClassPhoto[0]!'#'}" style="width: 720px; height: 300px; ">
                    </#if>
                </div>
            </div>
        </div>
    </form>
    <#--<#if mode?has_content && mode == 'view'>-->
        <a class="btn btn-primary pull-right" style="margin-top: 10px; margin-left: 15px;" href="javascript:window.history.go(-1)">
            <i class="glyphicon glyphicon-chevron-left"></i> 返  回
        </a>
    <#if !mode?has_content || mode != 'view'>
    <div class="btn btn-success pull-right" id="submitBtn" style="margin-top: 10px;">
        <i class="glyphicon glyphicon-floppy-saved"></i> 保  存
    </div>
    </#if>
</div>
<script>
    var courseId = "${courseId!0}";
</script>
<script src="/public/plugin/ueditor-1-4-3/third-party/zeroclipboard/ZeroClipboard.min.js" type="text/javascript"></script>
<script src="/public/plugin/ueditor-1-4-3/ueditor.config.js" type="text/javascript"></script>
<script src="/public/plugin/ueditor-1-4-3/ueditor.all.min.js" type="text/javascript"></script>
<script src="/public/plugin/ueditor-1-4-3/lang/zh-cn/zh-cn.js" type="text/javascript"></script>
</@module.page>