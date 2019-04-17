<#import "../module.ftl" as module>
<@module.page
title="用户管理-编辑"
pageJsFile={"siteJs" : "public/script/config/sysuser"}
pageJs=["siteJs"]
leftMenu="用户管理"
>

<div class="bread-nav">
    <a class="parent-dir" href="/config/user/index.vpage">用户管理</a>
    &gt;
    <a class="current-dir" href="javascript:void(0)" style="cursor: default">${isNew?string("新增","编辑")}用户</a>
</div>
<form id="detail-form" action="/config/user/add.vpage" method="post">
    <input value="${userId!}" name="id" style="display:none;">
    <input value="" name="roleGroupIds" style="display:none;">
    <div style="float:left;">
        <div class="input-control">
            <label><span class="red-mark">*</span> 账号：</label>
            <input name="accountName" data-title="账号" class="require item" value="${(userInfo.accountName)!}"
                   placeholder="账号只能输入字母、数字以及下划线"/>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>姓名：</label>
            <input name="realName" data-title="姓名" class="require item" value="${(userInfo.realName)!}"
                   placeholder="请输入姓名"/>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>手机：</label>
            <input name="mobile" data-title="手机" class="require item" value="${(userInfo.mobile)!}"
                   placeholder="请输入手机号"/>
        </div>
        <div class="input-control">
            <label>备注：</label>
            <textarea name="userComment" data-title="备注" style="resize: none"
                      rows="3">${(userInfo.userComment)!}</textarea>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>用户角色：</label>
        <#--<div class="checkboxes clearfix" style="overflow: hidden;">
            <#if isNew && currentUser.isBD()>
                &lt;#&ndash; BD角色只能新建机构业主 &ndash;&gt;
                <label class="checkbox">
                    <input class="role-checkbox" data-value="21" type="checkbox" checked disabled/>机构业主
                </label>
            <#else>
                <#list allRoleMap?keys as item>
                    <label class="checkbox"><input class="role-checkbox" data-value="${item!}" type="checkbox"
                        <#if userInfo?? && userInfo.userRoles??>
                            <#list userInfo.userRoles as roleItem>
                                <#if item == roleItem?string> checked </#if>
                                &lt;#&ndash; BD角色只能编辑机构业主 &ndash;&gt;
                                <#if currentUser.isBD() && item != "21"> disabled </#if>
                            </#list>
                        </#if>
                    />${allRoleMap[item?string].roleName!}</label> <br/>
                </#list>
            </#if>
        </div>-->
            <div class="fancytree-checkbox" id="role-tree"></div>
        </div>
        <div class="input-control">
            <label>状态：</label>

            <select name="userStatus" class="sel">
                <option value="0" <#if userInfo ?? && userInfo.status == 0>selected</#if>>新建</option>
                <option value="1" <#if userInfo ?? && userInfo.status == 1>selected</#if>>正常</option>
                <option value="9" <#if userInfo ?? && userInfo.status == 9>selected</#if>>关闭</option>
            </select>
        </div>
        <#--添加学校-->
        <#if useRole?? && useRole>
            <div class="input-control">
                <label>负责学校：</label>
                <a id="AddingSchoolBtn" class="blue-btn submit-search" href="javascript:void(0)"
                   style="float: none; display: inline-block;" data-url="/config/user/finduserschool.vpage" data-ignore="${tangram?c}">添加学校</a>
                    <div class="op-wrapper clearfix addInfantSchool" style="<#if isNew!false>display: none;</#if><#if successSchools?? && successSchools?size lt 1>display:none;</#if>">
                        <table id="schoolManager" class="data-table" style="width:90%; margin-left: 84px;">
                            <thead>
                            <tr>
                                <td style="width:100px;">学校ID</td>
                                <td style="width:100px;">学校名称</td>
                                <td style="width:60px;">阶段</td>
                                <td>合同开始时间</td>
                                <td>合同结束时间</td>
                                <td>操作</td>
                            </tr>
                            </thead>
                            <tbody>
                            <#if successSchools?? && successSchools?size gt 0>
                                <#list successSchools as school>
                                    <tr class="schoolList">
                                        <td class="schoolId" style="width:100px;">${school.id!}</td>
                                        <td style="width:100px;overflow: hidden;word-break: break-all">${school.cname!}</td>
                                        <td style="width:60px;">${school.level!}</td>
                                        <td><input value="${school.contractStartMonth!}" name="startDate" autocomplete="off" class="v-select startTime" disabled="disabled" placeholder="请选择月份"  /></td>
                                        <td><input value="${school.contractEndMonth!}" name="startDate" autocomplete="off" class="v-select endTime timeChose" <#if school.expired?? && school.expired>disabled="disabled"</#if> placeholder="请选择月份"  /></td>
                                        <td style="width:70px;">
                                            <#if tangram!false>
                                            <a class="op-btn change-remark delete_userSchool"
                                               href="javascript:void(0)" style="float: none; display: inline-block;" data-id="${school.id!}">删除</a>
                                            </#if>
                                        </td>
                                    </tr>
                                </#list>
                            </#if>
                            </tbody>
                        </table>
                        <input name="schoolsJson" data-title="学校" class="item" value="" hidden/>
                    </div>
            </div>
        <#else >
        <#if (!isNew)!false>
            <div class="input-control">
                <label>机构列表：</label>
                <a id="AddingAgenciesBtn" class="blue-btn submit-search" href="javascript:void(0)"
                   style="float: none; display: inline-block;" data-url="/config/user/addusershop.vpage">添加机构</a>
                <#if shopList?? && shopList?size gt 0>
                    <div class="op-wrapper clearfix">
                        <table class="data-table" style="width:90%; margin-left: 84px;">
                            <#list shopList as shop>
                                <tr>
                                    <td style="width:140px;">${shop.id!}</td>
                                    <td>${shop.fullName!}</td>
                                    <td style="width:70px;">
                                        <a id="delete_usershop_${shop.id!}" class="op-btn change-remark"
                                           href="javascript:void(0)" style="float: none; display: inline-block;">删除</a>
                                    </td>
                                </tr>
                            </#list>
                        </table>
                    </div>
                </#if>
            </div>
            <#if showOA?? &&showOA>
                <div id="officialAccountDiv" class="input-control">
                    <label>公众号列表：</label>
                    <#--BD不能编辑公众号-->
                    <#if !currentUser.isBD()>
                        <a id="addOfficialAccountBtn" class="blue-btn submit-search" href="javascript:void(0)"
                           style="float: none; display: inline-block; width: auto;">添加公众号</a>
                    </#if>
                </div>
                <div id="officialAccountBox" class="input-control"></div>
            </#if>
        </#if>
        </#if>
    </div>
    <div class="clearfix submit-box" style="margin:20px 85px;">
        <a id="save-btn" class="submit-btn save-btn" href="javascript:void(0)">保存</a>
        <a id="abandon-btn" class="submit-btn abandon-btn" href="/config/user/index.vpage">取消</a>
    </div>
