<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#assign shortIconTail = "?x-oss-process=image/resize,w_48,h_48/auto-orient,1">
<@layout.page title="学校鉴定状态" pageJs="" footerIndex=4>
    <@sugar.capsule css=['school','photo_pic']/>
<style>
    *{font-size:.65rem}
</style>
<div class="head fixed-head">
    <a class="return" href="javascript:window.history.back()"><i class="return-icon"></i>返回</a>
    <span class="return-line"></span>
    <span class="h-title">学校鉴定状态</span>
    <a href="javascript:void(0)" class="inner-right js-submit"><#if canOperation?? && canOperation>提交</#if></a>
</div>

    <#include "school_new.ftl"/>
<script type="text/javascript">

    var AT = new agentTool();
    var schoolId = ${schoolId!0};
    $(document).on("ready",function(){

        $(".js-stage").html($("#schoolPhase>option:selected").text());
        $(".js-type").html($("#schoolType>option:selected").text());

        $(document).on("click",".js-submit",function () {
            var phase = $("#schoolPhase").val();
            var data = {
                phase:phase
            };
            $.post("save_update_school_info.vpage", data, function (res) {
                if (res.success) {
                    AT.alert("创建成功，老师可以注册啦！学校ID:" + res.schoolId);
                    window.location.href = "/mobile/school_clue/user_clues.vpage";
                } else {
                    AT.alert(res.info);
                }
            });
        });

        $(document).on("click",".js-name",function(){
            chooseSchoolName("name");
        });

        $(document).on("change","#schoolPhase",function(){
            var data = $("#schoolPhase>option:selected").text();
            $(".js-stage").html(data);
            $.post("choice_phase.vpage", {phase: data}, function (res) {
                if (res.success) {
                    $("#schoolName").html("请填写");
                }
            })
        });

        var chooseSchoolName = function(nameType) {
            var phase = $("#schoolPhase").val();
            var data = {
                phase: phase,
                schoolId:schoolId
            };
            $.post("save_school_info_session.vpage", data, function (res) {
                if (res.success) {
                    window.location.href = "school_name.vpage?nameType=" + nameType +"&schoolId="+schoolId;
                } else {
                    AT.alert(res.info);
                }
            });
        };

        $(document).on("click","#regionName",function () {
            var schoolPhase = $("#schoolPhase").val();
            var regionCode = $("#regionCode").val();
            var data = {
                phase:schoolPhase,
                schoolId:schoolId
            };
            $.post("save_school_info_session.vpage", data, function (res) {
                if (res.success) {
                    window.location.href = "/mobile/work_record/load_region_page.vpage?type=newSchool&regionCode=" + regionCode+"&schoolId="+schoolId;
                } else {
                    AT.alert(res.info);
                }
            });
        });
    });
    //照片
    $("#photoShow").on("click", function () {
        var schoolPhase = $("#schoolPhase").val();
        var data = {
            phase:schoolPhase,
            schoolId:schoolId
        };
        $.post('save_school_info_session.vpage',data , function (res) {
            if (res.success) {
                window.location.href = 'school_clue_photo.vpage?returnUrl=add_school_info.vpage&schoolId='+schoolId;
            }
        });
    });
</script>
</@layout.page>