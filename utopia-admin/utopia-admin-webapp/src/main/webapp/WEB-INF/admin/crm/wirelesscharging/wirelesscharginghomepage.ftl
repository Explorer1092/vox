<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="充值查询" page_num=3>
<div id="main_container" class="span9">
    <div>
        <form method="post" action="wirelesslist.vpage" class="form-horizontal">

            <legend>充值查询</legend>
            <span>起始时间不输入默认查询7天之内。如果输入手机号/ID，则不受起始时间限制</span>
            <hr/>
            <ul class="inline form_datetime">
                <li>
                    <label for="startDate">
                        起始时间
                        <input name="startDate" id="startDate" type="text" placeholder="格式：2014-11-26"/>
                    </label>
                </li>
                <li>
                    <label for="mobile">
                        手机号
                        <input name="mobile" id="mobile" type="text"/>
                    </label>
                </li>
                <li>
                    <label for="userId">
                        用户ID
                        <input name="userId" id="userId" type="text"/>
                    </label>
                </li>
                <li>
                    <label for="type">
                        充值状态
                        <select name="type" id="type">
                            <option value="-1">全部</option>
                            <option value="0">未提交</option>
                            <option value="1">提交</option>
                            <option value="2">接收成功</option>
                            <option value="3">充值成功</option>
                            <option value="9">充值失败</option>
                        </select>
                    </label>
                </li>
                <li>
                    <button type="submit" class="btn btn-primary">查询</button>
                </li>
            </ul>
        </form>
    </div>
    <br/>
    <div>
        <legend>查询结果：</legend>
        <table class="table table-hover table-striped table-bordered">
            <tr>
                <th>用户ID</th>
                <th> 充值手机</th>
                <th> 充值描述</th>
                <th> 金额</th>
                <th> 状态</th>
                <th> 充值时间</th>
            </tr>
            <#if datas?has_content>
                <#list datas as data>
                    <tr>
                        <td>${data.userId!}</td>
                        <td>${data.targetSensitiveMobile!""}</td>
                        <td>${data.chargeDesc!""}</td>
                        <td>${(data.amount/100)!"0"}元</td>
                        <td>
                            <#switch data.status>
                                <#case 0>
                                    未提交
                                    <#break>
                                <#case 1>
                                    提交
                                    <#break>
                                <#case 2>
                                    接收成功
                                    <#break>
                                <#case 3>
                                    充值成功
                                    <#break>
                                <#case 9>
                                    充值失败
                                    <#break>
                                <#default>
                                    <#break>
                            </#switch>
                        </td>
                        <td>
                            ${data.createDatetime?string('yyyy-MM-dd HH:mm:ss')}
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
<script type="text/javascript">
    <#if startDate?has_content>
        $('#startDate').val('${startDate!''}')
    </#if>
    <#if type?has_content>
        $('#type').val('${type!''}')
    </#if>
    <#if mobile?has_content>
        $('#mobile').val('${mobile!''}')
    </#if>
    <#if userId?has_content && userId lt 0>
        $('#userId').val('${userId!''}')
    </#if>
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
    });

</script>
</@layout_default.page>