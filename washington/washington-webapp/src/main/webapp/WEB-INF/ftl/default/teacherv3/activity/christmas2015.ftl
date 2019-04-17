<#import "../../layout/project.module.ftl" as temp />
<@temp.page header="show">
    <@app.css href="public/skin/project/christmas/css/skin_pc.css"/>
    <div class="christmas">
        <div class="bgs">
            <div class="bg01"></div>
            <div class="bg02"></div>
            <div class="bg03"></div>
            <div class="bg04"></div>
            <div class="bg05"></div>
        </div>
        <div class="main">
            <div class="main-hd">
                <h3>变身圣诞老人，布置作业／测验时可将学豆装入圣诞袜来奖励孩子！</h3>
                <p>活动时间：12月21日－12月31日</p>
            </div>
            <div class="main-mn">
                <div class="receive mn">
                    <div class="inner">
                        <h2>将学豆装入圣诞袜：</h2>
                        <div class="gift"></div>
                        <a href="/teacher/homework/batchassignhomework.vpage?ref=christmas" class="get-btn click">点击装入</a>
                    </div>
                    <span class="snowflake"></span>
                </div>
                <div class="step mn">
                    <div class="inner">
                        <h2>活动具体步骤：</h2>
                        <div class="inner-box">
                            <div class="tea-intro">
                                <span class="avatar"><img src="<@app.link href="public/skin/project/christmas/images/tea-pic.png"/>"></span>
                                <div class="info">
                                    <h3>老师需要做：</h3>
                                    <p>1.布置作业／测验</p>
                                    <p>2.将学豆装入圣诞袜</p>
                                </div>
                            </div>
                            <div class="stu-intro">
                                <span class="avatar"><img src="<@app.link href="public/skin/project/christmas/images/stu-pic.png"/>"></span>
                                <div class="info">
                                    <h3>学生需要做：</h3>
                                    <p>3.完成作业／测验</p>
                                    <p>4.领取圣诞袜</p>
                                </div>
                            </div>
                        </div>
                        <div class="stu-box"></div>
                    </div>
                </div>
                <div class="table mn table-tea">
                    <div class="tea-select">
                        <select class="js-changeSelect">
                            <#list clazzs as cl>
                                <option value="${(cl.id)!}">${(cl.className)!}</option>
                            </#list>
                        </select>
                    </div>
                    <div class="inner">
                        <div class="thead">
                            <table cellpadding="0" cellspacing="0">
                                <thead>
                                <tr>
                                    <td style="width: 200px">学生</td>
                                    <td style="width: 200px">获得圣诞袜</td>
                                    <td class="no-border">获得学豆</td>
                                </tr>
                                </thead>
                            </table>
                        </div>
                        <div class="tbody" id="studentListBox"><#--content--></div>
                    </div>
                    <span class="snowflake"></span>
                </div>
            </div>
        </div>
    </div>
    <script type="text/html" id="T:studentListBox">
        <table cellpadding="0" cellspacing="0">
            <tbody>
                <%var item = data.students%>
                <%if(data.success && item.length){%>
                    <%for(var i = 0; i < item.length; i++){%>
                        <tr class="<%if(i == 0){%>first<%}%>">
                            <td style="width: 200px"><%=item[i].studentName%></td>
                            <td style="width: 200px"><%=item[i].sc%></td>
                            <td class="no-border"><%=item[i].ic%></td>
                        </tr>
                    <%}%>
                <%}else{%>
                    <tr class="first">
                        <td colspan="3">
                            <div style="width: 100%; text-align: center; line-height: 130px;">还没有学生完成作业</div>
                        </td>
                    </tr>
                <%}%>
            </tbody>
        </table>
    </script>
    <script type="text/javascript">
        $(function(){
            function getChangeList(clazzId){
                $.post("christmascsi.vpage", {clazzId : clazzId}, function(data){
                    $("#studentListBox").html( template("T:studentListBox", { data : data}) );
                });
            }

            $(document).on("change", ".js-changeSelect", function(){
                var $that = $(this);

                getChangeList($that.val());
            });

            $(".js-changeSelect:first").change();
        });
    </script>
</@temp.page>