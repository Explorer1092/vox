<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page pageJs=["teachersDay2016"] pageJsFile={"teachersDay2016" : "public/script/teacherv3/activity/teachersDay2016"} pageCssFile={"teachersDay" : ["public/skin/project/teachersday2016/skin"]}>
    <#if !(hasBless!false)>
        <div class="teaDay-bg">
            <div class="bg bg01">
                <div class="info">
                    <div class="t-avatar"><img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>"><p class="name">${(currentUser.profile.realname)!}</p></div>
                    <div class="text">
                        <p>亲爱的<span class="yellow">${(currentUser.profile.realname)!}</span>老师:</p>
                        <p>您已经使用一起作业<span class="yellow">${dayCount!0}</span>天</p>
                        <p>感谢您和我们一起成长。</p>
                    </div>
                </div>
            </div>
            <div class="bg bg02"></div>
            <div class="bg bg03"></div>
        </div>
    <#else>
        <div class="teaDay-bg2">
            <div class="bg bg01"></div>
            <div class="bg bg02"></div>
            <div class="bg bg03"></div>
        </div>
        <div class="teaDay-main">
            <div class="t-avatar"><img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>"><p class="name">${(currentUser.profile.realname)!}</p></div>
            <div class="t-btnBox">
                <a href="javascript:void(0)" class="btn">共收到
                    <!-- ko foreach : {data : clazzDetail, as : '_clazz'} -->
                    <!--ko if: _clazz.checked()-->
                    <span data-bind="text: _clazz.blessCount()">0</span>
                    <!--/ko-->
                    <!--/ko-->
                    个祝福
                </a>
                <a href="javascript:void(0)" class="btn">共收到
                    <!-- ko foreach : {data : clazzDetail, as : '_clazz'} -->
                    <!--ko if: _clazz.checked()-->
                    <span data-bind="text: _clazz.flowerCount()">0</span>
                    <!--/ko-->
                    <!--/ko-->
                    朵鲜花
                </a>
            </div>
            <div class="t-stuList">
                <div class="classList">
                    <!--ko if: clazzDetail().length > 4-->
                    <div class="arrow arrow-l" data-bind="click: scrollLeftBtn"></div>
                    <div class="arrow arrow-r" data-bind="click: scrollRightBtn"></div>
                    <!--/ko-->

                    <div class="inner">
                        <ul data-bind="style: {'width': clazzDetail().length * 206 +'px'}"><!--ul的宽度为li的个数*206px-->
                            <!-- ko foreach : {data : clazzDetail, as : '_clazz'} -->
                            <li data-bind="text: _clazz.clazzName(), css: {'active': _clazz.checked()}, click: $root.selectClazzBtn, visible: _clazz.pageNum() == $root.currentPage()"></li>
                            <!--/ko-->
                        </ul>
                    </div>
                </div>
                <div class="classMain" data-bind="visible: clazzDetail" style="display: none;">
                    <ul>
                        <!-- ko foreach : {data : clazzDetail, as : '_clazz'} -->
                        <!--ko if: _clazz.checked()-->
                        <!-- ko foreach : {data : _clazz.blesses(), as : '_blesses'} -->
                        <li>
                            <a href="javascript:void(0);" data-bind="attr: {href: _blesses.imgUrl()}" target="_blank">
                                <img src="" data-bind="attr: {src: _blesses.imgUrl()}" alt="photo" class="pic">
                                <div class="footer">
                                    <img src="" data-bind="attr: {src: _blesses.avatar()}" class="avatar">
                                    <span data-bind="text: _blesses.studentName()"></span>
                                </div>
                            </a>
                        </li>
                        <!--/ko-->
                        <!--/ko-->
                        <!--/ko-->
                    </ul>
                </div>
            </div>
            <div class="t-footer">
                <a href="javascript:void(0)" data-bind="click: gotoIntegralBtn" class="btn">鲜花兑换学豆</a>
                <a href="javascript:void(0)" data-bind="click: shareBtn" class="btn">分享教师节祝福</a>
            </div>
        </div>
    </#if>
    <script>
        var teachersDayMap = {
            blessList : ${json_encode(blessList)![]},
            hasBless: ${(hasBless!false)?string},
            imgDomain: '<@app.avatar href='' />',
            teacherId :${(currentUser.id)!},
            toWechatJava: <@toWechatJava/>
        };
    </script>

    <#--跳转到微信处理-->
    <#macro toWechatJava>
        <#compress>
            <#if ProductDevelopment.isDevEnv()>
            '${requestContext.webAppBaseUrl?replace(8081,8180)}'
            <#elseif  ProductDevelopment.isTestEnv()>
            '//wechat.test.17zuoye.net/'
            <#elseif ProductDevelopment.isStagingEnv()>
            '//wechat.staging.17zuoye.net/'
            <#elseif ProductDevelopment.isProductionEnv()>
            '//wechat.17zuoye.com/'
            </#if>
        </#compress>
    </#macro>

</@layout.page>
