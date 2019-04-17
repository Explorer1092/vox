<#import "../../layout_default.ftl" as layout_default>
    <@layout_default.page page_title = "小鹰学堂管理" page_num= 24 >
    <link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
    <link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>

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
    <form class="form-horizontal" action="courseinfoindex.vpage" method="get" id="courseKindForm"
          enctype="multipart/form-data">
        <div class="control-group">
            <select id="kindId" name="kindId">
                <option value="">请选择课程信息</option>
                <#if courseKinds?exists>
                    <#list courseKinds as coursekind>
                        <option value="${(coursekind.id)!}"
                                <#if courseKind?? && courseKind.id?default("") == coursekind.id>selected</#if>>
                 ${(coursekind.babyEagleSubject.subjectName) ?default("")}  <#if (coursekind.clazzLevel.description)?default("") == "自定义">
                     全年级 <#else> ${(coursekind.clazzLevel.description) ?default("")} </#if>${(coursekind.babyEagleTerm.termName) ?default("")}
                        </option>
                    </#list>
                </#if>
            </select>
            <#if courseKind??>
                <input id="addCourseInfo" class="btn btn-primary" type="button" value="新增课程信息"
                       onclick="addCourseinfo()">
            </#if>
        </div>

    </form>

    <#if errMsg??>
        <div class="control-group">
                <label class="col-sm-2 control-label"></label>
                <div class="controls">
                ${errMsg}
                </div>
            </div>
    </#if>

    <#if courseInfoList ?? >
        <div class="table_soll">

            <#list courseInfoList as courseinfo>
                <table class="table table-bordered" <#if (courseinfo.isBannerRecommend == true)!false>style="background-color: #d9edf7;"</#if> >
                            <tr>
                            <th width="50%">
                                <#if courseinfo.recommendOrder??&&(courseinfo.recommendOrder == 3)>
                                    <span class="label label-warning">第1广告位</span>
                                </#if>
                                <#if courseinfo.recommendOrder??&&(courseinfo.recommendOrder == 2)>
                                    <span class="label label-warning">第2广告位</span>
                                </#if>
                                <#if courseinfo.recommendOrder??&&(courseinfo.recommendOrder == 1)>
                                    <span class="label label-warning">第3广告位</span>
                                </#if>
                                课程名称
                            </th>
                            <th width="100px">课时数</th>
                            <th width="100px">状态</th>
                            <th>操作</th>
                            </tr>
                        <tbody>
                            <tr data-courseinfoId="${courseinfo.id ?default("")}">
                                <#escape x as x?html>
                                    <td>
                                    <#if courseinfo.iconUrl??&&courseinfo.iconUrl!=""><img src="${courseinfo.iconUrl}" style="width: 200px; height: 80px;"/> </#if>
                                    ${(courseinfo.courseName)!}
                                        <#if courseinfo.courseTypeName??>
                                            <span class="label label-info">${courseinfo.courseTypeName}</span>
                                        </#if>
                                    </td>
                                </#escape>
                                <td><#if courseinfo.classHourLists ?? >${courseinfo.classHourLists?size}</#if></td>
                                <td>
                                    <#if (courseinfo.onlineStatus == true)!false>
                                        <strong style="color:blue">线上</strong>
                                    <#else>
                                        <strong style="color:grey">线下</strong>
                                    </#if>
                                </td>
                                <td>
                                     <#if courseinfo.hasStarted?string('true', 'false') == 'true'>
                                     <#else>
                                         <input type="button" class="btn btn-danger" value="删除"
                                                data-courseId="${(courseinfo.id)!}"
                                                data-courseName="${(courseinfo.courseName)!}"
                                                name="deleteCourseInfo">
                                     </#if>
                                    <input type="button" class="btn btn-default" value="编辑"
                                           data-courseId="${(courseinfo.id)!}"
                                           data-courseName="${(courseinfo.courseName)!}"
                                           data-kindId="${(courseinfo.kindId)!}"
                                           data-status="${(courseinfo.onlineStatus)?c}"
                                           data-intro="${(courseinfo.courseIntro)!}"
                                           data-bannerOrder="${(courseinfo.recommendOrder)!}"
                                           data-imgUrl="${(courseinfo.iconUrl)!}"
                                           data-sinologyType="${(courseinfo.sinologyType)!}"
                                           name="updateCourseInfo">
                                    <#if courseinfo.hasClassHours?string('true', 'false') == 'true'>
                                    <#else>
                                        <input type="button" class="btn btn-default" value="课时设定"
                                               onclick="addClasshourbyModel('${courseinfo.id?default("")}')">
                                    </#if>
                                    <input type="button" class="btn btn-default" value="新增课时"
                                           onclick="addClasshour('${courseinfo.id?default("")}')">
                                </td>
                            </tr>
                            <#if courseinfo.courseIntro??&&courseinfo.courseIntro!="">
                                <tr><td colspan="4">${courseinfo.courseIntro}</td></tr>
                                </#if>

                                <#if courseinfo.classHourLists ?? >
                                    <#list courseinfo.classHourLists as classhour>
                                    <tr data-classhourId="${classhour.id ?default("")}"
                                        data-teacherId="${classhour.teacherId ?default("")}"
                                        data-talkFunCourseId="${classhour.talkFunCourseId ?default("")}">
                                        <td colspan="4">
                                            <#if (classhour.isFinish)!false>
                                                <span class="label label-success">finish(${classhour.liveUv ?default("0")})</span>
                                            <#else>
                                                <a id="NoFinish"></a>
                                                <#if classhour.startTime?date gt .now?date>
                                                    <span class="label label-warning">waiting</span>
                                                <#else>
                                                    <span class="label label-info">doing</span>
                                                </#if>
                                            </#if>
                                            <a href="/equator/babyeagle/studentlearnrecordbyclasshour.vpage?courseId=${(courseinfo.id)!}&classHourId=${(classhour.id)!}"
                                               title="欢拓课程ID：${classhour.talkFunCourseId ?default("")}">
                                            ${classhour.startTime ?default("")}
                                                ~ ${classhour.endTime ?default("")} </a> <span
                                                title="${classhour.teacherTalkFunId ?default("")}"></span><span
        class="label label-primary" title="欢拓主播ID：${classhour.teacherTalkFunId ?default("")}">${classhour.teacherName?default("")}</span>
        <#if classhour.startTime?datetime lt .now?datetime>
        <span class="label <#if (classhour.isExpire)!false>label-warning<#else>label-success</#if>" type="button"
               data-classHourId="${(classhour.id)!}"
               data-isExpire="<#if (classhour.isExpire)!false>false<#else>true</#if>"
               name="updateClassHourStatusBtn" title="点击可更改状态,置为不推荐回放时，如用户之前没有看过回放，将不会优先在回放列表里出现"><#if (classhour.isExpire)!false>不推荐回放<#else>推荐回放</#if></span>
        <#else>
            <input class="btn btn-danger" type="button" value="删除课时"
                   data-id="${(classhour.id)!}" data-startTime="${(classhour.startTime)!}"
                   data-endTime="${(classhour.endTime)!}"
                   data-teacherName="${(classhour.teacherName)!}"
                   name="deleteClassHour">
            <input class="btn btn-danger" type="button" value="编辑课时"
                   data-id="${(classhour.id)!}" data-startTime="${(classhour.startTime)!}"
                   data-endTime="${(classhour.endTime)!}"
                   data-teacherId="${(classhour.teacherId)!}"
                   name="editClassHour">

    </#if>
    </td>
    </tr>
    </#list>
    </#if>

    </tbody>
    </table>
    </#list >
    </div>
    </#if>

    <div id="updatecourseInfo_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>编辑课程信息</h3>
        </div>
        <div class="modal-body">

            <div class="form-horizontal">
                <input type="hidden" id="courseinfoId" name="courseinfoId"/>

                <div class="control-group">
                    <label class="col-sm-2 control-label">课程名称</label>
                    <div class="controls">
                        <input type="text" id="courseName" name="courseName" class="form-control"
                               value="${(courseinfo.courseName)!}"/>
                        <span class="controls-desc"></span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label">课程种类</label>
                    <div class="controls">
                        <input type="hidden" id="courseKind" name="courseKind" class="form-control" readonly/>
                        ${(courseKind.babyEagleSubject.subjectName)?default("")}
                        <#if (courseKind.clazzLevel.description)?default("") == "自定义">
                            全年级
                            <#else> ${(courseKind.clazzLevel.description) ?default("")}
                        </#if>
                        ${(courseKind.babyEagleTerm.termName) ?default("")} </br>
                    </div>
                    <span class="controls-desc"></span>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label">状态</label>
                    <div class="controls">
                        <select id="updateOnlineStatus" name="updateOnlineStatus">
                            <option value="true">线上</option>
                            <option value="false">线下</option>
                        </select>
                        <span class="controls-desc"></span>
                    </div>
                </div>

                <#if courseKind?? && courseKind.babyEagleSubject?? && courseKind.babyEagleSubject.babyEagleType?default("") == 'ChinaCulture' &&
                courseKind.babyEagleSubject.subjectName?default("") == '国学精品课一期' >
                    <div class="control-group">
                        <label class="col-sm-2 control-label">国学课程类型</label>
                        <div class="controls">
                                <select id="sinologyType">
                                    <#if sinologyCourseTypes?exists>
                                        <#list sinologyCourseTypes as sinologyCourseType>
                                            <option value="${(sinologyCourseType)!}">
                                                ${(sinologyCourseType.typeName)!}
                                            </option>
                                        </#list>
                                    </#if>
                                </select>
                            <span class="controls-desc"></span>
                        </div>
                    </div>
                </#if>

                <div class="control-group">
                    <label class="col-sm-2 control-label">首页轮播</label>
                    <div class="controls">
                        <select id="recommend" name="recommend">
                            <option value="0">不显示</option>
                            <option value="1">第3广告位</option>
                            <option value="2">第2广告位</option>
                            <option value="3">第1广告位</option>
                        </select>
                        <span class="controls-desc"></span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label">课程图片</label>
                    <div class="controls">
                        <input type="file" name="file" id="uploadFile"
                               accept="image/gif, image/jpeg, image/png, image/jpg">
                        <button id="upload_confirm" class="btn btn-primary">上传新图片</button>
                        <button class="btn btn-danger" onclick="$('#iconUrl').val('');alert('原有图片地址已清空，请再次点击确定进行保存！')">删除原有图片</button>
                        <input style="display:none;" type="text" id="iconUrl" value="">
                        <span class="controls-desc"></span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label">课程介绍</label>
                    <div class="controls">
                        <textarea id="courseIntro" name="courseIntro" class="form-control"></textarea>
                        <span class="controls-desc"></span>
                    </div>
                </div>
                <span>注意！所有修改均5分钟后生效!<br/>确认后处理会消耗一定时间，请耐心等待，不要重复点击确认提交!</span>
            </div>

        </div>
        <div class="modal-footer">
            <button id="editCourseInfo_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="addcourseInfo_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>新增课程信息</h3>
        </div>
        <div class="modal-body">

            <form class="form-horizontal" action="" method="post" enctype="multipart/form-data">

                <div class="control-group">
                    <label class="col-sm-2 control-label">课程名称</label>
                    <div class="controls">
                        <input type="text" id="name" name="courseName" class="form-control"/>
                        <span class="controls-desc"></span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label">课程种类</label>
                    <div class="controls">
                        <input type="hidden" id="kind" name="courseKind" class="form-control" readonly
                               value="${(courseKind.id)!}"/>
                        ${(courseKind.babyEagleSubject.subjectName) ?default("")}
                        <#if (courseKind.clazzLevel.description)?default("") == "自定义">
                            全年级
                            <#else> ${(courseKind.clazzLevel.description) ?default("")}
                        </#if>
                        ${(courseKind.babyEagleTerm.termName) ?default("")} </br>
                    </div>
                    <span class="controls-desc"></span>
                </div>

            </form>

        </div>
        <div class="modal-footer">
            <button id="addCourseInfo_btn"
                    class="btn btn-primary">确 定
            </button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="addClassHour_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>新增课时</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>课程ID</dt>
                        <dd>
                            <input id="id" name="courseId" type="text" readonly="readonly" value="${(courseId)!}"/>
                        </dd>
                    </li>
                </ul>

                <ul class="inline">
                    <li>
                        <dt>教师id</dt>
                        <dd>
                            <select id="teacher" name="teacherId">
                                <#if teachers?exists>
                                    <#list teachers as teacher>
                                        <option value="${(teacher.id)!}">${teacher.name?default("")}</option>
                                    </#list>
                                </#if>
                            </select>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>开始日期</dt>
                        <dd>
                            <input id="startTime" class="datetimepicker"
                                   type="text" readonly/>
                        </dd>
                    </li>
                </ul>

                <ul class="inline">
                    <li>
                        <dt>结束日期</dt>
                        <dd>
                            <input id="endTime" class="datetimepicker"
                                   type="text" readonly/>
                        </dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="addClassHour_btn"
                    class="btn btn-primary">确 定
            </button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="addClassHourbyModel_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>课时设定</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>课程ID</dt>
                        <dd>
                            <input id="courseId" name="courseId" type="text" readonly="readonly"
                                   value="${(courseId)!}"/>
                        </dd>
                    </li>
                </ul>

                <ul class="inline">
                    <li>
                        <dt>课时模板</dt>
                        <dd>
                            <select id="templetId">
                                <#if classhourTemplets?exists>
                                    <#list classhourTemplets as classhourTemplet>
                                        <option value="${(classhourTemplet.id)!}">
                                            ${classhourTemplet.templetName?default("")}
                                        </option>
                                    </#list>
                                </#if>
                            </select>
                        </dd>
                    </li>
                </ul>

                <ul class="inline">
                    <li>
                        <dt>教师id</dt>
                        <dd>
                            <select id="teacherId" name="teacherId">
                                <#if teachers?exists>
                                    <#list teachers as teacher>
                                        <option value="${(teacher.id)!}">${teacher.name?default("")}</option>
                                    </#list>
                                </#if>
                            </select>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>选择日期</dt>
                        <dd>
                            <input id="date1" name="startTime" class="datepicker"
                                   type="text" readonly/>
                        </dd>
                        <dd>
                            <input id="date2" name="startTime" class="datepicker"
                                   type="text" readonly/>
                        </dd>
                        <dd>
                            <input id="date3" name="startTime" class="datepicker"
                                   type="text" readonly/>
                        </dd>
                        <dd>
                            <input id="date4" name="startTime" class="datepicker"
                                   type="text" readonly/>
                        </dd>
                        <dd>
                            <input id="date5" name="startTime" class="datepicker"
                                   type="text" readonly/>
                        </dd>

                    </li>
                </ul>
                <span>注意！使用模板批量增加课时会消耗一定时间，确认后请耐心等待应答，不要重复点击确认提交！！</span>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="addClassHourbyModel_btn"
                    class="btn btn-primary">确 定
            </button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="deleteCourseinfo_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>删除课程信息</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li id="li1">
                        <dt>课程id</dt>
                        <dd><input id="deleteCourseId" readonly type="text"/></dd>
                    </li>
                    <li id="li2">
                        <dt>课程名称</dt>
                        <dd><input id="deleteCourseName" readonly type="text"/></dd>
                    </li>
                    <li id="li3">
                        <dt>删除原因:</dt>
                        <dd><textarea id="deleteCourseDesc" cols="35" rows="3" placeholder="删除原因不能为空"></textarea></dd>
                    </li>
                </ul>
                <span>注意！该操作的同时当前课程下的所有课时也将一并删除！！</span>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="deleteCourseInfo_dialog_confirm_btn" class="btn btn-primary">确定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="deleteClassHour_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>删除课时</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>课时id</dt>
                        <dd><input type="text" id="deleteClassHourid" readonly/></dd>
                    </li>
                    <li>
                        <dt>开始时间</dt>
                        <dd><input id="deletestartTime" readonly type="text"/></dd>
                    </li>
                    <li>
                        <dt>结束时间</dt>
                        <dd><input type="text" id="deleteendTime" readonly/></dd>
                    </li>
                    <li>
                        <dt>教师姓名</dt>
                        <dd><input type="text" id="teacherName" readonly/></dd>
                    </li>
                    <li>
                        <dt>删除原因:</dt>
                        <dd><textarea id="deleteHourDesc" cols="35" rows="3" placeholder="删除原因不能为空"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="deleteClassHour_dialog_confirm_btn" class="btn btn-primary">确定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="editClassHour_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>编辑课时</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>课时id</dt>
                        <dd><input type="text" id="editClassHourid" readonly/></dd>
                    </li>
                    <li>
                        <dt>开始时间</dt>
                        <dd><input id="editstartTime" readonly type="text"/></dd>
                    </li>
                    <li>
                        <dt>结束时间</dt>
                        <dd><input type="text" id="editendTime" readonly/></dd>
                    </li>
                    <ul class="inline">
                        <li>
                            <dt>教师id</dt>
                            <dd>
                                <select id="editteacherId">
                                    <#if teachers?exists>
                                        <#list teachers as teacher>
                                            <option value="${(teacher.id)!}">${teacher.name?default("")}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </dd>
                        </li>
                    </ul>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="editClassHour_btn" class="btn btn-primary">确定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    </span>
