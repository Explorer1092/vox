<#-- @ftlvariable name="conditionMap" type="java.util.Map" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<style>
    .table_soll{ overflow-y:hidden; overflow-x: auto;}
    .table_soll table td,.table_soll table th{white-space: nowrap;}
</style>
<div id="main_container" class="span9">

    <div>
        <form method="post" action="?" class="form-horizontal">
            <fieldset>
                <legend>老师查询</legend>
                <ul class="inline">
                    <li>
                        <label for="authenticationState">
                            认证相关
                            <select id="authenticationState" name="authenticationState" class="multiple" style="width: 260px;">
                                <option value="2">符合前置认证条件但未自动认证通过</option>
                                <option value="4">系统自动认证</option>
                            </select>
                        </label>
                    </li>
                    <li class="queryDate">
                        <label for="startDate">
                            起始时间
                            <input name="startDate" id="startDate" type="text" placeholder="格式：1985-05-12"/>
                        </label>
                    </li>
                    <li class="queryDate">
                        <label for="endDate">
                            截止时间
                            <input name="endDate" id="endDate" type="text" placeholder="格式：1985-05-12"/>
                        </label>
                    </li>
                </ul>
                <ul class="inline">
                    <li class="queryForApplication">
                        <label>用户ID:<input name="userId" type="text" value="${(conditionMap.userId)!}"/></label>
                    </li>
                    <li class="queryForApplication">
                        <label>区域编码:<input name="regionCode" type="text" value="${(conditionMap.regionCode)!}"/></label>
                    </li>
                </ul>
                <ul class="inline">
                    <li class="queryForApplication">
                        <label>按时间由早到晚：<input type="checkbox" name="reverseOrder"
                            <#if (conditionMap.reverseOrder)?? && conditionMap.reverseOrder = 'on'>checked</#if> /></label>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <button id='submit' type="submit" class="btn btn-primary">查 询</button>
                        <button id='exportData' type="button" class="btn btn-primary">导出满足前置未认证成功列表</button>
                    </li>
                    <li>
                        <#if isAuthApplication??>
                            <input id="deleteApplication" type="button" class="btn btn-danger" value="删除认证申请" />
                        </#if>
                    </li>
                    <li>
                        <input id="currentPage" name="currentPage" type="hidden"/>
                        <input id="totalPage" name="totalPage" type="hidden"/>
                    </li>
                </ul>
                <br/>
            </fieldset>
        </form>
    </div>

    <#if teacherList?has_content>
        <ul class="inline">
            <li>
                <a id='first_page' href="javascript:void(0)">首页</a>
            </li>
            <li>
                <a id='pre_page' href="javascript:void(0)">上一页</a>
            </li>
            <li>
                <a>${conditionMap.currentPage}/${conditionMap.totalPage}</a>
            </li>
            <li>
                <a id='next_page' href="javascript:void(0)">下一页</a>
            </li>
            <li>
                <a id='last_page' href="javascript:void(0)">末页</a>
            </li>
        </ul>
        <div class="table_soll">
            <table id="teachers" class="table table-hover table-striped table-bordered">
                <tr>
                    <#if isAuthApplication??><td></td></#if>
                    <th>ID</th>
                    <th>姓名</th>
                    <th>进线日志数目</th>
                    <th>手机</th>
                    <th>邮箱</th>
                    <th>学科</th>
                    <th>学校</th>
                    <th>学校类型</th>
                    <th>省市区</th>
                    <th>园丁豆</th>
                    <th>是否认证</th>
                    <th>申请状态更新时间</th>
                </tr>

                <#list teacherList as teacher>
                    <tr>
                        <#if isAuthApplication??>
                            <td>
                                <input type="checkbox" name="chkDelAuthApp" id="${teacher.teacherId!""}" tname="${teacher.teacherName!''}" />
                            </td>
                        </#if>
                        <td>${teacher.teacherId!""}</td>
                        <td><a href="teacherhomepage.vpage?teacherId=${teacher.teacherId!""}">${teacher.teacherName!''}</a></td>
                        <td>${teacher.customerServiceRecordCount!'0'}</td>
                        <td>${teacher.teacherMobile!''}</td>
                        <td>${teacher.teacherEmail!''}</td>
                        <td>${(teacher.subjectName)!''}</td>
                        <td><a href="../school/schoolhomepage.vpage?schoolId=${teacher.schoolId!''}">${teacher.schoolName!''}</a>（${teacher.schoolId!''}）</td>
                        <td>${teacher.schoolType!''}</td>
                        <td>${teacher.regionName!''}（${teacher.regionCode!''}）</td>
                        <td><a href="../integral/integraldetail.vpage?userId=${teacher.teacherId!""}">${teacher.integral!''}</a></td>
                        <td>${teacher.verifiedState!''}</td>
                        <td>${teacher.applyDate!}</td>
                    </tr>
                </#list>
            </table>
        </div>
    </#if>
