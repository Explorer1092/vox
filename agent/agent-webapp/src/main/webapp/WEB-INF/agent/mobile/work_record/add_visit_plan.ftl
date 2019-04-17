<#import "../layout_new.ftl" as layout>
<@layout.page group="work_record" title="添加计划">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="/mobile/work_record/index.vpage" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">添加计划</div>
            <a href="javascript:void(0);" class="headerBtn js-submitVisPlan">确定</a>
        </div>
    </div>
</div>
<p>必填:</p>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="mobileCRM-V2-list">
        <li>
            <a href="javascript:void(0);" class="link link-ico">
                <div class="side-fl" dataNeed="visitSchool">拜访学校</div>
                <div class="side-fr side-time js-visitSchoolBtn"><#if plan.schoolName??>${plan.schoolName!''}<#else>请选择</#if></div>
                <input hidden type="text" id="schoolId" name="schoolId" value="${plan.schoolId!''}" class="js-need js-postData" data-einfo="请选择拜访学校"/>
            </a>
        </li>
        <li>
            <a href="javascript:void(0);" class="link link-ico">
                <div class="side-fl" dataNeed="visitDate">拜访时间</div>
                <div class="side-fr side-time js-visitDateBtn" id="visitDate" value=""><#if plan.visitTime??>${plan.visitTime?string("yyyy-MM-dd")!''}<#else>请选择</#if></div>
                <input hidden type="text" id="visitTime" name="visitTime" value="<#if plan.visitTime??>${plan.visitTime?string("yyyy-MM-dd")!''}</#if>"  class="js-need js-postData" data-einfo="请选择拜访时间"/>
            </a>
        </li>
    </ul>
</div>
<p>选填:</p>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="mobileCRM-V2-list">
        <li>
            <a href="javascript:void(0);" class="link">
                <div class="side-fl" dataNeed="visitPlanContent">计划内容</div>
                <input class="side-fr side-time js-postData"  type="text" id="content" name="content" value="<#if plan.visitTime??>${plan.content!''}</#if>" placeholder="填写进校时，计划做什么"/>
            </a>
        </li>
    </ul>
</div>

<div id="visitDateDialog" class="mobileCRM-V2-layer" style="display:none">
    <div class="dateBox">
        <div class="boxInner">
            <ul class="mobileCRM-V2-list">
                <li>
                    <div class="box">
                        <div class="side-fl">拜访时间</div>
                        <input type="date" id="start" value="<#if startDate??>${startDate?string("yyyy-MM-dd")!}<#else>${.now?string("yyyy-MM-dd")}</#if>" class="textDate">
                    </div>
                </li>
            </ul>
            <div class="boxFoot" style="cursor: pointer;">
                <div class="side-fl" id="visDateCancel">取消</div>
                <div class="side-fr" id="visDateSure">确定</div>
            </div>
        </div>
    </div>
</div>

<script>
$(function () {
    $(document).on("click",".js-visitDateBtn",function(){
        $("#visitDateDialog").show();
    });

    //保存信息跳页
    var saveInfoToNewPage = function(url){

        var visitTime = $("#visitTime").val();
        var content = $("#content").val();
        var schoolId = $("#schoolId").val();
        var postDate = {
            schoolId:schoolId,
            visitTime:visitTime,
            content:content
        };
        $.post("saveVisitPlan.vpage",postDate,function(res){
            if(res.success){
                location.href = url;
            }else{
                alert(res.info);
            }
        });
    };

    //拜访学校
    $(document).on("click",".js-visitSchoolBtn",function(){
        saveInfoToNewPage("chooseSchool.vpage?back=add_visit_plan.vpage");
    });

    $(document).on("click","#visDateSure",function(){
        var startVal = $("#start").val();
        $("#visitDate").html(startVal);
        $("#visitTime").val(startVal);
        $("#visitDateDialog").hide();
    });

    $(document).on("click","#visDateCancel",function(){
        $("#visitDateDialog").hide();
    });

    //检测提交数据
    var checkData = function(){
        var flag = true;
        $.each($(".js-need"),function(i,item){
            console.log(item);
            console.log($(item).val());
            if(!($(item).val())){
                alert($(item).data("einfo"));
                flag = false;
                return false;
            }
        });

        return flag;
    };

    $(document).on("click",".js-submitVisPlan",function(){
        if(checkData()){
            var postData = {};
            $.each($(".js-postData"),function(i,item){
                postData[item.name] = $(item).val();
            });

            $.post("savePlan.vpage",postData, function (res) {
                if(res.success){
                    location.href =  "/mobile/work_record/visitplan.vpage";
                }else{
                    alert(res.info);
                }
                console.log(res);
            })
        }
    });
});
</script>
</@layout.page>
