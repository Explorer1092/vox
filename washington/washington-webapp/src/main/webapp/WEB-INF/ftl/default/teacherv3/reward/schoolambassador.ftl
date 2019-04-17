<#import "../../nuwa/teachershellv3.ftl" as temp />
<@temp.page showNav="hide">
    <@app.css href="public/skin/project/ambassador/apply/skin.css" />
<div class="apply">
    <div class="banner">
        <div class="banner-inner">
            <#--<#if (currentUser.fetchCertificationState() == "SUCCESS" && currentUser.level gte 2)!false>-->
            <#if (currentUser.fetchCertificationState() == "SUCCESS")!false>
                <a href="javascript:void(0);" class="v-clickCompetitionBtn" title="马上报名"></a>
            <#else>
                <a href="javascript:void(0);" class="v-clickNotSign" title="马上报名"></a>
            </#if>
        </div>
    </div>
    <div class="main">
        <div class="m-do">
            <div class="main-inner">
                <h2><i></i>您需要做什么</h2>
                <ul>
                    <li>
                        <div class="pic pic01"></div>
                        <h3>服务</h3>
                        <p>帮助并指导老师<br>使用网站功能</p>
                    </li>
                    <li>
                        <div class="pic pic02"></div>
                        <h3>宣传</h3>
                        <p>将一起作业<br>推广给身边老师</p>
                    </li>
                </ul>
            </div>
        </div>
        <div class="m-get">
            <div class="main-inner">
                <h2><i></i>您将得到什么？</h2>
                <ul>
                    <li>
                        <div class="pic pic01"></div>
                        <h3>成长 </h3>
                        <p>定期专家讲座<br>专业定制课程</p>
                    </li>
                    <li>
                        <div class="pic pic02"></div>
                        <h3>机会</h3>
                        <p>专业定制课程<br>个人事迹宣传<br>教育部重点课程基地</p>
                    </li>
                    <li>
                        <div class="pic pic03"></div>
                        <h3>人脉</h3>
                        <p>结交全国优秀教师</p>
                    </li>
                    <li>
                        <div class="pic pic04"></div>
                        <h3>特权</h3>
                        <p>专属VIP客服<br>专属礼物兑换</p>
                    </li>
                </ul>
            </div>
        </div>
        <div class="m-condition">
            <div class="main-inner">
                <h2><i></i>申请条件</h2>
                <ul>
                    <li><i></i>认证老师 （<#if (currentUser.fetchCertificationState() == "SUCCESS")!false><span class="txt-red">您已经是认证老师，满足条件</span><#else><span class="txt-red">您还不是认证老师</span>，<a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage" target="_blank" class="txt-link">看看如何认证</a></#if>）</li>
                    <#--<li><i></i>教师等级在LV2及以上 （<span class="txt-red">您目前的等级是LV${(currentUser.level)!0}，<#if (currentUser.level gte 2)!false>满足条件<#else><a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/mylevel.vpage" target="_blank" class="txt-link">看看如何升级</a></#if></span>）</li>-->
                    <li><i></i>熟悉一起作业的各项功能</li>
                    <li><i></i>具有良好的人际关系与沟通能力</li>
                </ul>
            </div>
        </div>
    </div>
