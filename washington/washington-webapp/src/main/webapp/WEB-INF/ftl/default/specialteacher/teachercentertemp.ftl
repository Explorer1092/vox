<#--默认显示的个人信息-->
<script id="teacherInfoTemp" type="text/html">
    <div class="personalData-head">
        <a href="javascript:;" class="green_fontBtn fr" style="display: none;">未认证</a>
        <div class="ped-image">
            <img src="<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>"><!--此处放头像-->
        <#--<span class="icon-camera"></span>-->
        </div>
        <div class="ped-name">${(currentUser.profile.realname)!}</div>
    </div>
    <div class="class-module mt-20">
        <div class="module-head bg-f6 clearfix">
            <div class="title">我的资料</div>
            <a href="#modifyInfoTemp" class="green_fontBtn fr">修改资料 ></a>
        </div>
        <div class="personalData-box">
            <ul>
                <li class="bg-white">所在学校：<span data-bind="text: $root.schoolName()"></span></li>
                <li>学校地址：<span data-bind="text: $root.schoolRegion()"></span></li>
            </ul>
        </div>
    </div>
    <div class="class-module mt-20">
        <div class="module-head bg-f6 clearfix">
            <div class="title">账号安全</div>
            <a href="#modifyAccountTemp" class="green_fontBtn fr">修改设置 ></a>
        </div>
        <div class="personalData-box">
            <ul>
                <li class="bg-white">密码：********</li>
                <li>手机号：<span data-bind="text: $root.mobile()"></span></li>
            </ul>
        </div>
    </div>
</script>

<#--修改我的资料-->
<script id="modifyInfoTemp" type="text/html">
    <div class="class-module mt-20">
        <div class="module-head bg-f6 clearfix">
            <div class="title">我的资料</div>
        </div>
        <div class="personalData-list">
            <dl>
                <dt>姓名：</dt>
                <dd>
                    <input type="text" class="txt JS-teacherName" placeholder="姓名" maxlength="10" value="${(currentUser.profile.realname)!}">
                    <span class="icon-arrow">（*必填）</span>
                </dd>
                <dt>所在学校：</dt>
                <dd>
                    <input type="text" class="txt JS-schoolName" disabled="disabled" data-bind="value: $root.schoolName" style="background-color: #e6e6e6;">
                    <span class="icon-arrow">（*必填）</span>
                    <p class="ped-info">学校信息不能随意修改，有问题请致电：400-160-1717</p>
                </dd>
                <#--<dt>所在地区：</dt>-->
                <#--<dd>-->
                    <#--<select class="sel">-->
                        <#--<option>北京</option>-->
                    <#--</select>-->
                    <#--<select class="sel">-->
                        <#--<option>北京市</option>-->
                    <#--</select>-->
                    <#--<select class="sel">-->
                        <#--<option>朝阳区</option>-->
                    <#--</select>-->
                    <#--<span class="icon-arrow">（*必填）</span>-->
                <#--</dd>-->
                <#--<dt>收货地址：</dt>-->
                <#--<dd>-->
                    <#--<span class="ped-area">北京市朝阳区</span><input type="text" class="txt width227" placeholder="北京市昌平区五中">-->
                    <#--<span class="icon-arrow">（*必填）</span>-->
                    <#--<p class="ped-info">学生兑换奖品收货地址</p>-->
                <#--</dd>-->
                <#--<dt>邮政编码：</dt>-->
                <#--<dd>-->
                    <#--<input type="text" class="txt">-->
                <#--</dd>-->
                <dt>联系电话：</dt>
                <dd>
                    <div class="telephone"><span data-bind="text: $root.mobile"></span>已经绑定手机号，<a href="javascript:void(0);" data-bind="click: $root.toChangeMobilePage.bind($data)">去更换</a></div>
                </dd>
                <#--<dt>配送方式：</dt>-->
                <#--<dd>-->
                    <#--<label class="radio-current"><input type="radio" class="rad"> 普通快递（市、县可到达）</label>-->
                    <#--<label class="radio-current"><input type="radio" class="rad"> 邮局（乡、镇、村可到达，寄送时间较长）</label>-->
                    <#--<p class="ped-info">学生兑换奖品配送方式</p>-->
                <#--</dd>-->
            </dl>
        </div>
    </div>
    <div class="personalData-foot"><a href="javascript:void(0);" class="green_btn" data-bind="click: $root.sureModifyInfo.bind($data)">确定</a></div>
</script>

