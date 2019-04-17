<#import "../layout.ftl" as layout>
<@layout.page title="APP下载操作指南">
    <@sugar.capsule css=['chipsAll'] />

<div class="unloadIntroWrap">
    <div class="unloadIntro-banner">
        <span></span>
    </div>
    <div class="unloadIntroMain">
        <div class="introList">
            <div class="listTitle">
                <i class="num">1</i>
                <span class="introText">下载一起学App<br>（点击右上角在Safari打开）</span>
            </div>
            <div class="listPic listPic-differ">
                <div class="pic pic01"></div>
            </div>
        </div>
        <div class="introList">
            <div class="listTitle">
                <i class="num">2</i>
                <span class="introText">使用购买账号登录</span>
            </div>
            <div class="listPic">
                <div class="pic pic02"></div>
            </div>
        </div>
        <div class="introList">
            <div class="listTitle">
                <i class="num">3</i>
                <span class="introText">点击薯条英语开始学习</span>
            </div>
            <div class="listPic">
                <div class="pic pic03"></div>
            </div>
        </div>
    </div>
</div>


<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            // app下载页介绍 _被加载
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'downloadapp_intro_load'
            })
        })
    }
</script>
</@layout.page>

<#--</@chipsIndex.page>-->
