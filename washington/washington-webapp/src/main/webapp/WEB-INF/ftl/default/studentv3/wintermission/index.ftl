<#import '../layout/layout.ftl' as temp>
<@temp.page>
    <@sugar.capsule js=["countdown"] />
    <div class="t-home-container">
        <div class="t-home-main">
            <div class="th-learning" style="float: none; width: auto;">
                <div class="t-learning-tasks" style="width: auto;">
                    <!--left - right / btn -->
                    <div class="tl-operation">
                        <div class="th-line"><span class="m-header-line"></span></div>
                        <div style="left: 850px;" class="th-line"><span class="m-header-line"></span></div>
                        <span class="tlo-title tlo-title-nav tlo-title-nav-win"></span>
                        <div id="homework_card_tab_box">
                            <a class="tlo-back tlo-back-dis prevBtn" data-c_p_page="0" href="javascript:void(0);"></a>
                            <a class="tlo-next nextBtn" data-c_n_page="1" href="javascript:void(0);"></a>
                        </div>
                    </div>

                    <!--practice list-->
                    <div class="tl-practice-box" style="width: 90%;">
                        <div class="tlp-title">
                            <p id="countdownBox"> <#--倒计时--> </p>
                            全部完成还能获得PK套装！
                        </div>
                        <div class="tlp-title-info">
                            <span class="tf">每个任务完成条件：正确率达到80%以上</span>
                        </div>
                        <div class="tlp-getPractice-box">
                            <ul id="mission_homework_list_box" class="tlp-ul" style="position: relative;">
                                <#list 1..15 as missionList> <#-- 固定的15个任务包 -->
                                    <#if missionList lte finishedDay>
                                        <li class="finished">
                                            <i class="succeed"></i>
                                            <span class="icon-holiday icon-holiday-1 holiday-con">
                                                <em>${missionList}</em>
                                            </span>
                                            <p class="lp">已获得</p>
                                        </li>
                                    <#else >
                                        <#if missionList - finishedDay == 1 >
                                            <#if showStart >
                                                <li>
                                                    <i class="succeed"></i>
                                                    <span class="icon-holiday icon-holiday-2 holiday-con">
                                                        <em>${missionList}</em>
                                                    </span>
                                                    <p class="lp">
                                                        <a class="icon-holiday btn-h-start" href="/student/wintermission/startMission.vpage">开始</a>
                                                    </p>
                                                </li>
                                            <#else >
                                                <li>
                                                    <i class="succeed"></i>
                                                    <span class="icon-holiday icon-holiday-3 holiday-con">
                                                        <em>${missionList}</em>
                                                    </span>
                                                    <p class="lp"><a class="icon-holiday btn-h-start btn-h-gray" href="javascript:void (0);">待开启</a></p>
                                                </li>
                                            </#if>
                                        <#else >
                                            <li>
                                                <i class="succeed"></i>
                                                    <span class="icon-holiday icon-holiday-3 holiday-con">
                                                        <em>${missionList}</em>
                                                    </span>
                                                <p class="lp"><a class="icon-holiday btn-h-start btn-h-gray" href="javascript:void (0);">待开启</a></p>
                                            </li>
                                        </#if>
                                    </#if>
                                </#list>
                            </ul>
                        </div>
                        <div id="showCardTabBox" class="tl-practice-tab"></div>
                    </div>
                </div>
            </div>

        </div>
    </div>


    <script type="text/javascript">
        $(function(){
            var homeworkDetail = new $17.Model({
                cardTotal : $("#mission_homework_list_box li").length,
                finishedCardTotal : $("#mission_homework_list_box li.finished").length,
                homeworkListBox : $("#mission_homework_list_box"),
                cardBoxMaxWidth : '',
                cardWidth : 154 ,//单个卡片宽度
                showCardNum : 6 , //显示卡片数
                homeworkCardTabBox : $("#homework_card_tab_box"),
                totalPage : 1
            });

            homeworkDetail.extend({
                updateBigTabClass : function(currentPage){
                    if(homeworkDetail.totalPage == currentPage){
                        $("a.nextBtn").addClass('tlo-next-dis');
                        $("a.prevBtn").removeClass('tlo-back-dis');
                    }else if(currentPage > 1){
                        $("a.prevBtn").removeClass('tlo-back-dis');
                        $("a.nextBtn").removeClass('tlo-next-dis');
                    }else if(currentPage - 1 == 0){
                        $("a.prevBtn").addClass('tlo-back-dis');
                        $("a.nextBtn").removeClass('tlo-next-dis');
                    }else if(currentPage - 1 < homeworkDetail.totalPage){
                        $("a.nextBtn").removeClass('tlo-next-dis');
                    }

                    homeworkDetail.homeworkListBox.css({left : -homeworkDetail.cardWidth*homeworkDetail.showCardNum*(currentPage-1) +"px"});

                    $('#mission_homework_list_box').attr('data-left', -homeworkDetail.cardWidth*homeworkDetail.showCardNum*(currentPage-1));

                    //更新小标签选中的状态
                    homeworkDetail.updateSmallTabClass(currentPage);

                    //还原卡片状态
                    $("#mission_homework_list_box li").removeClass('pv_active').show();
                },
                updateSmallTabClass : function(pageNum){
                    $("#showCardTabBox span[data-s_tab_index="+pageNum+"]").addClass('current').siblings().removeClass('current');
                },
                init : function(){

                    /*根据卡片数量 生成相应数量的小标签*/
                    var html = '';
                    for(var i = 0; i < Math.ceil(homeworkDetail.cardTotal/homeworkDetail.showCardNum) ; i++){
                        var currentClass = (i == 0) ? 'current' : '';
                        html += '<span data-s_tab_index='+(i+1)+' class='+currentClass+'></span>';
                    }
                    $('#showCardTabBox').html(html);

                    //根据卡片数量 生成相应页面宽度
                    cardBoxMaxWidth = homeworkDetail.cardWidth * homeworkDetail.cardTotal;
                    homeworkDetail.homeworkListBox.css({width : cardBoxMaxWidth + "px"});

                    //获取总页数
                    homeworkDetail.totalPage = Math.ceil(homeworkDetail.cardTotal/homeworkDetail.showCardNum);

                    //当卡片总数小于等于6时，小标签隐藏
                    if(homeworkDetail.cardTotal <= homeworkDetail.showCardNum){
                        $('#showCardTabBox').hide();
                        $("#homework_card_tab_box").hide();
                    }

                    /*小标签切换卡片*/
                    $("#showCardTabBox span").on('click', function(){
                        var $this = $(this);
                        $this.addClass('current').siblings().removeClass('current');
                        var currentSmallTabIndex = $this.data('s_tab_index');
                        homeworkDetail.updateBigTabClass(currentSmallTabIndex);

                        //更换大标签的值
                        $("a.prevBtn").attr("data-c_p_page",currentSmallTabIndex - 1);
                        $("a.nextBtn").attr("data-c_n_page",currentSmallTabIndex);
                    });


                    /*大标签切换卡片*/
                    $("a.prevBtn").on('click', function(){
                        var $this = $(this);
                        if($this.hasClass('tlo-back-dis')){return false}
                        var cPage = $this.attr('data-c_p_page')* 1 - 1;
                        $this.attr('data-c_p_page',cPage);
                        $("a.nextBtn").attr("data-c_n_page",$("a.nextBtn").attr("data-c_n_page") * 1 - 1);
                        homeworkDetail.updateBigTabClass(cPage + 1);
                    });

                    $("a.nextBtn").on('click', function(){
                        var $this = $(this);
                        if($this.hasClass('tlo-next-dis')){return false}
                        var cPage = $this.attr('data-c_n_page')* 1 + 1;
                        $this.attr('data-c_n_page',cPage);
                        $("a.prevBtn").attr("data-c_p_page",$("a.prevBtn").attr("data-c_p_page") * 1 + 1);
                        homeworkDetail.updateBigTabClass(cPage);
                    });

                    //根据完成进度，自动切换到下一页
                    if((homeworkDetail.finishedCardTotal)%homeworkDetail.showCardNum == 0){
                        for(var i = 0;i < homeworkDetail.finishedCardTotal/homeworkDetail.showCardNum; i++){
                            $("a.nextBtn").click();
                        }
                    }
                }

            }).init();

            //倒计时
            $('#countdownBox').countdown( "2015/3/10 23:59:59", function(event) {
                $(this).html(event.strftime(
                    '距离结束还有: <span>%D</span>天 '
                    + '<span>%H</span>小时 '
                    + '<span>%M</span>分 '
                    + '<span>%S</span>秒')
                );
            });
        });
    </script>
</@temp.page>