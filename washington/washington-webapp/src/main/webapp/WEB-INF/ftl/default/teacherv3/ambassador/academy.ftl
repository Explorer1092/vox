<#import "../../layout/project.module.ftl" as temp />
<@temp.page title="布置作业区抽大奖" header="hide">
<@app.css href="public/skin/project/ambassador/skin.css" />
<#assign recordTypeTemp = recordType!-1/>
<div class="school-am-main">
    <div class="am-head">
        <div class="am-inner">
            <div style="position: relative; width: 980px; margin: 0 auto;">
                <a href="/teacher/index.vpage" style="width: 120px; height: 55px; display: block;"></a>
            </div>
        </div>
    </div>
    <div class="am-content">
        <div class="am-inner">
            <div class="am-contentBg-box">
                <div class="partake-font-box">
                    <ul>
                        <li>1、参与答题，晋升为正式校园大使，一次性实时奖励100园丁豆。</li>
                        <li>2、每一项课程内容需全部学习完并答对该课程全部题目视为课程通过，五项课程都通过才能晋升为正式校园大使。</li>
                        <#--<li>-->
                            <#--3、新大使需在15天内学完全部学习内容，否则将被取消大使资格-->
                        <#--</li>-->
                    </ul>
                </div>
                <div class="partake-progress-box">
                    <div class="stepflex">
                        <dl class="first <#if recordTypeTemp gte 0>done<#else><#if recordTypeTemp == -1>start</#if></#if>">
                            <dt>
                                <span class="s-num am-icon am-icon-gray" data-step-index="0" data-title="入学课程"></span>
                            </dt>
                            <dd class="s-text">
                                <span class="tips"><span class="font">入学课程</span><#if recordTypeTemp gte 0><i class="am-icon am-icon-greenRight"></i></#if></span>
                            </dd>
                        </dl>
                        <dl class="normal <#if recordTypeTemp gte 1>done<#else><#if recordTypeTemp == 0>start</#if></#if>">
                            <dt>
                                <span class="s-num am-icon am-icon-gray" data-step-index="1" data-title="大一课程"></span>
                            </dt>
                            <dd class="s-text"><span class="tips"><span class="font">大一课程</span><#if recordTypeTemp gte 1><i class="am-icon am-icon-greenRight"></i></#if></span></dd>
                        </dl>
                        <dl class="normal <#if recordTypeTemp gte 2>done<#else><#if recordTypeTemp == 1>start</#if></#if>">
                            <dt>
                                <span class="s-num am-icon am-icon-gray" data-step-index="2" data-title="大二课程"></span>
                            </dt>
                            <dd class="s-text"><span class="tips"><span class="font">大二课程</span><#if recordTypeTemp gte 2><i class="am-icon am-icon-greenRight"></i></#if></span></dd>
                        </dl>
                        <dl class="normal <#if recordTypeTemp gte 3>done<#else><#if recordTypeTemp == 2>start</#if></#if>">
                            <dt>
                                <span class="s-num am-icon am-icon-gray" data-step-index="3" data-title="大三课程"></span>
                            </dt>
                            <dd class="s-text"><span class="tips"><span class="font">大三课程</span><#if recordTypeTemp gte 3><i class="am-icon am-icon-greenRight"></i></#if></span></dd>
                        </dl>
                        <dl class="normal last <#if recordTypeTemp gte 4>done<#else><#if recordTypeTemp == 3>start</#if></#if>">
                            <dt>
                                <span class="s-num am-icon am-icon-gray" data-step-index="4" data-title="大四课程"></span>
                            </dt>
                            <dd class="s-text"><span class="tips"><span class="font">大四课程</span><#if recordTypeTemp gte 4><i class="am-icon am-icon-greenRight"></i></#if></span></dd>
                        </dl>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        var description = "${(ambassadorLevel.level.description)!'实习大使'}";
        var descriptionAnswer = ["实习大使", "铜牌大使", "银牌大使", "金牌大使"];
        var topicItem = {
            0 : [{
                type : "one",
                title : "1.阅读校园大使招募帖（论坛）",
                content : "http://www.17huayuan.com/forum.php?mod=viewthread&tid=15743&extra=page%3D1"
            }, {
                type : "one",
                title : "2.阅读校园大使集体奖励制度（论坛）",
                content : "http://www.17huayuan.com/forum.php?mod=viewthread&tid=21982&extra=page%3D1"
            }],
            1 : [{
                type : "one",
                title : "1.校园大使是什么？",
                content : "校园大使是一起作业网的核心老师用户，布置作业频率和完成作业的学生数均高于普通老师，并能熟练使用一起作业网的大多数功能。同时，校园大使还能够帮助、指导本校其他老师使用一起作业网，并提升本校的“校园活跃度”。校园大使享有高于普通老师的园丁豆奖励、兑换折扣和实物奖励。"
            }, {
                type : "one",
                title : "2.正式校园大使有哪些福利待遇？",
                content : "<p>a.教学用品中心兑换享受折上9折优惠</p><p>b.享有教师等级加速特权</p><p>c.发放校园大使证书</p><p>d.优秀大使每学期一份精美礼品<p>"
            }, {
                type : "one",
                title : "3.园丁豆有什么用？",
                content : "园丁豆可以兑换成学豆，奖励给学生，学生能够用学豆在教学用品中心兑换礼品，从而能够提升学生的学习积极性。老师也可以用园丁豆在教学用品中心兑换数码产品、厨房小家电等礼品。"
            },{
                type : "two",
                title : "1.一起作业网的核心用户是：",
                content : [
                    "A: 注册用户",
                    "B: 认证老师",
                    "C: 非认证老师",
                    "D: 正式校园大使"
                ],
                answer : "4"
            }, {
                type : "two",
                title : "2.选择自己的大使级别：进入“校园大使”专区查看自己的大使级别。",
                content : [
                    "A: 实习大使",
                    "B: 铜牌大使",
                    "C: 银牌大使",
                    "D: 金牌大使"
                ],
                answer : ($.inArray(description, descriptionAnswer) + 1)
            }, {
                type : "two",
                title : "3.下面哪一项不是校园大使专享福利？",
                content : [
                    "A: 教学用品中心兑换享受折上9折优惠",
                    "B: 唤醒老师园丁豆奖励",
                    "C: 教师等级加速特权",
                    "D: 发放校园大使证书"
                ],
                answer : "2"
            }, {
                type : "two",
                title : "4.下面哪一项不属于校园大使工作职责范围？",
                content : [
                    "A: 唤醒老师",
                    "B: 邀请其他老师使用一起作业网",
                    "C: 提升本校校园活跃度",
                    "D: 每月兑换一次奖品"
                ],
                answer : "4"
            }],
            2 : [{
                type : "one",
                title : "1.什么是活跃老师？",
                content : "活跃老师指本月至少布置过1次作业，并有8名学生以上完成的老师。"
            }, {
                type : "one",
                title : "2.什么是校园活跃度？",
                content : "本月认证老师人数与本校所有认证老师的人数比例。 例如：当月本校有7名活跃老师，本校共有10名认证老师，则当月的校园月活跃度为70%。"
            }, {
                type : "one",
                title : "3.校级校园大使如何分级：",
                content : "<p>a.实习大使：通过审核，但从未唤醒或邀请老师。</p><p>b.铜牌大使：累计唤醒1-3人；或累计邀请1-2人。</p><p>c.银牌大使：累计唤醒4-9人；或累计邀请3-6人。</p><p>d.金牌大使：唤醒10人及以上；或累计邀请7人及以上。</p><p>e.唤醒人数和邀请人数均从成为大使之日算起</p>"
            }, {
                type : "one",
                title : "4.所有的校园大使都会有证书吗？",
                content : "校园大使证书的发放范围，仅限于有过邀请或唤醒记录的校园大使。每年9月份发放一次。"
            }, {
                type : "one",
                title : "5.什么是校园大使学校集体奖励？",
                content : "<p>每月活跃老师在3人以上，且活跃度在70%以上的学校，全校当月活跃老师都可获得园丁豆奖励。</p><h4>奖励办法：</h4><p>本校老师活跃度≥90%，每名活跃认证老师奖励200园丁豆；</p><p>本校老师活跃度≥80%，＜90%，每名活跃认证老师奖励150园丁豆；</p><p>本校老师活跃度≥70%，＜80%，每名活跃认证老师奖励100园丁豆；</p><p>本校老师活跃度＜70%，无园丁豆奖励</p>"
            }, {
                type : "two",
                title : "1.集体奖励的条件是：___________",
                content : [
                    "A: 本月有2名以上活跃老师，校园活跃度在50%以上",
                    "B: 本月有3名以上活跃老师，校园活跃度在70%以上",
                    "C: 本月有4名以上活跃老师，校园活跃度在80%以上",
                    "D: 本月有6名以上活跃老师，校园活跃度在90%以上"
                ],
                answer : "2"
            }, {
                type : "two",
                title : "2.张老师在成为校园大使后，唤醒了4名老师，邀请了1名老师，张老师的大使级别应该是：_______ ",
                content : [
                    "A:实习大使",
                    "B:铜牌大使",
                    "C:银牌大使",
                    "D:金牌大使"
                ],
                answer : "3"
            }, {
                type : "two",
                title : "3.（本题一个题干下面两个小题）",
                small : "银座九号小学有甲乙丙丁4名认证老师。甲老师本月布置2次作业，共有34名学生完成；乙老师本月布置1次作业，16名学生完成；丙老师本月布置3次作业，7名学生；丁老师本月布置5次作业，43人完成。银座九号小学的校园活跃度是_____ ?",
                content : [
                    "A: 25% ",
                    "B: 50% ",
                    "C: 75% ",
                    "D: 100%"
                ],
                answer : "3"
            }, {
                type : "two",
                title : "4.银座九号小学的校园活跃度为75%，该校当月是否可以获得集体奖励？",
                content : [
                    "A: 可以",
                    "B: 不可以"
                ],
                answer : "1"
            }, {
                type : "two",
                title : "5.银座九号小学本月校园活跃度为75%，每个活跃老师应获奖励______园丁豆",
                small : "<p>老师活跃度≥90%，每名活跃认证老师奖励200园丁豆；</p><p>本校老师活跃度≥80%，＜90%，每名活跃认证老师奖励150园丁豆；</p><p>本校老师活跃度≥70%，＜80%，每名活跃认证老师奖励100园丁豆；</p><p>本校老师活跃度＜70%，无园丁豆奖励</p>",
                content : [
                    "A: 100",
                    "B: 150",
                    "C: 200",
                    "D: 300"
                ],
                answer : "1"
            }],
            3 : "step",
            4 : [1, 2, 3, 4]
        };

        //点击弹出框
        var $recordTypeTemp = ${recordTypeTemp + 1};
        var $popupDataTitle = "";
        var $recordError = 0;
        $(document).on("click", "[data-step-index='"+ $recordTypeTemp +"']", function(){
            var $this = $(this);

            $popupDataTitle = $this.attr("data-title");

            $.prompt(template("T:Popup-box", { title : $popupDataTitle }),{
                prefix : "null-popup",
                buttons : {},
                classes : {
                    fade: 'jqifade',
                    close: 'w-hide'
                },
                loaded : function(){
                    $("#topicTemplate").html( template("T:content", { dataItem : topicItem[$recordTypeTemp], $index : 0, recordTypeTemp : $recordTypeTemp}) );
                }
            });
        });

        //点击开始阅读
        $(document).on("click", "[data-btn-start]", function(){
            var $this = $(this);
            var $topicType = $this.attr("data-topic-type");
            var $radioItem = $("[data-radio-key]");

            if($topicType == "two"){
                if($radioItem.hasClass("active")){
                    if($(".active[data-radio-key]").attr("data-answer-type") == $this.attr("data-answer-type")){
                        $("[data-error-type='1']").show();
                    }else{
                        $("[data-error-type='2']").show();
                        $recordError++;
                    }
                    $this.hide();
                    if($this.attr("data-btn-type") == "end"){
                        $this.siblings("[data-btn-complete]").show();
                    }else{
                        $this.siblings("[data-btn-next]").show();
                    }
                }
                return false;
            }

            $this.hide();
            if($this.attr("data-btn-type") == "end"){
                $this.siblings("[data-btn-complete]").show();
            }else{
                $this.siblings("[data-btn-next]").show();
            }
        });

        //点击下一题
        $(document).on("click", "[data-btn-next]", function(){
            var $this = $(this);
            var $index = $this.attr("data-btn-next");

            $("#topicTemplate").html( template("T:content", { dataItem : topicItem[$recordTypeTemp], $index : $index, recordTypeTemp : $recordTypeTemp}) );

            if($this.attr("data-record-type-small") == 2){
                submitAcademy();
            }

            if($this.attr("data-record-type-small") == 1){
                var intCode = $("[data-int-code='qq']");

                if(!$17.isBlank(intCode.val())){
                    $.post("/ambassador/saveqq.vpage", {qq : intCode.val()}, function(data){
                        if(data.success){
                            //成功
                        }else{
//                            $17.alert(data.info);
                        }
                    });
                }
            }
        });

        //点击完成
        $(document).on("click", "[data-btn-complete]", function(){
            var $this = $(this);

            $("#topicTemplate").html( template("T:complete", { dataTitle : $popupDataTitle, recordTypeTemp : $recordTypeTemp, recordError : $recordError}) );
            submitAcademy();
        });

        //提交Type
        function submitAcademy(){
            if($recordError == 0){
                //成功
                $.post("/ambassador/saverecord.vpage", {recordType : $recordTypeTemp}, function(data){
                    if(data.success){
                        //成功
                    }else{
//                        $17.alert(data.info);
                    }
                });
            }
        }

        //点击确定
        $(document).on("click", "[data-btn-submit]", function(){
            window.location.href = "/ambassador/academy.vpage?type=down";
            return false;
        });

        //选择
        $(document).on("click", "[data-radio-key]", function(){
            var $this = $(this);

            $this.siblings().removeClass("active").find(".am-icon-radio").removeClass("am-icon-radioCurrent");
            $this.addClass("active").find(".am-icon-radio").addClass("am-icon-radioCurrent");
        });

        if($17.getQuery("type") == "down"){
            $("html, body").animate({ scrollTop: 800 }, 200);
        }

        if($17.getQuery("step") == 3){
            $.prompt(template("T:Popup-box", { title : "大三课程" }),{
                prefix : "null-popup",
                buttons : {},
                classes : {
                    fade: 'jqifade',
                    close: 'w-hide'
                },
                loaded : function(){
                    $("#topicTemplate").html( template("T:complete", { dataTitle : "大三课程", recordTypeTemp : 2, recordError : 0}) );
                }
            });
        }
    });
