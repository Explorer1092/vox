<#import "../../module.ftl" as module>
<#import "../../common/pager.ftl" as pager />
<@module.page
title="江西版-教学资源-视频"
leftMenu="视频"
>
<link  href="${ctx}/public/plugin/bootstrap-2.3.0/css/bootstrap.min.css" rel="stylesheet">
<script src="${ctx}/public/plugin/jquery/jquery-1.9.1.min.js"></script>
<script src="${ctx}/public/plugin/bootstrap-2.3.0/js/bootstrap.min.js"></script>

<style type="text/css">
    body { padding-bottom: 40px; background-color: #f5f5f5; }
    a, input, button, select{ outline:none !important;}
    .form-signin { max-width: 300px; padding: 19px 29px 29px; margin: 0 auto 20px; background-color: #fff; border: 1px solid #e5e5e5; -webkit-border-radius: 5px; -moz-border-radius: 5px; border-radius: 5px; -webkit-box-shadow: 0 1px 2px rgba(0,0,0,.05); -moz-box-shadow: 0 1px 2px rgba(0,0,0,.05); box-shadow: 0 1px 2px rgba(0,0,0,.05); }
    .form-signin .form-signin-heading,  .form-signin .checkbox { margin-bottom: 10px; }
    .form-signin input[type="text"],  .form-signin input[type="password"] { font-size: 16px; height: auto; margin-bottom: 15px; padding: 7px 9px; vertical-align: inherit;}
    .font-zh{font-family: '微软雅黑', 'Microsoft YaHei', Arial; font-weight: normal;}
    .table th, .table td{word-break:break-all; word-wrap:break-word;}
    .date { width: 6em; }
    .ui-dialog-titlebar-close { display: none;}
    .navbar-fixed-top {position: static!important; margin-bottom: 20px!important;}
    .operate-ul li{ margin-bottom: 10px; }
</style>

<div>
    <div class="op-wrapper orders-wrapper clearfix">
        <span class="title-h1">课程列表</span>
    </div>
    <div class="row-fluid">
        <form id="query_frm" class="form-horizontal" method="get" action="index.vpage">
            <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>
            <input type="hidden" id="action" name="action" />
            <ul class="inline operate-ul">
                <li>
                    标题：<input type="text" id="sid" style="width:354px;height: 30px;" name="name" value="<#if searchName??>${searchName}</#if>">
                </li>
                <br />
                <li>
                    类型：
                    <select name="category" style="width:80px;">
                        <option value="">全部</option>
                        <option value="IMPORTANT_CASE" <#if searchCategory == 'IMPORTANT_CASE'>selected</#if>>关键课例</option>
                        <option value="GROW_UP" <#if searchCategory == 'GROW_UP'>selected</#if>>成长心语</option>
                        <option value="ACTIVITY_NOTICE" <#if searchCategory == 'ACTIVITY_NOTICE'>selected</#if>>活动公告</option>
                        <option value="OTHER_STONE" <#if searchCategory == 'OTHER_STONE'>selected</#if>>他山之石</option>
                    </select>
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
                    状态：<select name="status" style="width:80px;">
                    <option value=0 <#if searchOnline == 0>selected</#if>>全部</option>
                    <option value=1 <#if searchOnline == 1>selected</#if>>上线</option>
                    <option value=2 <#if searchOnline == 2>selected</#if>>下线</option>
                </select>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <button type="button" id="filter" class="btn btn-success">
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
        <#--<@pager.pager/>-->
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>ID</th>
                <th style="width: 85px;">题图</th>
                <th style="width: 90px;">文章标题</th>
                <th>发布/修改时间</th>
                <#--<th>学科</th>-->
                <th style="width: 112px;">年级</th>
                <th style="width: 112px;">类型</th>
                <#--<th>标签</th>-->
                <th>状态</th>
                <#--<th>展示在首页</th>-->
                <#--<th>置顶排序</th>-->
                <th style="width: 58px;">操作</th>
            </tr>
            </thead>
            <tbody>
                <#if resourceList?? && resourceList?has_content>
                    <#list resourceList as resource>
                    <tr>
                        <td>${resource.id!'0'}</td>
                        <td style="text-align: center;"><img src="${resource.titlePictureUrl!'0'}"></td>
                        <td>${resource.title!''}</td>
                        <td style="text-align:center">${resource.updateDatetime!''}</td>
                        <#--<td>${resource.subjectNames!''}</td>-->
                        <td>${resource.gradeNames!''}</td>
                        <td>${resource.categoryName!''}</td>
                        <#--<td>${resource.label!''}</td>-->
                        <td><#if resource.status?? && resource.status!=2>上线<#else>下线</#if></td>
                        <#--<td><#if resource.featuring?? && resource.featuring >展示<#else>不展示</#if></td>-->
                        <#--<td>${resource.topNum!''}</td>-->
                        <td style="text-align: center;">
                            <a href="javascript:void(0);" class="btn btn-info" onclick="openUrl(${resource.id!'0'})">
                                <i class=""></i> 预 览
                            </a>
                            <a href="info.vpage?id=${resource.id}" class="btn btn-info">
                                <i class=""></i> 编 辑
                            </a>
                            <#if resource.status?? && resource.status==2>
                                <button id="online-btn" class="btn btn-success">
                                    <i class=""></i> 上 线
                                </button>
                            <#else>
                                <button id="offline-btn" class="btn btn-danger">
                                    <i class=""></i> 下 线
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

        <#--var initSubjectValue = '${searchSubject!''}';-->
        <#--setTimeout(function(){-->
            <#--$("select[name=subject]").val(initSubjectValue);-->
        <#--},200);-->
    });
    function openUrl(id) {
        var url = "${(ProductConfig.getMainSiteBaseUrl())!''}//view/mobile/teacher/activity/newforum/detail?forum_index=" + id + "&share=true";
        window.open(url, '_top ', 'width=375,height=667')
    }
</script>
</@module.page>