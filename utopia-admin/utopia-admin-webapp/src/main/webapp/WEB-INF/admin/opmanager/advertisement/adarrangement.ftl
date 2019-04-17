<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="广告运营管理平台" page_num=9>
<div id="main_container" class="span9">
    <legend>
        <a href="adindex.vpage">广告管理</a>&nbsp;&nbsp;&nbsp;&nbsp;
        <a href="slotindex.vpage">广告位信息</a> &nbsp;&nbsp;&nbsp;&nbsp;
        <strong>广告排期管理</strong> &nbsp;&nbsp;&nbsp;&nbsp;
    </legend>

    <div class="btn-group" data-toggle="buttons-radio" style="margin-bottom: 10px;">
        <a href="/opmanager/advertisement/adarrangement.vpage?queryDate=${(queryDate?string("yyyy-MM-dd"))!(.now?string("yyyy-MM-dd"))}&week=pre" class="btn <#if (week == "pre")!false>active</#if>">上周</a>
        <input class="btn <#if (week?has_content)!false>active</#if>active" type="button" id="selectDate" value="${(queryDate?string("yyyy-MM-dd"))!(.now?string("yyyy-MM-dd"))}">
        <a href="/opmanager/advertisement/adarrangement.vpage?queryDate=${(queryDate?string("yyyy-MM-dd"))!(.now?string("yyyy-MM-dd"))}&week=next" class="btn <#if (week == "next")!false>active</#if>">下周</a>
    </div>

    <div class="row-fluid">
        <div class="span12">
            <table class="table table-bordered table-striped table-hover">
                <thead>
                    <tr>
                        <td style="width: 150px;">广告位名称(ID)</td>
                        <#assign dayHost = ['日', '一', '二', '三','四','五','六'] />
                        <#list result as st>
                            <#if st_index == 0>
                            <#list st.items as it>
                                <td>星期${dayHost[it_index]}<br/>${it.day}</td>
                            </#list>
                            </#if>
                        </#list>
                    </tr>
                </thead>
                <tbody>
                <#list result as st>
                    <tr>
                        <td>${st.title} <span style="display: inline-block;">(${st.id})</span></td>
                        <#list st.items as items>
                            <td>
                                <#if (items.list)?has_content>
                                <#list items.list as list>
                                    <a href="javascript:void(0);" style="background-color: #eee; margin: 3px 0; font-size: 12px; display: block; padding:5px 10px;" target="_blank">(${list.id!})${list.name!}</a>
                                </#list>
                                <#else>
                                    <div style="color: #999; text-align: center;">----</div>
                                </#if>
                            </td>
                        </#list>
                    </tr>
                </#list>
                 </tbody>
            </table>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $("#selectDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){
                location.href = "/opmanager/advertisement/adarrangement.vpage?queryDate=" + selectedDate;
            }
        });
    });
</script>
</@layout_default.page>