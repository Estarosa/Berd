<#macro masterTemplate title="Welcome">
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>${title} | Berd</title>
    <link rel="stylesheet" type="text/css" href="/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="/css/style.css">
</head>
<body>
<div class="container" >
    <nav class="navbar navbar-default" role="navigation">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle"
                    data-toggle="collapse" data-target="#example-navbar-collapse">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>

            <a class="navbar-brand" href="/">Berd</a>
            <a href="/">
            <image src="../images/Berd.png" style="width:30px;height:30px;top:18px;left:65px;position: absolute;" alt ="Berd"/>
            </a>
                <div class="pane1">
                    <form class="form-horizontal" action="/sh" method="get">
                          <div class="input-group">
                                <input type="text" name="input" class="form-control" required/>
                                     <span class="input-group-btn">
                                          <button class="btn btn-primary" type="submit"> Search </button>
                                     </span>
                          </div>
                    </form>
                </div>

        </div>
        <div class="collapse navbar-collapse" id="example-navbar-collapse">
            <ul class="nav navbar-nav navbar-right">
                <#if user??>
                    <li><a href="/">My Timeline</a></li>
                    <li><a href="/public">Public Timeline</a></li>
                    <li><a href="/logout">Sign Out</a></li>
                <#else>
                    <li><a href="/public">Public Timeline</a></li>
                    <li><a href="/register">Sign Up</a></li>
                    <li><a href="/login">Sign In</a></li>
                </#if>
            </ul>
        </div>
    </nav>

    <div class="container">
        <#nested />
    </div>

    <footer class="footer">
        <p>Berd</p>
    </footer>
</div><!-- /container -->
</body>
</html>
</#macro>