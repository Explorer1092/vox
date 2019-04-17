<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main" showNav="hide">
    <@sugar.capsule js=["ko"] css=["new_teacher.goal","new_teacher.carts"] />
<style>
   .m-main { overflow: visible}
</style>
<div id="mainContent" class="w-base" style="margin-top:20px;">
    <div id="slideContainer" class="sliderHolder tE-tabGrade" name="slider">
        <div id="swipingLeft" class="arrow arrow-disabled arrow-l" style="display: none;"><i class="icon"></i></div>
        <div id="swipingRight" class="arrow arrow-r"><i class="icon"></i></div>
        <ul data-bind="style:{width:$root.clazzList().length*157+'px',position:'absolute',left:'0px',transition: 'left 1s'}">
            <!--ko if: $root.clazzList && $root.clazzList().length > 0-->
            <!--ko foreach:{ data: $root.clazzList(), as: 'item' }-->
            <li class="slideItem" data-bind="css:{active:$index()==0},click:$root.changeClazz.bind($data,$element,$root)">
                <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                <span data-bind="text:item.className"></span>
            </li>
            <!--/ko-->
            <!--/ko-->
        </ul>
    </div>
    <div class="tE-tabGrade-line"></div>
    <div class="tE-unitInfo">
        <div class="title"><span data-bind="text:$root.unitName()"></span>
            <div class="h-slide on">
                <span class="slideText">全部单元<em class="w-icon-arrow w-icon-arrow-blue"></em></span>
                <div class="h-slide-box">
                    <label class="bookName" data-bind="click:changeBook.bind($data,$root)"><span class="name" data-bind="text:$root.bookName()"></span><span class="w-icon-public w-icon-switch w-icon-switchBlue"></span></label>
                    <!--ko if:$root.isModuleList() -->
                    <!--ko foreach:{ data: $root.unitList(), as: 'item' }-->
                    <label><span class="name" data-bind="text:item.moduleName"></span></label>
                    <!--ko foreach:item.units-->
                    <label class="J_unitRadio" style="cursor: pointer;" data-bind="css:{'w-radio-current':defaultUnit},click:$root.changeUnit.bind($data,$element,$root)"><span class="w-radio"></span> <span class="w-icon-md name" data-bind="text:cname"></span></label>
                    <!--/ko-->
                    <!--/ko-->
                    <!--/ko-->
                    <!--ko ifnot:$root.isModuleList() -->
                    <!--ko foreach:{ data: $root.unitList(), as: 'item' }-->
                    <label class="J_unitRadio" style="cursor: pointer;" data-bind="css:{'w-radio-current':item.defaultUnit},click:$root.changeUnit.bind($data,$element,$root)"><span class="w-radio"></span> <span class="w-icon-md name" data-bind="text:item.cname"></span></label>
                    <!--/ko-->
                    <!--/ko-->
                </div>
            </div>
        </div>
    </div>
    <div class="tE-tabMain">
        <!--学生详情-->
        <!--ko if: $root.studentList && $root.studentList().length > 0-->
        <div class="tE-tableList">
            <div class="hd-title" style="margin: -5px 0 5px 0">
                <span data-bind="text:'本单元共布置'+$root.layoutHomeworkTimes()+'次作业'"></span>
                <div class="link"><a data-bind="attr:{href:$root.downloadData()}" target="_blank" class="w-btn w-btn-well w-btn-green">下载</a></div>
            </div>
            <table cellpadding="0" cellspacing="0">
                <thead>
                <tr>
                    <td width="100">学生</td>
                    <td>按时完成</td>
                    <td>补做 </td>
                    <td>未做</td>
                    <td>平均分</td>
                    <td>总作业时长</td>
                    <td>出勤率 </td>
                </tr>
                </thead>
                <tbody>
                <!--ko foreach:{ data: $root.studentList(), as: 'item' }-->
                <tr data-bind="css:{odd:$index()%2==0}">
                    <td><span class="name" data-bind="text:item.studentName || item.studentId"></span></td>
                    <td data-bind="text:item.onTimeNum"></td>
                    <td data-bind="text:item.makeupNum"></td>
                    <td data-bind="text:item.notDoneNum"></td>
                    <td data-bind="text:item.avgScore"></td>
                    <td data-bind="text:item.doHomeworkDuration"></td>
                    <td data-bind="text:item.attendanceRate + '%'"></td>
                </tr>
                <!--/ko-->
                </tbody>
            </table>
            <div class="reportTips" style="color: #b9b9b9;">
                <div class="r-left">报告说明:</div>
                <div class="r-right">
                    1.此报告每天更新，更新数据来源于老师在一起小学布置的作业。仅供参考。<br>
                    2.删除作业，或者老师换班等班级变动都可能对统计数据造成影响。<br>
                    3.平时每次作业的作业时长进行了四舍五入，相加的总时长结果可能和此表的总作业时长有差异，请知悉。
                </div>
            </div>
        </div>
        <!--/ko-->
        <!--ko if: $root.studentList && $root.studentList().length == 0-->
        <div class="tE-tableEmpty">本单元暂无内容</div>
        <!--/ko-->
    </div>
</div>
    <#include "../templates/homeworkv3/changebook.ftl">
    <script type="text/javascript">
        var constantObj = {
            subject          : "${subject!}",
            batchclazzs      : ${batchclazzs![]},
            domain           : '${requestContext.webAppBaseUrl}/'
        };
    </script>
    <@sugar.capsule js=["goal.unitreport"] />
</@shell.page>


