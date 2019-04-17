<!doctype html>
<html>
<head>
<meta charset="utf-8">
<title>${title!'一起教育科技_让学习成为美好体验'}</title>
<meta name="keywords" content="一起作业,一起作业网,17作业网,一起作业网英语,一起作业学生端,一起作业教师端,家长通,在线教育平台,学生APP">
<meta name="description" content="一起作业是一款免费学习工具，是一个学生、老师和家长三方互动的作业平台，老师轻松布置作业，学生快乐做作业，家长可以定期查看孩子的学习进度及报告，情景交融的学习模式，让孩子轻松搞定各科学习！一起作业，让学习成为美好体验。">
<link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />
<@sugar.capsule js=['jquery','alert', 'ko','core','template'] css=['plugin.alert', 'cnedu'] />
</head>
<body class="bg-02">
<div class="downloadPage-main JS-animation">
    <ul class="slides">
        <li style="display: block;">
            <!--家长通-->
            <div class="carousel-inner JS-slides-banner" data-type="2">
                <div class="item active">
                    <div class="car-left">
                        <div class="viewport-inner  JS-currentImg">
                            <ul style="width: 980px; height: 439px;">
                                <li style="float: left;">
                                    <img data-bind="attr: {src: appImg}" alt="封面图">
                                </li>
                            </ul>
                        </div>
                    </div>
                    <div class="car-right">
                        <h1 data-bind="text: title"></h1>
                        <div class="info JS-textInfo">
                            <p data-bind="text: description"></p>
                        </div>
                        <div class="content">
                            <div class="c-left" data-bind="visible: qrcodeSrc" style="display: none;">
                                <p>请用手机微信或QQ扫描二维码登录或下载APP使用</p>
                                <img data-bind="attr: {src: qrcodeSrc}">
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </li>
    </ul>
</div>

<script>
    $(function () {
        function cncEduMode() {
            var _this = this,
                cdnHeader = "<@app.link href='/'/>",
                qrurl = "${qrurl!''}",
                errInfo = "${info!''}";
                application = "${application!'17teacher'}",
                titleMap = {
                    '17teacher': '一起小学老师',
                    '17student': '一起小学学生',
                    '17juniorteacher': '一起中学老师',
                    '17juniorstudent': '一起中学学生'
                },
                descriptionMap = {
                    '17teacher': '您的高效智能教学助手：布置、检查作业、详实学情分析，尽在掌握。',
                    '17student': '陪孩子一起成长的好伙伴。写作业、听课文、背单词，快乐学习。',
                    '17juniorteacher': '你的专属教学好助手：查学情分析，个性化教与学，一个都不少。',
                    '17juniorstudent': '陪伴学生成长的好帮手。基于大数据的个性化练习，精准提高摆脱题海，让学习成为美好体验。'
                },
                appImgMap = {
                    '17teacher': cdnHeader + 'public/skin/default/images/cnedu/app_17teacher.jpg',
                    '17student': cdnHeader + 'public/skin/default/images/cnedu/app_17student.jpg',
                    '17juniorteacher': cdnHeader + 'public/skin/default/images/cnedu/app_17juniorteacher.png',
                    '17juniorstudent': cdnHeader + 'public/skin/default/images/cnedu/app_17juniorstudent.png'
                };
            _this.title = ko.observable(titleMap[application]);
            _this.description = ko.observable(descriptionMap[application]);
            _this.appImg = ko.observable(appImgMap[application]);
            _this.qrcodeSrc = ko.observable('');

            if (errInfo) {
                $17.alert(errInfo);
                return;
            }
            if (!qrurl) {
                $17.alert('无效的ticket，请刷新页面重新进入');
                return;
            }
            var middlePage = window.encodeURIComponent("${(ProductConfig.getMainSiteBaseUrl())!''}" + "/view/mobile/common/dtm?app_type=" + application + "&qrurl=" + window.encodeURIComponent(qrurl));
            _this.qrcodeSrc("https://www.17zuoye.com/qrcode?m=" + middlePage);
        }
        ko.applyBindings(new cncEduMode());
    });
</script>
</body>
</html>