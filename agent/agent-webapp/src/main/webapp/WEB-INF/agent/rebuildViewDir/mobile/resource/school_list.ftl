<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="学校列表" pageJs="common" footerIndex=2 navBar="show">
<@sugar.capsule css=['res',"home"]/>
<div class="crmList-box">
    <div class="fixed-head" style="position:absolute!important;">
        <div class="c-head">
            <a class="the">学校列表</a>
            <a href="../teacher/list.vpage">全部老师</a>
        </div>
        <!--查询输入框-->
        <div class="search">
            <input class="s-input" placeholder="请输入学校名称 / ID" <#if key??>value="${key!}"</#if>/>
            <span class="js-search school-search s-go">搜索</span>
        </div>
        <#--专员-->
        <#if requestContext.getCurrentUser().isBusinessDeveloper()><#--专员-->
            <#if schoolLevelList?? && schoolLevelList?size gt 1>
                <div class="tab-head c-opts c-flex js-f1" style="display: -webkit-box;display: -moz-box;">
                    <#list schoolLevelList as list>
                        <span class="js-sort" data-key="potentialDasc" data-name="${list!""}" style="-webkit-box-flex:1;-moz-box-flex:1;float: none;display:block;text-align: center" data-status="1" id="dDigTab">
                            <#if list??><#if list == "JUNIOR">小学<#elseif list == "MIDDLE">中学<#elseif list == "HIGH">高中<#elseif list == "INFANT">学前</#if></#if>
                        </span>
                    </#list>
                </div>
            </#if>
        </#if>

        <!--排序&筛选选项-->
        <#if requestContext.getCurrentUser().isCityManager()><#--市经理-->
            <div class="tab-head">
                <div class="f1 js-f1">
                    <div class="users tab-head">
                    <#--<a class="the" href="javascript:void(0);" data-sid="-1" data-status="0">不限</a>-->
                        <#if managedUsers?? && managedUsers?has_content>
                            <#list managedUsers as user>
                                <a href="?selectedUser=${user.uid!0}" data-sid="${user.uid!0}" class="<#if selectedUser?? && selectedUser==(user.uid!0)>the</#if>">${user.uname!''}</a>
                            </#list>
                        </#if>
                    </div>
                </div>
                <#if schoolLevelList?? &&schoolLevelList?size gt 1>
                    <div class="tab-head c-opts  c-flex js-f1" style="display: -webkit-box;display: -moz-box;">
                        <#list schoolLevelList as list>
                            <span class="js-sort" data-key="potentialDasc" data-name="${list!""}" style="-webkit-box-flex:1;-moz-box-flex:1;float: none;display:block;text-align: center" data-status="1" id="dDigTab">
                                <#if list??><#if list == "JUNIOR">小学<#elseif list == "MIDDLE">中学<#elseif list == "HIGH">高中<#elseif list == "INFANT">学前</#if></#if>
                            </span>
                        </#list>
                    </div>
                </#if>
            </div>
        </#if>
    </div>
    <div class="c-main">
        <div>
        <@apptag.pageElement elementCode="b96c2a17ed244bf8">
            <#if dataMap?? && dataMap?size gt 0>
            <#list dataMap?keys as key >
                <#assign dataKey = dataMap[key]>
                    <div class="vacation_list school_${key!0}" hidden>
                        <ul style="background:#f1f2f5">
                            <#list dataKey as data>
                                <li id="detail${data.schoolId!}" class="js-school <#if data.isDictSchool?? && !data.isDictSchool> gray</#if>" data-sid="${data.schoolId!}" data-user="${data.bdId!0}" style="background: #fff;">
                                    <div class="student_name" style="font-size:.7rem;overflow:hidden;width:100%;height:1rem;text-overflow:ellipsis;white-space: nowrap;">
                                        <div class="icon_info"><#if data.isNew?? && data.isNew><i class="icon-new"></i></#if></div>
                                        <span style="font-size:.75rem">${data.schoolName!}</span><span style="font-size:.6rem">（${data.schoolId!}）</span>
                                    </div>
                                    <div class="side">
                                        <span><#if data.schoolLevel??><#if data.schoolLevel == "JUNIOR"><i class="icon-junior"></i><#elseif data.schoolLevel == "MIDDLE"><i class="icon-middle"></i><#elseif data.schoolLevel == "HIGH"><i class="icon-high"></i><#elseif data.schoolLevel == "INFANT"><i class="icon-infant"></i></#if></#if></span>
                                        <span class="font_red"><#if data.schoolPopularityType??><i class="icon-${data.schoolPopularityType!""}"></i></#if></span>
                                        <span class="font_red"><#if data.permeabilityType??><span><i class="icon-${data.permeabilityType!""}"></i></span></#if></span>
                                        <#--<span class="font_red">四高</span>-->
                                        <span class="font_green"><#if data.authState?? && data.authState == "SUCCESS"><#else><i class="icon-unjian"></i></#if></span>
                                        <#if data.schoolLevel == "MIDDLE" || data.schoolLevel == "HIGH"><span class="font_green"><#if data.scannerFlag?? && !data.scannerFlag><i class="icon-unyiqi"></i></#if></span></#if>
                                        <#if data.competitiveProductFlag?? && data.competitiveProductFlag gt 1><i class="icon-competitiveProductFlag"></i></#if>
                                        <div class="icon_item" style="font-size:.6rem"><#if data.isDictSchool?? && data.isDictSchool><#if data.hasBd?? && data.hasBd>${data.bdName!''}<#else>市经理未分配</#if><#else>未加入字典表</#if></div>
                                    </div>
                                </li>
                            </#list>
                        </ul>
                    </div>
            </#list>
        </#if>
        </@apptag.pageElement>
        </div>
        <!--学校-->
        <div>
                <div id="schoolList"></div>
        <#if error??>
            <p class="error-tip" style="text-align:center;color:red;line-height:2em;font-size:0.75rem;padding:0 2rem;word-break: break-all;">${error!''}</p>
        </#if>
        </div>
    </div>
