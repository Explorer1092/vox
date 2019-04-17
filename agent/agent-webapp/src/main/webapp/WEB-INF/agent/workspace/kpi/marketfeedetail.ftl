<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的工作台' page_num=1>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-info"></i>市场支持费用审核</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered" id="DataTables_Table_0"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                        <tr>
                            <th class="sorting" style="width: 100px;">用户姓名</th>
                            <th class="sorting">指标名称</th>
                            <th class="sorting" style="width: 100px;">负责地区</th>
                            <th class="sorting" style="width: 130px;">考核期间</th>
                            <th class="sorting" style="width: 60px;">目标</th>
                            <th class="sorting" style="width: 60px;">实绩</th>
                            <th class="sorting" style="width: 60px;">现金奖励</th>
                            <th class="sorting" style="width: 60px;">点数奖励</th>
                            <th class="sorting" style="width: 100px;">全国总监确认</th>
                            <th class="sorting" style="width: 100px;">财务确认</th>
                        </tr>
                    </thead>

                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if marketfeeData??>
                            <#list marketfeeData?keys as userId>
                            <tr class="odd">
                                <td class="center sorting_1" rowspan="${marketfeeData[userId]?size}">
                                    ${users[userId].realName!}
                                </td>
                                <#list marketfeeData[userId] as user>
                                    <#if user_index == 0>
                                        <td>
                                            ${user.kpiName!}
                                        </td>
                                        <td id="cityName">
                                            ${user.regionName!}
                                        </td>
                                        <td id="evalDate">
                                            <#if user.evalDuration??>${user.evalDuration!}</#if>
                                        </td>
                                        <td>
                                            <#if user.kpiTarget?string == "-1">
                                                -
                                            <#else>
                                            ${user.kpiTarget!}
                                            </#if>
                                        </td>
                                        <td>
                                            ${user.kpiResult!}
                                        </td>
                                        <td <#if user.dataType == 'summary'>class="green" </#if>>
                                            ${user.cashReward?string(",##0.##")}
                                        </td>
                                        <td <#if user.dataType == 'summary'>class="green" </#if>>
                                            ${user.pointReward?string(",##0.##")}
                                        </td>
                                        <td>
                                            <#if user.dataType == 'summary'|| user.dataType == 'citydata'>
                                                ${user.countryCheck!}
                                            <#else>
                                                ${user.countryCheck?string("已确认","未确认")}
                                            </#if>
                                        </td>
                                        <td>
                                            <#if user.dataType == 'summary'|| user.dataType == 'citydata'>
                                                ${user.financeCheck!}
                                            <#else>
                                                ${user.financeCheck?string("已确认","未确认")}
                                            </#if>
                                        </td>
                                    </#if>
                                </#list>
                            </tr>
                                <#if marketfeeData[userId]?size gt 1>
                                    <#list marketfeeData[userId] as user>

                                        <#if user_index gt 0>
                                        <tr class="odd">
                                            <td>
                                                ${user.kpiName!}
                                            </td>
                                            <td id="cityName">
                                                ${user.regionName!}
                                            </td>
                                            <td id="evalDate">
                                                <#if user.evalDuration??>${user.evalDuration!}</#if>
                                            </td>
                                            <td>
                                                <#if user.kpiTarget?string == "-1">
                                                    -
                                                <#else>
                                                    ${user.kpiTarget!}
                                                </#if>
                                            </td>
                                            <td>
                                                ${user.kpiResult!}
                                            </td>
                                            <td <#if user.dataType == 'summary'>class="green" </#if> >
                                                ${user.cashReward?string(",##0.##")}
                                            </td>
                                            <td <#if user.dataType == 'summary'>class="green" </#if> >
                                                ${user.pointReward?string(",##0.##")}
                                            </td>
                                            <td>
                                                <#if user.dataType == 'summary' || user.dataType == 'citydata'>
                                                    ${user.countryCheck!}
                                                <#else>
                                                    ${user.countryCheck?string("已确认","未确认")}
                                                </#if>
                                            </td>
                                            <td>
                                                <#if user.dataType == 'summary' || user.dataType == 'citydata'>
                                                    ${user.financeCheck!}
                                                <#else>
                                                    ${user.financeCheck?string("已确认","未确认")}
                                                </#if>
                                            </td>
                                        </tr>
                                        </#if>
                                    </#list>
                                </#if>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $('#finance_confirm').live('click',function(){

            if(!confirm("确定要审核通过所有条目?")){
                return false;
            }
            $.post('financeconfirm.vpage',{
                evalDate: $('#evalDate').html().trim()
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });

        $('#country_confirm').live('click',function(){

            if(!confirm("确定要审核通过所有条目?")){
                return false;
            }
            $.post('managerconfirm.vpage',{
                evalDate: $('#evalDate').html().trim()
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });
    });
</script>
</@layout_default.page>
