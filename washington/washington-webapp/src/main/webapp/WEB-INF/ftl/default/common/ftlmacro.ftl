<#-- 网站客服电话 [servicePhone:客服电话，paidPhone：付费电话]-->
<#macro hotline phoneType="servicePhone"><#if phoneType == "junior">400-160-1717<#else>400-160-1717</#if></#macro>

<#--Dev Test Staging 开关-->
<#assign devTestStagingSwitch = (ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv() || ProductDevelopment.isStagingEnv()) >

<#--Dev Test 开关-->
<#assign devTestSwitch = (ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv()) >

<#--是否显示pk #36275-->
<#assign __showPkEndDate = devTestSwitch?string("2016/11/29 00:00:00","2016/12/05 00:00:00")/>
<#assign showPkEntrance = (currentUser.createTime lt __showPkEndDate?datetime('yyyy/MM/dd HH:mm:ss'))!false />

<#--应试作业 获取当前环境-->
<#macro getCurrentProductDevelopment name=''>
    <#compress>
        <#if (ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv())>
            'test'
        <#elseif ProductDevelopment.isStagingEnv()>
            'staging'
        <#elseif ProductDevelopment.isProductionEnv()>
            'prod'
        </#if>
    </#compress>
</#macro>

<#-- flash游戏功能宏 -->
<#macro flashWind data='data' title='' wsize=720>
    $.prompt(${data}, {
        title    : "${title}",
        buttons  : {},
        position : { width:${wsize}},
        close: function(){
            $('iframe').each(function(){
                var win = this.contentWindow || this;
                if(win.destroyHomeworkJavascriptObject){
                    win.destroyHomeworkJavascriptObject();
                }
            });
        }
    });
</#macro>

<#assign isInJuneForInvite = true />

<#-- 当前时间是否属于寒暑假 奖品中心使用 -->
<#assign isInSummerRange = (.now?string('M') == '6' || .now?string('M') == '7' || .now?string('M') == '8')/>
<#assign isInWinterRange = (.now?string('M') == '1' || .now?string('M') == '2' || .now?string('M') == '12')/>

