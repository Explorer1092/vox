<#if tipType?exists>
    <#switch tipType>
        <#case "qinqin">
            <div class="parentApp-emptyProm parentApp-emptyProm-5">
                <div class="promIco"></div>
                <div class="promTxt">${tipText!""}</div>
            </div>
            <#break>
        <#case "card">
            <div class="parentApp-emptyProm parentApp-emptyProm-1">
                <div class="promIco"></div>
                <div class="promTxt">${tipText!""}</div>
            </div>
            <#break>
        <#case "medal">
            <div class="parentApp-emptyProm parentApp-emptyProm-2">
                <div class="promIco"></div>
                <div class="promTxt">${tipText!""}</div>
            </div>
            <#break>
        <#case "nomessage">
            <div class="parentApp-messageNull">
                ${tipText!"暂时没内容，何不休息休息"}
            </div>
            <#break>
        <#case "not released">
            <div class="parentApp-messageNull parentApp-noreleased">
                ${tipText!"敬请期待"}
            </div>
            <#break>
        <#default>
            <#break>
    </#switch>
</#if>
