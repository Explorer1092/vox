<#-- @ftlvariable name="userName" type="java.lang.String" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
<#-- @ftlvariable name="studentHomeworkHistoryList" type="java.util.List<com.voxlearning.utopia.mapper.DisplayStudentHomeWorkHistoryMapper>" -->
<#-- @ftlvariable name="mathHomeWorkHistoryMapperList" type="java.util.List<com.voxlearning.utopia.mapper.DisplayStudentMathHomeWorkHistoryMapper>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <form method="post" action="?" class="form-horizontal">
            <fieldset>
                <legend><a href="../user/userhomepage.vpage?userId=${userId!}">${userName!}</a>(${userId!})作业详情</legend>
            </fieldset>
            <ul class="inline form_datetime">
                <li>
                    <label for="startDate">
                        查询时间
                        <input name="date" id="date" type="text" placeholder="格式：2014-07-01"/>
                    </label>
                </li>
                <li>
                    <button type="submit" class="btn btn-success">查询</button>
                </li>
            </ul>
            <input name="userId" type="hidden" value="${userId!}">
        </form>
        <strong>英语作业</strong>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 作业ID</th>
                <th> 应用</th>
                <th> 作业开始时间</th>
                <th> 作业结束时间</th>
                <th> 作业提交时间</th>
                <th> 是否检查</th>
                <th> 是否完成</th>
                <th> 作业分数</th>
                <th> 班级ID</th>
                <th> 课本名称</th>
            </tr>
            <#if studentHomeworkHistoryList?has_content>
                <#list studentHomeworkHistoryList as studentHomeworkHistory>
                    <tr>
                        <td>
                            <a target="_blank" href="../homework/homeworkhomepage.vpage?homeworkId=${studentHomeworkHistory.homeworkId!}&homeworkSubject=ENGLISH&studentId=${userId!}">${studentHomeworkHistory.homeworkId!""}</a>
                        </td>
                        <td>
                            <a target="_blank" href="http://www.17zuoye.com/flash/loader/selfstudy-${studentHomeworkHistory.practiceId!}-${userId!}-${studentHomeworkHistory.bookId!}-${studentHomeworkHistory.unitId!}-${studentHomeworkHistory.lessonId!}.vpage">${studentHomeworkHistory.practiceName!""}</a>
                        </td>
                        <td>${studentHomeworkHistory.startDate!""}</td>
                        <td>${studentHomeworkHistory.endDate!""}</td>
                        <td>${studentHomeworkHistory.finishTime!""}</td>
                        <td><#if studentHomeworkHistory.checked?? && studentHomeworkHistory.checked>是<#else >否</#if></td>
                        <td>${(studentHomeworkHistory.completed?string('是', '否'))!'否'}</td>
                        <td>${studentHomeworkHistory.score!""}</td>
                        <td>
                            ${studentHomeworkHistory.clazzName!""}(${studentHomeworkHistory.clazzId!""})
                        </td>
                        <td>${studentHomeworkHistory.bookName!""}</td>
                    </tr>
                </#list>
            </#if>
        </table>
        <strong>数学作业</strong>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 作业ID</th>
                <th> 应用</th>
                <th> 作业开始时间</th>
                <th> 作业结束时间</th>
                <th> 作业提交时间</th>
                <th> 是否检查</th>
                <th> 是否完成</th>
                <th> 作业分数</th>
                <th> 课本名称</th>
            </tr>
            <#if mathHomeWorkHistoryMapperList?has_content>
                <#list mathHomeWorkHistoryMapperList as mathHomeWorkHistoryMapper>
                    <tr>
                        <td>
                            <a target="_blank" href="../homework/homeworkhomepage.vpage?homeworkId=${mathHomeWorkHistoryMapper.homeworkId!}&homeworkSubject=MATH&studentId=${userId!}">${mathHomeWorkHistoryMapper.homeworkId!}</a>
                        </td>
                        <td>
                            <a target="_blank" href="http://www.17zuoye.com/flash/loader/mathselfstudy-${mathHomeWorkHistoryMapper.practiceId!}-${userId!}-${mathHomeWorkHistoryMapper.bookId!}-${mathHomeWorkHistoryMapper.unitId!}-${mathHomeWorkHistoryMapper.lessonId!}-${mathHomeWorkHistoryMapper.pointId!}.vpage">${mathHomeWorkHistoryMapper.practiceName!""}</a>
                        </td>
                        <td>${mathHomeWorkHistoryMapper.startDate!""}</td>
                        <td>${mathHomeWorkHistoryMapper.endDate!""}</td>
                        <td>${mathHomeWorkHistoryMapper.finishTime!""}</td>
                        <td>${(mathHomeWorkHistoryMapper.checked?string('是', '否'))!'否'}</td>
                        <td>${(mathHomeWorkHistoryMapper.completed?string('是', '否'))!'否'}</td>
                        <td>${mathHomeWorkHistoryMapper.score!""}</td>
                        <td>${mathHomeWorkHistoryMapper.bookName!""}</td>
                    </tr>
                </#list>
            </#if>
        </table>
        <strong>语文作业</strong>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 作业ID</th>
                <th> 应用</th>
                <th> 作业开始时间</th>
                <th> 作业结束时间</th>
                <th> 作业提交时间</th>
                <th> 是否检查</th>
                <th> 是否完成</th>
                <th> 作业分数</th>
                <th> 班级ID</th>
                <th> 课本名称</th>
            </tr>
            <#if chineseHomeWorkHistoryMapperList?has_content>
                <#list chineseHomeWorkHistoryMapperList as studentHomeworkHistory>
                    <tr>
                        <td>
                            <a target="_blank" href="../homework/homeworkhomepage.vpage?homeworkId=${studentHomeworkHistory.homeworkId!}&homeworkSubject=CHINESE&studentId=${userId!}">${studentHomeworkHistory.homeworkId!""}</a>
                        </td>
                        <td>
                            <a target="_blank" href="http://www.17zuoye.com/flash/loader/chineseselfstudy-${studentHomeworkHistory.practiceId!}-${userId!}-${studentHomeworkHistory.bookId!}-${studentHomeworkHistory.unitId!}-${studentHomeworkHistory.lessonId!}.vpage">${studentHomeworkHistory.practiceName!""}</a>
                        </td>
                        <td>${studentHomeworkHistory.startDate!""}</td>
                        <td>${studentHomeworkHistory.endDate!""}</td>
                        <td>${studentHomeworkHistory.finishTime!""}</td>
                        <td><#if studentHomeworkHistory.checked?? && studentHomeworkHistory.checked>是<#else >否</#if></td>
                        <td>${(studentHomeworkHistory.completed?string('是', '否'))!'否'}</td>
                        <td>${studentHomeworkHistory.score!""}</td>
                        <td>
                            ${studentHomeworkHistory.clazzName!""}(${studentHomeworkHistory.clazzId!""})
                        </td>
                        <td>${studentHomeworkHistory.bookName!""}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>

<script type="text/javascript">

    $(function() {
        $("#date").datepicker({
            dateFormat: 'yy-mm-dd',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
            }
        });
    });
</script>
</@layout_default.page>