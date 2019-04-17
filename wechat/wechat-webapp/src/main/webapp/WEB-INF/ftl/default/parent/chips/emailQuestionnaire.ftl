<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语教材邮寄地址登记表" pageJs="emailQuestionnaire">
    <@sugar.capsule css=["emailQuestionnaire"] />

<style>
    [v-cloak] { display: none }
    #chips_survey{
        height:100%;
        width:100%;
    }
    .site{
        width: 7.5rem;
        height: 2rem;
        position: relative;

    }
    .site .cityTitle{
        width:100%;
        height: 1rem;
        line-height: 1rem;
        color: #757575;
        font-weight: 400;
        font-size: 0.7rem;
        text-align: left;
        border-width: 2px;
        border-style: inset;
        border-color: initial;
    }
    .site .cityBox{
        position: absolute;
        width: 100%;
        min-height: 0;
        max-height: 8rem;
        overflow-y: scroll;
        background: #fff;
        z-index: 5;
        padding: 0.2rem 0;
    }
    .site .cityBox li{
        width: 100%;
        height: 1rem;
        line-height: 1rem;
        color: #757575;
        font-size: 0.7rem;
        text-align: center;
        border-bottom:1px solid #a0a0a0;
        margin: 0;
        padding: 0.2rem 0;
    }
    .site .cityBox li:hover{
        background: #a0a0a0;
    }
    .site .detailSite{
        margin-top: 0.2rem;
        width: 7.7rem;
    }
    .site .coureseBox{
        width: 7.7rem;
        background: #fff;
        position: absolute;
        top:-7.5rem;
    }
    .site .coureseBox li{
        width: 80%;
        height: 1rem;
        line-height: 1rem;
        color: #757575;
        font-size: 0.7rem;
        border-top:1px solid #a0a0a0;
        margin: 0;
        padding: 0.2rem 0;
        text-align: center;
    }
    .site .coureseBox li:hover{
        background: #a0a0a0;
    }
</style>

<div id="chips_survey" v-cloak>
    <div class="questionWrap">
        <div class="questionHead">
            <div class="title">薯条英语系统课程教材</div>
            <div class="title">邮寄地址登记表</div>
            <div class="intro">请您登记下您的地址信息，准备收获属于孩子的薯条英语纸质教材吧~</div>
        </div>
        <div class="questionMain">
            <!-- 1 unselected -->
            <div class="questionList" v-for="(item,index) in list" v-bind:class="{unselected:item.red_sign == 'red'}" style="margin-bottom: 1rem">
                <div class="listTitle"><i>*</i>{{ item.index + 1 }}.{{ item.title }}</div>
                <ul class="answerBox">
                    <li v-for="(sub_item,sub_index) in item.options" style="display: flex">
                        <input v-show="index!=3 && index!=1" :id="'text'+item.index+sub_index" type="text"  :placeholder="sub_item.sTitle">
                        <div class="site courseGrade"  v-show="index==3">
                            <div v-if="index==3" class="cityTitle courseTitle" @click="clickCourse">{{courseList[0]}}</div>
                            <ul class="coureseBox" v-show="courseShow">
                                <li class="courseName" v-for="(cItem,cIndex) in courseList" @click="choiceCourse(cIndex)" :key="cIndex">{{cItem}}</li>
                            </ul>
                        </div>
                        <div v-if="index==1" class="site">
                            <div class="cityTitle proveTitle " @click="clickProv">所有省份</div>
                            <ul class="cityBox" v-show="proveShow && regionList!=0" v-show="regionList.length==0">
                                <li class="cityBar" v-show="regionList!=0"  v-for="(pItem,pIndex) in regionList" @click="choiceProv(pIndex)">{{pItem.name}}</li>
                            </ul>
                            <input type="text" class="detailSite" placeholder="街道地址">
                        </div>
                    </li>
                </ul>
            </div>
        </div>
        <div class="questionSubmit" @click="submit">提交</div>
    </div>
    <!-- 提示弹窗 -->
    <div class="questionPopup" v-if="toast_status">
        <div class="tipsBox">请完成作答哦～</div>
    </div>
