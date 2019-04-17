<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='趣味活动审批' page_num=4>
<style>
    span { font: "arial"; }
</style>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>趣味活动审批&nbsp; &nbsp;<a href="/site/activity/data/list.vpage">趣味活动数据查看</a></legend>
        </fieldset>
        <div id = "activityTable">
            <form id="ad-query" class="form-horizontal" method="get"
                  action="">
                <div style="margin-bottom: 10px">
                    <span style="white-space: nowrap;">
                        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
                         审核状态：
                         <select id="status" name="status"  onchange="queryChange()">
                            <option value="1">未审核</option>
                            <option value="2" <#if status = 2> selected </#if>>通过</option>
                            <option value="3" <#if status = 3> selected </#if>>拒绝</option>
                         </select>
                         &nbsp; &nbsp;
                         游戏名称：
                         <input type="text" id="name" name="name" value="${name!''}"/>
                         &nbsp; &nbsp;
                         年级：
                         <select name="clazzLevel"  onchange="queryChange()">
                            <option value="0">全部</option>
                            <option value="1" <#if clazzLevel = 1> selected </#if>>一年级</option>
                            <option value="2" <#if clazzLevel = 2> selected </#if>>二年级</option>
                            <option value="3" <#if clazzLevel = 3> selected </#if>>三年级</option>
                            <option value="4" <#if clazzLevel = 4> selected </#if>>四年级</option>
                            <option value="5" <#if clazzLevel = 5> selected </#if>>五年级</option>
                            <option value="6" <#if clazzLevel = 6> selected </#if>>六年级</option>
                         </select>
                         &nbsp; &nbsp;
                         游戏类型：
                        <select name="type"  onchange="queryChange()">
                            <option value="">全部</option>
                            <#list types as aType>
                              <option value="${aType.type}" <#if type = aType.type> selected </#if>>${aType.name}</option>
                            </#list>
                         </select>
                     </span>
                </div>
                <button type="button" class="btn btn-primary" onclick="queryChange()">查询</button>
                &nbsp; &nbsp;
                <button type="button" class="btn" onclick="resetQuery()">重置</button>

            </form>
            <table class="table table-bordered">
                <tr>
                    <th>标题</th>
                    <th>时间</th>
                    <th>游戏名称</th>
                    <th>游戏起始时间</th>
                    <th>参与年级</th>
                    <th>状态</th>
                    <th>申请人</th>
                    <th>参与人数</th>
                    <th>操作</th>
                </tr>
                <#if activity?has_content>
                    <#list activity as content>
                        <tr>
                            <td>${content.title!}(${content.id!})</td>
                            <td>${content.applicantTime!}</td>
                            <td>${content.name!}</td>
                            <td>${content.startTime!} ~ ${content.endTime!}</td>
                            <td>
                                ${content.clazzLevel!}
                            </td>
                            <td>${content.statusDesc!}</td>
                            <td>${content.applicant!}</td>
                            <td>${content.total!}</td>
                            <td>
                                <button class="btn btn-success" onclick="show('${content.id}')">查看</button>
                            </td>
                        </tr>
                    </#list>
                </#if>
            </table>
            <ul class="pager">
                <li><a href="javaScript:;" onclick="pagePost(0)" title="Pre">首页</a></li>
                    <#if hasPrev>
                        <li><a href="javaScript:;" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>
                    <#else>
                        <li class="disabled"><a href="javaScript:;">&lt;</a></li>
                    </#if>
                <li class="disabled"><a>第 ${currentPage+ 1} 页</a></li>
                <li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>
                    <#if hasNext>
                        <li><a href="javaScript:;" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>
                    <#else>
                        <li class="disabled"><a href="javaScript:;">&gt;</a></li>
                    </#if>
            </ul>
        </div>
    </div>
</div>

