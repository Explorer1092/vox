<#import "../../layout/project.module.ftl" as temp />
<#--teacher/reward/mobilerecharge.vpage-->
<@temp.page title="100元话费轻松拿">
    <style>
        html, body { font-family: "微软雅黑", "Microsoft YaHei", Arial, "黑体"; font-size: 14px; }
        a { text-decoration: none; color: #39f; }
        a:hover { text-decoration: underline; }
        .color_orange { color: #ff7902 !important; }
        .color_green { color: #009328 !important; }
        .color_blue { color: #39f !important; }
        .section { width: 902px; margin: 0 auto; }
        .header { background: url(<@app.link href="public/skin/project/billrewardall/images/br_02.png"/>) repeat-x; height: 280px; overflow: hidden; }
        .header h1 { background: url(<@app.link href="public/skin/project/billrewardall/images/br_04.png"/>) no-repeat; margin: 0 auto; width: 902px; height: 280px; }
        .header h1 a { display: block; float: left; width: 165px; height: 60px; }
        .aside h2 { background: url(<@app.link href="public/skin/project/billrewardall/images/br_08.png"/>) no-repeat; height: 38px; margin: 10px 0; text-indent: 110px; }
        .aside h2 p { line-height: 38px;}
        .aside h2.active{ background-image:url(<@app.link href="public/skin/project/billrewardall/images/br_08_1.png"/>);}
        .aside { padding: 10px 0; }
        .aside .step { background: url(<@app.link href="public/skin/project/billrewardall/images/step.png"/>) no-repeat 0 0; height: 122px; }
        .aside .step_1 { background-position: center -258px; }
        .aside .step_2 { background-position: center -388px; }
        .aside .step_3 { background-position: center -517px; }
        .aside .step_4 { background-position: center -647px; }
        .aside .step_5 { background-position: center -130px; }
            /*article*/
        .article { background: #f1f1f0; }
        .article h1, .article h2 { background: url(<@app.link href="public/skin/project/billrewardall/images/br_06.png"/>) no-repeat 0 0; height: 38px; line-height: 38px; border: 2px solid #ccc; padding: 0 0 0 130px; font-size: 12px; border-radius: 5px 5px 0 0; }
        .article h2 { background-image: url(<@app.link href="public/skin/project/billrewardall/images/br_07.png"/>); }
        .article dl { border: 2px solid #ccc; margin: -2px 0 0; height: 100%; border-radius: 0 0 5px 5px; }
        .article dt { float: left; background: url(<@app.link href="public/skin/project/billrewardall/images/br_09.png"/>) no-repeat 0 0; width: 130px; height: 38px; }
        .article dd { margin: 0 0 0 130px; padding: 10px 0; }
        .article dd p { line-height: 24px; color: #333; }
        .article .ol { display: inline-block; font: 11px/1.125 arial; color: #fff; background: #009328; border-radius: 100px; padding: 2px 5px; margin-right: 3px; }
        .winners { margin: 10px 0; }
        .winners dl { border-radius: 5px }
        .winners dl dt { background: url(<@app.link href="public/skin/project/billrewardall/images/br_03.png"/>) no-repeat 0 -0px; }
            /*footer*/
        .footer { clear: both; padding: 15px; color: #666; font: 12px/20px arial; }
    </style>

<!--//header-->
<div class="header">
    <h1></h1>
</div>
<div class="section">
    <!--//article-->
    <div class="article winners">
        <dl>
            <dt></dt>
            <dd>
                <div class="winners_new_list">
                ${pageBlockContentGenerator.getPageBlockContentHtml('TeacherIndex', 'billrewardallBoxItems')}
                </div>
            </dd>
        </dl>
    </div>
    <div class="article">
        <h1 class="color_green">一起作业网新注册老师<span class="color_orange"></span></h1>
        <h2 class="color_green" style="height: auto; line-height: 22px; ">2013年8月1日 至 2013年10月15日 <br/>
            <span class="text_red">活动结束后，最迟于2013年10月21日前为达到标准的老师手机充值。</span>
        </h2>
        <dl>
            <dt></dt>
            <dd>
                1.注册一起作业网，验证手机并成为认证老师；<br>
                2.活动期间（2013.8.1-2013.10.15）给学生布置作业，所有班级：<br>
                &nbsp; &nbsp;◆完成作业学生数量累计达到50人，奖励<span class="color_orange">50元</span>话费； <br>

                &nbsp;&nbsp;◆完成作业学生数量累计达到100人，再奖励<span class="color_orange">50元</span>话费；
                <br>
                &nbsp;&nbsp;◆每位老师最高奖励不超过100元。   <br>
                3.活动每周结算一次，每周五为本周达到标准的老师手机充值。
             </dd>
        </dl>
    </div>
    <!--//footer-->
    <div class="footer"><strong class="color_orange">特别声明：</strong><br>
        1.主办方将对所有参与者进行严格审核，任何恶意注册、重复注册、虚假信息等均视为舞弊，一经查出，除取消获奖资格外，还将从系统中扣除所有园丁豆和奖品兑<br>
        &nbsp;&nbsp;&nbsp; 换资格，并保留起诉的权利。<br>
        2.一起作业网拥有对此次活动的最终解释权。</div>
</div>
</@temp.page>