<#import "../servicemodule.ftl" as temp>
<#macro page title="渠道合作">
    <@temp.page title="">
    <@sugar.capsule css=["plugin.cooperation"] />
    <div class="section">
        <ul class="section_tab clear">
            <li>
                <a <#if title=="公司优势">class="active"</#if> href="/help/cooperation/advantage.vpage">公司优势</a>
            </li>
            <li>
                <a <#if title=="产品创新">class="active"</#if> href="/help/cooperation/innovation.vpage">产品创新</a>
            </li>
            <li>
                <a <#if title=="媒体报道">class="active"</#if> href="/help/cooperation/media.vpage">媒体报道</a>
            </li>
            <li>
                <a <#if title=="专家点评">class="active"</#if> href="/help/cooperation/doctor.vpage">专家点评</a>
            </li>
            <li>
                <a <#if title=="用户声音">class="active"</#if> href="/help/cooperation/user.vpage">用户声音</a>
            </li>
            <li>
                <a <#if title=="合作流程">class="active"</#if> href="/help/cooperation/copotation.vpage">合作流程</a>
            </li>
            <li >
                <a <#if title=="联系我们">class="active"</#if> href="/help/cooperation/connact.vpage">联系我们</a>
            </li>
        </ul>
        <div class="article" >
            <#nested>
        </div>
    </div>
    </@temp.page>
</#macro>

