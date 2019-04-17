<#--老师学生管理侧边菜单栏模板-->
<script id="manageMenuTemp" type="text/html">
    <div class="class-sidebar">
        <div class="nav-mode teacherManagement-nav">
            <!--ko foreach:menu -->
            <a href="javascript:;" data-bind="click: $root.menuClick.bind($data,$index()),css:{'active':$index() == 0} "><span data-bind="text: $data"></span></a>
            <!-- /ko -->
        </div>
    </div>
</script>
<#--右侧复用下载学生名单及使用指南-->
<script id="userGuideModal" type="text/html">
    <div class="teacherManagement-btn fr">
        <a class="green-btn" href="javascript:void(0);" data-bind="click: downLoadStudentExcel">下载学生名单</a>
        <a class="green-btn" href="javascript:void(0);" data-bind="visible: printBarShow,click: printBarCode" style="display:none;">打印学生条形码</a>
        <form action="/specialteacher/downloadstudents.vpage" method="POST" id="downloadStudents" enctype="multipart/form-data">
            <input type="hidden" data-bind="attr:{value:gradeIdIn}" name="gradeId">
            <input type="hidden" data-bind="attr:{value:clazzsIdIn}" name="clazzIds">
            <input type="hidden" data-bind="attr:{value:mergeStatus}" name="mergeStatus">
        </form>
    </div>
    <div class="class-module mt-20">
        <div class="teacherManagement-title bg-f6 clearfix">
            <a href="javascript:void(0);" class="green_fontBtn fr" data-bind="click: toggleGuide.bind($data)"><span>收起 </span><i></i></a>
            <div class="title">使用指南</div>
        </div>
        <div class="teacherManagement-box">
            <ul class="step">
                <li><span class="con">校内第一次使用</span><span class="sub">STEP1 添加老师账号 <i></i>STEP2 为老师建班授课 <i></i>STEP3 添加学生账号</span></li>
                <li><span class="con">新学期打散换班</span><span class="sub">STEP1 为老师建班授课 <i></i>STEP2 使用打散换班功能</span></li>
                <li><span class="con">走班制学生管理</span><span class="sub">STEP1 为行政班、教学班老师建班授课 <i></i>STEP2 添加学生账号 <i></i>STEP3 使用复制教学班学生功能</span></li>
            </ul>
        </div>
        <form action="/specialteacher/batchgeneratebarcode.vpage" id="printCodeBarSubmit" method="POST" enctype="multipart/form-data">
            <input type="hidden" name="codeNum" data-bind="attr:{value:codeNumInput }">
            <input type="hidden" name="clazzId" data-bind="attr:{value:clazzIdInput }">
        </form>
    </div>
</script>
<#--添加老师账号模板-->
<script id="addTeacherModal" type="text/html">
    <div class="class-module mt-20 teacherManagement-box"  style="overflow:visible">
        <ul class="management-left">
            <li class="subtitle">添加老师账号：</li>
            <li>1.可单个或批量注册老师账号</li>
            <li><span>2.注册成功后，会通过短信通知老师</span><div class="icon-grayAsk global-ques">
                <div class="text" style="right:-110px;top:22px;">老师您好，教务XXX老师统一为校内老师注册了一起作业账号，您可通过手机号+密码xxxxxx登录17zuoye.com</div>
            </div></li>
            <li>3.上传的excel文件需要为.xls或.xlsx格式</li>
            <li>4.excel模板如右图，<a data-bind="click: downLoadExcel.bind($data,'lxy')" href="javascript:;" style="color:#34a8fb;">点击下载模板</a></li>
        </ul>
        <div class="management-right">
            <div class="uploadBtn">
                <label>
                    <form action="/specialteacher/admin/batchimportteacher.vpage" method="POST" id="importTeacherForm" enctype="multipart/form-data">
                        <input type="file" data class="v-fileupload v-fileupload1" accept=".xls, .xlsx" name="importTeacher" style="left:-9999px;position:absolute;"/>
                    </form>
                    <span class="uploadSpan" data-bind="text:importTName"></span>
                </label>
                <a href="javascript:void(0);" class="up_btn" data-bind="click: fileUploadBtn.bind($data,'#importTeacherForm',1)">上传</a>
                <div class="info" data-bind="visible: teacherErrorShow" style="display:none;"><i></i><span data-bind="html: teacherErrorText"></span></div>
            </div>
            <div class="table-mode">
                <table cellpadding="0" cellspacing="0">
                    <thead>
                    <tr>
                        <td>老师姓名</td>
                        <td>学科</td>
                        <td>手机号</td>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td><span class="name">王潇潇</span></td>
                        <td>数学</td>
                        <td>13210002222</td>
                    </tr>
                    <tr>
                        <td><span class="name">张思</span></td>
                        <td>语文</td>
                        <td>13255888999</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <div style="clear:both;"></div>
    </div>