</script>
<script type="text/html" id="T:Popup-box">
    <div class="school-am-alert">
        <div class="am-inner-alert">
            <div class="alert-title">
                <%==title%>
                <a class="am-icon am-icon-close" href="javascript:void(0);" data-btn-submit="end"></a>
            </div>
            <div id="topicTemplate"><div style="padding: 70px 0; text-align: center; font-size: 18px; color: #999;">加载中...</div></div>
        </div>
        <div class="am-bottom"></div>
    </div>
</script>
<script type="text/html" id="T:content">
    <%if(recordTypeTemp < 3){%>
        <%for(var i = 0; i < dataItem.length; i++){%>
            <#--阅读模版-->
            <%if(i == $index && dataItem[i].type == "one"){%>
            <div class="alert-con">
                <p class="a-title">
                    <span class="am-num"><%==dataItem[i].title%></span>
                    <span class="am-icon am-icon-tips">阅读（<%=(i+1)%>/<%=dataItem.length%>）</span>
                </p>
                <%if(recordTypeTemp != 0){%>
                <div class="a-con-box">
                    <%==dataItem[i].content%>
                </div>
                <%}%>
            </div>
            <div class="am-btn">
                <%if(recordTypeTemp == 0){%>
                <a class="am-icon am-icon-btn" href="<%=dataItem[i].content%>" target="_blank" data-btn-start="<%=i%>" <%if(dataItem.length-1 == i){%>data-btn-type="end"<%}%>>开始阅读</a>
                <%}%>
                <a class="am-icon am-icon-btn" href="javascript:void (0);" <%if(recordTypeTemp == 0){%>style="display:none;"<%}%> <%if(dataItem.length-1 > i){%>data-btn-next="<%=(i+1)%>"<%}%>>下一题</a>
                <a class="am-icon am-icon-btn" href="javascript:void (0);" style="display:none;" data-btn-complete="end">完成</a>
            </div>
            <%}%>
            <#--单选模版-->
            <%if(i == $index && dataItem[i].type == "two"){%>
                <div class="alert-con">
                    <p class="a-title">
                        <span class="am-num"><%==dataItem[i].title%></span>
                        <span class="am-icon am-icon-tips">单选（<%=(i+1)%>/<%=dataItem.length%>）</span>
                    </p>
                    <div class="a-con-box">
                        <%if(dataItem[i].small){%><div><%==dataItem[i].small%></div><%}%>
                        <div class="answer-box answer-block-box">
                            <%var content = dataItem[i].content%>
                            <%for(var c = 0; c < content.length; c++){%>
                            <span class="ab" data-radio-key="select" data-answer-type="<%=c+1%>">
                                <#--am-icon-radioCurrent-->
                                <i class="am-icon am-icon-radio"></i>
                                <em><%==content[c]%></em>
                            </span>
                            <%}%>
                        </div>
                    </div>
                </div>
                <div class="am-btn">
                    <a class="am-icon am-icon-btn" href="javascript:void (0);" data-btn-start="<%=i%>" data-answer-type="<%=dataItem[i].answer%>" data-topic-type="<%=dataItem[i].type%>" <%if(dataItem.length-1 == i){%>data-btn-type="end"<%}%>>提交</a>
                    <a class="am-icon am-icon-btn" href="javascript:void (0);" style="display:none;" data-topic-type="<%=dataItem[i].type%>" <%if(dataItem.length-1 > i){%>data-btn-next="<%=(i+1)%>"<%}%>>下一题</a>
                    <a class="am-icon am-icon-btn" href="javascript:void (0);" style="display:none;" data-btn-complete="end">完成</a>
                </div>
                <div class="judge-box am-icon am-icon-right" data-error-type="1" style="display: none;"></div>
                <div class="judge-box am-icon am-icon-wrong" data-error-type="2" style="display: none;"></div>
            <%}%>
        <%}%>
    <%}%>

    <%if(recordTypeTemp == 3){%>
        <div class="alert-con">
            <div class="a-con-box" style="text-align: center; padding: 50px 0 30px; font-size: 16px;">
                大三学习内容为校园大使专区使用方法介绍，<br/>点击【开始学习】进入学习流程
            </div>
        </div>
        <div class="am-btn">
            <a class="am-icon am-icon-btn" href="/ambassador/startlearning.vpage">开始学习</a>
        </div>
    <%}%>

    <%if(recordTypeTemp == 4){%>
        <%if($index == 0){%>
        <div class="alert-con">
            <p class="a-title">
                <span class="am-num">1、留下你的QQ号</span>
                <span class="am-icon am-icon-tips">任务（1/3）</span>
            </p>
            <div class="a-con-box">
                <input class="alert-int" type="text" data-int-code="qq">
            </div>
        </div>
        <%}%>
        <%if($index == 1){%>
        <div class="alert-con">
            <p class="a-title">
                <span class="am-num">2、绑定校园大使微信服务号</span>
                <span class="am-icon am-icon-tips">任务（2/3）</span>
            </p>
            <div class="a-con-box">
                <span class="swap">
                    <img src="//cdn.17zuoye.com/static/project/app/publiccode_teacherAcademy.jpg" width="144" height="144">
                </span>
            </div>
        </div>
        <%}%>
        <%if($index == 2){%>
        <div class="alert-con">
            <p class="a-title">
                <span class="am-num">3、恭喜你！</span>
                <span class="am-icon am-icon-tips">任务（3/3）</span>
            </p>
            <div class="a-con-box">
                <p class="success">您已经完成 大四课程 ，获得<strong class="w-red">100园丁豆</strong>奖励！</p>
            </div>
        </div>
        <%}%>
        <%if($index == 3){%>
        <div class="alert-con">
            <p class="a-title">
                <span class="am-num">成为校园大使后该做什么？</span>
            </p>
            <div class="a-con-lastBox">
                <p>1.保持本人活跃度，每月布置1-4次作业</p>
                <p>2.在“校园大使专区”查看本校状态，唤醒不活跃的老师</p>
                <p>3.将一起作业网推荐给本校其他老师，并教TA使用</p>
                <p>4.登录论坛校园大使板块，积极与全国校园大使相互交流学习</p>
            </div>
        </div>
        <%}%>
        <div class="am-btn">
            <%if($index < 3){%>
                <a class="am-icon am-icon-btn" href="javascript:void (0);" data-btn-next="<%=$index*1+1%>" data-record-type-small="<%=$index*1+1%>">确定</a>
            <%}else{%>
            <a class="am-icon am-icon-btn" href="javascript:void (0);" data-btn-submit="end">知道了</a>
            <%}%>
        </div>
    <%}%>
</script>
<script type="text/html" id="T:complete">
    <div class="alert-con">
        <div class="a-con-box" style="text-align: center; padding: 50px 0 30px; font-size: 16px;">
            <%if(recordError > 0){%>
                <p style="padding-bottom: 10px; color: #f00;">考核未通过！</p>
                <p>答对全部题目才可以通过考核，您还可以再次尝试</p>
            <%}else{%>
                <p style="padding-bottom: 10px;">恭喜你！</p>
                <%if(recordTypeTemp > 0 && recordTypeTemp <= 2){%>
                    <p>您已经完成  <%=dataTitle%> ，获得<strong class="w-red">10园丁豆</strong>奖励！</p>
                <%}else{%>
                    <p>您已经完成  <%=dataTitle%></p>
                <%}%>
            <%}%>
        </div>
    </div>
    <div class="am-btn">
        <a class="am-icon am-icon-btn" href="javascript:void (0);" data-btn-submit="end">确定</a>
    </div>
</script>
</@temp.page>