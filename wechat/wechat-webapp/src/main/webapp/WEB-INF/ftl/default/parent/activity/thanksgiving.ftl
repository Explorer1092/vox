<#import "../layout.ftl" as thanksgiving>
<@thanksgiving.page title="感恩节" pageJs="thanksgiving">
    <@sugar.capsule css=['jbox','thanksgiving'] />
    <div class="t-thanksGiving-box">
        <div class="banner">
            <img src="/public/images/parent/activity/thanksgiving/thanksgiving-header.png" alt=""/>
            <img src="/public/images/parent/activity/thanksgiving/thanksgiving-header-2.jpg" alt=""/>
        </div>
        <div class="list">
            <div class="title">
                <h1>达成目标</h1>
                <select name="" id="" data-bind="options: studentList,optionsText : 'name', value : currentStudent" class="int"></select>
            </div>
            <h3>
                <!-- ko if: currentStudent() -->
                    <!-- ko if: currentStudent().img() -->
                        <img data-bind="attr: {src : '<@app.avatar href='/'/>'+currentStudent().img()}" alt=""/>
                    <!-- /ko -->
                    <!-- ko if: !currentStudent().img() -->
                        <img src="<@app.avatar href=''/>" alt=""/>
                    <!-- /ko -->
                    <span class="name" data-bind="text: currentStudent().name()"></span>
                <!-- /ko -->
                的今天目标：
                <span data-bind="text: currentDayTarget()"></span>
            </h3>

            <div data-bind="template: {name : 'detailList', data : thanksDetail}"></div>

            <script id="detailList" type="text/html">
                <div class="table-box">
                    <table>
                        <thead>
                        <tr>
                            <td style="width:33%">学生</td>
                            <td style="width:33%">获得<i class="icon icon-3"></i></td>
                            <td style="width:33%">获得<i class="icon icon-2"></i></td>
                        </tr>
                        </thead>
                        <tbody>
                            <!-- ko if : $root.thanksDetail().length > 0 -->
                                <!-- ko foreach : {data : $root.thanksDetail, as : '_de'} -->
                                    <tr>
                                        <td data-bind="text: _de.studentName"></td>
                                        <td data-bind="text: _de.chickenCount"></td>
                                        <td data-bind="text: _de.coinCount"></td>
                                    </tr>
                                <!-- /ko-->
                            <!-- /ko-->

                            <!-- ko if : $root.thanksDetail().length == 0 -->
                                <tr>
                                    <td>暂无数据</td>
                                </tr>
                            <!-- /ko-->
                        </tbody>
                    </table>
                </div>
            </script>
        </div>
        <div class="content">
            <h1>感谢老师</h1>
            <div class="column">
                <h2>已有 <span data-bind="text: thanksParentCount"></span> 位家长感谢了老师 <i class="icon icon-1"></i></h2>
                <p class="info">感恩节，孩子们正在用实际行动感谢老师，家长们也来表达一下对老师的感恩吧！</p>
                <div class="btn"><a href="javascript:void(0);" data-bind="click : thanksTeacherBtn" class="btn-thanks">感谢老师</a></div>
            </div>
            <div class="column">
                <h2 style="line-height: 105px;">已有 <span data-bind="text: integralParentCount"></span> 位家长贡献了 <span data-bind="text: integralCount"></span> 个学豆<i class="icon icon-2"></i></h2>
                <p class="info" style="width:275px;padding:0 60px;margin:0 0 0 0;">你可以充实班级学豆，支持老师鼓励学生进步</p>
                <div class="btn" style="margin:10px 0 0 0;">
                    <!-- ko if: currentStudent() -->
                        <a href="javascript:void(0);" data-bind="attr: {href : '/parent/integral/order.vpage?sid='+currentStudent().id()}" class="btn-send">给班级送学豆</a>
                    <!-- /ko -->
                </div>
            </div>
        </div>
    </div>
</@thanksgiving.page>
