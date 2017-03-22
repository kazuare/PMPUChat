<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page session="true" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<script src='http://code.jquery.com/jquery-latest.min.js' type='text/javascript'></script>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
<script type="text/javascript">
  function iframeLoaded() {
      var iFrameID = document.getElementById('idIframe');
      if(iFrameID) {
            iFrameID.height = "";
            iFrameID.height = iFrameID.contentWindow.document.body.scrollHeight + "px";
         
      }   
  }
</script>  
<title>Счетчик шарфов</title>
<style>

html{
    height: 100%;
}
iframe{
	overflow-x: hidden;
}
body {
    width:100%;
    height: 100%;
    overflow-x: hidden;
    
    font-size: medium;
}
#main {
    width:60%;
    margin: auto;
} 
#bordered{
    width:100%;
    background-color:#c1c4d0;
    border-color: #1a242f; 
    border-style: solid;  
    border-width: 1px;
} 
#chat {
    width:100%;
} 
#chat div{
    width:90%;
    margin: 1em;
    background-color:#d5d9e5;
}
#greeting {
	width:100%;
}
#greeting div{
	float:right;
	background-color:#d5d9e5;
    border-color: #1a242f; 
    border-style: solid;  
    border-width: 1px;
}

h1 {
    text-align:center;
} 
wbr { display: inline-block; }
.well img {
	max-height: 400px; 
	max-width: 400px;
}
</style>
</head>
<body>
<div id='main'>
<div id='greeting'>
<a href="#"><span class="glyphicon glyphicon-remove-sign"></span><span class="glyphicon glyphicon-ok-sign"></span></a>
<%= 
		( session.getAttribute( "mod" ) !=null ) ? 
		"<div onclick=\"location.href='http://80.87.202.12:8080/game/admin.jsp?';\">&nbsp;Панель модерации&nbsp;</div>":
		"<div><form action='Autorizator' method = 'POST'> Логин:&nbsp; <input type='text' name='login'>"+
		"&nbsp;Пароль:&nbsp; <input type='password' name='password'>"+
		"<input type='submit'></form></div>" 		
%>.</div>
<div class='jumbotron'>
<h1 id = 'total'></h1>
</div>
<div id='bordered'>
<div style="background-color:#d5d9e5;">
 <iframe src="form.jsp" scrolling="no" width="100%" frameborder="0" id="idIframe" onload="iframeLoaded()">
    Ваш браузер не поддерживает плавающие фреймы(чем вы пользуетесь?...)
 </iframe>
 </div>
<div id='chat'>
</div>
</div>
</div>
<script>
//if changing chatlength, change the same property in Chat.java
chatLength = 15;
extendedChat = false;
messagesLeft = true;
totalScarfs = 0;

$(document).ready(function(){
	//messages refresher initialize
	function refresh(message_id){
		$.ajax({
		  type: "GET",
		  //we got message_id from the previous refresh call
		  url: 'Manager',
		  data:{refreshFrom: message_id.toString()},
	      timeout:1000 * 50,
		  success: function( data ) {
			charIndex = data.indexOf('#');
			//this gets the actual number of messages and the actual scarf number at the server side 
			lastId = parseInt(data.substring(0, charIndex));
			totalScarfs = parseInt(data.substring(charIndex + 1, data.indexOf('%')));
			charIndex = data.indexOf('%');
			data = data.substring(charIndex + 1);
			
			if((lastId+1>chatLength) && messagesLeft){
				if (!($("#more").length)){
					$( '#chat' ).append("<div id='more'> Нажмите сюда, чтобы посмотреть больше... </div>");
					$("#more").click(function(){
						$.ajax({
							  type: "GET",
							  url: 'Manager',
							  data:{
								  id: lastId, 
								  len: $( ".msg" ).size()
							  },
							  success: function( data ) {
								  extendedChat = true;
								  $( '#chat' ).append(data.substring(1));
								  if(data[0] == "1"){
								  	$( '#chat' ).append($( "#more" ));	
								  }else{
									  $( "#more" ).remove(); 
									  messagesLeft = false;
								  }
							  }
							});
					});
				}
			}
			
			if(lastId != message_id)
		  		$( '#chat' ).prepend(data);
			if(!extendedChat)
				if($( ".msg" ).size() > chatLength)
					for(var i = chatLength; i < $( ".msg" ).size();)
						$( ".msg" ).toArray()[chatLength].remove();
			
			$("#total").text("Всего связано:" + totalScarfs);
					
			if ($( window ).width() < $( window ).height()){
				if ($(document.body).css( "background-size" ) == '100% 100%')
					$(document.body).css( "background-size", "cover" );			
			}else{
				if ($(document.body).css( "background-size" ) == "cover")
					$(document.body).css( "background-size", '100% 100%' );			
			}
			//set timeout to the next refresh call
			setTimeout(function(lastId){
				refresh(lastId);	
				console.log("messages refresh");
			}, 1000,lastId);},
		  error: function(lastId) {
				console.log("refresh error");
				refresh(lastId);	
		  }
		
		});
	}
	//starting with no messages at all, get all at first iteration, then ask for updates with recursion
	refresh(-1);
});

</script>
</body>
</html>