<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page header="show">
    <@sugar.capsule js=["DD_belatedPNG"] css=[] />
    <@app.css href="public/skin/project/thanks2015/skin.css" />
<div class="thanksgiving">
    <div class="container">
        <div class="header">
            <#if target??>
                <div class="main-selected">
                    <div class="goal-selected">
                        <p class="title" style="margin: 55px 0 0 104px;">你明天要努力的方向：</p>
                        <#if target.target == "热爱劳动"><div class="selected selected-rald"></div></#if>
                        <#if target.target == "积极发言"><div class="selected selected-jjfy"></div></#if>
                        <#if target.target == "遵守纪律"><div class="selected selected-zsjl"></div></#if>
                        <#if target.target == "团结友爱"><div class="selected selected-tjya"></div></#if>
                    </div>
                </div>
            <#else>
                <div class="main">
                    <p class="title" style="margin-bottom: 15px;">选择你明天要努力的方向：</p>
                    <div class="goal-select" >
                        <a class="button btn-rald js-clickSendTab" data-type="rald" data-key="热爱劳动" style="margin-right: 29px;"></a>
                        <a class="button btn-jjfy js-clickSendTab" data-type="jjfy" data-key="积极发言"></a>
                        <a class="button btn-zsjl js-clickSendTab" data-type="zsjl" data-key="遵守纪律" style="margin-right: 29px;"></a>
                        <a class="button btn-tjya js-clickSendTab" data-type="tjya" data-key="团结友爱"></a>
                    </div>
                </div>
            </#if>
            <div class="thsgiv-popup" style="display: none;"></div>
        </div>
        <div class="center">
            <div class="main">
                <div class="rule" style="">
                    <p class="title">活动规则：</p>
                    <div class="con">
                        <div>1、活动时间：11月25日——12月8日</div>
                        <div>2、活动期间，你每天都可以来选择一项你要努力的方向</div>
                        <div>3、我们会告诉老师，每位同学的选择</div>
                        <div>4、当老师认为哪位同学达标，将奖励该同学一只火鸡<span class="icons chicken-small"></span></div>
                        <div>5、老师根据同学们的表现，还会奖励学豆<span class="icons bean-small"></span></div>
                    </div>
                </div>
                <#if rankInfo??>
                <div class="reach">
                    <p class="title" style=" margin-bottom: 15px;">达成目标：</p>
                    <table>
                        <thead style="background-color: #674fe4;">
                        <tr>
                            <td>学生</td>
                            <td>获得<span class="icons chicken-big"></span></td>
                            <td>获得奖励<span class="icons bean-big"></span></td>
                        </tr>
                        </thead>
                        <tbody style="background-color: #2362d2;">
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
                </#if>
            </div>
        </div>
    </div>
</div>
<script type="text/html" id="T:选择弹窗">
    <div class="goal-dialog">
        <div class="pro">
            <span class="button btn-rald-pro rald"></span>
        </div>
        <p style="margin: 24px auto; clear: both; width: 100%;">明天努力的目标是
            <span class="goal-text rald">热爱劳动</span>
            <span class="goal-text jjfy">积极发言</span>
            <span class="goal-text zsjl">遵守纪律</span>
            <span class="goal-text tjya">团结友爱</span>
        </p>
        <div><span class="btn reset" style="margin-right: 43px;">重选</span><span class="btn submit">确定</span></div>
    </div>
</script>

<script type="text/javascript">
$(function(){
    var $productType = "热爱劳动";
    $(document).on("click", ".js-clickSendTab", function(){
        var $this = $(this);
        $productType = $this.attr("data-key");

        $.prompt(template("T:选择弹窗", {}),{
            prefix : "null-popup",
            buttons : {},
            loaded : function(){
                $(".pro .button").removeClass().addClass("button "+"btn-"+$this.data('type')+"-pro");
                $(".goal-text."+$this.data('type')).show().siblings().hide();
            },
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
            $productType = "热爱劳动";
        }

        $.post("settarget.vpage",{type: $productType, source : "studentPC"},function(data){
            if(data.success){
                location.href = "/student/activity/thanks.vpage";
            }else{
                $17.alert(data.info);
            }
        });
    });
});
</script>
</@temp.page>