<#macro page title="一起教育科技_让学习成为美好体验" htmlClass="" bodyClass="">
    <#assign mainSiteBaseUrl = (ProductConfig.getMainSiteBaseUrl())!''>
<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <title>${title!'一起教育科技_让学习成为美好体验'}</title>
    <meta name="keywords" content="一起作业,一起作业网,17作业网,一起作业网英语,一起作业学生端,一起作业教师端,家长通,在线教育平台,学生APP">
    <meta name="description" content="一起作业是一款免费学习工具，是一个学生、老师和家长三方互动的作业平台，老师轻松布置作业，学生快乐做作业，家长可以定期查看孩子的学习进度及报告，情景交融的学习模式，让孩子轻松搞定各科学习！一起作业，让学习成为美好体验。">
    <link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />
    <@sugar.capsule js=['jquery','alert', 'ko','core','template'] css=['plugin.alert', 'cjlschool'] />
</head>
<body class="${bodyClass!}">

    <#nested />

<div class="zy-homeFooter">
    <div class="innerBox">
        <div class="left">
            <#--<div class="il-ppxd">
                <span class="il-md">兄弟品牌：</span>
                <a href="http://www.ustalk.com" target="_blank"><i class="zy-icon icon-ustalk"></i></a>
                <a href="${(ProductConfig.getMainSiteBaseUrl())!}/help/downloadApp.vpage?refrerer=pc&count=2" target="_blank"><i class="zy-icon icon-parent"></i><span>家长通</span></a>
            &lt;#&ndash;<a href="http://kuailexue.com" target="_blank"><i class="zy-icon icon-kuailexue"></i><span>快乐学</span></a>&ndash;&gt;
            </div>-->
            <div class="text">
            ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
            </div>
        </div>
        <div class="right">
            <div class="cell">
                <a href="javascript:;" class="tag" style="cursor: default;">使用须知</a>
                <a href="${mainSiteBaseUrl}/help/serviceagreement.vpage?agreement=0" target="_blank"><i class="zy-icon icon-agree1"></i><span>用户协议</span></a>
                <a href="${mainSiteBaseUrl}/help/privacyprotection.vpage" target="_blank"><i class="zy-icon icon-agree2"></i><span>隐私保护</span></a>
            </div>
            <div class="cell" style="width:120px;">
                <a href="javascript:;" class="tag" style="cursor: default;">联系我们</a>
                <a href="${mainSiteBaseUrl}/help/kf/index.vpage" target="_blank"><i class="zy-icon icon-bzfk"></i><span>帮助反馈</span></a>
                <a href="javascript:;"><i class="zy-icon icon-tel"></i><span>400-160-1717</span></a>
            </div>
            <div class="cell">
                <a href="javascript:;" class="tag" style="cursor: default;">关注我们</a>
                <a href="http://weibo.com/yiqizuoye" target="_blank"><i class="zy-icon icon-xlwb"></i><span>新浪微博</span></a>
                <a href="javascript:;">
                    <i class="zy-icon icon-gfwx"></i><span>官方微信</span>
                    <div class="codeImg"></div>
                </a>
            </div>
        </div>
    </div>
</div>

    <@sugar.capsule js=['cjlschool'] />
</body>
</html>
</#macro>