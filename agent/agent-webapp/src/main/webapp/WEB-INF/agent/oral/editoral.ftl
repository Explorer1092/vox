<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='添加系统权限' page_num=8>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<#--<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>-->
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<style type="text/css">
    select.s_time{
        width: 60px;
    }
</style>
    <#macro forOption start=0 end=0 defaultVal="00">
        <#list start..end as index>
        <#if index lt 10>
            <#assign actValue="0${index}"/>
        <#else>
            <#assign actValue="${index}"/>
        </#if>
        <option value="${actValue}" <#if defaultVal == actValue>selected</#if>>${actValue}</option>
        </#list>
    </#macro>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 编辑统考测评</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <#if oralMapper?has_content>
                <form class="form-horizontal" method="POST">
                    <fieldset>
                        <div class="control-group">
                            <label for="date01" class="control-label">测试开始时间</label>
                            <div class="controls">
                                <input type="text" value="${oralMapper.beginDate}" id="beginDate" readonly="readonly" placeholder="格式：2015-11-04" class="input-xlarge">
                                <select class="s_time" name="startHour" id="startHour">
                                    <@forOption start=0 end=23 defaultVal="${(oralMapper.startHour)!'00'}" />
                                </select> 时
                                <select class="s_time" name="startMin" id="startMin">
                                    <@forOption start=0 end=59 defaultVal="${(oralMapper.startMin)!'00'}" />
                                </select> 分

                            </div>
                        </div>
                        <div class="control-group">
                            <label for="date02" class="control-label">测试结束时间</label>
                            <div class="controls">
                                <input type="text" value="${oralMapper.endDate}" readonly="readonly" id="endDate" placeholder="格式：2015-11-04" class="input-xlarge">
                                <select class="s_time" name="endHour" id="endHour">
                                    <@forOption start=0 end=23 defaultVal="${(oralMapper.endHour)!'00'}" />
                                </select> 时
                                <select class="s_time" name="endMin" id="endMin">
                                    <@forOption start=0 end=59 defaultVal="${(oralMapper.endMin)!'00'}" />
                                </select> 分
                            </div>
                        </div>
                        <div class="control-group">
                            <label for="date02" class="control-label">老师批改结束时间</label>
                            <div class="controls">
                                <input type="text" value="${oralMapper.correctStopDate}" readonly="readonly" id="correctStopDate" placeholder="格式：2015-11-04" class="input-xlarge">
                                <select class="s_time" name="correctStopHour" id="correctStopHour">
                                    <@forOption start=0 end=23 defaultVal="${(oralMapper.correctStopHour)!'00'}" />
                                </select> 时
                                <select class="s_time" name="correctStopMin" id="correctStopMin">
                                    <@forOption start=0 end=59 defaultVal="${(oralMapper.correctStopMin)!'00'}" />
                                </select> 分
                            </div>
                        </div>
                        <div class="control-group">
                            <label for="date02" class="control-label">成绩发布时间</label>
                            <div class="controls">
                                <input type="text" value="${oralMapper.resultIssueDate}" readonly="readonly" id="resultIssueDate" placeholder="格式：2015-11-04" class="input-xlarge">
                                <select class="s_time" name="resultIssueHour" id="resultIssueHour">
                                    <@forOption start=0 end=23 defaultVal="${(oralMapper.resultIssueHour)!'00'}" />
                                </select> 时
                                <select class="s_time" name="resultIssueMin" id="resultIssueMin">
                                    <@forOption start=0 end=59 defaultVal="${(oralMapper.resultIssueMin)!'00'}" />
                                </select> 分
                            </div>
                        </div>
                        <#if oralMapper.status == "NEW" || oralMapper.status == "ONLINE">
                            <div class="form-actions">
                                <button id="editOralBtn" type="button" class="btn btn-primary">保存</button>
                                <a class="btn" href="list.vpage"> 取消 </a>
                            </div>
                        </#if>
                    </fieldset>
                </form>
            <#else>
               未查询到相关数据(可能原因：数据已在录入中)
            </#if>
        </div>
    </div><!--/span-->
</div>
<script type="text/javascript">
    $(function(){
    <#---时间设置--->
        var defaultOptions = {
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : "${defaultDate}",
            minDate         : "${defaultDate}",
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        };
        $("#beginDate").datepicker(defaultOptions);
        $("#endDate").datepicker(defaultOptions);
        $("#correctStopDate").datepicker(defaultOptions);
        $("#resultIssueDate").datepicker(defaultOptions);

        <#---时间设置结束--->
        <#--待录入状态才可编辑-->
        <#if oralMapper?has_content && oralMapper.status == "NEW" || oralMapper.status == "ONLINE">
            $("#editOralBtn").on("click",function(){
                var $this = $(this);
                $.ajax({
                    type : 'post',
                    url : "editoraltime.vpage",
                    data : $.toJSON({
                        id              : "${oralMapper.id}",
                        beginDate       : $.trim($("#beginDate").val()),
                        startHour       : $("#startHour").val(),
                        startMin        : $("#startMin").val(),
                        endDate         : $.trim($("#endDate").val()),
                        endHour         : $("#endHour").val(),
                        endMin          : $("#endMin").val(),
                        correctStopDate : $.trim($("#correctStopDate").val()),
                        correctStopHour : $("#correctStopHour").val(),
                        correctStopMin  : $("#correctStopMin").val(),
                        resultIssueDate : $.trim($("#resultIssueDate").val()),
                        resultIssueHour : $("#resultIssueHour").val(),
                        resultIssueMin  : $("#resultIssueMin").val()
                    }),
                    success : function(data){
                        if(!data.success){
                            alert(data.info);
                        }else{
                            alert("修改成功");
                            window.location.href='list.vpage';
                        }
                    },
                    error : function(){
                        $this.removeClass("locked-btn");
                    },
                    dataType : "json",
                    contentType : 'application/json;charset=UTF-8',
                    cache : false
                });
            });
        </#if>
    });
</script>

</@layout_default.page>