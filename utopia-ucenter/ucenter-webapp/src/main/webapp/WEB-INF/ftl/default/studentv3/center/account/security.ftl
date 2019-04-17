<style type="text/css">
    .alert_vox { padding: 8px 35px 8px 14px; margin-bottom: 20px; text-shadow: 0 1px 0 rgba(255,255,255,0.5); background-color: #fcf8e3; border: 1px solid #fbeed5; -webkit-border-radius: 4px; -moz-border-radius: 4px; border-radius: 4px }
    .stepInfoBox{ float:right; margin: -20px 50px 20px; height: 30px;}
    .stepInfoBox li{ font:12px/1.125 "微软雅黑", "Microsoft YaHei", Arial, "黑体"; color: #666; float:left; clear:none;}
    .stepInfoBox li s{ font:2px/2px arial; height:2px; overflow:hidden; background:#ccc; display:inline-block; width:45px; margin: 0 2px;}
    .stepInfoBox li i{ background:#999; color:#fff; display:inline-block; padding:1px 4px ; border-radius:3px; margin: 0 2px; font: bold 11px/1.125 arial;}
    .stepInfoBox li.sel { color: #9ad64d;}
    .stepInfoBox li.sel s{ background:#9ad64d;}
    .stepInfoBox li.sel i{ background:#9ad64d;}
</style>
<div class="tb-box">
    <div class="t-center-list">
    <#if securityQuestionSetted?? && securityQuestionSetted>
        <div class="tf-left w-fl-left">
            <span class="w-detail w-right"></span>
        </div>
        <div class="tf-center w-fl-left">
            <p class="w-green">设置密保问题：已设置</p>
            <p>设置后，可以通过回答自己设置的密保问题找回密码 </p>
        </div>
        <div class="tf-right w-fl-left">
            <a class="w-btn-dic w-btn-gray-new" style="cursor: default;" href="javascript:void(0);">密保已设置</a>
        </div>

    <#else>
        <div class="tf-left w-fl-left">
            <span class="w-detail w-wrong"></span>
        </div>
        <div class="tf-center w-fl-left">
            <p class="w-red">设置密保问题：未设置</p>
            <p>设置后，可以通过回答自己设置的密保问题找回密码 </p>
        </div>
        <div class="tf-right w-fl-left">
            <a class="w-btn-dic w-btn-green-new accountBut" data-box_type="security" href="javascript:void(0);">设置密保</a>
        </div>
    </#if>
        <div class="w-clear"></div>
    </div>
    <div class="w-form-table accountBox" data-box_type="security" style="display: none;">
        <div id="first_box">
            <ul class="stepInfoBox">
                <li class="sel"><i>1</i><b>设置密保问题</b></li>
                <li><s></s><i>2</i><b> 验证问题</b></li>
            </ul>
            <div class="alert_vox edge_vox" style="margin-top: 40px !important;">
            <span class="text_small" style="line-height: 22px;">
                请选择你能够牢记的问题作为密保问题，将来可以用于找回密码<br/>
            </span>
            </div>
            <dl>
                <dt>问题1：</dt>
                <dd>
                    <select id="encryptedFirst" class="w-int securitySelect" style="width: 212px;"></select>
                    <span class="init"></span>
                </dd>
                <dt>答案：</dt>
                <dd>
                    <input name="answer" class="w-int" type="text" maxlength="20">
                    <span class="init"></span>
                </dd>
                <dt>问题2：</dt>
                <dd>
                    <select id="encryptedSecond" class="w-int securitySelect" style="width: 212px;"></select>
                    <span class="init"></span>
                </dd>
                <dt>答案：</dt>
                <dd>
                    <input name="answer" type="text" class="w-int" maxlength="20">
                    <span class="init"></span>
                </dd>
                <dt>问题3：</dt>
                <dd>
                    <select id="encryptedThird" class="w-int securitySelect" style="width: 212px;"></select>
                    <span class="init"></span>
                </dd>
                <dt>答案：</dt>
                <dd>
                    <input name="answer" type="text" class="w-int" maxlength="20">
                    <span class="init"></span>
                </dd>
                <dd>
                    <a id="encrypted_submit" href="javascript:void(0);" class="w-btn-dic w-btn-green-new">
                        <strong><span>下一步</span></strong>
                    </a>
                </dd>
            </dl>
        </div>

        <div id="next_box" style="display: none;" >
            <ul class="stepInfoBox">
                <li class="sel"><i>1</i><b>设置密保问题</b></li>
                <li class="sel"><s></s><i>2</i><b> 验证问题</b></li>
            </ul>
            <div class="alert_vox edge_vox" style="margin-top: 40px !important;">
                <span class="text_small" style="line-height: 22px;">
                    请输入你刚刚设置问题的答案，以验证问题设置的是否准确
                </span>
            </div>
            <dl>
                <dt>问题1：</dt>
                <dd>
                    <input name="myQuestion" type="text" class="w-int" disabled="disabled" value="" style="border: 0"/>
                    <span class="init"></span>
                </dd>
                <dt>答案：</dt>
                <dd>
                    <input name="newAnswer" type="text" class="w-int" maxlength="20">
                    <span class="init"></span>
                </dd>
                <dt>问题2：</dt>
                <dd>
                    <input name="myQuestion" disabled="disabled" class="w-int" value="" style="border: 0" type="text"/>
                    <span class="init"></span>
                </dd>
                <dt>答案：</dt>
                <dd>
                    <input name="newAnswer" placeholder="" class="w-int" type="text" maxlength="20">
                    <span class="init"></span>
                </dd>
                <dt>问题3：</dt>
                <dd>
                    <input name="myQuestion" disabled="disabled" class="w-int" value="" style="border: 0" type="text"/>
                    <span class="init"></span>
                </dd>
                <dt>答案：</dt>
                <dd>
                    <input name="newAnswer" type="text" class="w-int" maxlength="20">
                    <span class="init"></span>
                </dd>
                <dd>
                    <a id="submit" href="javascript:void(0);" class="w-btn-dic w-btn-green-new">
                        <strong><span>验证</span></strong>
                    </a>
                </dd>
            </dl>
        </div>
    </div>
</div>

<script type="text/javascript">
    var encryptedMap = {};

    function encrypted(key, value) {
        encryptedMap[key] = value;
    }
    var qObj = {
        elmt:'select.securitySelect',
        tip:'请选择',
        tVal:'',
        cur:[],
        arr:{
            1: "妈妈的手机",
            2: "爸爸的手机",
            3: "爷爷的姓名",
            4: "奶奶的姓名",
            5: "姥姥的姓名",
            6: "姥爷的姓名",
            7: "爷爷家电话",
            8: "姥姥家电话" ,
            9: "爷爷的生日" ,
            10: "奶奶的生日" ,
            11: "姥姥的生日",
            12: "姥爷的生日"
        },
        isAllSelected : function(){
            if(this.cur.length === 0) return false;
            for(var i in this.cur){
                if(this.cur[i] === this.tVal) return false;
            }
            return true;
        },
        inAllInput : function(){
            //todo
        }
    }

    $(function(){
        var elements = $(qObj.elmt);
        var randomItem ;

        elements.each(function(i){
            var html = '<option value="'+ qObj.tVal +'">'+ qObj.tip +'</option>';
            for(var q in qObj.arr){
                html += '<option value="'+ q +'">' + qObj.arr[q] + '</option>';
            }
            $(this).html(html);
        });

        elements.change(function(){
            var cValue = {}, elmts = $(qObj.elmt),cIndex = elmts.index($(this));
            elmts.each(function(i){
                qObj.cur[i] = $(this).val();
            });
            for(var i in qObj.cur){
                cValue[qObj.cur[i]] = 1;
            }
            elmts.each(function(i){
                if (cIndex == i) return;
                var html = '<option value="'+ qObj.tVal +'">'+ qObj.tip +'</option>';
                for(var q in qObj.arr){
                    if (cValue[q] && q != qObj.cur[i]) continue;
                    html += '<option value="'+ q +'"' + (q == qObj.cur[i]?' selected="selected"': '') + '>' + qObj.arr[q] + '</option>';
                }
                $(this).html(html);
            });
        });

        /*下一步*/
        $("#encrypted_submit").on('click', function(){
            if(qObj.isAllSelected()){
                var l = [];
                $('input[name = answer]').each(function(){
                    if(!$17.isBlank($(this).val())){
                        l.push($(this).val());
                    }
                });

                if(l.length < 3){
                    $17.alert("请填写密保问题的答案");
                    return false;
                }

                $("#first_box").hide();
                $("#next_box").show();

                //密保 key value
                $.each(qObj.cur,function(i){
                    encrypted(qObj.cur[i], l[i]);
                });

                //密保问题乱序
                randomItem = qObj.cur.sort(function(){ return Math.random() > 0.5 ? -1 : 1;});
                $("#next_box input[name=myQuestion]").each(function(i){
                    $(this).attr("value", qObj.arr[randomItem[i]]);
                });

            }else{
                $17.alert('请选择密保问题');
                return false;
            }
        });

        //验证
        $("#submit").on('click', function(){
            var answer = [];
            var newAnswer = [];
            var isTrue = true;

            //获取答案
            $("#next_box input[name=newAnswer]").each(function(i){
                newAnswer.push($(this).val());
            });

            //获取原有答案
            $.each(qObj.cur,function(i){
                answer.push(encryptedMap[randomItem[i]])
            });

            //密保答案验证
            for(var i=0; i<answer.length; i++){
                if(answer[i] != newAnswer[i]){
                    isTrue = false;
                    $17.alert("密保答案有误，重新输入");
                    return false;
                }
            }

            if(isTrue){
                var data = {
                    firstQuestion : qObj.arr[qObj.cur[0]],
                    secondQuestion : qObj.arr[qObj.cur[1]],
                    thirdQuestion : qObj.arr[qObj.cur[2]],
                    firstAnswer : answer[0],
                    secondAnswer : answer[1],
                    thirdAnswer : answer[2]
                };

                App.postJSON('/student/center/securityquestion.vpage', data, function(data){
                    if(data.success){
                        $17.tongji('学生-设置了密保问题的人数');
                        $17.alert("密保设置成功", function(){
                            location.href = '/student/center/account.vpage';
                        });
                    }else{
                        $17.alert("密保设置失败")
                    }
                });
            }
        });
    })
</script>