<#import "../../module.ftl" as module>
<@module.page
title="公众号"
leftMenu="基本信息"
>

    <@app.script href="/public/plugin/jquery/jquery-1.7.1.min.js"/>
    <@app.script href="/public/plugin/jquery-impromptu/jquery-impromptu.js"/>
    <@app.css href="/public/plugin/jquery-impromptu/impromptu-atuo-ui.css"/>

<div class="op-wrapper clearfix">
    <span class="title-h1">一起作业公众号基本信息</span>
    <span class="item" style="width:60; float: right;">
            <a class="blue-btn" href="/basic/officialaccount/pushmessage/index.vpage">推送消息</a>
    </span>
</div>
<table class="data-table">
    <thead>
    <tr>
        <th style="display:none">id</th>
        <th style="width: 120px;">公众号名称</th>
        <th>允许<br />关注</th>
        <th style="width: 100px;">创建时间</th>
        <th>关注用户数</th>
        <th width="77">当前状态</th>
        <th style="width: 35%">公众号说明</th>
        <th>公众号LOGO</th>
        <th>关注回复</th>
        <th>副标题</th>
    </tr>
    </thead>
    <tbody>
        <#if accountList?? && accountList?size gt 0>
            <#list accountList as account>
                <tr name="account-${(account.id)!''}">
                    <td style="display:none">${(account.id)!''}</td>
                    <td>${(account.name)!''}</td>
                    <td>${(account.followLimit?string('是','否'))!''}</td>
                    <td>${(account.createDatetime)!''}</td>
                    <td>${(account.pn)!0}人</td>
                    <td>
                        <#if account.status == 'Online'>
                            已上线
                        <#elseif account.status == 'Offline'>
                            已下线
                        </#if>
                    </td>
                    <td>${(account.instruction)!0}</td>
                    <td>
                        <#if (account.imgUrl)?has_content>
                            <img src="${(account.imgUrl)!}" style="width: 80px; height: 80px; border-radius: 50px;" alt="">
                        </#if>
                    </td>
                    <td name="greetings" data-greetings="${(account.greetings)!''}">
                        <a class="edit-greetings op-btn"  href="javascript:void(0);">编辑</a>
                    </td>
                    <td name="title" data-title="${(account.title)!''}">
                        <a class="edit-title op-btn" href="javascript:void(0);">编辑</a>
                    </td>
                </tr>
            </#list>
        <#else>
            <tr>
                <td>暂无公众号数据</td>
            </tr>
        </#if>
    </tbody>
</table>

<script>
    $(document).ready(function(){

        function updateAccount(accountId){
            var params = {};
            var $tr = $("tr[name=account-"+ accountId +"]");
            $.each($("td",$tr),function(){
                var name = $(this).attr("name");
                if(name){
                    params[name] = $(this).data(name);
                }
            });

            params["accountId"] = accountId;
            $.post("account.vpage",params, function (data) {
                if(data.success){
                    //成功
                    location.reload();
                }else{
                    $.prompt(("<div style='text-align: center'>" + data.info +"</div>" || "输入错误"), {
                        title: "提示",
                        buttons: {"知道了": false}
                    });
                }
            });
        }

        $(".edit-greetings").click(function(){
            var $that = $(this);
            var greetingContent = $(this).parent("td").data("greetings");
            $.prompt("<div style='text-align:center;'>" +
                    "<div class='input-control' style='margin:20px auto 0;width:300px;'>" +
                    "<textarea class='new-mark' id='greetings-content' style='resize: none;width:300px;' maxlength='30' placeholder='请输入30字以内的回复消息'>" +
                        greetingContent +
                    "</textarea>" +
                    "</div>" +
                    "</div>", {
                title: "关注回复",
                buttons: {"取消": false, "确定": true},
                submit: function (e, v) {
                    if (v) {
                        var accountId = $that.parent().siblings().eq(0).html();
                        $that.parent("td").data("greetings",$("#greetings-content").val());
                        updateAccount(accountId);
                    }
                }
            });
        });

        $(".edit-title").click(function(){
            var $that = $(this);
            var title = $(this).parent("td").data("title");
            $.prompt("<div style='text-align:center;'>" +
                    "<div class='input-control' style='margin:20px auto 0;width:300px;'>" +
                    "<textarea class='new-mark' id='title-content' style='resize: none;width:300px;' maxlength='16' placeholder='请输入16字以内的副标题'>" +
                    title +
                    "</textarea>" +
                    "</div>" +
                    "</div>", {
                title: "副标题",
                buttons: {"取消": false, "确定": true},
                submit: function (e, v) {
                    if (v) {
                        var accountId = $that.parent().siblings().eq(0).html();
                        $that.parent("td").data("title",$("#title-content").val());
                        updateAccount(accountId);
                    }
                }
            });
        });
    });

</script>
</@module.page>