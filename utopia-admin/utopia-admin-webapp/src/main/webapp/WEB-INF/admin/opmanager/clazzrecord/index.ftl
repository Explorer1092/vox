<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="班级空间管理" page_num=9>
<div id="main_container" class="span9">
    <legend>
        <strong>班级空间管理</strong>&nbsp;&nbsp;&nbsp;&nbsp;
    </legend>
    <ul class="nav nav-tabs" role="tablist">
        <li role="presentation" class="active"><a href="#record" aria-controls="record" role="tab" data-toggle="tab"><strong>班级录音管理</strong></a></li>
        <li role="presentation"><a href="#learningzone" aria-controls="learningzone" role="tab" data-toggle="tab"><strong>自学数据查询</strong></a></li>
        <li role="presentation"><a href="#headline" aria-controls="headline" role="tab" data-toggle="tab"><strong>班级新鲜事查询</strong></a></li>
        <li role="presentation"><a href="#like" aria-controls="like" role="tab" data-toggle="tab"><strong>学生点赞查询</strong></a></li>
    </ul>
    <div class="tab-content">
        <div role="tabpanel" class="tab-pane active" id="record">
            <div>
                <ul class="inline">
                    <li>
                        <label>班级ID：&nbsp;
                            <input id="clazzId" name="clazzId" type="text" class="input-large">
                        </label>
                    </li>
                    <li style="margin-left: 50px;">
                        <button id="clazz-search-btn" class="btn btn-primary">
                            <i class="icon-search icon-white"></i> 按班级查询
                        </button>
                    </li>
                    <li>
                        <label>学生ID：&nbsp;
                            <input id="studentId" name="studentId" type="text" class="input-large">
                        </label>
                    </li>
                    <li style="margin-left: 50px;">
                        <button id="student-search-btn" class="btn btn-primary">
                            <i class="icon-search icon-white"></i> 按学生查询
                        </button>
                    </li>
                    <li style="margin-left: 50px; display: none;">
                        <button id="clear-btn" class="btn btn-danger">
                            <i class="icon-trash icon-white"></i> 批量清除学生录音
                        </button>
                    </li>
                </ul>
            </div>
            <div class="row-fluid span10 well">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th style="width: 8%;">操作</th>
                        <th>学生信息</th>
                        <th style="width: 10%;">记录类型</th>
                        <th style="width: 15%;">创建时间</th>
                        <th style="width: 40%">录音URI</th>
                        <th style="width: 7%;">时长(秒)</th>
                    </tr>
                    </thead>
                    <tbody id="recordList">
                    </tbody>
                </table>
            </div>
        </div>

        <div role="tabpanel" class="tab-pane" id="learningzone">
            <div>
                <ul class="inline">
                    <li>
                        <label>学生ID：&nbsp;
                            <input id="stuId" name="stuId" type="text" class="input-large">
                        </label>
                    </li>
                    <li style="margin-left: 50px;">
                        <button id="student-learningzone-btn" class="btn btn-primary">
                            <i class="icon-search icon-white"></i> 按学生查询
                        </button>
                    </li>
                </ul>
            </div>
            <div class="row-fluid span10 well">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th colspan="3" style="text-align: center; width: 50%;">上周数据</th>
                        <th colspan="3" style="text-align: center; width: 50%;">本周数据</th>
                    </tr>
                    <tr>
                        <th style="text-align: center;">自学</th><th style="text-align: center;">闯关</th><th style="text-align: center;">竞技</th>
                        <th style="text-align: center;">自学</th><th style="text-align: center;">闯关</th><th style="text-align: center;">竞技</th>
                    </tr>
                    </thead>
                    <tbody id="learningzoneResult">
                    </tbody>
                </table>
            </div>
        </div>

        <div role="tabpanel" class="tab-pane" id="headline">
            <div>
                <ul class="inline">
                    <li>
                        <label>学生ID：&nbsp;
                            <input id="stuId_h" name="stuId_h" type="text" class="input-large">
                        </label>
                    </li>
                    <li style="margin-left: 50px;">
                        <button id="student-headline-btn" class="btn btn-primary">
                            <i class="icon-search icon-white"></i> 按学生查询
                        </button>
                    </li>
                </ul>
            </div>
            <div class="row-fluid span10 well" id="headlineList">
            </div>
        </div>

        <div role="tabpanel" class="tab-pane" id="like">
            <div>
                <ul class="inline">
                    <li>
                        <label>学生ID：&nbsp;
                            <input id="stuId_l" name="stuId_l" type="text" class="input-large">
                        </label>
                    </li>
                    <li style="margin-left: 50px;">
                        <button id="student-like-btn" class="btn btn-primary">
                            <i class="icon-search icon-white"></i> 按学生查询
                        </button>
                    </li>
                </ul>
            </div>
            <div class="row-fluid span10" id="likeList">
                <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
                    <div class="panel panel-default well">
                        <div class="panel-heading" role="tab" id="statistic">
                            <h4 class="panel-title">
                                <a role="button" data-toggle="collapse" data-parent="#accordion" href="#statisticData" aria-expanded="true" aria-controls="statisticData">
                                    被赞概览
                                </a>
                            </h4>
                        </div>
                        <div id="statisticData" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="statistic">
                            <table class="table table-bordered">
                                <thead>
                                    <th style="width: 70px;">日期</th>
                                    <th style="width: 100px;">点赞人ID</th>
                                    <th style="width: 70px;">点赞时间</th>
                                    <th>附加信息</th>
                                </thead>
                                <tbody id="statisticResult"></tbody>
                            </table>
                        </div>
                    </div>
                    <div class="panel panel-default well">
                        <div class="panel-heading" role="tab" id="headingTwo">
                            <h4 class="panel-title">
                                <a class="collapsed" role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseTwo" aria-expanded="false" aria-controls="collapseTwo">
                                    班级签到被赞
                                </a>
                            </h4>
                        </div>
                        <div id="collapseTwo" class="panel-collapse collapse" role="tabpanel" aria-labelledby="headingTwo">
                            <table class="table table-bordered">
                                <thead>
                                <th style="width: 100px;">点赞人ID</th>
                                <th style="width: 150px;">点赞时间</th>
                                <th>附加信息</th>
                                </thead>
                                <tbody id="attendanceResult"></tbody>
                            </table>
                        </div>
                    </div>
                    <div class="panel panel-default well">
                        <div class="panel-heading" role="tab" id="headingThree">
                            <h4 class="panel-title">
                                <a class="collapsed" role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseThree" aria-expanded="false" aria-controls="collapseThree">
                                    班级成就被赞
                                </a>
                            </h4>
                        </div>
                        <div id="collapseThree" class="panel-collapse collapse" role="tabpanel" aria-labelledby="headingThree">
                            <table class="table table-bordered">
                                <thead>
                                <th style="width: 100px;">点赞人ID</th>
                                <th style="width: 150px;">点赞时间</th>
                                <th>附加信息</th>
                                </thead>
                                <tbody id="achievementResult"></tbody>
                            </table>
                        </div>
                    </div>
                    <div class="panel panel-default well">
                        <div class="panel-heading" role="tab" id="clazzJournalTitle">
                            <h4 class="panel-title">
                                <a class="collapsed" role="button" data-toggle="collapse" data-parent="#accordion" href="#clazzJournalBody" aria-expanded="false" aria-controls="clazzJournalBody">
                                    班级头条被赞
                                </a>
                            </h4>
                        </div>
                        <div id="clazzJournalBody" class="panel-collapse collapse" role="tabpanel" aria-labelledby="clazzJournalTitle">
                            <table class="table table-bordered">
                                <thead>
                                <th style="width: 100px;">点赞人ID</th>
                                <th style="width: 150px;">点赞时间</th>
                                <th>附加信息</th>
                                </thead>
                                <tbody id="journalResult"></tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</div>
