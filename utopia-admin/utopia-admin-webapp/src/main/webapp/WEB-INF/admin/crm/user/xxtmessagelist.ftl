<#-- @ftlvariable name="feedbacks" type="java.util.List<Map<String,Object>>" -->
<#-- @ftlvariable name="startDate" type="java.lang.String" -->

<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="校讯通消息" page_num=3>
<div class="span9">
    <div>
        <form method="get" action="?" class="form-horizontal">
            <fieldset><legend>校讯通消息</legend></fieldset>
            <ul class="inline form_datetime">
                <li>
                    <label for="userId">
                        老师ID
                        <input name="userId" value="${userId!''}">
                    </label>
                </li>
                <li>
                    <label for="userId">
                        处理状态
                        <select id="messageType" name="messageType">
                            <option value="1" <#if messageType == 1>selected </#if>>通知类</option>
                            <option value="2" <#if messageType == 2>selected </#if>>回执类</option>
                            <option value="3" <#if messageType == 3>selected </#if>>礼物问候类</option>
                            <option value="4" <#if messageType == 4>selected </#if>>图片</option>
                            <option value="5" <#if messageType == 5>selected </#if>>感恩节</option>
                            <option value="6" <#if messageType == 6>selected </#if>>作业报告留言</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="startDate">
                        起始时间
                        <input name="startDate" id="startDate" type="text" placeholder="格式：2013-11-04"/>
                    </label>
                </li>
                <li>
                    <label for="endDate">
                        截止时间
                        <input name="endDate" id="endDate" type="text" placeholder="格式：2013-11-04"/>
                    </label>
                </li>
                <li>
                    <button type="submit" class="btn btn-success">查询</button>
                </li>
            </ul>
        </form>
        <table class="table table-striped table-bordered" style="font-size: 14px;">
            <thead>
            <tr>
                <th style="width: 90px;max-width:90px;">ID号</th>
                <th>班级ID</th>
                <th>信息</th>
                <th>发送时间</th>
            </tr>
            </thead>
            <#if xxtMessageList?has_content>
                <#list xxtMessageList as xxtMessage>
                    <tr>
                        <td>${xxtMessage.getId()}</td>
                        <td>${xxtMessage.getClazzId()!}</td>
                        <td>${xxtMessage.getTextContent()!''}</td>
                        <td>${xxtMessage.getCreateDatetime()?datetime!''}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>

<script type="text/javascript" src="http://17zuoye.com/public/plugin/jquery-jmp3/jquery.jmp3.min.js"></script>
<script type="text/javascript">
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
        $('#startDate').val('${startDate!''}');
        $('#endDate').val('${endDate!''}');
    });

</script>
</@layout_default.page>
