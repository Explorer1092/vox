<script id="t:修改名字" type="text/html">
    <div class="w-form-table">
        <dl>
            <dt>学生姓名</dt>
            <dd><input type="text" value="" placeholder="请输入学生姓名" class="v-newname w-int"/></dd>
        </dl>
    </div>
</script>
<script id="t:修改密码" type="text/html">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-label">
                <div class="label">
                    <div class="title">新登录密码：</div>
                    <input class="v-password" placeholder="请输入新密码" type="text">
                </div>
                <div class="label">
                    <div class="title">再次输入新密码：</div>
                    <input class="v-confirmPassword" placeholder="请再次输入新密码" type="text" >
                </div>
            </div>
        </div>
    </div>
</script>
<script id="t:绑定手机重置安全密码" type="text/html">
    <div style="text-align:center;">
        <p style="font-size:16px; padding-bottom: 20px">正在帮 <span style="color:#f64;"><%=studentName%>同学</span> 重置密码，请取得学生或家长同意后操作</p>
        密码将发送至学生家长手机：<span style="color:#f64;"><%=mobile%></span>
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
        以下学生最近登录一起作业失败，可能是忘记了密码，请您找学生确认，并告知密码
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

<script id="t:编辑学生" type="text/html">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-label">
                <div class="label">
                    <div class="title">学生姓名：</div>
                    <input class="v-klxstudent-name" maxlength="16" placeholder="请输入学生姓名(汉字)" type="text" value="<%=klxStudentName%>">
                </div>
                <div class="label">
                    <div class="title">校内学号：</div>
                    <input class="v-klxstudent-number" placeholder="请输入校内学号" type="text" value="<%=klxStudentNumber%>">
                </div>
                <div class="label">
                    <div class="title">阅卷机填涂号：</div>
                    <input class="v-klxstudent-scan-number" placeholder="请输入阅卷机填涂号" type="text" value="<%=klxStudentScanNumber%>">
                </div>
                <div class="label">
                    <div class="title">标记：</div>
                    <div class="tag JS-klx-transientTag">借读生</div>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="t:选择导入学生方式" type="text/html">
    <div class="addStudentPop">
        <div class="add-title">为<%=nowClassName%>添加学生，请选择添加方式：</div>
        <div class="btn">
            <a href="javascript:;" class="JS-addByOnline">通过在线添加</a>
            <a href="javascript:;" class="JS-addByExcel">通过excel上传</a>
        </div>
    </div>
</script>

<#--在线添加学生弹窗-->
<script id="t:在线添加学生弹窗" type="text/html">
    <div class="addAccountOnlinePop">
        <div class="account-top mt-20">
            <p>添加学生规则：</p>
            <p>1. 每行一次输入：学生姓名（必填）、校内学号（选填），以空格隔开。</p>
            <p>2. 姓名仅支持中文，每行输入一个学生。</p>
            <p>3. 如果填写校内学号，则会将学号后${(scanNumberDigit)!5}位作为学生的阅卷机填涂号；如遇校内重复则随机生成${(scanNumberDigit)!5}位数。</p>
            <p>4. 班内学生姓名不可重复，如遇重名请做以区分，如张三甲、张三乙。</p>
            <p>5. 仅支持添加20个以内的学生账号，更多账号请通过excel上传添加。</p>
        </div>
        <div class="account-column mt-20">
            <div class="left">
                <textarea name="" id="" cols="30" rows="10" class="JS-onlineText" data-teacherId="${(currentUser.id)!}" data-clazzId="${clazzId!}"></textarea>
                <div class="text JS-errorInfo"></div>
            </div>
            <div class="right">
                <div class="info">
                    <p>小提示：</p>
                    <p class="con">您可以从已有学生名单的文档中，把学生信息粘贴到左侧文本框中，如下：</p>
                    <p>1.打开文档，选择文档中需要上传学生的姓名，单击鼠标右键复制；</p>
                    <p>2.点击左侧输入框，单击鼠标右键粘贴。</p>
                </div>
                <div class="table-mode">
                    <table cellpadding="0" cellspacing="0">
                        <thead>
                        <tr>
                            <td>学生姓名</td>
                            <td>校内学号</td>
                        </tr>
                        </thead>
                        <tbody>
                        <tr class="active">
                            <td><span class="name">曲筱绡</span></td>
                            <td>3983993</td>
                        </tr>
                        <tr>
                            <td><span class="name">曲筱绡</span></td>
                            <td>3983993</td>
                        </tr>
                        </tbody>
                    </table>
                    <div class="copyCut-box">
                        <ul>
                            <li><i class="icon-copy"></i>复制（C）</li>
                            <li><i class="icon-copy cut"></i>剪切（T）</li>
                            <li><i class="icon-copy paste"></i>粘贴（P）</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="t:通过excel添加账号" type="text/html">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-upload">
                <div class="head clearfix">
                    <div class="title">选择文档：</div>
                    <a href="javascript:;" class="upload v-uploadKlxDoc">上传</a>
                    <a href="javascript:;" class="download v-downloadTemplate">下载模板</a>
                    <input type="file" id="v-fileupload" data-clazzId="${clazzId!}" accept=".xls, .xlsx" name="file" style="display: none" />
                    <div class="name v-fileName" style="display:none;"></div>
                </div>
                <div class="text">
                    <div>上传说明：</div>
                    <p><em>1.</em>需要按照模版格式要求上传学生姓名、学号；</p>
                    <p><em>2.</em>班内无该姓名学生时，会为其新注册账号：有该学生时，会更新该生学号信息；</p>
                    <p><em>3.</em>学号后${(scanNumberDigit)!5}位在学校内不重复的情况下将作为学生阅卷机填涂号，如遇重复将随机生成${(scanNumberDigit)!5}位数字；</p>
                </div>
                <div class="prom" style="text-align: left;" id="v-errMsg" style="display: none"></div>
            </div>
        </div>
    </div>
