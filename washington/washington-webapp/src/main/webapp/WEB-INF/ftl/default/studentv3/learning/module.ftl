<#import "../layout/layout.ftl" as temp>
<#macro learningCenter menuName='index'>
    <@temp.page pageName='learcingCenter'>
    <style>
        /*switchBox*/
        .switchBox { position: relative;}
        .switchBox li { display: none;}
        .switchBox .tab{ text-align:center; padding:10px 0; font: 0px/0px arial;}
        .switchBox .tab .prve { width:10px; height:10px; overflow:hidden; display: inline-block; cursor: pointer; margin: 0 2px; border-radius: 10px; color: #fff; background-color: #eee;}
        .switchBox .tab .even { background-color: #bbb;}
        .switchBox .back, .switchBox .next{ margin-top: -22px; display: inline-block; padding:6px; cursor:pointer; font:1px/0px arial; border-radius:5px;}
        .switchBox .back{ float:left;}
        .switchBox .next{ float:right;}
    </style>
    <div class="t-learn-container">
        <div class="t-learn-inner">
            <div class="learn-slide w-fl-left">
                <div class="slide-inner">
                    <div class="l-title" id="l_left_menu_box">
                        <div class="nav w-ag-center">
                            <a class="l_left_menu_but" data-m_type="record" href="javascript:void (0)">作业记录</a>
                            <ul class="childMenu" style="display: none;">
                                <li data-subject_type="english" style="padding-top: 20px" id="englishTest">
                                    <a onclick="$17.tongji('左侧学习中心菜单-作业历史-英语历史');" href="/student/learning/history/list.vpage?subject=ENGLISH">英语作业</a>
                                </li>
                                <li data-subject_type="math">
                                    <a onclick="$17.tongji('左侧学习中心菜单-作业历史-数学历史');" href="/student/learning/history/list.vpage?subject=MATH">数学作业</a>
                                </li>
                                <li data-subject_type="chinese">
                                    <a onclick="$17.tongji('左侧学习中心菜单-作业历史-语文历史');" href="/student/learning/history/list.vpage?subject=CHINESE">语文作业</a>
                                </li>
                            </ul>
                        </div>
                        <div class="nav w-ag-center">
                            <a class="l_left_menu_but" data-m_type="history" href="javascript:void (0)">作业历史</a>
                            <ul class="childMenu" style="display: none;">
                                <li data-subject_type="english" style="padding-top: 20px" id="englishTest">
                                    <a onclick="$17.tongji('左侧学习中心菜单-作业历史-英语历史');" href="/student/learning/history/earlylist.vpage?subject=ENGLISH">英语历史</a>
                                </li>
                                <li data-subject_type="math">
                                    <a onclick="$17.tongji('左侧学习中心菜单-作业历史-数学历史');" href="/student/learning/history/earlylist.vpage?subject=MATH">数学历史</a>
                                </li>
                                <li data-subject_type="chinese">
                                    <a onclick="$17.tongji('左侧学习中心菜单-作业历史-语文历史');" href="/student/learning/history/earlylist.vpage?subject=CHINESE">语文历史</a>
                                </li>
                            </ul>
                        </div>
                        <div class="nav w-ag-center">
                            <a class="l_left_menu_but"  data-m_type="examination" href="javascript:void(0);">测试</a>
                            <ul class="childMenu" style="display: none;">
                                <li data-submenu="unittest" style="padding-top: 20px">
                                    <a onclick="$17.tongji('左侧学习中心菜单-作业历史-英语历史');" href="/student/learning/examination.vpage?submenu=unittest">单元检测</a>
                                </li>
                                <li data-submenu="regiontest">
                                    <a onclick="$17.tongji('左侧学习中心菜单-作业历史-数学历史');" href="/student/learning/examination.vpage?submenu=regiontest">地区统考</a>
                                </li>
                            </ul>
                        </div>
                        <#--<div class="nav w-ag-center">
                            <a class="l_left_menu_but" data-m_type="index" href="/student/learning/index.vpage">自学中心</a>
                        </div>-->
                    </div>
                    <div class="l-banner w-ag-center">
                        <div class="switchBox" id="a-headSwitchBanner-box"></div>
                    </div>
                </div>
            </div>
            <#nested >
        </div>
    </div>

    <script type="text/html" id="T:学生端学生中心广告">
        <ul style="height: 205px; overflow: hidden;">
            <%for(var i = 0, len = dataInfo.length; i < len; i++){%>
            <%if( i < 4 ){%>
            <li data-banner-voxlog="<%=dataInfo[i].id%>" <%if(i == 0){%>style="display: block;"<%}%>>
            <%if(!dataInfo[i].resourceUrl){%>
            <img src="<@app.avatar href="<%=dataInfo[i].img%>"/>"/>
            <%}else{%>
            <a href="<%=dataInfo[i].resourceUrl%>" target="_blank"><img src="<@app.avatar href="<%=dataInfo[i].img%>"/>"/></a>
            <%}%>
            </li>
            <%}%>
            <%}%>
        </ul>
        <div class="tab"></div>
    </script>

    <@sugar.capsule js=["learningcenterad"]/>
    <script type="text/javascript">
        $(function () {
            var mType =  '${menuName}';
            var $ul = $('#l_left_menu_box').find('a[data-m_type='+mType+']');
            $('#l_left_menu_box').find('a[data-m_type='+mType+']').addClass('active');
            //一级目录选择
            $("#l_left_menu_box a.l_left_menu_but").on('click', function() {
                var $this = $(this);
                $this.addClass('active');
                $this.parent().siblings().find("a.l_left_menu_but").removeClass('active');

                //查找子元素下是否有展开
                $('ul.childMenu').hide();
                $ul = $this.siblings('ul.childMenu');
                if($ul.length > 0){
                    $ul.show();
                }
            });

            //展开对应的子目录
            switch (mType) {
                case "examination":
                    var subMenuValue = $17.getQuery("submenu").toLocaleLowerCase();
                    $ul.next().show().find('li[data-submenu="' + subMenuValue + '"]').addClass('current');
                    break;
                default:
                    var subject = $17.getQuery("subject").toLocaleLowerCase();
                    $ul.next().show().find('li[data-subject_type="'+subject+'"]').addClass('current');
                    break;
            }

        });
    </script>
    </@temp.page>
</#macro>