</script>
<#--为老师建班授课模板-->
<script id="createClazzModal" type="text/html">
    <div class="class-module mt-20 teacherManagement-box">
        <ul class="management-left">
            <li class="subtitle">为老师建班授课：</li>
            <li>1.可批量为老师创建班级授课</li>
            <li>2.需要先添加老师账号，再为老师建班</li>
            <li>3.可一次性添加多个学科、班级和老师</li>
            <li>4.上传的excel文件需要为 .xls或 .xlsx格式</li>
            <li>5.处理完成后，可在班级管理界面查看老师带班情况</li>
            <li>6.excel模板如右图，<a data-bind="click: downLoadExcel.bind($data,'lkh')" href="javascript:;" style="color:#34a8fb;">点击下载模板</a></li>
        </ul>
        <div class="management-right">
            <div class="uploadBtn">
                <label>
                    <form>
                        <input type="file" data class="v-fileupload v-fileupload2" accept=".xls, .xlsx" name="importTeacher" style="left:-9999px;position:absolute;"/>
                    </form>
                    <span class="uploadSpan" data-bind="text:teacherClazzName"></span>
                </label>
                <a href="javascript:void(0);" class="up_btn" data-bind="click: fileUploadBtn.bind($data,'#teacherClazzForm',2)">上传</a>
                <div class="info" data-bind="visible: creatErrorShow" style="display:none;"><i></i><span data-bind="html: creatErrorText"></span></div>
            </div>
            <div class="table-mode">
                <table cellpadding="0" cellspacing="0">
                    <tbody>
                    <tr>
                        <td rowspan="2" class="td">学科</td>
                        <td colspan="3" class="td">7年级</td>
                        <td colspan="2" class="td">高一</td>
                    </tr>
                    <tr>
                        <td>1班</td>
                        <td>2班</td>
                        <td>5班</td>
                        <td>2班</td>
                        <td>3班</td>
                    </tr>
                    <tr>
                        <td>数学</td>
                        <td>张三</td>
                        <td>李四</td>
                        <td>王晓晓</td>
                        <td>张思</td>
                        <td>王林</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</script>
<#--添加学生账号模板-->
<script id="addStudentModal" type="text/html">
    <div class="class-module mt-20 teacherManagement-box">
        <ul class="management-left">
            <li class="subtitle">添加学生账号：</li>
            <li>1.可单个或批量注册学生账号</li>
            <li>2.需要所填班级均已添加授课老师</li>
            <li>3.上传的excel文件需要为 .xls或 .xlsx格式</li>
            <li>4.班内学生姓名不能重名，如遇重复请用张三甲、张三乙这类规则避免</li>
            <li>5.添加完成的学生账号，可在班级管理界面查看</li>
            <li>6.excel模板如右图，<a data-bind="click: downLoadExcel.bind($data,'zlr')" href="javascript:;" style="color:#34a8fb;">点击下载模板</a></li>
        </ul>
        <div class="management-right">
            <div class="uploadBtn">
                <label>
                    <form action="/specialteacher/admin/batchimportstudent.vpage" method="POST" id="importStudentForm" enctype="multipart/form-data" style="left:-9999px;position:absolute;">
                        <input type="file" class="v-fileupload v-fileupload3" accept=".xls, .xlsx" name="importStudent" />
                    </form>
                    <span class="uploadSpan" data-bind="text:importSName"></span>
                </label>
                <a href="javascript:void(0);" class="up_btn" data-bind="click: fileUploadBtn.bind($data,'#importStudentForm',3)">上传</a>
                <div class="info" data-bind="visible: studentErrorShow" style="display:none;"><i></i><span data-bind="html: studentErrorText"></span></div>
            </div>
            <div class="table-mode">
                <table cellpadding="0" cellspacing="0">
                    <thead>
                    <tr>
                        <td>年级</td>
                        <td>班级</td>
                        <td>学生姓名</td>
                        <td>学生校内学号</td>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>7年级</td>
                        <td>3班</td>
                        <td>王晓晓</td>
                        <td>20120569</td>
                    </tr>
                    <tr>
                        <td>高一</td>
                        <td>1班</td>
                        <td>张思</td>
                        <td>20120555</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</script>
