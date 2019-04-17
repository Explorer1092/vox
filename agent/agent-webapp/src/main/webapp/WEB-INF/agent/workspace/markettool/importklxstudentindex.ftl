<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='往已有班级里面添加新学生' page_num=1>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 往已有老师的班级里面添加新学生</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <#assign messages = requestContext.getAlertMessageManager().getMessages() />
            <#list messages as msg>
                <#if msg.content?? && msg.content!='errorMessage' && msg.content != '您没有权限进行此操作'>
                    <div class="alert alert-${(msg.category)!''}">${(msg.content)!''}</div>
                </#if>
                <#if msg.content?? && msg.content=='errorMessage' && msg.data??>
                    <#list msg.data as data>
                        <#if data.exit>
                            <div class="alert alert-error">
                                ${data.errorType.value!''}&nbsp;&nbsp;
                                <#if data.rows?? && (data.rows?size > 0)>
                                    所在行：[
                                    <#list data.rows as row>
                                         ${row}  <#if row_has_next> ,</#if >
                                    </#list>
                                    ]
                                </#if>
                            </div>
                        </#if>
                    </#list>
                </#if>
            </#list>
            <script type="text/javascript">
                ${requestContext.getAlertMessageManager().clearMessages()};
            </script>
            <form method="post" action="/workspace/import/import_klxstudents.vpage" enctype="multipart/form-data">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">选择EXCEL数据文件，格式如下图所示。<br/>
                        注意事项:<br/>
                        &nbsp;&nbsp;1. EXCEL文件需要为97－2003版本格式(后缀名为.xls)或者 2007/2010及更高版本的(后缀名为.xlsx)<br/>
                        &nbsp;&nbsp;2. 需要保留第一行表头<br/>
                        &nbsp;&nbsp;3. 所上传的学校需要为字典表内学校，学校ID和学校名必须和系统中的学校信息一致。<br/>
                        &nbsp;&nbsp;4. 老师已经注册，且为快乐学模式老师，老师ID和老师姓名必须和系统中老师信息保持一致。<br/>
                        &nbsp;&nbsp;5. 老师和学校的关联关系需要确保正确。<br/>
                        &nbsp;&nbsp;6. 每次只能上传一所学校，且同一学校学生学号不重复。<br/>
                        &nbsp;&nbsp;7. 班内无该姓名学生时，会为其新注册账号；有该学生时，会更新该生学号信息。<br/>
                        &nbsp;&nbsp;8. 学号后N位在学校内不重复的情况下将作为学生阅卷机填涂号，如遇重复将随机生成N位数字（N位学校当前填涂号位数，一般是5位）<br/>
                    </label>
                    <br/>
                    <table border="1">
                        <tr>
                            <td width="50px">学校ID</td>
                            <td width="180px">学校名称</td>
                            <td width="100px">老师ID</td>
                            <td width="100px">老师姓名</td>
                            <td width="80px">年级</td>
                            <td width="90px">班级</td>
                            <td width="120px">学生姓名</td>
                            <td width="120px">学生学号</td>
                        </tr>
                        <tr>
                            <td>47869</td>
                            <td>北京市朝阳区第二外国语学院</td>
                            <td>156678097</td>
                            <td>李xx</td>
                            <td>8</td>
                            <td>15班</td>
                            <td>王五</td>
                            <td>0101010101</td>
                        </tr>
                        <tr>
                            <td>101010</td>
                            <td>北京市朝阳区第二外国语学院</td>
                            <td>15667900</td>
                            <td>王xx</td>
                            <td>9</td>
                            <td>10班</td>
                            <td>张潇潇</td>
                            <td>10101010</td>
                        </tr>
                    </table>
                    <br/>
                    <div class="controls">
                        <input type="file" name="sourceExcelFile">
                        <input  type="submit" value="上传" />
                        <input type="button" id="download_model" value="下载模板">
                        <div style="display: inline-block;margin-left: 10px;color: red;">注：最大仅支持2000条数据同时上传</div>
                    </div>
                </div>
            </form>
        </div>
        <#if record?? && (record?size > 0)>
            <div id="topRegisterDataTab" class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 50px;">日期</th>
                        <th class="sorting" style="width: 50px;">学校</th>
                        <th class="sorting" style="width: 50px;">备注</th>
                        <th class="sorting" style="width: 50px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list record?sort_by("createTime")?reverse as cancel>
                        <tr class="odd tbody01">
                            <td class="sorting_1">${cancel.createTime?string('yyyy-MM-dd HH:mm:ss')!}</td>
                            <td class="sorting_2">${cancel.schoolName!}</td>
                            <td class="sorting_3">${cancel.comments!}</td>
                            <td class="sorting_4"><a href="/workspace/import/download_klxstudents.vpage?recordId=${cancel.id}">下载学生名单</a></td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </#if>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        String.prototype.replaceAll = function(s1,s2) {
            return this.replace(new RegExp(s1,"gm"),s2);
        }
        $("#download_model").click(function(){
           window.location.href="/workspace/import/import_klxstudents_model.vpage";
        })
    });
</script>
</@layout_default.page>
