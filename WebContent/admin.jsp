<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Админка</title>
<% 
if(session.getAttribute( "mod" ) ==null)
	response.sendRedirect("room.jsp"); 
%>
<script src='http://code.jquery.com/jquery-latest.min.js' type='text/javascript'></script>
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
body {
    background-color: #000000;
    width:100%;
    height: 100%;
    overflow-x: hidden;
    
    font-size: medium;
    
    background: url(/music/back.jpg);
    background-repeat: no-repeat;
    background-attachment: fixed;
    background-size: 100% 100%;
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
#total {
	color: #FFFFFF;
}
#buttons{
	display:block;
}
#accept {
	color: #FFFFFF;
}
#reject {
	color: #FFFFFF;
}
#noSubmissionsMessage {
	color: #FFFFFF;
}
h1 {
    text-align:center;
} 
wbr { display: inline-block; }
.msg img {
	max-height: 400px; 
	max-width: 400px;
}
</style>
</head>
<body>
<div id='main'>
<div id='greeting'>
<div onclick="location.href='http://80.87.202.12:8080/game/room.jsp?';">&nbsp;На главную&nbsp;</div>
&nbsp;
<div onclick="location.href='LoadXML';">&nbsp;Получить все данные в XML-формате&nbsp;</div>
</div>
<h1 id = 'total'>Панель модерации</h1>
<div id='bordered'>
<div id='buttons'>
<span id='reject'>
	Отклонить
</span>
&nbsp;&nbsp;&nbsp;
<span id='accept'>
	Принять
</span>
</div>
<div id='noSubmissions'>
<span id='noSubmissionsMessage'>
	Ничего нового не пришло.
</span>
</div>
<div id='chat'>
</div>
</div>
</div>
<script>

messagesLeft = false;

$(document).ready(function(){
	
	$("#reject").click(function(){
		$.ajax({
			type: "POST",
			data:{
				  del: "reject"
			  },
			  url: 'ToApprove'
		});
	});
	$("#accept").click(function(){
		$.ajax({
			type: "POST",
			data:{
				  del: "accept"
			  },
			  url: 'ToApprove'
		});
	});
	
	//messages refresher initialize
	function refresh(){
		$.ajax({
		  type: "GET",
		  //we got message_id from the previous refresh call
		  url: 'ToApprove',
	      timeout:1000 * 50,
		  success: function( data ) {	
		  	$( '#chat' ).html(data);
		  	
		  	
		  	if($("#chat").text() == ""){
				$("#buttons").css( "display", 'none' );	
				$("#noSubmissions").css( "display", 'block' );	
				
		  	}else{
		  		$("#buttons").css( "display", 'block' );	
		  		$("#noSubmissions").css( "display", 'none' );	
		  	}
		  	
		  	
			if ($( window ).width() < $( window ).height()){
				if ($(document.body).css( "background-size" ) == '100% 100%')
					$(document.body).css( "background-size", "cover" );			
			}else{
				if ($(document.body).css( "background-size" ) == "cover")
					$(document.body).css( "background-size", '100% 100%' );			
			}
			//set timeout to the next refresh call
			setTimeout(function(){
				refresh();	
				console.log("messages refresh");
			}, 1000);},
		  error: function() {
				console.log("refresh error");
				refresh();	
		  }
		
		});
	}
	//starting with no messages at all, get all at first iteration, then ask for updates with recursion
	refresh();
});

</script>
</body>
</html>