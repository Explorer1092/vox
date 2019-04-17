<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="发送订单给家长支付"
pageJs=['jquery', 'weui', 'voxLogs']
pageCssFile={"css" : ["public/skin/paymentmobile/authority/css/pay"]}
>
<div id="s1">
    <div class="p-intro">订单信息将发送给家长，由家长支付确认发送订单给以下家长吗？</div>
    <div class="p-roleList">
        <#if parentList?has_content>
        <#list parentList as item>
            <div class="role JS-selectParent" data-parentid="${item.parentId}" style="overflow: hidden; white-space: nowrap; text-overflow:ellipsis;">我的${ ((item.callName)?has_content)?string(item.callName, item.parentId)}</div>
        </#list>
        </#if>
    </div>
    <div class="w-footer">
        <div class="inner">
            <div class="w-btnBox">
                <a href="javascript:;" class="btn btn-orange JS-cancelOrder">取消并返回</a>
                <a href="javascript:;" class="btn btn-blue tail JS-submitOrder">发送订单</a><!--置灰disabled-->
            </div>
        </div>
    </div>
</div>

<div id="s2" style="display: none;">
    <div class="p-tipsBox">
        <i class="cartoon" style="height: 5.2rem; background-size: auto 100%; background-position: center center"></i>
        <p>订单已发送到家长手机，<br>你也可以提醒他们去支付哦</p>
    </div>
    <div class="w-footer">
        <div class="inner fixed bg-footer">
            <div class="w-btnBox">
                <a href="javascript:;" class="btn btn-blue tail JS-returnOrder">返回应用</a>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    signRunScript = function () {
        var currentParentId = [];
        var orderId = "${(orderId)!0}";
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

        $(document).on('click', '.JS-returnOrder', function(){
            if (window.history.length == 1) {
                if(isWinExternal()['disMissView']){
                    isWinExternal().disMissView();
                }
            } else {
                window.history.back();
            }
        });

        //取消并返回
        $(document).on('click', '.JS-cancelOrder', function(){
            $.post('/api/1.0/afenti/order/cancel.vpage', {
                orderId: orderId
            }, function(data){
                if(data.success){
                    if (window.history.length == 1) {
                        if(isWinExternal()['disMissView']){
                            isWinExternal().disMissView();
                        }
                    } else {
                        window.history.back();
                    }
                }else{
                    //fail;
                    $.alert(data.info);
                }
            });

            YQ.voxLogs({
                module: 'm_HdKWCdiV',
                op: 'o_cwlRTZYC'
            });
        });

        //发送订单
        $(document).on('click', '.JS-submitOrder', function(){
            var $this = $(this);

            if($this.hasClass('dis')){
                return;
            }

            if(currentParentId.length < 1){
                $.alert('请选择需要发送的家长！');
                return;
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
                    $.alert(data.info);
                }
            });

            YQ.voxLogs({
                module: 'm_HdKWCdiV',
                s1: orderId,
                op: 'o_y5kuZtrG',
                s0: currentParentId.join(',')
            });
        });

        YQ.voxLogs({
            module: 'm_HdKWCdiV',
            s1: orderId,
            op: 'o_l34CVMc2'
        });

        //是否有X5的存在
        function isWinExternal() {
            var _win = window;
            if (_win['yqexternal']) {
                return _win.yqexternal;
            } else if (_win['external']) {
                return _win.external;
            }else{
                _win.external = {};
                return _win.external
            }
        }
    }
</script>
</@layout.page>