</div>

    <#if true>
        <@sugar.capsule js=["datepicker"] css=["plugin.datepicker"] />

    <script type="text/javascript">
        $(function(){
            $(document).on("click", ".v-clickNotSign", function(){
                $17.alert("对不起，您还不满足申请条件，请满足后再来申请！");
            });

            //点击查看大使职责和福利
            $(document).on("click", ".v-clickDetailAmbFl", function(){
                $.prompt(template("T:大使责任和福利", {}), {
                    title: "大使职责和福利",
                    buttons: { "知道了": false},
                    position: {width: 910}
                });
            });

            //努力值
            $(document).on("click", ".v-clickLlValue", function(){
                $.prompt(template("T:努力值计算公式", {}), {
                    title: "努力值计算规则",
                    buttons: { "知道了": false},
                    position: {width: 600}
                });
            });

            $(document).on("click", "input[name='isFx']", function(){
                $("#applt-ft").show();
            });

            $(document).on("click", ".v-clickCompetitionBtn", function(){
                if(${(haveBeAmbassador!false)?string}){
                    $17.alert("您先前辞任过大使，目前尚无法申请，谢谢。");
                    return false;
                }

                if(${(inWinter!false)?string}){
                    $17.alert("寒假期间，大使报名暂时关闭，3月1日正常开启。");
                    return false;
                }

                var schoolAddress = "";
                $.prompt(template("T:校园大使竞聘申请表", {}), {
                    focus : 1,
                    title: "申请校园大使",
                    buttons: {"提交申请": true },
                    position: {width: 900},
                    loaded : function(){
                        $( "#datepicker1" ).datepicker({
                            dateFormat: 'yy-mm-dd',
                            yearRange: '1900:2050',
                            monthNamesShort:['01','02','03','04','05','06','07','08','09','10','11','12'],
                            changeMonth: true,
                            changeYear: true
                        });
                        //省的下拉事件
                        var $province = $17.modules.select("#province");
                        $("ul[id='provinceList'] a").on("click",function(){
                            $province.set($(this).data("region_id"),$(this).text());
                            $("#show-province").text($(this).text());
                            schoolAddress += $(this).text();
                            fillRegionList("#city","#cityList",$(this).data("region_id"),"#county","#countyList");
                        });

                        //市的下拉事件
                        var $city = $17.modules.select("#city");
                        $("ul[id='cityList'] a").die().live("click",function(){
                            $city.set($(this).data("region_id"),$(this).text());
                            $("#show-city").text($(this).text());
                            schoolAddress += $(this).text();
                            fillRegionList("#county","#countyList",$(this).data("region_id"));
                        });

                        //区的下拉事件
                        var $county = $17.modules.select("#county");
                        $("ul[id='countyList'] a").die().live("click",function(){
                            $county.set($(this).data("region_id"),$(this).text());
                            $("#show-county").text($(this).text());
                        });

                        //初始化省市区
                        fillRegionList("${(userShippingAddressMapper.cityCode)!}","#cityList","${(userShippingAddressMapper.provinceCode)!}","${(userShippingAddressMapper.countyCode)!}","#countyList");

                    },
                    submit : function(e, v){
                        if(v){
                            var birthday = $("input[name='birthday']").val().split("-");
                            var ifFx = $("input[name='isFx']:checked").val();
                            var data = {
                                name    :   $.trim($("input[name='name']").val()),
                                gender  :   $("input[name=gender]:checked").val(),
                                bYear   :   birthday[0],
                                bMonth  :   birthday[1],
                                bDay    :   birthday[2],
                                tYear   :   $("select[name='tYear']").val(),
                                mobile  :   $("input[name='mobile']").val(),
                                qq      :   $("input[name='qq']").val(),
                                isFx    :   ifFx,
                                schoolLevel :   $("select[name='schoolLevel']").val(),
                                schoolName  :   $("input[name='schoolName']").val(),
                                pname:  $("#province").find("span.content").text(),
                                cname:  $("#city").find("span.content").text(),
                                aname: $("#county").find("span.content").text(),
                                address : $("input[name='addressDetail']").val(),//学校地址
                                leader  :   $("input[name=leader]:checked").val(),
                                englishCount    :   $("input[name='englishCount']").val() ,
                                mathCount   :   $("input[name='mathCount']").val(),
                                chineseCount    :   $("input[name='chineseCount']").val() ,
                                oneGradeClazzCountBegin : $("input[name='oneGradeClazzCountBegin']").val(),
                                oneGradeClazzCountEnd   :$("input[name='oneGradeClazzCountBegin']").val(),
                                oneClazzStudentCountBegin   :$("input[name='oneClazzStudentCountBegin']").val(),
                                oneClazzStudentCountEnd :$("input[name='oneClazzStudentCountEnd']").val(),
                                _from : "apply"
                            };

                            if(ifFx == 'false'){
                                data.eduSystemType = $("input[name='eduSystemType']:checked").val();
                                delete data.fxClass;
                            }else{
                                var fxClass = [];
                                $("input[name='fxClass']:checked").each(function(){
                                    fxClass.push($(this).val());
                                });
                                data.fxClass = fxClass.join(',');
                                delete data.eduSystemType;
                            }

                            if(!$17.isValidCnName(data.name)){
//                            if(!$17.isCnString(data.name)){
                                alertInfo("请填写正确的中文姓名", function(){ $("input[name='name']").focus(); });
                                return false;
                            }

                            if($17.isBlank(data.gender)){
                                alertInfo("请选择性別");
                                return false;
                            }

                            if($17.isBlank(data.bMonth)){
                                alertInfo("请选择生日", function(){ $("input[name='bMonth']").focus(); });
                                return false;
                            }

                            if($17.isBlank(data.tYear) || data.tYear == "请选择年份"){
                                alertInfo("请选择年份", function(){ $("select[name='tYear']").focus(); });
                                return false;
                            }

                            if(!$17.isMobile(data.mobile)){
                                alertInfo("请填写真实手机号码", function(){ $("input[name='mobile']").focus(); });
                                return false;
                            }

                            if(!$17.isNumber(data.qq)){
                                alertInfo("请填写QQ号码,输入数字格式", function(){ $("input[name='qq']").focus(); });
                                return false;
                            }

                            if($17.isBlank(data.isFx)){
                                alertInfo("请选择是否为分校");
                                return false;
                            }

                            if(data.isFx == "true"){
                                if($17.isBlank(data.fxClass)){
                                    alertInfo("请选择分校有哪几个年级");
                                    return false;
                                }
                            }else{
                                if($17.isBlank(data.eduSystemType)){
                                    alertInfo("请选择学制");
                                    return false;
                                }
                            }

                            if($17.isBlank(data.schoolLevel) || data.schoolLevel == "请选择"){
                                alertInfo("请选择学校水平", function(){ $("select[name='schoolLevel']").focus(); });
                                return false;
                            }

                            if($17.isBlank(data.schoolName)){
                                alertInfo("请输入学校全称", function(){ $("input[name='schoolName']").focus(); });
                                return false;
                            }

                            if($17.isBlank(data.pname)){
                                alertInfo("请选择省份", function(){ $("#province").trigger('click'); });
                                return false;
                            }
                            if($17.isBlank(data.cname)){
                                alertInfo("请选择城市", function(){ $("#city").trigger('click'); });
                                return false;
                            }
                            if($17.isBlank(data.aname)){
                                alertInfo("请选择县", function(){ $("#county").trigger('click'); });
                                return false;
                            }

                            if($17.isBlank(data.address)){
                                alertInfo("请输入详细地址", function(){ $("input[name='addressDetail']").focus(); });
                                return false;
                            }

                            if($17.isBlank(data.leader)){
                                alertInfo("请选择是否为教研组长");
                                return false;
                            }

                            if(!$17.isNumber(data.englishCount)){
                                alertInfo("本校英语老师数量,请输入数字", function(){ $("input[name='englishCount']").focus(); });
                                return false;
                            }

                            if(!$17.isNumber(data.mathCount)){
                                alertInfo("本校数学老师数量,请输入数字", function(){ $("input[name='mathCount']").focus(); });
                                return false;
                            }

                            if(!$17.isNumber(data.chineseCount)){
                                alertInfo("本校语文老师数量,请输入数字", function(){ $("input[name='chineseCount']").focus(); });
                                return false;
                            }

                            if(!$17.isNumber(data.oneGradeClazzCountBegin)){
                                alertInfo("请输入正确数字", function(){ $("input[name='oneGradeClazzCountBegin']").focus(); });
                                return false;
                            }

                            /*if(!$17.isNumber(data.oneGradeClazzCountEnd)){
                                alertInfo("请输入正确数字", function(){ $("input[name='oneGradeClazzCountEnd']").focus(); });
                                return false;
                            }*/

                            if(!$17.isNumber(data.oneClazzStudentCountBegin)){
                                alertInfo("请输入正确数字", function(){ $("input[name='oneClazzStudentCountBegin']").focus(); });
                                return false;
                            }

                            if(!$17.isNumber(data.oneClazzStudentCountEnd)){
                                alertInfo("请输入正确数字", function(){ $("input[name='oneClazzStudentCountEnd']").focus(); });
                                return false;
                            }

                            $.post("/ambassador/schoolambassador.vpage", data, function(data){
                                if(data.success){
                                    $17.alert("报名成功！", function(){
                                        location.href = "/ambassador/center.vpage";
                                    });
                                } else {
                                    alertInfo(data.info);
                                }
                            });

                            function alertInfo(content, callback){
                                $("#alertInfo").text(content).slideDown();

                                setTimeout(function(){
                                    $("#alertInfo").slideUp().text("");
                                }, 3000);
                                if(callback){
                                    callback();
                                }
                            }
                            return false;
                        }
                    }
                });
            });
            $(document).on("click", "input[name='isFx']", function(){
                var ifFx =$("input[name='isFx']:checked").val();
                if(ifFx == 'true'){
                    $("#applt-mn02").show();
                    $("#applt-mn01").hide();
                    $(".fx").text("分");
                }else{
                    $("#applt-mn01").show();
                    $("#applt-mn02").hide();
                    $(".fx").text("本");
                }
            });
        });

        /**
         *
         * @param select 页面下拉框样式的DIV的属性ID
         * @param target 填充区域数据的页面元素属性ID
         * @param parentRegionCode 上级区域编码
         * @param childSelect 下一级下拉框样式的DIV的属性ID
         * @param childTarget 下一级区域数据的页面元素属性ID
         * @returns {boolean} 返回值
         */
        function fillRegionList(select,target,parentRegionCode,childSelect,childTarget){
            var $target = $(target);
            $target.empty();
            if($17.isBlank(parentRegionCode)){
                return false;
            }
            $.getJSON('/getregion-' + parentRegionCode + '.vpage',function(data){
                if(data.success){
                    if(data.rows.length > 0){
                        for(var i = 0; i < data.rows.length; i++){
                            var $li = $("<li></li>");
                            var $a = $("<a href='javascript:void(0);'></a>");
                            $a.attr("data-region_id",data.rows[i].key);
                            $a.text(data.rows[i].value);
                            $li.append($a);
                            $target.append($li);
                        }
                        if(!$17.isBlank(select)){
                            var $dark = $(select).find("span.content");
                            $dark.attr("data-value", data.rows[0].key);
                            $dark.text(data.rows[0].value);
                        }

                        if(!$17.isBlank(childTarget) && !$17.isBlank(childSelect)){
                            fillRegionList(childSelect,childTarget, (select != "#city" ? select : data.rows[0].key));
                        }
                    }else{
                        if(!$17.isBlank(select)){
                            var $dark = $(select).find("span.content");
                            $dark.attr("data-value", "");
                            $dark.text("");
                        }
                    }
                }
            });
            return false;
        }
    </script>
    <#--2015-11-05 - html template-->
    <script type="text/html" id="T:大使责任和福利">
        <div class="duty-benefit" style="margin: -40px -20px -20px">
            <div class="db-hd">
                <div class="dh-left"><p>您需要做什么？</p></div>
                <div class="dh-right"><p>您将得到什么？</p></div>
            </div>
            <div class="db-mn">
                <div class="dm-left">
                    <ul>
                        <li>
                            <span class="gold-01 gold-icon"></span>
                            <table><tr><td><span>服务</span><br>帮助并指导老师使用网站功能</td></tr></table>
                        </li>
                        <li>
                            <span class="gold-02 gold-icon"></span>
                            <table><tr><td><span>宣传</span><br>将一起作业推广给身边老师</td></tr></table>
                        </li>
                    </ul>
                </div>
                <div class="dm-right">
                    <ul>
                        <li>
                            <span class="gold-04 gold-icon"></span>
                            <table>
                                <tr><td>结交全国优秀教师<br>专业定制课程</td></tr>
                            </table>
                        </li>
                        <li>
                            <span class="gold-05 gold-icon"></span>
                            <table><tr><td>结交全国优秀教师</td></tr></table>
                        </li>
                        <li>
                            <span class="gold-03 gold-icon"></span>
                            <table><tr><td>论文发表<br>个人事迹宣传<br>教育部重点课程实验基地</td></tr></table>
                        </li>
                        <li>
                            <span class="gold-06 gold-icon"></span>
                            <table><tr><td>专属VIP客服<br>专属礼物兑换</td></tr></table>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </script>
    <script type="text/html" id="T:努力值计算公式">
        <div class="count-box">
            <div class="count-rule" style="height: 258px; overflow: hidden; overflow-y: auto;">
                <tr><td class="td01">努力值获取方法</td><td class=" td02">基础积分</td></tr>
                <table>
                    <tbody>
                    <tr>
                        <td>给1个班布置作业，有30名及以上学生完成</td>
                        <td>+3</td>
                    </tr>
                    <tr>
                        <td colspan="1">给1个班布置作业，有20-29名学生完成</td>
                        <td colspan="1">+2</td>
                    </tr>
                    <tr>
                        <td colspan="1">给1个班布置作业，有10-19名学生完成</td>
                        <td colspan="1">+1</td>
                    </tr>
                    <tr>
                        <td colspan="1">微信布置，30及以上完成</td>
                        <td colspan="1">+6</td>
                    </tr>
                    <tr>
                        <td colspan="1">微信布置，20-29完成</td>
                        <td colspan="1">+4</td>
                    </tr>
                    <tr>
                        <td colspan="1">微信布置，10-19完成</td>
                        <td colspan="1">+2</td>
                    </tr>
                    <tr>
                        <td colspan="1">当月唤醒老师成功</td>
                        <td colspan="1">+20</td>
                    </tr>
                    <tr>
                        <td>使用智慧课堂，奖励5名及以上学生（每月最多奖励10）</td>
                        <td>+2</td>
                    </tr>
                    <tr>
                        <td>帮助1名老师完成认证</td>
                        <td>+50</td>
                    </tr>
                    <tr>
                        <td>论坛发帖、回帖（每月最多加5）</td>
                        <td>+1</td>
                    </tr>
                    <tr>
                        <td>使用校讯通（每月最多加5）</td>
                        <td>+1</td>
                    </tr>
                    <tr>
                        <td colspan="1">使用评论功能（每月最多加5，每天最多奖励1）</td>
                        <td colspan="1">+1</td>
                    </tr>
                    <tr>
                        <td colspan="1">关注大使微信</td>
                        <td colspan="1">+20</td>
                    </tr>
                    <tr>
                        <td colspan="1">填写问卷</td>
                        <td colspan="1">+20</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </script>
    <script type="text/html" id="T:校园大使竞聘申请表">
        <div class="w-form-table" style="margin-top: -30px;">
            <div id="applt-hd">
                <dl>
                    <dl>
                        <dt>姓名：</dt>
                        <dd>
                            <input name="name" value="${(currentUser.profile.realname)!}"  class="w-int"/></p>
                        </dd>
                    </dl>
                    <dl>
                        <dt>性別：</dt>
                        <dd>
                            <label><input type="radio" name="gender" value="M" >男</label>
                            <label><input type="radio" name="gender" value="F">女</label>
                            <span class="w-form-misInfo"><strong class="info">（*必填）</strong></span>
                        </dd>
                    </dl>
                    <dl>
                        <dt style="display: ;">生日：</dt>
                        <dd style="display: ;">
                            <input id="datepicker1" name="birthday" value="" class="w-int"/>
                            <span class="w-form-misInfo w-form-info-error"><strong class="info">（*必填）</strong></span>
                        </dd>
                    </dl>
                    <dl>
                        <dt>成为老师年份：</dt>
                        <dd>
                            <select name="tYear">
                                <option value="">请选择年份</option>
                                <#list 1900..2050 as year>
                                    <option>${year}</option>
                                </#list>
                            </select>
                        </dd>
                    </dl>
                    <dl>
                        <dt>手机号：</dt>
                        <dd>
                            <input type="text" value="" class="w-int" name="mobile" maxlength="11">
                            <span class="w-form-misInfo"><strong class="info">（*必填，请输入11位正确手机号）</strong></span>
                        </dd>
                    </dl>
                    <dl>
                        <dt>QQ号：</dt>
                        <dd>
                            <input type="text" value="" class="w-int" name="qq">
                            <span class="w-form-misInfo"><strong class="info">（*必填，请输入正确的qq号）</strong></span>
                        </dd>
                    </dl>
                    <dl>
                        <dt>是否为分校：</dt>
                        <dd>
                            <label><input class="int-radio" type="radio" name="isFx" value="true" >是</label>
                            <label><input class="int-radio" type="radio" name="isFx" value="false" >否</label>
                            <span class="w-form-misInfo"><strong class="info">（*必填）</strong></span>
                        </dd>
                    </dl>
                </dl>
            </div>

            <div id="applt-ft" style="display: none;">
                <div id="applt-mn">
                <#--step1-->
                    <div id="applt-mn01">
                        <dl>
                            <dt>学制：</dt>
                            <dd>
                                <label><input class="int-radio" type="radio" name="eduSystemType" value="P5" >5年</label>
                                <label><input class="int-radio" type="radio" name="eduSystemType" value="P6">6年</label>
                                <span class="w-form-misInfo"><strong class="info">（*必填）</strong></span>
                            </dd>
                        </dl>
                    </div>
                <#--step2-->
                    <div id="applt-mn02" style="display: none;">
                        <dl>
                            <dt style="display: ;">分校有哪几个年级：</dt>
                            <dd style="display: ;">
                                <label><input class="w-checkbox" name="fxClass" type="checkbox" value="一年级"/>一年级 </label>
                                <label><input class="w-checkbox" name="fxClass" type="checkbox" value="二年级" />二年级 </label>
                                <label><input class="w-checkbox" name="fxClass" type="checkbox" value="三年级" />三年级 </label>
                                <label><input class="w-checkbox" name="fxClass" type="checkbox" value="四年级" />四年级 </label>
                                <label><input class="w-checkbox" name="fxClass" type="checkbox" value="五年级" />五年级 </label>
                                <label><input class="w-checkbox" name="fxClass" type="checkbox" value="六年级" />六年级 </label>
                            </dd>
                        </dl>
                    </div>
                </div>
                <dl>
                    <dt>学校水平：</dt>
                    <dd>
                        <div class="w-select">
                            <select name="schoolLevel">
                                <option value="">请选择</option>
                                <option value="省市重点">省市重点</option>
                                <option value="区县重点">区县重点</option>
                                <option value="普通学校">普通学校</option>
                                <option value="资源较贫乏的学校">资源较贫乏的学校</option>
                            </select>
                        <#--<div class="current" name="schoolLevel"><span class="content" data-value=""></span><span class="w-icon w-icon-arrow"></span></div>-->
                        </div>
                    </dd>
                </dl>
                <dl>
                    <dt><span class="fx">本</span>校全称：</dt>
                    <dd>
                        <input type="text" value="" class="w-int" name="schoolName">
                        <span class="w-form-misInfo"><strong class="info">（*必填，请输入正确的学校全称）</strong></span>
                    </dd>
                </dl>
                <dl>
                    <dt style="display: ;"><span class="fx">本</span>校地址：</dt>
                    <dd style="display: ;" >
                        <div id="province" class="w-select">
                            <div class="current"><span class="content" data-value=""></span><span class="w-icon w-icon-arrow"></span></div>
                            <ul id="provinceList" data-size="8" style="display: none;">
                                <li><a href="javascript:void(0);" data-region_id="120000">天津</a></li>
                                <li><a href="javascript:void(0);" data-region_id="320000">江苏</a></li>
                                <li><a href="javascript:void(0);" data-region_id="360000">江西</a></li>
                                <li><a href="javascript:void(0);" data-region_id="440000">广东</a></li>
                                <li><a href="javascript:void(0);" data-region_id="520000">贵州</a></li>
                                <li><a href="javascript:void(0);" data-region_id="640000">宁夏</a></li>
                                <li><a href="javascript:void(0);" data-region_id="130000">河北</a></li>
                                <li><a href="javascript:void(0);" data-region_id="210000">辽宁</a></li>
                                <li><a href="javascript:void(0);" data-region_id="330000">浙江</a></li>
                                <li><a href="javascript:void(0);" data-region_id="370000">山东</a></li>
                                <li><a href="javascript:void(0);" data-region_id="410000">河南</a></li>
                                <li><a href="javascript:void(0);" data-region_id="450000">广西</a></li>
                                <li><a href="javascript:void(0);" data-region_id="530000">云南</a></li>
                                <li><a href="javascript:void(0);" data-region_id="610000">陕西</a></li>
                                <li><a href="javascript:void(0);" data-region_id="650000">新疆</a></li>
                                <li><a href="javascript:void(0);" data-region_id="810000">香港</a></li>
                                <li><a href="javascript:void(0);" data-region_id="140000">山西</a></li>
                                <li><a href="javascript:void(0);" data-region_id="220000">吉林</a></li>
                                <li><a href="javascript:void(0);" data-region_id="340000">安徽</a></li>
                                <li><a href="javascript:void(0);" data-region_id="420000">湖北</a></li>
                                <li><a href="javascript:void(0);" data-region_id="460000">海南</a></li>
                                <li><a href="javascript:void(0);" data-region_id="500000">重庆</a></li>
                                <li><a href="javascript:void(0);" data-region_id="540000">西藏</a></li>
                                <li><a href="javascript:void(0);" data-region_id="620000">甘肃</a></li>
                                <li><a href="javascript:void(0);" data-region_id="820000">澳门</a></li>
                                <li><a href="javascript:void(0);" data-region_id="110000">北京</a></li>
                                <li><a href="javascript:void(0);" data-region_id="150000">内蒙古</a></li>
                                <li><a href="javascript:void(0);" data-region_id="230000">黑龙江</a></li>
                                <li><a href="javascript:void(0);" data-region_id="310000">上海</a></li>
                                <li><a href="javascript:void(0);" data-region_id="350000">福建</a></li>
                                <li><a href="javascript:void(0);" data-region_id="430000">湖南</a></li>
                                <li><a href="javascript:void(0);" data-region_id="510000">四川</a></li>
                                <li><a href="javascript:void(0);" data-region_id="630000">青海</a></li>
                            <#--<li><a href="javascript:void(0);" data-region_id="710000">台湾</a></li>-->
                            </ul>
                        </div>
                        <div id="city" class="w-select">
                            <div class="current"><span class="content" data-value=""></span><span class="w-icon w-icon-arrow"></span></div>
                            <ul id="cityList" data-size="8"></ul>
                        </div>
                        <div id="county" class="w-select">
                            <div class="current"><span class="content" data-value=""></span><span class="w-icon w-icon-arrow"></span></div>
                            <ul id="countyList" data-size="8"></ul>
                        </div>
                        <div style="padding: 10px 0 0;">
                            <input type="text" value="" class="w-int" name="addressDetail" placeholder="详细街道名称、门牌号等" style="width: 324px;">
                        </div>
                        <span class="w-form-misInfo w-form-info-error"><strong class="info">（*必填）</strong></span>
                    </dd>
                </dl>
                <dl>
                    <dt>是否为教研组长：</dt>
                    <dd>
                        <label><input class="int-radio" type="radio" name="leader" value="YES" >是</label>
                        <label><input class="int-radio" type="radio" name="leader" value="NO">否</label>
                        <span class="w-form-misInfo"><strong class="info">（*必填）</strong></span>
                    </dd>
                </dl>
                <dl>
                    <dt><span class="fx">本</span>校英语老师数：</dt>
                    <dd>
                        <input type="text" value="" class="w-int" name="englishCount">
                        <span class="w-form-misInfo"><strong class="info">（*必填，请输入数字）</strong></span>
                    </dd>
                </dl>
                <dl>
                    <dt><span class="fx">本</span>校数学老师数：</dt>
                    <dd>
                        <input type="text" value="" class="w-int" name="mathCount">
                        <span class="w-form-misInfo"><strong class="info">（*必填，请输入数字）</strong></span>
                    </dd>
                </dl>
                <dl>
                    <dt><span class="fx">本</span>校语文老师数：</dt>
                    <dd>
                        <input type="text" value="" class="w-int" name="chineseCount">
                        <span class="w-form-misInfo"><strong class="info">（*必填，请输入数字）</strong></span>
                    </dd>
                </dl>
                <dl>
                    <dt><span class="fx">本</span>校班级总数：</dt>
                    <dd class="sdd">
                        <input type="text" value="" class="w-int" name="oneGradeClazzCountBegin">
                        <#--<span>至</span>
                        <input type="text" value="" class="w-int" name="oneGradeClazzCountEnd">
                        <span class="w-form-misInfo"><strong class="info">（*如果只教一个年级，前后数字保持一致）</strong></span>-->
                    </dd>
                </dl>
                <dl>
                    <dt>所教单个班级的学生数： </dt>
                    <dd class="sdd">
                        <input type="text" value="" class="w-int" name="oneClazzStudentCountBegin">
                        <span>至</span>
                        <input type="text" value="" class="w-int" name="oneClazzStudentCountEnd">
                        <span class="w-form-misInfo"><strong class="info">（*如果学生数相同，前后数字保持一致）</strong></span>
                    </dd>
                </dl>
            </div>
        </div>
        <div style="clear: both;"></div>
        <div id="alertInfo" style="text-align: center; color:#f00;"></div>
    </script>
    </#if>
</@temp.page>