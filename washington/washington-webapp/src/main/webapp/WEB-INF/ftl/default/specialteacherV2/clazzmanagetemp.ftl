<#--侧边菜单栏模板-->
<script id="gradeMenuTemp" type="text/html">
    <div class="class-sidebar columnLeft">
        <div class="nav-mode claManagement-nav">
            <!--ko if:hasData -->
            <!--ko foreach:menu -->
            <a href="javascript:;" class="link js-classMenuItem" data-bind="click:$root.toggleGradeMenu.bind($data,$index)"><span data-bind="text:$data.gradeName"></span></a>
        <#--此处注释掉 ko if 判断，因为如果不存在dom结构会影响js中的判断-->
        <#--<!-- ko if:clazzs.length != 0 &ndash;&gt;-->
            <div class="claManagement-nav-2 hidden js-subMenuItem" data-bind="attr:{gradeId: gradeId}">
                <!--ko foreach:clazzs -->
                <a href="javascript:;" class="js-classMenuItem" data-bind="click:$root.toggleClassMenu.bind($data)"><span data-bind="text:clazzName"></span></a>
                <!-- /ko -->
            </div>
        <#--<!-- /ko &ndash;&gt;-->
            <!-- /ko -->
            <a href="javascript:;" class="adjust" data-bind="click:$root.adjustClassMenu.bind($data)"><span>调整班级</span></a>
            <!-- /ko -->
            <!-- ko if:!hasData -->
            <a href="javascript:;" class="link"><span>暂无</span></a>
            <!-- /ko -->
        </div>
    </div>
</script>

<#--年级卡片模板-->
<script id="gradeCardListTemp" type="text/html">
    <#--<!-- ko if:($root.gradeMenu().hasData && administrativeClass.length == 0 && teachingClass.length == 0) &ndash;&gt;
    <div style="padding:300px 0 0 0;text-align: center;font-size:18px;color:#4d4d4d">班级内暂无老师</div>
    <!-- /ko &ndash;&gt;-->

    <div class="claManagement-list">
        <div class="title">行政班：</div>
        <!-- ko if: $root.isHasSameClassMultiGroup() -->
        <p class="tips"><i></i>出现一个班内多个班群，请先确认老师和学生信息是否正确，如果正确可进行合并处理，也可删除错误班群。</p>
        <!-- /ko -->
        <ul class="clazzDetails">
            <!--ko if:administrativeClass.length != 0 -->
            <!--ko foreach:administrativeClass -->
            <li class="JS-mergeOperateBox" data-bind="click: $root.selectMergeGroup.bind($data)">
                <div class="box">
                    <div class="content">
                        <img class="modify-icon" src="<@app.link href='public/skin/specialteacherV2/images/dean/dean-btn02.png'/>" data-bind="click:$root.editCardClazzName.bind($data,$parent.gradeId)">
                        <div class="num fr" data-bind="text:studentNum+'人', visible: studentNum > -1"></div>
                        <div class="name" data-bind="text:groupName, attr: {title: groupName}, visible: groupName"></div>
                    </div>

                <#--高中才展示分层信息-->
                    <!-- ko if: $root.gradeClassInfo().schoolLevel == 'HIGH' -->
                    <!-- ko if: groupIds.length != 0 -->
                    <div class="artScienceType-para">
                        <!-- ko if: artScienceType == null || artScienceType == '' || artScienceType == 'UNKNOWN' || artScienceType == 'ARTSCIENCE' -->不分文理科<!-- /ko -->
                        <!-- ko if: artScienceType == 'ART' -->文科<!-- /ko -->
                        <!-- ko if: artScienceType == 'SCIENCE' -->理科<!-- /ko -->
                    </div>
                    <!-- /ko -->
                    <!-- ko if: groupIds.length == 0-->
                    <div class="artScienceType-para">&nbsp;&nbsp;</div>
                    <!-- /ko -->
                    <!-- /ko -->

                    <!-- ko if:subjectAndTeacher.length == 0 -->
                    <div class="side">
                        <div>暂无老师和学生</div>
                    </div>
                    <!-- /ko -->
                    <!-- ko if:subjectAndTeacher.length != 0 -->
                    <div class="side">
                        <!-- ko foreach:subjectAndTeacher -->
                        <!-- ko switch:subject -->
                        <!-- ko case: 'ENGLISH' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'英语：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'英语：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'MATH' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'数学：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'数学：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'CHINESE' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'语文：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'语文：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'PHYSICS' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'物理：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'物理：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'CHEMISTRY' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'化学：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'化学：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'BIOLOGY' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'生物：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'生物：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'POLITICS' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'政治：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'政治：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'GEOGRAPHY' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'地理：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'地理：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'HISTORY' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'历史：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'历史：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'INFORMATION' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'信息：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'信息：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- /ko -->
                    </div>
                    <!-- /ko -->

                    <!-- ko if: groupIds.length != 0 -->
                    <a href="javascript:void(0);" class="cli_btn fr" data-bind="click: $root.showCardDetail.bind($data,$parent.gradeId)">点击查看</a>
                    <!-- /ko -->
                    <!-- ko if: groupIds.length == 0 -->
                    <a href="javascript:void(0);" class="cli_btn fr" data-bind="click: $root.deleteCardClazz.bind($data,$parent.gradeId)" style="color: #ff0000;">删除班级</a>
                    <!-- /ko -->

                <#-- 合并班群功能-->
                    <div class="group-mergebox JS-groupBox">
                    <#--已选中（选中打勾）-->
                        <div class="group-selectbox">
                            <img src="<@app.link href='public/skin/specialteacher/images/group_select.png'/>" alt="">
                        </div>
                    <#--鼠标滑过（可点击手势）-->
                        <div class="group-hoverbox">
                            <img src="<@app.link href='public/skin/specialteacher/images/group_select_hover.png'/>" alt="">
                        </div>
                    <#--选择错误（错误，2s消失）-->
                        <div class="group-errorbox">
                            <img src="<@app.link href='public/skin/specialteacher/images/group_select_disabled.png'/>" alt="">
                            <p>不能合并两个相同的科目班群！</p>
                        </div>
                    <#--不能选择（置灰）-->
                        <div class="group-disabledbox"></div>
                    </div>
                </div>
            </li>
            <!-- /ko -->
            <!-- /ko -->

            <!-- ko ifnot:$root.isShowMerge()-->
            <li>
                <div class="addclass-box" data-bind="click: $root.createSchoolClass.bind($data, '1')">
                    <p>新增校内班级</p>
                </div>
            </li>
            <!-- /ko -->
        </ul>
    </div>

    <div class="claManagement-list">
        <div class="title">教学班：</div>
        <ul class="clazzDetails">
            <!--ko if:teachingClass.length != 0 -->
            <!--ko foreach:teachingClass  -->
            <li class="JS-mergeOperateBox2">
                <div class="box box2">
                    <div class="content">
                        <img class="modify-icon" src="<@app.link href='public/skin/specialteacherV2/images/dean/dean-btn02.png'/>" data-bind="click:$root.editCardClazzName.bind($data,$parent.gradeId)">
                        <div class="num fr" data-bind="text:studentNum+'人', visible: studentNum > -1"></div>
                        <div class="name" data-bind="text:groupName, attr: {title: groupName}"></div>
                    </div>

                <#--初高中均展示分层信息-->
                    <!-- ko if: groupIds.length != 0 -->
                    <div class="stageType-para" data-bind="html: stageType"></div>
                    <!-- /ko -->
                    <!-- ko if: groupIds.length == 0 -->
                    <div class="stageType-para">&nbsp;&nbsp;</div>
                    <!-- /ko -->

                    <!-- ko if:subjectAndTeacher.length == 0 -->
                    <div class="side">
                        <div>暂无老师和学生</div>
                    </div>
                    <!-- /ko -->
                    <!-- ko if:subjectAndTeacher.length != 0 -->
                    <div class="side" style="overflow:hidden;text-overflow:ellipsis;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:2;height: 60px;"> <#--// TODO 样式拆到css-->
                        <!-- ko foreach:subjectAndTeacher -->
                        <!-- ko switch:subject -->
                        <!-- ko case: 'ENGLISH' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'英语：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'英语：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'MATH' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'数学：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'数学：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'CHINESE' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'语文：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'语文：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'PHYSICS' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'物理：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'物理：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'CHEMISTRY' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'化学：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'化学：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'BIOLOGY' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'生物：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'生物：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'POLITICS' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'政治：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'政治：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'GEOGRAPHY' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'地理：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'地理：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'HISTORY' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'历史：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'历史：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'INFORMATION' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'信息：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'信息：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- /ko -->
                    </div>
                    <!-- /ko -->

                    <!-- ko if: groupIds.length != 0 -->
                    <a href="javascript:void(0);" class="cli_btn fr" data-bind="click:$root.showCardDetail.bind($data,$parent.gradeId)">点击查看</a>
                    <!-- /ko -->
                    <!-- ko if: groupIds.length == 0 -->
                    <a href="javascript:void(0);" class="cli_btn fr" data-bind="click: $root.deleteCardClazz.bind($data,$parent.gradeId)" style="color: #ff0000;">删除班级</a>
                    <!-- /ko -->

                <#--当行政班执行合并操作是，教学班虽不能合并，但此时禁止操作-->
                    <div class="group-mergebox2 JS-groupBox2">
                    <#--不能选择（置灰）-->
                        <div class="group-disabledbox"></div>
                    </div>
                </div>
            </li>
            <!-- /ko -->
            <!-- /ko -->

            <!-- ko ifnot:$root.isShowMerge()-->
            <li>
                <div class="addclass-box" data-bind="click: $root.createSchoolClass.bind($data, '3')">
                    <p>新增校内班级</p>
                </div>
            </li>
            <!-- /ko -->
        </ul>
    </div>
