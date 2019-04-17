<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>正在进入在线支付系统 - 一起作业</title>
</head>

<body onload="<#if ProductDevelopment.isUsingProductionData()>document.getElementById('payment-form').submit();</#if>">

正在进入在线支付系统 ...

${paymentRequestForm.generateHtml('payment-form')}

<#if ProductDevelopment.isUsingTestData()>
仅供内部测试：
<button onclick="document.getElementById('payment-form').submit();">Submit</button>
<a href="../recharge/debug-notify.vpage?tradeNumber=${paymentRequest.tradeNumber}&amp;externalTradeNumber=${paymentRequest.tradeNumber}&amp;payAmount=${paymentRequest.payAmount}">Debug
    Payment Success</a>
<a href="../recharge/debug-notify.vpage?tradeNumber=${paymentRequest.tradeNumber}&amp;externalTradeNumberBad=${paymentRequest.tradeNumber}&amp;payAmount=${paymentRequest.payAmount}">Debug
    Payment Fail</a>
</#if>

</body>
