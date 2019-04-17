<#import "../../researchstaffv3.ftl" as com>
<@com.page menuIndex=12 menuType="normal">
<div class="row_vox_right">
    <input type="button" value="返回" id="goldBackBtn">
    <a target="_blank" href="/reward/index.vpage">
        <i class="icon_rstaff icon_rstaff_7"></i> <strong class="text_orange">教学用品中心</strong>
    </a>
</div>
<ul class="breadcrumb_vox">
    <li><a href="javascript:void(0);">大数据报告</a> <span class="divider">/</span></li>
    <li>积分统计 <span class="divider">/</span></li>
    <li class="active">园丁豆记录</li>
</ul>
<#if currentUser.isResearchStaffForCounty()>
<div class="paperStep" style="line-height: 2">
    <strong>获得条件：每份教研员试卷被认证老师布置后，认证老师班级每有一位学生完成试卷，您即可获得1枚园丁豆</strong><br/>
</div>
</#if>
<div class="fill_vox_20">
    <div style="margin: 0 0 15px">
        <strong>园丁豆记录：</strong>（仅显示六个月的记录）
    </div>
    <div id="gold_list_box"><#-- 园丁豆记录列表 --></div>
</div>
<script>
    function createGoldList(index){
        $.get('/rstaff/report/integralstat/goldrecordchip.vpage?pageNumber=' + index, function(data){
            $("#gold_list_box").html(data);
        });
    }

    $(function(){
        createGoldList(1);
        $("#goldBackBtn").on("click",function(){
            window.location.href="/rstaff/report/integralstat/summary.vpage";
        });
    });
</script>
</@com.page>