<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='用户成绩' page_num=26>
<style>
    .table-responsive {
        min-height: .01%;
        overflow-x: auto
    }

    @media screen and (max-width: 767px) {
        .table-responsive {
            width: 100%;
            margin-bottom: 15px;
            overflow-y: hidden;
            -ms-overflow-style: -ms-autohiding-scrollbar;
            border: 1px solid #ddd
        }

        .table-responsive > .table {
            margin-bottom: 0
        }

        .table-responsive > .table > tbody > tr > td, .table-responsive > .table > tbody > tr > th, .table-responsive > .table > tfoot > tr > td, .table-responsive > .table > tfoot > tr > th, .table-responsive > .table > thead > tr > td, .table-responsive > .table > thead > tr > th {
            white-space: nowrap
        }

        .table-responsive > .table-bordered {
            border: 0
        }

        .table-responsive > .table-bordered > tbody > tr > td:first-child, .table-responsive > .table-bordered > tbody > tr > th:first-child, .table-responsive > .table-bordered > tfoot > tr > td:first-child, .table-responsive > .table-bordered > tfoot > tr > th:first-child, .table-responsive > .table-bordered > thead > tr > td:first-child, .table-responsive > .table-bordered > thead > tr > th:first-child {
            border-left: 0
        }

        .table-responsive > .table-bordered > tbody > tr > td:last-child, .table-responsive > .table-bordered > tbody > tr > th:last-child, .table-responsive > .table-bordered > tfoot > tr > td:last-child, .table-responsive > .table-bordered > tfoot > tr > th:last-child, .table-responsive > .table-bordered > thead > tr > td:last-child, .table-responsive > .table-bordered > thead > tr > th:last-child {
            border-right: 0
        }

        .table-responsive > .table-bordered > tbody > tr:last-child > td, .table-responsive > .table-bordered > tbody > tr:last-child > th, .table-responsive > .table-bordered > tfoot > tr:last-child > td, .table-responsive > .table-bordered > tfoot > tr:last-child > th {
            border-bottom: 0
        }
    }

    .wh {
        width: 100px;
        text-align: center;
        white-space: nowrap;
        text-overflow: ellipsis; /* for IE */
        overflow: hidden;
    }
    .whSix{
        width:60px;
    }
    .whEight{
        width:80px;
    }
    .wh200{
        width:140px;
    }
    .table_box {
        height:650px;
    }

    .table_box table tbody tr td {
        white-space: nowrap;
    }

    .form-group {
        margin: 5px 0;
        display: inline-block;
    }

    .form-group .mylabel {
        width: 150px;
        text-align: right;
    }