<#macro gameAreaVersion typeContent="text"><#compress>
    <#--
    淄博市：张店区、淄川区、周村区
    370303,370302,370306
    徐州市：泉山区、云龙区、沛　县、开发区、睢宁县
    320311,320303,320322,320383,320324
    -->
    <#assign code = (currentStudentDetail.studentSchoolRegionCode)!0/>
    <#assign ZBcode = [370303,370302,370306]?seq_contains(code)/>
    <#assign ZBcodeLevel = (currentStudentDetail.getClazzLevelAsInteger())?? && [3,4, 5]?seq_contains(currentStudentDetail.getClazzLevelAsInteger())/>
    <#assign XZcode = [320311,320303,320322,320383,320324]?seq_contains(code)/>
    <#assign XZcodeLevel = (currentStudentDetail.getClazzLevelAsInteger())?? && [3,4]?seq_contains(currentStudentDetail.getClazzLevelAsInteger())/>
    <#if typeContent == "text">
        <#if ZBcode>淄博版</#if>
        <#if XZcode>徐州版</#if>
    </#if>
    <#if typeContent == "banner">
        <#if ZBcode && ZBcodeLevel>
        <li>
            <a onclick="$17.tongji('阿分题淄博版','课外乐园-顶部广告');" href="/afenti/api/index.vpage?ref=fairyland-big-zb" target="_blank" style="background-image: url(<@app.link href="public/skin/project/afenti/exam/afenti-zibo-banner.jpg"/>)"></a>
        </li>
        </#if>
        <#if XZcode  && XZcodeLevel>
        <li>
            <a onclick="$17.tongji('阿分题徐州版','课外乐园-顶部广告');" href="/afenti/api/index.vpage?ref=fairyland-big-xz" target="_blank" style="background-image: url(<@app.link href="public/skin/project/afenti/exam/afenti-xuzhou-banner.jpg"/>)"></a>
        </li>
        </#if>
    </#if>
    <#if typeContent == "popup" && ((ZBcode && ZBcodeLevel) || (XZcode && XZcodeLevel))>
        //阿分题地区版POPUP
        if($.cookie('aftarea') == null){
            $.prompt(template("T:AfentiAreaVersion", {}),{
                prefix : "null-popup",
                buttons : {},
                classes : {
                fade: 'jqifade',
                close: 'w-hide'
                }
            });
            $.cookie("aftarea", 7, { expires: 7 });
            return false;
        }
    </#if>
    <#if typeContent = "popupHtml" && ((ZBcode && ZBcodeLevel) || (XZcode && XZcodeLevel))>
        <script type="text/html" id="T:AfentiAreaVersion">
            <style>
                .afenti-area-bg{ background: url(<@app.link href="public/skin/project/afenti/exam/afenti-area-pop.png"/>) no-repeat 5000px 5000px;}
                .afenti-area-alertBox{ width: 546px; height: 432px; position: relative; background-position: 0 0;}
                .afenti-area-alertBox .hp-btn{ position: absolute; bottom: 70px; right: 173px;}
                .afenti-area-alertBox .hp-btn a{ width: 182px; height: 70px; text-decoration: none; display: inline-block;  text-align: center;}
                .afenti-area-alertBox .hp-btn a:hover{ background-position: -52px -431px;}
                .afenti-area-alertBox .hp-btn a:active{ background-position: -307px -430px;}
                .afenti-area-alertBox .hp-close{ position: absolute; top: 44px; right: 5px;}
                .afenti-area-alertBox .hp-close a{ width: 38px; height: 38px; display: inline-block;}
                .afenti-area-alertBox .hp-icon{ width: 187px;  height: 73px; top: 47px; right: 100px; position: absolute; z-index: 22; background: url(<@app.link href="public/skin/project/afenti/exam/afenti-area-pop-text.png"/>) no-repeat 5000px 5000px;}
                .afenti-area-alertBox .h-icon-zb{background-position: 0 0; }
                .afenti-area-alertBox .h-icon-xz{ background-position: -240px 0;}
            </style>
            <div class="afenti-area-alertBox afenti-area-bg">
                <div class="hp-icon <#if ZBcode>h-icon-zb<#else>h-icon-xz</#if>"></div>
                <div class="hp-close">
                    <a class="afenti-area-bg" href="javascript:void (0)" onclick="$.prompt.close();"></a>
                </div>
                <div class="hp-btn">
                    <a class="afenti-area-bg" href="/afenti/api/index.vpage?ref=index-popup"></a>
                </div>
            </div>
        </script>
    </#if>
</#compress></#macro>

<#-- 旧版 IE 检测 -->
<#macro oldIeInfoBox>
<#--
            <a href="http://www.17zuoye.com/static/project/ie8install/index.html" target="_blank" style="color:red; font-weight: bold;">
                <span>
                    <img src="http://cdn.17zuoye.com/static/project/ie8install/images/state_warning.png" width="20" height="20">
                    为了您能够正常使用一起作业，建议您升级您的浏览器到新版本的IE8，点击图标免费下载并安装：
                </span>
                <img src="http://cdn.17zuoye.com/static/project/ie8install/images/ie_icon.png" width="20" height="20" />IE8中文浏览器
            </a>
-->
    <!--[if lte IE 7]>
        <div id="_old_ie_info_box" style="z-index:3; background: #FFDB5E; padding:6px 0; text-align:center; ">
            <a href="<@app.liebao_setup_url />" target="_blank" style="color:red; font-weight: bold;" onclick="$17.tongji('download-liebao-banner')">
            <span><img src="//cdn.17zuoye.com/static/project/ie8install/images/state_warning.png" width="20" height="20">
                为了您能够正常使用一起作业，建议您使用猎豹安全浏览器，点击图标免费下载并安装：
            </span><img src="//cdn.17zuoye.com/static/project/ie8install/images/LBlogo.png" width="20" height="20">猎豹安全浏览器</a>
        </div>
    <![endif]-->