<#--校内打散换班模板-->
<script id="changeClazzModal" type="text/html">
    <div class="class-module mt-20 teacherManagement-box"  style="overflow:visible">
        <ul class="management-left">
            <li class="subtitle">校内打散换班：</li>
            <li>1.需要区分行政班内部打乱、或是教学重新分班两种不同类型</li>
            <li>2.一次导入只能处理一种类型打散换班</li>
            <li>3.上传的excel文件需要为 .xls或 .xlsx格式</li>
            <li>4.处理完成后会下载excel到电脑，excel中有成功/失败备注</li>
            <li>5.excel模板如右图，<a data-bind="click: downLoadExcel.bind($data,'xhjx')" href="javascript:;" style="color:#34a8fb;">点击下载模板</a></li>
        </ul>
        <div class="management-right">
            <div class="uploadBtn">
                <label>
                    <form action="/specialteacher/clazz/executechangeclass.vpage" method="POST" id="changeStudentForm" enctype="multipart/form-data" style="left:-9999px;position:absolute;">
                        <input type="file" class="v-fileupload v-fileupload4" accept=".xls, .xlsx" name="changeStudentData" />
                        <input type="text" value="changeclazz" name="changeStudentType" />
                    </form>
                    <span class="uploadSpan" data-bind="text:changeStudentName"></span>
                </label>
                <a href="javascript:void(0);" class="up_btn" data-bind="click: fileUploadBtn.bind($data,'#changeStudentForm',4)">上传</a>
                <div class="info" data-bind="visible: changeStudentErrorShow" style="display:none;"><i></i><span data-bind="html: changeStudentErrorText"></span></div>
            </div>
            <div class="table-mode">
                <table cellpadding="0" cellspacing="0">
                    <thead>
                    <tr>
                        <td>年级</td>
                        <td>学生姓名</td>
                        <td>换班后新班级</td>
                        <td>类型</td>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>7年级</td>
                        <td>王思思</td>
                        <td>4班</td>
                        <td>行政班</td>
                    </tr>
                    <tr>
                        <td>高一</td>
                        <td>王晓晓</td>
                        <td>物理B班</td>
                        <td>教学班</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <div style="clear:both;"></div>
    </div>
</script>
<#--复制教学班学生模板-->
<script id="copyTeachingModal" type="text/html">
    <div class="class-module mt-20 teacherManagement-box"  style="overflow:visible">
        <ul class="management-left">
            <li class="subtitle">复制教学班学生：</li>
            <li>1.可解决学生既在行政班、又在教学班的问题</li>
            <li>2.上传的excel文件需要为 .xls或 .xlsx格式</li>
            <li>3.需要已在行政班中添加该学生</li>
            <li>4.处理完成后会下载excel到电脑，excel中有成功/失败备注</li>
            <li>5.excel模板如右图，<a data-bind="click: downLoadExcel.bind($data,'jt')" href="javascript:;" style="color:#34a8fb;">点击下载模板</a></li>
        </ul>
        <div class="management-right">
            <div class="uploadBtn">
                <label>
                    <form action="/specialteacher/clazz/executelinkclass.vpage" method="POST" id="copyStudentForm" enctype="multipart/form-data" style="display: none">
                        <input type="file" class="v-fileupload v-fileupload5" accept=".xls, .xlsx" name="changeStudentData" />
                        <input type="text" value="linkclazz" name="changeStudentType" />
                    </form>
                    <span class="uploadSpan" data-bind="text:copyStudentName"></span>
                </label>
                <a href="javascript:void(0);" class="up_btn" data-bind="click: fileUploadBtn.bind($data,'#copyStudentForm',5)">上传</a>
                <div class="info" data-bind="visible: copyStudentErrorShow" style="display:none;"><i></i><span data-bind="html: copyStudentErrorText"></span></div>
            </div>
            <div class="table-mode">
                <table cellpadding="0" cellspacing="0">
                    <thead>
                    <tr>
                        <td>年级</td>
                        <td>学生姓名</td>
                        <td>关联的教学班</td>
                        <#--<td>教学班老师手机号</td>-->
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>7年级</td>
                        <td>王思思</td>
                        <td>数学A班</td>
                        <#--<td>14888888888</td>-->
                    </tr>
                    <tr>
                        <td>高一</td>
                        <td>王晓晓</td>
                        <td>物理B班</td>
                        <#--<td>14888333333</td>-->
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <div style="clear:both;"></div>
    </div>
