<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="else" showNav="show">
    <@sugar.capsule css=["teacher.flower"] />
<#assign fetchCertificationState = ((currentUser.fetchCertificationState())?? && currentUser.fetchCertificationState() == "SUCCESS")>
<div class="w-base t-allTeacherRange-box">
    <div class="w-base-title">
        <#if !((currentTeacherDetail.subjects?size gt 1)!false)>
            <h3>班级鲜花</h3>
        <#else>
            <h3>班级鲜花-${curSubjectText!((currentTeacherDetail.subject.value)!'')}</h3>
            <div class="w-base-ext">
                <#include "../block/switchsubjcet.ftl"/>
            </div>
        </#if>
    </div>
    <div class="w-base-switch w-base-two-switch">
        <ul id="rankBox">
            <li data-rank_type="parent">
                <a href="javascript:void(0);">
                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                    <span class="flower-icon flowerRange-icon"></span>
                    家长送花月榜
                </a>
            </li>
            <li data-rank_type="teacher">
                <a href="javascript:void(0);">
                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                    <span class="flower-icon allRange-icon"></span>
                    全校${(curSubjectText)!''}老师鲜花榜
                </a>
            </li>
        </ul>
    </div>
    <div class="t-sendFlower-box">
        <#if fetchCertificationState>
            <div id="clazzListBox" class="w-border-list t-homeworkClass-list">
                <ul>
                    <#if clazzList?? && clazzList?size gt 0>
                        <#list clazzList as clazzList>
                            <li data-cid="${clazzList.id!''}">${clazzList.classLevel!''}年级${clazzList.className!''}</li>
                        </#list>
                    </#if>
                </ul>
            </div>
        </#if>

        <div class="Sf-result-item" id="getRewardBox" style="display: none;" ></div>

        <div id="notFetchCertificationState" style="padding: 100px 0;text-align: center; display: none;">成为认证老师，即可使用该功能。</div>

        <div id="flowerListBox"></div>

    </div>
</div>


<div class="w-base">
    <div class="w-base-title">
        <h3>如何获得鲜花？</h3>
    </div>
    <div class="w-base-container">
        <div class="t-howGet-flower-box">
            <div class="hf-content">
                <p><span>■</span>学生完成作业后，家长可在微信或APP上给老师送鲜花</p>
                <p><span>■</span>督促学生完成作业</p>
                <p><span>■</span>邀请家长参与学生教育</p>
            </div>
        </div>
    </div>
</div>


<script type="text/html" id="rewardTemplateBox">
    <div class="info" style="text-align: center; font-size: 16px; padding: 10px 0;">
        家长赠送的鲜花可以兑换班级学豆
        <a href="/teacher/flower/exchange.vpage?ref=flower" class="w-btn w-btn-orange w-btn-mini" target="_blank">立即兑换</a>

        <%if(data.lastMonthRank != -1){%>
            &nbsp;&nbsp;&nbsp;上月排名：<span class="w-orange"><%=data.lastMonthRank%></span>
        <%}%>
    </div>
</script>

<script type="text/html" id="flowerListTemplateBox">
    <#if fetchCertificationState>
        <div class="w-table">
            <table>
                <thead>
                <tr>
                    <td>排名</td>
                    <td>姓名</td>
                    <td>
                        <%if(type == 'teacher'){%>
                            鲜花榜
                        <%}else{%>
                            献花榜
                        <%}%>
                    </td>
                </tr>
                </thead>
            </table>
        </div>
    <#else>
        <%if(type == 'teacher'){%>
            <div class="w-table">
                <table>
                    <thead>
                    <tr>
                        <td>排名</td>
                        <td>姓名</td>
                        <td>鲜花榜</td>
                    </tr>
                    </thead>
                </table>
            </div>
        <%}%>
    </#if>


    <div class="t-range-item-list" >
        <ul>
            <%if(data.rankList.length > 0){%>
            <%for(var i = 0; i < data.rankList.length; i++ ){ %>
            <li class="odd">
                <span class="num"><i class="flower-icon FGold-0<%=i+1%>"><%if(i > 2){%><%=i+1%><%}%></i></span>
                    <span>
                        <%if(data.rankList[i].senderName == ''){%>
                            <%=data.rankList[i].id%>
                        <%}else{%>
                            <%=data.rankList[i].senderName%>
                            <%if(type == 'parent'){%>
                                家长
                            <%}%>
                        <%}%>
                    </span>
                <span><%=data.rankList[i].count%></span>
            </li>
            <% } %>
            <%}else{%>
            <li>
                <%if(type == 'parent'){%>
                <div style="padding: 50px 0; text-align: center;">该班级还没有学生，<a href="/teacher/clazz/clazzsdetail.vpage?clazzId=<%=clazzId%>" style="color: #18B1FC;">快去邀请吧</a></div>
                <%}else{%>
                <div style="padding: 50px 0; text-align: center;">暂无数据</div>
                <%}%>
            </li>
            <%}%>
        </ul>
    </div>
