<#import "../../layout_default.ftl" as layout_default>
<#import "../headsearch.ftl" as headsearch>
<#if resultMap??&& resultMap.groupClazzInfo?has_content>
    <#assign groupTitle = "${(resultMap.groupClazzInfo.titleName)!'组的详情'}(${resultMap.groupClazzInfo.clazzId!})"/>
</#if>
<@layout_default.page page_title="${groupTitle!'组的详情'}" page_num=3>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap.min.js"></script>
<div class="span9">
    <@headsearch.headSearch/>
    <div>
        <fieldset>
            <legend>组所在班级信息</legend>
        </fieldset>
        <table class="table table-hover table-striped table-bordered">
            <tr>
                <th>班级编号</th>
                <th>年级班级</th>
                <th>班级类型</th>
                <th>修改年制</th>
                <th>学校 ID</th>
                <th>学校名称</th>
            </tr>
            <tr>
                <#if resultMap??&& resultMap.groupClazzInfo?has_content>
                    <td>
                        <a target="_blank" href="/crm/clazz/groupinfo.vpage?clazzId=${resultMap.groupClazzInfo.clazzId!}">${(resultMap.groupClazzInfo.clazzId)!?html}</a>
                    </td>
                    <td>
                        <a target="_blank" href="/crm/clazz/groupinfo.vpage?clazzId=${resultMap.groupClazzInfo.clazzId!}">${(resultMap.groupClazzInfo.clazzName?html)!''}</a>&nbsp&nbsp
                        <button class="btn btn-danger" onclick="changeClazzName(${resultMap.groupClazzInfo.clazzId!})">修改班名</button>
                    </td>
                    <td>
                        <#if clazzType == 'WALKING'> 教学班 <#else> 行政班 </#if>
                    </td>
                    <td>
                        <#if resultMap.groupClazzInfo.schoolLevel != 5>
                            <button class="btn btn-danger" onclick="changeClazzEduSystem(${resultMap.groupClazzInfo.clazzId!})">修改年制</button>
                        </#if>
                    </td>
                    <td>${(resultMap.groupClazzInfo.schoolId)!?html}</td>
                    <td>
                        <a href="../school/schoolhomepage.vpage?schoolId=${(resultMap.groupClazzInfo.schoolId?html)!''}">${(resultMap.groupClazzInfo.schoolName?html)!''}</a>
                    </td>
                </#if>
            </tr>
        </table>
    </div>
    <#if resultMap??&&resultMap.groupInfoList?has_content>
        <table class="table table-hover table-striped table-bordered" style="display: none;">
            <#list resultMap.groupInfoList as groupInfo>
                <tr>
                    <td>组ID :  ${groupInfo.groupId!}</td>
                    <td>User :  <#if (groupInfo.studentList)?has_content>${(groupInfo.studentList)?size}</#if></td>
                    <td>Klx  :  <#if (groupInfo.klxStudentList)?has_content> ${(groupInfo.klxStudentList)?size}</#if></td>
                </tr>
            </#list>
        </table>
    </#if>
    <legend>组的详情</legend>
    <#if resultMap??&&resultMap.groupInfoList?has_content>
        <#list resultMap.groupInfoList as groupInfo>
            <legend style="border:0;border-top: 1px solid #A21616;">组ID(${groupInfo.groupId!} / <#if groupInfo.groupType=='WALKING_GROUP'>教学<#else>行政</#if>)
                <button class="btn btn-success" onclick="transferGroupToClazz(${groupInfo.groupId!})">转移</button>
                <button class="btn btn-info" onclick="mergeOtoGroupStudent(${groupInfo.groupId!})">合并OTO班组学生</button>
                <button class="btn btn-warning" onclick="syncOTOGroupStudent(${groupInfo.groupId!})">同步学生名单</button>
                <button class="btn btn-danger" onclick="syncShareGroupStudent(${groupInfo.groupId!})">同步共享组OTO学生</button>

                <#if groupInfo.groupType != 'WALKING_GROUP'>
                    <a target="_blank" class="btn btn-info" href="/crm/shensz/multistudentinfo.vpage?groupId=${groupInfo.groupId!}">多账号合并处理</a>
                </#if>
            </legend>
            <legend>老师列表&nbsp;
                <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                    <button id="change_clazz_level" changeGroupId="${groupInfo.groupId!}" class="btn change_clazz_level">修改年级</button>
                    <#if  seniorSchool?has_content && seniorSchool == true>
                        <#if groupInfo?has_content && (groupInfo.artScienceType)?has_content >
                            <#if groupInfo.artScienceType == 'ARTSCIENCE'>
                                <span>不分文理</span>
                            <#elseif groupInfo.artScienceType == 'SCIENCE' >
                                <span>理科</span>
                            <#elseif groupInfo.artScienceType == 'ART' >
                                <span>文科</span>
                            <#else>
                                <span>未知</span>
                            </#if>
                        </#if>
                        <button id="change_clazz_group_artsciencetype" changeGroupId="${groupInfo.groupId!}" class="btn change_clazz_group_artsciencetype">修改文理科</button>
                    </#if>
                </#if >
            </legend>

            <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                <ul class="inline">
                    <li>
                        <a class="btn" href="changeteacherhistory.vpage?groupId=${(groupInfo.groupId)!?html}">任课历史</a>
                    </li>
                    <li>
                        <button class="btn" id="addTeacher" groupId="${groupInfo.groupId!}">添加老师</button>
                    </li>
                </ul>
            </#if>
            <table class="table table-hover table-striped table-bordered">
                <tr>
                    <th> 姓 名(ID)</th>
                    <th> 学 科</th>
                    <th> 教 材(ID)</th>
                    <th> 操作</th>
                </tr>
                <#if groupInfo.teacherInfoList?has_content>
                    <#list groupInfo.teacherInfoList as teacherInfo>
                        <tr <#if (teacherInfo.currentTeacher)!false> class="success" </#if>>
                            <td>
                                <a href="../user/userhomepage.vpage?userId=${teacherInfo.teacherId!}"> ${(teacherInfo.teacherName?html)!}</a>(${teacherInfo.teacherId!})
                            </td>
                            <td>${teacherInfo.subject!}</td>
                            <td>
                                <#if teacherInfo.clazzBookInfoList?has_content>
                                    <#list teacherInfo.clazzBookInfoList as clazzBookInfo>
                                        <ul class="inline">
                                            <li>
                                            ${clazzBookInfo.bookName!}
                                                (${clazzBookInfo.bookId!}，${clazzBookInfo.compulsoryTextbook?string("必修", "非必修")})
                                            </li>
                                        </ul>
                                    </#list>
                                </#if>
                            </td>
                            <td class="text-center">
                                <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                                    <button teacherId="${teacherInfo.teacherId!}" groupId="${groupInfo.groupId!}" class="btn btn-danger delClassTeacher">删除</button>
                                    <button class="btn btn-danger" onclick="openDelegateClazzDialog(${teacherInfo.teacherId!},${groupInfo.groupId!},${clazzId!})">转让</button>
                                </#if>
                            </td>
                        </tr>
                    </#list>
                </#if>
            </table>
            <legend>
                <a href="javascript:void(0);" id="show_student_${groupInfo.groupId!}" showGroupId="${groupInfo.groupId!}" showStatus="0" class="show_student">查看学生列表</a>
                <#if !requestContext.getCurrentAdminUser().isCsosUser()><a target="_blank" href="deletedstudentlist.vpage?groupId=${(groupInfo.groupId)!}">删除历史</a></#if>
            </legend>
            <#if groupInfo.studentList?has_content || groupInfo.klxStudentList?has_content>
                <table id="students_${groupInfo.groupId!}" class="table table-hover table-striped table-bordered" style="display: none">
                    <thead>
                    <tr>
                        <td>
                            <input class="v-selectAllStudents" data-groupId="${groupInfo.groupId!''}" name="student" type="checkbox" value=""/>全选
                        </td>
                        <td colspan="4">
                            <button class="btn v-changeStudentsClazz" data-groupId="${groupInfo.groupId!''}">更换班级</button>
                            <button class="btn v-importStudents" data-groupId="${groupInfo.groupId!''}">导入学生</button>
                        </td>
                    </tr>
                    </thead>
                    <tr>
                        <th> 姓 名(ID)</th>
                        <th> 认证日期 <br/> 加入班级时间 </th>
                        <th> 操 作</th>
                        <th> 姓 名(ID)</th>
                        <th> 认证日期 <br/> 加入班级时间 </th>
                        <th> 操 作</th>
                        <th> 姓 名(ID)</th>
                        <th> 认证日期 <br/> 加入班级时间 </th>
                        <th> 操 作</th>
                    </tr>
                    <#list groupInfo.studentList as student>
                        <#if student_index % 3 == 0>
                        <tr></#if>
                        <td>
                            <input class="v-selectStudent" data-groupId="${groupInfo.groupId!''}" data-studentId="${student.id!''}" name="student" type="checkbox" value=""/>
                            <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                                <a href="../student/studenthomepage.vpage?studentId=${student.id!""}"> ${student.name!''}</a>(${student.id!})
                            <#else>
                            ${student.name!''}
                            </#if>
                        </td>
                        <td class="text-center">
                            <#if student.authTime??>${student.authTime!'未认证'}<#else> 未认证 </#if><br/>
                            <#if student.joinTime??> ${student.joinTime?string('yyyy-MM-dd HH:mm:ss')} <#else> -- </#if>
                        </td>
                        <td class="text-center">
                            <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                                <button class="btn btn-danger" onclick="deleteUser(${student.id!""},${groupInfo.groupId!''});">删除
                                </button>
                            </#if>
                        </td>
                        <#if student_index % 3 == 2 || !student_has_next>
                            <#if student_index % 3 == 1>
                                <td colspan="3"></td> </#if>
                            <#if student_index % 3 == 0>
                                <td colspan="3"></td>
                                <td colspan="3"></td> </#if>
                        </tr>
                        </#if>
                    </#list>
                    <#if groupInfo.studentList?has_content>
                        <tr><td>学生人数：</td><td colspan="8">${(groupInfo.studentList)?size}</td> </tr>
                    </#if>
                </table>
                <br/>
                <#if groupInfo.klxStudentList?has_content>
                    <table id="klxStudents_${groupInfo.groupId!}" class="table table-hover table-striped table-bordered" style="display: none">
                        <thead>
                        <tr>
                            <td>
                                <input class="v-selectAllKlxStudents" data-groupId="${groupInfo.groupId!''}" name="klxStudent" type="checkbox" value=""/>全选
                            </td>
                            <td colspan="11">
                                <button class="btn v-changeKlxStudentsClazz" data-groupId="${groupInfo.groupId!''}">更换快乐学学生班级</button>
                                <button class="btn v-mergeOtoSingleStudent" data-groupId="${groupInfo.groupId!''}">合并OTO学生</button>
                                <button class="btn btn-danger only-delete-klx-student" data-groupId="${groupInfo.groupId!''}">删除OTO学生</button>
                            </td>
                        </tr>
                        </thead>
                        <tr>
                            <th> 姓 名</th>
                            <th> 填涂号</th>
                            <th> 操 作</th>
                            <th> 姓 名</th>
                            <th> 填涂号</th>
                            <th> 操 作</th>
                            <th> 姓 名</th>
                            <th> 填涂号</th>
                            <th> 操 作</th>
                            <th> 姓 名</th>
                            <th> 填涂号</th>
                            <th> 操 作</th>
                        </tr>
                        <#list groupInfo.klxStudentList as klxStudent>
                            <#if klxStudent_index % 4 == 0>
                            <tr></#if>
                            <td>
                                <input class="v-selectKlxStudent" data-groupId="${groupInfo.groupId!''}" data-klxStudentId="${klxStudent.id!''}" name="klxStudent" type="checkbox" value=""/>
                            <#if (klxStudent.studentId)?? && (klxStudent.studentId) gt 0>
                                <a href="../student/studenthomepage.vpage?studentId=${klxStudent.studentId!""}">${klxStudent.name}</a>(${klxStudent.studentId!'--'})
                            <#else>
                            ${klxStudent.name}
                            </#if>
                            </td>
                            <td class="text-center">${klxStudent.scanNumber!}</td>
                            <td class="text-center">
                                <button class="btn btn-danger deleteKlxStudent" data-groupId="${groupInfo.groupId!''}" data-klxStudentId="${klxStudent.id!''}" data-klxStudentName="${klxStudent.name!}">删除</button>
                            </td>
                            <#if klxStudent_index % 4 == 3 || !klxStudent_has_next>
                                <#if klxStudent_index % 4 == 2>
                                    <td colspan="3"></td> </#if>
                                <#if klxStudent_index % 4 == 1>
                                    <td colspan="3"></td>
                                    <td colspan="3"></td></#if>
                                <#if klxStudent_index % 4 == 0>
                                    <td colspan="3"></td>
                                    <td colspan="3"></td>
                                    <td colspan="3"></td></#if>
                            </tr>
                            </#if>
                        </#list>
                        <#if groupInfo.klxStudentList?has_content>
                            <tr><td>学生人数：</td><td colspan="11">${(groupInfo.klxStudentList)?size}</td> </tr>
                        </#if>
                    </table>
                </#if>
            <#else >该组暂无学生
            </#if>
        </#list>
    </#if>

    <div id="dialog-confirm" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>是否真的删除此学生</h3>
        </div>
        <div class="modal-body">
            <input id="studentId" style="display: none"/>
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>组 ID</dt>
                        <dd id="groupId"></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>备 注</dt>
                        <dd><textarea id="deleteDesc" name="deleteDesc" cols="35" rows="4" value=""></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="delete_student_dialog_btn_ok" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="jclass" class="modal hide fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">将老师加入该班，请输入老师ID</h4>
                </div>
                <div class="modal-body" id="scid">
                    <ul class="inline">
                        <li>班级ID</li>
                        <li>${clazzId!}</li>
                    </ul>
                    <ul class="inline">
                        <li>组&nbsp;&nbsp;&nbsp;&nbsp;ID</li>
                        <li id="groupIdAddTeacher">${clazzId!}</li>
                    </ul>
                    <ul class="inline">
                        <li>老师ID</li>
                        <li>
                            <input type="text" size="20" id="joinTid"/>
                        </li>
                    </ul>
                </div>

                <div class="modal-footer" id="edb">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="btn_join_submit" type="button" class="btn btn-primary">提交</button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->

    <div id="joinTeacherIntoEmptyGroup" class="modal hide fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">将老师加入班级</h4>
                </div>
                <div class="modal-body" id="scid">
                    <ul class="inline">
                        <li>班级ID</li>
                        <li>${clazzId!}</li>
                    </ul>
                    <ul class="inline">
                        <li>组&nbsp;&nbsp;&nbsp;&nbsp;ID</li>
                        <li id="emptyGroupIdAddTeacher">${clazzId!}</li>
                    </ul>
                    <ul class="inline">
                        <li>老师ID:</li>
                        <li>
                            <input type="text" size="20" id="joinTeacherId"/>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>问题描述</dt>
                            <dd><textarea id="joinTeacherIntoEmptyGroupDesc" name="joinTeacherIntoEmptyGroupDesc"
                                          cols="35" rows="4"></textarea></dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>记录类型</dt>
                            <dd>老师操作</dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>所做操作</dt>
                            <dd>老师加入班级</dd>
                        </li>
                    </ul>
                </div>

                <div class="modal-footer" id="edb">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="btn_joinemptygroup_submit" type="button" class="btn btn-primary">提交</button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->


    <div id="changecl" class="modal hide fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">请选择年级</h4>
                </div>
                <div class="modal-body" id="scid">
                    <ul class="inline">
                        <li>年级列表:</li>
                        <li>
                            <select id="clazz_level" name="clazz_level" class="input-small" style="width: 100px">
                                <#if  seniorSchool?has_content && seniorSchool == true>
                                    <option value="11">高一</option>
                                    <option value="12">高二</option>
                                    <option value="13">高三</option>
                                <#else>
                                    <#list clazzLevels as clazzLevel>
                                        <option value="${clazzLevel.key!}" <#if resultMap?? && resultMap.groupClazzInfo?has_content && clazzLevel.key?c == resultMap.groupClazzInfo.clazzLevel!>
                                                selected </#if>>${clazzLevel.value!}</option>
                                    </#list>
                                </#if>
                            </select>
                        </li>
                    <#--新班级体系先去掉学制的修改了---20150820-->
                    <#--<li>学制:</li>-->
                    <#--<li>-->
                    <#--<select id="eduSystem" name="eduSystem" class="input-small" style="width: 150px">-->
                    <#--<option value="P5" <#if clazzInfo.eduSystem == 'P5' > selected </#if>>小学五年制</option>-->
                    <#--<option value="P6" <#if clazzInfo.eduSystem == 'P6' > selected </#if>>小学六年制</option>-->
                    <#--</select>-->
                    <#--</li>-->
                    </ul>
                </div>

                <div class="modal-footer" id="edb">
                    <input id="changeGroupId" name="changeGroupId" type="hidden">
                    <input id="oldClazzLevel" name="oldClazzLevel" type="hidden">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="btn_change_clazz_submit" type="button" class="btn btn-primary">提交</button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->


    <div id="changeartsicencetype" class="modal hide fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">选择文理科</h4>
                </div>
                <div class="modal-body" id="scid">
                    <ul class="inline">
                        <li>选择文理</li>
                        <li>
                            <select id="art_science_type" name="art_science_type" class="input-small"
                                    style="width: 100px">
                                <option value="SCIENCE">理科</option>
                                <option value="ART">文科</option>
                                <option value="ARTSCIENCE">不分文理</option>
                            </select>
                        </li>
                    </ul>
                </div>

                <div class="modal-footer" id="edb">
                    <input id="selectedGroupId" name="selectedGroupId" type="hidden">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="btn_change_group_artsicencetype_submit" type="button" class="btn btn-primary">提交
                    </button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->


<#--转让组-->
    <div id="delegateClazz_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>老师转让班级</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal inline">
                <ul class="inline">
                    <li>
                        <dt>老师ID</dt>
                        <dd id="oldTeacherId"></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>班级ID</dt>
                        <dd id="delegateClazzClazzId"></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>组ID</dt>
                        <dd id="groupIdTransferGroup"></dd>
                    </li>
                </ul>

                <ul class="inline">
                    <li>
                        <dt>记录类型</dt>
                        <dd>老师操作</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>接受班级的老师ID</dt>
                        <dd><input type="text" id="recipientId"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd><textarea id="delegateClazzDesc" cols="35" rows="4"></textarea></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>所做操作</dt>
                        <dd>管理员转让老师班级</dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="delegateClazz_dialog_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

<#--学生转换班级-->
    <div id="modal-changeStudentsClazz" class="modal hide fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                    <h3>更换学生所在班级</h3>
                </div>
                <div class="modal-body dl-horizontal inline" id="scid">
                    <dl>
                        <dt>分组ID</dt>
                        <dd><input id="v-csc-groupId" type="text"/></dd>
                    </dl>
                    <dl>
                        <dt>问题描述</dt>
                        <dd><textarea id="v-csc-desc" cols="35" rows="4" style="resize: none;"></textarea></dd>
                    </dl>
                </div>
                <div class="modal-footer" id="edb">
                    <input id="modal-groupId" type="hidden">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="modal-btn-changeStudentsClazz" type="button" class="btn btn-primary">提交</button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->

    <div id="modal-inputScanNumber" class="modal hide fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                    <h3>更换学生所在班级</h3>
                </div>
                <div class="modal-body dl-horizontal" id="scid">
                    <dl>
                        <dt>该学生新阅卷机填涂号</dt>
                        <dd><input id="v-csc-scanNumber" type="text" maxlength="5"/></dd>
                    </dl>
                    <dl>
                        <dt>问题描述</dt>
                        <dd><textarea id="v-csc-scannumber-desc" cols="35" rows="4" style="resize: none;"></textarea></dd>
                    </dl>
                </div>
                <div class="modal-footer" id="edb">
                    <input id="modal-selected-groupId" type="hidden">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="modal-btn-changeStudentsClazzWithScanNumber" type="button" class="btn btn-primary">提交
                    </button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->

<#--分组导入学生-->
    <div id="modal-importStudents" class="modal hide fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                    <h3>批量导入学生</h3>
                </div>
                <div class="modal-body dl-horizontal">
                    <dl>
                        <dt>请输入学生ID</dt>
                        <dd><textarea id="v-is-studentIds" cols="70" rows="4"></textarea></dd>
                    </dl>
                    <dl>
                        <dt>操作描述</dt>
                        <dd><textarea id="v-is-desc" cols="35" rows="4"></textarea></dd>
                    </dl>
                </div>
                <div class="modal-footer" id="edb">
                    <input id="modal-is-groupId" type="hidden">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="modal-btn-importStudents" type="button" class="btn btn-primary">提交</button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->

<#--分组导入学生结果显示-->
    <div id="modal-importStudentsResult" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>以下学生导入失败</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <table id="isResults">
                </table>
            </dl>
            <div class="modal-footer">
                <button id="btn-copyISResult" class="btn btn-primary">确认</button>
            </div>
        </div>
    </div>

    <div id="dialog-delete-klxStudent-confirm" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>是否真的删除此快乐学学生</h3>
        </div>
        <div class="modal-body">
            <#--<input id="deleteKlxStudentId" style="display: none"/>-->
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>学生姓名</dt>
                        <dd id="deleteKlxStudentName"></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>学生 ID</dt>
                        <dd id="deleteKlxStudentId"></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>组 ID</dt>
                        <dd id="klx-student-delete-groupId"></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>备 注</dt>
                        <dd><textarea id="deleteKlxDesc" name="deleteKlxDesc" cols="35" rows="4" style="resize: none;"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="delete_klxStudent_dialog_btn_ok" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

<#--快乐学学生转换班级-->
    <div id="modal-changeKlxStudentsClazz" class="modal hide fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                    <h3>快乐学学生转换分组</h3>
                </div>
                <div class="modal-body" id="scid">
                    <dl class="dl-horizontal">
                        <ul class="inline">
                            <li>
                                <dt>请输入分组ID:</dt>
                                <dd><input id="v-klx-csc-groupId" type="text"/></dd>
                            </li>
                        </ul>
                        <ul class="inline">
                            <li>
                                <dt>问题描述:</dt>
                                <dd><textarea id="v-klx-csc-desc" cols="35" rows="4" style="resize: none;"></textarea>
                                </dd>
                            </li>
                        </ul>
                    </dl>
                </div>
                <div class="modal-footer" id="edb">
                    <input id="modal-klx-groupId" type="hidden">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="modal-btn-changeKlxStudentsClazz" type="button" class="btn btn-primary">提交
                    </button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->

<#--快乐学合并oto单个学生-->
    <div id="modal-mergeOtoSingleStudent" class="modal hide fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                    <h3>合并OTO班组学生</h3>
                </div>
                <div class="modal-body dl-horizontal" id="scid">
                    <dl>
                        <dt>需要合并的学生ID</dt>
                        <dd><input id="v-klx-oto-single-studentId" type="text"/></dd>
                    </dl>
                    <dl>
                        <dt>操作备注</dt>
                        <dd><textarea id="v-klx-merge-single-desc" cols="35" rows="4" style="resize: none;"></textarea>
                        </dd>
                    </dl>
                </div>
                <div class="modal-footer" id="edb">
                    <input id="modal-klx-oto-single-groupId" type="hidden">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="modal-btn-mergeOtoSingleStudent" type="button" class="btn btn-primary">提交</button>
                </div>
            </div>
        </div>
    </div>

</div>

<div id="transferGroupToClazz_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>group组转移</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>需转入的班级ID:</dt>
                    <dd><input id="transferClazzId" type='text'/></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>操作备注</dt>
                    <dd><textarea id="operationNotes" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="transferGroupToClazz_dialog_confirm_btn" class="btn btn-primary" groupId="">确定</button>
        <button id="transferGroupToClazz_dialog_cancel_btn" class="btn btn-primary">取消</button>
    </div>
</div>

<div id="changeClazzName_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>修改班级名称</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>班级ID</dt>
                    <dd>${clazzId!}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>新的班名</dt>
                    <dd><input type="text" name="clazz_name" id="clazz_name"/></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>问题描述</dt>
                    <dd><textarea id="modifyTeacherNameDesc" cols="35" rows="4"></textarea></dd>

                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>所做操作</dt>
                    <dd>更改班级名称。</dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="changeClazzName_dialog_confirm_btn" class="btn btn-primary" clazzId="">确定</button>
        <button id="changeClazzName_dialog_cancel_btn" class="btn btn-primary">取消</button>
    </div>
</div>

<div id="changeClazzEduSystem_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>修改年制</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>学制列表:</li>
                <li>
                    <select id="clazz_edusystem" name="clazz_edusystem" class="input-small" style="width: 100px">
                        <option value="P5" selected>小学五年制</option>
                        <option value="P6">小学六年制</option>
                        <option value="J3">初中三年制</option>
                        <option value="J4">初中四年制</option>
                        <option value="S3">高中三年制</option>
                        <option value="S4">高中四年制</option>
                    </select>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="changeClazzEduSystem_dialog_confirm_btn" class="btn btn-primary" clazzId="">确定</button>
        <button id="changeClazzEduSystem_dialog_cancel_btn" class="btn btn-primary">取消</button>
    </div>
</div>

<div id="mergeOtoGroupStudent_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>合并OTO班组</h3>
    </div>
    <div class="modal-body dl-horizontal">
        <dl>
            <dt style="width: 170px;">当前选择班组ID</dt>
            <dd id="targetGroupId"></dd>
        </dl>
        <dl>
            <dt style="width: 170px;">选择合并班组ID</dt>
                    <dd><input id="groupIdByOtoMerge" type='text'/></dd>
        </dl>
        <dl>
            <dt style="width: 170px;">班组合并方式</dt>
            <dd>
                <select id="mergeMode" style="width: 190px;">
                    <option value="">-- 请选择合并方式 --</option>
                    <option value="move">选中组合并到当前组</option>
                    <option value="sync">选中组与当前组同步</option>
                </select>
                <span style="color:red;">
                    <i class="icon-info-sign" data-toggle="tooltip" data-placement="right"
                       title="【选中组合并到当前组】: 从选中组中将学生转移到当前组，适用于行政班组或者同科教学班组
                       <br/>---------------------------<br/>
                        【选中组与当前组同步】: 将选中组和目标组学生全量同步，适用于行政班组和教学班组之间或者异科教学班组"></i> *
                </span>
            </dd>
        </dl>
        <dl>
            <dt style="width: 170px;">操作备注</dt>
                    <dd><textarea id="mergeOtoGroupDesc" cols="35" rows="4" style="resize: none;"></textarea></dd>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="mergeOtoGroupStudent_dialog_confirm_btn" class="btn btn-primary">确定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取消</button>
    </div>
</div>
<#include "../specialschool.ftl">
<script>

    function deleteUser(studentId, groupId) {
        $("#studentId").val(studentId);
        $("#groupId").text(groupId);
        $('#deleteDesc').val('');
        $("#dialog-confirm").modal("show");
    }

    function openDelegateClazzDialog(teacherId, groupId, clazzId) {
        if (confirm("换班前请务必确认该老师已经拒绝所有换班申请")) {
            $("#oldTeacherId").text(teacherId);
            $("#delegateClazzClazzId").text(clazzId);
            $("#groupIdTransferGroup").text(groupId);
            $('#recipientId').val('');
            $("#delegateClazzDesc").val('');
            $("#delegateClazz_dialog").modal("show");
        }
    }
    function transferGroupToClazz(groupId) {
        $("#transferGroupToClazz_dialog_confirm_btn").attr("groupId", groupId);
        $("#transferGroupToClazz_dialog").modal("show");
    }

    function changeClazzName(clazzId) {
        $("#changeClazzName_dialog_confirm_btn").attr("clazzId", clazzId);
        $("#changeClazzName_dialog").modal("show");
    }

    function changeClazzEduSystem(clazzId) {
        $("#changeClazzEduSystem_dialog_confirm_btn").attr("clazzId", clazzId);
        $("#changeClazzEduSystem_dialog").modal("show");
    }

    function mergeOtoGroupStudent(groupId) {
        $("#targetGroupId").html(groupId);
        $("#mergeOtoGroupStudent_dialog").modal("show");
    }

    function syncOTOGroupStudent(groupId) {
        if (!confirm("是否将一起作业学生同步显示到O2O老师名下")) {
            return false;
        }
        if (!checkSpecialSchool()) {
            return false;
        }
        $.post('syncotogroupstudent.vpage', {groupId: groupId}, function (res) {
            if (res.success) {
                window.location.reload();
            } else {
                alert("处理失败：" + res.info);
            }
        });
    }

    function syncShareGroupStudent(groupId) {
        if (!confirm("是否将共享班组下的快乐学学生同步")) {
            return false;
        }
        if (!checkSpecialSchool()) {
            return false;
        }
        $.post('syncsharegroupstudent.vpage', {groupId: groupId}, function (res) {
            if (res.success) {
                window.location.reload();
            } else {
                alert("处理失败：" + res.info);
            }
        });
    }

    $(function () {
        $("[data-toggle='tooltip']").tooltip({html : true });

        var clazzId = ${clazzId!};
        var groupId = ${groupId!};
        var teacherId = ${teacherId!};

        $("#delete_student_dialog_btn_ok").on("click", function () {
            if (!checkSpecialSchool()) {
                return false;
            }
            var queryUrl = "deletestudent.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    studentId: $("#studentId").val(),
                    groupId: $("#groupId").text(),
                    deleteDesc: $('#deleteDesc').val()
                },
                success: function (data) {
                    if (data.success) {
                        location.href = "/crm/clazz/groupinfo.vpage?groupId=" + groupId + "&teacherId=" + teacherId + "&clazzId=" + clazzId;
                    } else {
                        alert("删除班级学生失败, " + data.info);
                    }
                    $("#dialog-confirm").modal("hide");
                }
            });
        });

        $(document).on("click", "#addTeacher", function () {
            var groupIdAddTeacher = $(this).attr("groupId");

            //判断groupIdAddTeacher组下是否有老师
            var hasTeacher = false;
            <#if resultMap??&&resultMap.groupInfoList??&&resultMap.groupInfoList?has_content>
                <#list resultMap.groupInfoList as groupInfo>
                    var cmpGroupId = ${groupInfo.groupId}
                    if (cmpGroupId == groupIdAddTeacher) {
                        <#if groupInfo.teacherInfoList?has_content>
                            hasTeacher = true;
                        </#if>
                    }
                </#list>
            </#if>

            if (hasTeacher) {
                $("#groupIdAddTeacher").text(groupIdAddTeacher);
                $("#jclass").modal("show");
            } else {
                $("#emptyGroupIdAddTeacher").text(groupIdAddTeacher);
                $("#joinTeacherIntoEmptyGroup").modal("show");
            }
        });

        $(document).on("click", "#btn_join_submit", function () {
            var thisGroupId = $("#groupIdAddTeacher").text();
            if (isNaN(thisGroupId) || isNaN($("#joinTid").val())) {
                alert("请输入数字");
                return;
            }
            if (!checkSpecialSchool()) {
                return false;
            }
            $.post('joinclass.vpage', {teacherId: $("#joinTid").val(), groupId: thisGroupId}, function (data) {
                alert(data.info);
                window.location.href = "/crm/clazz/groupinfo.vpage?groupId=" + groupId + "&teacherId=" + teacherId + "&clazzId=" + clazzId;
            });
        });

        $(document).on("click", "#btn_joinemptygroup_submit", function () {
            var thisTeacherId = $("#joinTeacherId").val()
            var thisGroupId = $("#emptyGroupIdAddTeacher").text();
            var thisClazzId = clazzId;
            var joinTeacherIntoEmptyGroupDesc = $("#joinTeacherIntoEmptyGroupDesc").val();
            if (!checkSpecialSchool()) {
                return false;
            }
            $.post(
                    'jointeacherintogroup.vpage',
                    {
                        teacherId: thisTeacherId,
                        groupId: thisGroupId,
                        clazzId: thisClazzId,
                        joinTeacherIntoEmptyGroupDesc: joinTeacherIntoEmptyGroupDesc
                    },
                    function (data) {
                        alert(data.info);
                        window.location.href = "/crm/clazz/groupinfo.vpage?groupId=" + thisGroupId + "&teacherId=" + thisTeacherId + "&clazzId=" + thisClazzId;
                    }
            );
        });

        $(document).on("click", ".delClassTeacher", function () {
            var thisTeacherId = $(this).attr("teacherid");
            var thisGroupId = $(this).attr("groupId");
            if (window.confirm("！！！删除老师将会删除老师的学生资源，请确认是否删除，如果想转让学生资源给其他老师，请使用转让功能")) {
                deleteGroup(thisTeacherId, thisGroupId, clazzId, false);
            }
        });

        $(".change_clazz_level").on("click", function () {
            var changeGroupId = $(this).attr("changeGroupId");
            var oldClazzLevel = $('#clazz_level').find('option:selected').val()

            $("#changeGroupId").val(changeGroupId);
            $("#oldClazzLevel").val(oldClazzLevel);
            $("#changecl").modal("show");
        });

        $(".change_clazz_group_artsciencetype").on("click", function () {
            var changeGroupId = $(this).attr("changeGroupId");
            $("#selectedGroupId").val(changeGroupId);
            $("#changeartsicencetype").modal("show");
        });

        $('#btn_change_clazz_submit').on("click", function () {
            if (!checkSpecialSchool()) {
                return false;
            }
            $.post('changeclazzlevel.vpage',
                    {
                        groupId: $("#changeGroupId").val(),
                        oldClazzLevel: $("#oldClazzLevel").val(),
                        oldClazzId: clazzId,
                        clazzLevel: $('#clazz_level').find('option:selected').val(),
                    }, function (data) {
                        alert(data.info);
                        window.location.href = "/crm/clazz/groupinfo.vpage?groupId=" + groupId + "&teacherId=" + teacherId + "&clazzId=" + clazzId;
                    });
        });

        $('#btn_change_group_artsicencetype_submit').on("click", function () {
            $.post('changeartsicencetype.vpage',
                    {
                        groupId: $("#selectedGroupId").val(),
                        artScienceType: $('#art_science_type').find('option:selected').val(),
                    }, function (data) {
                        alert(data.info);
                        window.location.href = "/crm/clazz/groupinfo.vpage?groupId=" + groupId + "&teacherId=" + teacherId + "&clazzId=" + clazzId;
                    });

        });

        $('#delegateClazz_dialog_btn').on('click', function () {
            var data = {
                teacherId: $('#oldTeacherId').text(),
                clazzId: $('#delegateClazzClazzId').text(),
                newTeacherId: $('#recipientId').val(),
                delegateClazzDesc: $("#delegateClazzDesc").val()
            };
            if (!checkSpecialSchool()) {
                return false;
            }
            $.post("transfergroup.vpage", data, function (data) {
                if (data.success) {
                    // 强行踢出，使用户重新登录
                    $.ajax({
                        url: "/crm/teacher/kickOutOfApp.vpage",
                        type: "POST",
                        async: false,
                        data: {
                            "userId": $('#oldTeacherId').text()
                        },
                        success: function (data) {
                        }
                    });
                    alert(data.info);
                    window.location.href = "/crm/clazz/groupinfo.vpage?groupId=" + groupId + "&teacherId=" + teacherId + "&clazzId=" + clazzId;
                } else {
                    alert(data.info);
                }
            });
        });

        $(".show_student").on("click", function () {
            var tmpGroupId = $(this).attr("showGroupId");
            var showStatus = $(this).attr("showStatus");
            var studentTable = document.getElementById("students_" + tmpGroupId);
            var klxStudentTable = document.getElementById("klxStudents_" + tmpGroupId);
            if (showStatus == 0) {
//                studentTable.setAttribute("style", "display:block;");
                studentTable.removeAttribute("style");
//                klxStudentTable.setAttribute("style", "display:block;");
                if(klxStudentTable != null ) klxStudentTable.removeAttribute("style");
                $(this).attr("showStatus", 1);
                $(this).text("隐藏学生列表");
            } else if (showStatus == 1) {
                studentTable.setAttribute("style", "display:none;");
                if(klxStudentTable != null ) klxStudentTable.setAttribute("style", "display:none;");
                $(this).attr("showStatus", 0);
                $(this).text("查看学生列表");
            }
        });

        /**********************学生转班功能*************************/
        var selectedObj = {
            studentIds: [],
            groupId: -1
        };

        $(document).on("click", ".v-selectStudent", function () {
            var $this = $(this);
            var $studentId = $this.attr("data-studentId");
            var $groupId = $this.attr("data-groupId");

            if ($groupId != selectedObj.groupId) {
                selectedObj.groupId = $groupId;
                selectedObj.studentIds = [];
                $(".v-selectStudent").prop("checked", false);
                $(".v-selectAllStudents").prop("checked", false);
                $this.prop("checked", true);
            }

            if ($this.prop("checked")) {
                selectedObj.studentIds.push($studentId);
            } else {
                selectedObj.studentIds.splice($.inArray($studentId, selectedObj.studentIds), 1);
            }
        });

        $(document).on("click", ".v-selectAllStudents", function () {
            var $this = $(this);
            var $thisCheck = $(".v-selectStudent");

            var $groupId = $this.attr("data-groupId");
            if ($groupId != selectedObj.groupId) {
                selectedObj.groupId = $groupId;
                $(".v-selectStudent").prop("checked", false);
                $(".v-selectAllStudents").prop("checked", false);
                $this.prop("checked", true);
            }

            selectedObj.studentIds = [];

            if ($this.prop("checked")) {
                $thisCheck.each(function () {
                    var $that = $(this);
                    var $studentId = $that.attr("data-studentId");
                    var $studentGroupId = $that.attr("data-groupId");
                    if ($studentGroupId == $groupId) {
                        selectedObj.studentIds.push($studentId);
                        $that.prop("checked", true);
                    }
                });
            } else {
                $thisCheck.prop("checked", false);
            }

        });

        // 更换班级
        $(".v-changeStudentsClazz").on("click", function () {
            var groupId = $(this).attr("data-groupId");
            $("#modal-groupId").val(groupId);
            $("#modal-changeStudentsClazz").modal("show");
        });

        $("#modal-btn-changeStudentsClazz").on("click", function () {
            if (!checkSpecialSchool()) {
                return false;
            }
            var groupId = $("#modal-groupId").val();
            var targetGroupId = $("#v-csc-groupId").val();
            var desc = $("#v-csc-desc").val();
            if (groupId != selectedObj.groupId) {
                return false;
            }
            if (isBlank(targetGroupId)) {
                alert("无效的组ID");
                return false;
            }
            if (isBlank(desc)) {
                alert("问题描述不能为空");
                return false;
            }
            var postData = {
                srcGroupId: selectedObj.groupId,
                targetGroupId: targetGroupId,
                desc: desc,
                studentIds: selectedObj.studentIds.join()
            };

            $.post("movestudentscheck.vpage", postData, function (data) {
                if (data.success) {
                    var needScanNumber = data.needScanNumber;
                    if (needScanNumber) {
                        $("#v-csc-groupId").val("");
                        $("#modal-groupId").val("");
                        $("#v-csc-desc").val("");
                        $("#v-csc-scanNumber").val("");
                        $("#modal-changeStudentsClazz").modal("hide");
                        $("#v-csc-scannumber-desc").val(postData.desc);
                        $("#modal-selected-groupId").val(postData.targetGroupId);
                        $("#modal-inputScanNumber").modal("show");
                    } else {
                        $.post("movestudents.vpage", postData, function (data) {
                            if (data.success) {
                                alert("更换学生班级成功！");
                                location.reload();
                            } else {
                                alert(data.info);
                            }
                        });
                    }
                } else {
                    alert(data.info);
                }
            });
        });

        $("#modal-btn-changeStudentsClazzWithScanNumber").on("click", function () {
            if (!checkSpecialSchool()) {
                return false;
            }
            var targetGroupId = $("#modal-selected-groupId").val();
            var desc = $("#v-csc-scannumber-desc").val();
            var scanNumber = $("#v-csc-scanNumber").val();
            if (isBlank(targetGroupId)) {
                alert("无效的组ID");
                return false;
            }
            if (!isBlank(scanNumber) && !$.isNumeric(scanNumber)) {
                alert("阅卷机号必须是数字");
                return false;
            }
            if (isBlank(desc)) {
                alert("问题描述不能为空");
                return false;
            }

            var postData = {
                srcGroupId: selectedObj.groupId,
                targetGroupId: targetGroupId,
                desc: desc,
                studentIds: selectedObj.studentIds.join(),
                scanNumber: scanNumber
            };

            if (selectedObj.studentIds.length != 1) {
                alert("O2O业务学生无法批量换校，请选择一位学生再进行操作");
                return false;
            }

            $.post("movestudents.vpage", postData, function (data) {
                    if (data.success) {
                        alert("更换学生班级成功！");
                        location.reload();
                    } else {
                        alert(data.info);
                    }
                });
        });

        // 导入学生
        $(".v-importStudents").on("click", function () {
            if (!checkSpecialSchool()) {
                return false;
            }
            var groupId = $(this).attr("data-groupId");
            $("#modal-is-groupId").val(groupId);// 设置group id
            // 判断老师是否绑定手机
            $.post("importStudentsTeacherCheck.vpage", {groupId: groupId}, function (data) {
                if (data.success) {
                    // 已绑定手机，执行后续操作
                    // 弹窗
                    $("#modal-importStudents").modal("show");
                } else {
                    alert(data.info);
                }
            });
        });

        $("#modal-btn-importStudents").on("click", function () {
            if (!checkSpecialSchool()) {
                return false;
            }
            var groupId = $("#modal-is-groupId").val();
            var studentIds = $("#v-is-studentIds").val();
            if (isBlank(studentIds)) {
                alert("请填写学生ID");
                return false;
            }

            var desc = $("#v-is-desc").val();
            if (isBlank(desc)) {
                alert("请填写问题描述");
                return false;
            }

            // 再次弹出确认框
            if (window.confirm("导入新班级后，同学将解除与原班级的联系并将丢失作业数据！")) {
                // 执行导入学生操作
                $.post("importstudents.vpage", {groupId: groupId, content: studentIds, desc: desc}, function (data) {
                    if (data.success) {
                        $("#isResults").empty();
                        for (var key in data.result) {
                            var html = "<tr><td>" + data.result[key].id + "</td>" + "<td>" + data.result[key].reason + "</td></tr>";
                            $("#isResults").append(html);
                        }
                        if (data.result.length > 0) {
                            $('#modal-importStudents').modal('hide');
                            $('#modal-importStudentsResult').modal('show');
                        } else {
                            alert("导入成功");
                            location.reload();
                        }
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        $("#btn-copyISResult").on("click", function () {
            location.reload();
        });

        $("#transferGroupToClazz_dialog_confirm_btn").on("click", function () {
            if (!checkSpecialSchool()) {
                return false;
            }
            var groupId = $("#transferGroupToClazz_dialog_confirm_btn").attr("groupId");
            var clazzId = $("#transferClazzId").val();
            var operationDesc = $("#operationNotes").val();
            $.post('/crm/clazz/transfergrouptoclazz.vpage', {
                groupId: groupId,
                clazzId: clazzId,
                operationDesc: operationDesc
            }, function (data) {
                $("#transferGroupToClazz_dialog").modal("hide");
                if (data.success) {
                    var sharedTeacherInfo = data.sharedTeacherInfo;
                    if (sharedTeacherInfo == undefined || sharedTeacherInfo == null) {
                        // 强行踢出，使用户重新登录
                        var tempteacherIds = data.teacherIds;
                        if (tempteacherIds != undefined && tempteacherIds != null) {
                            for (var i = 0; i < tempteacherIds.length; i++) {
                                $.ajax({
                                    url: "/crm/teacher/kickOutOfApp.vpage",
                                    type: "POST",
                                    async: false,
                                    data: {
                                        "userId": tempteacherIds[i]
                                    },
                                    success: function (data) {
                                    }
                                });
                            }
                        }
                        alert("操作成功");
                        window.location.reload();
                    } else {
                        var desc = "是否要将如下关联group一同转移到新的班级\n\n";
                        for (var i = 0; i < sharedTeacherInfo.length; i++) {
                            desc = desc + sharedTeacherInfo[i]["teacherName"] + " (" + sharedTeacherInfo[i]["teacherId"] + ") " + sharedTeacherInfo[i]["groupId"] + "\n";
                        }
                        if (window.confirm(desc)) {
                            $.post('/crm/clazz/transfergrouptoclazz.vpage', {
                                groupId: groupId,
                                clazzId: clazzId,
                                operationDesc: operationDesc,
                                checked: true
                            }, function (data) {
                                if (data.success) {
                                    var tempteacherIds = data.teacherIds;
                                    if (tempteacherIds != undefined && tempteacherIds != null) {
                                        for (var i = 0; i < tempteacherIds.length; i++) {
                                            $.ajax({
                                                url: "/crm/teacher/kickOutOfApp.vpage",
                                                type: "POST",
                                                async: false,
                                                data: {
                                                    "userId": tempteacherIds[i]
                                                },
                                                success: function (data) {
                                                }
                                            });
                                        }
                                    }
                                    alert("操作成功");
                                    window.location.reload();
                                } else {
                                    alert(data.info);
                                }
                            });
                        }
                    }
                } else {
                    alert(data.info);
                }
            });
        });

        $("#changeClazzName_dialog_confirm_btn").on("click", function () {
            if (!checkSpecialSchool()) {
                return false;
            }
            var clazzId = $("#changeClazzName_dialog_confirm_btn").attr("clazzId");
            var clazzName = $("#clazz_name").val();
            var modifyTeacherNameDesc = $('#modifyTeacherNameDesc').val();
            if (isNaN(clazzId)) {
                alert("班级id有误");
                return;
            }
            if (clazzName == "") {
                alert("请填写新的班名");
                return;
            }
            if (clazzName.match(/[^0-9\u4e00-\u9fa5]/g)) {
                alert("班名请使用汉字或数字");
                return;
            }

            if (modifyTeacherNameDesc == "") {
                alert("请填写问题描述");
                return;
            }
            $.post('/crm/clazz/changeclazzname.vpage', {clazzId: clazzId, clazzName: clazzName, modifyTeacherNameDesc: modifyTeacherNameDesc}, function (data) {
                $("#changeClazzName_dialog").modal("hide");
                if (data.success) {
                    alert("修改班级名称成功!");
                } else {
                    alert(data.info);
                }
            });
        });

        $("#changeClazzEduSystem_dialog_confirm_btn").on("click", function () {
            if (!checkSpecialSchool()) {
                return false;
            }
            var clazzId = $("#changeClazzEduSystem_dialog_confirm_btn").attr("clazzId");
            var eduSystem = $("#clazz_edusystem").val();
            if (isNaN(clazzId)) {
                alert("班级id有误");
                return;
            }
            $.post('/crm/clazz/changeclazzedusystem.vpage', {clazzId: clazzId, eduSystem: eduSystem}, function (data) {
                $("#changeClazzEduSystem_dialog").modal("hide");
                if (data.success) {
                    alert("修改年制成功!");
                } else {
                    alert(data.info);
                }
            });
        });

        $("#transferGroupToClazz_dialog_cancel_btn").on("click", function () {
            $("#transferGroupToClazz_dialog").modal("hide");
        });

        $("#changeClazzName_dialog_cancel_btn").on("click", function () {
            $("#changeClazzName_dialog").modal("hide");
        });

        $("#changeClazzName_dialog_cancel_btn").on("click", function () {
            $("#changeClazzName_dialog").modal("hide");
        });

        $("#changeClazzEduSystem_dialog_cancel_btn").on("click", function () {
            $("#changeClazzEduSystem_dialog").modal("hide");
        });

        $("#mergeOtoGroupStudent_dialog_confirm_btn").on("click", function () {
            var targetGroupId = $("#targetGroupId").text();
            var sourceGroupId = $("#groupIdByOtoMerge").val();
            var mode = $("#mergeMode").val();
            var desc = $("#mergeOtoGroupDesc").val();
            if (!checkSpecialSchool()) {
                return false;
            }
            $.post("mergeotogroupstudent.vpage", {
                sourceGroupId: sourceGroupId,
                targetGroupId: targetGroupId,
                mode: mode,
                desc: desc
            }, function (data) {
                if (data.success) {
                    alert("合并成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });

        $("input[name='lock-show-rank']").on("change", function () {
            var $this = $(this);
            var clazzId = $this.parent().siblings().eq(0).find("a").html();
            var confirmMsg = $this.is(":checked") ?
                    '锁定后，老师无法开启或关闭班排行榜' :
                    '解锁后，老师可以开启或关闭班排行榜'

            if (confirm(confirmMsg)) {
                $.post('/crm/clazz/updatelockshowrank.vpage',
                        {'clazzId': clazzId, 'lockFlag': $this.is(":checked")},
                        function (data) {
                            if (data.success) {
                                alert("操作成功");
                            } else {
                                alert("操作失败.原因:\n" + data.info);
                            }

                            window.location.reload();
                        })
            }

        });

        $(".deleteKlxStudent").on('click', function () {
            var groupId = $(this).attr("data-groupId");
            var klxStudentId = $(this).attr("data-klxStudentId");
            var klxStudentName = $(this).attr("data-klxStudentName");

            $("#klx-student-delete-groupId").text(groupId);
            $("#deleteKlxStudentId").html(klxStudentId);
            $("#deleteKlxStudentName").html(klxStudentName);

            $("#dialog-delete-klxStudent-confirm").modal("show");

        });

        $("#delete_klxStudent_dialog_btn_ok").on('click', function () {
            var schoolId = "${(resultMap.groupClazzInfo.schoolId)!''}";
            var groupId = $("#klx-student-delete-groupId").text();
            var klxStudentId = $("#deleteKlxStudentId").text();
            var desc = $("#deleteKlxDesc").val();

            if (desc == '') {
                alert("请填写删除原因");
                return false;
            }

            if (!confirm("是否确认仅仅从此班组中删除OTO学生?")) {
                return false;
            }
            if (!checkSpecialSchool()) {
                return false;
            }
            var postData = {
                schoolId: schoolId,
                groupId: groupId,
                klxStudentIds: klxStudentId
            };

//            console.info(postData);

            $.post("onlydeleteklxstudent.vpage", postData, function (data) {
                if (data.success) {
                    alert("删除成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
//            $.post("klxstudentexitgroup.vpage", {
//                groupId: groupId,
//                klxStudentId: klxStudentId,
//                desc: desc
//            }, function (data) {
//                if (data.success) {
//                    window.location.reload();
//                } else {
//                    alert(data.info);
//                }
//            });

        });

        /**********************快乐学学生相关功能*************************/
        var selectedKlxStudentsObj = {
            klxStudentIds: [],
            groupId: -1
        };
        $(document).on("click", ".v-selectKlxStudent", function () {
            var $this = $(this);
            var $klxStudentId = $this.attr("data-klxStudentId");
            var $groupId = $this.attr("data-groupId");

            if ($groupId != selectedKlxStudentsObj.groupId) {
                selectedKlxStudentsObj.groupId = $groupId;
                selectedKlxStudentsObj.klxStudentIds = [];
                $(".v-selectKlxStudent").prop("checked", false);
                $(".v-selectAllKlxStudents").prop("checked", false);
                $this.prop("checked", true);
            }

            if ($this.prop("checked")) {
                selectedKlxStudentsObj.klxStudentIds.push($klxStudentId);
            } else {
                selectedKlxStudentsObj.klxStudentIds.splice($.inArray($klxStudentId, selectedKlxStudentsObj.klxStudentIds), 1);
            }
            console.info(selectedKlxStudentsObj);
        });

        $(document).on("click", ".v-selectAllKlxStudents", function () {
            var $this = $(this);
            var $thisCheck = $(".v-selectKlxStudent");

            var $groupId = $this.attr("data-groupId");
            if ($groupId != selectedKlxStudentsObj.groupId) {
                selectedKlxStudentsObj.groupId = $groupId;
                $(".v-selectStudent").prop("checked", false);
                $(".v-selectKlxStudents").prop("checked", false);
                $this.prop("checked", true);
            }

            selectedKlxStudentsObj.klxStudentIds = [];

            if ($this.prop("checked")) {
                $thisCheck.each(function () {
                    var $that = $(this);
                    var $klxStudentId = $that.attr("data-klxStudentId");
                    var $studentGroupId = $that.attr("data-groupId");
                    if ($studentGroupId == $groupId) {
                        selectedKlxStudentsObj.klxStudentIds.push($klxStudentId);
                        $that.prop("checked", true);
                    }
                });
            } else {
                $thisCheck.prop("checked", false);
            }
            console.info(selectedKlxStudentsObj);
        });

        // 更换班级
        $(".v-changeKlxStudentsClazz").on("click", function () {
            var groupId = $(this).attr("data-groupId");
            $("#modal-klx-groupId").val(groupId);
            $("#modal-changeKlxStudentsClazz").modal("show");
        });

        $("#modal-btn-changeKlxStudentsClazz").on("click", function () {
            var groupId = $("#modal-klx-groupId").val();
            var targetGroupId = $("#v-klx-csc-groupId").val();
            var desc = $("#v-klx-csc-desc").val();
            if (groupId != selectedKlxStudentsObj.groupId) {
                return false;
            }
            if (isBlank(targetGroupId)) {
                alert("参数错误");
                return false;
            }
            if (isBlank(desc)) {
                alert("问题描述不能为空");
                return false;
            }

            if (selectedKlxStudentsObj.klxStudentIds.length == 0) {
                alert("没有选择要换班级的快乐学学生");
                return false;
            }
            var postData = {
                srcGroupId: selectedKlxStudentsObj.groupId,
                targetGroupId: targetGroupId,
                desc: desc,
                klxStudentIds: selectedKlxStudentsObj.klxStudentIds.join(",")
            };
            if (!checkSpecialSchool()) {
                return false;
            }
            $.post("changeklxstudentsclazz.vpage", postData, function (data) {
                if (data.success) {
                    var confirmMsg = "修改成功";
                    for (var i = 0; i < data.scanNumberDiffInfo.length; i++) {
                        if (i == 0) {
                            confirmMsg = confirmMsg + ",请告知老师以下学生的填涂号已变更\n";
                        }
                        var name = data.scanNumberDiffInfo[i]["name"];
                        var oldScanNumber = data.scanNumberDiffInfo[i]["oldScanNumber"];
                        var newScanNumber = data.scanNumberDiffInfo[i]["newScanNumber"];
                        confirmMsg = confirmMsg + "姓名" + name + ",旧填涂号" + oldScanNumber + ",新填涂号" + newScanNumber + "\n";
                    }
                    alert(confirmMsg);
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });

        //合并oto单个学生
        $(".v-mergeOtoSingleStudent").on("click", function () {
            var groupId = $(this).attr("data-groupId");
            var klxStudents = $(".v-selectKlxStudent");
            var selectedKlxStudentIds = selectedKlxStudentsObj.klxStudentIds;

            if (selectedKlxStudentIds.length != 1) {
                alert("请选择一名学生");
                return false;
            }
            $("#modal-klx-oto-single-groupId").val(groupId);
            $("#modal-mergeOtoSingleStudent").modal("show");
        });

        $("#modal-btn-mergeOtoSingleStudent").on("click", function () {
            var groupId = $("#modal-klx-oto-single-groupId").val();
            var desc = $("#v-klx-merge-single-desc").val();
            var studentId = $("#v-klx-oto-single-studentId").val();
            if (groupId != selectedKlxStudentsObj.groupId) {
                return false;
            }
            var klxStudents = $(".v-selectKlxStudent");
            var selectedKlxStudentIds = selectedKlxStudentsObj.klxStudentIds;

            if (selectedKlxStudentIds.length != 1) {
                alert("只能选择一名学生");
                return false;
            }
            var postData = {
                groupId: groupId,
                studentId: studentId,
                klxStudentId: selectedKlxStudentIds[0],
                desc: desc
            };
            if (!checkSpecialSchool()) {
                return false;
            }
            $.post("mergeotosinglestudent.vpage", postData, function (data) {
                if (data.success) {
                    var confirmMsg = "修改成功";
                    alert(confirmMsg);
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });

        $('.only-delete-klx-student').on('click', function() {
            var schoolId = "${(resultMap.groupClazzInfo.schoolId)!''}";
            var groupId = $(this).attr("data-groupId");
            var klxStudents = $(".v-selectKlxStudent");
            var selectedKlxStudentIds = selectedKlxStudentsObj.klxStudentIds;

            if (selectedKlxStudentIds.length === 0) {
                alert("没有选择要换班级的快乐学学生");
                return false;
            }
            if (!confirm("是否确认仅仅从此班组中删除"+selectedKlxStudentIds.length+"个OTO学生?")) {
                return false;
            }
            var postData = {
                schoolId: schoolId,
                groupId: groupId,
                klxStudentIds: selectedKlxStudentIds.join(",")
            };
//            console.info(postData)
            if (!checkSpecialSchool()) {
                return false;
            }
            $.post("onlydeleteklxstudent.vpage", postData, function (data) {
                if (data.success) {
                    alert("删除成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });

        });
    });

    //验证是否未定义或null或空字符串
    function isBlank(str) {
        return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
    }

    function deleteGroup(teacherId, groupId, clazzId, force) {
        if (!checkSpecialSchool()) {
            return false;
        }
        $.post('/crm/clazz/deletegroup.vpage', {teacherId: teacherId, groupId: groupId, force: force}, function (data) {
            if (data.success) {
                alert("操作成功");
            } else {
                alert("操作失败.原因:\n" + data.info);
            }
            window.location.href = "/crm/clazz/groupinfo.vpage?groupId=" + groupId + "&teacherId=" + teacherId + "&clazzId=" + clazzId;
        });
    }

    function changeGroupClazz(teacherId, groupId, schoolId) {
        if (!checkSpecialSchool()) {
            return false;
        }
        $.post('/crm/clazz/changegroupclazz.vpage', {
            teacherId: teacherId,
            groupId: groupId,
            schoolId: schoolId
        }, function (data) {
            if (data.success) {
                alert("操作成功");
            } else {
                alert(data.info);
            }
            window.location.href = "/crm/clazz/groupinfo.vpage?groupId=" + groupId + "&teacherId=" + teacherId;
        })
    }
</script>
</@layout_default.page>