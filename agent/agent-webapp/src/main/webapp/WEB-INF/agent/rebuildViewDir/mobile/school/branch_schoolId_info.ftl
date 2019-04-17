<#import "../../../mobile/layout_new_no_group.ftl" as layout>
<#assign groupName="work_record">
<@layout.page title="分校">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">分校</div>
        </div>
    </div>
</div>
    <#--<#if schoolShortInfo??>-->
    <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
        <ul class="mobileCRM-V2-list">
            <li>
                <div class="box">
                    <div class="side-fl">学校ID</div>
                    <div class="side-fr side-time js-schoolIdDiv">${schoolInfo.schoolId!''}</div>
                </div>
            </li>
            <li>
                <div class="box">
                    <div class="side-fl">名称</div>
                    <div class="side-fr side-time">${schoolInfo.schoolName!''}</div>
                </div>
            </li>
            <li>
                <div class="box">
                    <div class="side-fl">地区</div>
                    <div class="side-fr side-time">${schoolInfo.regionName!''}</div>
                </div>
            </li>
            <li>
                <div class="box">
                    <div class="side-fl">年级分布</div>
                    <div class="side-fr side-time">${schoolInfo.gradeDistribution!''}</div>
                </div>
            </li>
        </ul>
    </div>
    <#--</#if>-->
    <div class="mobileCRM-V2-info mobileCRM-V2-mt">
        <div>
            <a href="javascript:void(0);" class="branchSchoolDelBtn">删除该分校</a>
        </div>
    </div>
<script>
    $(".branchSchoolDelBtn").on("click",function(){
        var schoolId = $(".js-schoolIdDiv").html();
        if(schoolId){
            $.post("/mobile/school_clue/drop_branch_school.vpage", {
                schoolId:${schoolId!0},
                branchSchoolId: schoolId
            }, function (result) {
                if(result.success){
                    alert(result.info);
                    window.history.back();
                }else{
                    alert(result.info);
                }
            })
        }
    });
</script>
</@layout.page>