</script>

<#--班级卡片模板-->
<script id="classCardListTemp" type="text/html">
    <#--<!-- ko if:groups.length == 0 &ndash;&gt;
    <div style="padding:200px 0 0 0;text-align: center;font-size:18px;color:#4d4d4d">班级内暂无老师</div>
    <!-- /ko &ndash;&gt;-->

    <!-- ko if:groups.length != 0 -->
    <div class="claManagement-list">
        <ul class="clazzDetails">
            <!--ko foreach:groups -->
            <li class="JS-mergeOperateBox" data-bind="click: $root.selectMergeGroup.bind($data)">
                <div class="box">
                    <div class="content">
                        <img class="modify-icon" src="<@app.link href='public/skin/specialteacherV2/images/dean/dean-btn02.png'/>" data-bind="click:$root.editCardClazzName.bind($data,$parent.gradeId)">
                        <div class="num fr" data-bind="text:studentNum+'人', visible: studentNum > -1"></div>
                        <div class="name" data-bind="text:groupName, attr: {title: groupName}"></div>
                    </div>

                <#--高中、行政班、非空数据（studentNum = -1 为人为创建）-->
                    <!-- ko if:($root.gradeClassInfo().schoolLevel == 'HIGH' && groupType == 'SYSTEM_GROUP' &&  studentNum != -1)-->
                    <div class="artScienceType-para">
                        <!-- ko if: (artScienceType == null || artScienceType == '' || artScienceType == 'UNKNOWN' || artScienceType == 'ARTSCIENCE') -->不分文理科<!-- /ko -->
                        <!-- ko if: artScienceType == 'ART' -->文科<!-- /ko -->
                        <!-- ko if: artScienceType == 'SCIENCE' -->理科<!-- /ko -->
                    </div>
                    <!-- /ko -->

                    <!-- ko if: groupType != 'SYSTEM_GROUP' &&  studentNum != -1 -->
                    <div class="stageType-para" data-bind="html: stageType"></div>
                    <!-- /ko -->

                    <!-- ko if:subjectAndTeacher.length == 0 -->
                    <div class="side">
                        <div>暂无老师和学生</div>
                    </div>
                    <!-- /ko -->
                    <!-- ko if:subjectAndTeacher.length != 0 -->
                    <div class="side">
                        <!-- ko foreach:subjectAndTeacher -->
                        <!-- ko switch:subject -->
                        <!-- ko case: 'ENGLISH' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'英语：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'英语：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'MATH' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'数学：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'数学：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'CHINESE' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'语文：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'语文：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'PHYSICS' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'物理：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'物理：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'CHEMISTRY' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'化学：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'化学：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'BIOLOGY' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'生物：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'生物：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'POLITICS' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'政治：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'政治：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'GEOGRAPHY' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'地理：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'地理：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'HISTORY' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'历史：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'历史：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- ko case: 'INFORMATION' -->
                        <!-- ko if:!$data.teacherName -->
                        <div class="sub" data-bind="text:'信息：无'"></div>
                        <!-- /ko -->
                        <!-- ko if:$data.teacherName -->
                        <div class="sub" data-bind="text:'信息：'+teacherName"></div>
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- /ko -->
                        <!-- /ko -->
                    </div>
                    <!-- /ko -->

                    <!-- ko if: groupIds.length != 0 -->
                    <a href="javascript:void(0);" class="cli_btn fr" data-bind="click:$root.showCardDetail.bind($data,$parent.gradeId)">点击查看</a>
                    <!-- /ko -->
                    <!-- ko if: groupIds.length == 0 -->
                    <a href="javascript:void(0);" class="cli_btn fr" data-bind="click: $root.deleteCardClazz.bind($data,$parent.gradeId)" style="color: #ff0000;">删除班级</a>
                    <!-- /ko -->

                <#-- 合并班群功能-->
                    <div class="group-mergebox">
                    <#--已选中（选中打勾）-->
                        <div class="group-selectbox">
                            <img src="<@app.link href='public/skin/specialteacher/images/group_select.png'/>" alt="">
                        </div>
                    <#--鼠标滑过（可点击手势）-->
                        <div class="group-hoverbox">
                            <img src="<@app.link href='public/skin/specialteacher/images/group_select_hover.png'/>" alt="">
                        </div>
                    <#--选择错误（错误，2s消失）-->
                        <div class="group-errorbox">
                            <img src="<@app.link href='public/skin/specialteacher/images/group_select_disabled.png'/>" alt="">
                            <p>不能合并两个相同的科目班群！</p>
                        </div>
                    <#--不能选择（置灰）-->
                        <div class="group-disabledbox"></div>
                    </div>
                </div>
            </li>
            <!-- /ko -->

            <!-- ko ifnot:$root.isShowMerge()-->
            <li>
                <!-- ko if: $root.gradeMenu().clazzInfo() && $root.gradeMenu().clazzInfo().clazzType == 'WALKING' -->
                <div class="addclass-box" data-bind="click: $root.createSchoolClass.bind($data, '3')">
                    <p>新增校内班级</p>
                </div>
                <!-- /ko -->
                <!-- ko ifnot: $root.gradeMenu().clazzInfo() && $root.gradeMenu().clazzInfo().clazzType == 'WALKING' -->
                <div class="addclass-box" data-bind="click: $root.createSchoolClass.bind($data, '1')">
                    <p>新增校内班级</p>
                </div>
                <!-- /ko -->
            </li>
            <!-- /ko -->
        </ul>
    </div>
    <!-- /ko -->
