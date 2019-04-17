<#import "../../layout_default.ftl" as layout_default>
    <@layout_default.page page_title = "小鹰学堂管理" page_num= 24 >

    <span class="span9">
    <div id="legend" class="">
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation"><a href="studentlearninfo.vpage?studentId=${studentId!''}">学生课程管理</a></li>
            <li role="presentation" class="active"><a href="courseinfoindex.vpage">课程内容管理</a></li>
            <li role="presentation"><a href="coursekindindex.vpage">课程种类管理</a></li>
            <li role="presentation"><a href="teacherindex.vpage">教师管理</a></li>
            <li role="presentation"><a href="classhourmodel.vpage">单日课时模板</a></li>
        </ul>
    </div>
        <div>
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>课时id</dt>
                    <dd><input type="text" id="classHourId" value="${(classHourId)!}" readonly/></dd></li>
                <li>
                    <dt>课程名称</dt>
                    <dd><input type="text" id="courseName" value="${(courseName)!}" readonly/></dd></li>
                <li>
                    <dt>课程种类</dt>
                    <dd><input type="text" id="courseKind" value="${(courseKind)!}" readonly/></dd></li>
                <li>
                    <dt>教师姓名</dt>
                    <dd><input type="text" value="${(TeacherName)!}" id="teacherName" readonly/></dd></li>
                <li>
                    <dt>开始时间</dt>
                    <dd><input id="startTime" value="${(StartTime)!}" readonly type="text"/></dd></li>
                <li>
                    <dt>结束时间</dt>
                    <dd><input type="text" value="${(EndTime)!}" id="endTime" readonly/></dd></li>
            </ul>
        </dl>
        </div>
        <#if studentLearnRecords ?? >
            <div class="table_soll">
            <#if recordSize ?? >总共：${recordSize!0}</#if>
                <table class="table table-bordered">
                    <tr>
                        <th>学生id</th>
                        <th width="160px">代替课时id</th>
                        <th>直播</th>
                        <th>观看时长（s）</th>
                        <th>星星数</th>
                        <th>礼包状态</th>
                        <th>创建时间</th>
                        <th width="180px">操作</th>
                    </tr>
                    <tbody id="tbody">
                        <#list studentLearnRecords as learnCourseRecord>
                        <tr>
                            <td><a href="/equator/babyeagle/studentlearninfo.vpage?studentId=${(learnCourseRecord.studentId)!}">${(learnCourseRecord.studentId)!} (${(learnCourseRecord.studentName)!})</a></td>
                            <td><#if learnCourseRecord.replaceClassHour??>${(learnCourseRecord.replaceClassHour.startTime)?default("")}~<br/>${(learnCourseRecord.replaceClassHour.endTime)?default("")}</#if></td>
                            <td>${(learnCourseRecord.isLive)?string('是', '否')}</td>
                            <td>${(learnCourseRecord.duration)?default("")}</td>
                            <td>${(learnCourseRecord.starNum)?default("")}</td>
                            <td>${(learnCourseRecord.giftStatus)?default("")}</td>
                            <td>${(learnCourseRecord.ct)?default("")}</td>
                            <td><input class="btn btn-danger" type="button" value="删除" name="deleteLearnRecord"
                                       data-recordId="${(learnCourseRecord.id)?default("")}">
                             <input class="btn btn-default" type="button" value="更换代替课时" name="addreplaceClassHour"
                                    data-recordId="${(learnCourseRecord.id)?default("")}" ></td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
                （以上总数超过100条时只显示最近100条学生购课详情）
            </div>
        </#if>
        <#if chinaculturestudentLearnRecords ?? >
             <div class="table_soll">
            <#if chinaculturestudentLearnRecords ?? >总共：${chinaculturerecordSize!0}</#if>
                <table class="table table-bordered">
                    <tr>
                        <th>学生id</th>
                        <th>观看时长（s）</th>
                        <th>礼包状态</th>
                        <th>创建时间</th>
                    </tr>
                    <tbody>
                        <#list chinaculturestudentLearnRecords as chinaculturestudentLearnRecord>
                        <tr>
                            <td><a href="/equator/babyeagle/studentlearninfo.vpage?studentId=${(chinaculturestudentLearnRecord.studentId)!}">${(chinaculturestudentLearnRecord.studentId)!} (${(chinaculturestudentLearnRecord.studentName)!})</a></td>
                            <td>${(chinaculturestudentLearnRecord.duration)?default("")}</td>
                            <td>${(chinaculturestudentLearnRecord.giftStatus)?default("")}</td>
                            <td>${(chinaculturestudentLearnRecord.ct)?default("")}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
                （以上总数超过100条时只显示最近100条学生购课详情）
            </div>
        </#if>

        <div id="addreplaceClassHour_dialog" class="modal hide fade">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3>更换代替课时</h3>
            </div>
            <div class="modal-body">
                 <dl class="dl-horizontal">
                 <ul class="inline">
                <input type="hidden" id="recordId" readonly/>
                        <dt>代替课时</dt>
                        <dd>
                            <select id="replaceclassHourId">
                        <#if classHourLists?exists>
                            <#list classHourLists as classhour>
                                <#if classhour.canPlayBack!false>
                                <option value="${(classhour.id)!}">
                                    ${(classhour.startTime)!}~ ${(classhour.endTime)!} (${(classhour.teacherName)!})(${(classhour.liveUv)!'0'})
                                </option>
                                </#if>
                            </#list>
                        </#if>
                    </select>
                        </dd>
                     </li>
                 </ul>
                 </dl>
            </div>
            <div class="modal-footer">
                 <button class="btn btn-primary" id="addreplaceClassHour">确定</button>
            </div>
        </div>

            <div id="deleteLearnRecord_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>删除记录</h3>
        </div>
        <div class="modal-body">

            <form class="form-horizontal" enctype="multipart/form-data">

                <div class="control-group">
                    <label class="col-sm-2 control-label">记录id</label>
                    <div class="controls">
                        <input type="text" id="learnrecordId" readonly class="form-control" readonly/>
                        <span class="controls-desc"></span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label">删除原因</label>
                    <div class="controls">
                        <input type="text" id="desc" class="form-control"/>
                    </div>
                    <span class="controls-desc"></span>
                </div>
        </form>
    </div>
    <div class="modal-footer">
        <button id="deleteLearnRecord_btn" class="btn btn-primary">确 定
        </button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
    </span>
    <script>