</form>
</@module.page>

<script type="text/html" id="officialAccount_item">
    <%if(list.length > 0){%>
    <table class="data-table one-page displayed" style="margin-left: 84px;width:90%">
        <thead>
        <tr>
            <th>公众号标识</th>
        <#if !currentUser.isBD()>
            <th>操作</th>
        </#if>
        </tr>
        </thead>
        <tbody>
        <%for(var i = 0; i < list.length;i++){%>
        <tr>
            <td><%=list[i].officialAccountKey%></td>
        <#if !currentUser.isBD()>
            <td>
                <a class="OfficialAccountDeleteBtn" href="javascript:void (0);" style="color: #00a0e9;"
                   data-index="<%=i%>" data-accountkey="<%=list[i].officialAccountKey%>">删除</a>
            </td>
        </#if>
        </tr>
        <%}%>
        </tbody>
    </table>
    <%}%>
</script>

<script type="text/javascript">
    <#if officialAccountsList?? && officialAccountsList?size gt 0>
    var pageOfficialAccountsList = [];
        <#list officialAccountsList as officialAccounts>
        pageOfficialAccountsList.push({
            officialAccountKey: '${officialAccounts.accountsKey!}'
        });
        </#list>
    </#if>
    <#if isNew!false>
        var addSchool = true;
    <#else>
        var addSchool = false;
    </#if>
</script>
<script type="text/html" id="schoolList">
    <%if (res.length>0){%>
        <%for (var i=0;i< res.length;i++){%>
            <tr class="schoolList newAddSchool">
                <td class="schoolId" data-id="<%=res[i].id%>" style="width:100px;"><%=res[i].id%></td>
                <td style="width:100px;overflow: hidden;word-break: break-all"><%=res[i].cname%></td>
                <td style="width:60px;"><%=res[i].level%></td>
                <td><input name="startDate" autocomplete="off" class="v-select startTime timeChose" placeholder="请选择月份"  /></td>
                <td><input value="" name="endDate" autocomplete="off" class="v-select endTime timeChose" placeholder="请选择月份"  /></td>
                <td style="width:70px;">
                    <a class="op-btn change-remark delete_userSchool"
                       href="javascript:void(0)" style="float: none; display: inline-block;" data-id="<%=res[i].id%>">删除</a>
                </td>
            </tr>
        <%}%>
    <%}%>
</script>
