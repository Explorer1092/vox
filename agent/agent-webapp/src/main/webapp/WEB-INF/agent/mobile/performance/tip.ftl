<#import "../layout_new.ftl" as layout>
<@layout.page group="业绩" title="我的业绩">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <div class="headerText">业绩（小学）</div>
            <a href="javascript:void(0);" class="headerBtn" type="noAuth">切换</a>
        </div>
    </div>
</div>
<div style="font-size: 24px; position: absolute;top: 48%; left: 50%; width: 260px; margin-left: -130px;">年后开放，准备过年啦~</div>
<script>
    $(function () {
        $(".headerBtn").hide();
        var selectSchoolLevel = $.cookie("selectSchoolLevel");
        if(selectSchoolLevel == 'MIDDLE') {
            $(".headerText").text("业绩（中学）");
        } else {
            $(".headerText").text("业绩（小学）");
        }
        $(".headerBtn").click(function(){
            var isAll = $.cookie("isAll");
            if(selectSchoolLevel == 'MIDDLE') {
                $.cookie("selectSchoolLevel", "", {path: "/", expires : -1});
                $.cookie("selectSchoolLevel","JUNIOR",{path: "/"});
            } else {
                $.cookie("selectSchoolLevel", "", {path: "/", expires : -1});
                $.cookie("selectSchoolLevel","MIDDLE",{path: "/"});
            }
            window.location.reload();
        });



        var isAll = $.cookie("isAll");
        if (isAll == 'true') {
            $(".headerBtn").show();
        }
    });
</script>
</@layout.page>