</style>
<div id="main_container" class="span9">
    <div class="row-fluid">
        <div class="span12">
            <div>
                <h2 style="float: left">${clazzName}</h2>
                <form id="frm1" class="form form-inline form-horizontal" style="float: right;">
                    <div class="form-group">
                        <input type="hidden" name="productId" value="${productId!}">
                        <label for="" class="mylabel">班级(Class)：</label>
                        <select id="clazzId" data-init='false' name="clazzId" class="multiple district_select">
                            <option value="">----请选择----</option>
                            <#if clazzOptionList?size gt 0>
                                <#list clazzOptionList as e >
                                    <option value="${e.value!}" <#if e.selected>selected</#if>>${e.desc!}</option>
                                </#list>
                            </#if>
                        </select>
                    </div>
                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="find" class="btn btn-info">查询</button>
                    </div>
                </form>
            </div>
            <table class="table table-bordered">
                <tr>
                    <td>班级ID(Class ID)</td>
                    <td>${clazz.clazzId!}</td>
                    <td>课程(Book)</td>
                    <td>${clazz.bookName!}</td>
                </tr>
                <tr>
                    <td>班主任(Teacher)</td>
                    <td>${clazz.clazzTeacherName!}</td>
                    <td>产品(Product)</td>
                    <td>${clazz.productName!}</td>
                </tr>
                <tr>
                    <td>用户上限(Limitation)</td>
                    <td>${clazz.userLimitation!}</td>
                    <td>用户数(Count)</td>
                    <td>${clazz.userCount!}</td>
                </tr>
                <tr>
                    <td>建立时间(Built-up Time)</td>
                    <td>${clazz.createTime!}</td>
                    <td></td>
                    <td></td>
                </tr>
            </table>
            <div class="well" style="font-size: 14px;">
                <form id="frm" class="form form-inline form-horizontal" action="/chips/chips/clazz/manager/userScore.vpage">
                    <input type="hidden" id="pageNumber" name="pageNumber" value="${pageNumber!}">
                    <input type="hidden" id="productId" name="productId" value="${productId!}">
                    <div class="form-group">
                        <lable for="" class="mylabel">用户Id(User ID):</lable>
                        <input type="text" name="userId" <#if userId ??> value=${userId!} </#if>>
                        <input type="hidden" class="form-control" name="clazzId" value="${clazz.clazzId!}">
                    </div>
                    <div class="form-group">
                        <label for="" class="mylabel">定级(Grading):</label>
                        <select id="grading" data-init='false' name="grading" class="multiple district_select">
                            <option value="">----请选择----</option>
                            <#if gradingOptionList?size gt 0>
                                <#list gradingOptionList as e >
                                    <option value="${e.value!}" <#if e.selected>selected</#if>>${e.desc!}</option>
                                </#list>
                            </#if>
                        </select>
                    </div>
                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="filter" class="btn btn-primary">筛选(Filter)</button>
                    </div>
                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="export" class="btn btn-primary">导出(Export)</button>
                    </div>
                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="rank" clazzId="${clazz.clazzId!}" class="btn btn-primary">生成榜单(Rank)</button>
                    </div>
                    <div class="form-group" style="text-align: center;width: 280px;">
                        <button type="button" id="preview" clazzId="${clazz.clazzId!}" class="btn btn-primary">预览定级报告</button>
                    </div>
                    <div class="form-group" style="text-align: center;width: 280px;">
                        <button type="button" id="energyExport" clazzId="${clazz.clazzId!}" class="btn btn-primary">导出能量榜</button>
                    </div>
                </form>
            </div>
        </div>
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation">
                <a href="basicInfo.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}&dataType=0">基础信息(Basic)</a>
            </li>
            <li role="presentation">
                <a href="operationInfo.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}&wxAdd=2&wxLogin=2&wxCodeShowType=2&wxNickName=2<#if userId ??>&userId=${userId!}</#if>">运营信息(Operation)</a>
            </li>
            <li role="presentation" class="active">
                <a href="userScore.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}">用户成绩(User)</a>
            </li>
            <li role="presentation">
                <a href="generalInfo.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}<#if userId ??>&userId=${userId!}</#if>">综合信息(General)</a>
            </li>
        </ul>
        <div id="tableDiv" class="table-responsive table_box" style= "border-top:0px solid #dddddd">
            <table class="table table-condensed  table-hover table-striped table-bordered" style="table-layout:fixed">
                <tr>
                    <td class="wh whSix" title="姓名(Name)">姓名</br>Name</td>
                    <td class="wh whEight" title="用户ID(User Id)">用户ID</br>User Id</td>
                    <td class="wh whSix" title="完成率(Complete/Lesson)">完成率</br>Complete/Lesson</td>
                    <td class="wh whSix" style=" white-space: normal" title="完课点评率">完课点评率</td>
                    <#if lessonTitleList?? && lessonTitleList?size gt 0>
                        <#list lessonTitleList as lesson >
                            <td class="wh whSix" title="${lesson[0]!}(${lesson[1]!})">${lesson[0]!}</br>${lesson[1]!}</td>
                        </#list>
                    </#if>
                    <td class="wh whSix" title="定级(Grading)">定级</br>Grading</td>
                    <td class="wh whSix" title="发放电子教材(E-Book)">发放电子教材</br>E-Book</td>
                    <td class="wh200">操作</td>
                </tr>
                <#assign const=1>
                <#if clazzAveScoreList?? && clazzAveScoreList?size gt 0>
                    <#list clazzAveScoreList as clazzAveScore >
                        <tr>
                            <td class="wh" title="${clazzAveScore.userName!}">${clazzAveScore.userName!}</td>
                            <td class="wh">${clazzAveScore.userId!}</td>
                            <td class="wh">${clazzAveScore.completeRate!}</td>
                            <td class="wh"></td>
                            <#if clazzAveScore.lessonScoreList?? && clazzAveScore.lessonScoreList?size gt 0>
                                <#list clazzAveScore.lessonScoreList as score >
                                    <td class="wh">${score!}</td>
                                </#list>
                            </#if>

                            <td class="wh">${clazzAveScore.grading!}</td>
                            <#if const=1 >
                                <td class="wh">
                                    <label><input id="sel_1" onchange="selectAll()" type="checkbox"
                                                  value="1"/>全选</label>
                                </td>
                                <#assign const=2>
                            <#else >
                                <td class="wh"></td>
                                <td></td>
                            </#if>
                            <td  class="wh200"></td>
                        </tr>
                    </#list>
                </#if>
                <#if userScoreList?? && userScoreList?size gt 0>
                    <#list userScoreList as userScore >
                        <tr>
                            <td class="wh">${userScore.userName!}</td>
                            <td><span id="id-${userScore.userId!}" onclick="copyToClipBoard(${userScore.userId!})">${userScore.userId!}</span></td>
                            <td class="wh">${userScore.completeRate!}</td>
                            <td class="wh">${userScore.servicedRate!}</td>
                            <#if userScore.lessonScoreList?? && userScore.lessonScoreList?size gt 0>
                                <#list userScore.lessonScoreList as score >
                                    <td class="wh">${score!}</td>
                                </#list>
                            </#if>

                            <td class="wh">${userScore.grading!}</td>
                            <td class="wh"><input type="checkbox" onclick="checkboxOnclick(this)" name="showPlay"
                                       value="${userScore.userId!}" <#if userScore.showPlay>checked</#if>></td>
                            <td  class="wh200">
                                <button userId="${userScore.userId!}" onclick="selectClick(${userScore.userId!})"
                                        class="btn btn-primary">查询
                                </button>
                                <button userId="${userScore.userId!}" onclick="scoreDetail(${userScore.userId!},'${userScore.bookId!}')"
                                        class="btn btn-primary">成绩明细
                                </button>
                                <#--<a href="/chips/user/question/index.vpage?userId=${userScore.userId!}}&productId=${productId!}">成绩明细</a>-->
                            </td>
                        </tr>
                    </#list>
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
            <li>共 ${pageData.totalPages!} 页</li>
            <#--<li>|共 ${total !} 条</li>-->
        </ul>
    </div>
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
<#--</div>-->


    <script type="text/javascript">
        function pagePost(pageNumber) {
            $("#pageNumber").val(pageNumber);
            $("#frm").attr('action', "/chips/chips/clazz/manager/userScore.vpage");
            $("#frm").submit();
        }

        function selectClick(userId) {
            window.location.href = "/chips/user/ai/detail.vpage?userId=" + userId;
        };
        function scoreDetail(userId, bookId) {
            window.location.href = "/chips/user/question/index.vpage?userId=" + userId + "&bookId=" + bookId
        };

        function copyToClipBoard(id) { //复制到剪切板
            const range = document.createRange();
            range.selectNode(document.getElementById('id-' + id));

            const selection = window.getSelection();
            if(selection.rangeCount > 0) selection.removeAllRanges();
            selection.addRange(range);
            document.execCommand("Copy");
        }

            function selectAll() {
            var isCheck = $("#sel_1").is(':checked');  //获得全选复选框是否选中
            var userIds = "";
            $("input[name='showPlay']").each(function (i) {
                this.checked = isCheck;       //循环赋值给每个复选框是否选中
                if (i == 0) {
                    userIds = $(this).val()
                } else {
                    userIds = userIds + "," + $(this).val()
                }
            });
            $.ajax({
                url: "/chips/chips/clazz/saveETextBook.vpage",
                type: "POST",
                data: {
                    "showPlay": userIds,
                    "isCheck": isCheck
                },
                success: function (res) {
                    if (res.success) {
//                    window.location.reload();
                    } else {
                        alert("保存发放电子教材失败")
                    }
                },
                error: function (e) {
                    alert("保存发放电子教材失败")
                }
            });
        }

        function checkboxOnclick(checkbox) {
            var isCheck = checkbox.checked;
            var userId = checkbox.value;
            $.ajax({
                url: "/chips/chips/clazz/saveETextBook.vpage",
                type: "POST",
                data: {
                    "showPlay": userId,
                    "isCheck": isCheck
                },
                success: function (res) {
                    if (res.success) {
//                    window.location.reload();
                    } else {
                        alert("保存发放电子教材失败")
                    }
                },
                error: function (e) {
                    alert("保存发放电子教材失败")
                }
            });
        }


        $(function () {
            $("#filter").on('click', function () {
                $("#frm").attr('action', "/chips/chips/clazz/manager/userScore.vpage");
                $("#frm").submit();
            });
            $("#find").on('click', function () {
                $("#frm1").attr('action', "/chips/chips/clazz/manager/userScore.vpage");
                $("#frm1").submit();
            });
            $("#export").on('click', function () {
                $("#frm").attr('action', "/chips/chips/clazz/userScoreExport.vpage");
                $("#frm").submit();
            });
             $("#energyExport").on('click', function () {
                $("#frm").attr('action', "/chips/chips/clazz/energyExport.vpage");
                $("#frm").submit();
            });
            $("#rank").on('click', function () {
                var clazzId = $(this).attr("clazzId");
                $.ajax({
                    type: "POST",
                    url: "/chips/chips/clazz/rank.vpage",
                    data: {
                        clazzId: clazzId
                    },
                    error: function(XMLHttpRequest){
                        alert(XMLHttpRequest.readyState)
                        alert(XMLHttpRequest.status)
                    },
                    success: function (data) {
                        $("#back_content").html("");
                        $("#back_content").html(data.info);
                        $("#courseware_detail").modal('show');
                    }
                });
            });
            $("#preview").on('click', function () {
                var clazzId = $(this).attr("clazzId");
                if(clazzId == ""){
                    alert("请选择班级")
                } else {
                    window.location.href = "/chips/user/unit/bookres/preview.vpage?clazz=" + clazzId;
                }
            });
        });

        $("#tableDiv").scroll(function(){
            var left=$("#tableDiv").scrollLeft();
            var trs=$("#tableDiv table tr");
            trs.each(function(i){
                if(i != 0) {
                    $(this).children().eq(0).css({
                        "position": "relative",
                        "top": "0px",
                        "left": left,
                        "background-color": "#f9f9f9"
                    });
                }
            });
            var top = $("#tableDiv").scrollTop();
            trs.eq(0).children().each(function (i) {
                if(i != 0) {
                    $(this).css({
                        "position": "relative",
                        "top": top, "left": "0px",
                        "background-color": "#f9f9f9"
                    });
                }
            })
            trs.eq(0).children().eq(0).css({"position": "relative", "top": top, "left": left,  "background-color": "#f9f9f9", "z-index": 1});
        });
    </script>
</@layout_default.page>