</div>
<script src="https://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<script type="text/javascript">
    /*
     * 注意：
     * 1. 所有的JS接口只能在公众号绑定的域名下调用，公众号开发者需要先登录微信公众平台进入“公众号设置”的“功能设置”里填写“JS接口安全域名”。
     * 2. 如果发现在 Android 不能分享自定义内容，请到官网下载最新的包覆盖安装，Android 自定义分享接口需升级至 6.0.2.58 版本及以上。
     * 3. 常见问题及完整 JS-SDK 文档地址：http://mp.weixin.qq.com/wiki/7/aaa137b55fb2e0456bf8dd9148dd613f.html
     *
     * 开发中遇到问题详见文档“附录5-常见错误及解决办法”解决，如仍未能解决可通过以下渠道反馈：
     * 邮箱地址：weixin-open@qq.com
     * 邮件主题：【微信JS-SDK反馈】具体问题
     * 邮件内容说明：用简明的语言描述问题所在，并交代清楚遇到该问题的场景，可附上截屏图片，微信团队会尽快处理你的反馈。
     */
    wx.config({
        debug: false,
        appId: '${appid!""}',
        timestamp:'${timestamp!""}',
        nonceStr: '${nonceStr!""}',
        signature: '${signature!""}',
        jsApiList: [
            'checkJsApi',
            'onMenuShareTimeline',
            'onMenuShareAppMessage',
            'onMenuShareQQ',
            'onMenuShareWeibo',
            'onMenuShareQZone',
            'hideMenuItems',
            'showMenuItems',
            'hideAllNonBaseMenuItem',
            'showAllNonBaseMenuItem',
            'translateVoice',
            'startRecord',
            'stopRecord',
            'onVoiceRecordEnd',
            'playVoice',
            'onVoicePlayEnd',
            'pauseVoice',
            'stopVoice',
            'uploadVoice',
            'downloadVoice',
            'chooseImage',
            'previewImage',
            'uploadImage',
            'downloadImage',
            'getNetworkType',
            'openLocation',
            'getLocation',
            'hideOptionMenu',
            'showOptionMenu',
            'closeWindow',
            'scanQRCode',
            'chooseWXPay',
            'openProductSpecificView',
            'addCard',
            'chooseCard',
            'openCard',
            'hideAllNonBaseMenuItem'
        ]
    });
    //    wx.ready(function(){
    //        wx.hideAllNonBaseMenuItem();
    //        wx.hideOptionMenu();
    //    })

    wx.ready(function () {
        // 微信朋友圈分享
        wx.onMenuShareTimeline({
            title: "薯条英语教材邮寄地址登记表", // 分享标题
            link: "", // 分享链接，该链接域名或路径必须与当前页面对应的公众号JS安全域名一致
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {
                // 用户点击了分享后执行的回调函数
            }
        });

        //分享给好友
        wx.onMenuShareAppMessage({
            title: "薯条英语教材邮寄地址登记表", // 分享标题
            desc: "请点击填写您的邮寄地址信息", // 分享描述
            link: '', // 分享链接
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            type: '', // 分享类型,music、video或link，不填默认为link
            dataUrl: '', // 如果type是music或video，则要提供数据链接，默认为空
            success: function () {
                // 用户确认分享后执行的回调函数
            },
            cancel: function () {
                // 用户取消分享后执行的回调函数
            }
        });

        wx.onMenuShareQQ({
            title: "薯条英语教材邮寄地址登记表", // 分享标题
            desc: "请点击填写您的邮寄地址信息", // 分享描述
            link: '', // 分享链接
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {
                // 用户确认分享后执行的回调函数
            },
            cancel: function () {
                // 用户取消分享后执行的回调函数
            }
        });

        // 分享到QQ空间
        wx.onMenuShareQZone({
            title: "薯条英语教材邮寄地址登记表", // 分享标题
            desc: "请点击填写您的邮寄地址信息", // 分享描述
            link: '', // 分享链接
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {
                // 用户确认分享后执行的回调函数
            },
            cancel: function () {
                // 用户取消分享后执行的回调函数
            }
        });
    });

</script>

</@layout.page>

<#--</@chipsIndex.page>-->
