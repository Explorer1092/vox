<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='积分活动管理' page_num=9>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">积分流通活动管理</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="activity-query" class="form-horizontal" method="post" action="${requestContext.webAppContextPath}/opmanager/integralactivity/activitypage.vpage" >
                    <ul class="inline">
                        <input type="hidden" id="pageNumber" name="pageNumber" value="1">
                        <li>
                            <a href="activityinfo.vpage?edit=true" type="button" class="btn btn-info">新建积分流通活动</a>
                        </li>
                        <br><br>
                        <li>
                            <label>活动归属部门&nbsp;
                                <select id="department" name="department">
                                    <option value=>全部部门</option>
                                    <#if departmentList??>
                                        <#list departmentList as dept>
                                            <option <#if department?? && dept.key == department> selected="selected" </#if> value="${dept.key!0}" > ${dept.value!}</option>
                                        </#list>
                                    </#if>
                                    <#--<option <#if department??><#if department==11>selected="selected" </#if></#if> value=11>产品部-平台</option>-->
                                    <#--<option <#if department??><#if department==12>selected="selected" </#if></#if> value=12>产品部-小学业务</option>-->
                                    <#--<option <#if department??><#if department==13>selected="selected" </#if></#if> value=13>产品部-中学业务</option>-->
                                    <#--<option <#if department??><#if department==21>selected="selected" </#if></#if> value=21>客服部</option>-->
                                    <#--<option <#if department??><#if department==31>selected="selected" </#if></#if> value=31>市场部</option>-->
                                </select>
                            </label>
                        </li>
                        <li>
                            <label>活动状态&nbsp;
                                <select id="status" name="status">
                                    <option value=>所有状态</option>
                                    <option <#if status??><#if status==1>selected="selected" </#if></#if> value=1>未发布</option>
                                    <option <#if status??><#if status==2>selected="selected" </#if></#if> value=2>未开始</option>
                                    <option <#if status??><#if status==3>selected="selected" </#if></#if> value=3>进行中</option>
                                    <option <#if status??><#if status==4>selected="selected" </#if></#if> value=4>已结束</option>
                                </select>
                            </label>
                        </li>
                        <li>
                            <button type="submit" id="filter" class="btn btn-primary">查询活动</button>
                        </li>
                    </ul>
                </form>
                <div id="data_table_journal">
                    <table class="table table-bordered table-striped">
                        <tr>
                            <td width="60px">活动ID</td>
                            <td width="150px">活动名称</td>
                            <td width="100px">活动归属部门</td>
                            <td width="100px">最后修改人</td>
                            <td width="150px">开始时间</td>
                            <td width="150px">结束时间</td>
                            <td width="80px">活动状态</td>
                            <td width="200px">操作</td>
                        </tr>
                        <#if activities??>
                            <#list activities as activity >
                                <tr <#switch activity.status>
                                    <#case 2>class="warning"<#break>
                                    <#case 3>class="success"<#break>
                                    <#case 4>class="error"<#break>
                                    <#default>
                                </#switch>>
                                    <td>${activity.id!}</td>
                                    <td><a href="activityinfo.vpage?id=${activity.id}&edit=false">${activity.activityName!}</a></td>
                                    <td>${activity.department.description!}</td>
                                    <td>${activity.creatorName!}</td>
                                    <td>${activity.startDate!'---'}</td>
                                    <td>${activity.endDate!'---'}</td>
                                    <td>${activity.statusDesc!}</td>
                                    <td>
                                        <#if activity.status==1>
                                            <button class="btn btn-success" onclick="changeStatus(2,${activity.id},'发布')">发布</button>
                                        </#if>
                                        <#if activity.status lt 3>
                                            <a href="activityinfo.vpage?id=${activity.id}&edit=true" class="btn btn-info" role="button">编辑</a>
                                        </#if>
                                        <#if activity.status lt 4>
                                            <button class="btn btn-danger" onclick="changeStatus(4,${activity.id},'结束')">结束</button>
                                        </#if>
                                    </td>
                                </tr>
                            </#list>
                        </#if>
                    </table>
                </div>
                <ul class="pager">
                    <#if hasPrev>
                        <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">&lt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&lt;</a></li>
                    </#if>
                    <li class="disabled"><a>第 ${pageNumber!} 页</a></li>
                    <li class="disabled"><a>共 <#if totalPages==0>1<#else>${totalPages!}</#if> 页</a></li>
                    <#if hasNext>
                        <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">&gt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&gt;</a></li>
                    </#if>
                </ul>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function () {

    });

    function pagePost(pageNumber) {
        $("#pageNumber").val(pageNumber);
        $("#activity-query").submit();
    }

    function changeStatus(status, activityId, action) {
        if (confirm("是否确认" + action + "此项活动？(ID:" + activityId + ")")) {
            $.ajax({
                type: "post",
                url: "changestatus.vpage",
                data: {
                    activityId: activityId,
                    status: status
                },
                success: function (data) {
                    if (data.success) {
                        alert("积分活动"+ action +"成功！");
                        window.location.href='activitypage.vpage';
                    } else {
                        alert("积分活动"+ action +"失败！");
                    }
                }
            });
        }
    }
</script>


</@layout_default.page>