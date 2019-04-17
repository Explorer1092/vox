<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="termreport" showNav="hide">
    <@sugar.capsule js=["ko"] css=["new_teacher.goal"] />


<div id="mainContent" class="w-base" style="margin-top:20px;">
    <div class="tE-tabGrade slider" id="tE-tabGrade" name="slider">
        <div class="arrow arrow-disabled arrow-l" id="sliderL"><i class="icon"></i></div>
        <div class="arrow arrow-r" id="sliderR"><i class="icon"></i></div>
        <ul data-bind="style:{width:$root.clazzList().length*157+'px',position:'absolute',left:'0px',transition: 'left 1s'}">
            <!--ko if:$root.clazzList && $root.clazzList().length > 0 -->
            <!--ko foreach:{data:$root.clazzList(),as:'item'}-->
                <li class="slideItem" data-bind="css:{active:item.groupId == $root.groupId()},click:$root.changeClazz.bind($data,$element,$root)">
                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                    <span data-bind="text:item.className"></span>
                </li>
            <!--/ko-->
            <!--/ko-->
        </ul>
    </div>
    <div class="tE-tabGrade-line"></div>
    <div class="tE-unitInfo">
        <div class="title">
            <span id="termMonth" data-bind="text:$root.termDate()"></span>
            <div class="h-slide on">
                <span class="slideText">更换学期<em class="w-icon-arrow w-icon-arrow-blue"></em></span><!--向上w-icon-arrow-topBlue-->
                <!--w-icon-arrow-blue-->
                <div class="h-slide-box" id="h-slide-box">
                    <!--ko foreach:{data : $root.dateList,as : 'dateObj'}-->
                    <label style="cursor: pointer;" class="defaultClass" data-bind="css:{'w-radio-current':dateObj.active && dateObj.active()},click:$root.changeTerm.bind($data,$element,$root)">
                        <span class="w-radio"></span> <span class="w-icon-md name" data-bind="text:dateObj.name">&nbsp;</span>
                    </label>
                    <!--/ko-->
                </div>
            </div>
        </div>
    </div>
    <div class="tE-tabMain">
        <!--学生详情-->
        <div class="tE-tableList">
            <div class="hd-title" style="margin: -5px 0 5px 0">
                <span data-bind="text:'本学期共布置'+$root.layoutHomeworkTimes()+'次作业'"></span>
                <div class="link">
                    <a href="javascript:void(0);" data-bind="attr:{href:$root.generateDownloadUrl()}" target="_blank" class="w-btn w-btn-well w-btn-green">下载</a>
                </div>
            </div>
            <table cellpadding="0" cellspacing="0">
                <thead>
                <tr>
                    <td class="fixed" width="129"><i class="tableTitle"></i></td>
                    <!--ko foreach:{data:$root.monthLayoutInfoList(), as: 'item'}-->
                        <td><span  data-bind="text:item.month"></span><br>(<span data-bind="text:item.layoutCount"></span>次)</td>
                    <!--/ko-->
                    <td>累计<br>(<span data-bind="text:layoutHomeworkTimes()"></span>次)</td>
                    <td>出勤率</td>
                    <td>平均分</td>
                </tr>
                </thead>
                <tbody>
                    <!--ko foreach:{data:$root.studentList(), as: 'item'}-->
                        <tr data-bind="css:{odd:$index()%2 == 0}">
                            <td><span class="name" data-bind="text:item.studentName || item.studentId"></span></td>
                            <!--ko foreach:{data:item.monthDoHomeworkBOList, as: 'item2'}-->
                                <td data-bind="text:item2.completeCount"></td>
                            <!--/ko-->
                            <td data-bind="text:item.attendTimes"></td>
                            <td><span data-bind="text:item.attendanceRate"></span>%</td>
                            <td data-bind="text:item.avgScore"></td>
                        </tr>
                    <!--/ko-->
                </tbody>
            </table>
            <div class="reportTips" style="color: #b9b9b9;">
                <div class="r-left">报告说明:</div>
                <div class="r-right">
                    1.学期报告，每个月结束后显示并更新此月作业数据；更新数据来源于老师在一起小学布置的作业。仅供参考。<br>
                    2.删除作业，或者老师换班等班级变动都可能对统计数据造成影响。
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    var constantObj = {
        subject   : "${subject!}",
        clazzList : ${batchclazzs!},
        domain    : '${requestContext.webAppBaseUrl}/',
        dateList  : <#if dateList??>${dateList}<#else>[]</#if>
    }
</script>
    <@sugar.capsule js=["goal.termreport"] />

</@shell.page>