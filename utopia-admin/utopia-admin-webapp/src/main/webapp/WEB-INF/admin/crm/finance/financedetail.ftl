<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span12">
    <div>
        <fieldset class="inline">
            <legend>用户<#if user??><a
                    href="../user/userhomepage.vpage?userId=${user.id!}">${user.profile.realname!}</a>(${user.id!}
                )</#if>学贝详情
            </legend>
        </fieldset>
        <br/>
        <ul class="inline">
            <li>
                学贝余额：${balance!}
            </li>
            <li>
                <button class="btn btn-primary" onclick="addBalance()">添 加</button>
                <button class="btn btn-warning" onclick="withdraw()">提现</button>
            </li>
        </ul>
        <table id="students" class="table table-hover table-striped table-bordered">
            <tr>
                <th> 订单号</th>
                <th> 创建时间</th>
                <th> 流水类型</th>
                <th> 交易金额</th>
                <th> 交易方式</th>
                <th> 外部流水号</th>
                <th> 关联订单号</th>
                <#--<th> 来源场景</th>-->
                <th> 购买商品</th>
                <th> 备注</th>
            </tr>
            <#if flows?has_content>
                <#list flows as flow>
                    <tr id="finance_history_${flow.financeFlow.id!""}">
                        <td>${flow.financeFlow.id!""}</td>
                        <td>${flow.financeFlow.createDatetime?string('yyyy-MM-dd HH:mm:ss')}</td>
                        <td>
                            <#switch flow.financeFlow.type!"">
                                <#case "Deposit">
                                    充值
                                    <#break>
                                <#case "Debit">
                                    消费
                                    <#break>
                                <#case "Refund">
                                    退款
                                    <#break >
                                <#case "Withdraw">
                                    提现
                                    <#break >
                            </#switch>
                        </td>
                        <td>${flow.financeFlow.amount!""}</td>
                        <td>
                            <#switch flow.financeFlow.source!"">
                                <#case "alipay_parentapp">
                                    支付宝充值
                                    <#break >
                                <#case "voxpay">
                                    余额支付
                                    <#break >
                                <#case "wechatpay_parent">
                                    微信充值
                                    <#break >
                                <#case "debugpay">
                                    测试支付
                                    <#break >
                                <#case "admin">
                                    系统操作
                                    <#break >
                            </#switch>
                        </td>
                        <td>${flow.financeFlow.outerId!""}</td>
                        <td>${flow.financeFlow.orderId!""}</td>
                        <#--<td>${flow.financeFlow.refer!""}</td>-->
                        <td><#if flow.afentiOrder??>${flow.afentiOrder.productName!""}</#if></td>
                        <td>${flow.financeFlow.memo!''}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
    <div id="finance_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>增加用户学贝</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户 ID</dt>
                        <dd>${user.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>学贝金额</dt>
                        <dd><input type="text" name="balance" id="balance" placeholder="只能是数字"/></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary"
                    onclick="$('#finance_dialog').modal('hide');change();">确 定
            </button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
    <div id="finance_withdraw_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>提现</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户ID</dt>
                        <dd>${user.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>提现金额</dt>
                        <dd><input type="text" name="amount" id="txt_withdraw_amount" placeholder="只能是数字"/></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary" onclick="submit_withdraw()" data-dismiss="modal" aria-hidden="true">确定
            </button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取消</button>
        </div>
    </div>
</div>
<script type="text/javascript">
    function addBalance() {
        $("#balance").val('');
        $("#finance_dialog").modal("show");
    }

    function change() {
        var queryUrl = "addbalance.vpage";
        $.ajax({
            type: "post",
            url: queryUrl,
            data: {
                balance: $("#balance").val(),
                userId: ${user.id!''}
            },
            success: function (data) {
                if (data.success) {
                    location.href = '?userId=${user.id}';
                } else {
                    alert(data.info);
                }
                $("#dialog-confirm").modal('hide');
            }
        });
    }

    function withdraw() {
        var modal = $('#finance_withdraw_dialog');

        modal.modal('show');
    }

    function submit_withdraw() {
        var amount = $('#txt_withdraw_amount').val();
        if (isNaN(amount)) {
            alert('请输入数字');
            return;
        }
        if (amount <= 0) {
            alert('请输入有效数字');
            return;
        }

        if (${balance} <= 0) {
            alert('余额为0,不能提现');
            return;
        }

        if (amount > ${balance}) {
            alert('请输入有效数字');
            return;
        }

        var userId = ${user.id};
        $.post('withdraw.vpage', {userId: userId, amount: amount}, function (data) {
            if (data.success) {
                alert('提现任务已发出,请等待财务人员操作.');
            } else {
                alert(data.info);
            }
            window.location.reload();
        });
    }
</script>
</@layout_default.page>