<#import "../../module.ftl" as module>
<@module.page
title="发送消息"
pageJsFile={"siteJs" : "public/script/basic/notifyedit"}
pageJs=["siteJs"]
leftMenu="我的消息"
>
<div class="op-wrapper clearfix">
<span class="title-h1">发送消息</span>
    <form action="send.vpage" method="post" id="msgForm" name="msgForm">
        <div style="float: left;">
            <div class="input-control">
                <label>消息标题：</label>
                <input type="text" name="title" class="v-select item" maxlength="70" placeholder="请填写消息标题">
            </div>
            <div class="input-control">
                <label>消息内容：</label>
                <textarea name="content" placeholder="请填写消息内容" maxlength="400"></textarea>
            </div>
            <div class="input-control">
                <label>消息链接：</label>
                <input type="text" name="url" class="v-select item" maxlength="140" placeholder="请填写消息链接">
            </div>
            <div class="input-control">
                <label>消息类型：</label>
                <select name="type" id="type" class="sel" style="width: 680px;">
                    <option value="0">请选择</option>
                    <#if types?has_content && types?size gt 0>
                        <#list types?keys as key >
                            <option value="${types[key]!''}">${types[key].getDesc()!''}</option>
                        </#list>
                    </#if>
                </select>
            </div>
            <div class="input-control">
                <label>附件：</label>
                <div style="margin-left: 84px;">
                <div id="fileList">
                    <div class="js-fileCon"><input type="file" class="js-file" name="file1" id="file1"> <span class="grade_btn js-moreFileBtn" style="width: 26px;">+</span><span class="grade_btn js-removeFileBtn" style="width: 26px;">-</span></div>
                </div>
                </div>
                <input type="hidden" id="file_list_val" name="files">
            </div>
            <div class="input-control">
                <label>用户：</label>
                <div style="margin-left: 84px;">
                    <#if users?has_content && users?size gt 0>
                        <#list users as u>
                            <span class="js-gradeItem grade_btn" style="white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" data-uid="${u.id!''}">${u.realName!''}</span>
                        </#list>
                    </#if>
                </div>
                <input type="hidden" id="users" name="receiver">
            </div>
        </div>
    </form>
    <div class=" submit-box">
        <a id="add-save-btn" data-type="add" class="submit-btn save-btn" href="javascript:void(0)">确定</a>
        <a id="abandon-btn" class="submit-btn abandon-btn" href="/basic/notify/index.vpage">取消</a>
    </div>
</div>
</@module.page>