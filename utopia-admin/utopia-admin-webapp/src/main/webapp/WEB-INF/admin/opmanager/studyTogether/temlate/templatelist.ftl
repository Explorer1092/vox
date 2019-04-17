<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div class="span9">
    <fieldset>
        <legend>课程内容模板管理</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get"
          action="">
        <input type="hidden" id="pageNum" name="page" value="${page!'1'}"/>
        <div>
            <span style="white-space: nowrap;">
                模板ID：<input type="text" id="template_id" name="template_id" style="width: 100px;" value="${template_id!''}"/>
            </span>
            <span style="white-space: nowrap;">
                模板名称：<input type="text" id="template_name" name="template_name" value="${template_name!''}"/>
            </span>
            <span style="white-space: nowrap;">
                SPU_ID：<input type="text" id="template_spu_id" name="template_spu_id" value="${template_spu_id!''}"/>
            </span>
            <span style="white-space: nowrap;">
                课程结构分类：<select id="course_type" name="course_type" style="width: 150px;">
                                <option value="1" <#if course_type?? && course_type == 1> selected="selected"</#if>>语文古文</option>
                                <option value="2" <#if course_type?? && course_type == 2> selected="selected"</#if>>英语绘本</option>
                                <option value="3" <#if course_type?? && course_type == 3> selected="selected"</#if>>语文阅读</option>
                                <option value="6" <#if course_type?? && course_type == 6> selected="selected"</#if>>语文故事</option>
                              </select>
            </span>

            <span style="white-space: nowrap;">
                创建人：<input type="text" id="create_user" name="create_user" style="width: 100px;" value="${create_user!''}"/>
            </span>
            <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
        </div>
    </form>
    <a class="btn btn-primary" target="_blank" href="/opmanager/studyTogether/template/classical_chinese_template_info.vpage?edit=1">新建模板-语文古文</a>
    <a class="btn btn-success" target="_blank" href="/opmanager/studyTogether/template/picturebook/picture_book_template_info.vpage?edit=1">新建模板-英语绘本</a>
    <a class="btn btn-warning" target="_blank" href="/opmanager/studyTogether/template/chinesereading/chinese_reading_template_info.vpage?edit=1">新建模板-语文阅读</a>
    <a class="btn btn-danger" target="_blank" href="/opmanager/studyTogether/template/chinesestory/chinese_story_template_info.vpage?edit=1">新建模板-语文故事</a>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>模板ID</th>
                        <th>模板名称</th>
                        <th>SPU_ID</th>
                        <th>课节类型</th>
                        <th>创建人</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if picture_book_template_list?? && picture_book_template_list?size gt 0>
                            <#list picture_book_template_list as  template>
                            <tr>
                                <td>${template.id!''}</td>
                                <td>${template.name!''}</td>
                                <td>${template.spu_id!0}</td>
                                <td>英语绘本</td>
                                <td>${template.create!''}</td>
                                <td>
                                    <a class="btn btn-primary" target="_blank" href="/opmanager/studyTogether/template/picturebook/picture_book_template_info.vpage?template_id=${template.id!''}">详情</a>
                                    <a class="btn btn-success" target="_blank" href="/opmanager/studyTogether/template/picturebook/picture_book_template_info.vpage?edit=1&template_id=${template.id!''}">修改</a>
                                    <a class="btn btn-warning" href="/opmanager/studyTogether/template/change_log_list.vpage?template_id=${template.id!''}&change_log_type=PictureBookTemplate">日志</a>
                                </td>
                            </tr>
                            </#list>
                        <#elseif  classical_chinese_template_list?? && classical_chinese_template_list?size gt 0>
                            <#list classical_chinese_template_list as  template>
                            <tr>
                                <td>${template.id!''}</td>
                                <td>${template.name!''}</td>
                                <td>${template.spu_id!0}</td>
                                <td>语文古文</td>
                                <td>${template.create!''}</td>
                                <td>
                                    <a class="btn btn-primary" target="_blank" href="classical_chinese_template_info.vpage?template_id=${template.id!''}">详情</a>
                                    <a class="btn btn-success" target="_blank" href="classical_chinese_template_info.vpage?edit=1&template_id=${template.id!''}">修改</a>
                                    <a class="btn btn-warning" href="/opmanager/studyTogether/template/change_log_list.vpage?template_id=${template.id!''}&change_log_type=ClassicChineseTemplate">日志</a>
                                </td>
                            </tr>
                            </#list>
                        <#elseif  chinese_reading_template_list?? && chinese_reading_template_list?size gt 0>
                            <#list chinese_reading_template_list as  template>
                            <tr>
                                <td>${template.id!''}</td>
                                <td>${template.name!''}</td>
                                <td>${template.spu_id!0}</td>
                                <td>语文阅读</td>
                                <td>${template.create!''}</td>
                                <td>
                                    <a class="btn btn-primary" target="_blank" href="/opmanager/studyTogether/template/chinesereading/chinese_reading_template_info.vpage?template_id=${template.id!''}">详情</a>
                                    <a class="btn btn-success" target="_blank" href="/opmanager/studyTogether/template/chinesereading/chinese_reading_template_info.vpage?edit=1&template_id=${template.id!''}">修改</a>
                                    <a class="btn btn-warning" target="_blank" href="/opmanager/studyTogether/template/change_log_list.vpage?template_id=${template.id!''}&change_log_type=ChineseReading">日志</a>
                                </td>
                            </tr>
                            </#list>
                        <#elseif chinese_story_template_list?? && chinese_story_template_list?size gt 0>
                            <#list chinese_story_template_list as  template>
                                <tr>
                                    <td>${template.id!''}</td>
                                    <td>${template.name!''}</td>
                                    <td>${template.spu_id!0}</td>
                                    <td>语文故事</td>
                                    <td>${template.create!''}</td>
                                    <td>
                                        <a class="btn btn-primary" target="_blank" href="/opmanager/studyTogether/template/chinesestory/chinese_story_template_info.vpage?template_id=${template.id!''}">详情</a>
                                        <a class="btn btn-success" target="_blank" href="/opmanager/studyTogether/template/chinesestory/chinese_story_template_info.vpage?edit=1&template_id=${template.id!''}">修改</a>
                                        <a class="btn btn-warning" target="_blank" href="/opmanager/studyTogether/template/change_log_list.vpage?template_id=${template.id!''}&change_log_type=ChineseStory">日志</a>
                                    </td>
                                </tr>
                            </#list>
                        <#else >
                        <tr>
                            <td colspan="7" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <ul class="message_page_list">
                </ul>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $(".message_page_list").page({
            total: ${total_page!1},
            current: ${page!1},
            autoBackToTop: false,
            maxNumber: 20,
            jumpCallBack: function (index) {
                $("#pageNum").val(index);
                $("#op-query").submit();
            }
        });

        //绘本和古诗的列表地址切换
        $("#course_type").on("change",function () {
           var type = $("#course_type").find("option:selected").val();
           if(type === "1"){
               $("#op-query").attr("action","/opmanager/studyTogether/template/classical_chinese_list.vpage");
           }else if(type === "2"){
               $("#op-query").attr("action","/opmanager/studyTogether/template/picturebook/picture_book_list.vpage");
           }else if(type === "3"){
               $("#op-query").attr("action","/opmanager/studyTogether/template/chinesereading/chinese_reading_list.vpage");
           }else if(type === "6"){
               $("#op-query").attr("action","/opmanager/studyTogether/template/chinesestory/chinese_story_list.vpage");
           } else {
               alert("课程结构分类错误");
               return;
           }
        });

        $("#searchBtn").on('click', function () {
            $("#pageNum").val(1);
            $("#op-query").submit();
        });
    });
</script>
</@layout_default.page>