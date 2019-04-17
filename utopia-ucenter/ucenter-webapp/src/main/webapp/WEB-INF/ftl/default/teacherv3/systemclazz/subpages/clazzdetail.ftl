<style>
    .global-prom {
        padding: 12px 20px;
        color: #f69696;
        font-size: 14px;
        line-height: 22px;
        background-color: #fff6e3;
        margin:10px 0;
    }
    .global-prom .icon {
        display: block;
        padding: 0 0 0 34px;
        background: url(<@app.link href='public/skin/teacherv3/images/icon-prom.png?v=e912ce3ecb'/>) 0 50% no-repeat
    }
    .w-table table td{
        text-align: center;
    }
    .w-table table tbody td a.disabledColor{
        color:#383a4c;
        cursor: default;
    }
    .student-name-maxl{
        display: inline-block;
        vertical-align: middle;
        max-width: 120px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

</style>
<script id="t:修改名字" type="text/html">
    <div class="w-form-table">
        <dl>
            <dt>学生姓名：</dt>
            <dd><input style="width: 200px;" type="text" placeholder="请输入学生姓名(汉字)" maxlength="16" value="<%=studentName%>" class="v-newname w-int"/></dd>
        </dl>
    </div>
</script>
<script id="t:修改密码" type="text/html">
    <div class="w-form-table">
        <dl>
            <dt>新登录密码</dt>
            <dd><input type="text" value="" placeholder="请输入新密码" class="v-password w-int"/></dd>
            <dt>再次输入新密码</dt>
            <dd><input type="text" value="" placeholder="请再次输入新密码" class="v-confirmPassword w-int"/></dd>
        </dl>
    </div>
</script>
<script id="t:绑定手机重置安全密码" type="text/html">
    <div style="text-align:center;">
        <p style="font-size:16px; padding-bottom: 20px">正在帮 <span style="color:#f64;"><%=studentName%>同学</span> 重置密码，请取得学生或家长同意后操作</p>
        密码将发送至学生家长手机：<span style="color:#f64;"><%=mobile%></span>
    </div>
</script>
<script id="t:编辑学生" type="text/html">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-label">
                <div class="label">
                    <div class="title">学生姓名：</div>
                    <input class="v-student-name" placeholder="请输入学生姓名(汉字)" maxlength="16" type="text" value="<%=studentName%>">
                </div>
                <div class="label">
                    <div class="title">校内学号：</div>
                    <input class="v-student-number" placeholder="请输入校内学号（必填）" type="text" value="<%=studentNumber%>">
                </div>
                <div class="label">
                    <div class="title">阅卷机填涂号：</div>
                    <input class="v-student-scan-number" placeholder="请输入阅卷机填涂号(必填)" type="text" value="<%=studentScanNumber%>">
                </div>
                <div class="label">
                    <div class="title">标记：</div>
                    <div class="tag JS-transientTag">借读生</div>
                </div>
            </div>
        </div>
    </div>
</script>
<script id="t:newJoinStudents" type="text/html">
    <div class="alert_code text_small" style="margin: 0;">
        以下是最近加入您班级的学生，如果不是真实学生请直接删除
    </div>
    <div class="t-changeclass-alert" id="newJoinStudentsContents" data-clazzid="${clazzId!}">
        <div class="check" style="margin: 0;">
            <div class="con" style="height: auto; border: none; padding: 8px 0;">
                <#if newJoinStudents?? && newJoinStudents?size gt 0>
                    <#list newJoinStudents as st>
                        <dl data-student-id="${st.studentId!''}">
                            <dt>
                                <span class="w-checkbox w-checkbox-current"></span>
                                <p><#--<img src="<@app.avatar href=''/>" width="80" height="80">--></p>
                            </dt>
                            <dd>${st.studentName!''}</dd>
                        </dl>
                    </#list>
                </#if>
            </div>
        </div>
    </div>
    <div class="clear"></div>
</script>
<script id="t:loginedFailuredStudents" type="text/html">
    <div class="alert_code text_small" style="margin: 0;">
        以下学生最近登录一起小学失败，可能是忘记了密码，请您找学生确认，并告知密码
    </div>
    <div class="container_summary" style="overflow: visible; margin: 10px 0 0 0;text-align:center;">
        <div class="w-table w-table-border">
            <table class="table_vox">
                <thead>
                <tr>
                    <th style="width:25%;">序号</th>
                    <th style="width:25%;">姓名</th>
                    <th style="width:25%;">学号</th>
                    <#--<th style="width:25%;">密码</th>-->
                </tr>
                </thead>
            </table>
            <#if loginedFailuredStudents??>
            <div <#if loginedFailuredStudents?size gt 8>style="height: 297px; position: relative; overflow: hidden; overflow-y: auto;top:-2px; width: 100%;"</#if>>
                <table class="table_vox">
                    <tbody>
                        <#list loginedFailuredStudents as st>
                        <tr>
                            <th style="width:25%;">${st_index + 1}</th>
                            <th style="width:25%;">
                                <#if st.studentName != ''>
                                ${st.studentName!''}
                                <#else>
                                    --
                                </#if>
                            </th>
                            <th style="width:25%;">${st.studentId!''}</th>
                            <#--<th style="width:25%;">${st.studentPassword!''}</th>-->
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
            </#if>
        </div>
    </div>
    <div class="clear"></div>
</script>
<form id="loginedFailuredStudentsForm" action="/teacher/clazz/batchdownload.vpage?clazzIds=${clazzId}" method="post" style="display: none;">
    <#if loginedFailuredStudents??>
    <input type="text" name="uIds" value="<#list loginedFailuredStudents as st>${st.studentId!''}<#if st_has_next>,</#if></#list>">
    </#if>
</form>
<script id="t:申请结果" type="text/html">
    <div class="t-addstudent-poput">
        <div class="uploadresults addstudent">
            <div class="main">
                <p>以下学生添加失败</p>
                <div class="w-table w-table-border">
                    <table>
                        <thead>
                            <tr>
                                <td>序号</td>
                                <td>学号</td>
                                <td>失败原因</td>
                            </tr>
                        </thead>
                        <tbody>
                            <%for(var i = 0; i < data.length; i++){%>
                                <%if(data[i].success == false){%>
                                    <tr>
                                        <th><%=i+1%></th>
                                        <th><%=data[i].studentId%></th>
                                        <td><%=data[i].info%></td>
                                    </tr>
                                <%}%>
                            <%}%>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</script>
<script id="t:添加学生" type="text/html">
    <div class="t-addstudent-poput">
        <div class="addstudent">
            <div class="main">
                <div class="w-base">
                    <div class="w-base-switch w-base-two-switch">
                        <ul>
                            <li class="active" data-content="1">
                                <a href="javascript:void(0);">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    <%if(type == "abtest"){%>
                                    创建新学生账号
                                    <%}else{%>
                                    批量添加学生
                                    <%}%>
                                </a>
                            </li>
                            <li data-content="2">
                                <a href="javascript:void(0);">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    添加已有账号学生
                                </a>
                            </li>
                        </ul>
                    </div>
                    <div class="t-student-create" data-tabnum="1">
                        <div class="main">
                            <div class="info">
                                <div class="box">
                                    <h3>上传名单要求：</h3>
                                    <p>1.姓名必须为中文。</p>
                                    <%if(type == "abtest"){%>
                                    <p>2.一次输入1个学生的姓名</p>
                                    <%}else{%>
                                    <p>2.每行只能输入一个学生的姓名。</p>
                                    <%}%>
                                </div>
                            </div>
                                <%if(type == "abtest"){%>
                                <input id="batch_student_name" placeholder="一次输入1个学生的姓名" class="w-int" style="width: 410px; padding: 10px;"/>
                                <%}else{%>
                            <div class="text">
                                <textarea id="batch_student_name" class="w-int"></textarea>
                            </div>
                                <%}%>
                        </div>
                    </div>
                    <div class="t-student-create" data-tabnum="2" style="display: none;">
                        <div class="main">
                            <div class="info">
                                <div class="box">
                                    <h3>上传名单要求：</h3>
                                    <p>1.学号必须为数字。</p>
                                    <%if(type == "abtest"){%>
                                    <p>2.一次输入1个学生的学号</p>
                                    <%}else{%>
                                    <p>2.每行只能输入一个学生的学号。</p>
                                    <%}%>
                                </div>
                            </div>
                                <%if(type == "abtest"){%>
                                <input id="batch_student_number" placeholder="一次输入1个学生的学号" class="w-int" style="width: 410px;; padding: 10px;"/>
                                <%}else{%>
                            <div class="text">
                                <textarea id="batch_student_number" class="w-int"></textarea>
                            </div>
                                <%}%>
                        </div>
                    </div>
                </div>
                <div class="t-pubfooter-btn">
                    <a class="v-wind-close w-btn w-btn-green w-btn-small" href="javascript:void(0);">取消</a>
                    <a class="v-wind-submit w-btn w-btn-small" data-contentnum="1" data-clazzid="${clazzId!}" href="javascript:void(0);">确定</a>
                </div>
            </div>
        </div>
    </div>
</script>

<!--w-base template-->
<div id="Anchor" class="w-base">
    <div class="w-base-title">
        <h3>学生管理</h3>
        <div class="w-base-right">
            <#--小学-->
            <#if (currentTeacherDetail.isPrimarySchool())!false>
                <a class="w-btn w-btn-mini data-ImportStudentName" style="width: 120px;" href="javascript:void(0);" data-clazzid="${clazzId!}" data-reload="yes" data-creatorType="${creatorType!'SYSTEM'}">添加学生名单</a>
                <#--<a class="w-btn w-btn-mini js-click-sendAccount" style="width: 120px;" href="javascript:void(0);">给学生发编号</a>-->
                <#--<a class="w-btn w-btn-mini" style="width: 120px;" href="/clazz/downloadletter.vpage" target="_blank">家长使用说明</a>-->
                <a class="w-btn w-btn-mini v-clickTeachingStudentsUse" style="width: 120px;" href="javascript:void(0);" >教学生如何使用</a>
            </#if>
            <#--初中非O2O(语文、英语)-->
            <#if klxScanMachineFlag!false>
                <a class="w-btn w-btn-mini data-ImportStudentName" style="width: 120px;" href="javascript:void(0);" data-clazzid="${clazzId!}" data-reload="yes" data-creatorType="${creatorType!'SYSTEM'}">导入学生名单</a>
                <span class="w-tips-main" style="cursor: pointer;">
                    <i class="w-icon w-icon-41" style="margin: 2px 0 0 10px; background-position: 0 0 !important;"></i>
                    <span class="tips-box">可通过此功能，批量注册学生账号，或给已有学生批量更新学号及阅卷机填涂号。<br/><br/>注：每次填涂号更新后，一定要及时告知学生，否则会影响学生答题卡扫描</span>
                </span>
            </#if>
            <#--<#if (currentTeacherDetail.isJuniorTeacher())!false>
                <a class="w-btn w-btn-mini" style="width: 120px;" href="/teacher/systemclazz/clazzindex.vpage" >去管理考试学生</a>
                <span class="w-tips-main" style="cursor: pointer;">
                    <i class="w-icon w-icon-41" style="margin: 2px 0 0 5px; background-position: 0 0 !important;"></i>
                    <span class="tips-box" style="width: 180px; left: -160px;">可通过此入口，去管理通过名单导入、已生成阅卷机填涂号但未注册一起小学ID的学生。<i style="left:170px;"></i></span>
                </span>
            </#if>-->
        </div>
    </div>
    <div class="w-base-container">
        <!--//start-->
        <div class="w-table">
            <div class="w-table-head" style="background-color: #edf5fa; margin-top: 1px;">
                <span style="float: right;">学生加入班级:允许/不允许 <a href="javascript:void(0);" class="w-blue v-setStudentJoin">设置</a></span>
                <span class="w-gray w-magR-10">新加入：<span class="w-orange">${newJoinStudents?size}</span>人
                    <#if newJoinStudents?size != 0 && (isManager!false)>
                        <a id="newJoinStudents" href="javascript:void(0);" >查看</a>
                    </#if>
                </span>
                <#--<span class="w-gray w-magR-10">忘记密码：<span class="v-lost-password w-orange">${(loginedFailuredStudents?size)!0}</span>人-->
                    <#--<#if (loginedFailuredStudents?size != 0 && isManager)!false>-->
                        <#--<a id="loginedFailuredStudents" href="javascript:void(0);">查看</a>-->
                    <#--</#if>-->
                <#--</span>-->
                <#if (currentTeacherDetail.isPrimarySchool())!false>
                    <span class="w-gray w-magR-10">绑APP数：<span class="w-orange" id="bindingWeixinCount">--</span>人</span>
                </#if>
                <#if (isManager!false)>
                    <a href="javascript:void(0);" class="w-blue js-clickDownloadExist">下载学生名单</a>
                </#if>
            </div>
            <#if klxScanMachineFlag!false>
                <div class="global-prom"><span class="icon">温馨提示：学生答卷时需使用“阅卷机填涂号”进行填涂哦！</span></div>
            </#if>
            <table id="checkboxs">
                <thead>
                <tr>
                    <td class="JS-studentNameSeq" style="cursor: pointer">学生姓名<i class="studentname-sequencing start"></i></td>
                    <#if klxScanMachineFlag!false>
                        <td class="JS-studentNoSeq" style="cursor: pointer;">校内学号<i class="studentno-sequencing"></i></td>
                    </#if>
                    <td>一起ID</td>
                    <td>绑定手机</td>
                    <#if klxScanMachineFlag!false>
                        <td>阅卷机填涂号
                            <span class="w-tips-main" style="cursor: pointer;">
                                <i class="w-icon w-icon-41" style="
                                margin: 0 0 0 5px; background-position: 0 0 !important;"></i>
                                <span class="tips-box">1.通过阅卷机填涂号，扫描答题卡时可定位该学生；<br/>2.在不重复的情况下，将使用学生学号后${(scanNumberDigit)!5}位作为填涂号，如遇重复，将随机生成，老师可自行修改；<br/>3.每次修改一定要及时告知学生，否则会影响学生答题卡扫描</span>
                            </span>
                        </td>
                    </#if>
                    <#if (currentTeacherDetail.isPrimarySchool())!false>
                        <#--<td>家长手机</td>-->
                        <td>家长APP</td>
                    </#if>
                    <#if klxScanMachineFlag!false>
                        <td>标记</td>
                    </#if>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody class="JS-studentTbody">
                    <#assign bindingWeixinCount = 0>
                    <#if klxstudents?has_content>
                        <#assign studentListData = klxstudents >
                    <#else>
                        <#assign studentListData = students >
                    </#if>

                    <#list studentListData as student>
                    <tr data-clazzid="${clazzId!}" data-id="${student.studentId}" data-schoolno="${student.studentNumber!}" data-name="${student.studentName!''}" data-mobile="${(student.mobile)!''}">
                        <td>
                            <div class="student-name-maxl" title="<#if student_index == 0 && student.studentName == ''>体验账号<#else>${student.studentName!}</#if>">
                                <#if student_index == 0 && student.studentName == "">
                                    体验账号
                                <#else>
                                    ${student.studentName!}
                                </#if>
                            </div>
                        </td>

                        <#if klxScanMachineFlag!false>
                            <#if student.studentNumber?has_content>
                                <td>${student.studentNumber!}</td>
                            <#else>
                                <td>未添加</td>
                            </#if>
                        </#if>

                        <td>
                            <#if student.studentId?has_content>
                               ${student.studentId!}
                            <#else>
                                未注册
                            </#if>
                        </td>

                        <td>
                            <#if student.mobile?has_content>
                                <a href="javascript:void(0);" class="v-checkStuPhone">
                                    <span style="color: black">${student.mobile!}</span>
                                </a>
                            <#else>
                                <a href="javascript:void(0);" class="unboundPhone<#if klxScanMachineFlag!false><#if student.studentId?has_content><#else> disabledColor</#if></#if>">未绑定</a>
                            </#if>
                        </td>

                        <#if klxScanMachineFlag!false>
                            <#if student.scanNumber?has_content>
                                <td>${student.scanNumber!}</td>
                            <#else>
                                <td>未添加</td>
                            </#if>
                        </#if>

                        <#if (currentTeacherDetail.isPrimarySchool())!false>
                            <th>
                                <#if ((student.keyParentAppBind)!false) >
                                    绑定<#assign bindingWeixinCount = (bindingWeixinCount + 1)>
                                <#else>
                                    --
                                </#if>
                            </th>
                        </#if>

                        <#if klxScanMachineFlag!false>
                            <td>
                                <#if (student.isMarked)!false>
                                    借读生
                                <#else>
                                    --
                                </#if>
                            </td>
                        </#if>

                        <th>
                            <#if student.studentId?has_content>
                                <#if student.studentPassword??>
                                    <span style="font-size: 7px;">默认密码：1</span>
                                <#else>
                                    <a data-studentbindmobile="${((student.studentBindMobile)!false)?string}" class="v-setpassword-student" href="javascript:void(0);">重置密码</a>
                                </#if>
                            <#else>
                                <span style="padding: 0 21px;">--&nbsp;&nbsp;&nbsp;</span>
                            </#if>
                            <a data-clazzid="${clazzId!}" data-studentid="${student.studentId!}" data-name="${student.studentName!''}" data-studentnumber="${(student.studentNumber)!''}" data-scannumber="${student.scanNumber!}" data-studentusername="${(student.studentUsername)!''}" data-ismarked="<#if klxScanMachineFlag!false>${((student.isMarked)!false)?string('true','false')}</#if>" data-usert="<#if klxScanMachineFlag!false>junior<#else>primary</#if>" class="v-setname" href="javascript:void(0);">编辑</a>
                            <a data-clazzid="${clazzId!}" data-studentid="${student.studentId!}" data-studentname="${student.studentName!}" data-studentusername="${(student.studentUsername)!''}" class="v-delete-student" href="javascript:void(0);">删除</a>
                        </th>
                    </tr>
                    </#list>

                    <#list importNameList as studentName>
                    <tr data-name="${studentName!''}">
                        <td>
                            <div class="student-name-maxl" title=${studentName!}>${studentName!}</div>
                        </td>
                        <td><span style="color: #f00;">未注册</span></td>
                        <td class="animateTest">---</td>
                        <#if (currentTeacherDetail.isPrimarySchool())!false>
                            <td align="center">--</td>
                        </#if>
                        <th>
                            <a data-clazzid="${clazzId!}" data-studentname="${studentName!}" class="v-delete-studentname" href="javascript:void(0);">删除</a>
                        </th>
                    </tr>
                    </#list>
                </tbody>
            </table>
            <#if (studentListData![])?size lt 10 >
            <div class="t-student-join-info">
                <#--<h3>-->
                    <#--捷径一：一键添加同班老师已经带来的学生！-->
                    <#--<input type="text" class="w-int newTeacherMobile" placeholder="输入同班老师学号或手机号"/>-->
                    <#--<a class="w-btn w-btn-mini v-confirmLink" href="javascript:void(0);" style="width: 60px;">确定</a>-->
                <#--</h3>-->
                <#--<h3>捷径二：让学生通过老师<#if mobile?has_content>手机号<span class="w-red">${mobile!''}</span><#else>编号<span class="w-red">${currentUser.id!}</span></#if>加入班级</h3>-->
                <h3>让学生通过老师号 <span class="w-red"><#if mobile?has_content>${mobile!}或</#if>${(currentUser.id)!'xxx'}</span> 加入班级</h3>

                <#if (currentTeacherDetail.isJuniorTeacher())!false>
                    <div>
                        <p>1. 学生下载一起中学手机版，下载地址：<a href="https://dwz.cn/q4a2Jzf6" target="_blank">https://dwz.cn/q4a2Jzf6</a></p>
                        <p>
                            <span style="display:block;">2. 输入老师号 <#if mobile?has_content>${mobile!}或</#if>${(currentUser.id)!'xxx'}注册账号</span>
                            <img style="display:block; margin: 30px 0 0 30px; width: 220px;" src="<@app.link href="public/skin/teacherv3/images/clazzsuccess/junior_info1.jpg"/>">
                        </p>
                        <p>
                            <span style="display:block;">3. 根据提示查找并加入班级，即可做作业</span>
                            <img style="display:block; margin: 30px 0 0 30px; width: 220px;" src="<@app.link href="public/skin/teacherv3/images/clazzsuccess/junior_info2.jpg"/>">
                        </p>
                    </div>
                <#else>
                    <div class="content">
                        <p>把老师号写在黑板，告诉学生</p>
                        <p style="color: #fff; font-size: 24px; line-height: 95px; height: 105px; width: 320px; text-align: center;">
                        <#if method == 'MOBILE' && mobile?has_content>
                            ${mobile!'xxx'}
                        <#else>
                            ${currentUser.id!'xxx'}
                        </#if>
                        </p>
                        <p>学生去www.17zuoye.com，输入老师号，注册账号做作业</p>
                        <p style="margin: 233px 0 0;">担心学生不会？打印使用指南发给学生！</p>
                    </div>
                </#if>
            </div>
            </#if>
        </div>
        <!--end//-->
    </div>
</div>
<script type="text/html" id="T:给新学生发账号">
    <div style="padding-left: 50px;">
        请输入新学生数量：
        <a class="v-minus-btn w-btn w-btn-mini" href="javascript:void (0)" style="width: 25px;">-</a>
        <input class="v-student-num w-int" type="text" value="<%=clazzNumMax%>" style="width: 50px;">
        <a class="v-plus-btn w-btn w-btn-mini" data-index="<%=i%>" href="javascript:void (0)" style="width: 25px;">+</a>
    </div>
</script>
<script type="text/html" id="T:允许学生加入">
    <div class="t-addclass-case" style="padding: 0;">
        <dl>
            <dt style="width:100px;">当前人数：</dt><dd style="line-height: 36px;"><span><#if klxstudents?has_content>${(klxstudents![])?size}<#else>${(students![])?size}</#if>人</span></dd>
            <dt style="width:100px;">学生加入：</dt>
            <dd>
                <div style="padding-top: 7px;">
                    <span style="cursor: pointer" class="<%if(freeJoin){%>active<%}%> v-free-join" data-type="yes"><i class="w-radio"></i> 允许</span>
                    <span style="cursor: pointer" class="<%if(!freeJoin){%>active<%}%> v-free-join" data-type="no"><i class="w-radio"></i> 不允许</span>
                </div>
            </dd>
            <#--<#if (displayShowRank)!false>-->
            <#if false> <#-- #39806 下线学生端排行榜-->
            <dt style="width:100px;">学生端排行榜：</dt>
            <dd>
                <div style="padding-top: 7px;">
                    <#if (lockShowRank)!false>
                        <span style="cursor: pointer" class=" JS-v-showRank" data-type="yes"><i class="w-radio" style="background:#e0e0e0;border-radius:50%"></i> 显示</span>
                        <span style="cursor: pointer" class=" JS-v-showRank" data-type="no"><i class="w-radio" style="background:#e0e0e0;border-radius:50%"></i> 不显示</span>
                        <a href="http://help.17zuoye.com/?page_id=1467" target="_blank" style="background:url(/public/skin/teacherv3/images/helping.png)  no-repeat;background-size:100%;width:14px;height:14px;display:inline-block;"></a><br/>
                        <div  style="margin-top:4px;font-size:80%">排行榜暂时无法操作，有疑问请咨询客服400-160-1717哦</div>
                    <#else>
                        <span style="cursor: pointer" class="<%if(showRank){%>active<%}%> v-showRank" data-type="yes"><i class="w-radio"></i> 显示</span>
                        <span style="cursor: pointer" class="<%if(!showRank){%>active<%}%> v-showRank" data-type="no"><i class="w-radio"></i> 不显示</span>
                        <a href="http://help.17zuoye.com/?page_id=1467" target="_blank" style="background:url(/public/skin/teacherv3/images/helping.png)  no-repeat;background-size:100%;width:14px;height:14px;display:inline-block;"></a>
                    </#if>

                </div>
            </dd>
            </#if>
        </dl>
    </div>
</script>
<@sugar.capsule js=["clazz.clazzdetail"] />
<script type="text/javascript">
    $(function(){
        //设置学生加入
        var $freeJoin = ${(freeJoin!false)?string};
        var $showRank = ${(showRank!false)?string};
        $(document).on("click", ".v-setStudentJoin", function(){
            $.prompt(template("T:允许学生加入", {freeJoin : $freeJoin , showRank: $showRank}), {
                focus: 1,
                title: "允许学生加入？",
                buttons: { "取消": false, "确定" : true },
                position: {width: 500},
                loaded : function(){
                    $(document).on("click", ".v-free-join", function(){
                        var $this = $(this);
                        $this.addClass("active").siblings().removeClass("active");
                        if($this.attr("data-type") == "yes"){
                            $freeJoin = true;
                        }else{
                            $freeJoin = false;
                        }
                    });
                    $(document).on("click", ".v-showRank", function(){
                        var $this = $(this);
                        $this.addClass("active").siblings().removeClass("active");
                        if($this.attr("data-type") == "yes"){
                            $showRank = true;
                        }else{
                            $showRank = false;
                        }
                    });
                    $('#jqi_state0_button取消').on('click',function(){
                        window.location.reload();
                    });
                    $('.jqiclose ').on('click',function(){
                        window.location.reload();
                    });

                },
                submit : function(e, v){
                    if(v){
                        $.post("/teacher/systemclazz/setfreejoin.vpage", { freeJoin : $freeJoin , showRank: $showRank, clazzId : ${clazzId!0}}, function(data){
                            if(data.success){
                                $17.alert("设置成功",function(){
                                    window.location.reload();
                                });
                            }else{
                                $17.alert("设置失败",function(){
                                    window.location.reload();
                                });
                            }
                        });
                    }
                }
            });
        });

        /*给新学生发账号 start*/
        var clazzNumMax = 60;
        $(document).on("click", ".js-click-sendAccount", function(){
            $.prompt(template("T:给新学生发账号", {clazzNumMax : clazzNumMax}), {
                title: "给新学生发编号",
                buttons: { "确定": true },
                position: {width: 500},
                submit: function(e, v){
                    if(v){
                        $("body").append("<iframe class='vox17zuoyeIframe' style='display:none;' src='/teacher/clazz/downloadnewnumber.vpage?clazzId=${clazzId!}&count="+clazzNumMax+"&creatorType=${creatorType!'SYSTEM'}'/>");
                        $17.voxLog({
                            module : "downloadStudentAccount",
                            op : "amount"
                        });
                    }
                }
            });
        });

        var downloadIframe = "<iframe style='display:none;' src='/teacher/clazz/batchdownload.vpage?clazzIds=${clazzId!}&creatorType=${creatorType!'SYSTEM'}'/>";
        <#if klxstudents?has_content>
            downloadIframe = "<iframe style='display:none;' src='/teacher/clazz/kuailexue/downloadklxstuinfo.vpage?clazzId=${clazzId!}'/>";
        </#if>
        //下载已有学生账号
        $(".js-clickDownloadExist").on("click", function(){
            $("body").append(downloadIframe);
            $17.voxLog({
                module : "downloadStudentAccount",
                op : "exist"
            });
        });

        // 点击姓名排序（此处由于使用freemarker数据注入，无法直接对数据进行排序，需操作dom排序）
        $(document).on('click', '.JS-studentNameSeq', function () {
            $('.JS-studentNoSeq').find('i').removeClass('start reverse');
            if (!$('.JS-studentNameSeq').find('i').hasClass('start')) {
                $('.JS-studentNameSeq').find('i').addClass('start');
                sequenceNameTableList();
            } else {
                if ($('.JS-studentNameSeq').find('i').hasClass('reverse')) {
                    $('.JS-studentNameSeq').find('i').removeClass('reverse');
                    sequenceNameTableList();
                } else {
                    $('.JS-studentNameSeq').find('i').addClass('reverse');
                    reverseNameTableList();
                }
            }
        });
        // 点击学号排序
        $(document).on('click', '.JS-studentNoSeq', function () {
            $('.JS-studentNameSeq').find('i').removeClass('start reverse');
            if (!$('.JS-studentNoSeq').find('i').hasClass('start')) {
                $('.JS-studentNoSeq').find('i').addClass('start');
                sequenceNoTableList();
            } else {
                if ($('.JS-studentNoSeq').find('i').hasClass('reverse')) {
                    $('.JS-studentNoSeq').find('i').removeClass('reverse');
                    sequenceNoTableList();
                } else {
                    $('.JS-studentNoSeq').find('i').addClass('reverse');
                    reverseNoTableList();
                }
            }
        });
        sequenceNameTableList(); // 默认按姓名顺序排列
        // 按学生姓名顺序排序
        function sequenceNameTableList () {
            var studentTbody = $('.JS-studentTbody');
            var studentTrList = studentTbody.find('tr');
            studentTrList.sort(function (a, b) {
                return a.getAttribute('data-name').localeCompare(b.getAttribute('data-name'), 'zh-Hans-CN');
            });
            studentTbody.empty().append(studentTrList);
        }
        // 按学生姓名倒序排序
        function reverseNameTableList () {
            var studentTbody = $('.JS-studentTbody');
            var studentTrList = studentTbody.find('tr');
            studentTrList.sort(function (a, b) {
                return b.getAttribute('data-name').localeCompare(a.getAttribute('data-name'), 'zh-Hans-CN');
            });
            studentTbody.empty().append(studentTrList);
        }
        // 按学生学号顺序排序
        function sequenceNoTableList () {
            var studentTbody = $('.JS-studentTbody');
            var studentTrList = studentTbody.find('tr');
            studentTrList.sort(function (a, b) {
                return a.getAttribute('data-schoolno') - b.getAttribute('data-schoolno');
            });
            studentTbody.empty().append(studentTrList);
        }
        // 按学生学号倒序排序
        function reverseNoTableList () {
            var studentTbody = $('.JS-studentTbody');
            var studentTrList = studentTbody.find('tr');
            studentTrList.sort(function (a, b) {
                return b.getAttribute('data-schoolno') - a.getAttribute('data-schoolno');
            });
            studentTbody.empty().append(studentTrList);
        }

        $(document).on("click", ".v-minus-btn, .v-plus-btn", function(){
            var $this = $(this);

            if($(this).hasClass("v-minus-btn")){
                clazzNumMax = ( clazzNumMax <= 1 ? 1 : (clazzNumMax - 1) );
            }else{
                clazzNumMax = ( clazzNumMax >= 300 ? 300 : (clazzNumMax + 1) );
            }

            if(clazzNumMax <= 1){
                $(".v-minus-btn").addClass("w-btn-disabled");
            }else{
                $(".v-minus-btn").removeClass("w-btn-disabled");
            }

            if(clazzNumMax >= 300){
                $(".v-plus-btn").addClass("w-btn-disabled");
            }else{
                $(".v-plus-btn").removeClass("w-btn-disabled");
            }

            $(".v-student-num").val(clazzNumMax);
        });

        $(document).on("focus, blur", ".v-student-num", function(){
            var tempNumCount = $(".v-student-num").val();

            if(tempNumCount <= 1 || !$17.isNumber(tempNumCount)){
                clazzNumMax = 1;
                $(".v-minus-btn").addClass("w-btn-disabled");
            }else{
                clazzNumMax = tempNumCount;
                $(".v-minus-btn").removeClass("w-btn-disabled");
            }

            if(tempNumCount >= 300){
                clazzNumMax = 300;
                $(".v-plus-btn").addClass("w-btn-disabled");
            }else{
                $(".v-plus-btn").removeClass("w-btn-disabled");
            }

            $(".v-student-num").val(clazzNumMax);
        });
        /*给新学生发账号 end*/

        //绑定微信数
        $("#bindingWeixinCount").text("${bindingWeixinCount}");

        var studentList = $("#checkboxs");

        studentList.find(".unboundPhone").on("click", function(){
            var $this =$(this);
            if($this.hasClass("disabledColor")){
                return ;
            }
            var $data = $this.parents("tr");
            var name = $data.attr("data-name");
            $.prompt("<textarea class='w-int siteSNSContent' style='height: 100px; width: 465px; line-height: 20px;'>"+ name +"同学，你好！由于学号密码公开发放，可能存在安全隐患，请尽量绑定自己或家长手机，绑定后可以通过手机号码查询学号和找回密码。</textarea><span class='init' style='color:#f00 '></span>", {
                title: "提醒"+ name +"同学",
                buttons: { "发送站内信": true },
                position:{width : 510},
                submit: function(){
                    var content= $(".siteSNSContent");
                    var userIds = [];

                    if($17.isBlank(content.val())){
                        content.siblings(".init").html("内容不能为空");
                        return false;
                    }

                    userIds.push($data.attr("data-id"));

                    var data = {
                        content : content.val(),
                        userIds : userIds
                    };
                    App.postJSON("/teacher/conversation/createconversation.vpage?userType=3", data, function(data){
                        if(data.success){
                            $17.alert("发送成功");
                        }else{
                            $17.alert("发送失败");
                        }
                    });
                }
            });
        });

        $(".v-download-clazz").on("click", function(){
            $17.voxLog({
                module: "common",
                op : "downloadletter"
            });
        });

        // 转给Ta（直接输入学号/手机号）
        $(".v-confirmLink").on("click", function() {
            var $this = $(this);

            var clazzId = ${clazzId};
            var clazzName = "${clazzName}";
            var idOrMobile = $this.siblings(".newTeacherMobile");

            var id = idOrMobile.val();
            if( $17.isBlank(id)){
                idOrMobile.addClass("w-int-error");
                return false;
            }

            idOrMobile.removeClass("w-int-error");

            var subject = "${currentTeacherDetail.subject}";

            var $postData = {
                id: id,
                clazzId : clazzId,
                targetSubject : subject == "ENGLISH" ? "MATH" : "ENGLISH"
            };

            // 查找老师
            $.post("/teacher/systemclazz/findlinkteacher.vpage", $postData, function(data){
                if (data.success) {
                    var teacherName = data.teacher.profile.realname;
                    var teacherId = data.teacher.id;

                    $.prompt("<div class='w-ag-center'>您确定将“"+clazzName+"”关联到“"+teacherName+"”老师吗？</div>", {
                        focus: 1,
                        title: "系统提示",
                        buttons: { "取消": false, "确定": true },
                        position: {width: 500},
                        submit : function(e, v){// 发送关联请求
                            if(v){
                                $.get("/teacher/systemclazz/sendlinkapp.vpage", {clazzId : clazzId, respondentId : teacherId}, function(data){
                                    if(data.success){
                                        $17.alert("关联老师请求发送成功！", function(){
                                            location.reload();
                                        });
                                    }else{
                                        $17.alert(data.info);
                                    }
                                });
                            }
                        }
                    });
                } else {
                    $17.alert("根据条件找不到符合条件的老师！");
                }
            });
        });

        //选择下载说明内容
        var _recordDownRadio = "";
        $(document).on("click", ".v-downRadio", function(){
            var $this = $(this);

            $this.addClass("active").siblings().removeClass("active");
            _recordDownRadio = $this.attr("data-type");
        });

        //教学生如何使用
        var $method = "${method!''}";
        $(".v-clickTeachingStudentsUse").on("click", function(){
            var $this = $(this);
            _recordDownRadio = "";
            $.prompt(template("T:教学生如何使用", {method : $method}), {
                title: "系统提示",
                buttons: { "确认下载": true },
                position: {width: 600},
                loaded : function(){
                    //切换学号或手机号
                    $(".v-clickSwitchAccountOrMobile").on("click", function(){
                        var $thisType = $(this).attr("data-account");
                        var msg = "1. 您的编号：${(currentUser.id)!}";
                        $method = $thisType;

                        function switchAccount(type, callback){
                            $.post("/teacher/mobileoraccount.vpage", {
                                method : type
                            }, function(data){
                                if(data.success){
                                    //成功
                                    if(callback){callback()}
                                }else{
                                    $17.alert(data.info);
                                }
                            });
                        }

                        if($thisType == "MOBILE"){
                            msg = "1. 您的手机号：${(mobile)!}";
                            $(this).attr("data-account", "ACCOUNT");
                            $(this).text("切换为一起小学ID");
                        }else{
                            $(this).attr("data-account", "MOBILE");
                            $(this).text("切换为手机号");
                        }

                        switchAccount($thisType, function(){
                            $(".v-userId").html(msg);
                        });
                    });
                },
                submit : function(e, v){// 发送关联请求
                    if(v){
                        if(_recordDownRadio == ""){
                            return false;
                        }

                        if(_recordDownRadio == "streamline"){
                            $("body").append("<iframe style='display:none;' src='/teacher/clazz/downloadnewnumber.vpage?clazzId=${clazzId!}'/>");
                        }
                        if(_recordDownRadio == "detail"){
                            $("body").append("<iframe style='display:none;' src='${(ProductConfig.getMainSiteBaseUrl())!''}/clazz/downloadletter.vpage'/>");
                        }
                    }
                }
            });
        });
    });
</script>
<#--在线导入-->
<#if (currentTeacherDetail.isPrimarySchool())!false>
<#include "../../block/batchAddStudentName.ftl"/>
</#if>
<#--excel导入-->
<#if klxScanMachineFlag!false>
<#include "../../block/batchAddStudentExcel.ftl"/>
</#if>
<script type="text/html" id="T:教学生如何使用">
    <style>
        .t-teachingStudentsPopup{ margin: -40px 20px -10px;}
        .t-teachingStudentsPopup .ts-title{ font-size: 16px; text-align: center; padding: 20px 0;}
        .t-teachingStudentsPopup .ts-msg{ background: url(<@app.link href="public/skin/teacherv3/images/publicbanner/ts-icon.png"/>) no-repeat 20px 15px #fff9e3; color: #e97300; font-size: 14px; line-height: 150%; padding: 15px 0 20px 140px; margin-bottom: 15px;}
        .t-teachingStudentsPopup h5{ margin-bottom: 10px; font-size: 14px;}
        .t-teachingStudentsPopup .ts-info{ font-size: 14px;}
        .t-teachingStudentsPopup .ts-info .v-downRadio{ display: inline-block; vertical-align: middle; margin-right: 20px; cursor: pointer;}
        .t-teachingStudentsPopup .ts-info .active{ color: #269ff8;}
    </style>
    <div class="t-teachingStudentsPopup">
        <div class="ts-title">下载学生使用说明可以让您的学生快速加入一起小学</div>
        <div class="ts-msg">
            <h5>使用说明中包含：</h5>
            <p><span class="v-userId"><%=(method == "MOBILE" ? "1. 您的手机号：${mobile!''}" : "1. 您的编号：${currentUser.id!}")%></span>
                <#if mobile?has_content>
                    <a href="javascript:void(0);" class="w-blue v-clickSwitchAccountOrMobile" data-account='<%=(method == "MOBILE" ? "ACCOUNT" : "MOBILE")%>'>切换为<%=(method == "MOBILE" ? "一起小学ID" : "手机号")%></a>
                </#if>
            </p>
            <p>2. 一起教育科技网址</p>
            <p>3. 学生注册流程</p>
        </div>
        <div class="ts-info">
            <h5>说明内容：</h5>
            <p>
                <span class="v-downRadio" data-type="streamline"><i class="w-radio"></i> 精简版（快速，省纸）</span>
                <span class="v-downRadio" data-type="detail"><i class="w-radio"></i> 详细版本（面向家长，打消疑虑）</span>
            </p>
        </div>
    </div>
</script>