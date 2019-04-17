<!doctype html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
-->
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>一起作业-雅安，我们在一起</title>
    <style>
        html, body { width: 100%; height:100%; background: url(<@app.link href="public/skin/project/donateLove/images/yaan.jpg"/>) repeat-x 0 0 #000;}
        body, button, input, select, textarea { font: 12px/1.125 Tahoma, Geneva, sans-serif; }
        body, h1, h2, h3, h4, h5, h6, dl, dt, dd, ul, ol, li, th, td, p, blockquote, pre, form, fieldset, legend, input, button, textarea, hr { padding: 0; margin: 0;}
        input, button, select { vertical-align: middle; }
        table { border-collapse: collapse; }
        li { list-style: none outside; }
        fieldset, img { vertical-align: middle; border: 0 none; }
        address, caption, cite, code, dfn, em, i, s, th, var { font-style: normal; font-weight: normal; }
        s { vertical-align: middle; font: 0px/0px arial;}
        a { color: #666; text-decoration: none; }
        a:hover { color: #39f; }/*overall wrap*/
        .clear { clear: both; font: 0pt/0 Arial; height: 0px;}
        .postnAbs{ position:absolute; display:block; overflow:hidden; text-indent:-1000px;}
        .postnAbs, .postnAbs a{ outline:none; blr:expression(this.onFocus=this.blur()); display:block;}
            /*header*/
        .wrap{ background:url(<@app.link href="public/skin/project/donateLove/images/head.jpg"/>) no-repeat center 0;}
        .header, .main, .footer{width:1000px; margin:0 auto; }
        .header{background:url(<@app.link href="public/skin/project/donateLove/images/yaan-02.jpg"/>) no-repeat; height:450px; position:relative;}
        .header .btn{ width: 164px; height: 60px; left: 1px; top: 1px; }
        .header .btn a{ width:100%; height:100%;}
            /*img*/
        .main .m1 .btn a, .main .m2 .btn a, .footer .btn a{ background:url(<@app.link href="public/skin/project/donateLove/images/allBtn.png?1.0.1"/>) no-repeat;}
            /**/
        .main .m1, .main .m2, .main .m3{ position:relative;}
        .main .m1{ background:url(<@app.link href="public/skin/project/donateLove/images/yaan-03.jpg"/>) no-repeat; height:270px; }
        .main .m1 .btn{ width: 173px; height: 60px; left: 416px; top: 159px; }
        .main .m1 .btn a{ width:100%; height:100%; background-position:1000px 10000px;}
        .main .m1 .btn a:hover{ background-position: 0 -1px ;}
        .main .m1 .btn a:active{ background-position: 0 -68px;}
        .main .m2{ background:url(<@app.link href="public/skin/project/donateLove/images/yaan-04.jpg"/>) no-repeat; height:330px;}
        .main .m2 .textareaBox{ float: right; padding:30px 95px 0 0; position: relative;}
        .main .m2 .textareaBox textarea{ width:445px; height:185px; outline: none; border: none; font: 18px/26px "微软雅黑", "Microsoft YaHei", Arial, "黑体"; resize:none; overflow-y: auto; background:none; color:#666;}
        .main .m2 .btn{ left: 814px; top: 249px; }
        .main .m2 .btn a{ background-position: 0 -132px; width:93px; height:37px; display:block; overflow: hidden; line-height:3000px;}
        .main .m2 .btn a:hover{ background-position: 0 -172px;}
        .main .m2 .btn a:active{ background-position: 0 -211px;}
        .main .m3{ background:url(<@app.link href="public/skin/project/donateLove/images/yaan-05.jpg?1.0.1"/>) no-repeat; height:675px;}
        .footer{ background:url(<@app.link href="public/skin/project/donateLove/images/yaan-06.jpg?1.0.1"/>) no-repeat; height:450px; position:relative;}
        .footer .btn{ width: 141px; height: 43px; left: 285px; top: 132px; }
        .footer .btn a{ width:100%; height:100%; background-position:1000px 10000px;}
        .footer .btn a:hover{ background-position: 0 -255px ;}
        .footer .btn a:active{ background-position: 0 -305px;}
        .footer .shareBox{ clear:both; text-align: center; padding: 330px 0 0; width:530px;  margin:0 auto;}
        .footer .copyright{ clear:both; text-align:center; color:#ccc; padding:60px 0 0;}
            /*blessingXinyu*/
        .blessingXinyu{ float:left; width:570px; padding: 90px 0 0 62px;}
        .blessingXinyu ul{ height:525px; overflow-y:auto;}
        .blessingXinyu li{ clear:both; padding:10px; border-radius:5px;}
        .blessingXinyu li.even{ background:#ffecbe;}
        .blessingXinyu li .avatar{background:url(<@app.link href="public/skin/project/donateLove/images/avatar1.png"/>) no-repeat 0 0; float:left; width:85px; height:82px;text-align:center; padding:3px 0 0}
        .blessingXinyu li .avatar img{ width:73px; height:73px; }
        .blessingXinyu li .subbox{ float: left; width: 420px;  padding:0 0 0 20px;}
        .blessingXinyu li p{ color:#333; line-height:22px;}
        .blessingXinyu li h4{ font: bold 12px/1.125 arial; color:#963; padding: 5px 0;}
        .blessingXinyu li h4 b{ display:inline-block; padding: 0 10px 0 0;}
            /*loveListBox*/
        .loveListBox{ float:right; width:310px; height:502px; overflow:hidden; padding: 65px 15px 0 0; margin: 70px 0 10px 0;}
        .loveListBox li{ background:url(<@app.link href="public/skin/project/donateLove/images/avatar.png?1.0.1"/>) no-repeat 0 0; height:96px; clear:both;}
        .loveListBox li .avatar{ float:left; width:96px; height:83px;text-align:center; padding:13px 0 0}
        .loveListBox li .avatar img{ width:76px; height:73px; }
        .loveListBox li .subbox{ float:left; padding:0 0 0 20px;}
        .loveListBox li p{ color:#333; padding:25px 0 10px;}
        .loveListBox li h4{ font: bold 12px/1.125 arial; color:#333;padding:5px 0;}
        .loveListBox li h4 b{ color:#d6181e; display:inline; padding: 0 10px 0 0; font-size:14px;}

    </style>
    <@sugar.capsule js=["jquery", "core", "toolkit"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>

<div class="wrap">
    <div class="header">
        <div class="postnAbs btn">
            <a href="/" title="返回首页">返回首页</a>
        </div>
    </div>
    <div class="main">
        <div class="m1">
            <div class="postnAbs btn">
                <a id="donate_but" href="http://www.17zuoye.com/" title="我要捐文具">我要捐文具</a>
            </div>
        </div>
        <div class="m2">
            <div class="textareaBox">
                <label style=" position: absolute; top:28px; left: 0px; width: 220px; height: 36px; font: 18px/26px '微软雅黑','Microsoft YaHei',Arial,'黑体'; cursor: text;" for="comment_context">请留下你对雅安的祝福!</label>
                <textarea id="comment_context" cols="" rows=""></textarea>
            </div>
            <div class="btn postnAbs">
                <a id="comment_submit_but" href="javascript:void(0);" title="提交">提交</a>
            </div>
            <div style="position: absolute; color: #666; top: 285px; right: 85px; font: 14px/26px '微软雅黑','Microsoft YaHei',Arial,'黑体';">截至目前，已有<b style="color:#c00;">128723</b>位学生和老师写下了对雅安小朋友的鼓励和祝福。</div>
        </div>
        <div class="m3">
            <!--//start-->
            <div class="blessingXinyu">
            <ul>
                <li>
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/51633808a310890c77c8023e.jpg" /></div>
                    <div class="subbox">
                        <h4>邱天	江苏	扬州市	仪征市	学生</h4>
                        <p>雅安，我们在一起。</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="even">
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/avatar-36298479.jpg" /></div>
                    <div class="subbox">
                        <h4>朱德铭	山东	济南市	历下区	学生</h4>
                        <p>希望雅安的小朋友，可以坚强，勇敢的面对未来。</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li>
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/512eb7fea310d02a659d1f35.jpg" /></div>
                    <div class="subbox">
                        <h4>叶思琪	黑龙江	哈尔滨市	动力区	学生</h4>
                        <p>我相信雅安你们的家园一定会从新建设起来的</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="even">
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/avatar-36457249.jpg" /></div>
                    <div class="subbox">
                        <h4>周紫媛	福建	福州市	平潭县	学生</h4>
                        <p>"雅安小朋友，加油！好好活下去！
                            "</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li>
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/515cad88a3100d9bff26e8da.jpg" /></div>
                    <div class="subbox">
                        <h4>贾玮琛	辽宁	大连市	西岗区	学生</h4>
                        <p>雅安加油！阳光总是在风雨之后的！！加油！</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="even">
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/avatar-36396718.jpg" /></div>
                    <div class="subbox">
                        <h4>徐皖豫	江苏	淮安市	开发区	学生</h4>
                        <p>"雅安加油！！一切苦难都会结束，乌云都将会消失，阳光终会出现！！！加油！
                            "</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li>
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/5125989fa310f22f0221fbbf.jpg" /></div>
                    <div class="subbox">
                        <h4>张泽龙	江苏	扬州市	高邮市	学生</h4>
                        <p>"雅安的小朋友，不要怕！有我们在！
                            所有困难都是暂时的，一定能挺过去的！加油，雅安！
                            "</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="even">
                <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/avatar-36634477.jpg" /></div>
                    <div class="subbox">
                        <h4>徐少娜	山东	烟台市	招远市	学生</h4>
                        <p>"雅安的小朋友你们还好吗？  我相信你们一定挺过去的！加油，雅安！
                            "</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li>
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/avatar-36614814.jpg" /></div>
                    <div class="subbox">
                        <h4>郭子豪	福建	厦门市	湖里区	学生</h4>
                        <p>"所有困难都是暂时的，一定能挺过去的！加油，雅安！
                            "</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="even">
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/avatar-36598368.jpg" /></div>
                    <div class="subbox">
                        <h4>刘康民	广东	深圳市	福田区	学生</h4>
                        <p>"所有困难都是暂时的，一定能挺过去的！加油，雅安！
                            "</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="">
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/avatar-36611082.jpg" /></div>
                    <div class="subbox">
                        <h4>周瑞雪	福建	南平市	建瓯市	学生</h4>
                        <p>你们要勇敢面对现时。雅安的哥哥，姐姐，弟弟，妹妹们要勇敢。</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="even">
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/avatar-36604890.jpg" /></div>
                    <div class="subbox">
                        <h4>郑尽忠	福建	莆田市	城厢区	学生</h4>
                        <p>雅安，加油！小小的困难难不住我们炎黄子孙。加油！！</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="">
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/510a21a6a31019fb8c1cabf3.jpg" /></div>
                    <div class="subbox">
                        <h4>贾宜衡	河南	郑州市	市辖区	学生</h4>
                        <p>没事的，要对自己有信心，什么困难也难不倒你们的！加油！</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="even">
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/51091065a3107e1fe47339c0.jpg" /></div>
                    <div class="subbox">
                        <h4>柏徐云	江苏	扬州市	高邮市	学生</h4>
                        <p>四川的人团结起来，汶川能挺过去，你们也能挺过去。加油！让我们一起祈祷。</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="">
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/5162b975a31080d61b03fa7c.jpg" /></div>
                    <div class="subbox">
                        <h4>潘珺颖	福建	厦门市	海沧区	学生</h4>
                        <p>雅安的哥哥姐姐叔叔阿姨爷爷奶奶们！住你们早日恢复健康！我在这里永远永远祝福你们！</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="even">
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/avatar-36578197.jpg" /></div>
                    <div class="subbox">
                        <h4>杨宇	湖南	株洲市	醴陵市	学生</h4>
                        <p>你们要坚强，要有勇气才能战胜困难。</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="">
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/513ede75a31004fbe561d0cd.jpg" /></div>
                    <div class="subbox">
                        <h4>马钰晓	北京	市辖区	海淀区	学生</h4>
                        <p>祝你们早日重建家园！雅安，加油！坚强些！</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="even">
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/510caa87a310bdcb4c60c8bc.jpg" /></div>
                    <div class="subbox">
                        <h4>江游	湖南	湘潭市	岳塘区	学生</h4>
                        <p>雅安的小朋友们不要怕，加油!</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="">
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/113633-1353285351196.jpg" /></div>
                    <div class="subbox">
                        <h4>王宝华	河北	唐山市	开平区	老师</h4>
                        <p>雅安，我是唐山人，我们坚强努力，幸福在自己手里！我们的今天就是你们的明天！</p>
                    </div>
                    <div class="clear"></div>
                </li>
                <li class="even">
                    <div class="avatar"><img src="//cdn.17zuoye.com/gridfs/117657-1356596327407.jpg" /></div>
                    <div class="subbox">
                        <h4>聂怡清	安徽	合肥市	包河区老师</h4>
                        <p>雅安别怕，您背后的祖国是强大的。</p>
                    </div>
                    <div class="clear"></div>
                </li>
            </ul>
            </div>
            <!--end//-->
            <!--//start-->
            <div class="loveListBox">
                <ul>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/294//1784289927.jpg"></div>
                        <div class="subbox">
                            <p>河北	唐山市	开平区</p>
                            <h4><b>高欣宇</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/283//4409208110.jpg"></div>
                        <div class="subbox">
                            <p>河北	石家庄市	井陉县</p>
                            <h4><b>高郡婵</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/283//4409208110.jpg"></div>
                        <div class="subbox">
                            <p>湖北	宜昌市	枝江市</p>
                            <h4><b>秦晓岚</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/351//2865334087.jpg"></div>
                        <div class="subbox">
                            <p>四川	宜宾市	南溪县</p>
                            <h4><b>王一茹</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/281//9008795404.jpg"></div>
                        <div class="subbox">
                            <p>四川	宜宾市	南溪县</p>
                            <h4><b>王一茹</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/283//4409208110.jpg"></div>
                        <div class="subbox">
                            <p>福建	福州市	仓山区</p>
                            <h4><b>吴奕</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/283//4409208110.jpg"></div>
                        <div class="subbox">
                            <p>广东	东莞市	南城区</p>
                            <h4><b>黄明钰</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/362//9273984609.jpg"></div>
                        <div class="subbox">
                            <p>山东	济南市	济阳县</p>
                            <h4><b>刘秀建</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/293//3092724055.jpg"></div>
                        <div class="subbox">
                            <p>吉林	长春市	经济开发区</p>
                            <h4><b>赵东航</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/364//3930861156.jpg"></div>
                        <div class="subbox">
                            <p>吉林	长春市	经济开发区</p>
                            <h4><b>王禹涵</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/283//4409208110.jpg"></div>
                        <div class="subbox">
                            <p>吉林	长春市	经济开发区</p>
                            <h4><b>王禹涵</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/294//1784289927.jpg"></div>
                        <div class="subbox">
                            <p>吉林	长春市	经济开发区</p>
                            <h4><b>赵东航</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/351//2865334087.jpg"></div>
                        <div class="subbox">
                            <p>北京	市辖区	门头沟区</p>
                            <h4><b>李佳乐</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/283//4409208110.jpg"></div>
                        <div class="subbox">
                            <p>北京	市辖区	门头沟区</p>
                            <h4><b>李佳乐</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/294//1784289927.jpg"></div>
                        <div class="subbox">
                            <p>江苏	扬州市	广陵区</p>
                            <h4><b>缪卓含</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/283//4409208110.jpg"></div>
                        <div class="subbox">
                            <p>吉林	长春市	经济开发区</p>
                            <h4><b>曾子孟</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/283//4409208110.jpg"></div>
                        <div class="subbox">
                            <p>山东	滨州市	滨城区</p>
                            <h4><b>王文豪</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/294//1784289927.jpg"></div>
                        <div class="subbox">
                            <p>吉林	长春市	朝阳区</p>
                            <h4><b>刘容坤</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/281//9008795404.jpg"></div>
                        <div class="subbox">
                            <p>山西	长治市	武乡县</p>
                            <h4><b>赵凌云</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/283//4409208110.jpg"></div>
                        <div class="subbox">
                            <p>山东	滨州市	滨城区</p>
                            <h4><b>程铭岳</b>学生捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/281//9008795404.jpg"></div>
                        <div class="subbox">
                            <p>山东	济南市	市中区</p>
                            <h4><b>刘蕾</b>老师捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/293//3092724055.jpg"></div>
                        <div class="subbox">
                            <p>福建	厦门市	湖里区</p>
                            <h4><b>林安农</b>老师捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/281//9008795404.jpg"></div>
                        <div class="subbox">
                            <p>福建	厦门市	湖里区</p>
                            <h4><b>林安农</b>老师捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/282//4834194321.jpg"></div>
                        <div class="subbox">
                            <p>北京	市辖区	东城区</p>
                            <h4><b>胡亚竹</b>老师捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/378//6357547316.jpg"></div>
                        <div class="subbox">
                            <p>江苏	镇江市	丹阳市</p>
                            <h4><b>郦仙萍</b>老师捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/281//9008795404.jpg"></div>
                        <div class="subbox">
                            <p>湖北	襄阳市	枣阳市</p>
                            <h4><b>邱新红</b>老师捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/447//5437885466.jpg"></div>
                        <div class="subbox">
                            <p>湖北	襄阳市	枣阳市</p>
                            <h4><b>邱新红</b>老师捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/383//5429669801.jpg"></div>
                        <div class="subbox">
                            <p>北京	市辖区	怀柔区</p>
                            <h4><b>杨俊岭</b>老师捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/314//2088981780.jpg"></div>
                        <div class="subbox">
                            <p>北京	市辖区	怀柔区</p>
                            <h4><b>杨俊岭</b>老师捐赠</h4>
                        </div>
                    </li>
                    <li>
                        <div class="avatar"><img src="http://reward.17zuoye.com/static/images/skuimages/0/0/281//9008795404.jpg"></div>
                        <div class="subbox">
                            <p>北京	市辖区	怀柔区</p>
                            <h4><b>杨俊岭</b>老师捐赠</h4>
                        </div>
                    </li>
                </ul>
            </div>
               <div style="position: absolute; color: #666; top: 651px; right: 45px; font: 14px/26px '微软雅黑','Microsoft YaHei',Arial,'黑体';">截至目前，已有<b style="color:#c00;">30091</b>位学生和老师为雅安小朋友捐赠了文具等物品。</div>
           <!--end//-->
        </div>
    </div>
    <div class="footer">
        <div class="postnAbs btn">
            <a href="http://weibo.com/yiqizuoye" title="关注一起作业" target="_blank">关注一起作业</a>
        </div>
        <div class="shareBox">
            <!-- JiaThis Button BEGIN -->
            <div class="jiathis_style">
                <span class="jiathis_txt">分享到：</span>
                <a class="jiathis_button_qzone">QQ空间</a>
                <a class="jiathis_button_tsina">新浪微博</a>
                <a class="jiathis_button_tqq">腾讯微博</a>
                <a class="jiathis_button_renren">人人网</a>
                <a class="jiathis_button_kaixin001">开心网</a>
                <a class="jiathis_button_douban">豆瓣</a>
                <a href="http://www.jiathis.com/share" class="jiathis jiathis_txt jiathis_separator jtico jtico_jiathis" target="_blank">更多</a>
            </div>
            <script type="text/javascript" >
                var jiathis_config={
                    url:"http://www.17zuoye.com/project/donateLove/index.vpage",
                    title:"#为雅安献爱心# 雅安发生地震后，当地很多受灾小朋友失去了亲人和家园，校舍倒塌，无法正常上学。@一起作业网 作为全国最大的中小学生在线作业平台，号召所有用户关注雅安，力挺雅安！希望全国同学们行动起来，为灾区小朋友献爱心、送祝福！",
                    summary:" ",
                    pic:"//cdn.17zuoye.com/public/skin/project/donateLove/images/love.png",
                    hideMore:false
                }
            </script>
            <script type="text/javascript" src="http://v3.jiathis.com/code/jia.js" charset="utf-8"></script>
            <!-- JiaThis Button END -->
        </div>
        <div class="copyright">
        ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
        </div>
    </div>
</div>
<@sugar.site_traffic_analyzer_end />
</body>
<script type="text/javascript">
    (function($){

        $.fn.kxbdMarquee = function(options){
            var opts = $.extend({},$.fn.kxbdMarquee.defaults, options);

            return this.each(function(){
                var $marquee    = $(this);				//滚动元素容器
                var _scrollObj  = $marquee.get(0);	    //滚动元素容器DOM
                var scrollW     = $marquee.width();		//滚动元素容器的宽度
                var scrollH     = $marquee.height();	//滚动元素容器的高度
                var $element    = $marquee.children();  //滚动元素
                var $kids       = $element.children();	//滚动子元素
                var scrollSize  = 0;//滚动元素尺寸
                var _type = (opts.direction == 'left' || opts.direction == 'right') ? 1:0;//滚动类型，1左右，0上下

                //防止滚动子元素比滚动元素宽而取不到实际滚动子元素宽度
                $element.css(_type?'width':'height',10000);
                //获取滚动元素的尺寸
                if (opts.isEqual) {
                    scrollSize = $kids[_type?'outerWidth':'outerHeight']() * $kids.length;
                }else{
                    $kids.each(function(){
                        scrollSize += $(this)[_type?'outerWidth':'outerHeight']();
                    });
                }
                //滚动元素总尺寸小于容器尺寸，不滚动
                if (scrollSize<(_type?scrollW:scrollH)) return;
                //克隆滚动子元素将其插入到滚动元素后，并设定滚动元素宽度
                $element.append($kids.clone()).css(_type?'width':'height',scrollSize*2);

                var numMoved = 0;
                function scrollFunc(){
                    var _dir = (opts.direction == 'left' || opts.direction == 'right') ? 'scrollLeft':'scrollTop';
                    if (opts.loop > 0) {
                        numMoved+=opts.scrollAmount;
                        if(numMoved>scrollSize*opts.loop){
                            _scrollObj[_dir] = 0;
                            return clearInterval(moveId);
                        }
                    }
                    if(opts.direction == 'left' || opts.direction == 'up'){
                        //newPos => 1   ,   scrollAmount ==> 1   ,   _scrollObj[_dir] ==> 0   ,   scrollSize ==> 1152
                        var newPos = _scrollObj[_dir] + opts.scrollAmount;
                        if(newPos>=scrollSize){
                            newPos -= scrollSize;
                        }
                        _scrollObj[_dir] = newPos;
                    }else{
                        var newPos = _scrollObj[_dir] - opts.scrollAmount;
                        if(newPos<=0){
                            newPos += scrollSize;
                        }
                        _scrollObj[_dir] = newPos;
                    }
                }
                //滚动开始
                var moveId = setInterval(scrollFunc, opts.scrollDelay);
                //鼠标划过停止滚动
                $marquee.hover(
                        function(){
                            clearInterval(moveId);
                        },
                        function(){
                            clearInterval(moveId);
                            moveId = setInterval(scrollFunc, opts.scrollDelay);
                        }
                );

                //控制加速运动
                if(opts.controlBtn){
                    $.each(opts.controlBtn, function(i,val){
                        $(val).bind(opts.eventA,function(){
                            opts.direction = i;
                            opts.oldAmount = opts.scrollAmount;
                            opts.scrollAmount = opts.newAmount;
                        }).bind(opts.eventB,function(){
                                    opts.scrollAmount = opts.oldAmount;
                                });
                    });
                }
            });
        };
        $.fn.kxbdMarquee.defaults = {
            isEqual:true,//所有滚动的元素长宽是否相等,true,false
            loop: 0,//循环滚动次数，0时无限
            direction: 'left',//滚动方向，'left','right','up','down'
            scrollAmount:1,//步长
            scrollDelay:20,//时长
            newAmount:3,//加速滚动的步长
            eventA:'mousedown',//鼠标事件，加速
            eventB:'mouseup'//鼠标事件，原速
        };

        $.fn.kxbdMarquee.setDefaults = function(settings) {
            $.extend( $.fn.kxbdMarquee.defaults, settings );
        };

    })(jQuery);

    $(function(){
        //loveListBox
        $('.loveListBox').kxbdMarquee({
            isEqual     : false,
            direction   : 'up',
            eventA      : 'mouseenter',
            eventB      : 'mouseleave'
        });
        <#--
        $("#comment_submit_but").lock('click',function( _this ){
            var _context = $("#comment_context").val();
            if( _context.length == 0 ){
                alert("请先填写您的祝福语！");
                return;
            }
            if(_context.length > 140){
                alert("留言已超过最大字数！");
                return;
            }
            _this.postJSON('/project/donateLove/newYaanComment.vpage', _context,function(data){
                if(data.success){
                    alert("成功发送祝福！");
                    return;
                }
                else{
                    alert("请在登录后发送祝福！");
                    location.href = '/index.vpage';
                    return;
                }
            });
        },3000);



        $("#comment_context").focus( function(){
            $(this).prev("label").hide();
        }).blur(function(){
            if( $(this).val() == "" ){
                $(this).prev("label").show();
            }else{
                $(this).prev("label").hide();
            }
        });
        -->

    }) ;
</script>
</html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
