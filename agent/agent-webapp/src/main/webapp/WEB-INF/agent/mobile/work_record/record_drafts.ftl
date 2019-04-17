<#import "../layout_new.ftl" as layout>
<@layout.page group="work_record" title="草稿箱">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back();" class="headerBack" id="addSchool_return">&lt;&nbsp;返回</a>
            <div class="headerText margin-l margin-r">
                <span>草稿箱</span>
            </div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul>
        <li>
            <div class="side-fl">搜索结果</div>
        </li>
        <#if schoolClues?has_content>
            <#list schoolClues as clue>
                <li>
                    <div class="side">${clue.schoolName!''}</div>
                    <div class="side">${clue.updateTime!''}</div>
                    <a>继续修改</a>
                    <a>删除草稿</a>
                </li>
            </#list>
        </#if>
    </ul>
</div>
</@layout.page>