<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout >
<@layout.page title="教研员拜访" pageJs="researchVisit" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['researchers']/>
<div style="display:none;">
    <div class="res-preserve subBtn" id="subBtn">保存</div>
</div>
<div class="visitResearchers-box">
    <div class="vir-head">
        <div class="time">${date?string('yyyy-MM-dd')!}</div>
    </div>
    <ul class="vir-list">
        <li>
            <div class="right selRight" id="selectVisitBtn"><#if researcherName?has_content>${researcherName!""}<#else>请选择</#if><i class="arrowRight"></i></div>
            拜访人员
        </li>
        <li>
            拜访目的
            <div class="c-main clearfix" id="visit_pur" style="padding-bottom: 0.25rem;">
                <div class="btn-stroke fix-padding" data-type="1">初次接洽</div>
                <div class="btn-stroke fix-padding" data-type="2">客情维护</div>
                <div class="btn-stroke fix-padding" data-type="3">促进组会</div>
                <div class="btn-stroke fix-padding" data-type="4">寻求介绍</div>
            </div>
        </li>
        <li>
            <div class="right"><input type="text" id="place" name="place" maxlength="15" class="js-need txt" style="width: 9rem;" data-einfo="请选择地点" placeholder="请填写" value="<#if visitedPlace?has_content>${visitedPlace!""}</#if>"></div>
            地点
        </li>
    </ul>
</div>
<div class="vir-content">
    <div class="vir-title"><i class="titleIco ico01"></i>拜访过程</div>
    <div class="text">
        <textarea name="flow" id="flow" maxlength="100" class="js-need" data-einfo="请填写拜访过程" placeholder="请点击填写，限100字" ><#if visitedFlow?has_content>${visitedFlow!""}</#if></textarea>
    </div>
</div>
<div class="vir-content">
    <div class="vir-title"><i class="titleIco ico02"></i>达成结果</div>
    <div class="text">
        <textarea name="conclusion" id="conclusion" maxlength="100" class="js-need" data-einfo="请填写达成结果" placeholder="请点击填写，限100字"><#if visitedConclusion?has_content>${visitedConclusion!""}</#if></textarea>
    </div>
</div>
<script>
    var AT = new agentTool();
    $(document).on("ready",function(){

        //保存数据跳转
        var saveAndJump = function(url){
            var postData = {},
                theNode = $("#visit_pur>div.the"),
                valList = ["place","flow","conclusion"];
            postData["intention"] = $(theNode[0]).data("type");
            $.each(valList,function(i,item){
                postData[item] = $("#"+item).val().trim("");
            });
            reloadCallBack();
            $.post("save_record_session.vpage",postData,function(res){
                if(res.success){
                    openSecond(url);
                }else{
                    AT.alert(res.info);
                }
            });
        };

        $(document).on("click","#selectVisitBtn",function(){
            saveAndJump("/mobile/researchers/researchers_list.vpage");
        });

        /*拜访目的*/
        $(document).on("click","#visit_pur>div",function(){
            var $this = $(this);
            $this.addClass("the").siblings("div").removeClass("the");
        });

        //提交
        $(document).on("click",".subBtn",function(){
            var _this = $(this);
            _this.removeClass('subBtn');
            var post = {},
                theNode = $("#visit_pur>div.the");

            post["researchersId"] = 1; //拜访人员页面出来后再从页面选取

            if(theNode.length !=0){
                post["intention"] = $(theNode[0]).data("type")
            }else{
                _this.addClass('subBtn');
                AT.alert("请选择拜访目的");
                return;
            }
            var flag = true;
            $.each($(".js-need"),function(i,item){
               if($(item).val()){
                    post[item.name] = $(item).val().trim("");
               }else{
                   AT.alert($(item).data("einfo"));
                   flag = false;
                   return false;
               }
            });
            if(!flag){
                _this.addClass('subBtn');
                return;
            }
            $.post("visited_researchers_record.vpage",post,function(res){
                if(res.success){
                    AT.alert("提交成功");
                    setTimeout("location.href = '/view/mobile/crm/visit/visit_detail.vpage'",2000);

                }else{
                    _this.addClass('subBtn');
                    AT.alert(res.info);
                }
            });
        });
    });
</script>
</@layout.page>