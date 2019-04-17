<#import "../layout_view.ftl" as activityMain>
<@activityMain.page title="教师节" pageJs="teachersDay2016">
    <@sugar.capsule css=["teachersDay2016"] />
    <#if !(hasBless!false)>
    <div class="my-wrapper">
        <p class="my-data">
            亲爱的<span class="yellow-color">${teacherName!}</span>老师:<br />
            您已经使用一起作业<span class="yellow-color">${dayCount!0}</span>天<br />
            感谢您和我们一起成长。
        </p>
    </div>
    <#else>
    <div class="wishes-wrapper">
        <div class="wishes-box">
            <div class="wishes">
                <div class="classes-wrapper">

                    <div class="classes" data-bind="style: {'width': clazzDetail().length * 4.05 +'rem'}"> <!--4.05rem*li的个数-->
                        <!-- ko foreach : {data : clazzDetail, as : '_clazz'} -->
                        <div class="item" data-bind="css: {'the': _clazz.checked()}, click: $root.selectClazzBtn">
                            <a href="javascript:void(0);">
                                <span data-bind="text: _clazz.clazzName()"></span>
                            </a>
                        </div>
                        <!--/ko-->
                    </div>
                </div>
                <div class="gifts-info">收到
                    <!-- ko foreach : {data : clazzDetail, as : '_clazz'} -->
                    <!--ko if: _clazz.checked()-->
                    <span data-bind="text: _clazz.blessCount()">0</span>
                    <!--/ko-->
                    <!--/ko-->
                    个教师节祝福
                    <!-- ko foreach : {data : clazzDetail, as : '_clazz'} -->
                    <!--ko if: _clazz.checked()-->
                    <span data-bind="text: _clazz.flowerCount()">0</span>
                    <!--/ko-->
                    <!--/ko-->
                    朵鲜花
                </div>
            </div>
        </div>
        <div class="students clearfix" data-bind="visible: clazzDetail" style="display: none;">
            <!-- ko foreach : {data : clazzDetail, as : '_clazz'} -->
            <!--ko if: _clazz.checked()-->
            <!-- ko foreach : {data : _clazz.blesses(), as : '_blesses'} -->
            <div class="item">
                <div class="clearfix">
                    <img src="" data-bind="attr: {src: _blesses.imgUrl()+'@200w_1o'}" width="100%" />
                    <div class="s-name" data-bind="text: _blesses.studentName()"></div>
                </div>
            </div>
            <!--/ko-->
            <!--/ko-->
            <!--/ko-->
        </div>
        <div class="students clearfix" data-bind="visible: !clazzDetail" style="text-align: center; color: #8D742F;font-size: 0.6rem;">
            图片加载中...
        </div>
    </div>
    <div class="btn-box">
        <div>
            <a class="btn change" href="javascript:void(0);" data-bind="click: gotoIntegralBtn">鲜花兑换学豆</a>
        </div>
        <div>
            <a class="btn share" href="javascript:void(0);" data-bind="click: gotoPhotosWallBtn">生成照片墙</a>
        </div>
    </div>
    </#if>
<script>
    var teachersDayMap = {
        blessList : ${json_encode(blessList)![]},
        hasBless: ${(hasBless!false)?string},
        imgDomain: '<@app.avatar href='/' />',
        teacherId :'${currentUserId!0}'
    };
</script>
</@activityMain.page>