</script>


<script type="text/javascript">
    $(function(){
        LeftMenu.changeMenu();
        LeftMenu.focus("authentication");

        var clazzListBox = $('#clazzListBox'),getRewardBox = $('#getRewardBox'),flowerListBox = $('#flowerListBox');

        //选择班级
        var flowerRankListCache = {};
        $('#clazzListBox ul li').on('click', function(){
            var $this = $(this);
            $this.addClass('current').siblings().removeClass('current');
            var clazzId = $this.data('cid');

            if(flowerRankListCache[clazzId] != undefined){
                flowerListBox.empty().html(template("flowerListTemplateBox",{data : flowerRankListCache[clazzId][0],clazzId : clazzId,type : 'parent'}));
                return;
            }

            $.get('/teacher/flower/flowerrankbyparent.vpage?clazzId='+clazzId+"&subject=${curSubject!}",function(data){
                if(data.success){
                    flowerListBox.empty().html(template("flowerListTemplateBox",{data : data,clazzId : clazzId,type : 'parent'}));
                    flowerRankListCache[clazzId] = [].concat(data);
                }else{
                    flowerListBox.empty().html('暂无数据');
                }
            });
        });

        //选择榜
        $('#rankBox li').on('click', function(){
            var $this = $(this);
            $this.addClass('active').siblings().removeClass('active');
            var rank_type = $this.data('rank_type');
            if(rank_type == 'parent'){
                <#if fetchCertificationState>
                    $('#clazzListBox ul li:eq(0)').click();
                    flowerListBox.show();
                <#else>
                    flowerListBox.hide();
                    $('#notFetchCertificationState').show();
                </#if>
                clazzListBox.show();
                getRewardBox.hide();
            }else{
                clazzListBox.hide();
                getRewardBox.show();
                $.get('/teacher/flower/flowerrankbyteacher.vpage?subject=${curSubject!}',function(data){
                    if(data.success){
                        $('#getRewardBox').html(template("rewardTemplateBox",{data : data}));
                        flowerListBox.empty().html(template("flowerListTemplateBox",{data : data,type : 'teacher'})).show();
                        $('#notFetchCertificationState').hide();
                    }else{
                        flowerListBox.empty().html('暂无数据');
                    }
                });
            }
        });

        <#if fetchCertificationState>
            $('#rankBox li').eq(0).click();
        <#else>
            $('#rankBox li').eq(1).click();
        </#if>

        //领取
        $(document).on('click','#getRewardBut', function(){
            $.prompt('<div class="t-flower-swap-pop"> <div class="swap" id="weixinCode"></div> </div>', {
                title: "领取园丁豆",
                buttons: {"知道了": true},
                position : { width: 550},
                loaded : function(){
                    $17.getQRCodeImgUrl({
                        role : "teacher"
                    }, function (url) {
                        $("#weixinCode").html('<img src='+url+' alt="二维码"/>');
                    });
                }
            });

            $17.voxLog({module: 'flower',op:'get_integral_button_click'});
        });
    });
</script>
</@shell.page>