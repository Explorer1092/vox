<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='用户视频审核' page_num=26>

<#--<script src="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.js"></script>-->
<#--<link href="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.css" rel="stylesheet">-->

<style type="text/css">
    body {
        line-height: 20px !important;
        font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
    }

    .navbar {
        min-height: 41px;
        height: 41px !important;
        border-width: 0;
        -webkit-box-sizing: border-box;
        -moz-box-sizing: border-box;
        box-sizing: border-box;
    }

    .navbar .navbar-inner {
        min-height: 41px;
        height: 41px !important;
        border-width: 0;
        -webkit-box-sizing: border-box;
        -moz-box-sizing: border-box;
        box-sizing: border-box;
    }

    .collapse {
        display: block;
    }

    .video_list {

    }

    .video_list .item {
        height: 460px;
        width: 220px;
        margin: 10px;
        border: 1px solid #e6e6e6;
        display: inline-block;
        box-shadow: 3px 3px 5px #DDD;
        border-radius: 3px;
        position: relative;
    }

    .video_list video {
        position: absolute;
        top: 0;
        width: 100%;
        height: 300px;
    }

    .video_list .play {
        position: absolute;
        top: 0;
        width: 100%;
        height: 300px;
        z-index: 9;
        background: rgba(0, 0, 0, 0.2);
    }

    .video_list .bottom {
        position: absolute;
        bottom: 0;
        width: 100%;
        height: 158px;
        border-top: 1px solid #e6e6e6;
        padding: 5px;
        box-sizing: border-box;
    }

    .video_list .bottom .form_detail p {
        margin: 0;
        padding: 0;
    }

    .video_list .bottom .form_detail p span {
        font-weight: 600;
        margin-left: 5px;
    }

    .video_list .bottom .select {
        width: 90px;
        display: inline-block;
        padding: 5px;
        margin-top: 5px;
    }

    .video_list .bottom .btn-box {
        position: absolute;
        bottom: 10px;
        width: 100%;
    }

    #ui-datepicker-div {
        z-index: 3000 !important;
    }
