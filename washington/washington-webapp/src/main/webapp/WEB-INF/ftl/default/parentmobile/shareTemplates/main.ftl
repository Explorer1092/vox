<#assign
templateInfo = {
    "english" : "english",
    "math" : "math"
}
module =(module!'none')?lower_case
templateFile = templateInfo[module]!"none"
>

<#if templateFile != "none">
    <div class="schoolReport">
        <div class="content">
            <h2 class="nope"></h2>
            <#include "./" + templateFile + ".ftl">
            <div class="schoolReport_foot">
                <dl>
                    <dt></dt>
                    <dd>
                        <p>关心孩子学习，从安装一起作业<span>家长通</span>开始</p>
                    </dd>
                    <dd>
                        <p class="sf">一起作业网</p>
                    </dd>
                </dl>
            </div>
        </div>
    </div>
</#if>