</script>
<#--标记借读生模板-->
<script id="markTransientModal" type="text/html">
    <div class="class-module mt-20 teacherManagement-box"  style="overflow:visible">
        <ul class="management-left">
            <li class="subtitle">标记借读生：</li>
            <li>1.可对班级中的借读生进行标记，用于在学情分析中区分查看成绩</li>
            <li>2.上传的excel文件需要为 .xls或 .xlsx格式</li>
            <li>3.可在班级管理中对学生进行取消标记</li>
            <li>4.处理完成后会下载excel到电脑，excel中有成功/失败备注</li>
            <li>5.excel模板如右图，<a data-bind="click: downLoadExcel.bind($data,'mst')" href="javascript:;" style="color:#34a8fb;">点击下载模板</a></li>
        </ul>
        <div class="management-right">
            <div class="uploadBtn">
                <label>
                    <form action="/specialteacher/admin/batchmarkstudents.vpage" method="POST" id="markTransientForm" enctype="multipart/form-data" style="display: none">
                        <input type="file" class="v-fileupload v-fileupload6" accept=".xls, .xlsx" name="markStudents" />
                    </form>
                    <span class="uploadSpan" data-bind="text:markTransientName"></span>
                </label>
                <a href="javascript:void(0);" class="up_btn" data-bind="click: fileUploadBtn.bind($data,'#markTransientForm',6)">上传</a>
                <div class="info" data-bind="visible: markTransientErrorShow" style="display:none;"><i></i><span data-bind="html: markTransientErrorText"></span></div>
            </div>
            <div class="table-mode">
                <table cellpadding="0" cellspacing="0">
                    <thead>
                    <tr>
                        <td>年级</td>
                        <td>班级</td>
                        <td>学生姓名</td>
                        <td>学生校内学号</td>
                        <td>标记</td>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>高二</td>
                        <td>3班</td>
                        <td>王子琪</td>
                        <td>20160824</td>
                        <td>借读生</td>
                    </tr>
                    <tr>
                        <td>高三</td>
                        <td>1班</td>
                        <td>何敏敏</td>
                        <td>20150825</td>
                        <td>借读生</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <div style="clear:both;"></div>
    </div>
</script>
<#--下载学生名单弹窗1-->
<script id="downloadStudentList" type="text/html">
    <div class="downloadRosterPop">
        <div class="management-column">
            <div class="sel-list">
                选择年级：
                <select class="sel" data-bind="
                     options: Grades,
                     optionsText: 'gradeName',
                     value:grade">
                </select>
            </div>
            <!-- ko with:grade  -->
            <div class="sel-list js-allgrade">
                选择班级：
                <select class="sel" data-bind="
                    options: clazzs,
                    optionsText: 'clazzName',
                    value:$parent.clazz,
                    optionsCaption:'全部班级'">
                </select>
            </div>
            <!-- /ko -->
        </div>
        <!-- ko ifnot:clazz() -->
        <a style="display: block" href="javascript:void(0);" class="download-info JS-downloadInfo" data-bind="click: choiceDownloadStudentStatus.bind($data)"><i class="icon-content"></i> 合并到一张表格下载</a>
        <!-- /ko -->
    </div>
</script>
<script id="printBarStudent" type="text/html">
    <div class="downloadRosterPop">
        <div class="management-column">
            <div class="sel-list">
                选择年级：
                <select class="sel" data-bind="
                 options: Grades,
                 optionsText: 'gradeName',
                 value:grade">
                </select>
            </div>
            <!-- ko with:grade  -->
            <div class="sel-list js-allgrade">
                选择班级：
                <select class="sel" data-bind="
                    options: clazzs,
                    optionsText: 'clazzName',
                    value:$parent.clazz">
                </select>
            </div>
            <!-- /ko -->
        </div>
    </div>
</script>
<script id="confirmPrintTypeTemp" type="text/html">
    <div class="popup-inner">
        <p class="inner-title">已选中1个班级，<!--ko text: studentNum --><!--/ko-->名学生。<i data-bind="visible: noScanNum() > 0" style="display:none;">其中有<!--ko text: noScanNum --><!--/ko-->名学生没有填涂号，将无法生成条形码！</i></p>
        <div class="main-text">
            <div class="student-code s-select" data-bind="click: printTypeItem.bind($data,1),attr:{datanum:printDataNum}">
                <p class="title">依次生成学生条形码</p>
                <div class="code-box">
                    <img src="<@app.link href='/public/skin/specialteacher/images/code-2.png'/>">
                </div>
                <p class="example">示例：每人生成1张条形码</p>
                <div class="input-num">每人<input class="js-scanNumInput" type="text" maxlength="2" data-bind="attr:{value: scanNumInput}">张条形码标签</div>
            </div>
            <div class="person-code" data-num="56" data-bind="click: printTypeItem.bind($data,2)">
                <p class="title">每人生成一整页条形码</p>
                <div class="code-box">
                    <img src="<@app.link href='/public/skin/specialteacher/images/code-1.png'/>">
                </div>
                <p class="example">示例：每人生成一整页条形码</p>
                <div class="input-num">＊一整页包含 <span class="js-defaultTotalNum">56</span>张条形码标签</div>
            </div>
        </div>
        <p class="atten-error" style="display: none;" data-bind="visible: makeSureNum">请确认每人生成条形码的数量</p>
        <p class="atten-text">注意：请确保打印文件时使用原始比例</p>
    </div>
</script>