<script>
    if(window.location.hash == "")
        window.location.href = window.location.href + "#NoFinish";

    $(function () {
    var text = $('#kindId option:selected').text();

    $('#kindId').on('change', function () {
        $("#courseKindForm").submit();
    });

    $('#addCourseInfo_btn').on('click', function () {
        var courseName = $("#name").val();
        var kindId = $("#kind").val();
        var onlineStatus = new Boolean(false);

        $.get('/equator/babyeagle/addcourseinfo.vpage', {
            courseName: courseName,
            onlineStatus: onlineStatus,
            kindId: kindId
        }, function (data) {
            if (data.success) {
                alert("添加成功");
                window.location.reload();
            } else {
                alert(data.info);
            }
        });
    });

    $("input[name='updateCourseInfo']").on('click', function () {
        $("#updatecourseInfo_dialog").modal("show");
        var id = $(this).attr("data-courseId");
        var courseName = $(this).attr("data-courseName");
        var kindId = $(this).attr("data-kindId");
        var status = $(this).attr("data-status");
        var recommend=$(this).attr("data-bannerOrder");
        var intro=$(this).attr("data-intro");
        var iconUrl=$(this).attr("data-imgUrl");
        var sinologyType=$(this).attr("data-sinologyType");
        $("#courseinfoId").val(id);
        $("#courseName").val(courseName);
        $("#courseKind").val(kindId);
        $("#updateOnlineStatus").val(status);
        $("#recommend").val(recommend);
        $("#courseIntro").val(intro);
        $("#iconUrl").val(iconUrl);
        $("#sinologyType").val(sinologyType);
    });

    $("input[name='deleteCourseInfo']").on('click', function () {
        var id = $(this).attr("data-courseId");
        var name = $(this).attr("data-courseName");
        $('#deleteCourseinfo_dialog').modal("show");
        $('#deleteCourseId').val(id);
        $('#deleteCourseName').val(name);
    });

    $("input[name='deleteClassHour']").on("click", function () {
        var id = $(this).attr("data-id");
        var startTime = $(this).attr("data-startTime");
        var endTime = $(this).attr("data-endTime");
        var teacherName = $(this).attr("data-teacherName");
        $('#deleteClassHour_dialog').modal("show");
        $('#deletestartTime').val(startTime);
        $('#deleteendTime').val(endTime);
        $('#teacherName').val(teacherName);
        $('#deleteClassHourid').val(id);
    });

    $("input[name='editClassHour']").on("click", function () {
        var id = $(this).attr("data-id");
        var startTime = $(this).attr("data-startTime");
        var endTime = $(this).attr("data-endTime");
        var teacherId = $(this).attr("data-teacherId");
        $('#editClassHour_dialog').modal("show");
        $('#editteacherId').val(teacherId);
        $('#editClassHourid').val(id);
        $('#editstartTime').val(startTime);
        $('#editendTime').val(endTime);
    });

    $('#editCourseInfo_btn').on('click', function () {
        var courseinfoId = $('#courseinfoId').val();
        var courseKind = $('#courseKind').val();
        var updateOnlineStatus = $('#updateOnlineStatus').val();
        var courseName=$('#courseName').val();
        var courseIntro = $('#courseIntro').val();
        var recommend = $('#recommend').val();
        var iconUrl=$('#iconUrl').val();
        var sinologyType=$('#sinologyType').val();
        $.post('/equator/babyeagle/updatecourseinfo.vpage', {
             courseinfoId :courseinfoId,
             courseKind:courseKind,
             updateOnlineStatus:updateOnlineStatus,
             courseName:courseName,
             courseIntro:courseIntro,
             recommend:recommend,
             iconUrl:iconUrl,
             sinologyType:sinologyType
        }, function (data) {
            if (data.success) {
                alert("编辑成功");
                window.location.reload();
            } else {
                alert(data.info);
            }
        });
    });

     $('#upload_confirm').on('click', function () {
            // 获取参数
            var path = "wonderland";
            // 拼formData
            var formData = new FormData();
            var file = $('#uploadFile')[0].files[0];
            if(file == undefined){
                alert("请先选择文件");
                return;
            }
             if((file.size / 1024) > 150){
                 alert("文件超过150k，这么大的图片压缩一下吧，用户忍受不了加载的等待！压缩网站推荐：https://tinypng.com/");
                 return;
             }
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);
            formData.append('path', path);
            // 发起请求
             $.ajax({
                url: '/equator/babyeagle/uploadphoto.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                async:true,
                success: function (res) {
                    if (res.success) {
                        $("#iconUrl").val(res.info);
                        alert("图片上传成功，请再次点击确定进行保存！");
                    } else {
                        alert(res.info);
                    }
                }
            });
        });

    $("#deleteCourseInfo_dialog_confirm_btn").on("click", function () {
        var deletedesc = $('#deleteCourseDesc').val();
        var id = $('#deleteCourseId').val();
        $.get('/equator/babyeagle/deletecourseinfo.vpage', {
            deletedesc: deletedesc,
            courseinfoId: id
        }, function (data) {
            if (data.success) {
                alert("删除成功");
                window.location.reload();
            } else {
                alert(data.info);
            }
        });
    });


    $("#addClassHourbyModel_btn").on("click", function () {
        var teacherId = $("#teacherId").val();
        var templetId = $("#templetId").val();
        var courseId = $("#courseId").val();
        var date1 = $("#date1").val();
        var date2 = $("#date2").val();
        var date3 = $("#date3").val();
        var date4 = $("#date4").val();
        var date5 = $("#date5").val();
        var dates = new Array();
        dates[0] = date1;
        dates[1] = date2;
        dates[2] = date3;
        dates[3] = date4;
        dates[4] = date5;
        $.get('/equator/babyeagle/addclasshourbymodel.vpage', {
            templetId: templetId,
            courseId: courseId,
            teacherId: teacherId,
            date1: date1,
            date2: date2,
            date3: date3,
            date4: date4,
            date5: date5
        }, function (data) {
            alert(data.info);
            window.location.reload();

        });
    });

    $("#addClassHour_btn").on("click", function () {
        var teacherId = $("#teacher").val();
        var courseId = $("#id").val();
        var startTime = $("#startTime").val();
        var endTime = $("#endTime").val();
        $.get('/equator/babyeagle/addclasshour.vpage', {
            courseId: courseId,
            teacherId: teacherId,
            startTime: startTime,
            endTime: endTime
        }, function (data) {
            if (data.success) {
                alert("新增课时成功");
                window.location.reload();
            } else {
                alert(data.info);
            }
        });
    });

    $("#deleteClassHour_dialog_confirm_btn").on("click", function () {
        var deletedesc = $('#deleteHourDesc').val();
        var id = $('#deleteClassHourid').val();
        $.get('/equator/babyeagle/deleteclasshour.vpage', {
            deletedesc: deletedesc,
            classhourId: id
        }, function (data) {
            if (data.success) {
                alert("删除成功");
                window.location.reload();
            } else {
                alert(data.info);
            }
        });
    });

    $("#editClassHour_btn").on("click", function () {
        var teacherId = $("#editteacherId").val();
        var classhourId = $("#editClassHourid").val();
        var startTime = $("#editstartTime").val();
        var endTime = $("#editendTime").val();
        $.get('/equator/babyeagle/editclasshour.vpage', {
            updateType: "",
            classhourId: classhourId,
            teacherId: teacherId,
            startTime: startTime,
            endTime: endTime
        }, function (data) {
            if (data.success) {
                alert("编辑课时成功");
                window.location.reload();
            } else {
                alert(data.info);
            }
        });
    });

    $("span[name='updateClassHourStatusBtn']").on("click", function () {
        var classHourId = $(this).attr("data-classHourId");
        var isExpire = $(this).attr("data-isExpire");
        $.get('/equator/babyeagle/editclasshour.vpage', {
            updateType: "local",
            classhourId: classHourId,
            isExpire: isExpire
        }, function (data) {
            if (data.success) {
                alert("状态更新成功");
                window.location.reload();
            } else {
                alert(data.info);
            }
        });
    });

    $('.datetimepicker').datetimepicker(
        {
            format: 'yyyy/mm/dd hh:ii',
            useCurrent: true,
            autoclose: true
        }
    );

    $('.datepicker').datetimepicker(
        {
            format: 'yyyy-mm-dd',
            minView: "month"
        }
    );

    });

    function addCourseinfo() {
        $("#addcourseInfo_dialog").modal("show");
    }

    function addClasshourbyModel(id) {
        $("#addClassHourbyModel_dialog").modal("show");
        $("#courseId").val(id);
    }

    function addClasshour(id) {
        $("#addClassHour_dialog").modal("show");
        $("#id").val(id);
    }
</script>
</@layout_default.page>