<#import "../../layout_default.ftl" as layout_default>
<#import "../../mizar/pager.ftl" as pager />

<@layout_default.page page_title="退款处理中心" page_num=3>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>

<div id="main_container" class="span9">
    <legend>
        <strong>退款处理中心</strong>
    </legend>
    <form id="activity-query" class="form-horizontal" method="get" action="${requestContext.webAppContextPath}/legacy/order/main.vpage" >
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
    </form>
    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="width: 1500px;">
                <form id="query_frm" class="form-horizontal" method="get" action="main.vpage">
                    <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>
                    <ul class="inline">
                        <li>
                            订单号：<input type="text" id="orderId" name="orderId" value="<#if orderId??>${orderId}</#if>" placeholder="输入订单号">
                            流水号：<input type="text" id="outTradeId" name="outTradeId" value="<#if outTradeId??>${outTradeId}</#if>" placeholder="输入流水号">
                            支付方式：<select id="payMethod" name="payMethod">
                                        <option value="alipay" <#if payMethod?? && "alipay" == payMethod>selected</#if>>支付宝PC</option>
                                        <option value="alipay_parentapp" <#if payMethod?? && "alipay_parentapp" == payMethod>selected</#if>>支付宝-家长APP</option>
                                        <option value="alipay_studentapp" <#if payMethod?? && "alipay_studentapp" == payMethod>selected</#if>>支付宝-学生APP</option>
                                        <option value="wechatpay" <#if payMethod?? && "wechatpay" == payMethod>selected</#if>>微信PC</option>
                                        <option value="wechatpay_parent" <#if payMethod?? && "wechatpay_parent" == payMethod>selected</#if>>微信-家长APP</option>
                                        <option value="wechatpay_studentapp" <#if payMethod?? && "wechatpay_studentapp" == payMethod>selected</#if>>微信-学生APP</option>
                                        <option value="wechatpay_pcnative" <#if payMethod?? && "wechatpay_pcnative" == payMethod>selected</#if>>微信扫码</option>
                                        <option value="qpay_studentapp" <#if payMethod?? && "qpay_studentapp" == payMethod>selected</#if>>QQ钱包-学生APP</option>
                                        <option value="wechatpay_chips" <#if payMethod?? && "wechatpay_chips" == payMethod>selected</#if>>微信-薯条英语</option>
                                        <option value="wechatpay_piclisten" <#if payMethod?? && "wechatpay_piclisten" == payMethod>selected</#if>>微信-随声听</option>
                                        <option value="wechat_studytogether" <#if payMethod?? && "wechat_studytogether" == payMethod>selected</#if>>微信-studytogether</option>
                                        <option value="alipay_wap_parentapp" <#if payMethod?? && "alipay_wap_parentapp" == payMethod>selected</#if>>支付宝-家长APP-H5支付</option>
                                        <option value="alipay_wap_studentapp" <#if payMethod?? && "alipay_wap_studentapp" == payMethod>selected</#if>>支付宝-学生APP-H5支付</option>
                                        <option value="wechatpay_h5_parentapp" <#if payMethod?? && "wechatpay_h5_parentapp" == payMethod>selected</#if>>微信-家长APP-H5支付</option>
                                        <option value="wechatpay_h5_studentapp" <#if payMethod?? && "wechatpay_h5_studentapp" == payMethod>selected</#if>>微信-学生APP-H5支付</option>
                                        <option value="wechatpay_studentapp_junior" <#if payMethod?? && "wechatpay_studentapp_junior" == payMethod>selected</#if>>微信-中学学生APP</option>
                                    </select>

                        </li>
                        <li>
                            状态：   <select id="status" name="status">
                                        <#list refundStatus as c>
                                            <option value="${c.name()!}" <#if status?? && c.name() == status>selected</#if> >${c.name()!}</option>
                                        </#list>
                                    </select>
                        </li>
                        <li>
                            用户：<input type="text" id="userId" name="userId" value="<#if userId??>${userId}</#if>" placeholder="输入用户ID">
                        </li>
                        <li>
                            <button type="submit" id="filter" class="btn btn-primary">
                                <i class="icon-search icon-white"></i> 查  询
                            </button>
                            <button type="button" id="refundBtn" class="btn btn-danger">
                                <i class="icon-search icon-white"></i> 批量退款
                            </button>
                        </li>
                    </ul>
                </form>
                <@pager.pager/>
                <table class="table table-striped table-bordered" style="width: auto;">
                    <thead>
                    <tr>
                        <th><input type="checkbox" class="choiceAll"></input></th>
                        <th width="230">流水号</th>
                        <th>订单号</th>
                        <th>OA单号</th>
                        <th>用户ID</th>
                        <th width="50">金额</th>
                        <th>状态</th>
                        <th>CODE</th>
                        <th>备注</th>
                        <th>支付方式</th>
                        <th>申请时间</th>
                        <th>更新时间</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if refundMapperList?? && refundMapperList?? >
                            <#list refundMapperList as p >
                            <tr>
                                <td><input type="checkbox" class="choiceSingle"></input></td>
                                <td><pre class="refundId">${p.id!}</pre></td>
                                <td><pre>${p.orderId!}</pre></td>
                                <td><pre>${p.oaOrderId!} </pre></td>
                                <td><pre>${p.userId!}</pre></td>
                                <td><pre>${p.refundFee!}</pre></td>
                                <td><pre>${p.status!}</pre></td>
                                <td><pre>${p.code!'-'}</pre></td>
                                <td><pre>${p.comment!'-'}</pre></td>
                                <td><pre>${p.payMethod!}</pre></td>
                                <td><pre>${p.createDatetime!}</pre></td>
                                <td><pre>${p.updateDatetime!}</pre></td>
                                <td>
                                    <#if p.status == 'FAIL' || p.status == 'REFUNDING'>
                                        <#if p.buttonFlag == 'gray'>
                                            <a type="button" class="btn" href="javascript:void(0)">
                                                <i class="icon-edit icon-white"></i>已手动退款
                                            </a>
                                        <#else>
                                            <a type="button" class="btn btn-info failBtn" href="javascript:void(0)">
                                                <i class="icon-edit icon-white"></i>已手动退款
                                            </a>
                                        </#if>
                                    </#if>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
                <@pager.pager/>
            </div>
        </div>
    </div>

    <#--备注弹窗-->
    <div id="alertDialog" class="modal fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3 id="alertDialogTitle"></h3>
        </div>
        <div class="modal-body">
            <span style="margin-left: 100px;">备注:</span>
            <textarea name="" id="commit" style="width: 250px; height: 120px; resize: vertical;" ></textarea>
            <p style="color: #f00; display: none; text-align: center" id="alertError"></p>
        </div>
        <div id="oaOrderDesc" style="display: none;" class="modal-body">
            <span style="margin-left: 150px;">OA转账的订单需要填写OA单号</span>
        </div>
        <div id="oaOrderText" style="display: none;" class="modal-body">
            <span style="margin-left: 75px;">OA单号:</span>
            <input name="oaOrderId" id="oaOrderId" style="width: 250px; height: 30px; resize: vertical;" />
        </div>
        <div class="modal-footer">
            <button class="btn btn-default" data-dismiss="modal" aria-hidden="true">取 消</button>
            <button class="btn btn-primary" id="submitBtn">确 定</button>
        </div>
    </div>

    <#--报错弹窗-->
    <div id="errorAlertDialog" class="modal fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3 id="errorTitle"></h3>
        </div>
        <div class="modal-body">
            <p id="errorInfo"></p>
        </div>
        <div class="modal-footer">
            <#--<button class="btn btn-default" data-dismiss="modal" aria-hidden="true">取 消</button>-->
            <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">确 定</button>
        </div>
    </div>

    <#--请求成功后调整-->
    <div id="FormAlipayPage"></div>
