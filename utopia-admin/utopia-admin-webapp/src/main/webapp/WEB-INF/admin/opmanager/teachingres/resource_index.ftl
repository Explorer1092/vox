<#import "../../layout_default.ftl" as layout_default />
<#import "../../mizar/pager.ftl" as pager />
<@layout_default.page page_title="教学资源" page_num=9>
<div id="main_container" class="span9">
    <legend>
        <strong>资源列表</strong>&nbsp;&nbsp;&nbsp;&nbsp;
        <a href="query_task.vpage">老师查询</a>
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
                            <option value="CHINESE" <#if searchSubject == 'CHINESE'>selected</#if>>语文</option>
                            <option value="MATH" <#if searchSubject == 'MATH'>selected</#if>>数学</option>
                            <option value="ENGLISH" <#if searchSubject == 'ENGLISH'>selected</#if>>英语</option>
                        </select>
                        </li>
                        <li>
                            资源类型：
                            <select name="category" style="width:95px;">
                                <option value="">全部</option>
                                <option value="COURSE_WARE" <#if searchCategory == 'COURSE_WARE'>selected</#if>>优质课件</option>
                                <option value="OUTSIDE_READING" <#if searchCategory == 'OUTSIDE_READING'>selected</#if>>课外拓展</option>
                                <option value="TEST_PAPER" <#if searchCategory == 'TEST_PAPER'>selected</#if>>精品试题</option>
                                <option value="LECTURE" <#if searchCategory == 'LECTURE'>selected</#if>>教研试训</option>

                                <option value="WEEK_WELFARE" <#if searchCategory == 'WEEK_WELFARE'>selected</#if>>v2_每周福利</option>
                                <option value="TEACHING_SPECIAL" <#if searchCategory == 'TEACHING_SPECIAL'>selected</#if>>v2_教学专题</option>
                                <option value="SYNC_COURSEWARE" <#if searchCategory == 'SYNC_COURSEWARE'>selected</#if>>v2_同步课件</option>
                            </select>
                        </li>
                        <li>
                            资源位置：<select name="position" style="width:80px;">
                            <option value="all" <#if searchPosition == "all">selected</#if>>全部</option>
                            <option value="featuring" <#if searchPosition == "featuring">selected</#if>>首页</option>
                        </select>
                        </li>
                        <li>
                            状态：<select name="online" style="width:80px;">
                            <option value="all" <#if searchOnline == "all">selected</#if>>全部</option>
                            <option value="true" <#if searchOnline == "true">selected</#if>>线上</option>
                            <option value="false" <#if searchOnline == "false">selected</#if>>线下</option>
                        </select>
                        </li>
                        <li>
                            <#if limitUserTypes??>
                                可见用户：
                                <select id="userVisitType" name="userVisitType" style="width:106px;">
                                    <option value="" <#if !searchUserVisitType ?? || searchUserVisitType == ''> selected </#if>>全部</option>
                                    <#list limitUserTypes as t >
                                        <option value="${t.name()!}" <#if searchUserVisitType ?? && (searchUserVisitType == t.name())>
                                                selected </#if>>${t.getDescription()!}</option>
                                    </#list>
                                </select>
                            </#if>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <#if limitUserTypes??>
                                领取用户：
                                <select id="userReceiveType" name="userReceiveType" style="width:106px">
                                    <option value="" <#if !searchUseReceiveType ?? || searchUseReceiveType == ''> selected </#if>>全部</option>
                                    <#list limitUserTypes as t >
                                        <option value="${t.name()!}" <#if searchUseReceiveType ?? && (searchUseReceiveType == t.name())>
                                                selected </#if>>${t.getDescription()!}</option>
                                    </#list>
                                </select>
                            </#if>
                        </li>
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
                        <th style="text-align: center; width: 80px;">资源类型</th>
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
                                                                     height="140" src="${resource.image!'0'}"></td>
                                <td>${resource.name!''}</td>
                                <td style="text-align:center">${resource.updateAt!''}</td>
                                <td>${resource.subjectNames!''}</td>
                                <td style="text-align: center;">${resource.categoryName!''}</td>
                                <td style="text-align: center;">${resource.label!''}</td>
                                <td style="text-align: center;"><#if resource.online?? && resource.online>线上<#else>
                                    线下</#if></td>
                                <td style="text-align: center;"><#if resource.featuring?? && resource.featuring>展示<#else>不展示</#if></td>
                                <td>${resource.displayOrder!''}</td>
                                <td style="text-align: center;width:120px;">
                                    <a href="javascript:void(0);" class="btn btn-info"
                                       onclick="openUrl('${resource.id!'0'}','${resource.category!''}')">
                                        <i class=""></i> 预 览
                                    </a>
                                    <a href="info.vpage?id=${resource.id}" class="btn btn-info">
                                        <i class=""></i> 编 辑
                                    </a>
                                    <#if resource.online?? && resource.online>
                                        <button id="offline-btn" class="btn btn-danger">
                                            <i class=""></i> 下线
                                        </button>
                                    <#else>
                                        <button id="online-btn" class="btn btn-success">
                                            <i class=""></i> 上线
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
</@layout_default.page>