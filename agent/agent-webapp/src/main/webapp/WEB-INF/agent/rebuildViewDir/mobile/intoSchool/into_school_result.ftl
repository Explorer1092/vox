<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="拜访记录" pageJs=""  footerIndex=4>
    <@sugar.capsule css=['record']/>
<style>
    body {
        background-color: rgb(241, 242, 245)
    }

    .vacation_box {
        margin: .5rem 0;
        padding: .5rem .75rem;
        color: #636880;
        background-color: #fff;
        overflow: hidden
    }

    .vacation_box .vacTitle {
        position: relative;
        font-size: .75rem
    }

    .add_btn {
        position: absolute;
        top: -.25rem;
        right: -.25rem;
        width: 2.25rem;
        height: 1.25rem;
        text-align: center;
        font-size: .7rem
    }

    .vacation_box .vacItem {
        font-size: .65rem;
        line-height: .95rem;
        position: relative;
        margin-top: .25rem
    }

    .vacation_box .vacItem .time {
        color: #bababa
    }

    .vacation_box .vacItem img {
        display: block;
        width: 5rem;
        height: 5rem
    }

    .vacation_box .more_btn {
        float: right;
        font-size: .6rem;
        color: #898c91
    }

    .vacation_box .vacInfo {
        padding: 0 0 .5rem 0;
        font-size: .65rem;
        color: #636880
    }

    .vacation_box .listNav {
        display: -webkit-box;
        display: -moz-box
    }

    .vacation_box .listNav li {
        -webkit-box-flex: 1;
        -moz-box-flex: 1;
        margin: 0 .125rem;
        display: block;
        text-align: center;
        width: 100%;
        height: 1.25rem;
        font-size: .6rem;
        color: #636880;
        line-height: 1.25rem;
        border: .05rem solid #636880;
        border-radius: .5rem
    }

    #vacChartDimension li {
        -webkit-box-flex: 1;
        -moz-box-flex: 1;
        margin: 0 .125rem;
        display: block;
        text-align: center;
        width: 45%;
        height: 1.5rem;
        float: left;
        font-size: .6rem;
        color: #636880;
        line-height: 1.5rem;
        border: .05rem solid #636880;
        border-radius: .5rem
    }

    .vacation_box .listNav li.active, #vacChartDimension li.active {
        color: #ff7d5a;
        border: .05rem solid #ff7d5a
    }
</style>
<div>
    <div class="tea-staffRes-main tab_box" style="">
        <div class="new_box01">
            <div class="info">
                <div class="inline-list data-list manager c-flex c-flex-4 record_4" style="background:#fff;margin-bottom:.5rem;border:none">
                    <div>
                    ${monthView.intoSchoolCount!0}
                        <p>进校（次）</p>
                    </div>
                    <div>
                    ${monthView.visitedSchoolCount!0}/${monthView.schoolTotal!0}
                        <p>已访校/学校总数</p>
                    </div>
                    <div>
                    ${monthView.visitTeacherAvg!0}
                        <p>校均拜访老师</p>
                    </div>
                    <div>
                    ${monthView.visitTeacherHwPro!0}%
                        <p>拜访老师布置作业率</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="vacation_box" style="margin-top:.5rem;clear:both;">
        <div class="vacTitle">
            本月拜访后未布置作业老师
            <a href="javascript:;" onclick="openSecond('/mobile/into_school/visit_teacher.vpage?userId=${userId!0}')" class="add_btn" style="line-height:1.35rem">${teacherSize!0}> </a>
        </div>
    </div>
