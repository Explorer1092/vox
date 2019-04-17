<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="公众号管理平台" page_num=17>
<#import "../pager.ftl" as pager />
<div id="main_container" class="span9">
    <legend>
        <strong>中间页课程管理</strong>
        <a id="add_advertiser_btn" href="coursedetail.vpage?mode=${mode!}" type="button" class="btn btn-info" style="float: right">添加课程</a>
    </legend>
    <form id="activity-query" class="form-horizontal" method="get" action="${requestContext.webAppContextPath}/mizar/course/index.vpage" >
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
    </form>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="query_frm" class="form-horizontal" method="get" action="index.vpage">
                    <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>
                    <ul class="inline">
                        <li>
                            课程名称：<input type="text" id="title" name="title" value="<#if title??>${title}</#if>" placeholder="输入课程名称">
                        </li>
                        <#if mode?? && mode == 'mc'>
                            <li>
                                课程类别：<select id="category" name="category">
                                    <option value="MICRO_COURSE_OPENING" <#if 'MICRO_COURSE_OPENING'==(category)!>selected</#if>>微课堂-公开课</option>
                                    <option value="MICRO_COURSE_NORMAL" <#if 'MICRO_COURSE_NORMAL'==(category)!>selected</#if>>微课堂-长期课</option>
                            </select>
                            </li>
                        <#else>
                        <li>
                            课程类别：<select id="category" name="category">
                                        <option value="">全部</option>
                                        <#list categorys as c>
                                            <option value="${c.name()!}" <#if category?? && c.name() == category>selected</#if> >${c.getDesc()!}</option>
                                        </#list>
                                    </select>
                        </li>
                        </#if>
                        <li>
                            状态：<select id="status" name="status">
                            <option value="">全部</option>
                            <option value="ONLINE" <#if status?? && status == 'ONLINE'>selected</#if> >上线</option>
                            <option value="OFFLINE" <#if status?? && status == 'OFFLINE'>selected</#if> >下线</option>
                            </select>
                        </li>
                        <li>
                            <button type="submit" id="filter" class="btn btn-primary">
                                <i class="icon-search icon-white"></i> 查  询
                            </button>
                        </li>
                    </ul>
                </form>
                <@pager.pager/>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <#--<th width="200">ID</th>-->
                        <th width="300px;"><#if mode?? && mode == 'mc'>微课堂课时<#else>标题 / 微课堂课时</#if></th>
                        <th width="200px;">创建时间</th>
                        <th>描述</th>
                        <th width="80px;">状态</th>
                        <th width="100px;">类别</th>
                        <th width="120px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if coursePage?? && coursePage.content?? >
                            <#list coursePage.content as course >
                            <tr>
                                <#--<td><pre>${course.id!}</pre></td>-->
                                <td><pre>${course.title!}</pre></td>
                                <td>${course.createAt?string('yyyy-MM-dd HH:mm:ss')}</td>
                                <td><pre>${course.description!}</pre></td>
                                <td>
                                    <#switch course.status>
                                        <#case "ONLINE">
                                            上线
                                            <#break>
                                        <#case "OFFLINE">
                                            下线
                                            <#break>
                                        <#default>
                                    </#switch>
                                </td>
                                <td>
                                    <#switch course.category>
                                        <#case "GOOD_COURSE">
                                            好课试听
                                            <#break>
                                        <#case "DAY_COURSE">
                                            每日一课
                                            <#break>
                                        <#case "VIDEO_COURSE">
                                            精品视频课程
                                            <#break>
                                        <#case "PARENTAL_ACTIVITY">
                                            亲子活动
                                            <#break>
                                        <#case "OPEN_LIVE_COURSE">
                                            公开课
                                            <#break>
                                        <#case "NORMAL_LIVE_COURSE">
                                            长期课
                                            <#break>
                                        <#case "MICRO_COURSE_OPENING">
                                            微课堂-公开课
                                            <#break>
                                        <#case "MICRO_COURSE_NORMAL">
                                            微课堂-长期课
                                            <#break>
                                        <#default>
                                    </#switch>
                                </td>
                                <td>
                                    <a href="coursedetail.vpage?courseId=${course.id!''}">编辑</a>
                                    <a href="javascript:void(0)" data-id="${course.id!''}" class="delete-btn">删除</a>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
                <@pager.pager/>
            </div>
        </div>
    </div>
</div>
<style>
    .table td , .table th{
        padding: 8px;
        line-height: 20px;
        text-align: center;
        vertical-align: middle;
        border-top: 1px solid #dddddd;
    }
</style>
<script type="text/javascript">
    $(function(){
        $(document).on('click','.delete-btn',function(){
            if(!confirm("确定删除该课程吗？")){
                return;
            }
            var $this=$(this);
            $.post('deletecourse.vpage',{courseId:$this.attr("data-id")},function(res){
                if(res.success){
                    window.location.reload();
                }else{
                    alert(res.info);
                }
            });
        });
    });
</script>
</@layout_default.page>