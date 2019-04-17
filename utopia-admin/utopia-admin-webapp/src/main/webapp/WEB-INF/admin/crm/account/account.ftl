<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="搜索帐户" page_num=3>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<div class="span9">
    <div id="message" style="display:none;">
        <div id="msgSuc" class="alert alert-success" style="display:none;"></div>
        <div id="msgErr" class="alert alert-error" style="display:none;"></div>
    </div>

    <div class="span9">
        <form action="index.vpage" method="post">
            <fieldset>
                <legend>搜索教研员帐户</legend>
            </fieldset>

            <ul class="inline">
                <li>
                    教研员ID
                    <input id="id" name="id" type="text"/>
                </li>
                <li>
                    手机
                    <input id="mobile" name="mobile" type="text"/>
                </li>
                <input type="hidden" name="userType" id="userType" value="8"/>
            </ul>
            <ul class="inline">
                <li>
                    <input type="submit" value="查询" class="btn btn-primary" id="searchSubmit"/>
                    <input type="hidden" id="currentPage" name="currentPage" value="1" />
                    <input type="hidden" id="totalPage" name="totalPage" value="${totalPage!1}" />
                </li>
            </ul>
        </form>
    </div>

    <div  class="span9">
        <div>
            <ul class="inline">
                <li><input id="btnModifyUserInfo" type="button" class="btn" value="修改帐号信息" /></li>
                <li><input id="btnManageRegion" type="button" class="btn" value="权限管理" /></li>
                <li><input id="btnAddAccount" type="button" class="btn" value="添加帐号" /></li>
            </ul>

        </div>
        <table class="table table-striped table-bordered" id="students">
            <tr>
                <th>选择</th>
                <th>用户ID</th>
                <th>姓名</th>
                <th>注册时间</th>
                <th>手机</th>
                <th>用户类型</th>
                <th>可用</th>
            </tr>
            <#if users??>
                <#list users as user>
                    <tr>
                        <td>
                            <input type="radio" name="user" id="${user.id!""}" ut="${user.userType!""}" />
                        </td>
                        <td>${user.id!""}</td>
                        <td>${user.realName!""}</td>
                        <td>${user.createTime!""}</td>
                        <td>${user.mobile!""}</td>
                        <td>${user.userType!""}</td>
                        <td>${user.disabled?string('不可用','可用')}</td>
                    </tr>
                </#list>
            </#if>
        </table>
        <ul class="inline">
            <li>
                <a id='first_page' href="javascript:void(0)">首页</a>
            </li>
            <li>
                <a id='pre_page' href="javascript:void(0)">上一页</a>
            </li>
            <li>
                <a><#if conditions ??>${conditions.currentPage!"1"}<#else >1</#if>/${totalPage!"1"}</a>
            </li>
            <li>
                <a id='next_page' href="javascript:void(0)">下一页</a>
            </li>
            <li>
                <a id='last_page' href="javascript:void(0)">末页</a>
            </li>
        </ul>
        <div id="modifyUserInfoModal" class="modal hide fade">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3>修改帐号信息</h3>
            </div>
            <div id="mMessage" style="display:none;">
                <div id="mMsgErr" class="alert alert-error"></div>
            </div>
            <div class="modal-body">
                <dl class="dl-horizontal">
                    <ul class="inline">
                        <li>
                            <dt>姓名</dt>
                            <dd><input id="mrealName" type='text' required="true"/></dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>手机</dt>
                            <dd><input id="mmobile" type="text" /></dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>邮箱</dt>
                            <dd><input id="memail" type="text" /></dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>是否认证</dt>
                            <dd>
                                <select id="mauthState">
                                    <option value="0">等待认证</option>
                                    <option value="1">已认证</option>
                                    <option value="2">信息不全</option>
                                    <option value="3">未通过</option>
                                </select>
                            </dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>可用</dt>
                            <dd>
                                <select id="mdisable">
                                    <option value="0">是</option>
                                    <option value="1">否</option>
                                </select>
                            </dd>
                        </li>
                    </ul>
                </dl>
            </div>
            <div class="modal-footer">
                <button id="submitUserInfo" class="btn btn-primary">确 定</button>
                <button  class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
            </div>
        </div>
        <div id="regionManageModal" class="modal hide fade">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3>数据权限管理</h3>
            </div>
            <div id="rMessage" style="display:none;">
                <div id="rMsgErr" class="alert alert-error"></div>
            </div>
            <div class="modal-body">
                <dl class="dl-horizontal">
                    <ul class="inline">
                        <li>
                            <label>
                                所在省：<select name="provinceCode" data-next_level="cityCode" >
                                <option value="-1">全国</option>
                                <#if provinces?has_content>
                                    <#list provinces as province>
                                        <option value="${province.code}">${province.name}</option>
                                    </#list>
                                </#if>
                            </select>
                                <input id="btnAddProvince" type="button" class="btn" value="添加" />
                            </label>
                        </li>
                        <li>
                            <label>
                                所在市：<select name="cityCode" data-init="false" data-next_level="countryCode">
                                <option value="-1">全部</option>
                            </select>
                                <input id="btnAddCity" type="button" class="btn" value="添加" />
                            </label>
                        </li>
                        <li>
                            <label>
                                所在区：<select name="countryCode" data-init="false" data-next_level="schoolId">
                                <option value="-1">全部</option>
                            </select>
                                <input id="btnAddDistrict" type="button" class="btn" value="添加" />
                            </label>
                        </li>
                        <li>
                            <label>
                                学&nbsp;&nbsp;&nbsp;&nbsp;校：<select name="schoolId" data-init="false" >
                                <option value="-1">全部</option>
                            </select>
                                <input id="btnAddSchool" type="button" class="btn" value="添加" />
                            </label>
                        </li>
                        <li>
                            <label>
                                <input id="schoolMasterSelect" type="checkbox" /> 设置为校长账号(仅仅用于用户类型为教研员)
                            </label>
                        </li>
                    </ul>
                </dl>
                <fieldset>
                    <legend>已选择：</legend>
                    <div id="selectedRegions" class="inline"></div>
                </fieldset>
            </div>
            <div class="modal-footer">
                <button id="submitRegion" class="btn btn-primary">确 定</button>
                <button  class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
            </div>
        </div>
        <div id="addManagerModal" class="modal hide fade">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3>添加帐号</h3>
            </div>
            <div id="uMessage" style="display:none;">
                <div id="uMsgErr" class="alert alert-error"></div>
            </div>
            <div class="modal-body">
                <dl class="dl-horizontal">
                    <ul class="inline">
                        <li>
                            <dt>角色</dt>
                            <dd>
                                <select id="selectRoleType" name="selectRoleType">
                                    <#--<option value="7">超级管理员</option>-->
                                    <#--<option value ="9">市场人员</option>-->
                                    <option value ="10">教研员</option>
                                    <#--<option value ="11">临时员工(试题录入)</option>-->
                                </select>
                            </dd>
                        </li>
                        <li>
                            <dt>密码</dt>
                            <dd>
                                <input type="password" id="password" name="password" required="true" />
                            </dd>
                        </li>
                        <li>
                            <dt>验证密码</dt>
                            <dd>
                                <input type="password" id="confirmpwd" name="confirmpwd" required="true"/>
                            </dd>
                        </li>
                        <li>
                            <dt>真实姓名</dt>
                            <dd>
                                <input type="text" id="realname" name="realname"  required="true"/>
                            </dd>
                        </li>
                        <li id="ktwelveText">
                            <dt>学段</dt>
                            <dd>
                                <select type="text" id="ktwelve" name="ktwelve">
                                    <option value="PRIMARY_SCHOOL">小学</option>
                                    <option value ="JUNIOR_SCHOOL">中学</option>
                                    <option value ="SENIOR_SCHOOL">高中</option>
                                </select>
                            </dd>
                        </li>
                        <li id="subjectText">
                            <dt>学科</dt>
                            <dd>
                                <select type="text" id="subject" name="subject">
                                    <option value="ENGLISH">英语</option>
                                    <option value ="MATH">数学</option>
                                    <option value ="CHINESE">语文</option>
                                </select>
                                <select type="text" id="subject" name="subject" style="display: none;">
                                    <option value="ENGLISH">英语</option>
                                    <option value ="MATH">数学</option>
                                    <option value ="CHINESE">语文</option>
                                    <option value ="PHYSICS">物理</option>
                                    <option value ="CHEMISTRY">化学</option>
                                    <option value ="BIOLOGY">生物</option>
                                    <option value ="HISTORY">历史</option>
                                    <option value ="GEOGRAPHY">地理</option>
                                    <option value ="POLITICS">政治</option>
                                    <option value ="INFORMATION">信息</option>
                                </select>
                            </dd>
                        </li>
                        <li>
                            <dt>电话</dt>
                            <dd>
                                <input type="text" id="rmobile" name="rmobile" />
                            </dd>
                        </li>
                    </ul>
                </dl>
            </div>
            <div class="modal-footer">
                <button id="submitUser" class="btn btn-primary">确 定</button>
                <button  class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">

    //帐号查询与信息修改
    var Account ={
        totalPage : $('#totalPage'),

        //修改账号信息参数
        mrealName : $('#mrealName'),
        memail : $('#memail'),
        mmobile : $('#mmobile'),
        mauthState : $('#mauthState'),
        mdisable : $('#mdisable'),

        //分页导航
        firstPage : $('#first_page'),
        prePage : $('#pre_page'),
        nextPage : $('#next_page'),
        lastPage : $('#last_page'),

    conditions : <#if conditions??>   //保存搜索条件用于重新渲染页面
            {
                id : '${conditions.id!""}',
                realName : '${conditions.realName!""}',
                email : '${conditions.email!""}',
                mobile : '${conditions.mobile!""}',
                userType : '${conditions.user_Type!""}',
                createTime : '${conditions.createTime!""}',
                currentPage : ${conditions.currentPage!"1"}
            }
    <#else>
    {}
    </#if>,

    init : function(){
        var me =Account;

        //分页导航事件
        $(me.firstPage).click(function(){
            if(me.conditions.currentPage == 1){
                return;
            }
            me.conditions.currentPage=1;
            me.render(me.conditions);
        });
        $(me.prePage).click(function(){
            if(me.conditions.currentPage == 1) {
                return false;
            }
            me.conditions.currentPage -=1;
            me.render(me.conditions);
        });
        $(me.nextPage).click(function(){
            if(me.conditions.currentPage == me.totalPage.val()){
                return false;
            }
            me.conditions.currentPage += 1;
            me.render(me.conditions);
        });
        $(me.lastPage).on('click',function(){
            if(me.totalPage.val() == '1' || me.conditions.currentPage == me.totalPage.val()){
                return;
            }
            me.conditions.currentPage = me.totalPage.val();
            me.render(me.conditions);
        });
        //修改帐号信息相关事件
        $('#btnModifyUserInfo').click(function(){
            var userId = me.getSelectedUser();
            me.renderModifyWin(userId);
        });
        $('#submitUserInfo').click(function(){
            me.submitModify();
        });
    },
    render :function(conditions){ //提交页面，重新渲染页面列表
        $('#id').val(conditions.id);
        $('#realName').val(conditions.realName);
        $('#email').val(conditions.email);
        $('#mobile').val(conditions.mobile);
        $('#userType').val(conditions.userType);
        $('#createTime').val(conditions.createTime);
        if (conditions.currentPage) {
            $('#currentPage').val(conditions.currentPage);
        }

        $('#searchSubmit').trigger('click');
    },
    renderModifyWin:function(userId){ //显示修改帐号弹窗
        var me = Account;
        if(null == userId){
            me.showMsg('请选择要修改的用户！',true);
            return;
        }
        $('#mMessage').hide();
        $.get('getuserinfo.vpage',{userId : userId},
                function(data){
                    if(data.success == false){
                        me.showMsg(data.info,true);
                    }else{
                        var dialog = $('#modifyUserInfoModal'); //显示弹窗前将帐号信息填充进去
                        dialog.find(me.mrealName).val(data.user.profile.realname);
                        dialog.find(me.memail).val(data.user.profile.sensitiveEmail);
                        dialog.find(me.mmobile).val(data.user.profile.sensitiveMobile);
                        dialog.find(me.mauthState).val(data.user.authenticationState);
                        dialog.find(me.mdisable).val(data.user.disabled?1:0);
                        dialog.modal('show');
                    }
                }
        );

    },
    submitModify:function(){ //提交修改后的帐号信息
        var me = Account;
        if(!me.modifyCheck()){
            return false;
        }
        $.post('updateuserinfo.vpage',
                {
                    id:me.getSelectedUser(),
                    realName : me.mrealName.val(),
                    mobile : me.mmobile.val()
                },
                function(data){
                    if(data.success == false){
                        me.showMsg(data.info,true);
                        var dialog = $('#modifyUserInfoModal');
                        dialog.modal('hide');
                    }else{
                        me.render(me.conditions);  //更新成功后会按原来的搜索条件再次渲染页面，如果修改后的记录已不符合搜索条件，则新搜索结果不会包括修改后的记录
                    }
                }
        );
    },
    getSelectedUser:function(){  //取被选中的用户id
        var userId = null;
        $('input[name="user"]').each(function(i){
            if($(this).is(':checked')){
                userId = $(this).attr('id');
            }
        });
        return userId;
    },
    getUserMaxAuthorityRegionCount:function(){ //取被选中的用户可设置的地区数量，0不可设置，1可设置一个，2可设置多个不限数量
        var userType = '';
        $('input[name="user"]').each(function(i){
            if($(this).is(':checked')){
                userType = $(this).attr('ut');
            }
        });
        if(userType =='市场人员' || userType == '管理员'){
            return 2;
        }else if(userType == '教研员'){
            return 2;
        }else{
            return 0;
        }
    },
    modifyCheck:function(){
        var me = Account;
        if(me.mrealName.val().trim() == ''){
            $('#mMsgErr').html('请输入帐号姓名！');
            $('#mMessage').show();
            return false;
        }
        //verify email
        var rexEmail =/^[a-z]([\.]?[a-z0-9]*[-_]?[a-z0-9]+)*@([a-z0-9]*[-_]?[a-z0-9]+)+[\.][a-z]{2,3}([\.][a-z]{2})?$/;
        if(me.memail.val().trim() != '' && !rexEmail.test(me.memail.val())){
            $('#mMsgErr').html('请输入正确的email！');
            $('#mMessage').show();
            return false;
        }
        //verify mobile 与主站其它绑定手机规则一致，1开头，11位即可。
        var rexMObile = /^0?1[0-9]{10}$/;
        if(me.mmobile.val().trim() != '' && !rexMObile.test(me.mmobile.val())){
            $('#mMsgErr').html('请输入正确的手机号！');
            $('#mMessage').show();
            return false;
        }
        return true;
    },
    showMsg:function(msg,iserr){
        if(iserr){
            $('#msgErr').html(msg);
            $('#msgErr').show();
            $('#msgSuc').hide();
        }else{
            $('#msgSuc').html(msg);
            $('#msgSuc').show();
            $('#msgErr').hide();
        }
        $('#message').show();
    }
    };

    //帐号权限管理
    var RegionManage ={
        init:function(){
            $('#btnManageRegion').click(function(){
                var me = RegionManage;
                me.renderRegionModal(Account.getSelectedUser());
            });

            $('select[data-next_level]').on('change', function() {
                var me = RegionManage;
                me.regionChange(this);
            });
            $('#btnAddProvince').click(function(){
                var me = RegionManage;
                var provinceCode =$('select[name="provinceCode"]').val();
                var provinceName =$('select[name="provinceCode"]').find('option:selected').text();
                me.addNewRegion(provinceCode,provinceName,"PROVINCE");
            });
            $('#btnAddCity').click(function(){
                var me = RegionManage;
                var cityCode=$('select[name="cityCode"]').val();
                var cityName = $('select[name="cityCode"]').find('option:selected').text();
                me.addNewRegion(cityCode,cityName,"CITY");
            });
            $('#btnAddDistrict').click(function(){
                var me = RegionManage;
                var districtCode = $('select[name="countryCode"]').val();
                var districtName = $('select[name="countryCode"]').find('option:selected').text();
                me.addNewRegion(districtCode,districtName,"COUNTY");
            });
            $('#btnAddSchool').click(function(){
                var me = RegionManage;
                var schoolId = $('select[name="schoolId"]').val();
                var schoolName = $('select[name="schoolId"]').find('option:selected').text();
                me.addNewRegion(schoolId,schoolName,"SCHOOL");
            });
            $('#submitRegion').click(function(){
                var me = RegionManage;
                me.submitSelectedRegion(Account.getSelectedUser());
            });
        },
        renderRegionModal:function(userId){ //显示权限管理弹窗
            $('#rMsgErr').html('');
            $('#rMessage').hide();
            $('#selectedRegions').html('');

            if(null == userId){
                Account.showMsg('请选择要管理的用户！',true);
                return false;
            }
            if(Account.getUserMaxAuthorityRegionCount() ==0){ //用户无需设置地区权限
                Account.showMsg('此用户不可设置权限',true);
                return false;
            }

            var me = RegionManage;
            $.get('getuserauthorityregion.vpage',{userId:userId},function(data){
                if(data.success == true){
                    var regionMappers = data.regions;
                    $('#selectedRegions').html('');
                    if(regionMappers.length !=0){
                        for(var i =0;i<regionMappers.length;i++){
                            me.appendRegion(regionMappers[i].regionCodes,regionMappers[i].regionNames,regionMappers[i].regionType);
                        }
                    }
                    var dialog = $('#regionManageModal');
                    dialog.modal('show');
                }else{
                    Account.showMsg(data.info,true);
                }
            });

        },
        regionChange:function(obj){
            var $this = $(obj);
            var nextLevel = $this.data('next_level');
            var $nextLevel = $('select[name="' + $this.data('next_level') + '"]');
            if($this.val() < 0) {
                return;
            }
            if(nextLevel != "schoolId") {
                $.get('../user/regionlist.vpage?regionCode=' + $this.val(), function(data) {
                    //noinspection JSUnresolvedVariable
                    var regionList = data.regionList;
                    var regionsStr = '';
                    if(regionList.length == 0) {
                        regionsStr = '<option value="-1">全部</option>';
                    } else {
                        for(var i = 0; i < regionList.length; i++) {
                            regionsStr += '<option value=' + regionList[i]['code'] + '>' + regionList[i]['name'] + '</option>';
                        }
                    }

                    if($nextLevel) {
                        $nextLevel.html(regionsStr);
                        if(!$nextLevel.data('init')) {
                            $nextLevel.val($nextLevel.data('default'));
                            $nextLevel.data('init', true);
                        }
                    }
                    $nextLevel.trigger('change');
                });
            } else {
                $.get('../user/schoollist.vpage?regionCode=' + $this.val(), function(data) {
                    var schoolList = data.schoolList;
                    var schoolsStr = '';

                    if(schoolList.length == 0) {
                        schoolsStr = '<option value="-1">全部</option>';
                    } else {
                        for(var i = 0; i < schoolList.length; i++) {
                            schoolsStr += '<option value=' + schoolList[i]['id'] + '>' + schoolList[i]['cname']+'('+schoolList[i]['id']+')' + '</option>';
                        }
                    }
                    if($nextLevel) {
                        $nextLevel.html(schoolsStr);
                        if(!$nextLevel.data('init')) {
                            $nextLevel.val($nextLevel.data('default'));
                            $nextLevel.data('init', true);
                        }
                    }
                    $nextLevel.trigger('change');
                    $nextLevel.chosen();
                    $nextLevel.trigger("chosen:updated");
                });
            }
        },
        addNewRegion:function(code,name,type){ //向已选择区添加新的地区
            var me = RegionManage;
            if(code != '-1'){
                var regions = me.getSelectedRegions();
                var rcount = Account.getUserMaxAuthorityRegionCount();
                if(rcount == 0){ //用户无需设置地区权限
                    return false;
                }else if(rcount == 1 && regions.length == rcount){ //用户只能设置一个地区权限，且已被设置
                    $('#rMsgErr').html('只能选择一个地区！');
                    $('#rMessage').show();
                    return false;
                }else{
                    $('#rMessage').hide();
                }
                for(var i=0;i<regions.length;i++){
                    if(regions[i] == code){
                        return false;
                    }
                }
                me.appendRegion(code,name,type);
            }
        },
        appendRegion:function(code,name,type){
            $('#selectedRegions').append('&nbsp;<label class=\"label\" id=\"'+code+'\" data-type=\"'+type+'\" name=\"selectedRegion\">'+name+'</label>');
            $('#selectedRegions').append('<label class=\"label control label-warning\" id=\"close'+code+'\" >X</label>');
            $('#close'+code).click(function(){
                $(this).prev().remove();
                $(this).remove();
            });
        },
        getSelectedRegions:function(){ //获取已设置的地区
            var regions =[];
            $('label[name="selectedRegion"]').each(function(i){
                regions[i] = $(this).attr('id');
            });
            return regions;
        },
        getSelectedRegionTypes:function(){
            var regionTypes =[];
            $('label[name="selectedRegion"]').each(function(i){
                regionTypes[i] = $(this).data('type');
            });
            return regionTypes;
        },
        submitSelectedRegion:function(userId){ //提交权限设置
            var me = RegionManage;
            var regions = me.getSelectedRegions();
            var regionTypes = me.getSelectedRegionTypes();
            if(regions.length == 0){
                return;
            }

            var isSchoolMaster = false;
            var userType = null;
            if($('#schoolMasterSelect').is(':checked')){
                userType = $('#'+userId).attr('ut');
            }
            var reminderInfo =  "提示\n\n请确定该用户是教研员,并把该用户("+userId+")设置为校长!!\n"
            if (userType == '教研员' ) { //是教研员
                if(window.confirm(reminderInfo)){
                    isSchoolMaster = true;   //条件:schoolMasterSelect被选中,userType是教研员,提示弹窗确定时
                    if(regions.length != 1){
                        reminderInfo =  "提示\n\n该用户("+userId+")添加的学校必须是1个!!\n"
                        alert(reminderInfo);
                        return;
                    }
                }else{
                    return;
                }
            }
            $.post('updateuserauthorityregion.vpage',
                    {
                        setSchoolMaster:isSchoolMaster,
                        userId:userId,
                        regions:'['+regions+']',
                        regionTypes:'['+regionTypes+']'
                    },
                    function(data){
                        if(data.success == false){
                            $('#rMsgErr').html(data.info);
                            $('#rMessage').show();
                        }else{
                            $('#regionManageModal').modal('hide');
                        }
                    });
        }
    };

    var UserRegister={
        init:function(){
            var me =UserRegister;
            $('#btnAddAccount').click(function(){
                $('#uMessage').hide();
                var dialog = $('#addManagerModal');
                dialog.modal('show');
            });
            $('#submitUser').click(function(){
                if(!me.accountCheck()){
                    return;
                }
                me.register();
            });

            $("#selectRoleType").change(function(){
                var roleVal = $(this).val();
                var $subject = $("#subjectText");
                var $ktwelve = $("#ktwelveText");
                if(roleVal == "10"){
                    $subject.show();
                    $ktwelve.show();
                }else{
                    $subject.hide();
                    $ktwelve.hide();
                }
            });

            $("#ktwelveText").change(function () {
                var $this = $(this).find('select').val();
                if ($this == 'PRIMARY_SCHOOL') { // 小学
                    $('#subjectText').find('select').eq(0).show();
                    $('#subjectText').find('select').eq(1).hide();
                } else {
                    $('#subjectText').find('select').eq(0).hide();
                    $('#subjectText').find('select').eq(1).show();
                }
            })
        },
        register:function(){  //注册帐号
            $.post('register.vpage',
                    {
                        roleType:$('#selectRoleType').find('option:selected').val(),
                        password:$('#password').val(),
                        realname:$('#realname').val(),
                        subject :$("#subject").find('option:selected').val(),
                        ktwelve :$("#ktwelve").find('option:selected').val(),
                        mobile:$("#rmobile").val()
                    },
                    function(data){
                        if(data.success == true){
                            var dialog = $('#addManagerModal');
                            dialog.modal('hide');
                            Account.render({id:data.userId,realName : '',email : '',mobile : '',authState : '',disable : ''});
                        }else{
                            $('#uMessage').show();
                            $('#uMsgErr').html(data.info);
                        }
                    });
        },
        accountCheck:function(){
            if($('#password').val()==''){
                $('#uMsgErr').html('请输入密码！');
                $('#uMessage').show();
                return false;
            }
            if($('#confirmpwd').val() == ''){
                $('#uMsgErr').html('请输入验证密码！');
                $('#uMessage').show();
                return false;
            }
            if($('#password').val() != $('#confirmpwd').val()){
                $('#uMsgErr').html('两次输入的密码不一致！');
                $('#uMessage').show();
                return false;
            }
            if($('#realname').val() == ''){
                $('#uMsgErr').html('请输入真实姓名！');
                $('#uMessage').show();
                return false;
            }

            alert($('#rmobile').val());
            if($('#rmobile').val() != ''){
                var rexMObile = /^0?1[0-9]{10}$/;
                if($('#rmobile').val().trim() != '' && !rexMObile.test($('#rmobile').val())){
                    $('#uMsgErr').html('请输入正确的手机号！');
                    $('#uMessage').show();
                    return false;
                }
            }
            return true;
        }
    };

    $(function(){
        Account.init();
        RegionManage.init();
        UserRegister.init();


    });
</script>

</@layout_default.page>