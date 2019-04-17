<script id="t:换课本" type="text/html">
    <div id="bookListV5" class="h-homework-dialog04 h-homework-dialog">
        <div class="inner">
            <p><span class="iname">册别：</span>
                <label style="cursor: pointer;" data-bind="css:{'w-radio-current' : term() == 1},click:changeBookTermClick.bind($data,1)"><span class="w-radio"></span> <span class="w-icon-md">上册&nbsp;&nbsp;</span></label>
                <label style="cursor: pointer;" data-bind="css:{'w-radio-current' : term() == 2},click:changeBookTermClick.bind($data,2)"><span class="w-radio"></span> <span class="w-icon-md">下册</span></label>
            </p>
            <p><span class="iname">年级：</span>
                <label style="cursor: pointer;" data-bind="css:{'w-radio-current' : level() == 1},click:changeBookLevelClick.bind($data,1)"><span class="w-radio"></span> <span class="w-icon-md">一年级</span></label>
                <label style="cursor: pointer;" data-bind="css:{'w-radio-current' : level() == 2},click:changeBookLevelClick.bind($data,2)"><span class="w-radio"></span> <span class="w-icon-md">二年级</span></label>
                <label style="cursor: pointer;" data-bind="css:{'w-radio-current' : level() == 3},click:changeBookLevelClick.bind($data,3)"><span class="w-radio"></span> <span class="w-icon-md">三年级</span></label>
                <label style="cursor: pointer;" data-bind="css:{'w-radio-current' : level() == 4},click:changeBookLevelClick.bind($data,4)"><span class="w-radio"></span> <span class="w-icon-md">四年级</span></label>
                <label style="cursor: pointer;" data-bind="css:{'w-radio-current' : level() == 5},click:changeBookLevelClick.bind($data,5)"><span class="w-radio"></span> <span class="w-icon-md">五年级</span></label>
                <label style="cursor: pointer;" data-bind="css:{'w-radio-current' : level() == 6},click:changeBookLevelClick.bind($data,6)"><span class="w-radio"></span> <span class="w-icon-md">六年级</span></label>
            </p>
            <div class="list-box">
                <div class="list-hd">
                    <p class="hdl">教材列表</p>
                    <p class="hdr"><input id="searchText" type="text" placeholder="输入关键字搜索，如人教、苏教" data-bind="event:{keyup:searchBook.bind($data,$element)}"></p>
                </div>
                <!--ko if: !noFilterRes() -->
                <div class="list-mn">
                    <!--ko ifnot:bookList().length > 0-->
                    <p class="tips-grey" style="padding-left:0;">未找到相关教材</p>
                    <!--/ko-->
                    <!--ko if:bookList().length > 0-->
                    <!--ko foreach:{data:bookList,as:'book'}-->
                    <a href="javascript:void(0)" data-bind="style:{display:book.isShow()?'':'none'},css:{'active' : book.id() == $root.selectBookId()},attr:{title:book.name },text:book.name,click:$root.bookClick.bind($data,$root)"></a>
                    <!--/ko-->
                    <!--/ko-->
                </div>
                <!--/ko-->
                <!--ko if: noFilterRes() -->
                <div style="padding:86px 0;">
                <p class="tips-grey" style="padding-left:0;">对不起，没有找到“<span data-bind="text:searchText"></span>”相关教材，请更换关键词重新查询或点击下方的操作将您想要查找的教材告诉我们，我们会尽快提供相应教材。</p>
                <p>没有教材？<a data-bind="click:noBookFeedBack" href="javascript:void(0)" style="color: #189cfb">点击这里</a></p>
                </div>
                <!--/ko-->
            </div>
        </div>
        <!--ko if: !noFilterRes() -->
        <div class="bottom">
            <div class="bot-left">
                <p><span data-bind="if:selectBookId() != null,visible:selectBookId() != null">确认要将</span>现在使用的“<span data-bind="text:bookName"></span>”</p>
                <p data-bind="if:selectBookId() != null,visible:selectBookId() != null">改为“<span data-bind="text:selectBookName"></span>”？</p>
            </div>
            <a href="javascript:void(0)" class="w-btn btn" data-bind="click:saveChangeBook.bind($data,$element)">确定</a>
        </div>
        <!--/ko-->
    </div>
</script>

<script type="text/html" id="t:缺失教材反馈">
    <style>
        .t-feedback-box {width: 400px;margin: -20px 0 -20px 75px;}
        .t-feedback-box p {text-align: left;color:#979e9e;padding-bottom: 10px;}
        .t-feedback-box ul li {padding:10px 0;}
    </style>
    <div class="t-feedback-box">
        <p>没有我的教材，提交准确信息，帮助更多老师使用此教材</p>
        <ul>
            <li>
                教材名称：<input id="t-fbBookInput" style="width: 220px;" type="text" class="w-int">
            </li>
            <li>
                地区通用：
                <span class="clazz t-fbRegionUniversal" style="margin-right: 30px;"><i class="radios radios_active"></i>是</span>
                <span class="clazz t-fbRegionUniversal"><i class="radios"></i>否</span>
            </li>
            <li>
            <span id="currentTeacherDistrict" style="display:none;">${currentTeacherDetail.rootRegionName!}省${currentTeacherDetail.cityName!}${currentTeacherDetail.countyName!}</span>
            <#if currentUser.profile.sensitiveMobile?has_content>
                联系方式：<span id="currentUserProfileMobile"> ${currentUserProfileMobile!}</span> ${(currentTeacherDetail.fetchRealname())!}
            <#else>
                联系方式：无手机号。<a class="w-blue" href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myprofile.vpage">完善资料</a>
            </#if>
            </li>
        </ul>
    </div>
</script>

<script type="text/html" id="T:自动更新新学期教材">
    <div class='w-ag-center' style='font-size: 16px; line-height: 32px;'>
        亲爱的老师，新学期已至，您正在使用的教材为<br/>
        <strong style='color: #f00;'><%=bookName%></strong>，是否要将其更换为新学期教材？
        <div style="padding: 10px 0 0;">
            <span class="v-change-book w-build-image w-build-image-<%=color%>" style="cursor: pointer;">
                <strong class="wb-title"><%=remindBookPress%></strong>
                <span class="wb-new"></span>
            </span>
            <p><%=remindBookName%></p>
        </div>
    </div>
</script>