</script>

<#--班群详情页面模板-->
<script id="classGroupDetailTemp" type="text/html">
    <!-- ko if:hasDetail -->
    <div class="class-module mt-20">
        <div class="teacherManagement-title bg-f6 clearfix">
            <a href="javascript:void(0);" class="green_fontBtn fr" data-bind="click:addStudentAccount">添加学生账号</a>
            <div>
                <span class="title span-space dark-color" data-bind="text: groupTypeName"></span>
                <span class="title span-space gradeid" data-bind="text:groupName,attr:{gradeId:gradeId}"></span>
                <!-- ko if:groupType !== 'SYSTEM_GROUP' -->
                <span class="title">任课老师：<span data-bind="text: subjAndTeacherList[0].teacherList[0].teacherName"></span><i class="item_btn icon-margin" data-bind="click:$root.editSubjectTeacher.bind($data,$data)"></i></span>
                <!-- /ko -->
            </div>
        </div>
    </div>
    <!-- ko if:groupType === 'SYSTEM_GROUP' -->
    <div class="class-module mt-20">
        <table class="clazzTable" cellpadding="0" cellspacing="0">
            <tr>
                <td rowspan="2" style="width: 100px;">任课老师</td>
                <!-- ko foreach:subjectArray -->
                <td data-bind="text:value"></td>
                <!-- /ko -->
            </tr>
            <tr data-bind="foreach:subjAndTeacherList">
                <!-- ko if:hasTeacher -->
                <td>
                    <!-- ko if:teacherList.length !=0 -->
                    <!-- ko foreach:teacherList -->
                    <span class="name" data-bind="text:teacherName"></span>
                    <i class="item_btn" data-bind="click:$root.editSubjectTeacher.bind($data,$data)"></i>
                    <!-- /ko -->
                    <!-- /ko -->
                </td>
                <!-- /ko -->
                <!-- ko if:!hasTeacher -->
                <td><a href="javascript:void(0);" class="green_fontBtn" data-bind="click:$root.addSubjectTeacher.bind($data,$data)">添加</a></td>
                <!-- /ko -->
            </tr>
        </table>
    </div>
    <!-- /ko -->
    <div class="clazzDetails-box mt-20">
        <div class="clazz-title">
            班级学生（学生数：<!--ko text:classGroupStudents().length--><!--/ko-->）
        </div>
        <!-- ko if:(isShowStuDateAbnormalTip() || mergeStudentsNames().length != 0) -->
        <div class="other-tip">
            <!-- ko if:isShowStuDateAbnormalTip -->
            <div class="tip">
                <i></i>系统检测到学生信息异常，<span class="green" data-bind="click: $root.restoreDetailStuDate">点此恢复</span>
            </div>
            <!-- /ko -->
            <!-- ko if: mergeStudentsNames().length != 0 -->
            <div class="tip">
                <i></i>系统检测到同姓名学生账号可合并，<span class="green" data-bind="click: $root.mergeDetailStuDate">点此合并</span>
            </div>
            <!-- /ko -->
        </div>
        <!-- /ko -->
        <div class="table-mode">
            <table cellpadding="0" cellspacing="0">
                <thead>
                <tr>
                    <!--ko if:isShow == 'SYSTEM_GROUP'-->
                    <td>选择</td>
                    <!--/ko-->
                    <td style="cursor: pointer;" data-bind="click: $root.orderStudentName">学生姓名<i class="studentname-sequencing JS-studentNameSeq order"></i></td>
                    <td style="cursor: pointer;" data-bind="click: $root.orderStudentNo">校内学号<i class="studentno-sequencing JS-studentNoSeq"></i></td>
                    <td>一起ID</td>
                    <td>手机</td>
                    <td>阅卷机填涂号
                        <div class="icon-grayAsk global-ques">
                            <div class="text">1. 通过阅卷机填涂号，扫描答题卡时可定位该学生；<br>2. 在不重复的情况下，将使用学生学号后<span data-bind="text:scanNumDigit"></span>位作为填涂号，如遇重复，将随机生成，老师可自行修改；<br>3. 每次修改一定要及时告知学生，否则会影响学生答题卡扫描</div>
                        </div>
                    </td>
                    <td>标记</td>
                    <td>操作</td>
                </tr>
                </thead>
                <tbody>
                <!-- ko if:classGroupStudents().length == 0 -->
                <tr>
                    <td colspan="7">暂无</td>
                </tr>
                <!-- /ko -->
                <!-- ko if:classGroupStudents().length != 0 -->
                <!-- ko foreach:classGroupStudents() -->
                <tr <#--class="active"--> data-bind="attr:{dataid: klxUserId,a17id:a17id,datasum:studentName}">
                    <!--ko if:$parent.isShow == 'SYSTEM_GROUP'-->
                    <td><span class="icon-content" data-bind="click: $root.changeCgs.bind($data)"></span></td>
                    <!--/ko-->
                    <td><span class="name student-name-maxl" data-bind="text:studentName"></span></td>
                    <td data-bind="text:studentNum?studentNum:'未添加'"></td>
                    <td data-bind="text:a17id?a17id:'未注册'"></td>
                    <td data-bind="text:studentMobile?studentMobile:'未绑定'"></td>
                    <td data-bind="text:scanNum?scanNum:'未添加'"></td>
                    <td data-bind="text:isMarked?'借读生':'--'"></td>
                    <td>
                        <a href="javascript:void(0);" class="green_fontBtn" data-bind="attr: {'data-ismarked': isMarked}, click:$root.editCgs">编辑</a>
                        <!--ko if:a17id-->
                        <a href="javascript:void(0);" class="green_fontBtn" data-bind="click:$root.resetCgs">重置密码</a>
                        <!--/ko-->
                        <!--ko ifnot:a17id--><a href="javascript:;" class="green_fontBtn" style="cursor:default;">----</a><!--/ko-->
                        <a href="javascript:void(0);" class="green_fontBtn" data-bind="click:$root.delCgs">删除</a>
                    </td>
                </tr>
                <!-- /ko -->
                <!-- /ko -->
                </tbody>
            </table>
        </div>
    </div>
    <div class="clazzDetails-btn">
        <!--ko if:isShow == 'SYSTEM_GROUP' &&　classGroupStudents().length != 0-->
        <a class="green-btn" href="javascript:void(0);" data-bind="click: $root.changeCgsBtn.bind($data,1)">转班</a>
        <a class="green-btn" href="javascript:void(0);" data-bind="click: $root.changeCgsBtn.bind($data,2)">复制到教学班</a>
        <!-- /ko -->
        <a class="green_fontBtn del-group"  data-bind="click: $root.delGroup.bind($data)">删除该班群</a>
    </div>
    <!-- /ko -->
