<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="小鹰学堂学生管理" page_num=24>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<span class="span9">
    <div>
        <#if error??>
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>${error!}</strong>
            </div>
        </#if>
        <#if success??>
            <div class="alert alert-success">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>${success!}</strong>
            </div>
        </#if>
    </div>
    <div id="legend" class="">
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active"><a href="studentlearninfo.vpage?studentId=${studentId!''}">学生课程管理</a></li>
            <li role="presentation"><a href="courseinfoindex.vpage">课程内容管理</a></li>
            <li role="presentation"><a href="coursekindindex.vpage">课程种类管理</a></li>
            <li role="presentation"><a href="teacherindex.vpage">教师管理</a></li>
            <li role="presentation"><a href="classhourmodel.vpage">单日课时模板</a></li>
        </ul>
    </div>

     <form class="form-horizontal" action="/equator/babyeagle/studentlearninfo.vpage" method="get"
           id="babyeagleLearnQueryForm">
            <ul class="inline">
                学生ID：<input type="text" id="studentId" name="studentId" value="${studentId!''}" autofocus="autofocus"
                            placeholder="请输入学生ID"/>
                <input type="button" class="btn btn-default" id="submit_query" name="submit_query" value="查询"/>
                <#if studentId??><input type="button" class="btn btn-default" id="send_chinaculture_push"
                                        name="send_chinaculture_push" value="发送消息"/></#if>
            </ul>
        </form>

    <#if studentId??>

        <div>
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>学生姓名</dt>
                    <dd><input type="text" value="${(studentName)!''}" readonly/></dd></li>
                <li>
                    <dt>听课卡数量<input type="button" class="btn btn-default" id="addtimesCard" value="+"
                                    onclick="addtimesCard('${studentId!''}')"/></dt>
                    <dd><input type="text" value="${(timesCardCount)!'0'}" readonly/></dd></li>
                <li>
                    <dt>体验券数量</dt>
                    <dd><input type="text" value="${(sinologyCardCount)!'0'}" readonly/></li>
                <li>
                    <dt>获得的总星星数</dt>
                    <dd><input type="text" value="<#if learnInfo?exists>${(learnInfo.starTotal)!'0'}</#if>"
                               readonly/></dd></li>
                <li>
                    <dt>被点赞总次数</dt>
                    <dd><input type="text" value="<#if learnInfo?exists>${(learnInfo.goodTotal)!'0'}</#if>"
                               readonly/></dd></li>
                <li>
                    <dt>国学精品课</dt>
                    <dd><#if isSinologyVip!false><span class="label label-warning">会员</span><#else><span class="label label-info">非会员</span></#if></dd></li>
            </ul>
        </dl>
        </div>

        <#if learnCourseRecords ?? && learnCourseRecords?size gt 0 >
            <div class="table_soll">
            <span class="label label-warning">基本课程</span>
            <table class="table table-bordered">
                <tr>
                    <th width="140px">课程种类</th>
                    <th width="200px">课程名称</th>
                    <th width="160px">课时信息</th>
                    <th width="160px">代替课时</th>
                    <th>直播</th>
                    <th>观看时长（s）</th>
                    <th>星星数</th>
                    <th>礼包状态</th>
                    <th width="160px">创建时间</th>
                    <th width="120px">操作</th>
                </tr>

                <tbody id="tbody">
                    <#list learnCourseRecords as learnCourseRecord>
                    <tr data-classhourId="${(learnCourseRecord.courseInfo.id)?default("")}">
                            <td>${(learnCourseRecord.courseKind.babyEagleSubject.subjectName)?default("")}  <#if (learnCourseRecord.courseKind.clazzLevel.description)?default("") == "自定义">
                                全年级 <#else> ${(learnCourseRecord.courseKind.clazzLevel.description) ?default("")} </#if> ${(learnCourseRecord.courseKind.babyEagleTerm.termName)?default("")}</td>
                        <#escape x as x?html>
                            <td>${(learnCourseRecord.courseInfo.courseName)?default("")}</td></#escape>
                            <td><a href="/equator/babyeagle/studentlearnrecordbyclasshour.vpage?courseId=${(learnCourseRecord.courseInfo.id)!}&classHourId=${(learnCourseRecord.classHourId)!}">
                            ${(learnCourseRecord.classHourId)?default("")}</a></td>
                            <td><#if learnCourseRecord.replaceClassHourId??><a
                                    href="/equator/babyeagle/studentlearnrecordbyclasshour.vpage?courseId=${(learnCourseRecord.courseInfo.id)!}&classHourId=${(learnCourseRecord.replaceClassHourId)!}">
                            ${(learnCourseRecord.replaceClassHourId)?default("")}</a></#if></td>
                            <td>${(learnCourseRecord.isLive)?string('是', '否')}</td>
                            <td>${(learnCourseRecord.duration)?default("")}</td>
                            <td>${(learnCourseRecord.starNum)?default("")}</td>
                            <td>${(learnCourseRecord.giftStatus.statusName)?default("")}</td>
                            <td>${(learnCourseRecord.ct)?default("")}</td>
                            <td><input class="btn btn-danger" type="button" value="删除"
                                       name="deleteLearnRecord" data-recordId="${(learnCourseRecord.id)?default("")}" data-recordType="base"
                                       data-courseName="${(learnCourseRecord.courseInfo.courseName)?default("")}">
                             <input class="btn btn-default" type="button" value="修改"
                                    name="editLearnRecord" data-recordId="${(learnCourseRecord.id)?default("")}"
                                    data-courseName="${(learnCourseRecord.courseInfo.courseName)?default("")}"
                                    data-isLive="${(learnCourseRecord.isLive)?string('是', '否')}"
                                    data-giftStatus="${(learnCourseRecord.giftStatus)?default("")}"
                                    data-duration="${(learnCourseRecord.duration)?default("")}"
                                    data-starNum="${(learnCourseRecord.starNum)?default("")}">
                            </td>
                        </tr>
                    </#list>

                </tbody>
            </table>
        </div>
        </#if>

        <#if ChinaCulturelearnCourseRecords?? && ChinaCulturelearnCourseRecords?size gt 0 >
            <div class="table_soll">
                 <span class="label label-warning">国学堂课程</span>
                 <table class="table table-bordered">
                    <tr>
                        <th width="250px">课程种类</th>
                        <th width="250px">课程名称</th>
                        <th width="250px">课时信息</th>
                        <th>礼包状态</th>
                        <th width="160px">创建时间</th>
                        <th width="120px">操作</th>
                    </tr>

                    <tbody>
                        <#list ChinaCulturelearnCourseRecords as ChinaCulturelearnCourseRecord>
                        <tr data-classhourId="${(ChinaCulturelearnCourseRecord.id)?default("")}">
                            <td>${(ChinaCulturelearnCourseRecord.courseKind.babyEagleSubject.subjectName)?default("")}  <#if (ChinaCulturelearnCourseRecord.courseKind.clazzLevel.description)?default("") == "自定义">
                                全年级 <#else> ${(ChinaCulturelearnCourseRecord.courseKind.clazzLevel.description) ?default("")} </#if> ${(ChinaCulturelearnCourseRecord.courseKind.babyEagleTerm.termName)?default("")}</td>
                            <#escape x as x?html>
                                <td>${(ChinaCulturelearnCourseRecord.courseInfo.courseName)?default("")}</td></#escape>
                            <td><a href="/equator/babyeagle/studentlearnrecordbyclasshour.vpage?courseId=${(ChinaCulturelearnCourseRecord.courseInfo.id)!}&classHourId=${(ChinaCulturelearnCourseRecord.classHourId)!}">
                            ${(ChinaCulturelearnCourseRecord.classHourId)?default("")}</a></td>
                            <td>${(ChinaCulturelearnCourseRecord.giftStatus.statusName)?default("")}</td>
                            <td>${(ChinaCulturelearnCourseRecord.ct)?default("")}</td>
                            <td><input class="btn btn-danger" type="button" value="删除"
                                       name="deleteLearnRecord" data-recordId="${(ChinaCulturelearnCourseRecord.id)?default("")}" data-recordType="culture"
                                       data-courseName="${(ChinaCulturelearnCourseRecord.courseInfo.courseName)?default("")}">
                            </td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </#if>
    </#if>

    <div id="addTimesCard_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>增加/扣除听课卡</h3>
        </div>
        <div class="modal-body">

            <form class="form-horizontal" enctype="multipart/form-data">

                <div class="control-group">
                    <label class="col-sm-2 control-label">学生id</label>
                    <div class="controls">
                        <input type="text" id="id" name="id" class="form-control" readonly/>
                        <span class="controls-desc"></span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label">变动的数量</label>
                    <div class="controls">
                        <input type="text" id="addNum" name="addNum" class="form-control"/><br/>(正数为增加，负数为扣除)
                    </div>
                    <span class="controls-desc"></span>
                </div>
        </form>
    </div>
    <div class="modal-footer">
        <button id="addTimesCard_btn" class="btn btn-primary">确 定
        </button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
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
                        <input type="hidden" id="recordType" value="" />
                        <input type="text" id="recordId" readonly class="form-control" readonly/>
                        <span class="controls-desc"></span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label">课程名称</label>
                    <div class="controls">
                        <input type="text" id="courseName" readonly class="form-control"/>
                    </div>
                    <span class="controls-desc"></span>
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

    <div id="editLearnRecord_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>修改记录</h3>
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
                    <label class="col-sm-2 control-label">课程名称</label>
                    <div class="controls">
                        <input type="text" id="learncourseName" readonly class="form-control"/>
                    </div>
                    <span class="controls-desc"></span>
                </div>

                 <div class="control-group">
                    <label class="col-sm-2 control-label">观看时长</label>
                    <div class="controls">
                        <input type="text" id="duration" class="form-control"/>
                    </div>
                    <span class="controls-desc"></span>
                </div>

                 <div class="control-group">
                    <label class="col-sm-2 control-label">星星数量</label>
                    <div class="controls">
                        <input type="text" id="starNum" class="form-control"/>
                    </div>
                    <span class="controls-desc"></span>
                </div>

                  <div class="control-group">
                    <label class="col-sm-2 control-label">礼包状态</label>
                    <div class="controls">
                        <select id="giftStatus">
                            <#if giftStatusList?exists>
                                <#list giftStatusList as status>
                                    <option value="${status}">
                                    ${status.statusName}
                        </option>
                                </#list>
                            </#if>
                 </select>
                    </div>
                    <span class="controls-desc"></span>
                </div>
        </form>
    </div>
    <div class="modal-footer">
        <button id="editLearnRecord_btn" class="btn btn-primary">确 定
        </button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

    <div id="snedPush_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>发送消息</h3>
        </div>
        <div class="modal-body">

            <form class="form-horizontal" enctype="multipart/form-data">

                <div class="control-group">
                    <label class="col-sm-2 control-label">学生id</label>
                    <div class="controls">
                        <input type="text" id="push_student_id" name="push_student_id" class="form-control"
                               value="${studentId!''}"/>
                        <span class="controls-desc"></span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label">题目</label>
                    <div class="controls">
                        <input type="text" id="push_title" name="push_title" class="form-control" value="小鹰国学堂"/>
                    </div>
                    <span class="controls-desc"></span>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">内容</label>
                    <div class="controls">
                        <input type="text" id="push_content" name="push_content" class="form-control"
                               value="购买小鹰国学堂精品课程"/>
                    </div>
                    <span class="controls-desc"></span>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">链接</label>
                    <div class="controls">
                        <input type="text" id="push_url" name="push_url" class="form-control"
                               value="/view/mobile/student/sinology/buy.vpage?canPay=true"/>
                    </div>
                    <span class="controls-desc"></span>
                </div>
        </form>
    </div>
    <div class="modal-footer">
        <button id="sendPush_btn" class="btn btn-primary">确 定
        </button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
