<#assign extraJs = [] doPayClassName  ="doPay" >

<#-- TODO master上 暂时关闭 -->
<#if false && ProductDevelopment.isTestEnv()>
    <#assign extraJs = ["public/script/parentMobile/ajax", "public/script/parentMobile/jqPopup","/public/script/parentMobile/testPay"] doPayClassName = "doTestPay">
</#if>