</script>

<#--调整班级页-->
<script id="adjustClassTemp" type="text/html">
    <!-- ko if:defaultTemp -->
    <div class="" style="text-align: center;padding-top: 205px;font-size:18px;color:#4d4d4d;">请选择年级</div>
    <!-- /ko -->
    <!-- ko if:!defaultTemp -->
    <!-- ko if:aClassList.length != 0 -->
    <div class="claManagement-list">
        <div class="title">行政班：</div>
        <ul class="clazzList">
            <!-- ko foreach:aClassList -->
            <li>
                <div class="mode">
                    <div style="display: inline-block;">
                        <a href="javascript:void(0);" class="item_btn js-editClassNameBtn" data-bind="attr:{'data-cname':clazzName}"></a>
                        <span data-bind="text:clazzName" class="js-classNameItem"></span>
                        <a href="javascript:void(0);" class="item_btn del_btn js-removeClassItemBtn" data-bind="attr:{'data-cid':clazzId,'data-cname':clazzName}"></a>
                    </div>
                    <div style="display: none;" class="js-editClassNameItem">
                        <input type="text" class="txt" placeholder="" maxlength="20">
                        <a href="javascript:void(0);" class="sure_btn js-editClassNameSureBtn" data-bind="attr:{'data-cid':clazzId}">确定</a>
                    </div>
                </div>
            </li>
            <!-- /ko -->
        </ul>
    </div>
    <!-- /ko -->
    <!-- ko if:tClassList.length != 0 -->
    <div class="claManagement-list">
        <div class="title">教学班：</div>
        <ul class="clazzList">
            <!-- ko foreach:tClassList -->
            <li>
                <div class="mode">
                <#--<a href="javascript:void(0);" class="item_btn"></a>-->
                    <span data-bind="text:clazzName"></span>
                    <a href="javascript:void(0);" class="item_btn del_btn js-removeClassItemBtn" data-bind="attr:{'data-cid':clazzId,'data-cname':clazzName}"></a>
                </div>
            </li>
            <!-- /ko -->
        </ul>
    </div>
    <!-- /ko -->

    <!-- ko if:tClassList.length == 0 && aClassList.length == 0-->
    <div class="" style="text-align: center;padding-top: 205px;font-size:18px;color:#4d4d4d;">暂无班级</div>
    <!-- /ko -->
    <!-- /ko -->
