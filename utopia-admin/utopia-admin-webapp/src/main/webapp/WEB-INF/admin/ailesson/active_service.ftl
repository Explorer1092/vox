<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='主动服务' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/moment.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/vue-datepicker/vue-datepicker.js"></script>
<script src="//open.thunderurl.com/thunder-link.js"></script>
<style>
    .control-label{
        width:80px !important;
    }
    .form-horizontal .controls {
        margin-left: 100px !important;
    }
    .box{
        display: inline-block;

    }
    .not-finished-status{
        color: red;
    }
    .vdp-datepicker input {
        height: 30px;
    }
    .remind-div{
        position: fixed;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        opacity: 0.9;
        background: #c3c3c3;
    }
    .remind-div-container {
        position: absolute;
        width: 200px;
        top: calc(40%);
        left: calc(50% - 100px);
        background-color: #fff;
        padding: 10px;
    }
    .remind-div-title {
        font-size: 18px;
        font-weight: 600;
        border-bottom: solid 1px #CCC;
        padding-bottom: 10px;
        height: 18px;
        line-height: 18px;
        margin-bottom: 10px;
    }
    .remind-div-close-btn {
        float: right;
        cursor: pointer;
        background: #f2f2f2;
        padding: 5px;
        margin-top: -5px;
    }
    .remind-div-body{
        margin-bottom: 10px;
        text-indent: 30px;
        font-size: 15px;
        color: #000;
        padding-bottom: 5px;: ;
        margin-bottom: 5px;
        border-bottom: solid 1px #CCC;
    }
    .remind-div-foot button {
        float: right;
    }
