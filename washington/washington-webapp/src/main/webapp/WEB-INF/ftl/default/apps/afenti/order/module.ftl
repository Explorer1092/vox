<#macro page step=1 title="阿分题 - 一起作业" paymentType = "" stepOnOff="">
<!doctype html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
-->
<html>
<head>
    <#include "../../../nuwa/meta.ftl" />
    <title>一起作业,一起作业网,师生家长互动平台</title>
    <@sugar.capsule js=["jquery", "toolkit", "core", "alert", "template", "student"] css=["plugin.alert", "new_student.base", "student.afenti", "new_student.widget"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
    <#if title == "picaro">
        <#nested>
    <#else>
    <!--header start-->
    <div class="header">
        <a class="logo" href="/"><span class="iblock"></span></a>
        <#if step != 0>
            <#if stepOnOff != "Confirmed" && stepOnOff != "Canceled">
                <#if paymentType == "agent">
                    <div class="payStep"><span class="payStepAgent s${step}"></span></div>
                <#else>
                    <div class="payStep"><span class="iblock s${step}"></span></div>
                </#if>
            </#if>
        <#else>
            <div class="head_menu">
                <div id="nav_head_menu_box" class="head_ct">
                    <ul>
                        <li><a href="/">首页</a></li>
                        <li><a href="/student/center/order.vpage">我的订单</a></li>
                    </ul>
                </div>
            </div>
        </#if>
    </div>
    <!--//-->
        <#nested>
    <!--footer start-->
    <div class="footer">
        <p>客服电话：<b><@ftlmacro.hotline/></b></p>
        ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
    </div>
    <!--//-->
    </#if>
    <@sugar.site_traffic_analyzer_end />
</body>
</html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
</#macro>

<#macro gPayProduct typeName="">
<a id="gPay" href="javascript:void(0);" class="getOrange gPaygetGreen buyNowSubmit">找人代付</a>
<form action="?" method="post" id="gPayfrm">
    <input type="hidden" name="p" value="0"/>
    <input type="hidden" name="productId" value=""/>
    <input type="hidden" name="payment" value="payment"/>
    <input type="hidden" name="refer" value="${refer!''}"/>
</form>
<script type="text/javascript">
    $(function(){
        $("#gPay").click(function(){
            if($(this).hasClass("getOrange_gray")){
                return false;
            }else{
                $('#gPayfrm').submit();
                $17.tongji("找人代付-点击找人代付按钮-${typeName!}");
                return false;
            }
        });
    });
</script>
</#macro>