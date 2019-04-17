<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的账户' page_num=4>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 我的账户</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a id="addRolePath" class="btn btn-info" href="editprofile.vpage">
                    <i class="icon-edit icon-white"></i>
                    编辑
                </a>
                &nbsp;
            </div>
        </div>


        <div class="box-content">
            <ul class="nav nav-tabs" id="myTab">
                <li class="active"><a href="#info">基本信息</a></li>
                <#if requestContext.getCurrentUser().isProvinceAgent()
                     || requestContext.getCurrentUser().isCityAgent()
                     || requestContext.getCurrentUser().isBusinessDeveloper()
                     || requestContext.getCurrentUser().isCityAgentLimited()>
                    <#if requestContext.getCurrentUser().isCityAgent() || requestContext.getCurrentUser().isCityAgentLimited()>
                    <li class=""><a href="#marketingData">市场数据</a></li>
                    </#if>
                    <li class=""><a href="#myincomePanel">我的收入</a></li>
                    <li class=""><a href="#invoicePanel">付款证明</a></li>
                </#if>
            </ul>
            <div id="myTabContent" class="tab-content">
                <div class="tab-pane active" id="info">
                    <div class="box-content">
                        <div class="form-horizontal">
                            <fieldset>
                                <div class="control-group">
                                    <label class="control-label" >登录名</label>
                                    <div class="controls">
                                        <label class="control-label" style="text-align: left">${user.accountName!}</label>
                                    </div>
                                </div>

                                <div class="control-group">
                                    <label class="control-label" >真实姓名</label>
                                    <div class="controls">
                                        <label class="control-label" style="text-align: left">${user.realName!}</label>
                                    </div>
                                </div>
                                <#if requestContext.getCurrentUser().isCityAgent() || requestContext.getCurrentUser().isCityAgentLimited()>
                                    <div class="control-group">
                                        <label class="control-label">合同开始时间</label>
                                        <div class="controls">
                                            <label class="control-label" style="text-align: left"><#if user.contractStartDate??>${user.contractStartDate?string('yyyy-MM-dd')}</#if></label>
                                        </div>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label" >合同结束时间</label>
                                        <div class="controls">
                                            <label class="control-label" style="text-align: left"><#if user.contractEndDate??>${user.contractEndDate?string('yyyy-MM-dd')}</#if></label>
                                        </div>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label" >合同编号</label>
                                        <div class="controls">
                                            <label class="control-label" style="text-align: left">${user.contractNumber!}</label>
                                        </div>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label">合同保证金</label>
                                        <div class="controls">
                                            <label class="control-label" style="text-align: left"><#if user.cashDeposit??>${user.cashDeposit!}</#if></label>
                                        </div>
                                    </div>
                                </#if>
                                <div class="control-group">
                                    <label class="control-label" >电话</label>
                                    <div class="controls">
                                        <label class="control-label" style="text-align: left">${user.tel!}</label>
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" >邮箱</label>
                                    <div class="controls">
                                        <label class="control-label" style="text-align: left">${user.email!}</label>
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" >QQ</label>
                                    <div class="controls">
                                        <label class="control-label" style="text-align: left">${user.imAccount!}</label>
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" >地址</label>
                                    <div class="controls">
                                        <label class="control-label" style="text-align: left">${user.address!}</label>
                                    </div>
                                </div>
                                <#if requestContext.getCurrentUser().isCityAgent()
                                    || requestContext.getCurrentUser().isProvinceAgent()
                                    || requestContext.getCurrentUser().isCityAgentLimited() >
                                    <div class="control-group">
                                        <label class="control-label">开户行名称</label>
                                        <div class="controls">
                                            <label class="control-label" style="text-align: left">${user.bankName!}</label>
                                        </div>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label">开户人姓名</label>
                                        <div class="controls">
                                            <label class="control-label" style="text-align: left">${user.bankHostName!}</label>
                                        </div>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label">银行帐号</label>
                                        <div class="controls">
                                            <label class="control-label" style="text-align: left">${user.bankAccount!}</label>
                                        </div>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label">点数帐户余额</label>
                                        <div class="controls">
                                            <label class="control-label" style="text-align: left">${user.pointAmount?string(",##0.##")}</label>
                                        </div>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label">可用点数帐户余额</label>
                                        <div class="controls">
                                            <label class="control-label" style="text-align: left">${user.usablePointAmount?string(",##0.##")}</label>
                                        </div>
                                    </div>
                                </#if>
                            </fieldset>
                        </div>
                    </div>
                </div>

                <#--市场数据-->
                <div class="tab-pane" id="marketingData">
                    <div class="box-content">
                        <h3>业绩完成情况</h3>
                        <div>
                            <table class="table table-striped table-bordered">
                                <thead>
                                <tr>
                                    <th class="sorting" >大区</th>
                                    <th class="sorting" >城市</th>
                                    <th class="sorting" >中/小学</th>
                                    <th class="sorting" >学校数量</th>
                                    <th class="sorting" >学生基数</th>
                                    <th class="sorting" >目标</th>
                                    <th class="sorting" >完成</th>
                                    <th class="sorting" >完成率</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#if marketData.perfromanceDataList?has_content && marketData.perfromanceDataList?size gt 0>
                                        <#list marketData.perfromanceDataList as pd>
                                            <tr class="odd">
                                                <td>${pd.regionGroupName!}</td>
                                                <td>${pd.cityGroupName!}</td>
                                                <td>${pd.schoolLevel!}</td>
                                                <td>${pd.schoolCount!}</td>
                                                <td>${pd.totalStudentsCount!}</td>
                                                <td>${pd.budget!}</td>
                                                <td>${pd.complete!}</td>
                                                <td>${pd.completeRate!0}%</td>
                                            </tr>
                                        </#list>
                                    </#if>
                                </tbody>
                            </table>
                        </div>
                        <br>
                        <h3>线索统计</h3>
                        <div>
                            <table class="table table-striped table-bordered">
                                <thead>
                                <tr>
                                    <th class="sorting" >中/小学</th>
                                    <#--<th class="sorting" >市场专场组会</th>
                                    <th class="sorting" >区级专场组会</th>
                                    <th class="sorting" >插播组会</th>-->
                                    <th class="sorting" >进校线索</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#assign mpc = marketData.meetingAndClueData!/>
                                    <#if mpc?has_content>
                                        <tr class="odd">
                                            <td>小学</td>
                                            <#--<td>${mpc.cityJuniorMeet!}</td>-->
                                            <#--<td>${pd.countyJuniorMeet!}</td>-->
                                            <#--<td>${pd.interCutJuniorMeet!}</td>-->
                                            <td>${mpc.juniorClueCount!}</td>
                                        </tr>
                                        <tr class="odd">
                                            <td>中学</td>
                                            <#--<td>${mpc.cityMiddleMeet!}</td>-->
                                            <#--<td>${mpc.countyMiddleMeet!}</td>-->
                                            <#--<td>${mpc.interCutMiddleMeet!}</td>-->
                                            <td>${mpc.middleClueCount!}</td>
                                        </tr>
                                    </#if>
                                </tbody>
                            </table>
                        </div>
                        <br>
                        <h3>组会记录</h3>
                        <div>
                            <table class="table table-striped table-bordered">
                                <thead>
                                  <th class="sorting" >组会日期</th>
                                  <th class="sorting" >地点</th>
                                  <th class="sorting" >主题</th>
                                  <th class="sorting" >级别</th>
                                  <th class="sorting" >类型</th>
                                </thead>
                                <tbody>
                                   <#if marketData.meetingDataList?has_content && marketData.meetingDataList?size gt 0>
                                     <#list marketData.meetingDataList as m>
                                     <tr class="odd">
                                         <td>${m.meetingTime!''}</td>
                                         <td>${m.meetingPlace!''}</td>
                                         <td>${m.meetingTitle!''}</td>
                                         <td>${m.meetingLevel!''}</td>
                                         <td>${m.meetingType!''}</td>
                                     </tr>
                                     </#list>
                                   </#if>
                                </tbody>
                            </table>
                        </div>
                        <br>
                        <h3>学校名单</h3>
                        <div>
                            <table class="table table-striped table-bordered">
                                <thead>
                                <tr>
                                    <th class="sorting" >城市</th>
                                    <th class="sorting" >地区</th>
                                    <th class="sorting" >学校ID</th>
                                    <th class="sorting" >学校名称</th>
                                    <th class="sorting" >中/小学</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#if marketData.schoolDataList?has_content && marketData.schoolDataList?size gt 0>
                                        <#list marketData.schoolDataList as sd>
                                        <tr class="odd">
                                            <td>${sd.cityName!}</td>
                                            <td>${sd.countyName!}</td>
                                            <td>${sd.schoolId!}</td>
                                            <td>${sd.schoolName!}</td>
                                            <td><#if sd.schoolLevel == "JUNIOR">小学<#elseif sd.schoolLevel = "MIDDLE">中学<#else>小学</#if></td>
                                        </tr>
                                        </#list>
                                    </#if>
                                </tbody>
                            </table>
                        </div>

                    </div>
                </div>


                <div class="tab-pane" id="myincomePanel">
                    <div class="box-content">
                        <div>
                            <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable" id="dt1">
                                <thead>
                                <tr>
                                    <th class="sorting" style="width: 140px;">期间</th>
                                    <th class="sorting" style="width: 60px;">地区</th>
                                    <th class="sorting" style="width: 100px;">项目内容</th>
                                    <th class="sorting" style="width: 60px;">金额</th>
                                    <th class="sorting" style="width: 300px;">业绩明细</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#if myincome??>
                                        <#list myincome.userRegionIncomeData?keys as userRegionKey>
                                            <#list myincome.userRegionIncomeData[userRegionKey].incomeList as incomeData>
                                                <tr class="odd">
                                                    <td class="center sorting_1">
                                                    ${incomeData.startTime?string("yyyy-MM-dd")}
                                                        -
                                                    ${incomeData.endTime?string("yyyy-MM-dd")}
                                                    </td>
                                                    <td class="center sorting_1">
                                                    ${userRegionKey}
                                                    </td>
                                                    <td class="center sorting_1">
                                                    ${incomeData.source}
                                                    </td>
                                                    <td class="center sorting_1">
                                                    ${incomeData.income?string(",##0")}
                                                    </td>
                                                    <td class="center sorting_1">
                                                    ${incomeData.extInfo}
                                                    </td>
                                                </tr>
                                            </#list>
                                        </#list>
                                    </#if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="tab-pane" id="invoicePanel">
                    <div class="box-content">
                        <div>
                            <table class="table span6">
                                <thead>
                                <tr>
                                    <th class="sorting" style="width: 140px;">时间</th>
                                    <th class="sorting" style="width: 250px;">操作</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#if invoiceList??>
                                        <#list invoiceList?keys as invoiceKey>
                                            <#if requestContext.getCurrentUser().isCityAgentLimited() || requestContext.getCurrentUser().isCityAgent()>
                                                <tr class="odd">
                                                    <td class="center sorting_1">${invoiceList[invoiceKey]}</td>
                                                    <td>
                                                        <form action="printagentinvoice.vpage" method="post">
                                                            <input type="hidden" name="salaryMonth" value="${invoiceKey}"/>
                                                            <input type="hidden" name="slv" value="1"/>
                                                            <button type="submit" class="btn btn-success">打印付款证明(小学)</button>
                                                        </form>
                                                    </td>
                                                </tr>
                                                <tr class="odd">
                                                    <td class="center sorting_1">${invoiceList[invoiceKey]}</td>
                                                    <td>
                                                        <form action="printagentinvoice.vpage" method="post">
                                                            <input type="hidden" name="salaryMonth" value="${invoiceKey}"/>
                                                            <input type="hidden" name="slv" value="2"/>
                                                            <button type="submit" class="btn btn-success">打印付款证明(中学)</button>
                                                        </form>
                                                    </td>
                                                </tr>
                                            <#else>
                                                <tr class="odd">
                                                    <td class="center sorting_1">${invoiceList[invoiceKey]}</td>
                                                    <td>
                                                        <form action="printinvoice.vpage" method="post">
                                                            <input type="hidden" name="salaryMonth" value="${invoiceKey}"/>
                                                            <button type="submit" class="btn btn-success">打印付款证明</button>
                                                        </form>
                                                    </td>
                                                </tr>
                                            </#if>
                                        </#list>
                                    </#if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>
</div><!--/span-->
</div>

</@layout_default.page>