</script>

<script id="t:批量导入快乐学学生成功" type="text/html">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-success">
                <div class="success"><span>导入成功</span></div>
                <div class="title">新注册<span class="js-klxImportNewSignNum"></span>名学生，更新<span class="js-klxImportUpdateNum"></span>名学生学号！</div>
                <div class="text">注：阅卷机填涂号更新后，一定要及时告知学生，否则会影响学生答题卡扫描</div>
            </div>
        </div>
    </div>
</script>

<script id="t:快乐学重复学生" type="text/html">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-success">
                <div class="text" style="color:black;font-size:17px">
                    上传名单中学生 <span class="js-klxRepeateStudentName" style="font-weight: bold"></span> 和班内已有学生重名，请确认是否更新班内学生学号信息。
                    如不是同一学生，请点击取消、并修改姓名进行区分。
                </div>
            </div>
        </div>
    </div>
</script>


<script id="t:快乐学填涂号占用信息" type="text/html">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-success">
                <div class="text" style="color:black;font-size:17px">
                    学生 <span class="js-klxTakeUpStudentNames" style="font-weight: bold"></span>学生的填涂号无法使用默认规则,已被如下学生占用：<br/>
                    <span class="js-klxTakeUpStuTeacherInfo" style="font-weight: bold"></span>
                    点击确认可使用随机填涂号，如有问题，可联系以上老师或客服进行处理
                </div>
            </div>
        </div>
    </div>
</script>

<script id="confirmPrintTypeTemp" type="text/html">
    <div class="popup-inner">
        <p class="inner-title">已选中<%= selectedNum%>名学生<%if(unSelectNum !=0){%>，<i>其中<%= unSelectNum%>名学生没有填涂号，将无法生成条形码</i><%}%></p>
        <div class="main-text">
            <div class="student-code s-select js-printTypeItem" data-num="1">
                <p class="title">依次生成学生条形码</p>
                <div class="code-box">
                    <img src="<@app.link href='/public/skin/teacherv3/images/code-2.png'/>">
                </div>
                <p class="example">示例：每人生成1张条形码</p>
                <div class="input-num">每人<input type="text" maxlength="2" class="js-scanNumInput" value="<%= defaultCodeNum%>">张条形码标签</div>
            </div>
            <div class="person-code js-printTypeItem" data-num="56">
                <p class="title">每人生成一整页条形码</p>
                <div class="code-box">
                    <img src="<@app.link href='/public/skin/teacherv3/images/code-1.png'/>">
                </div>
                <p class="example">示例：每人生成1张条形码</p>
                <div class="input-num">＊一整页包含 <span class="js-defaultTotalNum">56</span>张条形码标签</div>
            </div>
        </div>
        <p class="atten-error js-dialogErrorInfo" style="display: none;">请确认每人生成条形码的数量</p>
        <p class="atten-text">注意：请确保打印文件时使用原始比例</p>
    </div>
