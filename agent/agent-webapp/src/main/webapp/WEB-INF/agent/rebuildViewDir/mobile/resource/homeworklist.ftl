<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="作业记录" pageJs="common" footerIndex=2>
<@sugar.capsule css=['res']/>
<style>
    .active{
        background:#ff7d5a;
        color:#fff
    }
</style>
<div class="resources-box" style="background:#f1f2f5">
    <ul style="width:100%;height:2rem;background: #fff;line-height: 2rem;font-size:.7rem;margin-top:.5rem">
        <li class="active tab_homework" style="float:left;width:50%;text-align:center" data-info="usualHomework">日常作业</li>
        <li class="tab_homework" style="float:left;width:50%;text-align: center;" data-info="holidyHomework">假期作业包</li>
    </ul>
    <div style="margin-top:.5rem">
        <div class="usualHomework">
            <div class="res-autInfor autInor-mar">
                <table class="aut-table">
                    <thead>
                    <tr>
                        <td>班级</td>
                        <td>本月布置<br>(指定/所有)</td>
                        <td>上月布置<br>(所有)</td>
                    </tr>
                    </thead>
                    <tbody>
                        <#if clazzList?has_content>
                            <#list clazzList as clazz>
                                <tr>
                                <td>${clazz.classFullName!'-'}</td>
                                <td>${clazz.groupKpiData.tmTgtHwSc!'-'}/${clazz.groupKpiData.tmHwSc!'-'}</td>
                                <td>${clazz.groupKpiData.lmHwSc!'-'}</td>
                                </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
            <#if homeWorkList?has_content && homeWorkList?size gt 0>
                <#list homeWorkList as time>
                    <div class="homeworkRecord-box">
                        <div class="hwd-time">${time.monthGroupName!''}</div>
                        <#if time.hwList?has_content && time.hwList?size gt 0>
                            <#list time.hwList as Home>
                                <div class="hwd-list <#if Home.disabled!false> deleteBg</#if>">
                                    <div class="lis-right">
                                        <p class="num"><span class="fontOrange">${Home.finishHwStuNum!'0'}</span>/${Home.sumClassStuNum!'0'}</p>
                                        <p class="info" style="margin-top:0.4rem">已完成</p>
                                    </div>
                                    <div class="lis-left">
                                        <p class="title">${Home.clazzName!''}
                                            <#if Home.schoolLevel?has_content && Home.schoolLevel == 1>
                                                <#if Home.hwCheckStatus!false >
                                                    <span class="inspect fontGreen">已检查</span>
                                                <#else >
                                                    <span class="inspect fontOrange">未检查
                                                </#if>
                                            </#if>
                                        </p>
                                        <p class="subtitle" style="margin-top:0.4rem">布置时间：${Home.assignDate?string("yyyy/MM/dd")!''}<#if Home.oldTeacherId?has_content><span class="inspect" style="color:green">${Home.oldTeacherName!''}布置</span></#if></p>
                                    </div>
                                        <#if Home.disabled!false><div class="lis-tag">已删除</div></#if>
                                </div>
                            </#list>
                        </#if>
                    </div>
                </#list>
            </#if>
            <div class="hwd-foot">说明：仅供查询15天老师布置作业记录</div>
        </div>
        <div class="holidyHomework" style="display:none;">
            <div class="homeworkRecord-box">
                <#if vacationHomeworkPackageList?has_content && vacationHomeworkPackageList?size gt 0>
                    <#list vacationHomeworkPackageList as Home>
                        <div class="hwd-list">
                            <div class="lis-left">
                                <p class="title">
                                    ${Home.className!''}
                                    <#if Home.homeworkFlag?has_content && Home.homeworkFlag == 'termReview'>
                                        <i class="icon-final-review"></i>
                                    </#if>
                                    <#if Home.homeworkFlag?has_content && Home.homeworkFlag == 'vacnHw'>
                                        <i class="icon-summer-homework"></i>
                                    </#if>
                                </p>
                                <p class="subtitle" style="margin-top:0.4rem">作业时间：${Home.startTime!''}-${Home.endTime!''}</p>
                                <p class="subtitle" style="margin-top:0.4rem">班级人数:${Home.totalNum!0}，${Home.beginNum!'0'}人开始作业，${Home.finishNum!'0'}人完成</p>
                            </div>
                        </div>
                    </#list>
                </#if>
            </div>
        </div>
    </div>
</div>
<script>
    $(document).on("click",".tab_homework",function () {
        $(this).addClass("active").siblings().removeClass("active");
        var infoName = $(this).data("info");
        $("."+infoName).show().siblings().hide();
    })
</script>
</@layout.page>