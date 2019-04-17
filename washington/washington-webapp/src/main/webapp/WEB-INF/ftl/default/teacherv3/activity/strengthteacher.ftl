<#import "../../layout/project.module.ftl" as temp />
<@temp.page header="show">
    <@app.css href="public/skin/project/strengthteacher/css/skin.css" />
<div class="main">
    <div class="mn-ad"></div>
    <div class="mn-content">
        <div class="mn-cform">
            <div class="form-mn">
                <div class="form-mnl">
                    <div class="mnl-cell"><span>• 姓名：</span><input id="name" value=""><span style="color: #f80000; font-size: 14px;display: none;" name="errortip">(此项为必填)</span></div>
                    <div class="mnl-cell"><span>• Q Q：</span><input id="qq" value=""><span style="color: #f80000; font-size: 14px;display: none" name="errortip">(此项为必填)</span></div>
                    <div class="mnl-cell"><span>• 手机：</span><input id="mobile" value="" maxlength="11"><span style="color: #f80000; font-size: 14px;display: none" name="errortip" >(此项为必填)</span></div>
                    <div class="mnl-cell"><span>• 性别：</span>
                        <label><input type="radio" name="gender" value="M">男</label>
                        <label><input type="radio" name="gender" value="F">女</label>
                        <span style="color: #f80000; font-size: 14px;display: none" name="errortip">(此项为必填)</span>
                    </div>
                    <div class="mnl-cell"><span>• 学历：</span>
                        <label><input type="radio" name="education" value="专科">专科</label>
                        <label><input type="radio" name="education" value="本科">本科</label><br><br>
                        <label style="padding-left: 73px;"><input type="radio" name="education" value="研究生">研究生</label>
                        <label><input type="radio" name="education" value="博士">博士</label>
                        <span style="color: #f80000; font-size: 14px;display: none" name="errortip">(此项为必填)</span>
                    </div>
                    <div class="mnl-cell">
                        <p>• 是否为教研组长：<span style="color: #f80000; font-size: 14px;display: none" name="errortip">(此项为必填)</span></p>
                        <label><input type="radio" name="leader" value="true">是</label>
                        <label><input type="radio" name="leader" value="false">否</label>
                    </div>
                    <div class="mnl-cell">
                        <p>• 您是否愿意当话题小组长？<span style="color: #f80000; font-size: 14px;display: none" name="errortip">(此项为必填)</span></p>
                        <label><input type="radio" name="willing" value="true">是</label>
                        <label><input type="radio" name="willing" value="false">否</label>
                    </div>
                    <div class="mnl-cell">
                        <p>• 您是否为校园大使？<span style="color: #f80000; font-size: 14px;display: none" name="errortip">(此项为必填)</span></p>
                        <label><input type="radio" name="elchee" value="true">是</label>
                        <label><input type="radio" name="elchee" value="false">否</label>
                    </div>
                    <div class="mnl-cell">
                        <p>• 请您选择以下符合您的教学情况？<span style="color: #f80000; font-size: 14px;display: none" name="errortip">(此项为必填)</span></p>
                        <div class="select-box">
                            <select id="teach-type">
                                <option name="" value="请选择">请选择</option>
                                <option name="" value="英语">英语</option>
                                <option name="" value="数学">数学</option>
                            </select>
                            <select id="teach-grade">
                                <option name="" value="请选择">请选择</option>
                                <option name="" value="一年级">一年级</option>
                                <option name="" value="二年级">二年级</option>
                                <option name="" value="三年级">三年级</option>
                                <option name="" value="四年级">四年级</option>
                                <option name="" value="五年级">五年级</option>
                                <option name="" value="六年级">六年级</option>
                            </select>
                            <select id="teach-version">
                                <option name="" value="请选择">请选择</option>
                                <option name="" value="人教PEP">人教PEP</option>
                                <option name="" value="新标准版">新标准版</option>
                                <option name="" value="新起点版">新起点版</option>
                                <option name="" value="牛津（译林）">牛津（译林）</option>
                                <option name="" value="牛津版（上教）">牛津版（上教）</option>
                                <option name="" value="北师大版">北师大版</option>
                                <option name="" value="人教版">人教版</option>
                                <option name="" value="苏教版">苏教版</option>
                                <option name="" value="其它">其它</option>
                            </select>
                        </div>
                    </div>
                    <div class="mnl-cell">
                        <p>• 您所在的学校水平？<span style="color: #f80000; font-size: 14px;display: none" name="errortip">(此项为必填)</span></p>
                        <div class="select-box">
                            <select id="schoollevel">
                                <option name="" value="请选择">请选择</option>
                                <option name="" value="省市重点">省市重点</option>
                                <option name="" value="区县重点">区县重点</option>
                                <option name="" value="普通学校">普通学校</option>
                                <option name="" value="资源缺乏学校">资源缺乏学校</option>
                            </select>
                        </div>
                    </div>
                    <div class="mnl-cell">
                        <p>• 您对以下哪个话题最擅长？<span style="color: #f80000; font-size: 14px;display: none" name="errortip">(此项为必填)</span></p>
                        <div class="select-box">
                            <select id="topic">
                                <option name="" value="请选择">请选择</option>
                                <option name="" value="授课课件">授课课件</option>
                                <option name="" value="试题资源">试题资源</option>
                                <option name="" value="学困生">学困生</option>
                                <option name="" value="课堂氛围">课堂氛围</option>
                                <option name="" value="批评尺度">批评尺度</option>
                                <option name="" value="高效复习">高效复习</option>
                                <option name="" value="英语口语">英语口语</option>
                                <option name="" value="公开课">公开课</option>
                                <option name="" value="赛课">赛课</option>
                                <option name="" value="教师个人魅力">教师个人魅力</option>
                                <option name="" value="家校关系">家校关系</option>
                            </select>
                        </div>
                    </div>
                    <div class="mnl-cell">
                        <p>• 您每天愿意花多长时间组织或者参与专题讨论？</p>
                        <div class="select-box">
                            <select id="topic-span">
                                <option name="" value="请选择">请选择</option>
                                <option name="" value="三小时">三小时</option>
                                <option name="" value="二小时">二小时</option>
                                <option name="" value="一小时">一小时</option>
                                <option name="" value="45分钟以内 ">45分钟以内 </option>
                            </select>
                            <span style="color: #f80000; font-size: 14px;display:none" name="errortip">(此项为必填)</span>
                        </div>
                    </div>
                </div>
                <div class="form-mnr">
                    <div class="mnr-cell">
                        <p>分享一个令您印象最深的教学案例？<span style="font-size: 14px;">（不少于200字的真实案例，以攻克教学难题、授课难题、班级管理难题为最佳）</span><span style="color: #f80000; font-size: 14px;display: none" name="errortip">(此项为必填)</span><span style="color: #f80000; font-size: 14px;display: none" name="contenttip">(字数不少于200字)</span></p>
                        <textarea id="teachedcase" placeholder="标题：
