<#import "../../module.ftl" as module>
<#import "../../common/pager.ftl" as pager />
<@module.page
title="江西版-教学资源"
leftMenu="图文"
>

<link href="${ctx}/public/plugin/bootstrap-2.3.0/css/bootstrap.min.css" rel="stylesheet">
<script src="${ctx}/public/plugin/jquery/jquery-1.9.1.min.js"></script>
<script src="${ctx}/public/plugin/bootstrap-2.3.0/js/bootstrap.min.js"></script>
<script src="${ctx}/public/plugin/template.min.js"></script>
<style>
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
    .operate-ul li .li-label{ display: inline-block; width: 70px; }
    .operate-ul li select{ display: inline-block; width: 150px !important; }
</style>

<div>
    <div class="op-wrapper orders-wrapper clearfix">
        <span class="title-h1">资源列表</span>
    </div>
    <div class="op-wrapper marTop clearfix">
        <div class="row-fluid">
            <form id="query_frm" class="form-horizontal" method="get" action="index.vpage">
                <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>
                <input type="hidden" id="action" name="action" />
                <ul class="inline operate-ul">
                    <li>
                        <span class="li-label">标题：</span>
                        <input type="text" id="sid" style="width:388px; height: 30px;" name="name" value="<#if searchName??>${searchName}</#if>">
                    </li>
                    <br />
                    <li>
                        <span class="li-label">年级：</span>
                        <select name="grade">
                            <option value="">全部</option>
                            <option value="1">1年级</option>
                            <option value="2">2年级</option>
                            <option value="3">3年级</option>
                            <option value="4">4年级</option>
                            <option value="5">5年级</option>
                            <option value="6">6年级</option>
                        </select>
                    </li>
                    <#--<li>
                        <span class="li-label">学科：</span>
                        <select name="subject">
                            <option value="">全部</option>
                            <option value="CHINESE" <#if searchSubject == 'CHINESE'>selected</#if>>语文</option>
                            <option value="MATH" <#if searchSubject == 'MATH'>selected</#if>>数学</option>
                            <option value="ENGLISH" <#if searchSubject == 'ENGLISH'>selected</#if>>英语</option>
                        </select>
                    </li>-->
                    <li>
                        <span class="li-label">状态：</span>
                        <select name="online">
                            <option value="all" <#if searchOnline == "all">selected</#if>>全部</option>
                            <option value="true" <#if searchOnline == "true">selected</#if>>线上</option>
                            <option value="false" <#if searchOnline == "false">selected</#if>>线下</option>
                        </select>
                    </li>
                    <br />
                    <li>
                        <span class="li-label">资源类型：</span>
                        <select name="category">
                            <option value="">全部</option>
                            <option value="IMPORTANT_CASE" <#if searchCategory == 'IMPORTANT_CASE'>selected</#if>>关键课例</option>
                            <option value="GROW_UP" <#if searchCategory == 'GROW_UP'>selected</#if>>成长心语</option>
                            <option value="ACTIVITY_NOTICE" <#if searchCategory == 'ACTIVITY_NOTICE'>selected</#if>>活动公告</option>
                            <option value="OTHER_STONE" <#if searchCategory == 'OTHER_STONE'>selected</#if>>他山之石</option>
                        </select>
                    </li>
                    <li>
                        <span class="li-label">资源位置：</span>
                        <select name="position">
                            <option value="all" <#if searchPosition == "all">selected</#if>>全部</option>
                            <option value="featuring" <#if searchPosition == "featuring">selected</#if>>首页</option>
                        </select>
                    </li>
                    <br />
                    <li>
                        <#if limitUserTypes??>
                            <span class="li-label">可见用户：</span>
                            <select id="userVisitType" name="userVisitType">
                                <option value="" <#if !searchUserVisitType ?? || searchUserVisitType == ''> selected </#if>>全部</option>
                                <#list limitUserTypes as t >
                                    <option value="${t.name()!}" <#if searchUserVisitType ?? && (searchUserVisitType == t.name())>
                                            selected </#if>>${t.getDescription()!}</option>
                                </#list>
                            </select>
                        </#if>
                    </li>
                    <li>
                        <#if limitUserTypes??>
                            <span class="li-label">领取用户：</span>
                            <select id="userReceiveType" name="userReceiveType">
                                <option value="" <#if !searchUseReceiveType ?? || searchUseReceiveType == ''> selected </#if>>全部</option>
                                <#list limitUserTypes as t >
                                    <option value="${t.name()!}" <#if searchUseReceiveType ?? && (searchUseReceiveType == t.name())>
                                            selected </#if>>${t.getDescription()!}</option>
                                </#list>
                            </select>
                        </#if>
                    </li>
                    <br />
                    <li>
                        <button type="button" id="filter" class="btn btn-success">
                            <i class="icon-search icon-white"></i> 查询
                        </button>
                    </li>
                    <li>
                        <a type="button" class="btn btn-info" href="./info.vpage"><i
                                class="icon-plus icon-white"></i> 添加</a>
                    </li>
                </ul>
            </form>
            <#--<@pager.pager/>-->
            <table class="table table-striped table-bordered">
                <thead>
                <tr>
                    <th style="width: 70px;">ID</th>
                    <th style="width: 100px;">题图</th>
                    <th>文章标题</th>
                    <th>发布/修改时间</th>
                    <#--<th>学科</th>-->
                    <th>资源类型</th>
                    <#--<th>标签</th>-->
                    <th>状态</th>
                    <#--<th>展示在首页</th>-->
                    <#--<th>置顶排序</th>-->
                    <th style="width: 60px;">操作</th>
                </tr>
                </thead>
                <tbody>
                    <#if resourceList?? && resourceList?has_content>
                        <#list resourceList as resource>
                        <tr>
                            <td>${resource.id!'0'}</td>
                            <td style="text-align: center;"><img src="${resource.image!'0'}"></td>
                            <td>${resource.name!''}</td>
                            <td style="text-align:center">${resource.updateAt!''}</td>
                            <#--<td>${resource.subjectNames!''}</td>-->
                            <td>${resource.categoryName!''}</td>
                            <#--<td style="text-align: center;">${resource.label!''}</td>-->
                            <td><#if resource.online?? && resource.online>线上<#else>线下</#if></td>
                            <#--<td><#if resource.featuring?? && resource.featuring>展示<#else>不展示</#if></td>-->
                            <#--<td>${resource.displayOrder!''}</td>-->
                            <td>
                                <a href="javascript:void(0);" class="btn btn-info"
                                   onclick="openUrl('${resource.id!'0'}','${resource.category!''}')">
                                    <i class=""></i> 预 览
                                </a>
                                <a href="info.vpage?id=${resource.id}" class="btn btn-info">
                                    <i class=""></i> 编 辑
                                </a>
                                <#if resource.online?? && resource.online>
                                    <button id="offline-btn" class="btn btn-danger">
                                        <i class=""></i> 下 线
                                    </button>
                                <#else>
                                    <button id="online-btn" class="btn btn-success">
                                        <i class=""></i> 上 线
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
</div>

<script>
    $(function () {
        $("button#offline-btn").on('click', function () {
            var resourceId = $(this).parent().siblings().eq(0).html();
            $.post("./offline.vpage", {resourceId: resourceId}, function () {
                window.location.reload();
            });
        });

        $("button#online-btn").on('click', function () {
            var resourceId = $(this).parent().siblings().eq(0).html();
            $.post("./online.vpage", {resourceId: resourceId}, function () {
                window.location.reload();
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

    });
    function openUrl(id,type) {
        var url = "${(ProductConfig.getMainSiteBaseUrl())!''}/view/mobile/teacher/teaching_assistant/resourcedetail?resourceId=" + id + "&category=" + type + "&share=true";
        window.open(url, '_top ', 'width=375,height=667')
    }
</script>
</@module.page>