</script>

<#--搜索学生结果-->
<script id="searchStudentDialog" type="text/html">
    <div class="table-mode tablePop">
        <!-- ko if:resultList.length != 0 -->
        <div class="table-title">查询到以下学生：</div>
        <table cellpadding="0" cellspacing="0">
            <thead>
            <tr>
                <td>姓名</td>
                <td>学号</td>
                <td>班级</td>
                <td>操作</td>
            </tr>
            </thead>
            <tbody>
            <!-- ko foreach:resultList -->
            <tr>
                <td><span  class="name" data-bind="text:studentName"></span></td>
                <td data-bind="text:studentNum"></td>
                <td data-bind="text:clazzName"></td>
                <td><a href="javascript:void(0);" class="green_fontBtn" data-bind="click:$parent.showStudentDetail.bind($data,$data)">点击查看</a></td>
            </tr>
            <!-- /ko -->
            </tbody>
        </table>
        <!-- /ko -->
        <!-- ko if:resultList.length == 0 -->
        <div style="padding:30px 0 50px 0;text-align: center;font-size:16px;color:#4d4d4d;">无搜索结果</div>
        <!-- /ko -->
    </div>
</script>

<#--添加学生弹窗-->
<script id="addStudentDialogTemp" type="text/html">
    <div class="addStudentPop">
        <div class="add-title" data-bind="text:'为 '+classGroupName+' 添加学生，请选择添加方式：'"></div>
        <div class="btn">
            <a href="javascript:void(0);" data-bind="click:addByOnline">通过在线添加</a>
            <a href="javascript:void(0);" data-bind="click:addByExcel">通过excel上传</a>
        </div>
    </div>
</script>

<#--在线添加学生弹窗-->
<script id="onlineAddStudentDialogTemp" type="text/html">
    <div class="addAccountOnlinePop">
        <div class="account-top mt-20">
            <p>添加学生规则：</p>
            <p>1. 每行一次输入：学生姓名（必填）、校内学号（选填），以空格隔开。</p>
            <p>2. 姓名仅支持中文，每行输入一个学生。</p>
            <p>3. 如果填写校内学号，则会将学号后<span data-bind="text:scanNumDigit"></span>位作为学生的阅卷机填涂号；如遇校内重复则随机生成<span data-bind="text:scanNumDigit"></span>位数。</p>
            <p>4. 班内学生姓名不可重复，如遇重名请做以区分，如张三甲、张三乙。</p>
            <p>5. 仅支持添加20个以内的学生账号，更多账号请通过excel上传添加。</p>
        </div>
        <div class="account-column mt-20">
            <div class="left">
                <textarea name="" id="" cols="30" rows="10" data-bind="value:onlineText"></textarea>
                <div class="text" data-bind="if: errorInfo"><i></i><span data-bind="html:errorInfo"></span></div>
            </div>
            <div class="right">
                <div class="info">
                    <p>小提示：</p>
                    <p class="con">您可以从已有学生名单的文档中，把学生信息粘贴到左侧文本框中，如下：</p>
                    <p>1.打开文档，选择文档中需要上传学生的姓名，单击鼠标右键复制；</p>
                    <p>2.点击左侧输入框，单击鼠标右键粘贴。</p>
                </div>
                <div class="table-mode">
                    <table cellpadding="0" cellspacing="0">
                        <thead>
                        <tr>
                            <td>学生姓名</td>
                            <td>校内学号</td>
                        </tr>
                        </thead>
                        <tbody>
                        <tr class="active">
                            <td><span class="name">曲筱绡</span></td>
                            <td>3983993</td>
                        </tr>
                        <tr>
                            <td><span class="name">曲筱绡</span></td>
                            <td>3983993</td>
                        </tr>
                        </tbody>
                    </table>
                    <div class="copyCut-box">
                        <ul>
                            <li><i class="icon-copy"></i>复制（C）</li>
                            <li><i class="icon-copy cut"></i>剪切（T）</li>
                            <li><i class="icon-copy paste"></i>粘贴（P）</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>

<#--excel添加学生弹窗-->
<script id="excelAddStudentDialogTemp" type="text/html">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-upload">
                <div class="head clearfix">
                    <div class="title">选择文档：</div>
                    <a href="javascript:;" class="upload" data-bind="click:uploadExcelToStuAccount">上传</a>
                    <a href="/specialteacher/gettemplate.vpage?template=xcx" class="download" target="_blank">下载模板</a>
                    <input type="file" id="stuExcelFile" accept=".xls, .xlsx" name="file" style="display: none" data-bind="event:{'change':stuExcelFileChange}"/>
                    <div class="name" data-bind="text:stuExcelFileName"></div>
                </div>
                <div class="text">
                    <div>上传说明：</div>
                    <p><em>1.</em>需要按照模版格式要求上传学生姓名、学号；</p>
                    <p><em>2.</em>班内无该姓名学生时，会为其新注册账号：有该学生时，会更新该生学号信息；</p>
                    <p><em>3.</em>学号后<span data-bind="text:scanNumDigit"></span>位在学校内不重复的情况下将作为学生阅卷机填涂号，如遇重复将随机生成<span data-bind="text:scanNumDigit"></span>位数字；</p>
                </div>
                <div class="prom" data-bind="if: stuExcelTypeError"><i></i><span data-bind="html:stuExcelTypeError"></span></div>
            </div>
        </div>
    </div>
</script>

