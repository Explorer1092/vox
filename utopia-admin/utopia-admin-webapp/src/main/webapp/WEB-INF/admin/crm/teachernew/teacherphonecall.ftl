<script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
<div>
    <input id="cmdDial" type="button" value="外拨" onclick="return cmdDial_onclick()"  />
    <div id='softphonediv' style="width: 0em;height: 0em;"></div>
    <input id="txtPhoneNum" type="hidden" value="<#if teacherSummary??&&teacherSummary.sensitiveMobile??>${teacherSummary.sensitiveMobile!}</#if>"/>
    <input id="agentId" type="hidden" value="${adminUser.ccAgentId!''}"/>
    <input id="agentName" type="hidden" value="${adminUser.adminUserName!''}"/>
</div>

<SCRIPT ID=clientcode LANGUAGE=javascript>
    function cmdSetParam_onclick() {
        var phone = document.getElementById("txtPhoneNum").value;
        var agentId =document.getElementById("agentId").value;
        var agentName =document.getElementById("agentName").value;
        if(phone ==""){
            alert("老师电话不存在");
            return false;
        }
        if(agentId ==""){
            alert("客服CCAgentId错误，请刷新重试");
            return false;
        }

        //如果需要，可以根据业务系统的登录情况，设置工号和姓名

        document.getElementById("csSoftPhone").AgentID = agentId;
        document.getElementById("csSoftPhone").AgentName = agentName;

        //系统调试初期，建议打开Log开关。稳定后可以关闭
        document.getElementById("csSoftPhone").setInitParam( "DEBUGTRACE", "1");

        //LOGONMODE取值说明：
        //0，则代表座席已经通过认证，由业务将工号及姓名设置进来即可；
        //1，需要进行密码确认，工号以上面设置的工号为准
        //2，需要进行密码认证，座席可以修改登录的工号，然后再设置密码
        document.getElementById("csSoftPhone").setInitParam("LOGONMODE","0");
        document.getElementById("csSoftPhone").setInitParam("GETLOCALSETTING","1");
        //设置外拨模式
        document.getElementById("csSoftPhone").setInitParam("DEFAULTWORKMODE","3");
    }

</SCRIPT>

<SCRIPT ID=clientEventHandlersJS LANGUAGE=javascript>
    function cmdDial_onclick() {
        //初始化
        window_onload();
        //连接服务器
        cmdSetParam_onclick();
        //登录
        document.getElementById("csSoftPhone").logon();
        //拨打电话

        var state =  document.getElementById("csSoftPhone").GetAgentState;
        if(state == 3) {
            //话机空闲状态，允许外拨
            var phone = document.getElementById("txtPhoneNum").value;
            var outPhone = "9" + phone;
            document.getElementById("csSoftPhone").Dial(outPhone, "");
        }else{
            //话机状态异常，直接logout，重走全部逻辑
            document.getElementById("csSoftPhone").logOff();
            alert("重置电话成功，请重新拨打该电话");
            return false;
        }

    }

    function isIE() { //ie?
        if (!!window.ActiveXObject || "ActiveXObject" in window)
            return true;
        else
            return false;
    }

    function window_onload() {
        //如果需要加载时自动连接Server
        var html = "";
        if(isIE()){
            html = " &nbsp;<object id='csSoftPhone'  classid='CLSID:A972798F-50FC-4818-BCE2-2472BC68766C' codebase='CrystalSoftPhone32.cab#Version=3,2,0,3'"
            +" style='width:200pt; height: 65pt'><param name='ServerIP' value='172.28.168.231'/> <param name='LocalIP' value='127.0.0.1'/> <param name='INS' value='6001'/> <param name='SK' value='1001|1002'/>"
            +"<param name='AgentID' value='1001'/> <param name='AgentName' value='Demo'/></object>";
        }else{
            html = "<object id='csSoftPhone' TYPE='application/x-itst-activex' clsid='{A972798F-50FC-4818-BCE2-2472BC68766C}'"
            +" event_evtLogonSucc='csSoftPhone_evtLogonSucc'  event_evtStateChange='csSoftPhone_evtStateChange' event_evtCallArrive='csSoftPhone_evtCallArrive' event_evtDevOnHook='csSoftPhone_evtDevOnHook' style='width: 100%; height: 65pt'> </object>";
        }
        document.getElementById("softphonediv").innerHTML  = html;

//        window.setTimeout(cmdSetParam_onclick,1000);
        //cmdSetParam_onclick();
        //如果需要加载时自动连接Server
        if(isIE()){
            //cmdSetParam_onclick();
        }
        //
    }
</SCRIPT>

<script type="text/javascript">
    $(function(){
        setTimeout(cmdDial_onclick(), 5000);
    });
</script>