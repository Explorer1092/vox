<#--offline-->
<#if currentUser.fetchCertificationState() == "SUCCESS" && (!wxbinded)!false>
<div id="teacher_wechat_homework" class="t-weChatBackgroundArea" data-bind_wechat="${((!wxbinded)!false)?string}">
        <#--false未绑定,true是已绑定-->
        <a href="/teacher/reward/rewardreceivebonus.vpage">
            <div class="wb-content">
                <div class="wb-code" id="weChatBackgroundAreaCode"></div>
            </div>
        </a>
        <script type="text/javascript">
            var weiXinCode = "//cdn.17zuoye.com/static/project/app/publiccode_teacher.jpg";
            $(function(){
                <#if ((wxbinded)!false)>
                    $("#weChatBackgroundAreaCode").html("<img src='"+ weiXinCode +"' width='140'/>");
                <#else>
                    $.get("/teacher/qrcode.vpage", function(data){
                        if(data.success){
                            //fix qrcode in IE6 problem 统一处理
                            if ($.browser.msie && parseInt($.browser.version, 10) == 6) {
                                weiXinCode = (data.qrcode_url).replace('https://', 'http://');
                            }else{
                                weiXinCode = data.qrcode_url;
                            }
                        }
                        $("#weChatBackgroundAreaCode").html("<img src='"+ weiXinCode +"' width='140'/>");
                    });
                </#if>
            });
        </script>
</div>
</#if>