<#--添加学生成功-->
<script id="addStudentSuccessTemp" type="text/html">
    <div class="layer-module">
        <div class="layer-success">
            <div class="success"><span>导入成功</span></div>
            <div class="title" data-bind="text:'新注册'+newSignNum()+'名学生，恢复'+recoverNum()+'名学生，更新'+updateNum()+'名学生学号！'"></div>
            <div class="text">注：恢复学生后，请检查学生学号及填涂号是否与实际一致。阅卷机填涂号更新后，一定要及时告知学生，否则会影响学生答题卡扫描</div>
        </div>
    </div>
</script>

<#--添加学生存在重复提示-->
<script id="addStudentRepeatedErrorTemp" type="text/html">
    <div class="layer-module">
        <div class="layer-success">
            <div style="padding: 0 40px;">
                上传名单中学生 <span class="" style="font-weight: bold" data-bind="text:repeatedNames"></span> 和班内已有学生重名，请确认是否更新班内学生学号信息。<br/>
                如不是同一学生，请点击取消、并修改姓名进行区分。
            </div>
            <div class="info" data-bind="if: errorInfo"><i></i><span data-bind="html:errorInfo"></span></div>
        </div>
    </div>
</script>

<#--添加学生存在占用提示-->
<script id="addStudentTakeUpErrorTemp" type="text/html">
    <div class="layer-module">
        <div class="layer-success">
            <div>
                学生 <span class="" style="font-weight: bold" data-bind="text:importStudentNames"></span>学生的填涂号无法使用其学号后<span data-bind="text:scanNumDigit"></span>位,已被如下学生占用：<br/>
                <span class="" style="font-weight: bold" data-bind="html:takeUpSutInfo"></span>
                点击确认可使用<span data-bind="text:scanNumDigit"></span>位随机填涂号，如有问题，可联系以上老师或客服进行处理
            </div>
            <div class="info" data-bind="if: errorInfo"><i></i><span data-bind="html:errorInfo"></span></div>
        </div>
    </div>
</script>

<#--添加学生选择是否恢复-->
<script id="addStudentRestoreTemp" type="text/html">
    <div class="layer-module">
        <div class="layer-success">
            <div style="padding: 0 40px;">
                该班级删除学生历史中与导入学生同名：<br/><span data-bind="html: restoreStudentInfo"></span><br/>是否恢复学生？
            </div>
            <div class="info" data-bind="if: errorInfo"><i></i><span data-bind="html:errorInfo"></span></div>
        </div>
    </div>
</script>

<#--新增班级弹窗-->
<script id="createSchoolClassTemp" type="text/html">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-label">
                <div class="label">
                    <div class="title">选择班级类型：</div>
                    <select class="sel" name="" id="" data-bind="
                         options: classTypes,
                         optionsText: 'name',
                         value:classType,
                         optionsCaption: '选择班级类型',
                         event:{change:changeClassType}">
                    </select>
                </div>
                <div class="label">
                    <div class="title">选择班级年级：</div>
                    <select class="sel" name="" id="" data-bind="
                         options: classGrades,
                         optionsText: 'gradeName',
                         value:classGrade,
                         optionsCaption: '选择班级年级'">
                    </select>
                </div>
                <div class="label" data-bind="visible:showTeachingSubject()">
                    <div class="title">选择走课学科：</div>
                    <select class="sel" name="" id="" data-bind="
                         options: classSubjects,
                         optionsText: 'value',
                         value:classSubject,
                         optionsCaption: '选择走课学科',
                         event:{change:changeClassSubject}">
                    </select>
                </div>
                <!-- ko if:hasTeacherList -->
                <div class="label" data-bind="visible:showTeachingTeacher()">
                    <div class="title">选择任课老师：</div>
                    <select class="sel" name="" id="" data-bind="
                         options: walkingTeachers,
                         optionsText: function(item) {
                           return item.teacherName + ' (' + item.teacherId + ')'
                         },
                         value:walkingTeacher,
                         optionsCaption: '选择任课老师'">
                    </select>
                </div>
                <!-- /ko -->
                <div class="label">
                    <div class="title">填写班级名称：</div>
                    <input type="text" class="txt" data-bind="value:className" maxlength="25" style="width: 188px;">
                </div>

                <div class="label" data-bind="visible: showTeachingTeacher()">
                    <div class="title">设置分层：</div>
                    <select class="sel" name="" id="" data-bind="
                        options: stageTypeArr,
                        value: stageTypeValue"></select>
                </div>
            </div>
            <div class="error-edit" data-bind="visible:errorInfo!='',text:errorInfo"></div>
        </div>
    </div>
</script>

