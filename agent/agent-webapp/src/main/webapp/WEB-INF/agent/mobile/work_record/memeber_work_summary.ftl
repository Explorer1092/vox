<#import "../layout_new.ftl" as layout>
<@layout.page group="work_record" title="工作记录">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a class="headerBack" href="javascript:window.history.back()">&lt;&nbsp;返回</a>
                <div class="headerText">人员统计</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-date">
    <div class="date"><span><em></em>${startTime?string("yyyy-MM-dd")!''} ~ ${endTime?string("yyyy-MM-dd")!''}</span></div>
</div>

<ul class="mobileCRM-V2-list">
    <li>
        <div class="box link-ico">
            <div class="side-fl side-time">人员</div>
            <div class="side-fr side-time side-width">陪访</div>
            <div class="side-fr side-time side-width">组会</div>
            <div class="side-fr side-time side-width">进校</div>
        </div>
    </li>

    <#if memberSummary?? && memberSummary?size gt 0>
        <#list memberSummary as member>
            <#if member??>
                <li>
                    <a href="memeber_work_detail.vpage?user=${member['userId']!''}&startTime=${startTime?string("yyyy-MM-dd")}&endTime=${endTime?string("yyyy-MM-dd")}" class="link  link-ico">
                        <div class="side-fl">${member['realName']!''}</div>
                        <#if member['workSummary']??>
                            <div class="side-fr side-orange side-width">${member['workSummary']["MEETING"]!'0'}</div>
                            <div class="side-fr side-orange side-width">${member['workSummary']["VISIT"]!'0'}</div>
                            <div class="side-fr side-orange side-width">${member['workSummary']["SCHOOL"]!'0'}</div>
                        <#else>
                            <div class="side-fr side-orange side-width">0</div>
                            <div class="side-fr side-orange side-width">0</div>
                            <div class="side-fr side-orange side-width">0</div>
                        </#if>
                    </a>
                </li>
            </#if>
        </#list>
        <#if totalSummary??>
            <li>
                <a href="javascript:void(0)" class="link">
                    <div class="side-fl">合计</div>
                    <div class="side-fr  side-width">${totalSummary["MEETING"]!'0'}</div>
                    <div class="side-fr  side-width">${totalSummary["VISIT"]!'0'}</div>
                    <div class="side-fr  side-width">${totalSummary["SCHOOL"]!'0'}</div>
                </a>
            </li>
        </#if>
    </#if>
</ul>

<div class="mobileCRM-V2-layer" style="display:none">
    <div class="dateBox">
        <div class="boxInner">
            <ul class="mobileCRM-V2-list">
                <li>
                    <div class="box">
                        <div class="side-fl">起始日期</div>
                        <input type="date" placeholder="2015-10-01" class="textDate" onfocus="(this.type='date')" id="startTime" value="${startTime?string("yyyy-MM-dd")!''}">
                    </div>
                </li>
                <li>
                    <div class="box">
                        <div class="side-fl">结束日期</div>
                        <input type="date" placeholder="2015-10-31" class="textDate" onfocus="(this.type='date')" id="endTime" value="${endTime?string("yyyy-MM-dd")!''}">
                    </div>
                </li>
            </ul>
            <div class="boxFoot">
                <div class="side-fl">取消</div>
                <div class="side-fr">确定</div>
            </div>
        </div>
    </div>
</div>


<script type="text/javascript">

    $(function(){

        $("div.mobileCRM-V2-date").click(function(){
            $("div.mobileCRM-V2-layer").show();
        });
        $("div.mobileCRM-V2-layer .side-fl").click(function(){
            $("div.mobileCRM-V2-layer").hide();
        })
        $("div.mobileCRM-V2-layer .side-fr").click(function(){
            var startTime = $("#startTime").val();
            var endTime = $("#endTime").val();
            if(!startTime|| startTime ===""){
                alert("请输入起始日期");
                return false;
            }
            if(!endTime|| endTime ===""){
                alert("请输入结束日期");
                return false;
            }
            if(new Date(startTime)>new Date(endTime)){
                alert("起始日期大于结束日期，请重新输入");
                return false;
            }
            window.location.href = "memeber_work_summary.vpage?user=${curUserId!''}&startTime="+startTime+"&endTime="+endTime;
        })
    });
</script>
</@layout.page>