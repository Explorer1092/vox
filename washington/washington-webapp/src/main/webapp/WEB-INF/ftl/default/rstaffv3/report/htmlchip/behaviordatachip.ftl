<div class="r-table">
    <table>
        <thead>
        <tr class="blue">
            <td rowspan="3" style="width: 8%"><#if currentUser.isResearchStaffForProvince()>市<#else>区</#if></td>
            <td rowspan="3"><#if currentUser.isResearchStaffForProvince()>区<#else>学校</#if></td>
            <td colspan="2">认证老师</td>
            <td colspan="2">认证学生</td>
        <#if !currentUser.isResearchStaffForCounty()>
            <td rowspan="3" style="width: 8%">操作</td>
        </#if>
        </tr>
        <tr class="blue">
            <td colspan="2">练习</td>
            <td colspan="2">练习</td>
        </tr>
        <tr class="blue">
            <td style="width: 8%">人数</td>
            <td style="width: 8%">人次</td>
            <td style="width: 8%">人数</td>
            <td style="width: 8%">人次</td>
        </tr>
        </thead>
    </table>
    <div class="allAreaContainer">
    <#if behaviorList?has_content && behaviorList?size gt 0>
        <#if summaryMapper?has_content && (currentUser.isResearchStaffForCity() || currentUser.isResearchStaffForProvince())>
            <table>
                <tbody>
                <tr>
                    <td style="width: 8%">${behaviorList?size}</td>
                    <td>${summaryMapper.schoolNum}</td>
                    <td style="width: 8%">${summaryMapper.homeworkTeacherNum}</td>
                    <td style="width: 8%">${summaryMapper.homeworkTeacherTime}</td>
                    <td style="width: 8%">${summaryMapper.homeworkStuNum}</td>
                    <td style="width: 8%">${summaryMapper.homeworkStuTime}</td>
                    <td style="width: 8%"></td>
                </tr>
                </tbody>
            </table>
        </#if>
        <#list behaviorList as behavior>
            <table class="area-${behavior_index}">
                <thead>
                <tr class="blue">
                    <td style="width: 8%">${behavior.name}</td>
                    <td><#if behavior.childBehaviorData?has_content && behavior.childBehaviorData?size gt 0>${behavior.childBehaviorData?size}<#else>0</#if></td>
                    <td style="width: 8%">${behavior.homeworkTeacherNum}</td>
                    <td style="width: 8%">${behavior.homeworkTeacherTime}</td>
                    <td style="width: 8%">${behavior.homeworkStuNum}</td>
                    <td style="width: 8%">${behavior.homeworkStuTime}</td>
                    <#if !currentUser.isResearchStaffForCounty()> <td style="width: 8%;cursor: pointer;" data-current="hide" class="showOrHide">展开</td> </#if>
                </tr>
                </thead>
                <tbody class="area-${behavior_index}-child" <#if !currentUser.isResearchStaffForCounty()> style="display: none;" </#if>>
                    <#if behavior.childBehaviorData?has_content && behavior.childBehaviorData?size gt 0>
                        <#list behavior.childBehaviorData as childData>
                        <tr>
                            <td></td>
                            <td>${childData.name}</td>
                            <td>${childData.homeworkTeacherNum}</td>
                            <td>${childData.homeworkTeacherTime}</td>
                            <td>${childData.homeworkStuNum}</td>
                            <td>${childData.homeworkStuTime}</td>
                            <#if !currentUser.isResearchStaffForCounty()><td></td></#if>
                        </tr>
                        </#list>
                    </#if>
                </tbody>
            </table>
        </#list>
    <#else>
        <table>
            <tbody>
            <tr>
                <td colspan="8">暂无相关数据</td>
            </tr>
            </tbody>
        </table>
    </#if>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        var AreaOperate = {
            init : function(){
                //单元格展开或收起
                $("td.showOrHide").on("click",function(){
                    var $this = $(this);
                    var $table = $this.closest("table");
                    var targetTBody = $table.find("tbody");
                    //折叠状态
                    if($this.attr("data-current") == "hide"){
                        targetTBody.show();
                        $this.text("收起");
                        $this.attr("data-current","show");
                    }else{
                        $this.text("展开");
                        $this.attr("data-current","hide");
                        targetTBody.hide();
                    }
                });
            }
        };
        AreaOperate.init();
    });
</script>