<#--添加学生、修改学生-->
<script id="adjustTeacherDialog" type="text/html">
    <div class="newGroupPop">
        <div class="management-column">
        <#--最新需求：对于添加学生和修改学生（dialogType类型为：adjust），将下拉框移除。新建班群（dialogType类型为：add）保留原有select-->
            <!-- ko if:dialogType == "add" -->
            <span></span>
            <!-- /ko -->
            <!-- ko if:dialogType == "adjust" -->
            <span>为</span>
            <!-- /ko -->

            <span>
                <!-- ko if:dialogType == "add" -->
                <select class="sel js-gradeSelItem" data-bind="
                        options: Grades,
                        optionsText:'gradeName',
                        value:grade,
                        event:{change:changeGrade},
                        optionsCaption: '选择年级'">
                </select>
                <!-- ko with:grade  -->
                <select class="sel js-clazzSelItem" data-bind="
                        options: clazzs,
                        optionsText:'clazzName',
                        value:$parent.clazz,
                        optionsCaption: '选择班级'">
                </select>
                <!-- /ko -->
                <select class="sel js-subjectSelItem" data-bind="
                        options:subjects,
                        optionsText:'value',
                        optionsValue:'key',
                        value:subject,
                        event:{change:changeSubject},
                        optionsCaption: '选择学科'">
                </select>
                <!-- /ko -->

                <!-- ko if:dialogType == "adjust" -->
                <!-- ko with:grade -->
                <span data-bind="text: gradeName"></span>
                <!-- /ko -->
                <span> | </span>
                <!-- ko with:clazzGroup-->
                <span data-bind="text: groupName"></span>
                <!-- /ko -->
                <span>调整</span>
                <span data-bind="text: subjectName"></span>
                <!-- /ko -->
            </span>

        <#--******************************************************************-->
        <#--此处为原始结构，备份，防止后期更换需求-->
        <#--<!-- ko if:dialogType == "adjust" &ndash;&gt;-->
        <#--<span>为</span>-->
        <#--<!-- /ko &ndash;&gt;-->
        <#--<span>-->
        <#--<select class="sel js-gradeSelItem" data-bind="-->
        <#--options: Grades,-->
        <#--optionsText:'gradeName',-->
        <#--value:grade,-->
        <#--event:{change:changeGrade},-->
        <#--optionsCaption: '选择年级'">-->
        <#--</select>-->
        <#--<!-- ko if:dialogType == "add" &ndash;&gt;-->
        <#--<!-- ko with:grade  &ndash;&gt;-->
        <#--<select class="sel js-clazzSelItem" data-bind="-->
        <#--options: clazzs,-->
        <#--optionsText:'clazzName',-->
        <#--value:$parent.clazz,-->
        <#--optionsCaption: '选择班级'">-->
        <#--</select>-->
        <#--<!-- /ko &ndash;&gt;-->
        <#--<!-- /ko &ndash;&gt;-->
        <#--<!-- ko if:dialogType != "add" &ndash;&gt;-->
        <#--<!-- ko with:grade  &ndash;&gt;-->
        <#--<select class="sel js-clazzSelItem" data-bind="-->
        <#--options: classGroups,-->
        <#--optionsText:'groupName',-->
        <#--value:$parent.clazzGroup,-->
        <#--optionsCaption: '选择班群'">-->
        <#--</select>-->
        <#--<!-- /ko &ndash;&gt;-->
        <#--<!-- /ko &ndash;&gt;-->
        <#--<!-- ko if:dialogType == "adjust" &ndash;&gt;-->
        <#--<span>调整</span>-->
        <#--<!-- /ko &ndash;&gt;-->
        <#--<select class="sel js-subjectSelItem" data-bind="-->
        <#--options:subjects,-->
        <#--optionsText:'value',-->
        <#--optionsValue:'key',-->
        <#--value:subject,-->
        <#--event:{change:changeSubject},-->
        <#--optionsCaption: '选择学科'">-->
        <#--</select>-->
        <#--</span>-->
        <#--******************************************************************-->

            <!-- ko if:dialogType == "adjust" -->
            <span>老师如下：</span>
            <!-- /ko -->

            <div class="search-btn fr">
                <!-- ko if:dialogType == "adjust" -->
                <!-- ko if:temporaryTeacher() -->
                <div class="nowhas-teacherinfo" data-bind="with:temporaryTeacher"><span class="name" data-bind="text: teacherName"></span> <span data-bind="text: teacherId"></span><img class="delete-icon" src="<@app.link href='public/skin/specialteacher/images/dean/dean-btn01.png'/>" alt="" data-bind="click: $parent.deleteTeacher.bind($data)"></div>
                <!-- /ko -->
                <!-- ko if:dialogTypeDetail() == 'adjust' && !temporaryTeacher() -->
                <p class="nownoteacher">当前暂无老师，请选择添加老师</p>
                <!-- /ko -->
                <!-- /ko -->
                <input type="text" placeholder="老师姓名／ID／手机号" data-bind="value:searchTeacherName, event:{keyup: $root.searchTeacherEvent.bind($data)}" class="JS-inputSearchTeacher"><a href="javascript:void(0);" class="search_btn" data-bind="click:$root.searchTeacher.bind($data)">搜索</a>
            </div>
        </div>
        <div class="claManagement-list">
            <div class="subtitle">选择老师</div>
            <ul class="clazzList">
                <!--ko if:newTeacherList().length != 0 -->
                <!--ko foreach:newTeacherList() -->
                <li><div class="mode js-newTeacherItem" data-bind="click:$parent.chooseTeacher.bind($data)"><span class="name" data-bind="text:teacherName"></span> <span data-bind="text:teacherId"></span></div></li>
                <!-- /ko -->
                <!-- /ko -->
                <!--ko if:newTeacherList().length == 0 -->
                <li class="searchInfo"><i class="icon-search"></i>未找到老师，请核对信息或先注册老师账号</li>
                <!-- /ko -->
            </ul>
        </div>
        <div data-bind="visible:errorInfo!=''">
            <p data-bind="text:errorInfo" style="color: red;"></p>
        </div>
    </div>
</script>

<#--编辑学生弹窗-->
<script id="editStudentInfo" type="text/html">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-label" style="min-height:auto;">
                <div class="label">
                    <div class="title">学生姓名：</div>
                    <input type="text" class="student-name" data-bind="attr:{value: $root.editStudentInfoData().studentName}">
                </div>
                <div class="label">
                    <div class="title">校内学号：</div>
                    <input type="text" class="student-number" data-bind="attr:{value: $root.editStudentInfoData().studentNum}">
                </div>
                <div class="label">
                    <div class="title">阅卷机填涂号：</div>
                    <input type="text" class="student-scan-number" data-bind="attr:{value: $root.editStudentInfoData().scanNum}">
                </div>
                <div class="label">
                    <div class="title">标记：</div>
                    <div class="tag" data-bind="css: {active: isTransientStu}, click: checkTransient">借读生</div>
                </div>
            </div>
            <div class="error-edit" data-bind="text: $root.editStudentInfoData().errorText"></div>
        </div>
    </div>
</script>

<#--重置密码弹窗-->
<script id="resetPassword" type="text/html">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-label" style="min-height:auto;">
                <div class="label">
                    <div class="title">新登录密码：</div>
                    <input type="text" class="v-password">
                </div>
                <div class="label">
                    <div class="title">再次输入新密码：</div>
                    <input type="text" class="v-confirmPassword">
                </div>
            </div>
            <div class="error-edit" data-bind="text: $root.resetPasswordData().errorText"></div>
        </div>
    </div>
