<#import "../../module.ftl" as module>
<@module.page
title="公众号"
leftMenu="信息发布"
>
<div class="op-wrapper clearfix">
    <span class="title-h1">一起作业公众号—发送用户信息</span>
    <span style="color: #a94442"><#if errmsg??>    ${errmsg}</#if></span>
    <span style="color: #00b050"><#if resultMsg??>    ${resultMsg}</#if></span>
</div>
<form action="/basic/officialaccount/pushmessage/submit.vpage" method="post">
    <div class="op-wrapper clearfix">
        <table class="data-table conTable">
            <tr>
                <td><p>标题</p></td>
                <td><input title="" id="title" name="title" size="20"/></td>
            </tr>
            <tr>
                <td><p>内容</p></td>
                <td><input title="" id="content" name="content" size="20"/></td>
            </tr>
            <tr>
                <td><p>跳转链接（全路径）</p></td>
                <td><input title="" id="url" name="url" size="20"/></td>
            </tr>
            <tr>
                <td><p>账号列表</p></td>
                <td><textarea id="userIds" name="userIds" rows="20" cols="15"></textarea></td>
            </tr>
        </table>
    </div>

    <div class="op-wrapper marTop clearfix" style="margin-bottom: 10px;">
        <div class="item time-widAuto">
            <p>公众号：</p>
        </div>
        <div class="item time-widAuto">
            <select class="v-select" style="width: 140px;" name="accountId">
                <#if accounts??>
                    <#list accounts as account >
                        <option value="${account.id}">${account.name}</option>
                    </#list>
                </#if>
            </select>
        </div>
        <div class="item time-widAuto marLeft15">
            <button type="submit" class="blue-btn">Send
                <button/>
        </div>
    </div>
    </div>
</form>
</@module.page>