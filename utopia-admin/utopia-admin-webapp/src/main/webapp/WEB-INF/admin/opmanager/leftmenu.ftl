<div class="span2">
    <div class="well sidebar-nav" style="background-color: #fff;">
        <ul class="nav nav-list">

            <li class="nav-header"><a href="#manager_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>运营管理<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="manager_tag" class="nav nav-list collapse secondmenu" style="height: 0px;">
                <li><a href="${requestContext.webAppContextPath}/opmanager/smstask/index.vpage">短信管理平台</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/survey/surveyinfo.vpage">问卷调查查询</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/advertisement/adindex.vpage">广告管理平台</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/fairylandProduct/index.vpage">产品信息管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/officialaccounts/index.vpage">公众号管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/blacklist/index.vpage">用户黑白名单</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/coupon/index.vpage">优惠券管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/lordaeron/list.vpage">产品详情页配置（洛丹伦）</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/pictureBook/recommend/list.vpage">绘本合集管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/pictureBook/card/list.vpage">绘本馆常规卡管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/pictureBook/card/color/list.vpage">绘本馆彩蛋卡管理</a></li>
            </ul>

            <li class="nav-header"><a href="#activity_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>活动相关<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="activity_tag" class="nav nav-list collapse secondmenu" style="height: 0px;">
                <li><a href="${requestContext.webAppContextPath}/opmanager/integralactivity/activitypage.vpage">积分活动管理</a></li>
            <li><a href="${requestContext.webAppContextPath}/opmanager/ugc/index.vpage">UGC活动管理</a></li>
            <li><a href="${requestContext.webAppContextPath}/opmanager/groupon/index.vpage">团购活动管理</a></li>
            <li><a href="${requestContext.webAppContextPath}/opmanager/lottery/compaign/list.vpage">抽奖活动管理</a></li>
            <#--<li><a href="${requestContext.webAppContextPath}/opmanager/businessactivity/index.vpage">导流活动管理</a></li>-->
            <#--li><a href="${requestContext.webAppContextPath}/opmanager/poetry/import.vpage">中秋活动-诗词导入</a></li--></ul>

            <li class="nav-header"><a href="#message_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>消息推送<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="message_tag" class="nav nav-list collapse secondmenu" style="height: 0px;">
                <li><a href="${requestContext.webAppContextPath}/opmanager/pushmessage/fairyland.vpage">学生课外乐园消息</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/pushmessage/wechatbatchindex.vpage">微信消息发送和审核</a></li>
                <#--<li class="divider"></li>-->
                <li><a href="${requestContext.webAppContextPath}/opmanager/customerservice/index.vpage">每日要闻</a></li>
            </ul>

            <li class="nav-header"><a href="#student_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>学生APP<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="student_tag" class="nav nav-list collapse secondmenu" style="height: 0px;">
                <li><a href="${requestContext.webAppContextPath}/opmanager/gossip/index.vpage">班级大爆料</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/clazzrecord/index.vpage">班级空间</a></li>
            </ul>


            <li class="nav-header"><a href="#parent_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>家长奖励<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="parent_tag" class="nav nav-list collapse secondmenu" style="height: 0px;">
                <li><a href="${requestContext.webAppContextPath}/opmanager/parentreward/itemlist.vpage">家长奖励项管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/parentreward/categorylist.vpage">家长奖励类型管理</a></li>
            </ul>
            <li class="nav-header"><a href="#parentmission_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>家长任务<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="parentmission_tag" class="nav nav-list collapse secondmenu" style="height: 0px;">
                <li><a href="${requestContext.webAppContextPath}/opmanager/parentmission/categories.vpage">分类管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/parentmission/missions.vpage">任务管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/parentmission/missions/invite.vpage">邀请数据查询</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/parentmission/missions/reward.vpage">奖励数据查询</a></li>
            </ul>

            <li class="nav-header"><a href="#teacher_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>老师APP<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="teacher_tag" class="nav nav-list collapse secondmenu" style="height: 0px;">
                <li><a href="${requestContext.webAppContextPath}/opmanager/teachingresource/index.vpage">教学资源</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/17JTConf/index.vpage">一起新讲堂</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/teacherinvitation/index.vpage">教师邀请奖励配置</a></li>
                <#--<li><a href="${requestContext.webAppContextPath}/opmanager/teacher_resource/kit/index.vpage">教学锦囊</a></li>-->
                <li><a href="${requestContext.webAppContextPath}/opmanager/teacher_resource/course/index.vpage">同步课件</a></li>
                <#--<li><a href="${requestContext.webAppContextPath}/opmanager/teacher_resource/project/index.vpage">教学专题</a></li>-->
                <#--<li><a href="${requestContext.webAppContextPath}/opmanager/teacher_resource/newforum/index.vpage">一起新讲堂</a></li>-->
            </ul>

            <li class="nav-header"><a href="#together_study_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>一起学<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="together_study_tag" class="nav nav-list collapse secondmenu" style="height: 0px;">
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/opWechatEditPage.vpage">个人微信号管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/studyGroupList.vpage">课程班级管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/studentLessonRefPage.vpage">用户管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/getStudyData.vpage">数据管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/findJoinInfo.vpage">家长报名查询</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/repairJoin.vpage">家长报名修复</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/getPushText.vpage">push文案编辑</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/videodaylist.vpage">日榜视频审核</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/videototallist.vpage">总榜视频审核</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/recruit_list.vpage">KOL班长招募管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/pass_monitor_list.vpage">KOL班长管理后台</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/joinUpPageList.vpage">课程介绍页编辑列表</a></li>
                <#--<li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/ebook/pageDetail.vpage">电子书详情编辑测试</a></li>-->
                <#--<li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/ebook/bookDetail.vpage">电子书详情编辑测试</a></li>-->
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/ebook/pageList.vpage">电子书-书页列表</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/ebook/bookList.vpage">电子书-书籍列表</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/workcomment/commentcourse.vpage">作业点评-课程列表</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/workcomment/workervaluate.vpage">作业点评-录音列表</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/workcomment/feedback.vpage">作业点评-反馈列表</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/getGroupAreaInfos.vpage">班级区管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/monitor_recruit_list.vpage">班长招募开关控制</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/through_train/config.vpage">直通车配置管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studytogether/recommend/index.vpage">推荐位配置管理</a></li>
            </ul>

            <li class="nav-header"><a href="#university_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>家长大学<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="university_tag" class="nav nav-list collapse secondmenu" style="height: 0px;">
                <li class="nav-header"><a href="#talk_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>一起说<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
                <ul id="talk_tag" class="nav nav-list collapse secondmenu" style="height: 0px;">
                    <li><a href="${requestContext.webAppContextPath}/opmanager/talk/topicpublish.vpage">发布话题</a></li>
                    <li><a href="${requestContext.webAppContextPath}/opmanager/talk/topiclist.vpage">话题列表</a></li>
                    <li><a href="${requestContext.webAppContextPath}/opmanager/talk/topictotal.vpage">话题数据统计</a></li>
                    <li><a href="${requestContext.webAppContextPath}/opmanager/talk/audiopublish.vpage">发布音频文稿</a></li>
                    <li><a href="${requestContext.webAppContextPath}/opmanager/talk/audiolist.vpage">音频文稿列表</a></li>
                    <li><a href="${requestContext.webAppContextPath}/opmanager/talk/replypublish.vpage">发布观点</a></li>
                    <li><a href="${requestContext.webAppContextPath}/opmanager/talk/replylist.vpage">观点列表</a></li>
                    <li><a href="${requestContext.webAppContextPath}/opmanager/talk/rankList.vpage">排行榜表</a></li>
                    <li><a href="${requestContext.webAppContextPath}/opmanager/talk/sendAward.vpage">发送通知</a></li>
                </ul>
                <li class="nav-header"><a href="#bigshot_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>大咖讲座<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
                <ul id="bigshot_tag" class="nav nav-list collapse secondmenu" style="height: 0px">
                    <li><a href="${requestContext.webAppContextPath}/opmanager/bigshot/list.vpage">讲座列表</a></li>
                </ul>
            </ul>

            <li class="nav-header"><a href="#coin_type_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>17学-学习币类型<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="coin_type_tag" class="nav nav-list collapse secondmenu" style="height: 0px;">
                <li><a href="${requestContext.webAppContextPath}/opmanager/cointype/list.vpage">学习币类型管理</a></li>
            </ul>

            <li class="nav-header"><a href="#coin_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>学习币商城<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="coin_tag" class="nav nav-list collapse secondmenu" style="height: 0px;">
                <li><a href="${requestContext.webAppContextPath}/opmanager/commodity/list.vpage">商品管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/commodity/order/list.vpage">订单管理</a></li>
            </ul>

            <li class="nav-header"><a href="#course_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>17学-训练营课程管理<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="course_tag" class="nav nav-list collapse secondmenu" style="height: 0px;">
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/template/classical_chinese_list.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;课程内容模板管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/lessonCourseRef/get_lesson_list.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;课节与模版的关联管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/newTemplate/newTemplate_list_page.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;组件化课程模板管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/link/index.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;组件化课程环节管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studytogether/series/index.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;系列管理</a></li>
                <#--<li><a href="${requestContext.webAppContextPath}/opmanager/studytogether/subject/index.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;主题管理</a></li>-->
                <li><a href="${requestContext.webAppContextPath}/opmanager/studytogether/spu/index.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;SPU管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studytogether/coinreward/index.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;学习奖励管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studytogether/sku/index.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;SKU管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studytogether/share/index.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;分享管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studytogether/skip/index.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;跳转管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studytogether/notice/index.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;通知管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studytogether/clazzfestival/index.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;课节管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studytogether/chapter/chindex.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;章节管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studytogether/weeklyreward/wrindex.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;周奖励管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/studyTogether/address/index.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;订单地址查询</a></li>
            </ul>
            <li class="nav-header"><a href="#poetry_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>小学业务--活动管理<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="poetry_tag" class="nav nav-list collapse secondmenu" style="height: 0px;">
                <li><a href="${requestContext.webAppContextPath}/opmanager/poetry/activitymanager.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;活动管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/opmanager/poetry/poetrymanager.vpage"><i class="glyphicon glyphicon-user"></i>&nbsp;古诗管理</a></li>
            </ul>
            <li class="nav-header"><a href="#tobbit_course" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>托比同步课堂<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="tobbit_course" class="nav nav-list collapse secondmenu" style="height: 0px;">
                <li><a href="${requestContext.webAppContextPath}/opmanager/tobbit/courses.vpage">托比同步课堂</a></li>
            </ul>
        </ul>
    </div>
</div>