</#macro>

<#-- 短信验证提示 -->
<#macro smsdelay>
    <#--<div style=" border: 1px dashed #dfdc81; padding:8px 5px; color: #f00; margin: 3px; text-align: center; background-color: #fffed7;">-->
        <#--近期由于运营商扩容，部分地区短信会出现延迟情况，敬请谅解。-->
    <#--</div>-->
</#macro>

<#macro chargeinfo name="" game="">
    <#switch name>
        <#case 'exc'>
            <div class="stateThirdPartySection">
                课外练习由 <strong>北京敦煌教育科技有限责任公司</strong> 提供 <span>(部分内容需付费，请自愿使用)</span>
            </div>
            <#break >
        <#case 'all'>
            <div style=" color: #999; font: 12px/20px arial; padding: 3px 10px; border: 1px dashed #ddd; background-color: #f8f8f8; text-align: center; margin:10px 0;">
            <#if game=="1">阿分题<#elseif game=="2">冒险岛<#elseif game=="3">走遍美国<#elseif game=="4">PICARO <#else>单词达人</#if> 由北京敦煌教育科技有限责任公司提供，并授权在一起作业网使用。<br/>
            一起作业特别提示：<#if game=="1">阿分题<#elseif game=="2">冒险岛<#elseif game=="3">走遍美国<#elseif game=="4">PICARO <#else>单词达人</#if> 不是学校布置的作业，部分内容需要付费，请自愿购买。一起作业的作业板块免费。
            </div>
        <#break >
    </#switch>
</#macro>

