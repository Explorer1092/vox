<div class="aDetail-container">
    <div class="aDet-banner-stu"></div>
    <div class="aDet-main">
        <div class="aDet-list">
            <div class="title">期末复习作业时间</div>
            <div class="info time">
                <p class="textGreen">2016年5月30日-2016年6月30日</p>
                <p class="text">（视不同城市的考试时间而定）</p>
            </div>
        </div>
        <div class="aDet-list">
            <div class="title"><span class="js-afentiShowBox" style="display: none;">复习第一步，</span>完成老师布置的期末复习作业</div>
            <div class="info step01">
                <p class="text">1、完成老师布置的期末复习作业，可以获得额外的学豆奖励</p>
                <p class="text">2、每完成一份作业，额外获得2学豆</p>
            </div>
        </div>
        <#if (ftlmacro.devTestSwitch)!false>
            <#if (currentUser.userType == 3)!false>
                <#if ( !currentStudentDetail.inPaymentBlackListRegion && ![1, 2]?seq_contains(currentStudentDetail.getClazzLevelAsInteger()) )!false>
                    <div class="aDet-list js-afentiShowBox" style="display: none;">
                        <div class="title">复习第二步 根据错题记录，自主复习</div>
                        <div class="info step02" id="studentReturnMessageEnter" style="cursor: pointer;">
                            <a href="javascript:void(0);" class="textGreen js-clickVoxLog" data-op="activity_btn_studentApp">点击查看本学期错题记录，重做错题></a>
                        </div>
                    </div>
                </#if>
            </#if>
        </#if>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        var devTestSwitch = ${(ftlmacro.devTestSwitch)?string};
        var afentiUrl = "https://www.17zuoye.com/resources/apps/hwh5/afenti/V1_0_0/index.html";
        if(devTestSwitch){
            afentiUrl = "https://www.test.17zuoye.net/resources/apps/hwh5/afenti/V1_0_0/index.html";
        }

        var session_key = "";
        var native_version = "";

        if( window.external["getInitParams"] ){
            var $params = window.external.getInitParams();

            setTimeout(function(){
                if($params){
                    $params = $.parseJSON($params);
                    session_key = "?session_key=" + $params.session_key;
                    native_version = $params.native_version;

                    //App版本>=2.5.0时显示跳转按钮
                    var version = native_version.split('.'),
                            part1 = parseInt(version[0]),
                            part2 = parseInt(version[1]);
                    if(part1 > 2){$(".js_show").show();}
                    else if(part1==2 && part2 >= 5){$(".js_show").show();}
                }
            }, 100);
        }

        $(document).on("click", "#studentReturnMessageEnter", function(){
            if( window.external["openFairylandPage"] ){
                window.external.openFairylandPage( JSON.stringify({
                    url: afentiUrl + session_key,
                    name: "fairyland_app:afentiFinalreview",
                    initParams: JSON.stringify({hwPrimaryVersion: "V2_4_0"})
                }) );
            }else{
                alert("版本过低，建议升级APP。");
            }
        });
    });
</script>