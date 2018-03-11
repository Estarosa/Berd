<#import "masterTemplate.ftl" as layout />

<@layout.masterTemplate title="Timeline">

<div class="row">
    <div class="col-xs-11">
        <h3>${pageTitle}</h3>
        <#if user??>
            <#if profileUser?? && user.id != profileUser.id>
                <div class="pull-right">
                    <#if followed>
                        <a class="btn btn-warning" href="/t/${profileUser.username}/unfollow">Unfollow User</a>
                    <#else>
                        <a class="btn btn-primary" href="/t/${profileUser.username}/follow">Follow User</a>.
                    </#if>
            <#elseif pageTitle != 'Public Timeline'>
                <div class="panel panel-info">
                    <div class="panel-heading">
                        <h3 class="panel-title">What's on your mind ${user.username}?</h3>
                    </div>
                    <div class="panel-body">

                        <form class="form-horizontal" action="/message" method="post" enctype="multipart/form-data">
                            <div class="input-group">
                                <input type="text" name="text" class="form-control" required/>
                                <input type="file" name="file" accept=".png" >
                            <span class="input-group-btn">
                                  <button class="btn btn-primary" type="submit"> Share </button>
                              </span>
                              </div>
                        </form>
                    </div>
                </div>
            </#if>
           </div>
        </#if>
    </div>
</div>

<div class="row">
    <div class="col-xs-11">
        <div id="media-list" class="row">
            <#if messages??>
                <#list messages as message>
                    <hr/>

                    <div class="media">
                        <#if message.username??>
                        <#if message.userId != 0 >
                        <a class="pull-left" href="/t/${message.username}">

                            <img class="media-object" src="${message.gravatar}"/>
                            </a>
                         <#else>
                          <a class="pull-left" target="_blank" href="http://www.google.com/search?q=${message.text}">
                          </a>
                             </#if>
                        </#if>
                        <div class="media-body">
                            <h4 class="media-heading">
                                <#if message.username??>
                                <#if message.userId != 0 >
                                <a href="/t/${message.username}">
                                ${message.username}
                                </a>
                                <#else>
                                <a target="_blank" href="http://www.google.com/search?q=${message.text}" >
                                                                ${message.username}
                                 </a>
                                 </#if>
                                 </#if>
                            </h4>

                        <#if message.text??>
                        <#if message.userId != 0 >
                        ${message.text}
                        <#else>
                        #${message.text}
                        </#if>
                        </#if>
                        <#if message.img??>
                         <img class="media-object" src="../images/${message.img}"/>
                        </#if>
                        <br/>
                           <#if message.pubDateStr??>
                            <small>&mdash; ${message.pubDateStr}</small>
                           </#if>
                        </div>
                    </div>
                <#else>
                    <hr/>
                    <div class="well">
                        There's nothing so far.
                    </div>
                </#list>
            <#else>
                <hr/>
                <div class="well">
                    There's nothing so far.
                </div>
            </#if>
        </div>
    </div>
</div>

</@layout.masterTemplate>