</script>

<!--w-base template-->
<div class="class-module">
    <div class="module-head bg-f6 clearfix">
        <div class="title">学生管理</div>
        <div class="global-ques right fr">
            <div class="text">可通过此功能，批量注册学生账号，<br>或给已有学生批量更新学号及阅卷机填涂号。<br><br>注：每次填涂号更新后，一定要及时告知学生，否则会影响学生答题卡扫描</div>
        </div>
        <a href="javascript:;" class="v-batchimportklxstudent lead-in">导入学生名单</a>
    </div>
    <div class="module-table">
        <div class="thead clearfix">
            <div class="fr">允许学生加入：<a href="javascript:;" class="v-setStudentJoin">设置</a></div>
            <div class="fl">新加入：${newJoinStudents?size}人</div>
            <a href="javascript:;" class="fl js-clickDownloadExist">下载学生名单</a>
        </div>
    </div>
</div>

<#if klxScanMachineFlag?has_content && klxScanMachineFlag == true>
<#if klxNoScanNumberCount?has_content && klxNoScanNumberCount gt 0>
    <div class="global-prom mt-20"><span class="icon">班内仍有${klxNoScanNumberCount!''}名学生没有阅卷机填涂号，请尽快手动添加或批量导入，然后告知学生，否则学生试卷将无法扫描哦！</span></div>
</#if>
</#if>

<div class="global-prom mt-20"><span class="icon">温馨提示：学生答卷时需使用“阅卷机填涂号”进行填涂哦！</span></div>
<div class="all-select">
    <input type="checkbox" value="0" class="js-selectAllStuBtn"> <label for="" class="label-txt">全选</label>
    <a  class="make-btn js-createBarCodeBtn" href="javascript:;">批量生成条形码</a>
    <form action="/teacher/clazz/batchgeneratebarcode.vpage" id="printCodeBarForm" method="post">
        <input type="hidden" name="students" id="studentsInput">
        <input type="hidden" name="clazzName" id="clazzNameInput">
        <input type="hidden" name="codeNum" id="codeNumInput">
    </form>
</div>
<!--//start-->
<div id="Anchor" class="class-module mt-20">
    <div class="module-table">
        <table id="checkboxs">
            <thead>
                <tr>
                    <td class="frist-td"></td>
                    <td class="JS-studentNameSeq" style="cursor: pointer;">学生姓名<i class="studentname-sequencing start"></i></td>
                    <td class="JS-studentNoSeq" style="cursor: pointer;">校内学号<i class="studentno-sequencing"></i></td>
                    <td>一起ID</td>
                    <td>手机</td>
                    <td width="16%">
                        阅卷机填涂号
                        <div class="global-ques right">
                            <div class="text">1. 通过阅卷机填涂号，扫描答题卡时可定位该学生；<br>2. 在不重复的情况下，将使用学生学号后${(scanNumberDigit)!5}位作为填涂号，如遇重复，将随机生成，老师可自行修改；<br>3. 每次修改一定要及时告知学生，否则会影响学生答题卡扫描</div>
                        </div>
                    </td>
                    <td>标记</td>
                    <td width="26%">操作</td>
                </tr>
            </thead>
            <tbody class="JS-studentTbody">
                <#list klxstudents as student>
                <tr <#if student_index % 2 == 0>class="odd"</#if> data-clazzid="${clazzId!}" data-schoolno="${student.studentNumber!}" data-id="${student.studentId}" data-name="${student.studentName!''}" data-ismarked="${((student.isMarked)!false)?string('true','false')}" data-mobile="${(student.mobile)!''}" data-studentnumber="${(student.studentNumber)!''}" data-scannumber="${(student.scanNumber)!''}"  data-studentusername="${(student.studentUsername)!''}" data-klxScanMachineFlag="${klxScanMachineFlag?string!'false'}">
                    <td class="frist-td">
                        <input type="checkbox" value="" class="js-stuItemCheckBox" data-sname="<#if student.studentName?has_content>${student.studentName!}</#if>" data-scode="<#if student.scanNumber?has_content>${student.scanNumber!}</#if>">
                    </td>
                    <td>
                        <div style="max-width: 150px; display: inline-block; vertical-align: middle; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                            <#if student.studentName?has_content>
                                <span style="color: black" title=${student.studentName!}>${student.studentName!}</span>
                            <#else>
                                <a class="v-edit-klxstudent" href="javascript:;">未添加</a>
                            </#if>
                        </div>
                    </td>
                    <td>
                        <#if student.studentNumber?has_content>
                            <span style="color: black">${student.studentNumber!}</span>
                        <#else>
                            <a class="v-edit-klxstudent" href="javascript:;">未添加</a>
                        </#if>
                    </td>
                    <td>
                        <#if student.studentId?has_content>
                            <span style="color: black">${student.studentId!}</span>
                        <#else>
                            <span style="color: black">未注册</span>
                        </#if>
                    </td>
                    <td>
                        <#if student.mobile?has_content>
                            <div class="v-checkStuPhone">
                                <span style="color: black" >${student.mobile!}</span>
                            </div>
                        <#else>
                            <span style="color: black">未绑定</span>
                        </#if>
                    </td>
                    <td>
                        <#if student.scanNumber?has_content>
                            <span style="color: black">${student.scanNumber!}</span>
                        <#else>
                            <a class="v-edit-klxstudent" href="javascript:;">未添加</a>
                        </#if>
                    </td>
                    <td>
                        <#if (student.isMarked)!false>
                            <span>借读生</span>
                        <#else>
                            <span>--</span>
                        </#if>
                    </td>
                    <td>
                        <a class="v-edit-klxstudent" href="javascript:;" >编辑</a>
                        <#if student.studentId?has_content>
                            <a data-studentbindmobile="${((student.mobile)!false)?string}" class="v-setpassword-student" href="javascript:;">重置密码</a>
                        <#else>
                            <span style="padding: 0 0px;">----</span>
                        </#if>
                        <a data-clazzid="${clazzId!}" data-studentid="${student.studentId!}" data-studentname="${student.studentName!}" data-studentusername="${(student.studentUsername)!''}" data-klx="true" class="v-delete-student" href="javascript:;">删除</a>
                    </td>
                </tr>
                </#list>
            </tbody>
        </table>
    </div>
        <!--end//-->