<#--修改账号安全-->
<script id="modifyAccountTemp" type="text/html">
    <div class="class-module mt-20">
        <div class="module-head bg-f6 clearfix">
            <div class="title">账号安全</div>
        </div>
        <div class="accountSecurity-box">
            <div class="acs-list">
                <a href="javascript:void(0);" class="green_btn pad20 fr" data-bind="click: $root.modifySecret.bind($data)">修改密码</a>
                <div class="acs-image"><img src="<@app.link href='public/skin/specialteacher/images/personal/image01.png'/>"></div>
                <div class="acs-title">
                    <p class="t-1">登录密码：已设置<span class="icon-current"></span></p>
                    <p class="t-2">安全性高的密码，可以使账号更安全</p>
                </div>
            </div>
            <#--data-bind="visible: $root.isShowModifySecret"-->
            <div class="personalData-list JS-showModifySecret" style="display: none;">
                <dl>
                    <dt>验证手机：</dt>
                    <dd>
                        <div class="telephone"><span data-bind="text: mobile()"></span> <a href="javascript:void(0);" class="code_btn JS-getMobileCodeBtn1" data-bind="click: $root.getVerifiCode.bind($data, 1)">获取短信验证码</a></div>
                    </dd>
                    <dt>短信验证码：</dt>
                    <dd>
                        <input type="text" maxlength="4" class="txt JS-modifySecretCode">
                        <span class="icon-arrow">（*必填）</span>
                    </dd>
                    <dt>新的登录密码：</dt>
                    <dd>
                        <input type="password" maxlength="16" class="txt JS-newLoginSecret1" placeholder="请输入1-16位任意字符，字母区分大小写">
                        <span class="icon-arrow">（*必填）</span>
                    </dd>
                    <dt>再次输入新密码：</dt>
                    <dd>
                        <input type="password" maxlength="16" class="txt JS-newLoginSecret2" placeholder="请输入1-16位任意字符，字母区分大小写">
                        <span class="icon-arrow">（*必填）</span>
                    </dd>
                </dl>
                <div class="module-foot"><a href="javascript:void(0);" class="btn gray_btn" data-bind="click: modifySecret.bind($data)">取消</a><a href="javascript:;" class="btn" data-bind="click: $root.modifyLoginSecret.bind($data)">确定</a></div>
            </div>
        </div>
        <div class="accountSecurity-box">
            <div class="acs-list">
                <a href="javascript:void(0);" class="green_btn pad20 fr" data-bind="click: $root.changeMobile.bind($data)">更换手机</a>
                <div class="acs-image"><img src="<@app.link href='public/skin/specialteacher/images/personal/image02.png'/>"></div>
                <div class="acs-title">
                    <p class="t-1">手机绑定：已设置<span class="icon-current"></span></p>
                    <p class="t-2">绑定手机后，您即可享受手机登录、手机找回密码等服务。</p>
                </div>
            </div>
            <#--data-bind="visible: $root.isShowModifyMobile"-->
            <div class="acs-main JS-ShowModifyMobile" style="display: none;">
                <div class="acs-head">原号码：<span data-bind="text: $root.mobile()"></span>更换后，原号码不能再作为登录使用</div>
                <div class="personalData-list">
                    <dl>
                        <dt>新手机号码：</dt>
                        <dd>
                            <input type="text" maxlength="11" class="txt JS-newMobile" placeholder="请输入手机号，可用于登录和找回密码">
                            <span class="icon-arrow">（*必填）</span>
                            <p class="acs-code"><a href="javascript:void(0);" class="code_btn JS-getMobileCodeBtn2" data-bind="click: $root.getVerifiCode.bind($data, 2)">获取短信验证码</a></p>
                        </dd>
                        <dt>短信验证码：</dt>
                        <dd>
                            <input type="text" maxlength="4" class="txt JS-modifyMobileCode">
                            <span class="icon-arrow">（*必填）</span>
                        </dd>
                    </dl>
                    <div class="module-foot"><a href="javascript:void(0);" class="btn gray_btn" data-bind="click: changeMobile.bind($data)">取消</a><a href="javascript:void(0);" class="btn" data-bind="click: $root.modifyMobile.bind($data)">确定</a></div>
                </div>
            </div>
        </div>
    </div>
</script>

<#--消息中心-->
<script id="messageCenter" type="text/html">
    <div class="class-module mt-20">
        <div class="module-head bg-f6 clearfix">
            <div class="title">消息中心</div>
        </div>
        <div class="accountSecurity-box messageList">
            <div class="acs-list">
                <div class="listRight fr">
                    <a href="javascript:;" class="green_fontBtn blue_fontBtn fr" data-bind="click: $root.delMessage.bind($data)">删除</a>
                    <p class="t-3">2016/11/20  08:42:57</p>
                </div>
                <div class="acs-image"><img src="<@app.link href='public/skin/specialteacher/images/personal/image07.png'/>"></div>
                <div class="acs-title">
                    <p class="t-1">系统消息</p>
                    <p class="t-2">望天同学于2016年11月24日加入了七年级3班；不想新学生加入班级？<a href="javascript:void(0);" class="to-look">点击查看>></a></p>
                </div>
            </div>
        </div>
        <div class="module-page fr">
            <a href="javascript:;" class="prev"><上一页 </a>
            <span class="active">1</span><span>2</span><span>3</span><span>4</span><span>5</span>
            <a href="javascript:;" class="next">下一页></a>
        </div>
    </div>
</script>