</span>
<script>
    $(function () {
        $("#submit_query").on("click", function () {
            $("#babyeagleLearnQueryForm").submit();
        });
    });

    $("input[name='deleteLearnRecord']").on("click", function () {
        $("#deleteLearnRecord_dialog").modal("show");
        var recordType = $(this).attr("data-recordType");
        var courseName = $(this).attr("data-courseName");
        var recordId = $(this).attr("data-recordId");
        $("#recordType").val(recordType);
        $("#courseName").val(courseName);
        $("#recordId").val(recordId);
    });

    $("input[name='editLearnRecord']").on("click", function () {
        $("#editLearnRecord_dialog").modal("show");
        var courseName = $(this).attr("data-courseName");
        var recordId = $(this).attr("data-recordId");
        var isLive = $(this).attr("data-isLive");
        var duration = $(this).attr("data-duration");
        var starNum = $(this).attr("data-starNum");
        var LiveView = "";
        if (isLive == "是") {
            LiveView = new Boolean(true);
        }
        else {
            LiveView = new Boolean(false);
        }
        var giftStatus = $(this).attr("data-giftStatus");
        $("#learncourseName").val(courseName);
        $("#learnrecordId").val(recordId);
        $("#isLive").val(LiveView);
        $("#giftStatus").val(giftStatus);
        $("#duration").val(duration);
        $("#starNum").val(starNum);
    });

    $("#addTimesCard_btn").on("click", function () {
        var studentId = $("#id").val();
        var addtimesCard = $("#addNum").val();
        if (isNaN(addtimesCard) || addtimesCard == 0) {
            alert("请输入准确数字!");
            return;
        }
        var requestUrl = "/equator/babyeagle/addtimesCard.vpage";
        if(addtimesCard < 0){
            requestUrl = "/equator/babyeagle/deductiontimescard.vpage";
        }
        $.post(requestUrl, {
            studentId: studentId,
            addtimesCard: addtimesCard
        }, function (data) {
            if (data.success) {
                alert(data.info);
                window.location.reload();
            } else {
                alert(data.info);
            }
        });
    });

    $("#deleteLearnRecord_btn").on("click", function () {
        var recordType = $("#recordType").val();
        var recordId = $("#recordId").val();
        var desc = $("#desc").val();
        $.post('/equator/babyeagle/deletelearnrecord.vpage', {
            type : recordType,
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

    $("#editLearnRecord_btn").on("click", function () {
        var recordId = $("#learnrecordId").val();
        var isLive = $("#isLive").val();
        var duration = $("#duration").val();
        var starNum = $("#starNum").val();
        var giftStatus = $("#giftStatus").val();
        $.get('/equator/babyeagle/editlearnrecord.vpage', {
            recordId: recordId,
            isLive: isLive,
            duration: duration,
            starNum: starNum,
            giftStatus: giftStatus
        }, function (data) {
            if (data.success) {
                alert("修改成功");
                window.location.reload();
            } else {
                alert(data.info);
            }
        });
    });

    $("#sendPush_btn").on("click", function () {
        var push_student_id = $("#push_student_id").val();
        var push_title = $("#push_title").val();
        var push_content = $("#push_content").val();
        var push_url = $("#push_url").val();
        $.get('/equator/babyeagle/addcourseinfo.vpage', {
            studentId: push_student_id,
            isPush: true,
            title: push_title,
            content: push_content,
            url: push_url
        }, function (data) {
            if (data.success) {
                alert("发送成功！");
                $("#snedPush_dialog").modal("hide");
            } else {
                alert(data.info);
            }
        });
    });

    $("#send_chinaculture_push").on("click", function () {
        $("#snedPush_dialog").modal("show");
    });

    function addtimesCard(studentId) {
        $("#addTimesCard_dialog").modal("show");
        $("#id").val(studentId);
    }
</script>
</@layout_default.page>