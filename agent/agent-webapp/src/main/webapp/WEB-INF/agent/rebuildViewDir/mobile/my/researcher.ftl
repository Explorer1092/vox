<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="教研员资源" pageJs="newResearch" footerIndex=4>
    <@sugar.capsule css=['researchers']/>
    <#if (researchers.id)?has_content>
        <#assign title = "教研员信息编辑">
    <#else>
        <#assign title = "添加教研员">
    </#if>
<div role="main">
        <div class="res-preserve js-subBtn" style="display:none;">提交</div>
    <div class="flow researcher-main">
        <div class="item">
            姓名
            <span class="inner-right bgNon">
                <input type="text" maxlength="8" name="name" id="name" class="js-need txt" placeholder="请填写  不超过8个字" value="${researchers.name!}">
            </span>
        </div>
        <div class="item GPS">
            性别
            <#if gender?? && gender?size gt 0>
                <div class="res-right js-singleSelect js-gender">
                <#list gender as g>
                    <div class="res-gender <#if researchers.gender?? && (researchers.gender) == (g.key)?number>the</#if>" data-index="${g.key!0}">${g.value!''}</div>
                </#list>
                </div>
            </#if>
            </div>

        <div class="item">
            电话
            <span class="inner-right bgNon">
                <input type="text" maxlength="11" name="phone" class="js-need txt" id="phone" placeholder="请填写" value="${researchers.phone!}">
            </span>
        </div>
        <div class="item GPS">
            职务
            <#if job?? && job?size gt 0>
            <div class="res-right js-singleSelect js-job">
                <#list job as j>
                    <div class="res-gender <#if researchers.job?? && (researchers.job) == (j.key)?number>the</#if>" data-index="${j.key!0}">${j.value!''}</div>
                </#list>
            </div>
            </#if>
        </div>
    <div class="flow researcher-content js-resNeed" <#if researchers.job?? && (researchers.job) == 1><#else>style="display: none;" </#if>>
        <div class="item">
            管辖区域
                <span class="inner-right js-cityAndRegion"><#if cityRegion??>${cityRegion!}<#else>请选择</#if></span>
        </div>
        <div class="item">
            年级
                <span class="inner-right js-grade"><#if grade??>${grade!}<#else>请选择</#if></span>
        </div>
        <div class="item GPS" style="background: #fff;height: 5rem;">
            学科
            <div class="subjectBox js-singleSelect js-subject" style="width:80%;">
                <#if subject?? && subject?size gt 0>
                    <#list subject as s>
                        <div class="btn-stroke <#if subjectId?? &&(subjectId)==(s.key)?number>the</#if>" data-index="${s.key!0}">${s.value!''}</div>
                    </#list>
                </#if>
            </div>
        </div>
    </div>
    </div>
</div>
<script>
    var AT = new agentTool();
    $(document).on("ready",function(){

        //检测
        var checkPostData = function(){
            var phoneNum = $("#phone").val();
            var name = $("#name").val();
            var flag = true;

            if(AT.isBlank(name)){
                AT.alert("请填写姓名");
                return false;
            }
            if(!AT.isMobile(phoneNum)){
                AT.alert("请输入正确的手机号");
                return false;
            }

            var selectList = [
                {sel:".js-gender>.the",info:"请选择性别"},
                {sel:".js-job>.the",info:"请选择职务"}
            ];

            var jobNode = $(".js-job>.the");
            if(jobNode.length != 0){
                if($(jobNode[0]).data("index") == 1){
                    selectList.push({sel:".js-subject>.the",info:"请选择学科"})
                }
            }

            $.each(selectList,function(i,item){
                if($(item.sel).length == 0){
                    AT.alert(item.info);
                    flag = false;
                }
            });

            return flag;
        };

        var saveAndJump = function(url){
            var postData = {
                name:$("#name").val(),
                gender:$($(".js-gender>.the")[0]).data("index"),
                phone:$("#phone").val(),
                job:$($(".js-job>.the")[0]).data("index"),
                subject:$($(".js-subject>.the")[0]).data("index")
            };
            <#if (researchers.id)?has_content>
                postData["id"] = ${researchers.id!0};
            </#if>
            $.post("save_researchers_session.vpage",postData,function(res){
                if(res.success){
                    location.href = url;
                }else{
                    AT.alert(res.info);
                }
            });
        };

        //获取提交数据
        var getPostData = function () {
            var data = {},
                inputList = ["name","phone"],
                selectList = ["gender","job","subject"];
            $.each(inputList,function(i,item){
                data[item] = $("#"+item).val()
            });

            $.each(selectList,function(i,item){
                var selector = ".js-"+item+">.the";
                data[item] = $(selector).data("index");
            });
            <#if (researchers.id)?has_content>
                data["id"] = ${researchers.id!0};
            </#if>
            return data;

        };

        /*提交*/
        $(document).on("click",".js-subBtn",function(){
            if(checkPostData()){
                var postData = getPostData();
                var url = '/mobile/researchers/load_researchers_list.vpage';
                console.log(postData);
                $.post("upsert_researchers.vpage",postData,function(res){
                    if(res.success){
                        AT.alert('提交成功');
                        setTimeout('disMissViewCallBack()',1500);
                    }else{
                        AT.alert(res.info);
                    }
                });
            }
        });

        //城市和区域
        $(document).on("click",".js-cityAndRegion",function(){
            var url = "city_region_choice.vpage";
            saveAndJump(url);
        });

        //选择年级
        $(document).on("click",".js-grade",function(){
            var url = "phase_grade_choice.vpage";
            saveAndJump(url);
        });

        /*教研员角色*/
        $(document).on("click",".js-job>div",function(){
            $(this).addClass("the").siblings("div").removeClass("the");
            if($(this).data("index") != 1){
                $(".js-resNeed").hide();
            }else{
                $(".js-resNeed").show();
            }
        });


        /*单选*/
        $(document).on("click",".js-singleSelect>div",function(){
            $(this).addClass("the").siblings("div").removeClass("the");
        })

    });

</script>
</@layout.page>