案例：
方法论总结："></textarea>
                    </div>
                    <div class="mnr-cell">
                        <p>说说自己教师职业生涯中获得的荣誉、奖励！</p>
                        <p style="font-size: 14px;">（内容包括且不限于校级奖励、发表过的文章）<span style="color: #f80000; font-size: 14px;display: none" name="errortip">(此项为必填)</span></p>
                        <textarea id="profpaper" placeholder="请输入内容"></textarea>
                    </div>
                </div>
                <div style=" clear: both; text-align: center; color: #f00; padding: 0 0 50px; line-height: 120%; font-size: 14px;">提示：系统仅以您第一次提交的信息为准</div>
                <div style="clear: both;"><a href="javascript:void(0)" class="submit-btn">提交报名</a></div>
            </div>
        </div>
        <div class="mn-cview">
            <div class="view-hd">
                <h3>专属福利</h3>
            </div>
            <div class="view-mn">
                <p>• 加入实力派专属qq群，零距离和全国优秀教师接触；</p>
                <p>• 优先获得包括课题、名师讲座在内的平台资源倾斜的特权；</p>
                <p>• 享受教学实力派的成果，让天下没有难教的课程，没有难管的孩子，遇见更好的自己；</p>
                <p>• 园丁豆、优秀实力派证书、实力派专属礼物</p>
            </div>
            <div class="view-ft"></div>
        </div>
        <div class="mn-cview">
            <div class="view-hd">
                <h3>申请条件</h3>
            </div>
            <div class="view-mn">
                <p>• 富有热情、热爱教学和交流教学难题；</p>
                <p>• 教研组长、一线优秀教师优先录取；</p>
                <p>• 有责任心，敢于创新，专业度高，经验丰富的老师优先录取；</p>
                <p>• 申请表里提供的教学案例代表性强优先录取</p>
            </div>
            <div class="view-ft"></div>
        </div>
        <div class="mn-cview">
            <div class="view-hd">
                <h3>主要职责</h3>
            </div>
            <div class="view-mn">
                <p>• 负责参与每月一期的月刊的话题讨论、踊跃分享教学资源；</p>
                <p>• 不定期分享教学案例，保持自我学习、进步；</p>
            </div>
            <div class="view-ft"></div>
        </div>
        <div class="mn-cview mn-footer">
            <div class="ft-left">
                <h3>关注一起作业官方微信</h3>
                <p>扫描右侧二维码或者搜索微信号yiqizuoye关注一起作业，</p>
                <p>查看往期教学实力派风采和月刊精彩内容！</p>
            </div>
            <div class="ft-right">
                <img src="/public/skin/project/strengthteacher/images/code.png">
                <span>扫描获取首期月刊</span>
            </div>
        </div>
    </div>