<#--进校记录-->
    <#if schoolList?? &&schoolList?size gt 0>
        <div class="vacation_box" style="margin-bottom:-.5rem;border-bottom: 1px solid #eaeaea;">
            <div class="vacTitle">
                本月进校情况：
            </div>
        </div>
        <#list schoolList as school>
            <div class="vacation_box" <#if (school.visitCountLte30?has_content && school.visitCountLte30 gt 2) || (school.visitTeacherCount?? && school.visitTeacherCount lt 2)>style="background: yellow"</#if>>
                <div class="vacTitle">
                ${school.schoolName!""}
                    <a href="javascript:;"<#-- onclick="openSecond('/mobile/work_record/add_intoSchool_record.vpage?schoolId=${schoolId!0}')"-->
                       class="add_btn"> ${school.lastVisitTime!""} </a>
                </div>
                <div class="vacItem">
                    <div <#if school.visitTeacherCount?? && school.visitTeacherCount lt 2>style="color:red;" </#if>>
                        拜访了${school.visitTeacherCount!0}位老师<#if school.visitOtherTeacher?? && school.visitOtherTeacher?size gt 0>
                        ，<#list school.visitOtherTeacher as teacher>${teacher!""}<#if teacher_has_next>、</#if></#list></#if></div>
                    <#if school.visitCountLte30?has_content && school.visitCountLte30 gt 2 ><a style="color:red;width:6rem;line-height:1.4rem;font-size:.65rem"
                                                                                               class="add_btn">
                        30天内第${school.visitCountLte30!0}次拜访 </a></#if>
                </div>
            </div>
        </#list>
    </#if>
</div>
<script>
    var AT = new agentTool();
    var month = "${queryMonth!''}";

    $(document).on('click', '.js-tab span', function () {
        $(this).addClass("active").siblings("span").removeClass("active");
        var type = $(this).data("type");
        $('.tab_box').eq(type).show().siblings().hide();
    });
    $(document).on('click', '.personalDetails', function () {
        var workUserId = $(this).attr('data-id');
        AT.setCookie("workUserId", workUserId);
        openSecond("/mobile/into_school/record_list_and_statistics.vpage?workUserId=" + workUserId + "&queryMonth=" + month);
    });
    $(document).on('click', '.js-search02', function () {
        var roleType = $(this).attr("data-roleType");
        var schoolInput = $("#schoolSearch02").val();
        if (schoolInput != "") {
            $.get("record_statistics_list.vpage", {
                queryMonth: month,
                roleType: roleType,
                queryCriteria: schoolInput
            }, function (data) {
                if (data.success) {
                    if (data.statisticsList != null && data.statisticsList.length > 0) {
                        renderTemplate("new_man", {"statisticsList": data.statisticsList}, ".new_man02");
                    } else {
                        AT.alert('暂无数据')
                    }
                } else {
                    AT.alert(data.info);
                }
            });
        } else {
            AT.alert("请输入姓名");
        }
    });
    $('.js-showHand').on('click', function () {
        if ($('.feedbackList-pop').hasClass('show_now')) {
            $('.feedbackList-pop').removeClass('show_now').show();
        } else {
            $('.feedbackList-pop').addClass('show_now').hide();
        }

    });
    $(document).on('click', '.js-search01', function () {
        var roleType = $(this).attr("data-roleType");
        var schoolInput = $("#schoolSearch01").val();
        if (schoolInput != "") {
            $.get("record_statistics_list.vpage", {
                queryMonth: month,
                roleType: roleType,
                queryCriteria: schoolInput
            }, function (data) {
                if (data.success) {
                    if (data.statisticsList != null && data.statisticsList.length > 0) {
                        renderTemplate("new_man", {"statisticsList": data.statisticsList}, ".new_man01");
                    } else {
                        AT.alert('暂无数据')
                    }
                } else {
                    AT.alert(data.info);
                }
            });
        } else {
            AT.alert("请输入姓名");
        }
    });
    $(document).on('click', '.js-sort', function () {
        var setKey = $(this).data().type;
        AT.setCookie("dataType", setKey);
    });
    var csid = AT.getCookie("workUserId");
    if (csid) {
        var detail = $("#detail" + csid);
        console.log($("#detail" + csid).length);
        if (detail.length != 0) {
            var scroll_offset = $("#detail" + csid).offset();
            $("body,html").animate({
                scrollTop: parseFloat(scroll_offset.top) - 184 // 减掉被顶部和筛选条遮挡的部分
            }, 0);
        }
    }
    var getType = AT.getCookie("dataType");
    if (getType) {
        if ($('.js-key01').data().type == getType) {
            $('.js-key01').click();
        } else if ($('.js-key02').data().type == getType) {
            $('.js-key02').click();
        } else if ($('.js-key03').data().type == getType) {
            $('.js-key03').click();
        }
    }
    var renderTemplate = function (tempSelector, data, container) {
        var contentHtml = template(tempSelector, data);
        $(container).html(contentHtml);
    };
</script>
</@layout.page>