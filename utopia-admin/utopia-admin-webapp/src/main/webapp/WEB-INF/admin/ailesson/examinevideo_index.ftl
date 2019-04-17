<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='用户视频审核' page_num=26>
<style type="text/css">

</style>
<div id="main_container" class="span9">
    <legend>用户视频审核</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <form id="frm" class="form-horizontal" action="/chips/user/video/examine/list.vpage">
                    <input type="hidden" id="pageNumber" name="pageNumber" value="1">
                    用户Id：
                    <input type="text" name="userId" style="height: 29px;"
                           placeholder="精确搜索" <#if userId ?? && userId gt 0> value=${userId} </#if> >
                    用户名：
                    <input type="text" name="userName" style="height: 29px;"
                           placeholder="模糊搜索" <#if userName ?? && userName != ''> value=${userName} </#if> >
                    视频Id：
                    <input type="text" name="userVideoId" style="height: 29px;"
                           placeholder="精确搜索" <#if userVideoId ?? && userVideoId != ''> value=${userVideoId} </#if> >
                    课程：
                    <select id="book" name="book" class="multiple district_select" next_level="units">
                        <#if books??>
                            <#list books as p>
                                <option value="${p.id}" <#if book ?? && book == p.id> selected</#if>>${p.name}</option>
                            </#list>
                        </#if>
                    </select>
                    <br />
                    备注：<input id="desc" name="desc"  style="height: 29px;"
                           placeholder="模糊搜索" <#if desc ?? && desc != ''> value=${desc!} </#if> />
                    单元：
                    <select id="unit" data-init='false' name="unit" class="multiple district_select">
                        <#if units??>
                            <#list units as p>
                                <option value="${p.id}" <#if unit ?? && unit == p.id> selected</#if>>${p.name}</option>
                            </#list>
                        </#if>
                    </select>
                    <br/>
                    审核状态：
                    <select id="examineStatus" name="examineStatus" style="font-size: 12px;width: 100px;">
                        <#if examineStatusList?has_content>
                            <#list examineStatusList as s>
                                <option value="${s.name()!}" <#if examineStatus ?? && examineStatus == s.name()>
                                        selected</#if>>${s.getDescription()!}</option>
                            </#list>
                        </#if>
                    </select>
                    <input type="hidden" id="fromStatus" name="fromStatus" value="${examineStatus!}">
                    <button class="btn btn-primary">查 询</button>
                </form>

            </div>
        </div>
    </div>
    <div id="data_table_journal">
        <table class="table table-striped table-bordered">
            <tr>
                <td>视频id</td>
                <td>用户</td>
                <td>课程</td>
                <td>创建时间</td>
                <td>状态</td>
                <td>操作</td>
            </tr>
            <#if pageData.content?? && pageData.content?size gt 0>
                <#list pageData.content as e >
                    <tr>
                        <td>${e.id!}</td>
                        <td>${e.user!}</td>
                        <td>${e.lesson!}</td>
                        <td>${e.createDate!}</td>
                        <td>${e.status!}</td>
                        <td>
                            <button type="button" name="detail" data-id="${e.id!}" class="btn btn-primary">详情</button>
                        </td>
                    </tr>
                </#list>
            <#else >
                <tr>
                    <td colspan="6"><strong>暂无数据</strong></td>
                </tr>
            </#if>
        </table>
    </div>
    <ul class="pager">
        <#if (pageData.hasPrevious())>
            <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
        <#else>
            <li class="disabled"><a href="#">上一页</a></li>
        </#if>
        <#if (pageData.hasNext())>
            <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
        <#else>
            <li class="disabled"><a href="#">下一页</a></li>
        </#if>
        <li>当前第 ${pageNumber!} 页 |</li>
        <li>共 ${pageData.totalPages!} 页|</li>
        <li>共 ${total !} 条</li>
    </ul>


    <!-- Modal -->
    <div class="modal fade hide" id="courseware_detail" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="myModalLabel" style="text-align:center;">视频详情</h4>
                    <input type="hidden" id="detail_id"/>
                </div>
                <div class="modal-body" style="max-height: 550px !important;">
                    <div class="main_content_detail">
                        <div class="courseware_list">
                            <div class="inner">
                                <div class="item">
                                    <div class="head_img" style="width: 50%;margin: 0 auto;display: block;">
                                        <video controls="controls" id="detail_video" style="width: 100%;"/>
                                    </div>
                                    <div class="content_desc">
                                        <br/>
                                        <div class="state_and_op" style="margin: 10px">
                                            <div class="state">
                                                状态：<span id="detail_status" name="detail"></span>&nbsp;&nbsp;
                                                用户：<span id="detail_user" name="detail"></span> &nbsp;&nbsp;
                                                单元：<span id="detail_unit" name="detail"></span>&nbsp;&nbsp;
                                            </div>
                                        </div>
                                        <div class="state_and_op"  style="margin: 10px">
                                            <div class="state"> 视频分类：
                                                <select name="category" id = 'category'>
                                                    <option value=""></option>
                                                  <#if categoryList?has_content>
                                                      <#list categoryList as s>
                                                        <option value="${s.name()!}">${s.getDescription()!}</option>
                                                      </#list>
                                                  </#if>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="update_time"  style="margin: 10px">
                                            更新时间：<span class="time" id="detail_date"
                                                                            name="detail"></span></div>
                                        <div class="update_time"  style="margin: 10px">
                                            备注：<textarea name="description" id = "description"></textarea></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <span class="btn btn-primary exmine pass" data-val = "1" id="passed">通过</span>
                    <span class="btn btn-danger exmine failed" data-val = "0" id="failed">违规驳回</span>
                </div>
            </div>
        </div>
    </div>


    <script type="text/javascript">
        Date.prototype.format = function(format)
        {
            var o = {
                "M+" : this.getMonth()+1, //month
                "d+" : this.getDate(),    //day
                "h+" : this.getHours(),   //hour
                "m+" : this.getMinutes(), //minute
                "s+" : this.getSeconds(), //second
                "q+" : Math.floor((this.getMonth()+3)/3),  //quarter
                "S" : this.getMilliseconds() //millisecond
            }
            if(/(y+)/.test(format)) format=format.replace(RegExp.$1,
                    (this.getFullYear()+"").substr(4 - RegExp.$1.length));
            for(var k in o)if(new RegExp("("+ k +")").test(format))
                format = format.replace(RegExp.$1,
                        RegExp.$1.length==1 ? o[k] :
                                ("00"+ o[k]).substr((""+ o[k]).length));
            return format;
        }
        function pagePost(pageNumber) {
            $("#pageNumber").val(pageNumber);
            $("#frm").submit();
        }

        $(function () {
            $('button[name=detail]').on('click', function () {
                var dataId = $(this).attr("data-id");
                $.ajax({
                    type: "get",
                    url: "/chips/user/video/detail.vpage",
                    data: {
                        id: dataId,
                    },
                    success: function (data) {
                        if (data.success) {
                            $("#detail_id").val(data.data.id);
                            if (data.data.video != null && data.data.video != '') {
                                $("#detail_video").attr("src", data.data.video);
                            }
                            if (data.data.statusName != null && data.data.statusName != '') {
                                $("#detail_status").text(data.data.statusName);
                            }

                            $("#description").val("");
                            if (data.data.description != null && data.data.description != '') {
                                $("#description").val(data.data.description);
                            }

                            $("#category").val("");
                            if (data.data.category != null && data.data.category != '') {
                                $("#category").val(data.data.category);
                            }


                            if (data.data.status != null && data.data.status == 'Waiting') {
                                $("#passed").removeClass("hidden")
                                $("#failed").removeClass("hidden")
                            }

                            if (data.data.status != null && data.data.status == 'Passed') {
                                $("#passed").addClass("hidden")
//                                $("#failed").addClass("hidden");
                                $("#failed").removeClass("hidden");
                            }

                            if (data.data.status != null && data.data.status == 'Failed') {
                                $("#failed").addClass("hidden");
                                $("#passed").addClass("hidden");
                            }

                            $("#detail_user").text(data.data.userName + "(" + data.data.userId + ")");
                            $("#detail_unit").text(data.data.lessonName);
                            if (data.data.updateTime != null) {
                                var date = new Date(data.data.updateTime);
                                $("#detail_date").text(date.format("yyyy-MM-dd hh:mm:ss"));
                            }

                            $('#courseware_detail').modal('show');
                        } else {
                            alert(data.info);
                        }
                    }
                });
            });
            $(".exmine").on('click', function () {
                var id = $("#detail_id").val();
                var category = $("#category").val();
                var btn =  $(this).attr("data-val");
                var description = $("#description").val();
                if (btn == '0' && (description == null || description.trim() == '')) {
                    alert("请将违规原因填写到描述中");
                    return;
                }
                var fromStatus = $("#fromStatus").val();
                $.ajax({
                    type: "POST",
                    url: "/chips/user/video/examine.vpage",
                    data: {
                        id: id,
                        category: category,
                        description:description,
                        formStatus:fromStatus,
                        status: btn == '1' ? 'Passed' : 'Failed'
                    },
                    success: function (data) {
                        if (data.success) {
                            $('#courseware_detail').modal('hide');
                            alert("操作成功");
                        } else {
                            alert(data.info);
                        }
                        location.reload();
                    }
                });
            });
        });
    </script>
</@layout_default.page>