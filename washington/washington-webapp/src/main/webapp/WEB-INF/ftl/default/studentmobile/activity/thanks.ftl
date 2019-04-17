<#import "../../layout/mobile.layout.ftl" as temp>
<@temp.page dpi=".595">
    <@sugar.capsule js=['jquery', "core", "alert", "template"] css=["plugin.alert"] />
    <@app.css href="public/skin/project/thanks2015/studentapp/skin.css" />
    <div class="thanksgivingBg-1">
        <div class="text">感恩节到了，学生们的进步成长，就是对老师辛勤培养最好的感恩！</div>
        <div class="time"><span>活动时间：11月25日—12月8日</span></div>
    </div>
    <#if target??>
        <div class="thanksgivingBg-3">
            <div class="thanksgivingTitle">你明天要努力的方向：</div>
            <#if target.target == "热爱劳动"><div class="result-1"></div></#if>
            <#if target.target == "积极发言"><div class="result-2"></div></#if>
            <#if target.target == "遵守纪律"><div class="result-3"></div></#if>
            <#if target.target == "团结友爱"><div class="result-4"></div></#if>
        </div>
    <#else>
        <div class="thanksgivingBg-2">
            <div class="thanksgivingTitle">选择你明天要努力的方向：</div>
            <div class="options">
                <div class="a js-clickSendTab" data-key="热爱劳动"></div>
                <div class="b js-clickSendTab" data-key="积极发言"></div>
                <div class="c js-clickSendTab" data-key="遵守纪律"></div>
                <div class="d js-clickSendTab" data-key="团结友爱"></div>
            </div>
        </div>
    </#if>

    <div class="thanksgivingBg-4">
        <div class="thanksgivingTitle">活动规则：</div>
        <dl>
            <dt>1、</dt>
            <dd>活动时间：11月25日——12月8日</dd>
        </dl>
        <dl>
            <dt>2、</dt>
            <dd>活动期间，你每天都可以来选择一项你要努力的方向</dd>
        </dl>
        <dl>
            <dt>3、</dt>
            <dd>我们会告诉老师，每位同学的选择</dd>
        </dl>
        <dl>
            <dt>4、</dt>
            <dd>当老师认为哪位同学达标，将奖励该同学一只火鸡</dd>
        </dl>
        <dl>
            <dt>5、</dt>
            <dd>老师根据同学们的表现，还会奖励学豆</dd>
        </dl>
        <div class="thanksgivingTitle thanksgivingMt">达成目标：</div>
        <div class="table">
            <table>
                <thead>
                <tr>
                    <td>学生</td>
                    <td>获得<img src="<@app.link href="public/skin/project/thanks2015/studentapp/thanksgivingIcon-1.png"/>" alt=""></td>
                    <td>获得<img src="<@app.link href="public/skin/project/thanks2015/studentapp/thanksgivingIcon-2.png"/>" alt=""></td>
                </tr>
                </thead>
                <tbody>
                <#if rankInfo?? && rankInfo?size gt 0>
                    <#list rankInfo as rf>
                    <tr>
                        <td>${rf.studentName!'---'}</td>
                        <td>${rf.chickenCount!0}</td>
                        <td>${rf.coinCount!0}</td>
                    </tr>
                    </#list>
                <#else>
                    <tr>
                        <td colspan="3"><div style="text-align: center; font-size: 20px; color: #fff; padding: 50px 0;">暂无数据</div></td>
                    </tr>
                </#if>
                </tbody>
            </table>
        </div>
    </div>
    <div class="thanksgivingBg-5"></div>
    <script type="text/javascript">
        $(function(){
            var $productType = "热爱劳动";
            $(document).on("click", ".js-clickSendTab", function(){
                var $this = $(this);
                $productType = $this.attr("data-key");

                $.prompt(template("T:选择弹窗", {type : $productType}),{
                    prefix : "null-popup",
                    buttons : {},
                    classes : {
                        fade: 'jqifade'
                    }
                });
            });

            $(document).on("click", ".reset", function(){
                $.prompt.close();
            });

            $(document).on("click", ".submit", function(){
                if($.inArray($productType, ["热爱劳动", "积极发言", "遵守纪律", "团结友爱"]) == -1){
                    $productType = "团结友爱";
                }

                $.post("settarget.vpage",{type : $productType, source : "studentApp"},function(data){
                    if(data.success){
                        location.href = "/student/activity/thanks.vpage";
                    }else{
                        alert(data.info);
                    }
                });
            });
        });
    </script>

    <script type="text/html" id="T:选择弹窗">
        <div class="goal-dialog">
            <div class="pro">
                <%if(type == "热爱劳动"){%><span class="button btn-rald-pro rald"></span><%}%>
                <%if(type == "积极发言"){%><span class="button btn-jjfy-pro rald"></span><%}%>
                <%if(type == "遵守纪律"){%><span class="button btn-zsjl-pro rald"></span><%}%>
                <%if(type == "团结友爱"){%><span class="button btn-tjya-pro rald"></span><%}%>
            </div>
            <p style="margin: 24px auto; clear: both; width: 100%;">明天努力的目标是
                <%if(type == "热爱劳动"){%><span class="goal-text rald">热爱劳动</span><%}%>
                <%if(type == "积极发言"){%><span class="goal-text jjfy">积极发言</span><%}%>
                <%if(type == "遵守纪律"){%><span class="goal-text zsjl">遵守纪律</span><%}%>
                <%if(type == "团结友爱"){%><span class="goal-text tjya">团结友爱</span><%}%>
            </p>
            <div><span class="btn reset" style="margin-right: 43px;">重选</span><span class="btn submit">确定</span></div>
        </div>
    </script>
</@temp.page>