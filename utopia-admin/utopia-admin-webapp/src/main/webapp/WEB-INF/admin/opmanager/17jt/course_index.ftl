<#import "../../layout_default.ftl" as layout_default />
<#import "../../mizar/pager.ftl" as pager />
<@layout_default.page page_title="一起新讲堂" page_num=9>
<div id="main_container" class="span9">
    <legend>
        <strong>课程列表</strong>&nbsp;&nbsp;&nbsp;&nbsp;
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="query_frm" class="form-horizontal" method="get" action="index.vpage">
                    <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>
                    <input type="hidden" id="action" name="action" />
                    <ul class="inline">
                        <li>
                            标题：<input type="text" id="sid" style="width:140px;" name="name" value="<#if searchName??>${searchName}</#if>">
                        </li>
                        <li>
                            年级：<select name="grade" style="width:80px;">
                            <option value="">全部</option>
                            <option value="1">1年级</option>
                            <option value="2">2年级</option>
                            <option value="3">3年级</option>
                            <option value="4">4年级</option>
                            <option value="5">5年级</option>
                            <option value="6">6年级</option>
                        </select>
                        </li>
                        <li>
                            学科：<select name="subject" style="width:80px;">
                            <option value="">全部</option>
                            <option value="101" <#if searchSubject == 101>selected</#if>>语文</option>
                            <option value="102" <#if searchSubject == 102>selected</#if>>数学</option>
                            <option value="103" <#if searchSubject == 103>selected</#if>>英语</option>
                        </select>
                        </li>

                        <li>
                            状态：<select name="status" style="width:80px;">
                            <option value=0 <#if searchOnline == 0>selected</#if>>全部</option>
                            <option value=1 <#if searchOnline == 1>selected</#if>>上线</option>
                            <option value=2 <#if searchOnline == 2>selected</#if>>下线</option>
                        </select>
                        </li>
                    </ul>
                    <ul class="inline">

                        <li>
                            <button type="button" id="filter" class="btn btn-primary">
                                <i class="icon-search icon-white"></i> 查询
                            </button>
                        </li>
                        <li>
                        <#--<button type="button" id="filter" class="btn btn-info">
                            <i class="icon-plus icon-white"></i> 添加
                        </button>-->
                            <a type="button" class="btn btn-info" href="./info.vpage"><i
                                    class="icon-plus icon-white"></i> 添加</a>
                        </li>
                    </ul>
                </form>
                <@pager.pager/>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th style="text-align: center; width: 55px;">ID</th>
                        <th style="text-align: center; width: 100px;">题图</th>
                        <th style="text-align: center;width: 150px;">文章标题</th>
                        <th style="text-align: center; width: 50px;">发布/修改时间</th>
                        <th style="text-align: center; width: 80px;">学科</th>
                        <th style="text-align: center; width: 80px;">年级</th>
                        <th style="text-align: center; width: 50px;">标签</th>
                        <th style="text-align: center; width: 50px;">状态</th>
                        <th style="text-align: center; width: 50px;">展示在首页</th>
                        <th style="text-align: center; width: 50px;">置顶排序</th>
                        <th style="text-align: center; width: 100px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if resourceList?? && resourceList?has_content>
                            <#list resourceList as resource>
                            <tr>
                                <td>${resource.id!'0'}</td>
                                <td style="text-align: center;"><img style="width:210px;height:140px;" width="210"
                                                                     height="140" src="${resource.titlePictureUrl!'0'}"></td>
                                <td>${resource.title!''}</td>
                                <td style="text-align:center">${resource.updateDatetime!''}</td>
                                <td>${resource.subjectNames!''}</td>
                                <td style="text-align: center;">${resource.gradeNames!''}</td>
                                <td style="text-align: center;">${resource.label!''}</td>
                                <td style="text-align: center;"><#if resource.status?? && resource.status!=2>上线<#else>
                                    下线</#if></td>
                                <td style="text-align: center;"><#if resource.featuring?? && resource.featuring >展示<#else>不展示</#if></td>
                                <td>${resource.topNum!''}</td>
                                <td style="text-align: center;width:120px;">
                                    <a href="javascript:void(0);" class="btn btn-info" onclick="openUrl(${resource.id!'0'})">
                                        <i class=""></i> 预 览
                                    </a>
                                    <a href="info.vpage?id=${resource.id}" class="btn btn-info">
                                        <i class=""></i> 编 辑
                                    </a>
                                    <#if resource.status?? && resource.status==2>
                                        <button id="online-btn" class="btn btn-success">
                                            <i class=""></i> 上线
                                        </button>
                                    <#else>
                                        <button id="offline-btn" class="btn btn-danger">
                                            <i class=""></i> 下线
                                        </button>
                                    </#if>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="8" style="text-align: center;"><strong>No Data Found</strong></td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <@pager.pager/>
            </div>
        </div>
    </div>

    <div id="cache-data-dialog" class="modal fade hide" aria-hidden="true" style="display:none;left:40%;width:960px;overflow: scroll">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 class="modal-title">缓存数据</h3>
            </div>
            <div class="modal-body" style="height: auto; overflow: visible;">

            </div>
        </div>
    </div>


</div>
<script>
    $(function () {
        $("button#offline-btn").on('click', function () {
            if (!confirm('确定要下线选中的课程？')) {
                return false;
            }
            var courseId = $(this).parent().siblings().eq(0).html();
            $.post("./offline.vpage", {courseId: courseId}, function () {
                window.location.reload();
            });
        });

        $("button#online-btn").on('click', function () {
            if (!confirm('确定要上线选中的课程？')) {
                return false;
            }
            var courseId = $(this).parent().siblings().eq(0).html();
            $.post("./online.vpage", {courseId: courseId}, function (data) {
                if (data.success) {
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });

        $("a[name=test-btn]").click(function () {
            $.get("./get_cache_preview.vpage", {teacherId: $("input#teacher-id").val()}, function (data) {
                $("#cache-data-dialog div.modal-body").html('');

                data.result.map(function (item) {
                    $("#cache-data-dialog div.modal-body").append("<div>" + item + "</div>");
                });

                $("#cache-data-dialog").modal("show");
            });
        });

        $("#filter").click(function(){
            //$("#query_frm").submit();
            pagePost(1);
        });

        var initGradeValue = '${searchGrade!''}';
        setTimeout(function(){
            $("select[name=grade]").val(initGradeValue);
        },200);

        var initSubjectValue = '${searchSubject!''}';
        setTimeout(function(){
            $("select[name=subject]").val(initSubjectValue);
        },200);

    });
    function openUrl(id) {
        var url = "${(ProductConfig.getMainSiteBaseUrl())!''}//view/mobile/teacher/activity/newforum/detail?forum_index=" + id + "&share=true";
        window.open(url, '_top ', 'width=375,height=667')
    }
</script>
</@layout_default.page>