</div>
<div id="footerPablic" data-type="0" data-service=""></div>
<script type="text/javascript">
    /*$.prompt("<div>亲爱的老师，教学实力派第二期报名已经结束，报名结果将会已系统提示的方式通知您。报名请关注第三期通知，感谢您的参与！</div> <p style='text-align: right;'>一起作业教学实力派</p>",{
        title : " 系统通知",
        buttons : {"知道了": false}
    });*/

    $(".submit-btn").click(submitForm);
    var isSubmitSuccss = false;
    var oldParams;
    function submitForm(){
        var result  = validatePageParams();
        if(result){
                var params = getPostParams();
                $(".submit-btn").unbind("click");
                $.ajax({
                    url:"/teacher/activity/jxslp.vpage",
                    type:"POST",
                    data:params,
                    success:function(data){
                        if(data.success){
                            $17.alert("已经添加成功", function(){
                                location.reload();
                            });
                        }
                        else{
                            if(data.info){
                                $17.alert(data.info);
                            }
                            else{
                                $17.alert("添加失败，请重新添加");
                            }
                        }
                        $(".submit-btn").click(submitForm);
                    },
                    error:function(data){
                        $17.alert("提交信息失败，请重新提交");
                        $(".submit-btn").click(submitForm);
                    }
                });
            }

    }
    function getPostParams(){
         var qq = $("#qq").val();
         var degree=  $("input[name='education']:checked").attr("value");
         var grade = $("#teach-grade option:selected").attr("value");
         var book =  $("#teach-version option:selected").attr("value");
         var goodAt = $("#topic option:selected").attr("value");
         var schoolLevel = $("#schoollevel option:selected").attr("value");
         var duration = $("#topic-span option:selected").attr("value");
         var mitc = $("#teachedcase").val();
         var honour =  $("#profpaper").val();
         var leader = $("input[name='leader']:checked").attr("value");
         var willing  = $("input[name='willing']:checked").attr("value");
         var gender =$("input[name='gender']:checked").attr("value");
         var mobile = $("#mobile").val();
         var postParams = {
             "qq": qq,
             "degree": degree,
             "grade": grade,
             "book": book,
             "goodAt": goodAt,
             "schoolLevel": schoolLevel,
             "duration": duration,
             "mitc": mitc,
             "honour": honour,
             "leader": leader,
             "willing": willing,
             "gender":gender,
             "mobile":mobile
         };
        return postParams;

    }

    $("div.mnl-cell").click(function(){
        $(this).find("span[name='errortip']").hide();
    });
    $("div.mnr-cell").click(function(){
        $(this).find("span[name='errortip']").hide();
        $(this).find("span[name='contenttip']").hide();
    });
    //基础验证，并提供提示
    function validate(source,tip,placeholder){
        if(!source||source===placeholder){
            return false;
        }
        return true;
    }
    //验证radio
   function validateRadio(name,placeholder,tip){
        var value =  $("input[name='"+name+"']:checked").attr("value");

        var result =  validate(value,tip,placeholder);
        if(!result){
            $("input[name='"+name+"']").parents("div.mnl-cell").find("span[name='errortip']").show();
        }
       return result;
   }
    //验证select
   function validateSelect(id,tip,placeholder,postion) {
       var value = $("#" + id + " option:selected").val();
       var result = validate(value, tip, placeholder);
       if(!result){
           $("#" + id + " option").parents("div.mnl-cell").find("span[name='errortip']").show();
       }
       return result;
   }

  //验证页面填写的参数
  function validatePageParams(){
      var name = $("#name").val();
      var result = true;
      if(!validate(name,"请输入姓名","")){
          result = false;
          $("#name").parents("div.mnl-cell").find("span[name='errortip']").show();
      }
      var qq = $("#qq").val();
      if(!validate(qq,"请输入qq号","")){
          result = false;
          $("#qq").parents("div.mnl-cell").find("span[name='errortip']").show();
      }
      var mobile = $("#mobile").val();
      if(!validate(mobile,"请输入手机号","")){
          result = false;
          $("#mobile").parents("div.mnl-cell").find("span[name='errortip']").show();
      }
      var inputArr = [];
      inputArr[0] = {name:"gender",tip:"请选择性别",placeholder:""};
      inputArr[1] = {name:"education",tip:"请选择学历",placeholder:""};
      inputArr[2] = {name:"leader",tip:"请选择是否为教研组长",placeholder:""};
      inputArr[3] = {name:"willing",tip:"请选择是否愿意为专题组长",placeholder:""};
      inputArr[4] = {name:"elchee",tip:"请选择是否为校园大使",placeholder:""};
      for(var i=0;i<inputArr.length;i++){
          if(!validateRadio(inputArr[i].name,inputArr[i].placeholder,inputArr[i].tip)){
              result = false;
          }
      }
      var slectArr = [];
      slectArr[0] = {id:'teach-type',tip:"请选择科目",placeholder:"请选择"};
      slectArr[1] = {id:'teach-grade',tip:"请选择年级",placeholder:"请选择"};
      slectArr[2] = {id:'teach-version',tip:"请选择教材版本",placeholder:"请选择"};
      slectArr[3] = {id:'schoollevel',tip:"请选择学校水平",placeholder:"请选择"};
      slectArr[4] = {id:'topic',tip:"请选择擅长的话题",placeholder:"请选择"};
      slectArr[5] = {id:'topic-span',tip:"请选择讨论的时间",placeholder:"请选择"};

      for(var i=0;i<slectArr.length;i++){
          if(!validateSelect(slectArr[i].id,slectArr[i].tip,slectArr[i].placeholder)){
              result = false;
          }
      }
      var teachCase= $("#teachedcase").val();
      if(!teachCase|| teachCase ===""){
          $("#teachedcase").parents('div.mnr-cell').find("span[name='errortip']").show();
          result = false;
      }
      if(teachCase&&teachCase.length<200){
          $("#teachedcase").parents('div.mnr-cell').find("span[name='contenttip']").show();
          result = false;
      }
      var profpaper = $("#profpaper").val();
      if(!profpaper|| profpaper ===""){
          $("#profpaper").parents("div.mnr-cell").find("span[name='errortip']").show();
          result = false;
      }
      return result;
  }
</script>
</@temp.page>