</style>
<div id="main_container" class="span9">
    <legend>用户视频审核V2</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <form id="frm" class="form-horizontal form-inline" action="/chips/user/video/examine/list.vpage">
                    <input type="hidden" id="pageNumber" name="pageNumber" value="1">
                    用户Id：
                    <input id="userId" type="text" name="userId" style="height: 29px;"
                           placeholder="精确搜索" <#if userId ?? && userId gt 0> value=${userId} </#if>>
                    用户名：
                    <input type="text" name="userName" style="height: 29px;"
                           placeholder="模糊搜索" <#if userName ?? && userName != ''> value=${userName} </#if>>
                    视频Id：
                    <input type="text" name="userVideoId" style="height: 29px;"
                           placeholder="精确搜索" <#if userVideoId ?? && userVideoId != ''> value=${userVideoId} </#if>>
                    长/短期课：
                    <select id="bookType" name="bookType" class="multiple district_select" next_level="books">
                        <option value="" <#if bookType ?? && bookType == ""> selected</#if>>请选择</option>
                        <option value="all" <#if bookType ?? && bookType == "all"> selected</#if>>全部</option>
                        <option value="long" <#if bookType ?? && bookType == "long"> selected</#if>>长期课</option>
                        <option value="short" <#if bookType ?? && bookType == "short"> selected</#if>>短期课</option>
                    </select>
                    课程：
                    <select id="book" name="book" class="multiple district_select" next_level="units">
                        <#if books??>
                            <#list books as p>
                                <option value="${p.id}" <#if book ?? && book == p.id> selected</#if>>${p.name}</option>
                            </#list>
                        </#if>
                    </select>
                    单元：
                    <select id="unitSelect" data-init='false' name="unit" class="multiple district_select">
                        <#if units??>
                            <#list units as p>
                                <option value="${p.id}" <#if unit ?? && unit == p.id> selected</#if>>${p.name}</option>
                            </#list>
                        </#if>
                    </select>
                    <br/>
                    审核状态：
                    <select id="examineStatus" name="examineStatus" style="font-size: 12px;width: 100px;">
                        <option value="" <#if examineStatus ?? || examineStatus == ''>
                                selected</#if>>全部
                        </option>
                        <#if examineStatusList?has_content>
                            <#list examineStatusList as s>
                                <option value="${s.name()!}" <#if examineStatus ?? && examineStatus == s.name()>
                                        selected</#if>>${s.getDescription()!}</option>
                            </#list>
                        </#if>
                    </select>

                    开始时间：
                    <input name="startDate" id="startDate" style="height: 29px;" type="text" placeholder="视频创建开始时间"
                           readonly="readonly" <#if startDate ?? && startDate != ''> value=${startDate} </#if>/>
                    结束时间：
                    <input name="endDate" id="endDate" type="text" style="height: 29px;" placeholder="视频创建结束时间"
                           readonly="readonly" <#if endDate ?? && endDate != ''> value=${endDate} </#if>/>
                    <input type="hidden" id="fromStatus" name="fromStatus" value="${examineStatus!}">
                    <input type="hidden" id="showTip" name="showTip" value="1">
                    <input type="hidden" id="tab" name="tab" value="${tab!}">
                    <button class="btn btn-primary">查 询</button>
                </form>
                <button class="btn btn-primary" id="export_video">导出用户视频</button>

            </div>
        </div>
    </div>
    <ul class="nav nav-tabs" role="tablist">
        <li role="presentation" <#if tab?? && tab == "1">class="active"</#if>>
            <a id="shareTab" href="javascript:void" onclick="shareTabClick()">已分享视频</a>
        </li>
        <li role="presentation"  <#if tab?? && tab == "2">class="active"</#if>>
            <a id="oneByOneTab"  href="javascript:void" onclick="oneByOneTab()">一对一点评视频</a>
        </li>
        <li role="presentation" <#if tab?? && tab == "3">class="active"</#if>>
            <a id="lastOneTab"  href="javascript:void" onclick="lastOneTab()">最后一个视频</a>
        </li>
        <li role="presentation" <#if tab?? && tab == "4">class="active"</#if>>
            <a id="otherTab"  href="javascript:void" onclick="otherTab()">不审核的视频</a>
        </li>
    </ul>
    <div class="container-fluid">
        <div class="row video_list">
            <#if pageData?? && pageData.content?? && pageData.content?size gt 0>
                <#list pageData.content as e >
                    <div class="item">
                        <video src="${e.video!}" data-videos="${e.videos!}" controls></video>
                        <div class="play"></div>
                        <div class="bottom">
                            <div class="form_detail">
                                <p style="word-break: break-all;"><span style="margin: 0px">ID:</span>${e.id!}</p>
                                <p><span style="margin: 0px">用户:</span>${e.user!}</p>
                                <p><label style="margin-right:12px">${e.lesson!}</label><label>${e.createDate!}</label>
                                </p>
                            </div>
                            <div class="form">
                                <select class="form-control select catagory"
                                        <#if e.statusName ?? && e.statusName == 'Failed'>readonly="true"
                                        disabled="disabled" </#if>>
                                    <#if categoryList ?? && categoryList?has_content>
                                        <#list categoryList as s>
                                            <option value="${s.name()!}" <#if e.categoryName?? && e.categoryName == s.name()>
                                                    selected</#if>>${s.getDescription()!}</option>
                                        </#list>
                                    </#if>
                                </select>
                                <input value="${e.statusName!}" type="hidden" class="status"/>
                                <button class="btn btn-danger btn-sm exmine  <#if e.statusName ?? && e.statusName == 'Failed'>hidden</#if>"
                                        style="margin-top:3px;" data-id="${e.id!}" data-val="0">违规
                                </button>
                                <button class="btn btn-success btn-sm exmine <#if e.statusName ?? && (e.statusName == 'Failed' || e.statusName == 'Passed')>hidden</#if>"
                                        style="margin-top:3px;" data-id="${e.id!}" data-val="1">通过
                                </button>
                            </div>
                            <div class="btn-box text-center">

                            </div>
                        </div>
                    </div>
                </#list>
                <div style="padding-right: 150px;">

                    <#--<#if  tab ?? && tab != '2'>-->
                        <span class="btn btn-success pull-right" id="allPass">一键通过</span>
                    <#--</#if>-->
                </div>
            <#else>
                <#if filterMatch ?? && filterMatch>
                    <#if showTip && showTip>
                        <strong style="color: red">暂无数据</strong>
                    </#if>
                <#else >
                    <strong style="color: red">请输入查询条件:<br/>①用户id不为空 <br/>②视频id不为空 <br/>③单元和状态均不为空 <br/>
                        ④开始时间和结束时间不为空，且结束时间大于开始时间，且结束时间-开始时间小于8天<br/>上面条件满足其中一个即可</strong>
                </#if>

            </#if>

        </div>

    </div>
    <ul class="pager">
        <#if pageData?? && (pageData.hasPrevious())>
            <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
        <#else>
            <li class="disabled"><a href="#">上一页</a></li>
        </#if>
        <#if  pageData?? && (pageData.hasNext())>
            <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
        <#else>
            <li class="disabled"><a href="#">下一页</a></li>
        </#if>
        <li>当前第 ${pageNumber!} 页 |</li>
        <li>共<#if pageData??> ${pageData.totalPages!}</#if> 页|</li>
        <li>共 ${total !} 条</li>
    </ul>

    <script type="text/javascript">
        //    console.log("longBookList","");
        <#--var longBookList = JSON.parse() ${longBooks};-->
        //    console.log("longBookList",longBookList);

        <#--var shortBookList = ${shortBooks!};-->
        <#--console.log("shortBookList",shortBookList);-->
        <#--var allBookList = ${books!};-->
        <#--console.log("allBookList",allBookList);-->
        $(function () {
            $("#startDate").datepicker({
                dateFormat: 'yy-mm-dd',  //日期格式，自己设置
                monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
                defaultDate: new Date(),
                numberOfMonths: 1,
                changeMonth: false,
                changeYear: false,
                onSelect: function (selectedDate) {
                }
            });

            $("#endDate").datepicker({
                dateFormat: 'yy-mm-dd',  //日期格式，自己设置
                monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
                defaultDate: new Date(),
                numberOfMonths: 1,
                changeMonth: false,
                changeYear: false,
                onSelect: function (selectedDate) {
                }
            });
        });
        $(function () {
            $(".play").click(function () {
                var _this = $(this)
                _this.hide();
                var videoElem = _this.prev().get(0);
                var videos = _this.prev().data("videos").split(",");
                var vLen = videos.length;
                var curr = 0;
                videoElem.src = videos[curr];
                videoElem.play();
                videoElem.addEventListener('ended', function () {
                    play();
                });

                function play() {
                    if (curr >= vLen - 1) {
                        curr = 0;
                        _this.show();
                    } else {
                        curr++;
                        videoElem.src = videos[curr];
                        videoElem.load();
                        videoElem.play();
                    }
                }
            });
            $("#export_video").click(function () {
                var userId = $("#userId").val();
                if (userId == null || userId.trim() == '') {
                    alert("用户id不能为空");
                    return;
                }
                window.location.href = "/chips/user/video/export.vpage?user=" + userId;
            })
        });


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
        function shareTabClick(pageNumber) {
            $("#tab").val("1");
            $("#pageNumber").val(pageNumber);
            $("#frm").submit();
        }
        function oneByOneTab(pageNumber) {
            $("#tab").val("2");
            $("#pageNumber").val(pageNumber);
            $("#frm").submit();
        }
        function lastOneTab(pageNumber) {
            $("#tab").val("3");
            $("#pageNumber").val(pageNumber);
            $("#frm").submit();
        }
        function otherTab(pageNumber) {
            $("#tab").val("4");
            $("#pageNumber").val(pageNumber);
            $("#frm").submit();
        }

        $(function () {
            $("#allPass").on('click', function () {
                var count = 0;
                $(".form").each(function () {
                    var id = $(this).children("button:first").attr("data-id");
                    var val = "1";
                    var category = $(this).children("select:first").val();
                    var fromStatus = $(this).children("input:first").val();
                    console.log("id",id)
                    console.log("val",val)
                    console.log("category",category)
                    console.log("fromStatus",fromStatus)
                    if(fromStatus == 'Failed' || fromStatus == 'Passed') {
                        return true;
                    }
                    count = count + 1;
                    console.log("handle id : " + id)
                        $.ajax({
                            async: false,
                            type: "POST",
                            url: "/chips/user/video/examine.vpage",
                            data: {
                                id: id,
                                category: category,
                                description: '',
                                formStatus: fromStatus,
                                status: val == '1' ? 'Passed' : 'Failed'
                            },
                            success: function (data) {
                                if (data.success) {
                                    if(data.info){
                                        alert(data.info)
                                    }
//                            alert("操作成功");
                                } else {
                                    alert(data.info);
                                }
                            }
                        });
                });
                if(count > 0){
                    location.reload();
                }
            });
            $(".exmine").on('click', function () {
                var val = $(this).attr("data-val");
                var id = $(this).attr("data-id");
                var fromStatus = $(this).parent().find("input").val();
                var category = $(this).parent().find("select").val();
                console.log(val + "#" + id + "#" + fromStatus + "#" + category);
                $.ajax({
                    type: "POST",
                    url: "/chips/user/video/examine.vpage",
                    data: {
                        id: id,
                        category: category,
                        description: '',
                        formStatus: fromStatus,
                        status: val == '1' ? 'Passed' : 'Failed'
                    },
                    success: function (data) {
                        if (data.success) {
                            if(data.info){
                                alert(data.info)
                            }
//                            alert("操作成功");
                        } else {
                            alert(data.info);
                        }
                        location.reload();
                    }
                });
            });
            $("#book").on("change", function () {
                var bookId = this.value;
                $.ajax({
                    type: "POST",
                    url: "/chips/user/video/examine/bookUnit.vpage",
                    data: {
                        bookId: bookId,
                        all: true
                    },
                    error: function (XMLHttpRequest) {
                        alert(XMLHttpRequest.readyState)
                        alert(XMLHttpRequest.status)
                    },
                    success: function (data) {
                        var units = data.unitList;
                        console.log(units);
                        $("#unitSelect").html("");
                        if (units.length > 0) {
                            for (var i = 0; i < units.length; i++) {
                                $("#unitSelect").append("<option value='" + units[i].id + "'>" + units[i].name + "</option>");
                            }
                        }
                    }
                });
            });
            $("#bookType").on("change", function () {
                var type = $(this).val();
                console.log("type", type)
                if (type == '') {
                    return;
                }
                $.ajax({
                    type: "POST",
                    url: "/chips/user/video/examine/bookUnit.vpage",
                    data: {
                        bookType: type,
                    },
                    error: function (XMLHttpRequest) {
                        alert(XMLHttpRequest.readyState)
                        alert(XMLHttpRequest.status)
                    },
                    success: function (data) {
                        var books = data.books;
                        console.log(books);
                        $("#book").html("");
                        if (books.length > 0) {
                            for (var i = 0; i < books.length; i++) {
                                $("#book").append("<option value='" + books[i].id + "'>" + books[i].name + "</option>");
                            }
                        }
                    }
                });
            });
        })
        ;
    </script>
</@layout_default.page>


