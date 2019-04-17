<#import "../../layout/project.module.ftl" as temp />
<@temp.page title="智能组卷">
<@sugar.capsule js=["core", "ZeroClipboard"] css=["teacher.widget"] />
<@app.css href="public/skin/project/examgame/skin.css" version="1.0.6" />
<div class="header"></div>
<div class="main">
    <!--//start-->
    <div class="article">
        <div class="latest_awards">
            <h1>
                <ul></ul>
            </h1>
        </div>
        ${pageBlockContentGenerator.getPageBlockContentHtml('TeacherIndex', 'TopBanner')}
        <h3 style="color: #39f; font: 24px/1.125 '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; padding: 10px 0;">通知全区老师使用</h3>
        <div class="inof_use">
            <div class="border_me">
                <h3><i class="ex_icon ex_icon_10"></i>系统消息通知</h3>
                <p style="height:26px;">发送以下内容给全区老师：（您可以修改消息内容）</p>
                <p><textarea id="exam_game_message_content" name="exam_game_message_content" cols="" rows="" class="int_vox" style="width:390px; height:140px;">一起作业网现举办新学期组卷大赛活动。组试卷还能赢园丁豆，欢迎各位老师积极参与！</textarea></p>
                <p class="text_center"><a id="exam_game_message_send_btn" href="javascript:void(0);" class="btn_mark btn_mark_well btn_mark_primary"><strong>发送通知</strong></a></p>
            </div>

            <div class="border_me row_vox_right">
                <h3><i class="ex_icon ex_icon_11"></i>发送活动链接</h3>
                <p style="height:26px;"><strong>［复制下面的内容和链接］</strong><br/>通过QQ、MSN、飞信、微博、微信、邮箱等发给老师：</p>
                <p><textarea name="" cols="" rows="" class="int_vox" style="width:390px; height:140px;" disabled="disabled" readonly="readonly" id="copy_info_url">一起作业网现举办新学期组卷大赛活动。组试卷还能赢园丁豆，欢迎各位老师积极参与！</textarea></p>
                <p class="text_center"><a href="javascript:void(0);" class="btn_mark btn_mark_well btn_mark_primary"><span id="clip_container"><span id="clip_button"><strong>复制链接地址</strong></span></span></a></p>
            </div>
            <div class="clear"></div>
        </div>
        <h1><i class="ex_icon ex_icon_3"></i><strong>活动介绍</strong></h1>
        <div class="activi_intro">
            <div class="ai_1">
                <p>智能组卷大赛火热进行中！数万道名校试题！4000个区热点题型！全新智能组卷功能让您一键创建属于自己的试卷，让学生有针对性的快速提高成绩！</p>
                <p>每单元前10份试卷各奖励<strong>50园丁豆</strong>、每日3份最优试卷各奖励<strong>200园丁豆</strong>，参与即有机会得园丁豆，最高累计可获得<strong>10000园丁豆</strong>。</p>
            </div>
            <div class="ai_2">
                <span class="book"></span>
                <p>组卷时间：2013年09月15日  至  2013年10月15日</p>
                <p>评奖时间：2013年10月16日  至  2013年11月16日</p>
            </div>
            <div class="border_line">
                <h3>活动奖励</h3>
                <p>活动期间使用规定教材组卷，可获得如下奖励：</p>
                <p><i class="num_ks">1</i>每个单元的前10份试卷，可获得<strong>50园丁豆</strong>奖励；</p>
                <p><i class="num_ks">2</i>每天3份最优组卷，奖励<strong>200园丁豆</strong>/人；</p>
            </div>
            <div class="border_line">
                <h3>组卷要求</h3>
                <p><i class="num_ks">1</i>试卷类型：单元测试卷；</p>
                <p><i class="num_ks">2</i>题型要求：符合本地区常考题型，大题型不少于3个；</p>
                <p>
                    <i class="num_ks" style="float: left;">3</i>
					<span style=" margin-left:23px; display:block;">
						题量要求：
						<br/>3-4年级每份试卷总题目数不少于8道，不超过10道；
						<br/>5-6年级每份试卷总题目数不少于10道，不超过15道；
					</span>
                </p>
                <div class="clear"></div>
                <p>
                    <i class="num_ks" style="float:left;">4</i>
					 <span style=" margin-left:23px; display:block;">
                    参赛教材：以下教材的3至6年级上册<br/>
                    《川教版-新路径小学英语(一年级起始)》、《广东版-开心学英语》、《辽师大版-快乐英语(三年级起点)》、《辽师大版-快乐英语(一年级起始)》、《闽教版-小学英语(三年级起始)》、《牛津上海版(试用本)》、《上外新世纪版小学英语》、《小学英语-山东科技版(三年级起点)》、《小学英语-教科版-广州用》、《重庆大学版小学英语》、《冀教版-小学英语(一年级起始)》、《深圳朗文版-小学英语》、《北师大版-小学英语(三年级起始)》、《陕旅版-小学英语》、《人教版精通英语》。
                </span>
                    <br/>
                    <span style=" margin-left:23px; display:block;">
                    2013年新版四年级（上）册教材：<br/>
                    《小学英语-2013年上教版(三年级起点)-四年级(上)》、《2013年-新标准小学英语(三年级起点)-四年级(上)》、《小学英语-2013年湘少版(三年级起点)-四年级(上)》、《小学英语-2013年冀教版(三年级起点)-四年级(上)》、《小学英语-2013年译林版(三年级起点)-四年级(上)》、《2013年-PEP-小学英语-四年级（上）》、
                    《小学英语-山东科技版(三年级起点)-五年级(上)》、
                    《EEC小学英语-2013年教科版(三年级起点)-四年级(上)》、
                    《2013年新版-英语Join In-四年级(上)》、
                    《2013年-重庆大学版小学英语-四年级（上）》、
                    《2013年-广东版-开心学英语-第三册》、
                    《小学英语-2013年教科版-四年级(上)-广州用》、
                    《辽师大版-快乐英语(三年级起点)-四年级(上)》、
                    《(佛山市禅城区用)英语Join In(改编版)学生用书4》。
                </span>
                </p>
                <div class="clear"></div>
            </div>
        </div>
    </div>
    <!--end//-->
</div>
<script type="text/javascript">
    $(function(){
        $17.copyToClipboard($("#copy_info_url"), $("#clip_button"));

        $("#exam_game_message_send_btn").on("click", function(){
            var content = $.trim($("#exam_game_message_content").val());

            if($17.isBlank(content)){
                $.prompt("发送内容不能为空。", {
                    title: "系统提示",
                    buttons: { "知道了": true }
                });
                return false;
            }

            if(content == "一起作业网现举办新学期组卷大赛活动。组试卷还能赢园丁豆，欢迎各位老师积极参与！"){
                content = '<a href="javascript:void(0);" target="_blank">' + content + "</a>";
            }

            $.post("/rstaff/exampaper/sendmessagetoteachers.vpage", {
                content: content
            }, function(data){
                if(data.success){
                    $.prompt("发送成功", {
                        title: "系统提示",
                        buttons: { "知道了": true }
                    });
                }else{
                    $.prompt(data.info, {
                        title: "系统提示",
                        buttons: { "知道了": true }
                    });
                }
            });
        });
    });
</script>
</@temp.page>