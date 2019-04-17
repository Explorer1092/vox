<#import "../module.ftl" as module>
<@module.page
title="微课堂CRM"
pageJsFile={"micocourse" : "public/script/course/item"}
pageJs=["micocourse"]
leftMenu="列表管理"
>
<#include "bootstrapTemp.ftl">
<div class="op-wrapper orders-wrapper clearfix">
    <form action="saveitem.vpage" method="POST" id="courseForm" class="" role="form">
        <#if course?has_content>
        <input type="hidden" value="${course.id!''}" name="id">
        </#if>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon"><span class="red-mark">*</span>课时 ID</div>
                <input id="chooseId" type="text" class="v-select js-postData form-control" value="<#if course?has_content>${course.title!''}</#if>" name="title" maxlength="40" data-info="请填写课时ID" placeholder="请填写课时ID">
            </div>
        </div>
        <#if course?has_content>
            <div class="form-group">
                <div class="input-group">
                    <div class="input-group-addon">创建时间</div>
                    <input type="text" class="v-select form-control" value="<#if course?has_content>${course.createAt?string('yyyy-MM-dd HH:mm:ss')}</#if>" disabled>
                </div>
            </div>
        </#if>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">课时名称</div>
                <input id="theme" type="text" class="v-select form-control" value="<#if period?has_content>${period.theme!''}</#if>" disabled>
            </div>
        </div>
        <#--<div class="form-group">-->
            <#--<div class="input-group">-->
                <#--<div class="input-group-addon">课程描述</div>-->
                <#--<textarea class="v-select js-postData form-control" name="description" style="resize: none;"><#if course?has_content>${course.description!''}</#if></textarea>-->
            <#--</div>-->
        <#--</div>-->
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">课程类型</div>
                <div>
                    <span class="btn btn-default <#if course?has_content && course.category == 'MICRO_COURSE_OPENING'>active</#if> js-catBtn" data-cat="MICRO_COURSE_OPENING">公开课</span>
                    <span class="btn btn-default <#if course?has_content && course.category == 'MICRO_COURSE_NORMAL'>active</#if> js-catBtn" data-cat="MICRO_COURSE_NORMAL">长期课</span>
                </div>
                <input type="hidden" name="category" value="<#if course?has_content>${course.category!""}</#if>" id="category">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">课程状态</div>
                <div>
                    <span class="btn btn-default <#if course?has_content && course.status == 'OFFLINE'>active</#if> js-stBtn" data-st="OFFLINE">下线</span>
                    <span class="btn btn-default <#if course?has_content && course.status == 'ONLINE'>active</#if> js-stBtn" data-st="ONLINE">上线</span>
                </div>
                <input type="hidden" name="status" value="<#if course?has_content>${course.status!""}</#if>" id="status">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">标签分类</div>
                <div>
                    <#list ['数学', '语文', '英语', '才艺', '家庭教育'] as tag>
                        <span class="btn btn-default <#if course?has_content && course.subTitle == tag>active</#if> js-tagBtn" data-tag="${tag}">${tag}</span>
                    </#list>
                </div>
                <input type="hidden" name="subTitle" value="<#if course?has_content>${course.subTitle!""}</#if>" id="subTitle">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">标签颜色</div>
                <div>
                    <#list ['red', 'yellow', 'green', 'blue'] as cl>
                        <span class="btn btn-default <#if course?has_content && course.background == cl>active</#if> js-colorBtn" data-color="${cl}">${cl}</span>
                    </#list>
                </div>
                <input type="hidden" name="background" value="<#if course?has_content>${course.background!""}</#if>" id="background">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">适配年级</div>
                <input type="text" class="v-select js-postData form-control"
                       value="<#if course?has_content && course.clazzLevels?has_content>${course.clazzLevels?join(" , ")}</#if>"
                       name="levels" maxlength="40" data-info="请填写适配年级" placeholder="请填写适配年级，以英文逗号分隔">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">&nbsp;&nbsp;&nbsp;优先级</div>
                <input type="text" class="v-select js-postData form-control" value="<#if course?has_content>${course.priority!''}</#if>" name="priority" maxlength="40" data-info="请填写优先级" placeholder="请填写优先级，数字越大越靠前">
            </div>
        </div>
        <#if course?has_content>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">投放策略</div>
                <input type="text" class="v-select js-postData form-control" style="width: 200px; margin-right: 15px;" disabled
                       value="<#if has_3?? && has_3>投放所有用户<#elseif has_1?? && has_1>投放指定地区<#elseif has_2?? && has_2>投放指定学校<#else>尚未配置</#if>">
                <a href="javascript:void(0);" class="btn btn-primary" id="configBtn" data-item="${course.id!}">点此配置投放策略</a>
            </div>
        </div>
        </#if>
        <div class="from-group">
            <div class="input-group">
                <div class="input-group-addon">课时图片</div>
                <input type="file" class="js-classPic" accept="image/gif,image/jpeg,image/jpg,image/png">
                <input type="hidden" id="classPic" class="js-postData" name="speakerAvatar" value="<#if course?has_content>${course.speakerAvatar!}</#if>">
                <div id="imgDiv" >
                    <#if course?has_content>
                        <img src="${course.speakerAvatar!'#'}" style="width: 720px; height: 300px; ">
                    </#if>
                </div>
            </div>
        </div>
    </form>
    <a class="btn btn-primary pull-right" style="margin-top: 10px;" href="/course/manage/itemlist.vpage">返  回</a>
    <div class="btn btn-success pull-right" id="submitBtn" style="margin-top: 10px; margin-right: 30px;">保  存</div>
</div>
<script src="/public/plugin/ueditor-1-4-3/third-party/zeroclipboard/ZeroClipboard.min.js" type="text/javascript"></script>
<script src="/public/plugin/ueditor-1-4-3/ueditor.config.js" type="text/javascript"></script>
<script src="/public/plugin/ueditor-1-4-3/ueditor.all.min.js" type="text/javascript"></script>
<script src="/public/plugin/ueditor-1-4-3/lang/zh-cn/zh-cn.js" type="text/javascript"></script>
</@module.page>