</div>
<div class="mask">

</div>
<#--学校列表-->

<script type="text/html" id="T:学校列表">
    <div class="vacation_list <#--school_${key!0}-->">
        <ul>
            <%for(var i = 0; i < data.length; ++i){%>
                <li id="detail<%= data[i].schoolId%>" class="js-school  <%if(!data[i].isDictSchool){%>gray<%}%>" data-sid="<%= data[i].schoolId%>" data-user="<%= data[i].bdId%>">
                    <div class="student_name" style="font-size:.7rem">
                        <div class="icon_info"><%if(data[i].isNew){%><i class="icon-new"></i><%}%></div>
                        <%= data[i].schoolName%>（<%= data[i].schoolId%>）
                    </div>
                    <div class="side">
                        <span><%if(data[i].schoolLevel == "JUNIOR"){%><i class="icon-junior"></i><%}%><%if(data[i].schoolLevel == "MIDDLE"){%><i class="icon-middle"></i><%}%><%if(data[i].schoolLevel == "HIGH"){%><i class="icon-high"></i><%}%><%if(data[i].schoolLevel == "INFANT"){%><i class="icon-infant"></i><%}%></span>
                        <%if(data[i].schoolPopularityType){%><span class="font_red"><i class="icon-<%= data[i].schoolPopularityType%>"></i></span><%}%>
                        <%if(data[i].permeabilityType){%><span class="font_red"><i class="icon-<%= data[i].permeabilityType%>"></i></span><%}%>
                    <#--<span class="font_red">四高</span>-->
                        <span class="font_green"><%if(data[i].authState == "SUCCESS"){%><%}else{%><i class="icon-unjian"></i><%}%></span>
                        <%if(data[i].schoolLevel == "MIDDLE" || data[i].schoolLevel == "HIGH"){%><span class="font_green"><%if(data[i].scannerFlag){%><%}else{%><i class="icon-unyiqi"></i><%}%></span><%}%>
                        <%if(data[i].competitiveProductFlag && data[i].competitiveProductFlag > 1 ){%> <i class="icon-competitiveProductFlag"></i> <%}%>
                        <div class="icon_item">
                            <%if(data[i].isDictSchool){%>
                                <%if(data[i].hasBd){%>
                                    <%= data[i].bdName%>
                                <%}else{%>市经理未分配<%}%>
                            <%}else{%>未加入字典表<%}%>
                        </div>
                    </div>
                </li>
            <%}%>
        </ul>
    </div>
</script>
<script>
    $(document).ready(function () {
        //隐藏顶部title
        try{
            var setTopBar = {
                show:false
            };
            setTopBarFn(setTopBar);
        }catch(e){

        }
    });
    $('.vacation_list').eq(0).show();
    $('.js-sort').eq(0).addClass("the");
    var AT = new agentTool();
    //动态专员列表容器宽度
    var w= 0,innerDiv=$(".f1>div");
    innerDiv.children().each(function(){
        w+=$(this).outerWidth(true);
    });
    innerDiv.width(w+5);

    var schoolList=$("#schoolList");

    //学校卡片可点击
    $(document).on("click",".js-school",function(){
        var sid = $(this).data('sid');
        var userId = ${requestContext.getCurrentUser().getUserId()!0};
        $.post('/mobile/resource/school/school_detail_authority_message.vpage',{
            userId:userId,
            schoolId:sid,
            scene:3
        },function (res) {
            if(res.success){
                AT.setCookie("currentSid",sid);
                openSecond("/mobile/resource/school/card.vpage?schoolId="+sid);
            }else{
                AT.alert(res.info);
            }
        });
    });

    $(".tab-head").children("a,span").on("click",function(){
        var $this=$(this);
        $('#schoolList').hide();
        var dataName = $(this).data().name;
        $this.addClass("the").siblings().removeClass("the");
        $('.school_'+dataName).show().siblings().hide();
    });
    $(document).on('click','.js-sort',function(){
        var setKey = $(this).index();
        AT.setCookie("dataKey",setKey);
    });

    /*--搜索功能--*/
    $(".school-search").on("click",function(){
        $('.vacation_list').hide();
        $('.school_hide').show();
        $('#schoolList').show();
        schoolList.html("<img style='display:block;padding:2rem 0;margin:0 auto;width:6rem;' src='/public/rebuildRes/image/mobile/res/loading.gif' />");
        var $this=$(this);
        if($this.prev().val() != ""){
            $.post("search.vpage",{schoolKey:$this.prev().val(), scene:2},function(res){
                if(res.success){
                    var data={
                        data:res.schoolList.map(function(obj){
                            var tmp=obj;
                            return tmp;
                        })
                    };
                    schoolList.html(template("T:学校列表",data));
                }else{AT.alert(res.info);}
            });
        }else{
            AT.alert("请输入学校Id")
        }

    });
    var currentSid = AT.getCookie("currentSid");
    if(currentSid){
        var detailSchool = $("#detail"+currentSid);
        if(detailSchool.length!=0){
            var scroll_offset = $("#detail"+currentSid).offset();

            $("body,html").animate({
                scrollTop:parseFloat(scroll_offset.top) - 184 // 减掉被顶部和筛选条遮挡的部分
            },0);

        }
    }
    var getKey = AT.getCookie("dataKey");
    if(getKey){
        $(".js-sort").each(function(){
           if($(this).index() == getKey){
               $(this).click();
           }
        });
    }
</script>
</@layout.page>