<script id="T:RECORD" type="text/html">
    <%if (recordList.length > 0 ) {%>
    <%for(var i = 0; i < recordList.length; i++) {%>
    <%var record = recordList[i]%>
    <tr>
        <td><a class="clear-record" href="javascript:void(0);" data-uri="<%=record.uri%>" data-sid="<%=record.userId%>"> 删 除</a></td>
        <td><a href="/crm/student/studenthomepage.vpage?studentId=<%=record.userId%>" target="_blank"><%=record.userName%>(<%=record.userId%>)</a></td>
        <td><%=record.type%></td>
        <td><%=record.createTime%></td>
        <td><a href="<%=record.uri%>" target="_blank"><%=record.uri%></a></td>
        <td><%=record.duration%></td>
    </tr>
    <% } %>
    <% } else { %>
    <tr>
        <td colspan="6" style="text-align: center; color: red;">该学生没有录音分享记录</td>
    </tr>
    <% } %>
</script>
<script id="T:HEADLINE" type="text/html">
    <%if (headlineList.length > 0 ) {%>
    <%for(var i = 0; i < headlineList.length; i++) {%>
    <%var headline = headlineList[i]%>
    <div style="margin-bottom: 4px;" class="well">
        新鲜事类型：<strong><%=headline.type%></strong> | 新鲜事时间： <strong><%=headline.createTime%></strong> | 相关用户： <strong><%=headline.userName%></strong>
    <%if (headline.cl.length > 0 ) {%>
    <table class="table table-condensed table-bordered" style="margin-bottom: 0">
        <thead>
        <tr><th colspan="4">评论列表</th> </tr>
        <th style=" width: 100px;">操作</th>
        <th>评论人</th>
        <th>评论时间</th>
        <th>评论内容</th>
        </thead>
        <tbody>
        <%for(var ci = 0; ci < headline.cl.length; ci++) {%>
        <%var com = headline.cl[ci]%>
        <tr>
            <td>
                <a href="javascript:void(0);" class="recall-headline" data-type="com"
                   data-journal="<%=headline.id%>" data-uid="<%=com.userId%>" data-comment="<%=com.comment%>">删除</a>
            </td>
            <td><%=com.userName%>(<%=com.userId%>)</td>
            <td><%=com.ct%></td>
            <td><%=com.comment%></td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <% } %>
    <%if (headline.el.length > 0 ) {%>
    <table class="table table-condensed table-bordered" style="margin-top:3px;">
        <thead>
        <tr><th colspan="3">鼓励列表</th> </tr>
        <th style=" width: 100px;">操作</th>
        <th>鼓励人</th>
        <th>鼓励时间</th>
        </thead>
        <tbody>
        <%for(var ei = 0; ei < headline.el.length; ei++) {%>
        <%var enc = headline.el[ei]%>
        <tr>
            <td>
                <a href="javascript:void(0);" class="recall-headline" data-type="enc"
                   data-journal="<%=headline.id%>" data-uid="<%=enc.userId%>" data-comment="">删除</a>
            </td>
            <td><%=enc.userName%>(<%=enc.userId%>)</td>
            <td><%=enc.ct%></td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <% } %>
    </div>
    <% } %>
    <% } else { %>
    <div style="text-align: center; color: red;">该学生没有分享的新鲜事记录</div>
    <% } %>
</script>
<script type="text/javascript">
    $(function() {
        $('#student-search-btn').on('click', function() {
            var studentId = $('#studentId').val();
            if (studentId == '') {
                alert("请输入学生ID");
                return false;
            }
            $.get('childrecord.vpage', {studentId:studentId}, function (res) {
                if (res.success) {
                    renderList(res.recordList);
                } else {
                    alert(res.info);
                }
            });
        });

        $('#clazz-search-btn').on('click', function() {
            var clazzId = $('#clazzId').val();
            if (clazzId == '') {
                alert("请输入班级ID");
                return false;
            }
            $.get('clazzrecord.vpage', { clazzId:clazzId}, function (res) {
                if (res.success) {
                    renderList(res.recordList);
                } else {
                    alert(res.info);
                }
            });
        });

        $('#clear-btn').on('click', function() {
            var studentId = $('#studentId').val();
            if (studentId == '') {
                alert("请输入学生ID");
                return false;
            }
            if (!confirm("话说这是个隐藏按钮，最好别乱玩好吧")) {
                return false;
            }
            if (!confirm("请否确认清除该学生的所有录音记录？")) {
                return false;
            }
            $.post('naughtychild.vpage', {studentId:studentId}, function (res) {
                if (res.success) {
                    alert("清除成功");
                    renderList(res.afterModify);
                } else {
                    alert(res.info);
                }
            });
        });

        $('#student-learningzone-btn').on('click', function() {
            var studentId = $('#stuId').val();
            if (studentId == '') {
                alert("请输入学生ID");
                return false;
            }
            $.get('learningzone.vpage', {studentId:studentId}, function (res) {
                if (res.success) {
                    var tmp = "";
                    for (var i = 0; i < res.result.length; ++i) {
                        tmp = tmp +  '<td style="text-align: center;">' + res.result[i] + '</td>'
                    }
                    $('#learningzoneResult').html(tmp);
                } else {
                    alert(res.info);
                }
            });
        });

        $('#student-headline-btn').on('click', function() {
            var studentId = $('#stuId_h').val();
            if (studentId == '') {
                alert("请输入学生ID");
                return false;
            }
            $.get('headline.vpage', {studentId:studentId}, function (res) {
                if (res.success) {
                    renderHeadline(res.headlineList);
                } else {
                    alert(res.info);
                }
            });
        });

        $('#student-like-btn').on('click', function() {
            var studentId = $('#stuId_l').val();
            if (studentId == '') {
                alert("请输入学生ID");
                return false;
            }
            $.get('likedata.vpage', {studentId:studentId}, function (res) {
                if (res.success) {
                    fillStatistic(res.statistic);
                    fillAttendance(res.attendance);
                    fillAchievement(res.achievements);
                    fillClazzJournal(res.journals);
                } else {
                    alert(res.info);
                }
            });
        });

    });

    $(document).on('click','.clear-record',function(){
       if (!confirm("是否确认清除该条记录")) {
           return false;
       }
       var $this = $(this);
       var uri = $this.data().uri;
       var studentId = $this.data().sid;

       $.post('naughtychild.vpage', {studentId:studentId, uri:uri}, function (res) {
            if (res.success) {
                alert("清除成功");
                renderList(res.afterModify);
            } else {
                alert(res.info);
            }
       });
    });

    $(document).on('click', '.recall-headline', function() {
        if (!confirm("是否确认清除该条记录")) {
            return false;
        }
        var $this = $(this);
        var param = {
            journalId : $this.data().journal,
            type : $this.data().type,
            userId : $this.data().uid,
            comment : $this.data().comment
        };

        console.info(param);

        $.post('reacallheadline.vpage', param, function (res) {
            if (res.success) {
                alert("清除成功");
                $('#student-headline-btn').click();
            } else {
                alert(res.info);
            }
        });
    });

    $(document).on('click','.clear-headline',function(){
        if (!confirm("是否确认清除该条记录")) {
            return false;
        }
        var $this = $(this);
        var id = $this.data().journal;

        $.post('clearheadline.vpage', {journalId:journal}, function (res) {
            if (res.success) {
                alert("清除成功");
                renderHeadline(res.headlineList);
            } else {
                alert(res.info);
            }
        });
    });

    function renderList(recordList) {
        $('#recordList').html(template("T:RECORD", {recordList:recordList}));
    }

    function renderHeadline(headlineList) {
        $('#headlineList').html(template("T:HEADLINE", {headlineList:headlineList}));
    }

    function fillStatistic(statistic) {
        var str = "";
        for (var key in statistic) {
            if (!statistic.hasOwnProperty(key)) {
                continue;
            }
            var list = statistic[key];
            for (var i=0; i<list.length; ++i) {
                str += "<tr><td>"+key+"</td>";
                var info = list[i];
                for(var j=0; j<info.length; ++j) {
                    str += "<td>" + info[j] + "</td>";
                }
               str += "</tr>"
            }
            str += "</tr>";
        }
        $('#statisticResult').html(str);
    }

    function fillAttendance(attendance) {
        var str = "";
        for (var i=0; i<attendance.length; ++i) {
            str += "<tr>";
            var info = attendance[i];
            for(var j=0; j<info.length; ++j) {
                str += "<td>" + info[j] + "</td>";
            }
            str += "</tr>"
        }
        $('#attendanceResult').html(str);
    }

    function fillAchievement(achievement) {
        var str = "";
        for (var i=0; i<achievement.length; ++i) {
            str += "<tr>";
            var info = achievement[i];
            for(var j=0; j<info.length; ++j) {
                str += "<td>" + info[j] + "</td>";
            }
            str += "</tr>"
        }
        $('#achievementResult').html(str);
    }

    function fillClazzJournal(journal) {
        var str = "";
        for (var i=0; i<journal.length; ++i) {
            str += "<tr>";
            var info = journal[i];
            for(var j=0; j<info.length; ++j) {
                str += "<td>" + info[j] + "</td>";
            }
            str += "</tr>"
        }
        $('#journalResult').html(str);
    }

</script>
</@layout_default.page>