$(function () {
    $("input[name='addreplaceClassHour']").on('click', function () {
        var recordId = $(this).attr("data-recordId");
        $("#addreplaceClassHour_dialog").modal("show");
        $("#recordId").val(recordId);
    });

    $("input[name='deleteLearnRecord']").on("click", function () {
        $("#deleteLearnRecord_dialog").modal("show");
        var recordId = $(this).attr("data-recordId");
        $("#learnrecordId").val(recordId);
    });
    $('#addreplaceClassHour').on('click', function () {
        var classHourId = $("#replaceclassHourId").val();
        var recordId = $("#recordId").val();
        $.get('/equator/babyeagle/addreplaceclasshour.vpage', {
            replaceClassHourId: classHourId,
            recordId: recordId
        }, function (data) {
            if (data.success) {
                alert("更换成功");
                window.location.reload();
            } else {
                alert(data.info);
            }
        });
    });

    $("#deleteLearnRecord_btn").on("click", function () {
        var recordId = $("#learnrecordId").val();
        var desc = $("#desc").val();
        $.post('/equator/babyeagle/deletelearnrecord.vpage', {
            recordId: recordId,
            desc: desc
        }, function (data) {
            if (data.success) {
                alert("删除成功");
                window.location.reload();
            } else {
                alert(data.info);
            }
        });
    });

});




    </script>
</@layout_default.page>