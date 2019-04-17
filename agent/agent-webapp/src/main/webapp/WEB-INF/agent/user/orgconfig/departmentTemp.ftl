<#--部门详情头部模板-->
<script id="departmentDetailHeaderTemp" type="text/x-handlebars-template">

    <div class="opeTabs">
        {{#if isManager }}
            <button class="btn btn-large btn-primary" type="button" id="addDepBtn" data-dpid="{{groupId}}">添加用户</button>
            {{#if canAddSubDepartment }}
            <button class="btn btn-large btn-primary" type="button" id="addSubDepBtn" data-dpid="{{groupId}}">添加子部门</button>
            {{/if}}
            <button class="btn btn-large btn-primary" type="button" id="editInfoDepBtn" data-roleid="{{groupRoleId}}" data-dpid="{{groupId}}" data-gname="{{groupName}}" data-gdesc="{{description}}">修改信息</button>
            {{#if parentGroupId}}
            <button class="btn btn-large btn-primary" type="button" id="updateAreaBtn" data-dpid="{{groupId}}">调整负责区域</button>
            {{/if}}
            <button class="btn btn-large btn-danger" type="button" id="delDepBtn" data-dpid="{{groupId}}">删除部门</button>
            {{#compare groupRole '==' "Country"}}
            <a class="btn btn-large btn-success" href="checkDataPage.vpage" target="_blank">检查</a>
            <button class="btn btn-large btn-primary" type="button" id="importAgentPerformanceGoalBtn">业绩目标导入</button>
            <button class="btn btn-large btn-primary" type="button" id="exportAgentPerformanceGoalBtn" data-dpid="{{groupId}}">下载业绩目标</button>
            <a class="btn btn-large btn-primary" href="exportOrganization.vpage?agentGroupId={{groupId}}"　target="_blank">导出组织结构</a>
            {{/compare}}
        {{/if}}
        {{#compare groupRole '==' "Region" }}
        <button class="btn btn-large btn-primary" type="button" id="exportAgentPerformanceGoalBtn" data-dpid="{{groupId}}">下载业绩目标</button>
        {{/compare}}
        {{#compare groupRole '==' "City" }}
        <button class="btn btn-large btn-primary" type="button" id="exportAgentPerformanceGoalBtn" data-dpid="{{groupId}}">下载业绩目标</button>
        {{/compare}}

        {{#compare groupRole '==' "BusinessUnit" }}
        <button class="btn btn-large btn-primary" type="button" id="exportAgentPerformanceGoalBtn" data-dpid="{{groupId}}">下载业绩目标</button>
        {{/compare}}

        {{#compare groupRole '==' "Area" }}
        <button class="btn btn-large btn-primary" type="button" id="exportAgentPerformanceGoalBtn" data-dpid="{{groupId}}">下载业绩目标</button>
        {{/compare}}
    </div>
    <hr>
</script>
<#--部门详情主体模板-->
<script id="departmentDetailContentTemp" type="text/x-handlebars-template">
    <div class="detailContainer">
        <div class="row-fluid">
            <div class="areaDetailTitle span2">部门名称：</div>
            <div class="js-depName span10">{{groupName}}</div>
        </div>
        {{#if parentGroupId}}
        <div class="row-fluid">
            <div class="areaDetailTitle span2">所属部门：</div>
            <div class="areaDetailContent span10" data-pid="{{parentGroupId}}">{{parentGroupName}}</div>
        </div>
        {{/if}}
        <div class="row-fluid">
            <div class="areaDetailTitle span2">部门级别：</div>
            <div class="js-depDesc span10">{{groupType}}</div>
        </div>
        <div class="row-fluid">
            <div class="areaDetailTitle span2">业务类型：</div>
            <div class="js-depDesc span10">{{serviceTypeStr}}</div>
        </div>
        <div class="row-fluid">
            <div class="areaDetailTitle span2">备注说明：</div>
            <div class="js-depDesc span10">{{description}}</div>
        </div>
        {{#if regionLogo}}
        <div class="row-fluid">
            <div class="areaDetailTitle span2">大区徽章：</div>
            <div class="js-depDesc span10"> {{#if hasImage}}<img src='{{logoUrl}}'/>{{/if}}</div>
        </div>
        {{/if}}
        <div class="row-fluid">
            <div class="areaDetailTitle span2">负责区域：</div>
            <div class="areaDetailContent span10">
                <div class="dataTables_wrapper js-regionAreaDiv">

                </div>

            </div>
        </div>
        <div class="row-fluid">
            <div class="areaDetailTitle span2"></div>
        </div>
        <div class="row-fluid">
            <div class="areaDetailTitle span2">城市支持费用：</div>
            <div class="js-depDesc span10">近六个月城市支持费用余额：{{latest6MonthCityBudgetData.balance}}元 &nbsp; <a id="cityBudgetInfoBtn" class="aBtn btn btn-small btn-primary" data-id="{{groupId}}">查看明细</a></div>
        </div>

        <div class="row-fluid">
            <div class="areaDetailTitle span2"></div>
        </div>
        <div class="row-fluid">
            <div class="areaDetailTitle span2">专员情况：</div>
            <div class="js-depName span10">应召专员：{{headCount}}&nbsp;&nbsp;&nbsp;实际在岗专员：{{actuallyCount}}&nbsp;&nbsp;&nbsp;满编率：{{actuallyRate}}%</div>
        </div>
        <div class="agentPerformanceGoalTemp-general-table"></div>
        <div class="responsible-general-table"></div>

        <div class="undistributed-school-table"></div>
    </div>
</script>

<#--地区table数据-->
<script id="regionTableTemp" type="text/x-handlebars-template">

    {{#if agentGroupRegionInfo}}
    <div>
        {{#each agentGroupRegionInfo}}
            {{#if provinceName}}
                {{provinceName}}——
            {{/if}}
            {{cityName}}
            {{#if countyName}}
                —— {{countyName}}
            {{/if}}、
        {{/each}}
    </div>
{{else}}
<div>
    <p>
        暂无
    </p>
</div>
{{/if}}
</script>

<script id="updateUsableCashAmount" type="text/x-handlebars-template">
    <form class="form-horizontal" id="updateUsableCashAmountForm">
        <div class="control-group">
            <label class="control-label" for="focusedInput">可用余额</label>
            <div class="controls">
                <span style="margin-right: 10px;width: 64px;">
                    {{usableCashAmount}}元
                </span>
                <select id="usableCashAmountOpt" class="form-control" name="usableCashAmountOpt" style="width: 100px;">
                    <option value="1">增加余额</option>
                    <option value="-1">减少余额</option>
                </select>
                <input id="usableCashAmount" name="usableCashAmount" class="input-xlarge focused" style="width: 104px;"
                       type="text" maxlength="10">
                <span style="color:red;display: none;" class="js-budgetInfo"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">可用余额调整原因</label>
            <div class="controls">
                <input id="usable_cash_amount_cause" name="usableCashAmountCause" class="input-xlarge focused"
                       type="text" maxlength="100">
            </div>
        </div>
    </form>
    <div class="form-actions">
        <button id="update_usable_cash_amount" type="button" class="btn btn-primary">保存</button>
        <a class="btn" href="department.vpage"> 取消 </a>
    </div>
</script>

<#--添加用户模板 && 修改用户信息模板-->
<script id="addDepartmentUserTemp" type="text/x-handlebars-template">
    <form class="form-horizontal" id="addDepartmentForm">
        {{#compare type '==' 'editInfo'}}
        {{else}}
        <div class="control-group">
            <label class="control-label" for="focusedInput">部门</label>
            <div class="controls">
                <input id="group_name" class="input-xlarge focused" type="text" value="{{groupName}}" {{#if groupName}} readonly="readonly" {{/if}}>
                <input id="group_id" type="hidden" value="{{agentGroupId}}" name="agentGroupId">
            </div>
        </div>
        {{/compare}}
        <div class="control-group">
            <label class="control-label" for="focusedInput">姓名</label>
            <div class="controls">
                    <input id="real_name" name="realName" class="input-xlarge focused js-needed" type="text" data-needInfo="姓名不能为空"
                        {{#compare type '==' 'editInfo'}}
                            value="{{realName}}"
                        {{else}}
                            value=""
                        {{/compare}}
                   >
            </div>
        </div>
        {{#compare type '==' 'editInfo'}}
        {{else}}
        <div class="control-group">
            <label class="control-label" for="focusedInput">账号</label>
            <div class="controls">
                <input id="account_name" name="accountName" class="input-xlarge focused js-needed" type="text" value="" data-needInfo="账号由字母和数字组成且不能为空">
                <span style="color:red;display: none;" class="js-accountInfo"></span>
            </div>
        </div>
        {{/compare}}
        {{#compare type '==' 'editInfo'}}
        {{else}}
        <div class="control-group">
            <label class="control-label" for="focusedInput">角色</label>
            <div class="controls">
                <select id="role_name" class="form-control js-needed" data-needInfo="请选择角色" name="roleType" style="width: 280px;">
                    <option value="0">请选择</option>
                    {{#each roleList}}
                    <option value="{{uroleId}}">{{uroleName}}</option>
                    {{/each}}
                </select>
            </div>
        </div>
        {{/compare}}
        <div class="control-group">
            <label class="control-label" for="focusedInput">电话</label>
            <div class="controls">
                <input id="phone_no" name="tel" class="input-xlarge focused" type="text" maxlength="11"
                       {{#compare type '==' 'editInfo'}}
                            value="{{tel}}" data-type="edit"
                       {{else}}
                            value=""
                       {{/compare}}
                >
                <span style="color:red;display: none;" class="js-telInfo"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">工号</label>
            <div class="controls">
                <input id="account_number" name="accountNumber" class="input-xlarge focused" type="text" oninput="this.value=this.value.replace(/[^0-9]/g,'');" value="{{accountNumber}}" maxlength="4">
            </div>
        </div>
        {{#compare type '!=' 'editInfo'}}
        <div class="control-group">
            <label class="control-label">开通蜂巢账号</label>
            <div class="controls">
                <label class="control-label" style="text-align: left;">
                    <input type="radio" name="bindHoneycomb" value="true" {{#if honeycombId}} checked {{/if}} />是
                </label>
                <label class="control-label" style="text-align: left;">
                    <input type="radio" name="bindHoneycomb" value="false" {{#if honeycombId}} {{else}} checked {{/if}}/>否
                </label>
            </div>
        </div>
        {{/compare}}
        <div class="control-group">
            <label class="control-label" for="focusedInput">保证金</label>
            <div class="controls">
                <input id="promise_cash" name="cashDeposit" class="input-xlarge focused" type="text"
                {{#compare type '==' 'editInfo'}}
                       value="{{cashDeposit}}"
                {{else}}
                    value=""
                {{/compare}}
                >
            </div>
        </div>
        <#--物料预算-->
        <div class="control-group">
            <label class="control-label" for="focusedInput">物料预算</label>
            <div class="controls">
                <span style="margin-right: 10px;width: 64px;">
                    {{#if materielBudget}}
                    {{materielBudget}}元
                    {{else}}
                    0元
                    {{/if}}
                </span>
                <select id="budgetOpt" class="form-control" name="budgetOpt" style="width: 100px;">
                    <option value="1">增加目标</option>
                    <option value="-1">减少目标</option>
                </select>
                <input id="materielBudget" name="materielBudget" class="input-xlarge focused" style="width: 104px;" type="text" maxlength="10"
                       {{#compare type '==' 'editInfo'}}
                    value=""
                {{else}}
                    value=""
                {{/compare}}
                >
                <span style="color:red;display: none;" class="js-budgetInfo"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">物料预算调整原因</label>
            <div class="controls">
                <input id="adjust_cause" name="bankName" class="input-xlarge focused" type="text">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">开户行</label>
            <div class="controls">
                <input id="bank_name" name="bankName" class="input-xlarge focused" type="text"
                {{#compare type '==' 'editInfo'}}
                value="{{bankName}}"
                {{else}}
                value=""
                {{/compare}}
                >
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">开户人</label>
            <div class="controls">
                <input id="bank_hostname" name="bankHostName" class="input-xlarge focused" type="text"
                {{#compare type '==' 'editInfo'}}
                value="{{bankHostName}}"
                {{else}}
                value=""
                {{/compare}}>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">银行帐号</label>
            <div class="controls">
                <input id="bank_account" name="bankAccount" class="input-xlarge focused" type="text"
                {{#compare type '==' 'editInfo'}}
                value="{{bankAccount}}"
                {{else}}
                value=""
                {{/compare}}>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">合同开始日期</label>
            <div class="controls">
                <input id="contract_start_date" name="contractStartDate" class="input-xlarge focused js-needed" type="text" data-needInfo="合同开始日期不能为空"
                {{#compare type '==' 'editInfo'}}
                value="{{contractStartDate}}"
                {{else}}
                value=""
                {{/compare}}>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">合同结束日期</label>
            <div class="controls">
                <input id="contract_end_date" name="contractEndDate" class="input-xlarge focused" type="text"
                {{#compare type '==' 'editInfo'}}
                value="{{contractEndDate}}"
                {{else}}
                value=""
                {{/compare}}>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">合同编号</label>
            <div class="controls">
                <input id="contract_number" name="contractNumber" class="input-xlarge focused" type="text"
                {{#compare type '==' 'editInfo'}}
                value="{{contractNumber}}"
                {{else}}
                value=""
                {{/compare}}>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">地址</label>
            <div class="controls">
                <input id="address" name="address" class="input-xlarge focused" type="text"
                {{#compare type '==' 'editInfo'}}
                value="{{address}}"
                {{else}}
                value=""
                {{/compare}}>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">简介</label>
            <div class="controls">
                <textarea id="introDesc" name="userComment" class="input-xlarge focused">{{#compare type '==' 'editInfo'}}{{userComment}}{{/compare}}</textarea>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">用户头像</label>
            <div class="controls">
                <#--该input未被后端接受,不要重名-->
                <input id="fileUnUse" name="fileUnUse" class="input-xlarge" type="file">

                <div id="perviewIcon">
                    {{#compare type '==' 'editInfo'}}
                        {{#if avatar}}
                            <img src="{{avatar}}?x-oss-process=image/resize,w_100,h_100/auto-orient,1" alt="">
                        {{/if}}
                    {{/compare}}
                </div>
            </div>
        </div>

        <div class="form-actions">
            {{#compare type '==' 'editInfo'}}
                <button id="edit_userInfo_btn" type="button" class="btn btn-primary">保存</button>
            {{else}}
                <button id="add_sys_user_btn" type="button" class="btn btn-primary">保存</button>
            {{/compare}}
            <a class="btn" href="department.vpage"> 取消 </a>
        </div>
    </form>
</script>
<#--dialog schoolTable-->
<script id="dialogSchoolTemp" type="text/x-handlebars-template">
    <table class="table table-bordered table-striped">
        <colgroup>
            <col class="span2">
            <col class="span6">
            <col class="span1">
            <col class="span2">
            <col class="span1">
        </colgroup>
        <thead>
        <tr>
            <th>学校id</th>
            <th>学校名称</th>
            <th>学段</th>
            {{#if theUserIsManageAble}}
            <th>操作</th>
            {{/if}}
        </tr>
        </thead>
        <tbody>

        {{#each schoolData}}
        <tr>
            <td>{{schoolId}}</td>
            <td>{{schoolName}}</td>
            <td>{{schoolType level}}</td>
            {{#if theUserIsManageAble}}
            <td>
                <button class="btn btn-mini btn-danger js-delSchoolItemBtn" type="button" data-sid="{{schoolId}}"  data-sname="{{schoolName}}">删除</button>
            </td>
            {{/if}}
        </tr>
        {{else}}
        <tr>
            <td colspan="5" style="text-align: center;">暂无</td>
        </tr>
        {{/each}}
        </tbody>
    </table>
</script>
<#--添加子部门-->
<script id="addSubDepartmentTemp" type="text/x-handlebars-template">
    <form class="form-horizontal">
        <div class="control-group">
            <label class="control-label" for="focusedInput">所属部门</label>
            <div class="controls">
                <input id="parentDepartment_name" class="input-xlarge focused" type="text" value="{{pGroupName}}" {{#if pGroupName}} disabled {{/if}}>
                <input id="parentDepartment_id" type="hidden" value="{{pGroupId}}" class="js-subPostData" name="agentGroupId" data-alertInfo="所属部门不能为空">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">部门名称</label>
            <div class="controls">
                <input id="department_name" class="input-xlarge focused js-subPostData" type="text" value="" name="groupName" data-alertInfo="部门名称不能为空">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">部门级别</label>
            <div class="controls">
                <select id="departmentLevel" class="js-subPostData js-groupLevelChoice" name="dpLevel" data-alertInfo="请选择部门级别">
                    <option value="0"></option>
                    {{#each dpLevels}}
                    <option value="{{dplLevId}}">{{dpLName}}</option>
                    {{/each}}
                </select>
            </div>
        </div>
        <div class="control-group businessType">
            <label class="control-label">业务类型</label>
            <div class="controls" style="height:18px;line-height:18px">
                {{#each serviceTypeList}}
                <input type="checkbox"  name ="businessType" value="{{st_key}}" >{{st_value}} &nbsp;&nbsp;&nbsp;
                {{/each}}
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">备注说明</label>
            <div class="controls">
                <textarea id="department_desc" class="js-subPostData" name="description"></textarea>
            </div>
        </div>

        {{#if sub_department_region_icon_show}}
        <div class="control-group">
            <label class="control-label" for="sub_department_region_icon">大区徽章</label>
            <div class="controls">
                <input id="sub_department_region_icon" type="file" name="sub_department_region_icon">
                <div id="sub_department_region_icon_perview">
                    {{#if sub_department_region_icon_Link}}
                    <img src="{{sub_department_region_icon_Link}}" alt="">
                    {{/if}}
                </div>
            </div>
        </div>
        {{/if}}

        <div class="control-group">
            <div class="controls">
                <button class="btn btn-success btn-large" id="createSubDepartmentBtn" type="button" >创建</button>
                <a class="btn btn-default btn-large" href="department.vpage"> 返回 </a>
            </div>
        </div>
    </form>
</script>

<#--弹窗学校区域-->
<script id="dialogAreaSchoolTemp" type="text/x-handlebars-template">
    <table class="table table-bordered table-striped">
        <colgroup>
            <col class="span1">
            <col class="span2">
            <col class="span5">
            <col class="span1">
            <col class="span2">
            <col class="span1">
        </colgroup>
        <thead>
        <tr class="schoolAreaHeaderTr">
            <th>城市</th>
        </tr>
        </thead>
        <tbody>
        {{#each groupRegionData}}
        <tr class="js-regionItem">
            <td>
                {{cityName}}
            </td>
        </tr>
        {{/each}}
        </tbody>
    </table>
</script>

<#--修改信息模板-->
<script id="editInfoDialogTemp" type="text/x-handlebars-template">
    <div class="control-group">
        <label class="control-label" for="editDepName">部门名称</label>
        <div class="controls">
            <input id="editDepName" type="text" value="{{depGroupName}}" name="editDepName">
        </div>
    </div>
    <div class="control-group">
        <label class="control-label" for="focusedInput">部门级别</label>
        <div class="controls">
            <select id="departmentLevel" name="dpLevel" class="js-groupLevelChoice">
                <option value="0">请选择</option>
                {{#each groupRoleList}}
                {{#if dplLevId}}
                   {{#compare2 dplLevId gRoleId}}
                        <option class="group_name" value="{{dplLevId}}" selected>{{dpLName}}</option>
                   {{else}}
                        <option value="{{dplLevId}}">{{dpLName}}</option>
                   {{/compare2}}
                 {{/if}}
                {{/each}}
            </select>
        </div>
    </div>
    <div class="control-group businessType">
        <label class="control-label">业务类型</label>
        <div class="controls" style="height:18px;line-height:18px">
            {{#each serviceTypeList}}
            <input type="checkbox"  name ="businessType" value="{{st_key}}" {{#if st_show}}checked{{/if}}>{{st_value}} &nbsp;&nbsp;&nbsp;
            {{/each}}
        </div>
    </div>
    <div class="control-group">
        <label class="control-label" for="focusedInput">备注说明</label>
        <textarea id="editDepDesc" class="controls" style="margin-left: 21px;">{{depGroupDesc}}</textarea>
    </div>
    {{#if region_icon_show}}
    <div class="control-group">
        <label class="control-label" for="region_icon">大区徽章</label>
        <div class="controls">
            <input id="region_icon" type="file" name="region_icon">
            <div id="region_icon_perview">
                {{#if iconLink}}
                <img src="{{iconLink}}" alt="">
                {{/if}}
            </div>
        </div>
    </div>
    {{/if}}
</script>

<#--用户详情头-->
<script id="userDetailHeaderTemp" type="text/x-handlebars-template">
{{#if canOperation}}
    <div class="opeTabs">
        {{#compare isCityManage '!=' true}}
        <button class="btn btn-large btn-primary" type="button" id="editUserBtn" data-uid="{{userId}}" data-gpid="{{groupId}}">修改信息</button>
        <button class="btn btn-large btn-primary" type="button" id="updateUsableCashAmountBtn" data-uid="{{userId}}"
                data-gpid="{{groupId}}">修改可用余额
        </button>
        {{/compare}}
        {{#if theUserIsManageAble}}
            <button class="btn btn-large btn-primary" type="button" id="addSchoolBtn" data-uid="{{userId}}" data-gpid="{{groupId}}">添加学校</button>
        {{/if}}
        {{#compare isCityManage '!=' true}}
        <button class="btn btn-large btn-primary" type="button" id="updateDepBtn" data-uid="{{userId}}" data-gpid="{{groupId}}">调整部门</button>
        <button class="btn btn-large btn-primary" type="button" id="updateRoleBtn" data-uid="{{userId}}" data-gpid="{{groupId}}">调整角色</button>
        <button class="btn btn-large btn-primary" type="button" id="resetUserPsdBtn" data-uid="{{userId}}" data-gpid="{{groupId}}">重置密码</button>
        <a class="btn btn-large btn-primary" href="/user/orgconfig/exportResponsibleSchoolExcel.vpage?agentUserId={{userId}}&agentGroupId={{groupId}}" target="_blank">下载负责学校</a>
        <button class="btn btn-large btn-danger" type="button" id="closeAccountBtn" data-uid="{{userId}}" data-gpid="{{groupId}}">关闭账号</button>
        {{/compare}}
    </div>
    <hr>
{{/if}}
</script>

<#--用户详情主体-->
<script id="userDetailContentTemp" type="text/x-handlebars-template">
    <form class="form-horizontal">
        <div class="row-fluid">
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">姓名：</label>
                    <div name="userDetail_name" class="controls span6">{{realName}}</div>
                    <input type="hidden" value="{{userId}}" id="userDetailId">
                </div>
            </div>
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">账号：</label>
                    <div class="controls">
                        <div name="userDetail_account">{{accountName}}</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">所属部门：</label>
                    <div name="userDetail_groupName" class="controls span6">{{groupName}}</div>
                </div>
            </div>
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">角色：</label>
                    <div class="controls">
                        <div name="userDetail_role">{{userRole}}</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">电话：</label>
                    <div name="userDetail_phone" class="controls span6">{{tel}}</div>
                </div>
            </div>
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">工号：</label>
                    <div class="controls">
                        <div name="userDetail_account">{{accountNumber}}</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">蜂巢账号：</label>
                    <div name="userDetail_phone" class="controls span6">
                        <span>{{honeycombMobile}}</span>
                        {{#if honeycombMobile}}
                            <@apptag.pageElement elementCode="d25cf2db177b4b29">
                                <button class="btn btn-danger" type="button" id="unBind" data-uid="{{userId}}" data-gpid="{{groupId}}">解除绑定</button></div>
                            </@apptag.pageElement>
                        {{/if}}
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">地址：</label>
                    <div class="controls">
                        <div name="userDetail_address">{{address}}</div>
                    </div>
                </div>
            </div>
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">保证金：</label>
                    <div name="userDetail_cashDeposit" class="controls span6">{{cashDeposit}}</div>
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">开户行：</label>
                    <div class="controls">
                        <div name="userDetail_bankName">{{bankName}}</div>
                    </div>
                </div>
            </div>
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">开户人：</label>
                    <div name="userDetail_bankHostName" class="controls span6">{{bankHostName}}</div>
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">银行账号：</label>
                    <div class="controls">
                        <div name="userDetail_bankAccount">{{bankAccount}}</div>
                    </div>
                </div>
            </div>
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">合同开始日期：</label>
                    <div name="userDetail_contractStartDate" class="controls span6">{{contractStartDate}}</div>
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">合同结束日期：</label>
                    <div class="controls">
                        <div name="userDetail_contractEndDate">{{contractEndDate}}</div>
                    </div>
                </div>
            </div>
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">合同编号：</label>
                    <div name="userDetail_contractNumber" class="controls span6">{{contractNumber}}</div>
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <#--<div class="span6">-->
                <#--<div class="control-group">-->
                    <#--<label class="control-label" for="focusedInput" style="padding:0">物料预算：</label>-->
                    <#--<div style="margin-top:-10px" name="userDetail_contractNumber" class="controls span6">{{money materielBudget}}<input type="button" style="margin-left:15px" value="查看明细" class="type1 btn btn-large btn-primary"/></div>-->
                <#--</div>-->
            <#--</div>-->
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">可用余额：</label>
                    <div style="margin-top:-10px" name="userDetail_contractNumber" class="controls span6">{{money usableCashAmount}}<input type="button" style="margin-left:15px" value="查看明细" class="type2 btn btn-primary"/></div>
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span12">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">简介：</label>
                    <div name="userDetail_userComment" class="controls span6">{{userComment}}</div>
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span2">
                <div class="control-group">
                    <label class="control-label" for="focusedInput" style="padding:0">负责{{#if thisUserIsManager}}区域{{else}}学校{{/if}}：</label>
                </div>
            </div>
            <div class="span10 sortable ui-sortable">
                <#--<div class="control-group">-->
                    <#--<input id="userDetail_searchBtn" class="input-xlarge focused" type="text" value="" placeholder="搜索">-->
                <#--</div>-->
                <div class="control-group">
                    <div id="userDetailRegionTable" class="dataTables_wrapper" role="grid" ></div>
                </div>
            </div>
        </div>
    </form>
</script>

<#--schoolTableTemp-->
<script id="updateUserRoleTemp" type="text/x-handlebars-template">
    <select name="" id="updateUserRoleSel">
        {{#each roleList}}
            <option value="{{uroleId}}">{{uroleName}}</option>
        {{/each}}
    </select>
</script>

<#--选择学校table-->
<script id="chooseSchoolTableTemp" type="text/x-handlebars-template">
    <table class="table table-bordered table-striped">
        <thead>
        <tr>
            <th>学校id</th>
            <th>学校名称</th>
            <th>中/小学</th>
            <th>负责人</th>
        </tr>
        </thead>
        <tbody>
        {{#each schoolData}}
        <tr>
            <td class="js-schoolIds" >{{schoolId}}</td>
            <td >{{schoolName}}</td>
            <td >{{schoolType schoolLevel}}</td>
            {{#if enabled}}
                {{#if userName}}
                    <td style="color: red;" class="js-enableFlag">
                {{else}}
                    <td>
                {{/if}}
                {{userName}}</td>
            {{else}}
                <td>{{userName}}</td>
            {{/if}}
            <#--<td style="color: red;">{{userName}}</td>-->
        </tr>
        {{/each}}
        </tbody>
    </table>
    <div class="pull-left" style="padding-top: 10px;">共计 <span style="color: red;">{{#if totalNo}}{{totalNo}}{{else}}0{{/if}}</span> 所学校</div>
</script>

<#--调整部门模板-->
<script id="updateDepDialogTemp" type="text/x-handlebars-template">
<select name="newRoleName_dialog" id="newRoleName_dialog">
    <option value="0">请选择</option>
    {{#each roleList}}
    <option value="{{uroleId}}">{{uroleName}}</option>
    {{/each}}
</select>
</script>

<script id="responsibleGeneralTemp" type="text/x-handlebars-template">
    {{#if responsibleGeneral}}
    <div class="row-fluid">
        <div class="areaDetailTitle span2">负责概况：</div>
        <div class="areaDetailContent span10">
            <div class="dataTables_wrapper">
                <table class="table table-bordered table-striped bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" rowspan="2">负责人</th>
                        <th class="sorting" colspan="3">小学</th>
                        <th class="sorting" colspan="3">初中</th>
                        <th class="sorting" colspan="3">高中</th>
                        <th class="sorting" rowspan="2">学校总数</th>
                    </tr>
                    <tr>
                        <th class="sorting">学校数</th>
                        <th class="sorting">英语目标</th>
                        <th class="sorting">数学目标</th>
                        <th class="sorting">学校数</th>
                        <th class="sorting">英语目标</th>
                        <th class="sorting">数学目标</th>
                        <th class="sorting">学校数</th>
                        <th class="sorting">英语目标</th>
                        <th class="sorting">数学目标</th>
                    </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                    {{#each responsibleGeneral}}
                    <tr>
                        <td class="center sorting_1">
                            {{principal}}
                        </td>
                        <td class="center sorting_1">
                            {{juniorSchoolCount}}
                        </td>
                        <td class="center sorting_1">
                            {{juniorEngBud}}
                        </td>
                        <td class="center sorting_1">
                            {{juniorMathBud}}
                        </td>
                        <td class="center sorting_1">
                            {{middleSchoolCount}}
                        </td>
                        <td class="center sorting_1">
                            {{middleEngBud}}
                        </td>
                        <td class="center sorting_1">
                            {{middleMathBud}}
                        </td>
                        <td class="center sorting_1">
                            {{highSchoolCount}}
                        </td>
                        <td class="center sorting_1">
                            {{highEngBud}}
                        </td>
                        <td class="center sorting_1">
                            {{highMathBud}}
                        </td>
                        <td class="center sorting_1">
                            {{totalSchoolCount}}
                        </td>
                    </tr>
                    {{/each}}
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    {{/if}}
</script>

<script id="undistributedSchoolTemp" type="text/x-handlebars-template">
    {{#if undistributedSchool}}
    <div class="row-fluid">
        <div class="areaDetailTitle span2">学校名单：</div>
        <div class="areaDetailContent span10">
            <div class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable"
                       id="DataTables_Table_0"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                    <tr>
                        <th class="sorting">学校id</th>
                        <th class="sorting">学校名称</th>
                        <th class="sorting">地区</th>
                        <th class="sorting">阶段</th>
                        <th class="sorting">英语目标</th>
                        <th class="sorting">数学目标</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
    {{/if}}
</script>

<#--导入业绩目标模板-->
<script id="importAgentPerformanceGoalTemp" type="text/x-handlebars-template">
    <div id="loadingDiv" style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
        <p style="text-align: center;top: 30%;position: relative;">正在上传，请等待……</p>
    </div>
    <div class="row-fluid sortable ui-sortable">
        <div class="alert alert-error" hidden>
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong id="error-panel"></strong>
        </div>
        <div class="alert alert-info" hidden>
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong id="info-panel"></strong>
        </div>
        <div class="box span12">
            <div class="box-header well" data-original-title="">
                <h2><i class="icon-th"></i> 业绩目标导入</h2>
                <div class="box-icon">
                    <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                    <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
                </div>
                <div class="pull-right">
                    <a href="javascript:void(0)" class="js-isSaveBtn btn btn-success"><i class="icon-plus icon-white"></i>提交</a>&nbsp;&nbsp;
                </div>
            </div>

            <div class="box-content ">
                <form id="importSchoolDict" method="post" enctype="multipart/form-data"
                      action="/kpi/budget/import_budget.vpage" data-ajax="false"
                      class="form-horizontal">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">上传excel</label>
                        <div class="controls">
                            <input id="sourceFile" name="sourceFile" type="file">
                            &nbsp;&nbsp;&nbsp;&nbsp;
                            <a href="/kpi/budget/download_template.vpage" class="btn btn-primary">下载导入模版</a>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</script>
<#--导出业绩目标模板-->
<script id="exportAgentPerformanceGoalTemp" type="text/x-handlebars-template">
    <div id="loadingDiv"
         style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
        <p style="text-align: center;top: 30%;position: relative;">正在下载，请等待……</p>
    </div>
    <div class="row-fluid sortable ui-sortable">
        <div class="alert alert-error" hidden>
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong id="error-panel"></strong>
        </div>
        <div class="alert alert-info" hidden>
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong id="info-panel"></strong>
        </div>
        <div class="box span12">
            <div class="box-header well" data-original-title="">
                <h2><i class="icon-th"></i>导出业绩目标</h2>
                <div class="box-icon">
                    <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                    <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
                </div>
            </div>
            <div class="box-content ">
                <form id="exportSchoolDict" method="get" enctype="multipart/form-data"
                      action="/kpi/budget/export_budget.vpage" data-ajax="false"
                      class="form-horizontal">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">选择月份</label>
                        <div class="controls">
                            <input type="text" style="display: none" name="groupId" value="{{groupId}}">
                            <input type="text" class="reportMonth input-small checkData" id="month" name="month"
                                   value="201803" data-info="请选择月份">
                            <input type="submit" id="export" value="导出">
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</script>

<#--业绩目标模板-->
<script id="agentPerformanceGoalTemp" type="text/x-handlebars-template">
    <#if requestContext.getCurrentUser().isCountryManager()>
        <%var isCountry = true%>
    <#else>
        <%var isCountry = false%>
    </#if>
    <%var groupBudget = res.groupBudget%>
    <br/>
    <div class="row-fluid">
        <div class="areaDetailTitle span2">目标管理：</div>
        <div class="areaDetailContent span10">
            <input name="goal" onclick="chooseMonthType(1)" type="radio" <%= (res.month == 1)?"checked":"" %> />本月目标 &nbsp;  <input name="goal" onclick="chooseMonthType(2)" type="radio" <%= (res.month == 2)?"checked":"" %> />下月目标
        </div>
    </div>
    <%var showMau = false%>
    <div class="row-fluid">
        <div class="areaDetailTitle span2">我的目标：</div>
        <div class="areaDetailContent span10">
            <div class="dataTables_wrapper">
                <table class="table table-bordered table-striped bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" rowspan="1">月份</th>
                        <%if(res.kpiTypeList && res.kpiTypeList.length > 0){%>
                            <%for(var i = 0; i< res.kpiTypeList.length; i++){%>
                            <%if((res.kpiTypeList[i].type == 21 || res.kpiTypeList[i].type == 20) && !showMau){
                                showMau = true;
                            }%>
                            <th class="sorting" colspan="1"><%=res.kpiTypeList[i].desc%></th>
                            <%}%>
                            <%if(showMau){%>
                                <th class="sorting" rowspan="1">中英月活</th>
                            <%}%>
                        <%}%>
                    </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                    <%if(groupBudget.kpiBudgetList && groupBudget.kpiBudgetList.length > 0){%>
                    <%var showMauNum = 0%>
                        <tr>
                            <td class="center sorting_1"><%=res.groupBudget.month%></td>
                            <%for (var i=0;i < groupBudget.kpiBudgetList.length;i++){%>
                                <%var subBudgetList = groupBudget.kpiBudgetList[i]%>
                            <#--中英回流+中英新增 === 中英月活-->
                            <%if(subBudgetList.kpiType == 21 || subBudgetList.kpiType == 20){
                             showMauNum += subBudgetList.budget
                            }%>
                                <td class="center sorting_1 budgetSummary_<%=i%>"><%=subBudgetList.budget%></td>
                            <%}%>
                            <%if(showMau){%>
                                <td class="center sorting_1 budgetSummary_20"><%=showMauNum%></td>
                            <%showMauNum = 0%>
                            <%}%>
                        </tr>
                    <%}%>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="areaDetailTitle span2">
            目标：
        </div>
        <div class="areaDetailContent span10">
            <div class="dataTables_wrapper">
                <table class="table table-bordered table-striped bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" rowspan="1">月份</th>
                        <th class="sorting" rowspan="1">
                            名称
                        </th>
                        <%if(res.kpiTypeList && res.kpiTypeList.length > 0){%>
                            <%for(var i = 0; i< res.kpiTypeList.length; i++){%>
                                <th class="sorting" colspan="1"><%=res.kpiTypeList[i].desc%></th>
                            <%}%>
                            <%if(showMau){%>
                            <th class="sorting" rowspan="1">中英月活</th>
                            <%}%>
                        <%}%>
                        <th class="sorting" rowspan="1">操作</th>
                    </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                    <%if(res.subBudgetList && res.subBudgetList.length > 0){%>
                        <%var showMauNum = 0%>
                        <%for(var i = 0; i< res.subBudgetList.length; i++){%>
                            <%var subLen = res.subBudgetList[i]%>
                            <tr>
                                <td class="center sorting_1"><%=subLen.month%></td>
                                <td class="center sorting_1">
                                    <%if (subLen.groupOrUser == '1'){%>
                                        <%=subLen.groupName%>
                                    <%}else if(subLen.groupOrUser == '2'){%>
                                        <%=subLen.userName%>
                                    <%}%>
                                </td>
                                <%if(subLen.kpiBudgetList && subLen.kpiBudgetList.length > 0){%>
                                    <%for(var j = 0;j< subLen.kpiBudgetList.length; j++){%>
                                        <%var subBudget = subLen.kpiBudgetList[j]%>
                                        <#--中英回流+中英新增 === 中英月活-->
                                        <%if(subBudget.kpiType == 21 || subBudget.kpiType == 20){
                                            showMauNum += subBudget.budget
                                        }%>
                                        <td class="center sorting_1 budget_<%=j%>"><%=subBudget.budget%></td>
                                    <%}%>
                                    <%if(showMau){%>
                                        <td class="center sorting_1 budget_20"><%=showMauNum%></td>
                                        <%showMauNum = 0%>
                                    <%}%>
                                    <#--<td class="center sorting_1"><%=(subLen.kpiBudgetList[2].budget || 0) + (subLen.kpiBudgetList[3].budget || 0)%></td>-->
                                <%}%>
                                <td class="center sorting_1">
                                    <%if (!subLen.confirmed || isCountry){%>
                                    <button class="aBtn btn btn-small btn-primary" data-regiongroupid="<%=subLen.groupId%>" data-userid="<%=subLen.userId%>" data-grouporuser="<%=subLen.groupOrUser%>" data-month="<%=subLen.month%>">修改</button>
                                    <%}%>
                                    <%if (!subLen.confirmed && isCountry && res.groupRoleName == 'BusinessUnit'){%>
                                    <button class="sure_btn btn btn-small btn-primary" data-region="<%=subLen.groupName%>" data-regiongroupid="<%=subLen.groupId%>" data-groupOrUser="<%=subLen.groupOrUser%>" data-month="<%=subLen.month%>">确认</button>
                                    <%}%>
                                    <button class="history_btn btn btn-small btn-primary" data-regiongroupid="<%=subLen.groupId%>" data-userid="<%=subLen.userId%>" data-type="<%=subLen.groupOrUser%>" data-month="<%=subLen.month%>">查看调整记录</button>
                                </td>
                            </tr>
                        <%}%>
                    <%}%>
                    <tr>
                        <td class="center sorting_1"><%=res.groupBudget.month%></td>
                        <td class="center sorting_1">未分配</td>
                        <td class="center sorting_1 budget_sum_0">
                        </td>
                        <td class="center sorting_1 budget_sum_1">
                        </td>
                        <td class="center sorting_1 budget_sum_2">
                        </td>
                        <td class="center sorting_1 budget_sum_3"></td>
                        <td></td>
                        <td></td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</script>

<#--修改信息模板-->
<script id="changeTargetDialogTemp" type="text/html">
    <input type="text" name="groupId" value="<%=res.budgetDetail.groupId%>" style="display: none;"/>
    <input type="text" name="userId" value="<%=res.budgetDetail.userId%>" style="display: none;"/>
    <div class="control-group">
        <label class="control-label">月份</label>
        <div class="controls month" style="margin-top:5px" data-month="<%=res.budgetDetail.month%>"><%=res.budgetDetail.month%></div>
    </div>
    <div class="control-group">
        <label class="control-label">角色</label>
        <div class="controls groupOrUser" data-type="<%=res.budgetDetail.groupOrUser%>">
            <%if(res.budgetDetail.groupOrUser == 1){%>部门<%}%>
            <%if(res.budgetDetail.groupOrUser == 2){%>专员<%}%>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">名称</label>
        <div class="controls">
            <%=res.budgetDetail.groupName%>
        </div>
    </div>
    <%for (var i = 0; i < res.budgetDetail.kpiBudgetList.length;i++){%>
        <%var kpiBudgetList = res.budgetDetail.kpiBudgetList[i]%>
        <div class="control-group">
            <label class="control-label"><%=kpiBudgetList.kpiTypeDesc%></label>
            <div class="controls">
                <input class="kpiBudgetData" name="budget" type="text" value="<%=kpiBudgetList.budget%>" data-info="<%=kpiBudgetList.kpiType%>">
            </div>
        </div>
    <%}%>
    <div class="control-group">
        <label class="control-label">修改原因</label>
        <textarea class="controls changeReason" style="margin-left: 21px;"></textarea>
    </div>
</script>
<#--业绩目标修改记录-->
<script id="TargetHistoryDialogTemp" type="text/html">
    <div class="row-fluid" style="max-height: 200px;">
        <div class="areaDetailTitle span2">修改记录：</div>
        <div class="areaDetailContent span10">
            <div class="dataTables_wrapper">
                <table class="table table-bordered table-striped bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" rowspan="1">修改人</th>
                        <th class="sorting" colspan="1">修改时间</th>
                        <th class="sorting" colspan="1">指标</th>
                        <th class="sorting" colspan="1">修改前</th>
                        <th class="sorting" rowspan="1">修改后</th>
                        <th class="sorting" rowspan="1">修改幅度</th>
                        <th class="sorting" rowspan="1">修改原因</th>
                    </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                    <%for(var i = 0; i< res.length; i++){%>
                        <%var resLen = res[i]%>
                        <tr>
                            <td class="center sorting_1"><%=resLen.operatorName%></td>
                            <td class="center sorting_1"><%=resLen.updateTime%></td>
                            <td class="center sorting_1"><%=resLen.kpiType%></td>
                            <td class="center sorting_1"><%=resLen.beforeChange%></td>
                            <td class="center sorting_1"><%=resLen.afterChange%></td>
                            <td class="center sorting_1"><%=resLen.afterChange - resLen.beforeChange%></td>
                            <td class="center sorting_1"><%=resLen.comment%></td>
                        </tr>
                    <%}%>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</script>

<script type="text/javascript">
$(function(){
    $(document).on("click",'#unBind',function () {
        var data = {
            userId:$("#unBind").data("uid")
        }
        var _index = layer.confirm('是否解除绑定？', {
            btn: ['确定','取消'] //按钮
        }, function(){
            $.ajax({
                url: "/user/orgconfig/unBindHoneycomb.vpage",
                type: "POST",
                data: data,
                success: function (res) {
                    if (res.success) {
                        layer.alert('解除成功', function() {//关闭后的操作
                            window.location.reload();
                        });
                    } else {
                        layer.alert(res.info);
                    }
                },
                error: function (e) {
                    console.log(e);
                }
            });
        }, function(){
            layer.close(_index)
        });
    });
});

function unBindCheck() {
    
}
</script>
