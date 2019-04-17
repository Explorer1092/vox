<!--suppress HtmlUnknownTarget -->
<html>
<head>
    <title>Schedule Service</title>
</head>
<body>
<h3><a class="btn btn-default" href="/index.do" role="button">Schedule Service</a></h3>

<div class="container-fluid">

    <div class="row">
        <form class="form-inline" action="/schedule_service/query_job_journal.do" method="get">
            <div class="form-group">
                <label class="sr-only" for="date">Date</label>
                <input type="text" class="form-control" id="date"
                       name="date"
                       placeholder="yyyyMMdd">
            </div>
            <button type="submit" class="btn btn-default">Query Job Journal(s)</button>
        </form>
    </div>

</div>

</body>
</html>