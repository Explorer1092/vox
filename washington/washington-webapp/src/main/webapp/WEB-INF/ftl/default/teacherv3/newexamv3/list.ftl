<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
    <@sugar.capsule js=["vue","datepicker"] css=["plugin.datepicker","new_teacher.carts","homeworkv3.homework"] />
    <style type="text/css">
        [v-cloak] {
            display: none;
        }
    </style>
    <div id="newexamList" v-cloak class="h-homeworkList">
        <div class="w-base" style="border-color: #dae6ee;">
            <div class="hl-main">
                <div class="w-base-title">
                    <h3>作业报告</h3>
                    <div class="w-base-right w-base-switch">
                        <ul>
                            <li class="tab">
                                <a  href="javascript:void(0);" v-bind:href="'/teacher/new/homework/report/list.vpage?subject=' + subject">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    作业报告
                                </a>
                            </li>
                            <li class="tab active">
                                <a href="javascript:void(0);" v-bind:href="'/teacher/newexam/list.vpage?subject=' + subject">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    检测报告
                                </a>
                            </li>
                            <li class="tab">
                                <a href="javascript:void(0);" v-bind:href="'/teacher/newexam/report/index.vpage?subject=' + subject">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    统考报告
                                </a>
                            </li>
                            <li class="tab">
                                <a href="javascript:void(0);" v-bind:href="'/teacher/newhomework/unitreport.vpage?subject=' + subject">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    单元报告
                                </a>
                            </li>
                            <li class="tab">
                                <a href="javascript:void(0);" v-bind:href="'/teacher/newhomework/termreport.vpage?subject=' + subject">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    学期报告
                                </a>
                            </li>
                            <li class="tab" style="display: none;">
                                <a href="javascript:void(0);" target="_blank" v-bind:href="'/teacher/termreview/basicreviewreport.vpage?subject=' + subject">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    期末基础复习
                                </a>
                            </li>
                        </ul>
                    </div>
                </div>
                <div class="t-homework-form" v-if="levelClazzList.length > 0">
                    <dl v-for="(levelObj,index) in levelClazzList">
                        <dd style="margin: 0;">
                            <div class="t-homeworkClass-list">
                                <div class="pull-down">
                                    <p v-for="(clazzObj,zIndex) in levelObj.clazzs" v-bind:class="{'w-checkbox-current':groupIds.indexOf(clazzObj.groupId) !=-1}" v-on:click="clazzClick(clazzObj,levelObj.clazzLevel)">
                                        <span class="w-checkbox"></span>
                                        <span class="w-icon-md" v-text="levelObj.clazzLevel + '年级' + clazzObj.clazzName"></span>
                                    </p>
                                </div>
                            </div>
                        </dd>
                    </dl>
                </div>
            </div>
        </div>
        <template v-if="contentList.length > 0">
            <div class="h-workList-box" v-for="(content,index) in contentList">
                <div class="hwl-header">
                    <#--<span class="state" data-bind="css:{'txt-red':!checked(),'txt-green':checked()},text:checked()?'[单元小测]':'[课时小测]'"></span>-->
                    <span v-text="content.clazzName"></span>
                    <#--<span class="s-tag" style="display: none;" data-bind="visible:homework.isTermReview && homework.isTermReview()">期末作业</span>-->
                </div>
                <div class="hwl-main">
                    <table class="hwl-table">
                        <tr>
                            <td class="td-cell01">
                                <div class="title">内容：<span v-bind:title="content.newExamName" v-text="content.newExamName.length > 15 ? content.newExamName.substring(0,15) + '...' : content.newExamName"></span></div>
                                <div class="title">时间：<span v-text="timestampToTime(content.startAt) + ' -- ' + timestampToTime(content.stopAt)"></span></div>
                            </td>
                            <td class="td-cell02">
                                <p class="txt-green"><span class="font-b" v-text="content.finishUserCount">0</span>/<span v-text="content.allUserCount">0</span>人</p>
                                <p class="txt-green">已完成</p>
                            </td>
                            <#--<td class="td-cell02" data-bind="visible:homework.includeSubjective()">
                                <p class="txt-red"><span class="font-b" data-bind="text:(homework.finishedCount() - homework.correctedCount())">0</span>人</p>
                                <p class="txt-red">待批改</p>
                            </td>-->
                            <td class="td-cell03">
                                <a href="javascript:void(0);" v-on:click="deleteHomework(content)" v-if="content.showDelete" class="link">删除</a>
                                <a href="javascript:void(0);" v-on:click = "adjustHomework(content)" v-if="content.showAdjust" class="link">调整</a>
                                <a href="javascript:void(0);" v-on:click="viewReport(content)" class="w-btn w-btn-well">查看详情</a>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </template>
        <vue-page v-bind:total-page="totalPage" v-bind:page-no="pageNo" v-on:page-click="pageClickCb"></vue-page>
        <vue-loading v-if="contentList.length == 0 && hkLoading"></vue-loading>
        <div class="h-workList-box" v-if="contentList.length == 0 && !hkLoading">
            <div class="hwl-header"></div>
            <div class="hwl-main">
                暂未布置单元测
            </div>
        </div>
    </div>

<script id="T:ADJUSTTIME_POPUP" type="text/html">
    <div id="saveMathDialog" class="h-homework-dialog03 h-homework-dialog" style="width: 100%;">
        <div class="inner">
            <div class="list">布置时间：<span v-text="currentDate"></span></div>
            <div class="list">
                <div class="name">布置班级：</div>
                <div class="info grade" v-text="clazz"></div>
            </div>
            <div class="list">
                <div class="name" v-text="practices.typeName + '：'"></div>
                <div class="info">
                    <span class="tj" v-text="practices.title"></span>
                </div>
            </div>
            <div class="list">
                <div class="tips-grey" style="padding-left:70px;">共<span v-text="practices.questionNum">0</span>题&nbsp;&nbsp;预计<span v-text="practices.minutes">0</span>分钟 &nbsp;&nbsp;作答限时<span v-text="(practices.limitMinutes)">0</span>分钟</div>
            </div>
            <div class="list">开始时间：<span v-text="startDateTime"></span></div>
            <div class="list">完成时间：
                <datetime-17picker v-on:change-datetime="changeDateTime" v-bind:style="{display:'inline-block'}" v-bind:default-time-str="endDateTime" v-bind:min-time-str="startDateTimeStr"></datetime-17picker>
            </div>
        </div>
    </div>
</script>
    <#include "../templates/vuepagination.ftl">
    <#include "../templates/vuedatetimepicker.ftl">
    <@sugar.capsule js=["newexamv3.list"] />
<script type="text/javascript">
    var constantObj = {
        subject        : $17.getQuery("subject")
    };

    $(function(){
        LeftMenu.focus(constantObj.subject + "_homeworkhistory");

        $17.voxLog({
            module: "m_Odd245xH",
            op : "page_report_load",
            s0 : constantObj.subject
        });

    });
</script>
</@shell.page>