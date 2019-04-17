<#--个人中心首页模板-->
<script id="centerIndexTemp" type="text/html">
    <div class="class-content">
        <!-- 头像 -->
        <div class="personalData-head">
            <div class="ped-image">
                <img src="<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>">
                <#--http://img06.tooopen.com/images/20161112/tooopen_sl_185726827722.jpg-->
            </div>
            <div class="ped-name">${(currentUser.profile.realname)!}</div>
        </div>
        <!-- 我的资料 -->
        <div class="class-module">
            <div class="module-head clearfix">
                <div class="title">我的资料</div>
                <a href="javascript:;" class="fontBtn fr" data-bind="click: jumpToInfomation">修改资料</a>
            </div>
            <div class="personalData-box">
                <ul>
                    <#if idType == 'schoolmaster'>
                    <li class="bg-white"><span>所在学校：</span><!-- ko text: schoolName --><!-- /ko --></li>
                    </#if>
                    <li><span><#if idType == 'schoolmaster'>学校地址<#else>管辖区域</#if>：</span><!-- ko text: reginonAddress --><!-- /ko --></li>

                </ul>
            </div>
        </div>
        <!-- 账号安全 -->
        <div class="class-module">
            <div class="module-head clearfix">
                <div class="title">账号安全</div>
                <a href="javascript:;" class="fontBtn fr" data-bind="click: jumpToAccountSafe.bind($data, 0)">修改设置</a>
            </div>
            <div class="personalData-box">
                <ul>
                    <li class="bg-white"><span>密码：</span>********</li>
                    <li style="display:none;" data-bind="visible: isShowMobileModifySecret"><span>手机号：</span><!-- ko text: phoneNumber --><!-- /ko --></li>
                </ul>
            </div>
        </div>
    </div>
</script>

<#--我的资料模板-->
<script id="informationTemp" type="text/html">
    <div class="class-content edit-content">
        <div class="class-module ">
            <div class="module-head clearfix">
                <div class="title">编辑我的资料</div>
            </div>
            <div class="personalData-list edit-dataList">
                <ul>
                    <li>
                        <span>姓名：</span>
                        <div class="infoBox">
                            <input type="text" class="txt" placeholder="姓名" data-bind="value: modifyNameInputName">
                            <span class="icon-arrow">（*必填）</span>
                        </div>
                    </li>
                    <#if idType == 'schoolmaster'>
                    <li>
                        <span class="schoolName">所在学校：</span>
                        <div class="infoBox">
                            <input type="text" class="txt disabled" disabled="disabled" placeholder="北京市昌平区五中" data-bind="text: schoolName">
                            <span class="icon-arrow">（*必填）</span>
                            <p class="ped-info">学校信息不能随意修改，有问题请致电：400-160-1717</p>
                        </div>
                    </li>
                    </#if>
                    <li style="display: none;" data-bind="visible: isShowMobileModifySecret">
                        <span>联系电话：</span>
                        <div class="infoBox">
                            <div class="telephone"><!-- ko text: phoneNumber --><!-- /ko -->已经绑定手机号，<a href="javascript:void(0);" class="goChangeBtn" data-bind="click: jumpToAccountSafe.bind($data, 2)">去更换</a></div>
                        </div>
                    </li>
                </ul>
                <a href="javascript:void(0);" class="surebtn" data-bind="click: sureModifyInfo">确定</div>
            </div>
        </div>
    </div>
</script>

