<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='用户成绩展示' page_num=26>
<style type="text/css">

</style>
<div id="main_container" class="span9">
    <legend>用户成绩展示</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <form id="frm" class="form-horizontal" action="/chips/user/unit/list.vpage">
                    <input type="hidden" id="pageNumber" name="pageNumber" value="1">
                    用户名：
                    <input type="text" name="userName" style="height: 29px;"
                           placeholder="模糊搜索" <#if userName ?? && userName != ''> value=${userName} </#if> >
                    用户Id：
                    <input type="text" name="userId" style="height: 29px;"
                           placeholder="精确搜索" <#if userId ?? && userId gt 0> value=${userId!} </#if> >
                    班级：
                    <#--<select id="clazz" data-init='false' name="clazz" class="multiple district_select">-->
                        <#--<option value="1" >Hailey班</option>-->
                        <#--<option value="2" >winston班</option>-->
                    <#--</select>-->
                    <select id="clazz" data-init='false' name="clazz" class="multiple district_select">
                        <option value="">----请选择----</option>
                        <#if clazzOptionList?size gt 0>
                            <#list clazzOptionList as e >
                                <option value="${e.value!}" <#if e.selected>selected</#if>>${e.desc!}</option>
                            </#list>
                        </#if>
                    </select>
                    <button class="btn btn-primary">查 询</button>
                </form>
                <button class="btn btn-primary" id="export">导出</button>
                <button class="btn btn-primary" id="preview">预览定级报告</button>
                <button class="btn btn-primary" id="rank">生成榜单</button>
            </div>
        </div>
    </div>
    <div id="data_table_journal">
        <table class="table table-striped table-bordered">
            <tr>
                <td>班级</td>
                <td>用户</td>
                <td>完成率</td>
                <td>第一课:DAY 1</td>
                <td>第二课:DAY 2</td>
                <td>第三课:DAY 3</td>
                <td>第四课:DAY 4</td>
                <td>第五课:DAY 5</td>
                <td>第六课:DAY 6</td>
                <td>第七课:DAY 7</td>
                <td>第八课:DAY 8</td>
                <td>第九课:DAY 9</td>
                <td>第十课:DAY 10</td>
            </tr>
            <#if pageData.content?? && pageData.content?size gt 0>
                <#list pageData.content as e >
                    <tr>
                        <td>${e.clazz!}</td>
                        <td>${e.user!}</td>
                        <td>${e.finish!}</td>
                        <td>${e.c1!}</td>
                        <td>${e.c2!}</td>
                        <td>${e.c3!}</td>
                        <td>${e.c4!}</td>
                        <td>${e.c5!}</td>
                        <td>${e.c6!}</td>
                        <td>${e.c7!}</td>
                        <td>${e.c8!}</td>
                        <td>${e.c9!}</td>
                        <td>${e.c10!}</td>
                    </tr>
                </#list>
            <#else >
                <tr>
                    <td colspan="13"><strong>暂无数据</strong></td>
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
                    <h4 class="modal-title" id="myModalLabel" style="text-align:center;">请求结果</h4>
                </div>
                <div class="modal-body" style="max-height: 550px !important;">
                    <div class="main_content_detail">
                        <div class="courseware_list">
                            <div class="inner">
                                <div class="item">
                                    <div class="head_img" style="width: 50%;margin: 0 auto;display: block;">
                                    </div>
                                    <div class="content_desc">
                                       <span id="back_content"></span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script type="text/javascript">
        Date.prototype.format = function (format) {
            var o = {
                "M+": this.getMonth() + 1, //month
                "d+": this.getDate(),    //day
                "h+": this.getHours(),   //hour
                "m+": this.getMinutes(), //minute
                "s+": this.getSeconds(), //second
                "q+": Math.floor((this.getMonth() + 3) / 3),  //quarter
                "S": this.getMilliseconds() //millisecond
            }
            if (/(y+)/.test(format)) format = format.replace(RegExp.$1,
                    (this.getFullYear() + "").substr(4 - RegExp.$1.length));
            for (var k in o) if (new RegExp("(" + k + ")").test(format))
                format = format.replace(RegExp.$1,
                        RegExp.$1.length == 1 ? o[k] :
                                ("00" + o[k]).substr(("" + o[k]).length));
            return format;
        }

        function pagePost(pageNumber) {
            $("#pageNumber").val(pageNumber);
            $("#frm").submit();
        }
        $(function () {
            $("#export").on('click', function () {
                var clazz=$("#clazz").val();
                if(clazz == ""){
                    alert("请选择班级")
                } else {
                    window.location.href = "/chips/user/unit/data/export.vpage?clazz=" + clazz;
                }
            });

            $("#rank").on('click', function () {
                var clazz=$("#clazz").val();

                $.ajax({
                    type: "POST",
                    url: "/chips/user/unit/rank.vpage",
                    data: {
                        clazz: clazz,
                    },
                    success: function (data) {
                        $("#back_content").html("");
                        $("#back_content").html(data.info);
                        $("#courseware_detail").modal('show');
                    }
                });
            });

            $("#preview").on('click', function () {
                var clazz=$("#clazz").val();
                if(clazz == ""){
                    alert("请选择班级")
                } else {
                    window.location.href = "/chips/user/unit/bookres/preview.vpage?clazz=" + clazz;
                }
            });
        });
    </script>
</@layout_default.page>