</div>
<div id="deleteApplicationModal" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>删除认证申请</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>老师ID</dt>
                    <dd id="teachers"></dd>
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
                    <dt>问题描述</dt>
                    <dd><textarea id="delDesc" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>所做操作</dt>
                    <dd>管理员删除认证申请</dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="btnDelAuthApp" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
<script>

    $(function(){

        $("#startDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $("#endDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

    });

    $(function(){

        var $authenticationState = $('#authenticationState');

        <#if teacherList?has_content>
            var $currentPage = $('#currentPage');
            var $totalPage = $('#totalPage');
            var $submit = $('#submit');
            $('#first_page').on('click', function(){
                <#if conditionMap.currentPage?number != 0 && conditionMap.currentPage?number != 1>
                    $currentPage.val(1);
                    $totalPage.val('${conditionMap.totalPage}');
                    $submit.trigger('click');
                    $currentPage.val('');
                    $totalPage.val('');
                </#if>
            });

            $('#pre_page').on('click', function(){
                <#if conditionMap.currentPage?number gt 1>
                    $currentPage.val('${conditionMap.currentPage?number - 1}');
                    $totalPage.val('${conditionMap.totalPage}');
                    $submit.trigger('click');
                    $currentPage.val('');
                    $totalPage.val('');
                </#if>
            });

            $('#next_page').on('click', function(){
                <#if conditionMap.currentPage?number lt conditionMap.totalPage?number>
                    $currentPage.val('${conditionMap.currentPage?number + 1}');
                    $totalPage.val('${conditionMap.totalPage}');
                    $submit.trigger('click');
                    $currentPage.val('');
                    $totalPage.val('');
                </#if>
            });

            $('#last_page').on('click', function(){
                <#if conditionMap.totalPage?number gt 1 && conditionMap.currentPage?number != conditionMap.totalPage?number>
                    $currentPage.val('${conditionMap.totalPage}');
                    $totalPage.val('${conditionMap.totalPage}');
                    $submit.trigger('click');
                    $currentPage.val('');
                    $totalPage.val('');
                </#if>
            });
        </#if>

        $authenticationState.on('change', function() {
            var selfValue = $(this).val();
            if(selfValue == '1') {
                $('.queryDate').show();
                $('.queryForApplication').show();
            } else if(selfValue == '4') {
                $('.queryDate').show();
                $('.queryForApplication').hide();
            } else {
                $('.queryDate').hide();
                $('.queryForApplication').hide();
            }

        });

        <#if conditionMap?has_content>
            $authenticationState.val('${(conditionMap.authenticationState?html)!''}');
            $authenticationState.trigger('change');
            $('#startDate').val('${conditionMap.startDate!''}');
            $('#endDate').val('${conditionMap.endDate!''}');
        </#if>

        $('#deleteApplication').click(function(){
            var dialog = $('#deleteApplicationModal');
            var teachers = getSelectedTeacher();
            if(teachers.length == 0){
                return false;
            }
            var ids='';
            for(var i=0;i<teachers.length;i++){
                ids += teachers[i].name+'['+teachers[i].id+']&nbsp;';
            }
            dialog.find('#teachers').html(ids);
            dialog.modal('show');
        });

        $('#btnDelAuthApp').click(function(){
            if($('#delDesc').val() == ''){
                alert('请输入问题描述');
                return false;
            }
            if(confirm('确认删除吗?')){
                var teachers = getSelectedTeacher();
                var ids =[];
                for(var i=0;i<teachers.length;i++){
                    ids.push(teachers[i].id);
                }
                $.post('delauthapplication.vpage',
                        {
                            teacherIds:ids.join(','),
                            delDesc : $('#delDesc').val()
                        },function(data){
                            if(data.success == false){
                                alert(data.info);
                            }
                           $('#submit').trigger('click');
                        });
            }
        });

        var getSelectedTeacher = function(){
            var teachers=[];
            $('input[name="chkDelAuthApp"]').each(function(i){
                if($(this).is(':checked')){
                    var teacher = {id:$(this).attr('id'),name:$(this).attr('tname')};
                    teachers.push(teacher);
                }
            });
            return teachers;
        };

        $('#exportData').on('click', function() {
            location.href = "downloadpremiseteacher.vpage";
        });

    });
</script>
</@layout_default.page>