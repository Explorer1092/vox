<#import "../layout_view.ftl" as activityMain>
<@activityMain.page title="教师节" pageJs="teachersDayShare">
    <@sugar.capsule css=["teachersDay2016"] />
    <div class="teaDay-share">
        <div class="shareMain">
            <div class="shareTitle"><img src="/public/images/teacher/activity/teachersDay2016/title.png"></div>
            <div class="shareVideo" data-bind="click: playVideoBtn">
                <video class="video" id="videoBox" poster="http://cdn.17zuoye.com/static/project/teacherday/teacherday-video-img1.jpg" width="100%" height="100%" controls preload>
                    <source src="http://v.17zuoye.cn/corp/teachersDay_720.mp4" type="video/mp4">
                </video>
            </div>
            <div class="tips">温馨提示：建议在WiFi环境下播放</div>
            <div class="pictureBox pictureDif" data-bind="visible: shareListDetail" style="display: none;"> <#--不带视频 pictureDif -->
                <div class="students clearfix">
                    <table cellpadding="0" cellspacing="0">
                        <tr><td></td></tr>
                        <!--ko foreach:ko.utils.range(1,(shareListDetail().length/2 + 1))-->
                        <tr>
                            <!--ko foreach:{data:$root.shareListDetail.slice($index() * 2,($index() * 2 + 2)),as:'_detail'}-->
                            <td data-bind="attr: {id : 'transformImg'+$index()}, click: $root.viewBigImg">
                                <div class="picItem">
                                    <div class="image">
                                        <img src="/public/images/teacher/activity/teachersDay2016/student.jpg" data-bind="img: { src: _detail.imgUrl()+'@200w_1o', fallback: $root.defaultImg }" width="100%" />
                                    </div>
                                    <div class="s-name" data-bind="text: _detail.studentName()"></div>
                                </div>
                            </td>
                            <td data-bind="text: $root.transformImg($index())"></td>
                            <!--/ko-->
                        </tr>
                        <!--/ko-->
                    </table>
                </div>
            </div>
            <div data-bind="visible: !shareListDetail" style="text-align: center; color: #8D742F">图片墙生成中...</div>
        </div>
        <div class="shareFooter" data-bind="visible: showShareBtn" style="display: none">
            <div class="footerInner">
                <a data-bind="click: shareBtn" href="javascript:void(0)" class="btn">分享</a>
            </div>
        </div>
        <div class="shareFooter" data-bind="visible: !showShareBtn()" style="display: none">
            <div class="footerInner">
                <a href="javascript:void(0)" data-bind="click: goto17Btn" class="website-btn">进入一起作业官网 ></a>
            </div>
        </div>
    </div>

    <div class="wechat-shareBg" data-bind="visible: showShareTipBox" style="display: none; z-index: 1">
        <div class="bg"></div>
    </div>

    <script type="text/javascript">
        var teachersDayShareMap = {
            shareList: ${json_encode(shareList)![]},
            wxJsApiMap :${json_encode(ret)!},
            domain : '${requestContext.webAppBaseUrl}'
        };
    </script>
</@activityMain.page>