</script>

<#--更换班级弹窗-->
<script id="changeClazz" type="text/html">
    <div class="transferPop">
        <div class="management-column">
            <div class="transfer-title">将以下学生<!--ko text:text --><!--/ko-->到：</div>
            <select class="sel" style="float:left;" disabled>
                <option data-bind="text: gradeName"></option>
            </select>
            <select class="sel js-choption" style="float:left;margin-left:20px;width: auto">
                <!-- ko if:clazzs.length == 0-->
                <option value="" class="noTea">暂无教学班群</option>
                <!--/ko-->
                <!-- ko if:clazzs.length > 0-->
                <!-- ko foreach:clazzs-->
                <option data-bind="text: groupName,attr:{value: groupIds[0]}"></option>
                <!--/ko-->
                <!--/ko-->
            </select>
        </div>
        <div class="claManagement-list">
            <ul class="clazzList js-clazzList" data-bind="foreach: changeList">
                <li data-bind="attr:{dataid:userid,a17id:a17id,name:name}"><div class="mode"><!--ko text:name--><!--/ko--><a href="javascript:void(0);" class="item_btn del_btn" data-bind="click: $parent.changeDelete.bind($data)"></a></div></li>
            </ul>
        </div>
        <div class="error-edit" data-bind="visible:errorInfo!='',text:errorInfo"></div>
    </div>
</script>

<#--删除班群确定弹窗-->
<script id="deleteGroupDialog" type="text/html">
    <div class="deleteGroupPop">
        <div>
            <div>
                <span>请选择删除原因：</span>
                <select name="" class="delete-group" data-bind="
                        options: delGroupCause,
                        value: delGroupValue,
                        optionsCaption: '请选择原因',
                        event:{change: selectDeleteGroupCause}"></select>
            </div>
            <p class="del-group-errtext" data-bind="text: delGroupErrorText"></p>
        </div>
    </div>
</script>

<#--修改卡片上班级名称弹窗-->
<script id="editCardClazzNameDialog" type="text/html">
    <div class="editCardClazzName">
        <div class="editcard-clazzname-box">
            <span>班级名称：</span>
            <input type="text" data-bind="
                value: newClazzName,
                event: {keyup: classNameInputFocus}">
        </div>

        <!-- ko if: isShowArtScienceSelect() -->
        <div class="editcard-clazzname-box">
            <span>设置文理科：</span>
            <select name="" id="" data-bind="
                options: artScienceTypeArr,
                optionsText: 'artScienceValue',
                value: artScienceType,
                event:{change:changeArtScienceType}">
            </select>
        </div>
        <!-- /ko -->

        <!-- ko if: isShowStageSelect() -->
        <div class="editcard-clazzname-box">
            <span>设置分层：</span>
            <select name="" id="" data-bind="
                options: stageTypeArr,
                value: stageType,
                event:{change:changeStageType}">
            </select>
        </div>
        <!-- /ko -->

        <p class="editcard-clazzname-errtext" data-bind="text: editCardClazzNameErrorText"></p>
    </div>
</script>

<#--删除卡片（空班级）-->
<script id="deleteCardClazzDialog" type="text/html">
    <div class="eleteCardClazz">
        <p>确定删除<span data-bind="text: $root.deleteCardClazzDialogData().deleteClazzName"></span>吗？</p>
        <p class="delete-card-errtext" data-bind="text: $root.deleteCardClazzDialogData().deleteCardClazzErrorText"></p>
    </div>
</script>

<#--更新学生学号弹窗-->
<script id="updateStuNoDialog" type="text/html">
    <div class="updateStuNoInfo">
        <p>1、上传的excel文件需为.xls或.xlsx格式</p>
        <p>2、一次导入最多支持1000行，超出时可分多次导入</p>
        <p>3、请确认填写的年级名称、班级名称与系统中一致</p>
        <p>4、请确认填写的学生姓名与系统中一致，若学生尚无账号需先添加学生账号</p>
        <p>5、学号要求校内不重复，学号更新后填涂号自动更新，如遇重复则随机生成</p>
        <p>6、excel模板如右图，<a href="javascript:void(0);" class="blue" data-bind="click: downLoadExcel.bind($data, 'usm')">点击下载模板</a></p>
        <form action="/specialteacher/admin/updatestudentnum.vpage" method="POST" id="updateStuNoForm" enctype="multipart/form-data">
            <input class="JS-fileupload" type="file" accept=".xls, .xlsx" name="adjustExcel" style="left:-9999px;position:absolute;display:none;">
        </form>
        <div class="table-mode">
            <table cellpadding="0" cellspacing="0">
                <thead>
                <tr>
                    <td>年级</td>
                    <td>班级</td>
                    <td>学生姓名</td>
                    <td>新学号</td>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>七年级</td>
                    <td>2班</td>
                    <td>张维</td>
                    <td>201801003</td>
                </tr>
                <tr>
                    <td>高一</td>
                    <td>1班</td>
                    <td>李利楠</td>
                    <td>201811092</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</script>

<#--更新学生学号报错弹窗-->
<script id="updateStuNoErrorDialog" type="text/html">
    <div>
        <p data-bind="html: updateStuNoErrorTypeOneText"></p>
    </div>
</script>

<#--批量生成学科组弹窗-->
<script id="batchGenerateSubjectDialog" type="text/html">
    <div class="batchGenerateSubject">
        <div class="batchGenerateSubjectList" data-bind="css: {'align-center': batchGenerateSubjectList().length < 4}">
            <!-- ko foreach:batchGenerateSubjectList -->
            <div>
                <input type="checkbox" data-bind="attr: { id : 'batchGenerate' + key}, event:{ change: $parent.choiceBatchGenerateSubject.bind($data)}">
                <label data-bind="attr: { for : 'batchGenerate' + key}, text: value"></label>
            </div>
            <!-- /ko -->
        </div>
        <p class="batchGenerateErrtext" data-bind="text: choiceBatchGenerateErrorText"></p>
    </div>
</script>