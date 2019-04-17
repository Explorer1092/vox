<#import "../../layout_default.ftl" as layout_default>
<#import "../headsearch.ftl" as headsearch>
<#--<#import "parentquery.ftl" as parentQuery>-->
<@layout_default.page page_title="${parentInfoAdminMapper.parent.profile.realname!'家长主页'}(${parentInfoAdminMapper.parent.id!''})" page_num=3>
<div class="span9" xmlns="http://www.w3.org/1999/html">
    <@headsearch.headSearch/>
<#--<@parentQuery.queryPage/>-->
    <legend>家长主页:${parentInfoAdminMapper.parent.profile.realname!'家长主页'}(${parentInfoAdminMapper.parent.id!''})</legend>
    <ul class="inline">
        <li>
            <button class="btn" onclick="resetPassword()">重置密码</button>
        </li>
        <li>
            <button class="btn" onclick="bindStudent()">绑定孩子</button>
        </li>
        <li>
            <a class="btn" href="../user/wechatnoticelist.vpage?userId=${parentInfoAdminMapper.parent.id!''}">微信消息历史</a>
        </li>
        <li>
            <a class="btn" href="javascript:void(0)" onclick="kickOutOfApp(${(parentInfoAdminMapper.parent.id)!''})">App重新登录</a>
        </li>
        <li>
            <a id="btnBindMobile" class="btn" href="javascript:void(0)">绑定手机号</a>
        </li>
        <li>
            <a class="btn" href="javascript:void(0)" onclick="openFaultOrderDialog()">问题追踪</a>
        </li>
        <li>
            <a class="btn" href="../finance/financedetail.vpage?userId=${parentInfoAdminMapper.parent.id!}">学贝相关</a>
        </li>
        <li>
            <button class="btn" onclick="generateReward()">生成家长奖励</button>
        </li>
        <li>
            <button class="btn" onclick="delParentExtInfo()">删除家长学历和职业信息</button>
        </li>
        <li>
            <a class="btn" href="/crm/userlevel/parentuserlevel.vpage?userId=${parentInfoAdminMapper.parent.id!}">家长等级详情</a>
        </li>
        <li>
            <a class="btn" href="javascript:void(0)" onclick="unbindWechatAppLogin(${(parentInfoAdminMapper.parent.id)!''})">解绑App微信登录</a>
        </li>
        <li>
            <a class="btn btn-warning" href="javascript:void(0)" onclick="allowPay(${(parentInfoAdminMapper.parent.id)!''})">支付限制解除</a>
        </li>
        <li>
            <a class="btn btn-danger" onclick="javascript:$('#delete_user_dialog').modal('show');">注销账号</a>
        </li>

    </ul>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <td>ID</td>
            <td>创建时间/最近登录时间</td>
            <td>姓名</td>
            <td>密码</td>
            <td>手机</td>
        <#--<td>邮箱</td>-->
            <td>是否验证手机</td>
        <#--<td>是否验证邮箱</td>-->
            <td>家长端APP</td>
            <td>家长黑名单状态</td>
        </tr>
        <tr>
            <td>${parentInfoAdminMapper.parent.id!''}</td>
            <td>
                ${parentInfoAdminMapper.parent.createTime!""} <br/>
                ${parentInfoAdminMapper.lastLoginTime!""}
            </td>
            <td>${parentInfoAdminMapper.parent.profile.realname!''}</td>
            <td>
                <span id="real_code"></span>
                <#if parentInfoAdminMapper.parent?? && parentInfoAdminMapper.parent.id??>
                    <button type="button" id="query_user_password_${parentInfoAdminMapper.parent.id!''}"
                            class="btn btn-info">临时密码
                    </button>
                </#if>
            </td>
            <td>
                <button type="button" id="query_user_phone_${parentInfoAdminMapper.parent.id}" class="btn btn-info">查看
                </button>
            </td>
        <#--<td><button type="button" id="query_user_email_${parentInfoAdminMapper.parent.id}" class="btn btn-info">查看</button></td>-->
            <td>${parentInfoAdminMapper.verifyMobile?string('是', '')}</td>
        <#--<td>${parentInfoAdminMapper.verifyEmail?string('是', '')}</td>-->
            <td>
                <#if parentInfoAdminMapper.vendorAppsUserRef??>
                    已使用
                    <br/>
                    开始使用时间：
                    <br/>
                ${parentInfoAdminMapper.vendorAppsUserRef.createDatetime!""}
                <#else>
                    未使用
                </#if>
            </td>
            <td>
                ${(parentInfoAdminMapper.blackStatus)!""}
            </td>
        </tr>
    </table>
    <br>
    <br>

    <legend>学分列表
        <button type="button" id="modifycredit" data-userId="${parentInfoAdminMapper.parent.id!''}"
                class="btn btn-info">修改学分
        </button>
    </legend>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>学生id</th>
            <th>总学分</th>
            <th>可用学分</th>
            <th>创建时间</th>
            <th>更新时间</th>
        </tr>
        <tbody>
            <#if credit?has_content >
            <tr>
                <td>${credit.userId!""}</td>
                <td>${credit.totalCredit!""}</td>
                <td>${credit.usableCredit!""}</td>
                <td>${credit.createDatetime!""}</td>
                <td>${credit.updateDatetime!""}</td>
            </tr>
            </#if>
        </tbody>
    </table>
    <br>
    <br>

    <legend>学分历史列表</legend>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>学生id</th>
            <th>增加学分</th>
            <th>创建前总学分</th>
            <th>创建可用学分</th>
            <th>创建后总学分</th>
            <th>创建后用学分</th>
            <th>修改时间</th>
            <th>备注</th>
        </tr>
        <tbody>
            <#if creditHistories?has_content >
                <#list creditHistories as creditHistory >
                <tr>
                    <td>${creditHistory.userId!""}</td>
                    <td>${creditHistory.amount!""}</td>
                    <td>${creditHistory.totalCreditBefore!""}</td>
                    <td>${creditHistory.usableCreditBefore!""}</td>
                    <td>${creditHistory.totalCreditAfter!""}</td>
                    <td>${creditHistory.usableCreditAfter!""}</td>
                    <td>${creditHistory.updateDatetime!""}</td>
                    <td>${creditHistory.comment!""}</td>
                </tr>
                </#list>
            </#if>
        </tbody>
    </table>
    <br>
    <br>

    <legend>用户备注</legend>
    <ul class="inline">
        <li>
            <button class="btn" onclick="addCustomerServiceRecord()">新增备注</button>
        </li>
        <li>
            <button id="hide_record_btn" class="btn" onclick="hideCustomerServiceRecord()">隐藏备注</button>
        </li>
    </ul>
    <table id="customer_service_record" class="table table-hover table-striped table-bordered">
        <tr id="comment_title">
            <th>用户ID</th>
            <th>添加人</th>
            <th>创建时间</th>
            <th>操作内容</th>
            <th>备注</th>
            <th>类型</th>
        </tr>
        <#list parentInfoAdminMapper.customerServiceRecordList as record >
            <tr>
                <td style="width: 6em;">${record.userId!""}</td>
                <td style="width: 6em;">${record.operatorId!""}</td>
                <td style="width: 10em;">${record.createTime!""}</td>
                <td style="width: 10em;">${record.operationContent!""}</td>
                <td style="width: 20em;">${record.comments!""}</td>
                <td style="width: 6em;">${record.operationType!""}</td>
            </tr>
        </#list>
    </table>
    <br>
    <br>
    <legend>孩子列表</legend>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>孩子ID</th>
            <th>孩子姓名</th>
            <th>孩子班级</th>
            <th>孩子学校</th>
            <th>是否是关键家长</th>
        </tr>
        <#if parentInfoAdminMapper.childList??>
            <#list parentInfoAdminMapper.childList as child>
                <tr>
                    <td>${child.childId!''}</td>
                    <td>
                        <a href="../student/studenthomepage.vpage?studentId=${child.childId!''}">${child.childName!''}</a>
                    </td>
                    <td>
                        <#if child.clazzId??>
                        ${child.clazzLevel!''}年级${child.clazzName!''}(${child.clazzId!''})
                        </#if>
                    </td>
                    <td>
                        <#if child.schoolId??>
                            <a href="../school/schoolhomepage.vpage?schoolId=${child.schoolId!''}">${child.schoolName!''}</a>(${child.schoolId!''})
                        </#if>
                    </td>
                    <td>${child.keyParent?string('关键家长','')}</td>
                </tr>
            </#list>
        </#if>
    </table>
    <legend>学豆订单列表</legend>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>ID</th>
            <th>价格</th>
            <th>数量</th>
            <th>时间</th>
        </tr>
        <#if parentInfoAdminMapper.integralOrders??>
            <#list parentInfoAdminMapper.integralOrders as order>
                <tr>
                    <td>${order.id!''}</td>
                    <td>${order.payAmount!''}</td>
                    <td>
                    ${order.integral!''}
                    </td>
                    <td>${order.updateTime?datetime!''}</td>
                </tr>
            </#list>
        </#if>
    </table>
    <legend>订单列表</legend>
    <a href="${requestContext.webAppContextPath}/legacy/afenti/main.vpage?userId=${parentInfoAdminMapper.parent.id!}"
       class="btn">更多订单</a>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>订单号</th>
            <th>创建时间</th>
            <th>产品服务类型</th>
            <th>产品名称</th>
            <th>应付金额</th>
        </tr>
        <#if parentInfoAdminMapper.latestOrderList??>
            <#list parentInfoAdminMapper.latestOrderList as order>
                <tr>
                    <td>${order.id!''}</td>
                    <td>${order.createDatetime!''}</td>
                    <td>${order.orderProductServiceType!''}</td>
                    <td>${order.productName!''}</td>
                    <td>${order.orderPrice!''}</td>
                </tr>
            </#list>
        </#if>
    </table>
    <legend>优惠劵列表</legend>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>优惠劵ID</th>
            <th>名称</th>
            <th>类型</th>
            <th>折扣力度</th>
            <th>获取时间</th>
            <th>有效期</th>
            <th>状态</th>
            <th>发放人</th>
            <th>获取渠道</th>
            <th>使用时间</th>
            <th>使用人</th>

        </tr>
        <#if parentInfoAdminMapper.couponList??>
            <#list parentInfoAdminMapper.couponList as coupon>
                <tr>
                    <td>${coupon.couponId!''}</td>
                    <td>${coupon.couponName!''}</td>
                    <td>${coupon.couponType.getDesc()!''}</td>
                    <td>${coupon.typeValue!''}</td>
                    <td>${coupon.createDate!''}</td>
                    <td>${coupon.effectiveDateStr!''}</td>
                    <td>${coupon.couponUserStatus.getDesc()!''}</td>
                    <td>${coupon.sendUser!''}</td>
                    <td>${coupon.channel!''}</td>
                    <td>${coupon.usedTime!''}</td>
                    <td>${coupon.usedUser!''}</td>
                </tr>
            </#list>
        </#if>
    </table>
    <legend>App微信登录绑定历史列表</legend>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>OPEN_ID</th>
            <th>UNION_ID</th>
            <th>绑定时间</th>
            <th>解绑时间</th>
            <th>状态</th>

        </tr>
        <#if parentInfoAdminMapper.wechatRefList??>
            <#list parentInfoAdminMapper.wechatRefList as wechat>
                <tr>
                    <td>${wechat.openId!''}</td>
                    <td>${wechat.unionId!''}</td>
                    <td>${wechat.createDatetime}</td>
                    <td><#if wechat.disabled?string == "true">${wechat.updateDatetime}</#if></td>
                    <td><#if wechat.disabled?string == "true">无效<#else >有效</#if></td>
                </tr>
            </#list>
        </#if>
    </table>

    <!----------------------------dialog----------------------------------------------------------------------------------->
    <div id="fault_order_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>选择追踪项-家长</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户ID</dt>
                        <dd>${parentInfoAdminMapper.parent.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dd><input type="checkbox" name="faultType" value="2">用户登录-app</dd>
                        <dd><input type="checkbox" name="faultType" value="3">绑定手机</dd>

                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>追踪备注</dt>
                        <dd><textarea id="fault_order_create_info" cols="35" rows="5"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="dialog_add_fault_order" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="password_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>重置密码</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户ID</dt>
                        <dd>${parentInfoAdminMapper.parent.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>记录类型</dt>
                        <dd>家长操作</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>新的密码</dt>
                        <dd><input type="text" name="password" id="password" value="123456"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd>
                            <div class="btn-group" data-toggle="buttons-radio">
                                <button type="button" class="btn active">TQ在线</button>
                                <button type="button" class="btn">TQ电话</button>
                                <button type="button" class="btn">其他</button>
                            </div>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>附加描述</dt>
                        <dd><textarea id="passwordExtraDesc" cols="35" rows="2"></textarea></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>所做操作</dt>
                        <dd>重置用户密码。</dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="dialog_edit_parent_password" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="record_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>用户进线记录</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户ID</dt>
                        <dd>${parentInfoAdminMapper.parent.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>记录类型</dt>
                        <dd>
                            <select name="recordType" id="recordType" class="multiple">
                                <#if recordTypeList?has_content>
                                    <#list recordTypeList as recordType>
                                        <option value='${recordType.key}'>${recordType.value}</option>
                                    </#list>
                                </#if>
                            </select>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd><textarea id="questionDesc" name="questionDesc" cols="35" rows="3"></textarea></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>所做操作</dt>
                        <dd><textarea id="operation" name="operation" cols="35" rows="3"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="dialog_edit_parent_date" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="bind_student_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>绑定家长孩子</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户ID</dt>
                        <dd>${parentInfoAdminMapper.parent.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>记录类型</dt>
                        <dd>家长操作</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>孩子学号</dt>
                        <dd><input type="text" name="studentId" id="studentId" value=""/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>家长称呼</dt>
                        <dd>
                            <select id="parentCallName">
                                <option value="爸爸">爸爸</option>
                                <option value="妈妈">妈妈</option>
                                <option value="爷爷">爷爷</option>
                                <option value="奶奶">奶奶</option>
                                <option value="姥爷">姥爷</option>
                                <option value="姥姥">姥姥</option>
                                <option value="其它监护人">其他</option>
                            </select>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd>
                            <div class="btn-bind-group" data-toggle="buttons-radio">
                                <button type="button" class="btn active">TQ在线</button>
                                <button type="button" class="btn">TQ电话</button>
                                <button type="button" class="btn">其他</button>
                            </div>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>附加描述</dt>
                        <dd><textarea id="bindExtraDesc" cols="35" rows="2"></textarea></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>所做操作</dt>
                        <dd>绑定家长学生。</dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="dialog_bind_student_parent" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="modifycredit_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>修改家长学分</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>家长ID</dt>
                        <dd>${parentInfoAdminMapper.parent.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>增加或减少的学分</dt>
                        <dd>
                            <input id="modifycreditnum" value=""/>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>备注</dt>
                        <dd><textarea id="modifycreditdesc" name="modifycreditdesc" cols="200" rows="5"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="modifycredit_dialog_btn" data-userId="${parentInfoAdminMapper.parent.id!''}"
                    class="btn btn-primary">修 改
            </button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <!-- 绑定手机 -->
    <div id="bind_mobile_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>绑定手机</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>家长ID</dt>
                        <dd>${parentInfoAdminMapper.parent.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>手机</dt>
                        <dd><input type="text" id="txtMobile"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>备注</dt>
                        <dd><textarea id="txtBindMobileDesc" cols="35" rows="4"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="bind_mobile_submit" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="generate_reward_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
            <h3>生成家长奖励</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>家长ID</dt>
                        <dd>${parentInfoAdminMapper.parent.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>孩子列表</dt>
                        <dd>
                            <#if parentInfoAdminMapper.childList??>
                                <select id="childrenInfo" name="childrenInfo">
                                    <#list parentInfoAdminMapper.childList as child>
                                        <option value="${child.childId!''}">${child.childName!''}</option>
                                    </#list>
                                </select>
                            </#if>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>奖励类型</dt>
                        <dd>
                            <#if items??>
                                <select id="rewardType" name="rewardType">
                                    <#list items as item>
                                        <option value="${item.key}">${item.title}</option>
                                    </#list>
                                </select>
                            </#if>
                        </dd>
                    </li>
                </ul>
                <ul class="inline" id="extUl">
                    <li>
                        <dt>替换参数</dt>
                        <dd>
                            <input type="text" id="ext">
                        </dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="generate_reward_btn" class="btn btn-primary">确定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="delete_user_dialog" class="modal hide fade">
        <div class="modal-header">
           <h3>注销用户</h3>
        </div>
        <div class="modal-body">
            <p>注销账号后，该用户相关数据(与孩子的绑定关系，学习产品购买使用记录)都会删除且不可恢复，确认注销？</p>
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                    <dt>注销原因</dt>
                    <dd><textarea id="delete_user_reason" name="deleteReason" cols="35" rows="4"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="btn_delete_user" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
</div>
<script>

    function delParentExtInfo() {
        var parentId = '';
        parentId =${parentInfoAdminMapper.parent.id!''};
        if (parentId == '') {
            alert("家长id错误");
            return;
        }
        $.ajax({
            url: "/crm/parent/del_parent_ext_info.vpage",
            type: "POST",
            async: false,
            data: {
                "parentId": parentId
            },
            success: function (data) {
                if (data.success)
                    alert("操作成功");
                else
                    alert(data.info);
            }
        });
    }


    function generateReward() {
        $("#generate_reward_dialog").modal("show");
        var regex = /[\\{]/;
        var content = $("#rewardType option:selected").html();
        if (regex.test(content)) {
            var placeholder = content.substring(content.indexOf("{") + 1, content.indexOf("}"));
            $("#ext").attr("placeholder", placeholder + ":xx");
            $("#extUl").show();
        } else if (content.indexOf("岛") > 0) {
            $("#ext").attr("placeholder", "taskId:xx");
            $("#extUl").show();
        } else {
            $("#extUl").hide();
        }
    }

    function openFaultOrderDialog() {
        $("#fault_order_create_info").val();
        $("#fault_order_dialog").modal("show");
    }

    function resetPassword() {
        $("#password").val("123456");
        $('#passwordExtraDesc').val('');
        $("div[class='btn-group'] button").removeClass("active").eq(0).addClass("active");
        $("#password_dialog").modal("show");
    }

    function addCustomerServiceRecord() {
        $("#questionDesc").val('');
        $("#operation").val('');
        $('#recordType').val('4');
        $("#record_dialog").modal("show");
    }

    function hideCustomerServiceRecord() {
        $("#customer_service_record").toggle(function () {
            var $target = $("#hide_record_btn");
            switch ($target.html()) {
                case "隐藏备注":
                    $target.html("显示备注");
                    break;
                case "显示备注":
                    $target.html("隐藏备注");
                    break;
            }
        });
    }

    function bindStudent() {
        $("#studentId").val("");
        $('#bindExtraDesc').val('');
        $("div[class='btn-bind-group'] button").removeClass("active").eq(0).addClass("active");
        $("#bind_student_dialog").modal("show");
    }

    function appendNewRecord(data) {
        var record = "<tr>" +
                "<td>" + data.customerServiceRecord.userId + "</td>" +
                "<td>" + data.customerServiceRecord.operatorId + "</td>" +
                "<td>" + data.createTime + "</td> " +
                "<td>" + data.customerServiceRecord.operationContent + "</td> " +
                "<td>" + data.customerServiceRecord.comments + "</td> " +
                "<td>" + data.customerServiceRecord.operationType + "</td>  " +
                "</tr> ";

        $("#comment_title").after(record);
    }

    function kickOutOfApp(userId) {
        if (!confirm("确定踢出App重新登录?")) {
            return;
        }
        $.ajax({
            url: "/crm/parent/kickOutOfApp.vpage",
            type: "POST",
            async: false,
            data: {
                "userId": userId
            },
            success: function (data) {
                if (data.success)
                    alert("操作成功");
                else
                    alert(data.info);
            }
        });
    }
    function unbindWechatAppLogin(parentId) {
        if (!confirm("确定解绑App微信登录?")) {
            return;
        }
        $.ajax({
            url: "/crm/parent/unbind_wechat_app_login.vpage",
            type: "POST",
            async: false,
            data: {
                "parentId": parentId
            },
            success: function (data) {
                if (data.success)
                    alert("操作成功");
                else
                    alert(data.info);
            }
        });
    }

    function allowPay(parentId) {
        if (!confirm("确定解除支付限制?")) {
            return;
        }
        $.ajax({
            url: "/crm/parent/allowPay.vpage",
            type: "POST",
            async: false,
            data: {
                "parentId": parentId
            },
            success: function (data) {
                if (data.success)
                    alert("操作成功");
                else
                    alert(data.info);
            }
        });
    }


    $(function () {
        var regex = /[\\{]/;
        $("#rewardType").on("click", function () {
            var content = $("#rewardType option:selected").html();
            if (regex.test(content)) {
                var placeholder = content.substring(content.indexOf("{") + 1, content.indexOf("}"));
                $("#ext").attr("placeholder", placeholder + ":xx");
                $("#extUl").show();
            } else if (content.indexOf("岛") > 0) {
                $("#ext").attr("placeholder", "taskId:xx");
                $("#extUl").show();
            } else {
                $("#extUl").hide();
            }
        });
        $("#generate_reward_btn").on("click", function () {
            $.ajax({
                type: "post",
                url: "/crm/parent/generatereward.vpage",
                data: {
                    studentId: $("#childrenInfo").find("option:selected").val(),
                    itemKey: $("#rewardType").find("option:selected").val(),
                    ext: $("#ext").val()
                },
                success: function (data) {
                    if (data.success) {
                        alert("生成家长奖励成功");
                        $("#generate_reward_dialog").modal("hide");
                    } else {
                        alert(data.info);
                    }
                }
            });
        });

        $("#dialog_add_fault_order").on("click", function () {
            var faultTypes = [];
            $("input[type=checkbox][name=faultType]:checked").each(function (index, domEle) {
                var ckBox = $(domEle);
                faultTypes.push(ckBox.val());
            });

            if (faultTypes.length < 1) {
                alert("请至少选择一个追踪项");
                return;
            }

            var createInfo = $('#fault_order_create_info').val();
            if (!createInfo) {
                alert("请填写追踪备注");
                return;
            }
            if (createInfo.replace(/(^s*)|(s*$)/g, "").length == 0) {
                alert("请填写追踪备注");
                return;
            }

            var queryUrl = "/crm/faultOrder/addRecord.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId: "${parentInfoAdminMapper.parent.id!''}",
                    userName: "${parentInfoAdminMapper.parent.profile.realname!''}",
                    userType: "${parentInfoAdminMapper.parent.userType!''}",
                    createInfo: $('#fault_order_create_info').val(),
                    faultType: faultTypes.join(",")
                },
                success: function (data) {
                    if (data.success) {

                        $("#fault_order_dialog").modal("hide");
                    } else {
                        alert("添加跟踪记录失败。");
                    }
                }
            });
        });

        $("#dialog_edit_parent_password").on("click", function () {
            var queryUrl = "../user/resetpassword.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId: ${parentInfoAdminMapper.parent.id!""},
                    password: $("#password").val(),
                    passwordDesc: $("div[class='btn-group'] button[class='btn active']").html(),
                    passwordExtraDesc: $('#passwordExtraDesc').val()
                },
                success: function (data) {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                }
            });
        });

        $("#dialog_edit_parent_date").on("click", function () {
            var queryUrl = "../user/addcustomerrecord.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId: ${parentInfoAdminMapper.parent.id!''},
                    recordType: $("#recordType").val(),
                    questionDesc: $("#questionDesc").val(),
                    operation: $("#operation").val()
                },
                success: function (data) {
                    if (data.success) {
                        appendNewRecord(data);
                    } else {
                        alert("增加日志失败。");
                    }
                    $("#record_dialog").modal("hide");
                }
            });
        });

        $("#dialog_bind_student_parent").on("click", function () {
            var studentId = $("#studentId").val().trim();
            var parentCallName = $("#parentCallName").find('option:selected').val().trim();
            var bindExtraDesc = $('#bindExtraDesc').val().trim();
            if (studentId == '' || !$.isNumeric(studentId)) {
                alert("学号不能为空,并且必须为数字类型！");
                return false;
            }
            if (parentCallName == '') {
                alert("家长称呼不能为空！");
                return false;
            }
            if (bindExtraDesc == '') {
                alert("附加描述不能为空！");
                return false;
            }
            var queryUrl = "bindstudentparent.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    parentId: ${parentInfoAdminMapper.parent.id!''},
                    studentId: parseInt(studentId),
                    parentCallName: parentCallName,
                    bindDesc: $("div[class='btn-bind-group'] button[class='btn active']").html(),
                    bindExtraDesc: bindExtraDesc
                },
                success: function (data) {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                }
            });
        });

        $('[id^="query_user_password_"]').on('click', function () {
            var item = $(this);
            var id = parseInt(item.attr("id").substr("query_user_password_".length));
            $("#real_code").text("");
            $.get("../user/temppassword.vpage", {
                userId: id
            }, function (data) {
                $("#real_code").text(data.password);
            });
        });

        $("#modifycredit").on("click", function () {
            $("#modifycredit_dialog").modal("show");
        });

        $("#modifycredit_dialog_btn").on("click", function () {
            var userId = $(this).attr("data-userId");
            var amount = $("#modifycreditnum").val();
            var comment = $("#modifycreditdesc").val();

            if (isBlank(comment)) {
                alert("请填写备注");
                return false;
            }

            $.post('/crm/user/modifycredit.vpage', {userId: userId, amount: amount, comment: comment}, function (data) {
                if (data.success) {
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });

        $('#btnBindMobile').on('click', function () {
            var $dialog = $('#bind_mobile_dialog');
            $dialog.modal('show');
        });

        $('#bind_mobile_submit').on('click', function () {
            $.post('bindmobile.vpage',
                    {
                        parentId: ${parentInfoAdminMapper.parent.id!''},
                        mobile: $('#txtMobile').val(),
                        desc: $('#txtBindMobileDesc').val()
                    }, function (data) {
                        if (data.success) {
                            alert("绑定成功");
                            window.location.reload();
                        } else {
                            alert(data.info);
                        }
                    });
        });

        //删除用户
            $("#btn_delete_user").on("click", function () {
                var del_desc = $("#delete_user_reason").val().trim();
                if (del_desc == '') {
                    alert("请输入注销原因！");
                    return false;
                }
                $.ajax({
                    type: "post",
                    url: "/crm/user/delete.vpage",
                    data: {
                        userId : ${(parentInfoAdminMapper.parent.id)!''},
                        desc: del_desc,
                    },
                    success: function (data) {
                        $("#delete_user_dialog").modal("hide");
                        if (data.success) {
                            alert("注销成功！");
                            window.location.reload();
                        } else {
                            alert(data.info);
                        }
                    }
                });
            });

        function isBlank(str) {
            return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
        };
    });
</script>
</@layout_default.page>