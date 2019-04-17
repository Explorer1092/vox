<#import "../../help/servicemodule.ftl" as com>
<@com.page title="关于一起作业">
    <div class="main">
        <!--//关于一起作业-->
        <div class="section">
            <h2 class="blue"><i class="d_icon d_icon_6"></i>关于一起作业</h2>
            <div class="article ">
                <div class="videoSwitch">
                    <div class="nav sel">一起作业能做什么？</div>
                    <div class="nav vn1">如何使用一起作业？</div>
                    <div class="nav vn2">如何使用一起作业？</div>
                    <div class="str">
                        <p id="player1"><embed width="453" height="362" flashvars="file=http://cdn.17zuoye.com/static/project/video/speciallist/1_v.flv&amp;image=http://cdn.17zuoye.com/static/project/video/speciallist/1_v_1.jpg?&amp;width=453&amp;height=362" allowfullscreen="true" quality="high" name="single" id="single" style="undefined" src="http://cdn.17zuoye.com/static/project/video/flvplayer.swf" type="application/x-shockwave-flash"></p>
                    </div>
                </div>
            </div>
        </div>
        <!--//专家点评-->
        <div class="section">
            <h2 class="blue"><i class="d_icon d_icon_4"></i>专家点评</h2>
            <div class="article specialistBox">
                <ul class="video_list_special video_list"></ul>
            </div>
        </div>
        <!--//用户反馈-->
        <div class="section">
            <h2 class="blue"><i class="d_icon d_icon_5"></i>用户反馈</h2>
            <div class="article specialistBox">
                <ul class="video_list_user video_list"></ul>
            </div>
        </div>
        <!--//全国使用一起作业网的学校(部分)-->
        <a id="5"></a>
        <div class="section">
            <h2 class="blue"><i class="d_icon d_icon_3"></i>全国使用一起作业网的学校(部分)</h2>
            <div class="article" style="padding: 10px;">
                <div class="allSchool" id="allSchool">
                    <ul class="tab"></ul>
                    <div class="content"></div>
                </div>
                <div class="clear"></div>
            </div>
        </div>
    </div>
    <script type="text/javascript">
        $(function(){
            var myJson = <#include "datachip.ftl"/>;
            var allSchool = $("#allSchool");
            var tabHtml = "";
            var contentHtml = "";
            var videoList = $(".video_list");
            var videoDetail = $(".videoDetail");
            var jsonArray =  {
                "user": [
                    { "img":"userlist/1.jpg?1.0.2" , "url":"userlist/1.flv" , "time":"20130904" , "title":"教师评价 饶老师", "detail":"北京市昌平区南邵中心小学饶老师介绍使用一起作业网的感受" },
                    { "img":"userlist/2.jpg?1.0.2" , "url":"userlist/2.flv" , "time":"20130904" , "title":"学生评价 南邵中心小学", "detail":"北京市昌平区南邵中心小学学生使用一起作业网后的感受" },
                    { "img":"userlist/3.jpg?1.0.2" , "url":"userlist/3.flv" , "time":"20130904" , "title":"学校领导评价 张主任", "detail":"北京市昌平区南邵中心小学张主任介绍使用一起作业网的感受" },
                    { "img":"userlist/4.jpg?1.0.2" , "url":"userlist/4.flv" , "time":"20130904" , "title":"教研组长演讲 孙慧", "detail":"北京东城区黑芝麻胡同小学英语教研组长孙慧在年会上发表了如何利用一起作业网学习培养学生的英语学习素养" },
                    { "img":"userlist/5.jpg?1.0.2" , "url":"userlist/5.flv" , "time":"20130904" , "title":"教研员演讲 李静", "detail":"北京市海淀区教师进修学校小学英语教研员李静在年会上发表了基于信息技术的网络作业研究演讲" },
                    { "img":"userlist/6.jpg?1.0.2" , "url":"userlist/6.flv" , "time":"20130904" , "title":"教师演讲 梁丹", "detail":"辽宁省沈阳市实验学校教师梁丹在年会上发表了浅谈一起作业对英语教学的促进为主题的演讲" },
                    { "img":"userlist/7.jpg?1.0.2" , "url":"userlist/7.flv" , "time":"20130904" , "title":"教研组长评价 孙慧", "detail":"北京东城区黑芝麻胡同小学英语教研组长孙慧介绍使用一起作业网的感受" },
                    { "img":"userlist/8.jpg?1.0.2" , "url":"userlist/8.flv" , "time":"20130904" , "title":"教研员评价 李静", "detail":"北京市海淀区教师进修学校小学英语教研员李静介绍一起作业网对学生的帮助" },
                    { "img":"userlist/9.jpg?1.0.2" , "url":"userlist/9.flv" , "time":"20130904" , "title":"教师评价 梁丹", "detail":"辽宁省沈阳市实验学校教师梁丹讲述使用一起作业网的感受" },
                    { "img":"userlist/10.jpg?1.0.2" , "url":"userlist/10.flv" , "time":"20130904" , "title":"学生评价", "detail":"让学生一起快乐学习" }
                ],
                "special": [
                    { "img":"speciallist/1_v.jpg" , "url":"speciallist/1_v.flv" , "time":"20130904","type":"special" , "title":"全国小学英语教研员工作论坛", "detail":"2013年7月，全国小学英语教研员工作论坛在京隆重举行，共邀请到来自全国18个省的200余位教研员参与" },
                    { "img":"speciallist/2.jpg?1.0.2" , "url":"speciallist/2.flv" , "time":"20130904","type":"special" , "title":"教育专家点评 徐小平", "detail":"全国著名英语教育专家徐小平解读一起作业网" },
                    { "img":"speciallist/3.jpg?1.0.2" , "url":"speciallist/3.flv" , "time":"20130904","type":"special" , "title":"教育专家点评 龚亚夫", "detail":"中国教育学会外语专业委员会理事长龚亚夫发表本次参会感言" }
                ]
            };

            function myJsonMethod(index){
                for(var i = 0; i < myJson.length; i++){
                    tabHtml += "<li>" + myJson[i][0] + "</li>";
                    contentHtml += "<ul>";
                    for(var j = 0, jlen = myJson[i][1].length; j < jlen; j++){
                        var school = myJson[i][1];
                        contentHtml += "<li>"+ school[j] +"</li>";
                    }
                    contentHtml += "</ul>";
                }

                allSchool.find(".content").html( contentHtml );
                allSchool.find(".tab").html(tabHtml);

                myJsonSet(index);
            }

            function myJsonSet(index){
                allSchool.find(".content ul").hide().eq(index).show();
                allSchool.find(".tab li").removeClass("active").eq(index).addClass("active");
            }

            myJsonMethod(0);

            allSchool.find(".tab li").each(function(index){
                $(this).on('click', function(){
                    myJsonSet(index);
                });
            });

            allSchool.find(".tab li").hover( function(){
                $(this).addClass("over");
            },function(){
                $(this).removeClass("over");
            });

            $(".superiorityBox .t_1 a").die().on("click",function(){
                var url = $(this).attr("url");
                url += (url.indexOf("?") != -1 ? "&" : "?") + (new Date()).getTime();
                var data = '<iframe class="vox17zuoyeIframe" src="'+url+'" width="700" marginwidth="0" height="490" marginheight="0" scrolling="no" frameborder="0"></iframe>'
                <@ftlmacro.flashWind data="data" title="做作业!" />
                return false;
            });

            $("#download_client_url_but").click(function(){
                window.open("<@app.client_setup_url />");
            });

            $(".videoSwitch .nav").on('click', function(){
                var $this = $(this);
                if( $this.prevAll().length == 0){
                    $(".videoSwitch .str").html("<embed width='453' height='362' flashvars='file=http://cdn.17zuoye.com/static/project/video/speciallist/1_v.flv&amp;image=http://cdn.17zuoye.com/static/project/video/speciallist/1_v_1.jpg&amp;width=453&amp;height=362' allowfullscreen='true' quality='high' name='single' id='single' style='undefined' src='http://cdn.17zuoye.com/static/project/video/flvplayer.swf' type='application/x-shockwave-flash'>");
                }else if($this.prevAll().length == 1){
                    $(".videoSwitch .str").html("<embed width='453' height='362' flashvars='file=http://cdn.17zuoye.com/static/video/video1.flv&amp;image=http://cdn.17zuoye.com/static/project/video/s1.png&amp;width=453&amp;height=362' allowfullscreen='true' quality='high' name='single' id='single' style='undefined' src='http://cdn.17zuoye.com/static/project/video/flvplayer.swf' type='application/x-shockwave-flash'>");
                }else{
                    $(".videoSwitch .str").html("<embed width='453' height='362' flashvars='file=http://cdn.17zuoye.com/static/video/video2.flv&amp;image=http://cdn.17zuoye.com/static/project/video/s2.png&amp;width=453&amp;height=362' allowfullscreen='true' quality='high' name='single' id='single' style='undefined' src='http://cdn.17zuoye.com/static/project/video/flvplayer.swf' type='application/x-shockwave-flash'>");
                }
                $this.addClass("sel").siblings().removeClass("sel");
            });

            videoList.on("click", "li span", function(){
                var $id = $(this).attr("idx");
                var $index = $(this).attr("str");
                var array;
                if( $id == "special" ){
                    array = jsonArray.special;
                }else{
                    array = jsonArray.user;
                }
                var data = '<embed width="600" height="386" type="application/x-shockwave-flash"' +
                        'src="/public/skin/project/about/images/flvplayer.swf"' +
                        'style="undefined" id="single" name="single" quality="high" allowfullscreen="true"' +
                        'flashvars="file=http://cdn.17zuoye.com/static/project/video/'+ array[$index].url +'&amp;' +
                        'image=http://cdn.17zuoye.com/static/project/video/'+ array[$index].img +
                        '&amp;width=600&amp;height=386">';
                <@ftlmacro.flashWind data="data" title="视频" wsize=620 />
            });

            function videoType(array, id){
                for(var i = 0, len = array.length; i < len; i++){
                    $(id).append("<li><span idx="+ array[i].type +" str="+ i +"><img src='http://cdn.17zuoye.com/static/project/video/"+ array[i].img +"'></span><p>" + array[i].title + "</p></li>")
                }
            }

            videoType(jsonArray.special, ".video_list_special");
            videoType(jsonArray.user, ".video_list_user");
        });
    </script>
</@com.page>