<!-- 审核弹窗 -->
<div id="audit_dialog" class="modal fade hide" style="height: auto">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h3 class="modal-title">活动审核</h3>
                </div>
                <div class="form-horizontal">
                    <div class="modal-body" style="height: auto; overflow: auto;">
                        <div class="control-group">
                            <label><strong id="activity_name"></strong></label>
                        </div>
                        <div>
                            <label>申请人：<span id = "activity_applicant"></span></label>
                        </div>
                        <div>
                            <label>申请时间：<span id = "activity_applicant_time"></span></label>
                        </div>
                        <div class="control-group">
                            <label>申请状态：<span id = "activity_applicant_status"></span></label>
                        </div>


                        <div>
                            <label><strong>游戏简介</strong></label>
                        </div>
                        <div>
                            <label>游戏时间：<span id = "activity_time"></span></label>
                        </div>
                        <div>
                            <label>参与学校：<span id = "activity_school"></span></label>
                        </div>
                        <div>
                            <label>参与区域：<span id = "activity_areas"></span></label>
                        </div>
                        <div>
                            <label>参与年级：<span id = "activity_clazz_level"></span></label>
                        </div>
                        <div>
                            <label>开放学科：<span id = "activity_subjects"></span></label>
                        </div>
                        <div class="control-group">
                            <label>游戏说明：<span id = "activity_desc"></span></label>
                        </div>


                        <div>
                            <label><strong>游戏规则</strong></label>
                        </div>
                        <div>
                            <label><span id = "activity_rule"></span></label>
                        </div>
                        <div>
                            <label><strong>官方证明</strong></label>
                        </div>
                        <div>
                            <label><img id="proveImg" width="500px"></label>
                        </div>
                        <div style="display: none" id="activity_id"></div>
                    </div>
                    <div class="modal-footer">
                        <button id="btn_modal_agree_all" type="button" class="btn btn-primary">全学科通过</button>
                        <button id="btn_modal_agree" type="button" class="btn btn-primary">数学学科通过</button>
                        <button id="btn_modal_reject" type="button" class="btn btn-warning">驳回</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
<script>
        $('#btn_modal_agree').click(function () {
            if (confirm('确认通过该申请吗？')) {
                agree($('#activity_id').text(),"MATH")
            }
        });
        $('#btn_modal_agree_all').click(function () {
            if (confirm('确认全学科通过该申请吗？')) {
                agree($('#activity_id').text(), "UNKNOWN")
            }
        });

        $('#btn_modal_reject').click(function () {
            if (confirm('确认驳回该申请吗？')) {
                reject($('#activity_id').text())
            }
        });
        function show(id) {
            $.ajax({
                url: 'load.vpage',
                type: 'GET',
                data: {id:id},
                success: function (data) {
                    console.log(data);
                    if (data.success) {
                        $('#activity_name').text(data.activity.name);
                        $('#activity_id').text(data.activity.id);
                        $('#activity_applicant').text(data.activity.applicant);
                        $('#activity_applicant_time').text(data.activity.applicantTime);
                        $('#activity_applicant_status').text(data.activity.statusDesc);

                        $('#activity_time').text(data.activity.time);
                        $('#activity_clazz_level').text(data.activity.clazzLevel);
                        $('#activity_subjects').text(data.activity.subjects);
                        $('#activity_desc').text(data.activity.desc);

                        $('#activity_rule').text(data.activity.rules);
                        if (data.activity.status !== 1) {
                            $('#btn_modal_agree').hide();
                            $('#btn_modal_agree_all').hide();
                            $('#btn_modal_reject').hide();
                        }
                        var areaSchool = data.activity.areaSchools;
                        var html = "</br>";
                        $.each(areaSchool,function(i,item){
                            html += item.areaName + "(" + item.count + ")</br>";
                            $.each(item.schools,function(j,school){
                                html += school + "、"
                            });
                            html += "</br>"
                        });
                        $('#activity_school').html(html)

                        var areas = data.activity.areas;
                        var htmlArea = "</br>";
                        $.each(areas,function(i,item){
                            htmlArea += item.cityName + "</br>";
                            $.each(item.areas,function(j,area){
                                htmlArea += area + "、"
                            });
                            htmlArea += "</br>"
                        });
                        $('#activity_areas').html(htmlArea)
                        $('#proveImg').attr("src", data.activity.proveImg);

                        $('#audit_dialog').modal('show');
                    } else {
                        alert(data.info);

                    }
                }
            });

        }
        function pagePost(pageNumber) {
            $("#pageNum").val(pageNumber);
            $("#ad-query").submit();
        }

        function queryChange() {
            $("#pageNum").val(0);
            $("#ad-query").submit();
        }

        function resetQuery() {
            $(':input','#ad-query')
                    .not(':button,:submit,:reset')   //将myform表单中input元素type为button、submit、reset排除
                    .val('')  //将input元素的value设为空值
                    .removeAttr('checked')
                    .removeAttr('checked')
            $("#ad-query").submit();
        }
        function agree(id,subjects) {
            $.ajax({
                url: 'agree.vpage',
                type: 'POST',
                data: {id: id, subjects: subjects},
                dataType: "json",
                success: function (data) {
                    if (data.success) {
                        alert("成功");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                }
            });
        }
        function reject(id) {
            $.ajax({
                url: 'reject.vpage',
                type: 'POST',
                data: {id:id},
                dataType: "json",
                success: function (data) {
                    if (data.success) {
                        alert("成功");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                }
            });
        }
    </script>
</@layout_default.page>
