<#import "../layout.ftl" as temp >
<@temp.page>
<div class="wr">
    <ul id="menu_list_box" class="tab clearfix">
        <li data-type="作业" class="yellow">作业</li>
        <li data-type="测验" class="">测验</li>
    </ul>
    <ul id="historyListBox" class="bean-list">
        <#--内容展示区-->
    </ul>
</div>

<script type="text/html" id="historyListTemplateBox">
    <#--作业历史-->
    <%if(type == "作业" && data.homeworkHistory.length > 0){%>
        <%for(var i = 0 ; i < data.homeworkHistory.length; i++){%>
            <li>
                <div class="hd">学科：
                    <%if(data.homeworkHistory[i].subject == 'ENGLISH'){%>
                        英语
                    <%}else if(data.homeworkHistory[i].subject == 'MATH'){%>
                        数学
                    <%}else if(data.homeworkHistory[i].subject == 'CHINESE'){%>
                        语文
                    <%}%>
                </div>
                <p>结束时间：<%=data.homeworkHistory[i].endDate%></p>
                    <p>
                        <%if(data.homeworkHistory[i].score != null){%>
                            我的分数 : <%=data.homeworkHistory[i].score%>
                        <%}else{%>
                            我的分数 : -
                        <%}%>
                    </p>

                <%if(data.homeworkHistory[i].state == 'UNFINISHED'){%>

                <%}else if(data.homeworkHistory[i].state == 'UNCHECKED'){%>
                    <p>作业未检查</p>
                <%}else{%>
                    <div class="state">作业已完成</div>
                <%}%>

                <%if(data.homeworkHistory[i].state == 'UNFINISHED'){%>
                    <div onclick="reDoHomework('<%=data.homeworkHistory[i].homeworkId%>','<%=(data.homeworkHistory[i].subject)%>','<%=(data.homeworkHistory[i].homeworkCardSource)%>','<%=(data.homeworkHistory[i].homeworkCardVariety)%>')" class="btn btn-orange">补做</div>
                <%}else if(data.homeworkHistory[i].state == 'UNCHECKED'){%>
                    <div class="btn btn-blue btn-dis">未检查</div>
                <%}else if(data.homeworkHistory[i].note != '' && data.homeworkHistory[i].note != null){%>
                    <div class="btn btn-blue reviewsBut" data-note="<%=data.homeworkHistory[i].note%>" data-tname="<%=data.homeworkHistory[i].commentTeacherName%>">查看评语</div>
                <%}%>
            </li>
        <%}%>
    <%}else if(type == "测验" && data.studentQuizHistory.length > 0){%>
        <#--测试历史-->
        <%for(var i = 0 ; i < data.studentQuizHistory.length; i++){%>
            <li>
                <div class="hd">学科：
                    <%if(data.studentQuizHistory[i].subjectName == 'ENGLISH'){%>
                        英语
                    <%}else if(data.studentQuizHistory[i].subjectName == 'MATH'){%>
                        数学
                    <%}else if(data.studentQuizHistory[i].subjectName == 'CHINESE'){%>
                        语文
                    <%}%>
                </div>
                <p>结束时间：<%=data.studentQuizHistory[i].endDateTime%></p>
                <p>
                    共<%=data.studentQuizHistory[i].totalQuestionNum%>题
                    答对<%=data.studentQuizHistory[i].doneRightQuestionNum%>题
                </p>
                <%if(data.studentQuizHistory[i].state && data.studentQuizHistory[i].state == 'FINISHED'){%>
                    <div class="state">已完成</div>
                <%}else if(data.studentQuizHistory[i].state && data.studentQuizHistory[i].state == 'UNFINISHED'){%>
                    <p>未完成</p>
                <%}else if(data.studentQuizHistory[i].state && data.studentQuizHistory[i].state == 'UNCHECKED') { %>
                    <div class="btn btn-blue btn-dis">未检查</div>
                <%}%>
            </li>
        <%}%>
    <%}else{%>
        <div class="no-record">
            还没有<%=type%>记录哦～
        </div>
    <%}%>
</script>


<script type="text/javascript">
    document.title = '学习记录';

    //补做作业
    function reDoHomework(homeworkId,subject,source,variety){
        if($M.isBlank(source)){
            if(window.external && ('reDoHomework' in window.external)){
                window.external.reDoHomework(homeworkId,subject);
            }else{
                $M.promptAlert('作业补做失败，请联系客服');
            }
        }else{
            var homework = {
                homework_type: subject,
                homework_id: homeworkId,
                hw_card_source: source,//跳h5还是native
                hw_card_variety: variety,//调用的go api
                is_makeup: true
            };
            if(window.external && ('doHomework' in window.external)){
                window.external.doHomework(JSON.stringify(homework));
            }else{
                $M.promptAlert('作业补做失败，请联系客服');
            }
        }

        //log
        $M.appLog('homework',{
            app: "17homework_my",
            type: "log_normal",
            module: "learningrecord",
            operation: "page_homework_history_redo_click"
        });
    }

    $(function(){
        var historyListBox = $('#historyListBox');
        var isJoinClazz = true;
        <#if !(currentStudentDetail.clazz.classLevel)??>
            isJoinClazz = false;
        </#if>

        if(isJoinClazz){
            historyListBox.html('<div style="text-align: center; padding: 3rem 0;">加载中...</div>');
        }else{
            historyListBox.html('<div class="no-record">需加入班级后才可查看</div>');

        }

        function ajaxSynLoad(url, method){
            var result;
            $.ajax({
                url: url,
                async: false,//改为同步方式
                type: (method ? method : "GET"),
                data: {},
                success: function (data) {
                    if(data.success){
                        result = data;
                    }else{
                        loginInvalid(data);
                    }

                },
                error : function (data){
                    historyListBox.html('<div style="text-align: center; padding: 3rem 0;">数据加载失败...</div>');
                }
            });
            return result;
        }

        var homeworkHistoryJson, examHistoryJson;

        $(document).on("click", "#menu_list_box li", function(){
            var $this = $(this);
            $this.addClass("yellow").siblings().removeClass("yellow");

            if($this.attr("data-type") == "作业"){
                if(!homeworkHistoryJson){
                    homeworkHistoryJson = ajaxSynLoad('/studentMobile/homework/app/history.vpage?app_version=${app_version!}');
                }

                historyListBox.html(template('historyListTemplateBox',{type: "作业", data : homeworkHistoryJson }));
            }else{
                if(!examHistoryJson){
                    examHistoryJson = ajaxSynLoad('/studentMobile/quiz/app/history.vpage?app_version=${app_version!}');
                }

                historyListBox.html(template('historyListTemplateBox',{type: "测验", data : examHistoryJson }));
            }
        });

        $("#menu_list_box li").eq(0).click();

        //查看评语
        $(document).on('click','.reviewsBut',function(){
            var $this = $(this);
            var note = $this.data('note');
            var teacherName = $this.data('tname')+"老师：";
            $M.promptAlert(note,teacherName);
        });

        //log
        $M.appLog('homework',{
            app: "17homework_my",
            type: "log_normal",
            module: "learningrecord",
            operation: "page_homework_history"
        });
    });
</script>
</@temp.page>