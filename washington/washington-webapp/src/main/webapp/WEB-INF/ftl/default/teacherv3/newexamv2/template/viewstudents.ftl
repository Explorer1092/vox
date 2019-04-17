<script type="text/html" id="T:ViewByStudents">
    <div class="results-list">
        <h4 class="title">成绩单</h4>
        <p class="people-num">应考人数：<!--ko text:$root.stResult().studentNum--><!--/ko-->人 实考人数：<!--ko text:$root.stResult().joinNum--><!--/ko-->人</p>
        <p style="text-align: end;"><a style="color: #59a2f4;" data-bind="click:$root.download.bind($root)">下载班级成绩</a></p>
        <div class="box-table">
            <table>
                <thead>
                    <tr>
                        <td>姓名</td>
                        <td>总成绩（满分<!--ko text:$root.stResult().paperTotalScore--><!--/ko-->分）</td>
                        <td data-bind="visible:!$root.stResult().single">试卷</td>
                        <td>答题时长</td>
                        <td>查看</td>
                    </tr>
                </thead>
                <tbody data-bind="foreach:{data : $root.stResult().students,as:'student'}">
                    <tr>
                        <td data-bind="text:student.userName ? student.userName : student.userId">&nbsp;</td>
                        <td data-bind="text:student.scoreStr">99</td>
                        <td data-bind="visible:!$root.stResult().single"><!--ko text:student.paperName--><!--/ko--></td>
                        <td data-bind="text:student.durationStr">&nbsp;</td>
                        <td data-bind="if:student.begin,visible:student.begin">
                            <a class="color-1" style="color: #199cfc;" target="_blank" data-bind="attr:{href : $root.viewStudentHref($data)}" href="javascript:void(0);">查看</a>
                        </td>
                        <td data-bind="if:!student.begin,visible:!student.begin">--</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</script>