</style>
<div id="userContainer" class="span9">
    <div class="row">
        <div class="span12 well">
            <form class="form-horizontal">
                <div class="row">

                    <div class="box">
                        <div class="control-group">
                            <label class="control-label" for="serviceType">服务类型</label>
                            <div class="controls">
                                <select id="serviceType" v-model="serviceType">
                                    <option value="ALL">完课点评和催补课提醒</option>
                                    <option value="SERVICE">完课点评</option>
                                    <option value="REMIND">催补课提醒</option>
                                    <option value="BINDING">绑定公众号</option>
                                    <option value="USEINSTRUCTION">薯条英语开课指导</option>
                                    <option value="RENEWREMIND">续费提醒</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <div class="box">
                        <div class="control-group">
                            <label class="control-label" for="status">完成状态</label>
                            <div class="controls">
                                <template v-if="serviceType=='SERVICE'">
                                    <select id="status" v-model="status">
                                        <option value="2">全部</option>
                                        <option value="3">未审核</option>
                                        <option value="1">已完成</option>
                                        <option value="0">未完成</option>
                                    </select>
                                </template>
                               <template v-else>
                                   <select id="status" v-model="status">
                                       <option value="2">全部</option>
                                       <option value="1">已完成</option>
                                       <option value="0">未完成</option>
                                   </select>
                               </template>
                            </div>
                        </div>
                    </div>

                    <div v-if="serviceType == 'ALL' ||serviceType == 'SERVICE' || serviceType == 'REMIND' "class="box">
                        <div class="control-group">
                            <label class="control-label" for="unitId">单元</label>
                            <div class="controls">
                                <select id="unitId" v-model="unitId">
                                    <option v-for="(unit, index) in unitList" :key="unit.id" :value="unit.id">{{ unit.jsonData.name }}</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <div class="box" v-if="serviceType == 'RENEWREMIND'">
                        <div class="control-group">
                            <label class="control-label" for="level">定级</label>
                            <div class="controls">
                                <select id="level" v-model="level">
                                    <option value="">全部</option>
                                    <option value="One">一级</option>
                                    <option value="Two">二级</option>
                                    <option value="Three">三级</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="box"  v-if="serviceType == 'ALL' ||serviceType == 'SERVICE' || serviceType == 'REMIND'"  >
                        <div class="control-group">
                            <label class="control-label" for="createDate">时间</label>
                            <div class="controls">
                                <vuejs-datepicker clear-button input-class="date-picker" format="yyyy-MM-dd" v-model="createDate" name="createDate" style="height: 30px;"></vuejs-datepicker>
                            </div>
                        </div>
                    </div>

                    <div class="box">
                        <div class="control-group">
                            <label class="control-label" for="userId">用户id</label>
                            <div class="controls">
                                <input type="text" id="userId" v-model="userId" placeholder="用户id">
                            </div>
                        </div>
                    </div>

                    <div  v-if="serviceType == 'BINDING' ||serviceType == 'USEINSTRUCTION' || serviceType == 'RENEWREMIND'" class="box">
                        <div class="control-group">
                            <label class="control-label" for="updateBeginDate">开始服务时间</label>
                            <div class="controls">
                                <vuejs-datepicker clear-button  format="yyyy-MM-dd" v-model="updateBeginDate" name="updateBeginDate"></vuejs-datepicker>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="span6">
                        <div class="control-group">
                            <div class="controls">
                                <button type="button" @click="filter(false)" class="btn btn-success">查询</button>
                                <template v-if="serviceType == 'SERVICE'">
                                    <button type="button" @click="downLoadBatch" class="btn btn-success pull-right">批量下载</button>
                                    <#--<button type="button" id="downLoadBatch" class="btn btn-success pull-right">批量下载</button>-->
                                </template>
                            </div>
                        </div>
                        <div class="control-group">
                            <div class="controls">

                            </div>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </div>
    <div class="row">
        <div class="span12 well">
            <table class="table table-striped">
                <caption>服务列表</caption>
                <thead>
                    <tr>
                        <td v-for="(column, index) in tableColumns">{{column}}</td>
                    </tr>
                </thead>
                <tbody>
                    <tr v-if="infoList.length <= 0">
                        <td colspan="7" align="center">暂无数据</td>
                    </tr>
                    <tr v-else v-for="(info, index) in infoList">
                        <td>{{info.serviceName}}</td>
                        <td>
                            <span v-if="info.status=='0'">
                                未完成
                            </span>
                            <span v-if="info.status=='1'">
                                已完成
                            </span>
                            <span v-if="info.status=='3'">
                                未审核
                            </span>
                            <#--<span>{{ info.status }}</span>-->
                            <#--<span v-if="info.examineStatus">-->
                                <#--<span v-if="info.status">已完成</span>-->
                                <#--<span v-else class="not-finished-status">未完成</span>-->
                            <#--</span>-->
                        </td>
                        <td>{{info.unitName}}</td>
                        <td>{{info.userName}}</td>
                        <td>{{info.userId}}</td>
                        <td>
                            <span v-if="userPhone[info.userId] && userPhone[info.userId].loaded">{{userPhone[info.userId].phone}}</span>
                            <button v-else class="btn" type="button" @click="loadUserPhone(info.userId)">查看</button>
                        </td>
                        <td v-if="info.serviceType === 'REMIND'">{{ moment(new Date(info.createDate - 86400000)).format("YYYY-MM-DD") }}</td>
                        <td v-else>{{ moment(new Date(info.createDate)).format("YYYY-MM-DD HH:mm:ss") }}</td>
                        <td  v-if="info.serviceType === 'RENEWREMIND'">
                            <template v-for="(item, i) in info.renewList">
                                <button v-if="item.status" class="btn" type="button" @click="activeServiceRenew(index,info.serviceType, info.userId, item.type,i)">{{item.desc}}</button>
                                <button v-else class="btn btn-primary" type="button"
                                        @click="activeServiceRenew(index,info.serviceType, info.userId, item.type,i)">{{item.desc}}</button>
                            </template>
                        </td>
                        <td v-else>
                            <template v-if="info.serviceType === 'SERVICE'">
                                <a v-bind:id="'downLoad-' + index" target="_parent" ></a>
                                <button v-if="info.status == '1' && info.videoUrl" class="btn" type="button"
                                        @click="activeServiceVideoDownload(index,info.serviceType, info.userId, info.unitId,info.status,info.unitName,info.videoUrl)">视频下载</button>
                                <button  v-if="info.status == '0' && info.videoUrl" class="btn btn-primary" type="button"
                                         @click="activeServiceVideoDownload(index,info.serviceType, info.userId, info.unitId,info.status,info.unitName,info.videoUrl)">视频下载</button>
                                <button v-if="info.status == '3'" class="btn btn-primary" type="button"
                                        @click="activeServiceToExamine(info.userVideoId)">去审核</button>
                                <button  class="btn btn-primary" type="button"
                                         @click="genRemarkVideo(info.userId, info.unitId)">立即生成</button>
                            </template>
                            <template v-else>
                                <button v-if="info.status == '1'" class="btn" type="button"
                                        @click="activeServiceBtnClick(index,info.serviceType, info.userId, info.unitId, info.userName, info.createDate, info.linkStatus)">已服务</button>
                                <button  v-if="info.status == '0'" class="btn btn-primary" type="button"
                                         @click="activeServiceBtnClick(index,info.serviceType, info.userId, info.unitId, info.userName, info.createDate,  info.linkStatus)">主动服务</button>
                            </template>
                        </td>
                    </tr>
                    <tr v-if="infoList.length > 0">
                        <td colspan="6">
                            <div style="width: 300px; margin: 0 auto;">
                                <button v-if="currentPage > 1" class="btn" type="button" @click="prevPage()">上一页</button>
                                <button v-if="currentPage < totalPages" class="btn" type="button" @click="nextPage()">下一页</button>
                                <span>当前第 {{currentPage}} 页 | 共 {{totalPages}} 页</span>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
    <a id="downLoad" target="_parent" ></a>
    <div id="myModal" class="modal" :class="{hide:!model,fade:!model}" tabindex="-1" role="dialog"
         aria-labelledby="myModalLabel" aria-hidden="true">
        <div class="modal-header">
            <button type="button" class="close" @click="closeModel">×</button>
            <h3 id="myModalLabel">用手机扫描二维码预览</h3>
        </div>
        <div class="modal-body">
            <div id="sharecode_box" style="text-align: center"></div>
            <div id="sharecode_text" style="text-align: center"></div>
            <button style="display: none;" id="copy-btn">复制文本</button>
        </div>
    </div>

    <div :class="{in:model,'modal-backdrop':model,fade:model}"></div>
    <textarea id="tmp" style="display: none"></textarea>
