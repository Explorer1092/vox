<#import "../layout_new.ftl" as layout>
<@layout.page group="work_record" title="工作记录">

<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>

            <div class="headerText" id="pageTitle">工作记录</div>
        </div>
    </div>
</div>

<div class="mobileCRM-V2-rankInfo">
    <div class="infoBox infoTab">
        <div class="active" name="action" method="SCHOOL">
            <div class="boxNum">${workSummary.summary['SCHOOL']!0}</div>
            <div class="boxFoot">进校</div>
        </div>
        <div name="action" method="MEETING">
            <div class="boxNum">${workSummary.summary['MEETING']!0}</div>
            <div class="boxFoot">组会</div>
        </div>
        <div name="action" method="VISIT">
            <div class="boxNum">${workSummary.summary['VISIT']!0}</div>
            <div class="boxFoot">陪访</div>
        </div>
    </div>
</div>
    <#if workSummary?? && workSummary.detail?? >
        <#assign  details = workSummary.detail>
        <#if  details['SCHOOL']?? && details['SCHOOL']?size gt 0>
        <div class="mobileCRM-V2-box mobileCRM-V2-info" id="SCHOOL">
            <ul class="mobileCRM-V2-list">
                <#list details['SCHOOL'] as work>
                    <li>
                        <a href="user_work_record.vpage?workType=SCHOOL&worker=${curUserId!''}&user=${curUserId!''}&code=${work.code!''}&startTime=${startTime?string("yyyy-MM-dd")}&endTime=${endTime?string("yyyy-MM-dd")}"
                           class="link link-ico">
                            <div class="side-fl">${work.name!''}</div>
                            <div class="side-fr side-time">${work.count!'0'}</div>
                        </a>
                    </li>
                </#list>
            </ul>
        </div>
        </#if>
        <#if details['VISIT']?? && details['VISIT']?size gt 0>
        <div class="mobileCRM-V2-box mobileCRM-V2-info" id="VISIT" style="display: none">
            <ul class="mobileCRM-V2-list">
                <#list details['VISIT'] as work>
                    <li>
                        <a href="user_work_record.vpage?workType=VISIT&worker=${curUserId!''}&user=${curUserId!''}&code=${work.code!''}&startTime=${startTime?string("yyyy-MM-dd")}&endTime=${endTime?string("yyyy-MM-dd")}"
                           class="link link-ico">
                            <div class="side-fl">${work.name!''}</div>
                            <div class="side-fr side-time">${(work.count)!'0'}</div>
                        </a>
                    </li>
                </#list>
            </ul>
        </div>
        </#if>
        <#if details['MEETING']?? && details['MEETING']?size gt 0 >
        <div class="mobileCRM-V2-box mobileCRM-V2-info" id="MEETING" style="display: none">
            <ul class="mobileCRM-V2-list">

                <#list details['MEETING'] as work>
                    <li>
                        <a href="user_work_record.vpage?workType=MEETING&worker=${curUserId!''}&user=${curUserId!''}&code=${work.code!''}&startTime=${startTime?string("yyyy-MM-dd")}&endTime=${endTime?string("yyyy-MM-dd")}"
                           class="link link-ico">
                            <div class="side-fl">${work.name!''}</div>
                            <div class="side-fr side-time">${(work.count)!'0'}</div>
                        </a>
                    </li>
                </#list>
            </ul>
        </div>
        </#if>
    </#if>


<script type="text/javascript">
    $("div[name='action']").click(function () {
        $("div[name='action']").removeClass('active');
        $(this).addClass('active');
        var method = $(this).attr("method");
        $("div.mobileCRM-V2-box").hide();
        $("#" + method).show();
    });
</script>
</@layout.page>