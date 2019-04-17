<#--惊喜码兑换-->
<#if (data.showsc)?? && data.showsc>
    <div class="surpriseCode-right-popup">
        <div class="cp-inner">
            <div class="cp-close" id="clickSurpriseRightPopup"><#--close--></div>
            <h3>${.now?string("M")}月台历惊喜码兑换</h3>
            <a href="javascript:void (0);" class="clickSurprisePopup">点击兑换</a>
        </div>
    </div>

    <script type="text/html" id="T:领取惊喜弹窗">
        <#--进行领取-->
        <div class="surprise-code-container">
            <h3>恭喜您获得惊喜码兑换资格！</h3>
            <div class="code-int">
                请输入本月惊喜码：<input type="text" value="" class="w-int" id="surpriseCodeReceiveInt"/>
            </div>
            <div class="code-btn">
                <a href="javascript:void(0);" class="w-btn w-btn-small" id="surpriseCodeReceive">领取奖励</a>
            </div>
            <p class="code-info">注：惊喜码请在一起作业赠送的羊年台历中寻找，台历转赠无效哦！</p>
        </div>
    </script>
    <script type="text/html" id="T:领取惊喜成功弹窗">
        <#--领取成功-->
        <div class="surprise-code-container">
            <h4>成功领取园丁豆！</h4>
            <div class="code-btn">
                <a href="javascript:void(0);" class="w-btn w-btn-small" id="surpriseCodeClose"><#if .now?string("M") == "12">期待明年的惊喜码<#else><#if .now?string("M") == "6">九<#else>下</#if>月再来</#if></a>
            </div>
            <p class="code-info">注：惊喜码请在一起作业赠送的羊年台历中寻找，台历转赠无效哦！</p>
        </div>
    </script>
    <script type="text/html" id="T:领取惊喜Error弹窗">
        <div class="surprise-code-container">
            <h4><%=info%></h4>
            <div class="code-btn">
                <a href="javascript:void(0);" class="w-btn w-btn-small clickSurprisePopup">返回领取</a>
            </div>
            <p class="code-info">注：惊喜码请在一起作业赠送的羊年台历中寻找，台历转赠无效哦！</p>
        </div>
    </script>
    <script type="text/javascript">
        $(function(){
            //点击兑换-按钮
            $(document).on("click", ".clickSurprisePopup", function(){
                $.prompt(template("T:领取惊喜弹窗", {}), {
                    title: "兑换提示",
                    buttons : {},
                    loaded : function(){
                        $(".jqibox").addClass("surprise-popup");
                    }
                });
            });

            //领取奖励-按钮
            $(document).on("click", "#surpriseCodeReceive", function(){
                var surpriseCodeReceiveInt = $("#surpriseCodeReceiveInt");

                if( $17.isBlank(surpriseCodeReceiveInt.val()) ){
                    surpriseCodeReceiveInt.addClass("w-int-error");
                    return false;
                }

                //提交惊喜码
                $.post("/teacher/consumesc.vpage", { code : surpriseCodeReceiveInt.val()}, function(data){
                    if(data.success){
                        $.prompt(template("T:领取惊喜成功弹窗", {}), {
                            title: "兑换提示",
                            buttons : {},
                            loaded : function(){
                                $(".jqibox").addClass("surprise-popup");
                            }
                        });
                    }else{
                        $.prompt(template("T:领取惊喜Error弹窗", {info : data.info}), {
                            title: "系统提示",
                            buttons : {},
                            loaded : function(){
                                $(".jqibox").addClass("surprise-popup");
                            }
                        });
                    }
                });
            });

            //下月再来-按钮
            $(document).on("click", "#surpriseCodeClose", function(){
                $(".surpriseCode-right-popup").remove();
                $.prompt.close();
            });

            $(document).on("click", "#clickSurpriseRightPopup", function(){
                $(".surpriseCode-right-popup").remove();
            });
        });
    </script>
</#if>