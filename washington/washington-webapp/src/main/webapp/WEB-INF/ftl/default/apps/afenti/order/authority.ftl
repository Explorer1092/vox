<#import "module.ftl" as com>
<@com.page step=2 title="${(afentiOrder.productName)!''}确认订单 - 一起作业" paymentType="${payment}">
<style>
    .paymentMode-box{padding:73px 0}
    .paymentMode-box .pam-head{padding:35px 0;text-align:center;font-size:18px;color:#4a6080}
    .paymentMode-box .pam-list{overflow:hidden}
    .paymentMode-box .pam-list li{padding:10px 0 10px 60px;float:left;font-size:14px;color:#666;cursor:pointer; width: 120px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;}
    .paymentMode-box .pam-list li .choice-icon{margin-right:17px;display:inline-block;vertical-align:middle;width:16px;height:15px;border:3px solid #689cdc;border-radius:100%}
    .paymentMode-box .pam-list li.active .choice-icon{background-color:#b3cded}
    .paymentMode-box .pam-btn{padding:30px 0;text-align:center}
    .paymentMode-box .pam-btn a{margin:0 8px}
</style>
<div class="main">
    <div class="payMainBox">
        <div class="tabbox">
            <#--//start-->
            <div class="paymentMode-box" style="background-color: #fff;">
                <div id="s1">
                    <div class="pam-head">订单信息将发送给家长，由家长支付，确认发送吗？</div>
                    <div class="pam-list">
                        <ul>
                            <#if parentList?has_content>
                            <#list parentList as item>
                                <li class="JS-selectParent"data-parentid="${item.parentId}">
                                    <i class="choice-icon"></i> 我的${ ((item.callName)?has_content)?string(item.callName, item.parentId)}
                                </li>
                            </#list>
                            </#if>
                        </ul>
                    </div>
                    <div class="pam-btn">
                        <a class="w-btn-dic w-btn-gray-normal JS-cancelOrder" href="javascript:void(0);">取消并返回</a>
                        <a class="w-btn-dic w-btn-green-normal JS-submitOrder" href="javascript:void(0);">发送订单</a>
                    </div>
                </div>

                <div id="s2" style="display: none;">
                    <div class="pam-head">订单已发送到家长手机，你也可以提醒他们去支付哦。</div>
                    <div style="text-align: center;">
                        <img src="<@app.link href="public/skin/studentv3/images/small-info.png"/>" />
                    </div>
                    <div class="pam-btn">
                        <a class="w-btn-dic w-btn-green-normal" href="/student/fairyland/index.vpage">返回课外乐园</a>
                    </div>
                </div>
            </div>

            <#--end//-->
        </div>
    </div>
</div>
<script type="text/javascript">
    (function(){
        var currentParentId = [];
        var orderId = "${(afentiOrder.genUserOrderId())!0}";

        //选择家长
        $(document).on('click', '.JS-selectParent', function(){
            var $this = $(this);
            var $parentId = $this.attr('data-parentid');

            if($this.hasClass('active')){
                $this.removeClass('active');
                currentParentId.splice($.inArray($parentId, currentParentId), 1);
            }else{
                $this.addClass('active');
                currentParentId.push($parentId);
            }
        });

        //取消并返回
        $(document).on('click', '.JS-cancelOrder', function(){
            $.post('/api/1.0/afenti/order/cancel.vpage', {
                orderId: orderId
            }, function(data){
                if(data.success){
                    //window.history.back();
                    location.href = '/student/fairyland/index.vpage';
                }else{
                    //fail;
                    $17.alert(data.info);
                }
            });

            $17.voxLog({
                module: 'm_beQYGUC1',
                op: 'o_xnewJSw1'
            }, 'student');
        });

        //发送订单
        $(document).on('click', '.JS-submitOrder', function(){
            var $this = $(this);

            if($this.hasClass('dis')){
                return;
            }

            if(currentParentId.length < 1){
                $17.alert('请选择需要发送的家长！');
                return false;
            }

            $this.addClass('dis');

            $.post('/api/1.0/afenti/order/remindparent.vpage', {
                orderId: orderId,
                parentId: currentParentId.join(',')
            }, function(data){
                if(data.success){
                    $('#s1').hide();
                    $('#s2').show();
                    $this.removeClass('dis');
                }else{
                    //fail;
                    $17.alert(data.info);
                }
            });

            $17.voxLog({
                module: 'm_beQYGUC1',
                s1: orderId,
                op: 'o_jlil9QrS',
                s0: currentParentId.join(',')
            }, 'student');
        });

        $17.voxLog({
            module: 'm_beQYGUC1',
            s1: orderId,
            op: 'o_O8XGVzWS'
        }, 'student');
    })();
</script>
</@com.page>