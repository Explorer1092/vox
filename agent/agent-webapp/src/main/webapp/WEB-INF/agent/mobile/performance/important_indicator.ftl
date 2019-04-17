<#import "../layout_new.ftl" as layout>
<@layout.page group="业绩" title="重要指标">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="/mobile/performance/index.vpage" class="headerBack">&lt;&nbsp;首页</a>
            <div class="headerText">重要指标</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-date">
    <div class="date" id="select-date"><span><em></em>${startDate!''} ~ ${endDate!''}</span></div>
</div>

<div class="mobileCRM-V2-list">
    <ul class="mobileCRM-V2-list">
        <li>
            <a href="/mobile/performance/user_region_student_performance.vpage?startDate=${startDate!}&endDate=${endDate!}" class="link link-ico">
                <div class="side-fl">注册学生</div>
                <div class="side-fr side-orange side-width">${addStuRegNum!'0'}</div>
            </a>
        </li>
        <li id="addStuAuthGradeMathNum" class="stu-sl">
            <a href="/mobile/performance/user_region_student_grade_math.vpage?startDate=${startDate!}&endDate=${endDate!}" class="link link-ico">
                <div class="side-fl">高渗地区1~2年级数学新增认证</div>
                <div class="side-fr side-orange side-width">${addStuAuthGradeMathNum!'0'}</div>
            </a>
        </li>
    </ul>
</div>

<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="mobileCRM-V2-list">
        <li>
            <a href="/mobile/performance/user_region_teacher_only_region.vpage?startDate=${startDate!}&endDate=${endDate!}"  class="link link-ico">
                <div class="side-fl">注册老师</div>
                <div class="side-fr side-orange side-width">${addTeaRegNum!'0'}</div>
            </a>
        </li>
        <li>
            <a href="/mobile/performance/user_region_teacher_only_auth.vpage?startDate=${startDate!}&endDate=${endDate!}" class="link link-ico">
                <div class="side-fl">新增认证老师</div>
                <div class="side-fr side-orange side-width">${addTeaAuthNum!'0'}</div>
            </a>
        </li>
    </ul>
</div>

<div class="mobileCRM-V2-layer" style="display:none">
    <div class="dateBox">
        <div class="boxInner">
            <ul class="mobileCRM-V2-list">
                <li>
                    <div class="box">
                        <div class="side-fl">起始日期</div>
                        <input type="date" id="start" value="${startDate!''}" placeholder="${startDate!''}" class="textDate">
                    </div>
                </li>
                <li>
                    <div class="box">
                        <div class="side-fl">结束日期</div>
                        <input type="date" id="end" value="${endDate!''}" placeholder="${endDate!''}" class="textDate">
                    </div>
                </li>
            </ul>
            <div class="boxFoot">
                <div class="side-fl" id="select-cancel">取消</div>
                <div class="side-fr" id="select-ok">确定</div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $("#select-date").click(function () {
            $(".mobileCRM-V2-layer").show();
        });
        $("#select-cancel").click(function () {
            $(".mobileCRM-V2-layer").hide();
        });
        $("#select-ok").click(function () {
            var startDate = $("#start").val();
            if (!startDate || startDate === "") {
                alert("请输入起始日期");
                return;
            }
            var endDate = $("#end").val();
            if (!endDate || endDate === "") {
                alert("请输入结束日期");
                return;
            }
            if (new Date(startDate) > new Date(endDate)) {
                alert("起始日期大于结束日期，请重新输入");
                return;
            }
            window.location.href = "/mobile/performance/important_indicator.vpage?startDate=" + startDate + "&endDate=" + endDate ;
        });
        var schoolLevel = $.cookie("SCHOOL_LEVEL");
        if(schoolLevel == "MIDDLE"){
            $("#addStuAuthGradeMathNum").hide();
        }else{
            $("#addStuAuthGradeMathNum").show();
        }
    });
</script>
</@layout.page>