<#--账号安全模块-->
<script id="accountSafeTemp" type="text/html">
    <div class="class-content edit-content">
        <div class="class-module">
            <div class="module-head clearfix">
                <div class="title">设置账号</div>
            </div>
            <!-- 修改密码 -->
            <div class="accountSecurity-box" style="display: none;" data-bind="visible: isShowMobileModifySecret">
                <div class="acs-list">
                    <a href="javascript:;" class="setBtn fr" data-bind="click: showModifySecretModule">修改密码</a>
                    <div class="acs-title">
                        <p class="t-1">登录密码：已设置<span class="icon-current"></span></p>
                        <p class="t-2">安全性高的密码，可以使账号更安全</p>
                    </div>
                </div>
                <!-- 表单 -->
                <div class="personalData-list dataList" id="showModifySecret" style="display: none;">
                    <ul>
                        <li>
                            <span>验证手机：</span>
                            <div class="infoBox">
                                <div class="telephone"><!-- ko text: phoneNumber --><!-- /ko --><a href="javascript:;" class="code_btn" data-bind="
                                    text: modifySecretCodeBtnText,
                                    css: {'disabled' : isDisabledModifySecretCodeBtn},
                                    click: modifySecretGetCode"></a></div>
                            </div>
                        </li>
                        <li>
                            <span >短信验证码：</span>
                            <div class="infoBox">
                                <input type="text" class="txt" placeholder="请输入短信验证码" maxlength="6" data-bind="value: modifySecretInputCode">
                                <span class="icon-arrow">（*必填）</span>
                            </div>
                        </li>
                        <li>
                            <span>新的登录密码：</span>
                            <div class="infoBox">
                                <input type="password" class="txt" placeholder="请输入6~16位数字和字母组合的字符，区分大小写" maxlength="16" data-bind="value: modifySecretInputSecret1">
                                <span class="icon-arrow">（*必填）</span>
                            </div>
                        </li>
                        <li>
                            <span>再次输入新密码：</span>
                            <div class="infoBox">
                                <input type="password" class="txt" placeholder="请输入6~16位数字和字母组合的字符，区分大小写" maxlength="16" data-bind="value: modifySecretInputSecret2">
                                <span class="icon-arrow">（*必填）</span>
                            </div>
                        </li>
                    </ul>
                    <div class="module-foot">
                        <a href="javascript:;" class="btn" data-bind="click: showModifySecretModule">取消</a>
                        <a href="javascript:;" class="btn active" data-bind="click: sureModifySecret">确认</a>
                    </div>
                </div>
            </div>
            <!-- 更换手机 -->
            <div class="accountSecurity-box">
                <div class="acs-list acs-list02">
                    <a href="javascript:;" class="setBtn fr" data-bind="click: showModifyMobileModule">
                        <!-- ko if: isShowMobileModifySecret -->更换手机<!-- /ko -->
                        <!-- ko ifnot: isShowMobileModifySecret -->绑定手机<!-- /ko -->
                    </a>
                    <div class="acs-title">
                        <p class="t-1">手机绑定：<!-- ko if: isShowMobileModifySecret -->已设置<!-- /ko --><!-- ko ifnot: isShowMobileModifySecret -->未设置<!-- /ko --><span class="icon-current" data-bind="css: {'error': !isShowMobileModifySecret()}"></span></p>
                        <p class="t-2">绑定手机后，您即可享受手机登录、手机找回密码等服务。</p>
                    </div>
                </div>
                <!-- 表单 -->
                <div class="acs-main" id="showModifyMobile" style="display: none;">
                    <div class="acs-head" style="display: none;" data-bind="visible: isShowMobileModifySecret">原号码：<!-- ko text: phoneNumber --><!-- /ko -->更换后，原号码不能再作为登录使用</div>
                    <div class="personalData-list dataList dataList02">
                        <ul>
                            <li>
                                <span><!-- ko if: isShowMobileModifySecret -->新<!-- /ko -->手机号码：</span>
                                <div class="infoBox">
                                    <input type="text" class="txt" placeholder="请输入手机号，可用于登录和找回密码" maxlength="11" data-bind="value: modifyMobileInputMobile">
                                    <span class="icon-arrow">（*必填）</span>
                                </div>
                                <div class="codeBox">
                                    <a href="javascript:;" class="code_btn disabled" data-bind="
                                        text: modifyMobileCodeBtnText,
                                        css: {'disabled' : isDisabledModifyMobileCodeBtn},
                                        click: modifyMobileGetCode">获取短信验证码</a>
                                </div>
                            </li>
                            <li>
                                <span>短信验证码：</span>
                                <div class="infoBox">
                                    <input type="text" class="txt" placeholder="请输入短信验证码" maxlength="6" data-bind="value: modifyMobileInputCode">
                                    <span class="icon-arrow">（*必填）</span>
                                </div>
                            </li>
                        </ul>
                        <div class="module-foot">
                            <a href="javascript:;" class="btn" data-bind="click: showModifyMobileModule">取消</a>
                            <a href="javascript:;" class="btn active" data-bind="click: sureModifyMobile">确认</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>