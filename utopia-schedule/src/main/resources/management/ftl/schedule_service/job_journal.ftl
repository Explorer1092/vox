<#-- @ftlvariable name="jobJournals" type="java.util.List<com.voxlearning.utopia.schedule.journal.JobJournal>" -->
<!--suppress HtmlUnknownTarget -->
<html>
<head>
    <title>Job Journal(s)</title>
</head>
<body>
<h3><a class="btn btn-default" href="/schedule_service/index.do" role="button">Job Journal(s)</a></h3>

<div class="container-fluid">

<#if jobJournals?has_content>
    <div class="row">
        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td><strong>ID</strong></td>
                <td><strong>NAME</strong></td>
                <td><strong>CLASS</strong></td>
                <td><strong>START</strong></td>
            </tr>
            </thead>
            <tbody>
                <#list jobJournals as jobJournal>
                <tr>
                    <td>${jobJournal.id}</td>
                    <td>${jobJournal.jobName!''}</td>
                    <td>${jobJournal.jobClass!''}</td>
                    <td>${jobJournal.jobStartTime?datetime}</td>
                </tr>
                </#list>
            </tbody>
        </table>
    </div>
</#if>

</div>

</body>
</html>