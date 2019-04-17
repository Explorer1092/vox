<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="top校榜" pageJs="topschool" footerIndex=2>
    <@sugar.capsule css=['res','intoSchoEffeNew']/>
<style>
    .resources-box .resCom-table table tbody tr .name{
        width:10rem;
    }
</style>
    <div class="crmList-box resources-box">
        <div class="c-main resCom-content">
                <div class="resCom-main">
                    <ul class="resCommissioner-list">
                        <li class="active">
                            <div class="resCom-info js-togBtn">
                            认证学生完成2套作业英活TOP5(小学)
                            </div>
                            <div class="resCom-table" style="display: none;">
                                <table cellpadding="0" cellspacing="0">
                                    <thead>
                                    <tr style="border-bottom:1px dashed rbg(221,226,234)">
                                        <td class="name">
                                            学校
                                        </td>
                                        <td>
                                            2套英活
                                        </td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                        <#if juniorEnglish17Top?? && juniorEnglish17Top?size gt 0 >
                                            <#list juniorEnglish17Top as tl>
                                            <tr class="teacher-href" data-tid="${tl.schoolId!0}" data-sj="2" style="cursor: pointer;">
                                                <td>
                                                    <span class="name">${tl.schoolName!''}</span>
                                                </td>
                                                    <td>${tl.finEngHwEq2AuStuCount!''}&nbsp;</td>
                                            </tr>
                                            </#list>
                                        <#else>
                                            <tr style="cursor: pointer;">
                                                <td class="name">
                                                    暂无数据
                                                </td>
                                            </tr>
                                        </#if>
                                    </tbody>
                                </table>
                            </div>
                        </li>
                    </ul>
                    <ul class="resCommissioner-list">
                        <li class="active">
                            <div class="resCom-info js-togBtn">
                                认证学生完成2套作业英活TOP5(中学)
                            </div>
                            <div class="resCom-table" style="display: none;">
                                <table cellpadding="0" cellspacing="0">
                                    <thead>
                                    <tr style="border-bottom:1px dashed rbg(221,226,234)">
                                        <td class="name">
                                            学校
                                        </td>
                                        <td>
                                            2套英活
                                        </td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                        <#if middleEnglish17Top?? && middleEnglish17Top?size gt 0>
                                            <#list middleEnglish17Top as gl>
                                                <tr class="teacher-href" data-tid="${gl.schoolId!0}" data-sj="2" style="cursor: pointer;">
                                                    <td>
                                                        <span class="name">${gl.schoolName!''}</span>
                                                    </td>
                                                    <td>${gl.finEngHwEq2AuStuCount!''}&nbsp;</td>
                                                </tr>
                                            </#list>
                                        <#else>
                                            <tr style="cursor: pointer;">
                                                <td class="name">
                                                    暂无数据
                                                </td>
                                            </tr>
                                        </#if>
                                    </tbody>
                                </table>
                            </div>
                        </li>
                    </ul>
                    <ul class="resCommissioner-list">
                        <li class="active">
                            <div class="resCom-info js-togBtn">
                                认证学生完成2套作业数活TOP5(小学)
                            </div>
                            <div class="resCom-table" style="display: none;">
                                <table cellpadding="0" cellspacing="0">
                                    <thead>
                                    <tr style="border-bottom:1px dashed rbg(221,226,234)">
                                        <td class="name">
                                            学校
                                        </td>
                                        <td>
                                            2套数活
                                        </td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                        <#if math17Top?? && math17Top?size gt 0>
                                            <#list math17Top as tl>
                                                <tr class="teacher-href" data-tid="${tl.schoolId!0}" data-sj="4" style="cursor: pointer;">
                                                    <td>
                                                        <span class="name">${tl.schoolName!''}</span>
                                                    </td>
                                                    <td>${tl.finMathHwEq2AuStuCount!''}&nbsp;</td>
                                                </tr>
                                            </#list>
                                        <#else>
                                            <tr style="cursor: pointer;">
                                                <td class="name">
                                                    暂无数据
                                                </td>
                                            </tr>
                                        </#if>
                                    </tbody>
                                </table>
                            </div>
                        </li>
                    </ul>
                </div>
        </div>
    </div>
    <script>
        $(document).ready(function(){
            YQ.voxLogs({
                database : "marketing", //不设置database  , 默认库web_student_logs
                module : "m_wo7PHLtz", //打点流程模块名
                op : "o_yXeDhn5z" ,//打点事件名
                userId:${requestContext.getCurrentUser().getUserId()!0},
                s0 : <#if user?has_content>${user.id!0}<#else>${requestContext.getCurrentUser().getUserId()!0}</#if>
            });
        });
        /*--tab切换--*/
        $(".tab-head").children("a,span").on("click",function(){
            var $this=$(this);
            $this.addClass("the").siblings().removeClass("the");
            $(".tab-main").eq(0).children().eq($this.index()).show().siblings().hide();
        });

        $(document).on("click",".js-togBtn",function(){
            var $this=$(this);
            $this.next(".resCom-table").slideToggle(function(){
                $this.toggleClass("dashed");
                $this.parent("li").toggleClass("active");
            });
        });

        $(document).on("click",".teacher-href",function() {
            location.href="/mobile/resource/school/school_performance_data.vpage?schoolId="+$(this).data().tid + "&performanceType=" + $(this).data().sj;
        });
        var userName = "<#if user?has_content>${user.realName!""}</#if>";
    </script>

</@layout.page>