</div>

<script>
    // 点击全选复选框
    $('.choiceAll').on('click', function () {
        if ($(this).is(":checked")) {
            toChoiceAll();
        } else {
            toCancelChoiceAll();
        }
    });
    // 全选
    var toChoiceAll = function () {
        $(".choiceSingle").each(function(){
            $(this).prop("checked", true);
        });
    };
    // 取消全选
    var toCancelChoiceAll = function () {
        $(".choiceSingle").each(function(){
            $(this).prop("checked", false);
        });
    };
    // 点击批量退款按钮
    var checkedList = [];
    $('#refundBtn').on('click', function (event) {
        $("#oaOrderDesc").hide();
        $("#oaOrderText").hide();
        var checkNode = $('.choiceSingle');
        var idNode = $('.refundId');
        for (var i = 0, len = idNode.length; i < len; i++) {
            if (checkNode.eq(i).is(":checked")) {
                checkedList.push(idNode.eq(i).text());
            }
        }
        if (checkedList.length === 0) {
            showErrorAlert('', '您还未选择订单');
            return false;
        }
        showRemarkAlert('请填写退款备注信息', 'autoRefund', checkedList.join(','));
    });
    // 点击手动退款按钮
    $('.failBtn').on('click', function () {
        var trIndex = $(this).parents('tr').index();
        $("#oaOrderDesc").show()
        $("#oaOrderText").show();
        showRemarkAlert('请填写手动退款备注信息', 'handRefund', $('.refundId').eq(trIndex).text());
    });
    // 点击弹窗确定按钮
    $('#submitBtn').on('click', function () {
        var commitNode = $('#commit');
        if (!commitNode.val()) {
            $('#alertError').show().text('您还未填写备注信息');
            return false;
        }
        var alertType = $('#alertDialogTitle').attr('data-type');
        if (alertType === 'autoRefund') {
            var data = {
                ids: $('#alertDialogTitle').attr('data-id'),
                comment: commitNode.val()
            };
            $.post('/legacy/order/batchrefund.vpage', data, function (data) {
                if (data.success) {
                    // 隐藏弹窗
                    hideRemarkAlert();
                    // 接收form并submit
                    if(data.alipayForm){
                        $("#FormAlipayPage").html(data.alipayForm);
                        setTimeout(function(){
                            $("#refundForm").submit();
                        }, 50);
                        setTimeout(function(){
                            window.location.reload();
                        }, 100);
                    } else {
                        hideRemarkAlert();
                        showErrorAlert('', data.info, function(){
                            window.location.reload();
                        });
                    }
                } else {
                    hideRemarkAlert();
                    showErrorAlert('', data.info);
                }
            });
        } else if (alertType === 'handRefund') {
            $("#oaOrderDesc").show();
            $("#oaOrderText").show();
            var data = {
                id: $('#alertDialogTitle').attr('data-id'),
                comment: commitNode.val(),
                oaOrderId: $('#oaOrderId').val()
            };
            $.post('/legacy/order/manualrefund.vpage', data, function (data) {
                if (data.success) {
                    showErrorAlert('', data.info, function(){
                        window.location.reload();
                    });
                } else {
                    hideRemarkAlert();
                    showErrorAlert('', data.info);
                }
            });
        }
    });
    // 激活备注输入框时
    $('#commit').on('focus', function () {
        $('#alertError').hide();
    });
    // 显示备注弹窗
    var showRemarkAlert = function (title, type, id) {
        $('#commit').val('');
        $('#oaOrderId').val('');
        $('#alertError').text('');
        $('#alertDialog').modal('show');
        $('#alertDialogTitle').text(title).attr({'data-type': type, 'data-id': id});
    };
    // 隐藏备注弹窗
    var hideRemarkAlert = function () {
        $('#alertDialog').modal('hide');
    };
    // 显示错误弹窗
    var showErrorAlert = function (title, errInfo, callback) {
        $('#errorAlertDialog').modal('show');
        $('#errorTitle').text(title || '系统提示');
        $('#errorInfo').text(errInfo || '您还进行任何操作');
        if (callback) { // 存在callback时监听弹窗关闭
            $('#errorAlertDialog').on('hide.bs.modal', callback);
        }
    };
</script>

</@layout_default.page>
