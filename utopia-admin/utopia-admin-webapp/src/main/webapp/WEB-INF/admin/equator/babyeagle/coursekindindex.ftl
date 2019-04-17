<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="小鹰学堂课程种类管理" page_num=24>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<span class="span9">
    <div id="legend" class="">
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation"><a href="studentlearninfo.vpage?studentId=${studentId!''}">学生课程管理</a></li>
            <li role="presentation"><a href="courseinfoindex.vpage">课程内容管理</a></li>
            <li role="presentation" class="active"><a href="coursekindindex.vpage">课程种类管理</a></li>
            <li role="presentation"><a href="teacherindex.vpage">教师管理</a></li>
            <li role="presentation"><a href="classhourmodel.vpage">单日课时模板</a></li>
        </ul>
    </div>
    (新增课程种类暂不支持添加，请联系管理员)
    <div class="table_soll">
        <table class="table table-bordered">
            <tr>
                <th>年级</th>
                <th>学科</th>
                <th>学期</th>
                <th style="diSplay:none">模式</th>
                <th>状态</th>
                <th>操作(修改5分钟后生效）</th>
            </tr>
            <tbody id="tbody">
                <#if courseKindList ?? >
                    <#list courseKindList as coursekind>
                    <tr>
                        <td>
                            <#if (coursekind.clazzLevel.description)?default("") == "自定义">
                                全年级
                            <#else>
                                ${(coursekind.clazzLevel.description)?default("")}
                            </#if>
                        </td>
                        <td>${(coursekind.babyEagleSubject.subjectName)?default("")}</td>
                        <td>${(coursekind.babyEagleTerm.termName)?default("")}</td>
                        <td style="diSplay:none">${(coursekind.mode)?default("")}</td>
                        <td>
                            <#if (coursekind.onlineStatus == true)!false>
                                <strong style="color:blue">线上</strong>
                            <#else>
                                <strong style="color:grey">线下</strong>
                            </#if>
                        </td>
                        <td>
                            <#if (coursekind.onlineStatus == true)!false>
                                <input type="button" value="下线" class="btn btn-danger"
                                       onclick="modifyOnlineStatus('${coursekind.id}','${coursekind.babyEagleSubject}','${coursekind.clazzLevel}','${coursekind.babyEagleTerm}',false)">
                            <#else>
                                <input type="button" value="上线" class="btn btn-default"
                                       onclick="modifyOnlineStatus('${coursekind.id}','${coursekind.babyEagleSubject}','${coursekind.clazzLevel}','${coursekind.babyEagleTerm}',true)">
                            </#if>
                        </td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>

    <div id="addupdatecourseKind_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>编辑课程信息</h3>
        </div>
        <div class="modal-body">
            <form class="form-horizontal" id="coursekindForm" enctype="multipart/form-data">
                <input type="hidden" id="id" name="id" value="<#if coursekind??>${coursekind.id!}</#if>"/>
                <div class="control-group">
                    <label class="control-label">学科</label>
                    <div class="controls">
                        <select id="subject" name="subject">
                            <#if babyeagleSubject??>
                                <#list babyeagleSubject as subject>
                                    <option value="${(subject)!}"
                                            <#if coursekind?? && coursekind.babyEagleSubject?default("") == subject>selected</#if>>
                                    ${subject.subjectName?default("")}
                                 </option>
                                </#list>
                            </#if>
                        </select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">年级</label>
                    <div class="controls">
                        <select id="clazzLevel" name="clazzLevel">
                            <#if classLevels??>
                                <#list classLevels as classLevel>
                                    <option value="${(classLevel)!}"
                                            <#if coursekind?? && coursekind.clazzLevel?default("") == classLevel>selected</#if>>
                                        <#if classLevel.description?default("") == "自定义">
                                            全年级
                                        <#else>
                                        ${classLevel.description?default("")}
                                        </#if>
                                    </option>
                                </#list>
                            </#if>
                        </select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">学期</label>
                    <div class="controls">
                        <select id="term" name="term">
                            <#if babyeagleTermList??>
                                <#list babyeagleTermList as babyeagleTerm>
                                    <option value="${(babyeagleTerm)!}"
                                            <#if coursekind?? && coursekind.babyEagleTerm?default("") == babyeagleTerm>selected</#if>>
                                    ${babyeagleTerm.termName?default("")}
                                 </option>
                                </#list>
                            </#if>
                        </select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label">运行环境</label>
                    <div class="controls">
                        <select id="mode" name="mode">
                            <#if runModeList?exists>
                                <#list runModeList as runMode>
                                    <option value="${(runMode)!}"
                                            <#if coursekind?? && coursekind.mode?default("") == runMode>selected</#if>>
                                    ${runMode}
                            </option>
                                </#list>
                            </#if>
                        </select>
                        <span class="controls-desc"></span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">状态</label>
                    <div class="controls">
                        <select id="onlineStatus" name="onlineStatus">
                            <option value="true">线上</option>
                            <option value="false" selected>线下</option>
                        </select>
                        <span class="controls-desc"></span>
                    </div>
                </div>

            </form>
        </div>
        <div class="modal-footer">
            <button id="saveCoursekind" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
</span>
<script>
//后台添加接口
function addCoursekind(subject , clazzLevel , term ,  onlineStatus ) {
    $.get('/equator/babyeagle/addcoursekind.vpage', {
        subject: subject, clazzLevel: clazzLevel, term: term, onlineStatus:onlineStatus
    }, function (data) {
        if (data.success) {
            alert("添加成功");
        } else {
            alert(data.info);
        }
    });
}

function editCoursekind(id, subject, classlevel, term, mode) {
    $("#addupdatecourseKind_dialog").modal("show");
    $("#id").val(id);
    $("#subject").val(subject);
    $("#clazzLevel").val(classlevel);
    $("#term").val(term);
    $("#mode").val(mode);

    $('#saveCoursekind').on('click', function () {
        var id = $("#id").val();
        var subject = $("#subject").val();
        var clazzLevel = $("#clazzLevel").val();
        var term = $("#term").val();
        var onlineStatus = $("#onlineStatus").val();
        if (id != null) {
            $.get('/equator/babyeagle/updatecoursekind.vpage', {
                id:id,
                subject:subject,
                clazzLevel:clazzLevel,
                term:term,
                onlineStatus:onlineStatus
            }, function (data) {
                if (data.success) {
                    alert("编辑成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        }
    });
}

function modifyOnlineStatus(id, subject, clazzLevel, term, onlineStatus) {
    $.get('/equator/babyeagle/updatecoursekind.vpage', {
        id:id,
        subject:subject,
        clazzLevel:clazzLevel,
        term:term,
        onlineStatus:onlineStatus
    }, function (data) {
        if (data.success) {
            alert("编辑成功");
            window.location.reload();
        } else {
            alert(data.info);
        }
    });
}
</script>
</@layout_default.page>