<#-- 客服功能 -->
<#macro service type="all" phoneType="servicePhone">
    <#if phoneType == "servicePhone">
        <style type="text/css">
            .serviecBtnBox { display: inline-block; *display: inline;*zoom:1;position: relative; *margin:0 5px 0 0; vertical-align: middle;}
            .serviecBtnBox a, .serviecBtnBox .time { background: url(<@app.link href="public/skin/default/images/serviecBtnBack.png"/>) no-repeat; display: inline-block; text-align: center; }
            .serviecBtnBox .time { display: none; font: 12px/1.125 arial; position: absolute; background-position: 0 -60px; width: 115px; padding: 5px 0; z-index: 1; top: -20px; left: 0; color: #fff; }
            .serviecBtnBox a { font: 12px/1.125 "宋体"; color: #879cb7; text-shadow: 1px 1px 0 #fff; width: 93px; padding: 8px 0 8px 22px; outline: none; blr:expression(this.onFocus=this.blur());z-index: 2; position: relative; }
            .serviecBtnBox a:hover { color: #5fbeff; text-decoration: none;}
            .serviecBtnBox a:active { color: #4b95c8; }
            .serviecBtnBox a.off { background-position: 0 -30px; }
            .serviecBtnBox a.off:hover, .serviecBtnBox a.off:active { color: #879cb7; }
            .serviecBtnBox.sap a { width: 123px; background-position: -116px 0; }
            .serviecBtnBox.sap .time { width: 145px; background-position: -116px -60px; }
            .serviecBtnBox.sap a.off { background-position: -116px -30px; }
        </style>

            <#if type=="all" || type == "teacher">
            <em class="serviecBtnBox">
                <em class="time">8:00-21:00在线</em>
                <a href="javascript:void(0);" style="margin:0;" onclick="window.open('/redirector/onlinecs_new.vpage?type=teacher&question_type=question_other_pt','','width=856, height=519;');" alt="老师" data-tongji="老师-在线咨询">老师请点这里</a>
            </em>
            <script type="text/javascript">
                $(function() {
                    var _tTime = new Date().getHours();
                    if(_tTime < 8 || _tTime >= 21){
                        $(".serviecBtnBox a").html("客服不在线").addClass("off").removeAttr("onclick");
                    }else{
                        $(".serviecBtnBox a").html("老师咨询点这里").removeClass("off");
                    }
                });
            </script>
            </#if>

            <#if type=="all" || type="student">
            <em class="serviecBtnBox sap">
                <em class="time">8:00-21:00在线</em>
                <a href="javascript:void(0);" style="margin:0;" onclick="window.open('/redirector/onlinecs_new.vpage?type=teacher&question_type=question_other_pt','','width=856, height=519;');" alt="学生/家长" data-tongji="学生-在线咨询">学生/家长请点这里</a>
            </em>
            <script type="text/javascript">
                $(function() {
                    var _tTime = new Date().getHours();
                    if(_tTime < 9 || _tTime >= 21){
                        $(".serviecBtnBox.sap a").html("客服不在线").addClass("off").removeAttr("onclick");
                    }else{
                        $(".serviecBtnBox.sap a").html("学生/家长咨询点这里").removeClass("off");
                    }
                });
            </script>
            </#if>

        <script type="text/javascript">
            $(function() {
                $(".serviecBtnBox").hover( function(){
                    $(this).find(".time").show();
                },function(){
                    $(this).find(".time").hide();
                });

                //统一咨询点击次数
                $(".serviecBtnBox").on("click", function(){
                    var $this = $(this);
                    var homeHelp = $this.closest(".help");
                    if(homeHelp.hasClass("help")){
                        $17.tongji("首页-在线咨询");
                    }else{
                        $17.tongji($this.find("a").attr("data-tongji"));
                    }
            });
            });
        </script>
        </#if>
</#macro>

<#-- 右边广告栏事件 -->
<#macro allswitchbox target=".switchBox" second=3000 eff="0">
    <style type="text/css">
            /*switchBox*/
        .switchBox { position: relative;}
        .switchBox li { display: none;}
        .switchBox .tab{ text-align:center; padding:10px 0; font: 0px/0px arial;}
        .switchBox .tab .prve { width:10px; height:10px; overflow:hidden; display: inline-block; cursor: pointer; margin: 0 2px; border-radius: 10px; color: #fff; background-color: #eee;}
        .switchBox .tab .even { background-color: #bbb;}
        .switchBox .back, .switchBox .next{ margin-top: -22px; display: inline-block; padding:6px; cursor:pointer; font:1px/0px arial; border-radius:5px;}
        .switchBox .back{ float:left;}
        .switchBox .next{ float:right;}
    </style>
    <script type="text/javascript">
        $(function(){
            var id      = "${target}";
            var clazz   = "even";
            var second  = ${second};
            var index	= 0;					//default
            var idx		= $(id);				//id
            var pic		= idx.find("li");		//listBox
            var time	= setInterval(function(){
                index++;
                initSwitch();
            }, second);

            if(pic.length > 0){
                //遍历
                pic.eq(0).show().siblings().hide();
                if(pic.length > 1){
                    pic.each(function(i){
                        idx.find(".tab").append("<span class='prve "+ (i==index ? clazz : '') +"'>"+ (i+1) +"</span>");
                    });
                }
            }else{
                idx.hide();
            }

            //通用
            function initSwitch(){
                if( index >= pic.length){
                    index = 0;
                }
                if( index < 0){
                    index = pic.length-1;
                }
                idx.find(".prve").eq(index).addClass(clazz).siblings().removeClass(clazz);
                switch (${eff}){
                    case 1 :
                        pic.eq(index).fadeIn(500).siblings().fadeOut(500);
                        break;
                    default :
                        pic.eq(index).fadeIn(60).siblings().hide();
                }
            }

            //经过
            idx.find(".prve, li").on("mouseover", function(){
                clearInterval(time);
                index = $(this).prevAll().length;
                initSwitch();
            }).on("mouseout", function(){
                        time = setInterval(function(){
                            index++;
                            initSwitch();
                        }, second);
                    });

            //左点击
            idx.find(".back, .next").on("click", function(){
                switch( $(this).attr("class") ){
                    case "back":
                        index--;
                        break;
                    case "next":
                        index++;
                        break;
                }
                initSwitch();
                clearInterval(time);
                time = setInterval(function(){
                    index++;
                    initSwitch();
                }, second);
            });
        });
    </script>
</#macro>

<#macro feedbackWrongTitle types="odd" feedbackSource = "unknown">
    $("#feedback").on("click", function(){
        $.prompt("<div><span class='text_blue'>如果您发现题目出错了，请及时反馈给我们，感谢您的支持！</span><textarea id='feedbackContent' cols='91' rows='8' style='width: 94%' class='int_vox'></textarea><p class='init text_red'></p></div>",{
            title: "错题反馈",
            focus: 1,
            buttons: { "取消": false, "提交": true },
            submit: function(e, v){
                if(v){
                    var feedbackContent = $("#feedbackContent");
                    if($17.isBlank(feedbackContent.val())){
                        feedbackContent.siblings(".init").html("错题反馈不能为空。");
                        feedbackContent.focus();
                        return false;
                    }
                    if("${types}" == "odd"){
                        $17.tongji("布置作业_纠错_单班级");
                    }else{
                        $17.tongji("布置作业_纠错_多班级");
                    }
                    var feedbackType = 0;
                    if("${feedbackSource}" == "english"){
                        feedbackType = 1;
                    }
                    if("${feedbackSource}" == "math"){
                        feedbackType = 2;
                    }
                    $.post("/project/examfeedback.vpage", {
                        feedbackType    : feedbackType,
                        examId          : Exams.tempInfo.currentExam.id,
                        content         : feedbackContent.val()
                    }, function(data){
                        if(data.success){
                            $17.alert("提交成功，感谢您的支持！");
                        }
                    });
                }
            }
        });
    });
</#macro>

<#macro template name>
    <#switch name>
        <#case 'tiptop'>
        <script id="t:tiptop" type="text/html">
            <div class="dropDownBox_tip" style="display: none;">
                <span class="arrow arrowBot">◆<span class="inArrow">◆</span></span>
                <div class="tip_content" style="width: <%=width%>;"><%==msg%></div>
            </div>
        </script>
        <div id="t_tiptop"></div>
            <#break>
        <#case 'tipbottom'>
        <script id="t:tipbottom" type="text/html">
            <div class="dropDownBox_tip" style="display: none;">
                <span class="arrow">◆<span class="inArrow">◆</span></span>
                <div class="tip_content" style="width: <%=width%>;"><%==msg%></div>
            </div>
        </script>
        <div id="t_tipbottom"></div>
            <#break>
        <#case 'tipleft'>
        <script id="t:tipleft" type="text/html">
            <div class="dropDownBox_tip" style="display: none;">
                <span class="arrow arrowLeft">◆<span class="inArrow">◆</span></span>
                <div class="tip_content" style="width: <%=width%>;"><%==msg%></div>
            </div>
        </script>
        <div id="t_tipleft"></div>
            <#break>
        <#case "tipright">
        <script id="t:tipright" type="text/html">
            <div class="dropDownBox_tip" style="display: none;">
                <span class="arrow arrowRight">◆<span class="inArrow">◆</span></span>
                <div class="tip_content" style="width: <%=width%>;"><%==msg%></div>
            </div>
        </script>
        <div id="t_tipright"></div>
            <#break>
        <#case "tipbottomwhite">
        <script id="t:tipbottomwhite" type="text/html">
            <div class="dropDownBox_tip" style="display: none;">
                <span class="arrow arrowBot">◆<span class="inArrow">◆</span></span>
                <div class="tip_content" style="width: <%=width%>;"><%==msg%></div>
            </div>
        </script>
        <div id="t_tipbottomwhite"></div>
            <#break>
        <#case "pagenumber">
        <script id="t:pagenumber" type="text/html">
            <div class="quizPage">
                <span class="total">共 <strong><%=totalSize%></strong> 道同步试题</span>
                <% if(pageNum == 1){ %>
                <a href="javascript:void(0);" class="next disable">上一页</a>
                <% }else{ %>
                <a href="javascript:void(0);" data-gotopage="<%=pageNum - 1%>" class="next manualPages">上一页</a>
                <% } %>

                <% if(totalPage <= 5){ %>
                <% for(var i = 1; i <= totalPage; i++){ %>
                <a href="javascript:void(0);" data-gotopage="<%=i%>" <% if(i == pageNum){ %>class="active manualPages"<% }else{ %>class="manualPages"<% } %>><%=i%></a>
                <% } %>
                <% }else{ %>
                <% if(pageNum <= 3){ %>
                <a href="javascript:void(0);" data-gotopage="1" <% if(pageNum == 1){ %>class="active"<% }else{ %>class="manualPages"<% } %>>1</a>
                <a href="javascript:void(0);" data-gotopage="2" <% if(pageNum == 2){ %>class="active"<% }else{ %>class="manualPages"<% } %>>2</a>
                <a href="javascript:void(0);" data-gotopage="3" <% if(pageNum == 3){ %>class="active"<% }else{ %>class="manualPages"<% } %>>3</a>
                <a href="javascript:void(0);" data-gotopage="4" class="manualPages">4</a>
                <a href="javascript:void(0);" class="null">...</a>
                <% }else if(pageNum >= (totalPage - 2)){ %>
                <a href="javascript:void(0);" class="null">...</a>
                <a href="javascript:void(0);" data-gotopage="<%=totalPage - 3%>" class="manualPages"><%=totalPage - 3%></a>
                <a href="javascript:void(0);" data-gotopage="<%=totalPage - 2%>" <% if(totalPage - 2 == pageNum){ %>class="active"<% }else{ %>class="manualPages"<% } %>><%=totalPage - 2%></a>
                <a href="javascript:void(0);" data-gotopage="<%=totalPage - 1%>" <% if(totalPage - 1 == pageNum){ %>class="active"<% }else{ %>class="manualPages"<% } %>><%=totalPage - 1%></a>
                <a href="javascript:void(0);" data-gotopage="<%=totalPage%>" <% if(totalPage == pageNum){ %>class="active"<% }else{ %>class="manualPages"<% } %>><%=totalPage%></a>
                <% }else{ %>
                <a href="javascript:void(0);" class="null">...</a>
                <a href="javascript:void(0);" data-gotopage="<%=pageNum - 2%>" class="manualPages"><%=pageNum - 2%></a>
                <a href="javascript:void(0);" data-gotopage="<%=pageNum - 1%>" class="manualPages"><%=pageNum - 1%></a>
                <a href="javascript:void(0);" data-gotopage="<%=pageNum%>" class="active"><%=pageNum%></a>
                <a href="javascript:void(0);" data-gotopage="<%=pageNum + 1%>" class="manualPages"><%=pageNum + 1%></a>
                <a href="javascript:void(0);" data-gotopage="<%=pageNum + 2%>" class="manualPages"><%=pageNum + 2%></a>
                <a href="javascript:void(0);" class="null">...</a>
                <% } %>
                    <span class="jump">
                        <input type="text" value="<%=pageNum%>" class="dianGo" data-maxsize="<%=totalPage%>"/> /<%=totalPage%>页
                        <a href="javascript:void(0);" class="go dianGoBtn">GO</a>
                    </span>
                <% } %>

                <% if(pageNum == totalPage){ %>
                <a href="javascript:void(0);" class="back disable">下一页</a>
                <% }else{ %>
                <a href="javascript:void(0);" data-gotopage="<%=pageNum + 1%>" class="back manualPages">下一页</a>
                <% } %>
            </div>
        </script>
            <#break>
    </#switch>
</#macro>

<#macro garyBeansText><#compress>
    <#--小学为园丁豆-->
    <#if (currentUser.userType == 1)!false><#if (currentTeacherDetail.isPrimarySchool())!false>园丁豆<#else>学豆</#if><#else>学豆</#if>
</#compress></#macro>