</div>
<script type="text/html" id="T:允许学生加入">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-join">
                <div class="title">当前人数：<em>${(klxstudents![])?size}</em>人</div>
                <div class="choice">学生加入：
                    <a href="javascript:;" class="label <%if(freeJoin){%>active<%}%> v-free-join" data-type="yes">允许</a>
                    <a href="javascript:;" class="label <%if(!freeJoin){%>active<%}%> v-free-join" data-type="no">不允许</a>
                </div>
            </div>
        </div>
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

        //下载已有学生账号
        $(".js-clickDownloadExist").on("click", function(){
            $("body").append("<iframe style='display:none;' src='/teacher/clazz/kuailexue/downloadklxstuinfo.vpage?clazzId=${clazzId!}'/>");
            $17.voxLog({
                module : "downloadStudentAccount",
                op : "exist"
            });
        });

        var studentList = $("#checkboxs");

        studentList.find(".unboundPhone").on("click", function(){
            var $this =$(this);
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

        // 点击排序（此处由于使用freemarker数据注入，无法直接对数据进行排序，需操作dom排序）
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
        sequenceNameTableList(); // 默认顺序排列
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

        $(".v-download-clazz").on("click", function(){
            $17.voxLog({
                module: "common",
                op : "downloadletter"
            });
        });

        //选择下载说明内容
        var _recordDownRadio = "";
        $(document).on("click", ".v-downRadio", function(){
            var $this = $(this);

            $this.addClass("active").siblings().removeClass("active");
            _recordDownRadio = $this.attr("data-type");
        });
        // 点击编辑学生弹窗 借读生标记(说明，对于弹窗中的事件，由于弹窗中的dom初始化时不存在，故绑定事件时通过documeng来委托绑定)
        $(document).on('click', '.JS-klx-transientTag', function (event) {
            var $thisNode = $(event.currentTarget);
            $thisNode.hasClass('active') ? $thisNode.removeClass('active') : $thisNode.addClass('active');
        });
        $(document).on("click", '.v-edit-klxstudent', function () {
            // 判断是否是第三方，是则禁止操作
            if (isThirdParty == 'true') {
                isThirdPartyTip();
                return false;
            }

            var $self = $(this);
            var $data = $self.parents("tr");
            var editPass = {
                state0: {
                    title: "更新" + $data.attr("data-name") + "资料",
                    html: template("t:编辑学生", {
                        "klxStudentName": $data.attr("data-name"),
                        "klxStudentNumber": $data.attr("data-studentnumber"),
                        "klxStudentScanNumber": $data.attr("data-scannumber")
                    }),
                    focus: 1,
                    buttons: {"取消": false, "确定": true},
                    position: {width: 580},
                    submit: function (e, v) {
                        e.preventDefault();
                        if (v) {
                            var klxStudentName = $(".v-klxstudent-name").val();
                            var klxStudentNumber = $(".v-klxstudent-number").val();
                            var klxStudentScanNumber = $(".v-klxstudent-scan-number").val();
                            var klsIsMarked = $('.JS-klx-transientTag').hasClass('active');
                            if ($17.isBlank(klxStudentName) || $17.isBlank(klxStudentNumber) || $17.isBlank(klxStudentScanNumber)) {
                                $.prompt.goToState('state1');
                            } else if (!$17.isChinaString(klxStudentName)) {//学生姓名是否为纯汉字，不符合则提示——请输入正确的学生姓名
                                $.prompt.goToState('state2');
                            } else if (klxStudentName.length > 16) {//姓名是否<=12个字符，不符合则提示——填写的学生名过长
                                $.prompt.goToState('state3');
                            } else if (!$17.isNumber(klxStudentNumber)) {//校内学号是否为纯数字，不符合则提示——请输入纯数字学号
                                $.prompt.goToState('state4');
                            } else if (klxStudentNumber.length > 14) {//校内学号是否<=14个数字，不符合则提示——填写的校内学号过长
                                $.prompt.goToState('state5');
                            } else if (!$17.isNumber(klxStudentScanNumber)) {
                                $.prompt.goToState('state6');
                            } else {
                                $.post("/teacher/clazz/kuailexue/editklxstudentinfo.vpage", {
                                    clazzId: $data.attr("data-clazzid"),
                                    studentId: $data.attr("data-id"),
                                    klxStudentUserName: $data.attr("data-studentusername"),
                                    klxStudentName: klxStudentName,
                                    klxStudentNumber: klxStudentNumber,
                                    klxStudentScanNumber: klxStudentScanNumber,
                                    isMarked: klsIsMarked
                                }, function (data) {
                                    if (data.success) {
                                        $.prompt("修改成功", {
                                            title: "系统提示",
                                            buttons: {"知道了": true},
                                            submit: function () {
                                                setTimeout(function () {
                                                    location.reload()
                                                }, 200);
                                            }
                                        });
                                    } else {
                                        $.prompt.goToState('state7', false, function () {
                                            $("#editklxstudenterror").text(data.info);
                                        });
                                    }
                                });
                            }
                        } else {
                            $.prompt.close();
                        }
                    }
                },
                state1: {//表单未填写完
                    title: "系统提示",
                    html: '<h4 class="text_red" style="text-align: center;padding: 30px 0px;">您有未输入的信息</h4>',
                    buttons: {"知道了": true},
                    submit: function (e, v) {
                        e.preventDefault();
                        $.prompt.goToState('state0');
                    }
                },
                state2: {
                    title: "系统提示",
                    html: '<h4 class="text_red" style="text-align: center;padding: 30px 0px;">请输入正确的学生姓名</h4>',
                    buttons: {"知道了": true},
                    submit: function (e, v) {
                        e.preventDefault();
                        $.prompt.goToState('state0');
                    }
                },
                state3: {
                    title: "系统提示",
                    html: '<h4 class="text_red" style="text-align: center;padding: 30px 0px;">填写的学生名过长</h4>',
                    buttons: {"知道了": true},
                    submit: function (e, v) {
                        e.preventDefault();
                        $.prompt.goToState('state0');
                    }
                },
                state4: {
                    title: "系统提示",
                    html: '<h4 class="text_red" style="text-align: center;padding: 30px 0px;">请输入纯数字学号</h4>',
                    buttons: {"知道了": true},
                    submit: function (e, v) {
                        e.preventDefault();
                        $.prompt.goToState('state0');
                    }
                },
                state5: {
                    title: "系统提示",
                    html: '<h4 class="text_red" style="text-align: center;padding: 30px 0px;">填写的校内学号过长</h4>',
                    buttons: {"知道了": true},
                    submit: function (e, v) {
                        e.preventDefault();
                        $.prompt.goToState('state0');
                    }
                },
                state6: {
                    title: "系统提示",
                    html: '<h4 class="text_red" style="text-align: center;padding: 30px 0px;">请输入纯数字阅卷机号</h4>',
                    buttons: {"知道了": true},
                    submit: function (e, v) {
                        e.preventDefault();
                        $.prompt.goToState('state0');
                    }
                },
                state7: {
                    title: "系统提示",
                    html: '<h4 id="editklxstudenterror" class="text_red" style="text-align: center;padding: 30px 0px;"></h4>',
                    buttons: {"知道了": true},
                    submit: function (e, v) {
                        e.preventDefault();
                        $.prompt.goToState('state0');
                    }
                }
            };
            $.prompt(editPass);
            $data.attr('data-ismarked') === "true" ? $('.JS-klx-transientTag').addClass('active') : $('.JS-klx-transientTag').removeClass('active');
        });

        var createCodeFlag = true;
        var checkedStuList = [];
        var noScanStuList = [];
        $(document).on("click",".js-selectAllStuBtn",function () {
            var checked = $(".js-selectAllStuBtn").is(':checked');
            var stuItems = $(".js-stuItemCheckBox");
            if(checked){
                stuItems.attr("checked", true);
            }else{
                stuItems.attr("checked", false);
            }
        }).on("click",".js-createBarCodeBtn",function () {
            checkedStuList = [];
            noScanStuList = []; //重置
            var checkedStuItems = $(".js-stuItemCheckBox:checked");
            $.each(checkedStuItems,function (i,item) {
                if($(item).data('scode') != ""){
                    checkedStuList.push({
                        name:$(item).data('sname'),
                        scanNum:$(item).attr('data-scode')
                    })
                }else{
                    noScanStuList.push($(item).data('sname'));
                }
            });

            if(checkedStuList.length != 0){
                var postData = {
                    clazzId:${clazzId!0}
                };

                $.post('/teacher/clazz/batchgeneratebarcodecheck.vpage',postData,function (res) {
                    if(res.success){
                        $("#printCodeBarForm").find("input").val(""); //重置
                        $.prompt(template('confirmPrintTypeTemp',{selectedNum:checkedStuItems.length,unSelectNum:noScanStuList.length,defaultCodeNum:1}), {
                            focus: 1,
                            title: "批量生成条形码",
                            buttons: { "取消": false, "确定" : true },
                            position: {width: 700},
                            loaded : function(){

                            },
                            submit : function(e, v){
                                if(v){
                                    createBarCode(function () {
                                        e.preventDefault();
                                    });
                                }
                            }
                        });
                    }else{
                        $.prompt(res.info || '尚未开通条形码生成权限，请联系您的市场专员帮助开通',{
                                    title: "提示",
                                    buttons: { "确定" : true }
                                });
                    }
                });
            }else{
                $.prompt('请选择有阅卷机填涂号的学生', {
                    title: "提示",
                    buttons: { "确定" : true },
                    position: {width: 500}
                });
            }
        }).on("click",".js-printTypeItem",function () {
            $(this).addClass("s-select").siblings("div").removeClass("s-select");
        });

        var createBarCode = function (callback) {
            var inputValidateFlag = true;
            $(".js-dialogErrorInfo").hide();
            var val = $('.js-scanNumInput').val();
            if(!$17.isNumber(val) || ($17.isNumber(val) && (val <1 || val >56))){
                val = 1;
                $('.js-scanNumInput').val(1);
                $(".js-dialogErrorInfo").show();
                inputValidateFlag = false;
            }

            $('.js-scanNumInput').parents(".js-printTypeItem").attr('data-num',val);

            //TODO 确定是否加锁，防止频繁提交
            if(inputValidateFlag && createCodeFlag){
                var clazzName = ${clazzId!0};

                $("#clazzNameInput").val(clazzName);
                $("#codeNumInput").val(getPrintType());
                $("#studentsInput").val(JSON.stringify(checkedStuList));

                $("#printCodeBarForm").submit();

            }else{
                callback();
            }
        };

        var getPrintType = function () {
            return $('.js-printTypeItem.s-select').data('num');
        }

    });


</script>
<#include "../../block/batchAddStudentName.ftl"/>