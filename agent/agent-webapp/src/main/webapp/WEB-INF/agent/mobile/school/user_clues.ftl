<#import "../layout_new.ftl" as layout>
<#assign groupName="work_record">
<@layout.page group=groupName title="学校信息">
<div id="contaier">
    <div class="mobileCRM-V2-header">
        <div class="inner">
            <div class="box">
                <div class="headerBack"><a href="javascript:window.history.back();">&lt;&nbsp;返回</a></div>
                <div class="headerText">学校信息</div>
            </div>
        </div>
    </div>
    <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-message">
        <div class="list">
            <div class="activtaor active" active-value="待审核">
                <div class="hd red">${(schoolClues["待审核"])?size}</div>
                <div class="ft">待审核</div>
            </div>
            <div class="activtaor" active-value="已通过">
                <div class="hd red">${(schoolClues["已通过"])?size}</div>
                <div class="ft">已通过</div>
            </div>
            <div class="activtaor" active-value="已驳回">
                <div class="hd red">${(schoolClues["已驳回"])?size}</div>
                <div class="ft">已驳回</div>
            </div>
        </div>
    </div>
    <ul class="mobileCRM-V2-list mobileCRM-V2-dropDown activity" active-value="待审核">
        <#if (schoolClues["待审核"])?has_content>
            <#list schoolClues["待审核"] as schoolClue>
                <li>
                    <a href="javascript:viewer.view('${schoolClue.id}');" class="link link-ico">
                        <div>${schoolClue.shortName!""}</div>
                    </a>

                    <div id="view-${schoolClue.id!''}" class="dropDown" style="display:none;">
                        <div> <a href="/mobile/school_clue/new_school_detail.vpage?schoolId=${(schoolClue.schoolId)!""}" > ${schoolClue.schoolName!""}</a></div>
                        <p>  ${(schoolClue.showPhase.description)!""} ${(schoolClue.showType.description)!""} ${(schoolClue.schoolId)!""}</p>
                        <p>${schoolClue.provinceName!""} ${schoolClue.cityName!""} ${schoolClue.countyName!""} ${schoolClue.address!""}</p>
                    </div>
                </li>
            </#list>
        </#if>
    </ul>
    <ul class="mobileCRM-V2-list mobileCRM-V2-dropDown activity" style="display: none" active-value="已通过">
        <#if (schoolClues["已通过"])?has_content>
            <#list schoolClues["已通过"] as schoolClue>
                <li>
                    <a href="javascript:viewer.view('${schoolClue.id}');" class="link link-ico">
                        <div>${schoolClue.shortName!""}</div>
                        <div class="personalInfo">审核人员：${schoolClue.reviewerName!""}</div>
                    </a>

                    <div id="view-${schoolClue.id!''}" class="dropDown" style="display:none;">
                        <div><a href="/mobile/school_clue/new_school_detail.vpage?schoolId=${(schoolClue.schoolId)!""}" > ${schoolClue.schoolName!""}</a></div>
                        <p>  ${(schoolClue.showPhase.description)!""} ${(schoolClue.showType.description)!""}  ${(schoolClue.schoolId)!""}</p>
                        <p>${schoolClue.provinceName!""} ${schoolClue.cityName!""} ${schoolClue.countyName!""} ${schoolClue.address!""}</p>
                    </div>
                </li>
            </#list>
        </#if>
    </ul>
    <ul class="mobileCRM-V2-list mobileCRM-V2-dropDown activity" style="display: none" active-value="已驳回">
        <#if (schoolClues["已驳回"])?has_content>
            <#list schoolClues["已驳回"] as schoolClue>
                <li>
                    <a href="javascript:viewer.view('${schoolClue.id}');" class="link link-ico">
                        <div>${schoolClue.shortName!""}</div>
                        <div class="personalInfo">审核人员：${schoolClue.reviewerName!""}</div>
                        <div class="personalInfo">驳回原因：${schoolClue.reviewNote!""}</div>
                    </a>

                    <div id="view-${schoolClue.id!''}" class="dropDown spacing" style="display:none;">
                        <div><a href="/mobile/school_clue/new_school_detail.vpage?schoolId=${(schoolClue.schoolId)!""}" >${schoolClue.schoolName!""}</a></div>
                        <p>  ${(schoolClue.showPhase.description)!""} ${(schoolClue.showType.description)!""}  ${(schoolClue.schoolId)!""}</p>
                        <p>${schoolClue.provinceName!""} ${schoolClue.cityName!""} ${schoolClue.countyName!""} ${schoolClue.address!""}</p>
                    </div>
                </li>
            </#list>
        </#if>
    </ul>
</div>
<script type="text/javascript">
    $(function () {
        activtaor.bind();
    });
</script>
</@layout.page>