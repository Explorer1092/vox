<#import "../module.ftl" as module>
<@module.page
title="教师管理"
pageJsFile={"micocourse" : "public/script/course/teachers"}
pageJs=["micocourse"]
leftMenu="教师管理"
>
<#include "bootstrapTemp.ftl">
<div class="op-wrapper clearfix">
<h4 class="title">教师管理</h4>
<div class="pull-right">
<a class="btn btn-success" id="addBtn" style="float:left;" href="javascript:void(0)"><i class="glyphicon glyphicon-plus"></i> 添加老师</a>
</div>
<div class="clearfix"></div>
<div style="margin-top:10px;">
    <table class="table table-bordered table-striped" style="background-color: #FFF;">
        <thead>
        <tr>
            <th>教师账号</th>
            <th>老师名称</th>
            <th style="width:150px;">欢拓账号</th>
            <th style="width:140px;">操作</th>
        </tr>
        </thead>
        <tbody>
        <#if teachers?has_content && teachers?size gt 0>
            <#list teachers as t>
            <tr>
                <td>${t.accountName!''}</td>
                <td>${t.realName!''}</td>
                <td>${t.talkFunId!'尚未注册欢拓账号'}</td>
                <td data-tid="${t.id!0}" data-pic="${t.portrait!''}" data-account="${t.accountName!''}" data-name="${t.realName!''}">
                    <span class="js-editBtn btn btn-warning btn-xs"><i class="glyphicon glyphicon-edit" title="编辑"></i></span>
                    <span class="js-resetBtn btn btn-primary btn-xs"><i class="glyphicon glyphicon-refresh" title="重置密码"></i></span>
                    <span class="js-delBtn btn btn-danger btn-xs"><i class="glyphicon glyphicon-trash" title="删除"></i></span>
                    <a class="js-regBtn btn btn-success btn-xs" href="javascript:void(0);" title="注册老师"><span><i class="glyphicon glyphicon-tree-deciduous"></i></span></a>
                </td>
            </tr>
            </#list>
        <#else>
        <tr>
            <td colspan="4" style="text-align: center;">暂无老师数据~</td>
        </tr>
        </#if>

        </tbody>
    </table>
</div>
</div>
<script id="teacherDialog" type="text/html">
<div class="clearfix" style="clear:both;">
    <div id="tLeft" style="float:left;width: 74%;">
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon"><span class="red-mark">*</span>老师账号</div>
                <input type="text" class="v-select js-postData form-control" value="<%= tAccount %>" name="accountName" maxlength="20" placeholder="请填写老师账号" data-info="请填写老师账号">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon"><span class="red-mark">*</span>老师名称</div>
                <input type="text" class="v-select js-postData form-control" value="<%= tName %>" name="realName" maxlength="20" placeholder="请填写老师名称" data-info="请填写老师名称">
            </div>
        </div>
        <% if(type == 'new') {%>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon"><span class="red-mark">*</span>初始密码</div>
                <input type="password" class="v-select js-postData form-control" name="code1" id="code1" maxlength="20" data-info="请填写初始密码" placeholder="请填写初始密码">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon"><span class="red-mark">*</span>重复密码</div>
                <input type="password" class="v-select js-postData form-control" name="code2" id="code2" maxlength="20" data-info="请填写重复密码" placeholder="请填写重复密码">
            </div>
        </div>
        <%}%>
    </div>
    <div id="tRight" style="float:right; width:24%;">
        <div class="form-group">
            <div id="imgDiv">
                <% if(pic) {%>
                <img src="<%= pic %><% if(taril){%>@1e_1c_0o_0l_98h_98w_80q<%}%>" alt="" width="98px;" height="98px;">
                <% } else {%>
                <img alt="" width="98px;" height="98px;">
                <% }%>
            </div>
            <a class="blue-btn js-portrait" style="float:none;" href="javascript:void(0)">上传头像</a>
            <input id="upload-portrait" style="display: none;" type="file" class="js-userPic" accept="image/gif, image/jpeg, image/png, image/jpg">
            <input type="hidden" class="js-postData" name="portrait" id="portrait" value="<% if(pic) {%><%= pic %><% }%>" data-info="请上传头像">
        </div>
    </div>
</div>
</script>
<script id="restDialog" type="text/html">
    <form class="form-horizontal" role="form" style="width: 90%;">
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon"><span class="red-mark">*</span>输入新密码</div>
                <input type="password" class="form-control" id="code1" placeholder="请输入新密码" maxlength="20">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon"><span class="red-mark">*</span>重复新密码</div>
                <input type="password" class="form-control" id="code2" placeholder="请重复新密码" maxlength="20">
            </div>
        </div>
    </form>
</script>
<script id="registerDialog" type="text/html">
    <h4>此操作将同步更新Mizar后台和欢拓后台的登录密码</h4>
    <form class="form-horizontal" role="form" style="width: 100%;">
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon"><span class="red-mark">*</span>输入登录欢拓密码</div>
                <input type="password" class="form-control" id="code1" placeholder="请输入新密码" maxlength="20">
            </div>
        </div>
    </form>
</script>
</@module.page>