</div>
<script type="text/javascript">
    var vm = new Vue({
        el: '#userContainer',
        data: {
            model: false,
            copyModel: false,
            serviceType: '${serviceType!"ALL"}', // SERVICE | REMIND
            classId: '${classId!}',
            status: 2,
            unitId: '',
            createDate: "",
            updateBeginDate : "",
            userId: '',

            userPhone: {},

            unitList: [],
            infoList: [],
            currentPage: 1,
            totalPages: 0,
            level: "",

            remindDivInfo: {
                userName: '',
                unitDate: '',
                show: false,
            },

            tableColumns: ['服务类型', '完成状态', '单元', '姓名', '用户ID', '电话', '时间', '操作'],
        },
        components: {
            'vuejs-datepicker': vuejsDatepicker,
        },
        methods: {
            // 筛选
            filter: function (pageClick) {
                if (!pageClick) {
                    this.currentPage = 1;
                }
                var self = this;
                // 筛选用户
                var createDate = '';
                var map01 = {ALL: true, SERVICE: true, REMIND: true};
                if(map01[self.serviceType] && self.createDate && typeof self.createDate === 'object') {
                    createDate = self.createDate.getFullYear() + '-' + (self.createDate.getMonth() + 1) + '-' + self.createDate.getDate() + ' 00:00:00';
                }
                var updateBeginDate = '';
                var map02 = {BINDING: true, USEINSTRUCTION: true, RENEWREMIND: true};
                if(map02[self.serviceType] && self.updateBeginDate && typeof self.updateBeginDate === 'object'){
                    updateBeginDate = self.updateBeginDate.getFullYear() + '-' + (self.updateBeginDate.getMonth() + 1) + '-' + self.updateBeginDate.getDate() + ' 00:00:00';
                }
                $.post("/chips/ai/active/service/detail.vpage", {
                    serviceType: self.serviceType,
                    classId: ${classId!},
                    status: self.status,
                    unitId: self.unitId === '-1' ? '' : self.unitId,
                    level: self.level,
                    createDate: createDate,
                    updateBeginDate: updateBeginDate,
                    userId: self.userId,
                    pageNum: self.currentPage,
                }, function (res) {
                    var infoList = res['infoList'];
                    self.infoList = infoList || [];

                    var unitList = res['unitList'];
                    self.unitList = unitList || [];
                    self.unitList.push({id: '-1', jsonData: {name: '全部'}});

                    self.totalPages = res['totalPage'];
                });
            },
            activeServiceToExamine:function (videoId) {
                console.log("activeServiceToExamine", videoId)
                window.open('${requestContext.webAppContextPath}/chips/user/video/examine/list.vpage?pageNumber=1&showTip=1&userVideoId=' +videoId + '&tab=2');
            },

            downLoadBatch:function () {
                var _this = this;
                var total = 0;
                _this.infoList.forEach(function (info) {
//                    if(info.status == '0'){
                        total = total + 1;
//                    }
                })
                if(total == 0) {
                    return;
                }
                console.log("total:" + total)
                var tasks = [];
                _this.infoList.forEach(function (info,i) {
                    var url = info.videoUrl;
                    if(info.status == '0'){
                        $.ajaxSettings.async = false;
                        $.get('${requestContext.webAppContextPath}/chips/ai/active/service/updateToReminded.vpage', {
                            userId:info.userId,
                            serviceType:info.serviceType,
                            classId: ${classId!},
                            unitId: info.unitId,
                        }, function (res) {
                            if (res.success) {
                                info.status = "1";
                            } else {
//                                alert(res.info);
                            }
                        });
                        $.ajaxSettings.async = true;

                    }
                    var task = {url:url, dir:"", name:info.userId + "-" + info.unitName + ".mp4"}
                    tasks.push(task);
                })

                multiDownload(tasks);

            },
            genRemarkVideo:function ( userId, unitId) {
                    $.ajaxSettings.async = false;
                    $.get('${requestContext.webAppContextPath}/chips/ai/active/service/genRemarkVideo.vpage', {
                        userId:userId,
                        unitId: unitId,
                    }, function (res) {
                        if (res.success) {
                            if(res.info) {
                                alert(res.info);
                            } else {
                                alert("操作成功");
                            }
                        } else {
                            alert(res.info);
                        }
                    });
                    $.ajaxSettings.async = true;
            },
            activeServiceVideoDownload:function (index, serviceType, userId, unitId, code, unitName, url) {
                var _this = this;
                if(code == "0"){
                    $.ajaxSettings.async = false;
                    $.get('${requestContext.webAppContextPath}/chips/ai/active/service/updateToReminded.vpage', {
                        userId:userId,
                        serviceType:serviceType,
                        classId: ${classId!},
                        unitId: unitId,
                    }, function (res) {
                        if (res.success) {
                            _this.infoList[index].status = "1";
                        } else {
                            alert(res.info);
                        }
                    });
                    $.ajaxSettings.async = true;
                }
                //进行下载
                singleDownload(userId, unitName, url)
            },
            // 用户查询按钮点击
            activeServiceBtnClick: function(index, serviceType, userId, unitId, userName, createDate, linkStatus) {
                var _this = this;
                if (serviceType === 'SERVICE') {
                    if(linkStatus){
                        window.open('${requestContext.webAppContextPath}/chips/user/question/indexV2.vpage?bookId=${bookId!}&unitId='+ unitId +'&userId=' + userId);
                    } else {
                        window.open('${requestContext.webAppContextPath}/chips/user/question/index.vpage?bookId=${bookId!}&unitId='+ unitId +'&userId=' + userId);
                    }
                    return;
                }
                $.ajaxSettings.async = false;
                    $.get('${requestContext.webAppContextPath}/chips/ai/active/service/otherServiceUserTemplateSave.vpage', {
                        userId:userId,
                        serviceType:serviceType,
                        clazzId: ${classId!},
                        unitId: unitId,
                    }, function (res) {
                        if (res.success) {
                            if(serviceType === 'RENEWREMIND') {
                                var templateId = res.templateId;
                                console.log("templateId:", templateId);
                                if(templateId.indexOf("first") == -1) {
                                    //后续
                                    var href = '${requestContext.webAppContextPath}/chips/ai/active/service/otherServiceRenewUserIndex.vpage?renewType=more&userId='+ userId +'&clazzId=' + ${classId!};

                                    console.log("后续 href:", href);
                                    window.open(href);
                                } else {
                                    //首次
                                    var href = '${requestContext.webAppContextPath}/chips/ai/active/service/otherServiceRenewUserIndex.vpage?renewType=first&userId='+ userId +'&clazzId=' + ${classId!};
                                    console.log("首次 href:", href);
                                    window.open(href);
                                }
                                return;
                            }
                            // console.log(res.remindText)
                            _this.infoList[index].status = "1";

                            hostName = window.location.host;
                            var _map = {
                                'admin.test.17zuoye.net': 'wechat.test.17zuoye.net',
                                'admin.17zuoye.net': 'wechat.17zuoye.com',
                                'admin.dc.17zuoye.net': 'wechat.17zuoye.com',
                                'admin.staging.17zuoye.net': 'wechat.staging.17zuoye.net',
                            }

                            hostName = _map[hostName] ? _map[hostName] : hostName;
//
                            if (hostName.indexOf('8085') > -1) {
                                hostName = hostName.replace(/8085/g, "8180")
                            }
                            var timestamp = Date.parse(new Date());
                            var url = "http://" + hostName + "/chips/center/otherServiceTypePreview.vpage?userId=" + userId + "&serviceType=" + serviceType
                                    + "&clazzId=${classId!}" + "&t=" + timestamp;
                            // console.log("preview url: " + url)

                            var codeImgSrc = "https://www.17zuoye.com/qrcode?m=" + encodeURIComponent(url);

                            var imgObj = new Image();
                            imgObj.src = codeImgSrc;
                            imgObj.style.width = "200px";
                            imgObj.style.height = "200px";
                            $("#sharecode_box").html('');
                            $("#sharecode_box").append(imgObj);
                            $("#sharecode_text").html('');

                            if(res.remindText){

                                $("#sharecode_text").html(res.remindText);
                                $("#copy-btn").show().click(function(){
                                    copyToClipBoard("sharecode_text");
                                });
                            } else {
                                $("#copy-btn").hide();
                            }
                            _this.model = true;
                            // console.log($("#sharecode_box").html())
                        } else {
                            alert(res.info);
                        }
                    });
//                }
                $.ajaxSettings.async = true;

            },
            // 用户查询按钮点击
            activeServiceRenew: function(index, serviceType, userId, renewType, ind) {
                var _this = this;
                console.log("index",index);
                console.log("serviceType",serviceType);
                console.log("userId",userId);
                console.log("renewType",renewType);
                $.ajaxSettings.async = false;
                $.get('${requestContext.webAppContextPath}/chips/ai/active/service/otherServiceUserTemplateSave.vpage', {
                    userId:userId,
                    serviceType:serviceType,
                    clazzId: ${classId!},
                    renewType:renewType
                }, function (res) {
                    if (res.success) {
                        // console.log(res.remindText)
                        _this.infoList[index].status = "1";
                        _this.infoList[index].renewList[ind].status = "1"
                        hostName = window.location.host;
                        var _map = {
                            'admin.test.17zuoye.net': 'wechat.test.17zuoye.net',
                            'admin.17zuoye.net': 'wechat.17zuoye.com',
                            'admin.dc.17zuoye.net': 'wechat.17zuoye.com',
                            'admin.staging.17zuoye.net': 'wechat.staging.17zuoye.net',
                        }

                        hostName = _map[hostName] ? _map[hostName] : hostName;
//
                        if (hostName.indexOf('8085') > -1) {
                            hostName = hostName.replace(/8085/g, "8180")
                        }
                        var timestamp = Date.parse(new Date());
                        var url;
                        if("grade" == renewType){
                            url = "http://" + hostName + "/chips/center/reportV2.vpage?userId="  + userId + "&bookId=${bookId!}" + "&t=" + timestamp;
                        } else {
                            url = "http://" + hostName + "/chips/center/otherServiceTypePreview.vpage?userId=" + userId + "&serviceType=" + serviceType
                                    + "&clazzId=${classId!}" + "&renewType=" + renewType + "&t=" + timestamp;
                        }
                        console.log("renew  url: " + url)
                        var codeImgSrc = "https://www.17zuoye.com/qrcode?m=" + encodeURIComponent(url);

                        var imgObj = new Image();
                        imgObj.src = codeImgSrc;
                        imgObj.style.width = "200px";
                        imgObj.style.height = "200px";
                        $("#sharecode_box").html('');
                        $("#sharecode_box").append(imgObj);
                        $("#sharecode_text").html('');

                        if(res.remindText){

                            $("#sharecode_text").html(res.remindText);
                            $("#copy-btn").show().click(function(){
                                copyToClipBoard("sharecode_text");
                            });
                        } else {
                            $("#copy-btn").hide();
                        }
                        _this.model = true;
                        // console.log($("#sharecode_box").html())
                    } else {
                        alert(res.info);
                    }
                });
//                }
                $.ajaxSettings.async = true;

            },
            closeModel: function () {
                var _this = this;
                _this.model = false;
                _this.copyModel= false;
            },
            // 上一页
            prevPage: function () {
                this.currentPage = this.currentPage - 1;
                this.filter(true);
            },
            // 下一页
            nextPage: function () {
                this.currentPage = this.currentPage + 1;
                this.filter(true);
            },
            // 加载用户手机号
            loadUserPhone: function (userId) {
                var self = this;
                 $.get('/crm/account/getuserphone.vpage?userId=' + userId, function (res) {
                     if (res.success) {
                         // 响应式更新
                         self.$set(self.userPhone, userId, {
                             'loaded': true,
                             'phone': res.phone,
                         });
                     }
                 })
            },
            openRemindInfoContainer: function (userName, createDate) {
                if (this.remindDivInfo.show === true) {
                    return;
                }
                this.remindDivInfo.userName = userName || '亲爱的同学';
                this.remindDivInfo.unitDate = moment(createDate - 86400000).format("MM 月 DD 日");
                this.$set(this.remindDivInfo, 'show', true);
            },
            closeRemindDiv: function () {
                this.remindDivInfo.userName = '亲爱的同学';
                this.remindDivInfo.unitDate = '';
                this.$set(this.remindDivInfo, 'show', false);
            },
            copyRemindContent: function () {
                try {
                    var tmp = $('#tmp');
                    tmp.val(this.remindDivInfo.userName + ',' + this.remindDivInfo.unitDate + '的课程还没有完成哦，今日事今日毕，快来做完该做的功课吧！');
                    tmp.css('display', 'block');
                    tmp.select();
                    var successful = document.execCommand('copy');
                    tmp.val('');
                    tmp.css('display', 'none');
                    alert(successful ? '成功复制到剪贴板' : '该浏览器不支持点击复制到剪贴板');
                } catch (err) {
                    // console.log(err);
                    alert('该浏览器不支持点击复制到剪贴板');
                }
            },
        },
        created: function () {
            this.unitId = '-1';
            this.filter(false);
//            console.log("11111111")
//            console.log(this.remindDivInfo.show)
        }
    });

    function copyToClipBoard(id) { //复制到剪切板
        const range = document.createRange();
        range.selectNode(document.getElementById(id));

        const selection = window.getSelection();
        if(selection.rangeCount > 0) selection.removeAllRanges();
        selection.addRange(range);
        document.execCommand("Copy");
        alert( '成功复制到剪贴板');
    }

    thunderLink();

    function singleDownload(userId, unitName, url) {
        var name = userId + "-" + unitName + ".mp4";
        console.log("singleDownload",userId, unitName, url)
        thunderLink.newTask({

            downloadDir: "",
            tasks: [{
                name: name,
                url: url
            }]
        });
    }

    function multiDownload(tasks) {
        console.log("tasks", tasks)
        var opts =   {
            downloadDir: "",
            installFile: "",
            taskGroupName: "点评视频",
            tasks: tasks,
            excludePath: "",
            minVersion: "10.0.1.0",
            threadCount: 10,
        };
        thunderLink.newTask(opts);
    }
</script>
</@layout_default.page>
