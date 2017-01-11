<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="java.util.*,java.util.Map.*,com.vmware.metricprocessor.pojo.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%
		Map<String, List<Metric>> map = (Map<String, List<Metric>>) request.getAttribute("metricMap");
		map.size();
	%>
	<p><%=map.size()%></p>
	<form id="form1" name="form1" method="post" action="generateDescribe">
		<% int order = 0;
		for (Entry<String, List<Metric>> entry : map.entrySet()) {%>
			<% if (order%5==0){ %>
			<div>
			<%} %>
				<div style="float:left; width:20%;"> 
					<input type="checkbox" name="checkbox" value="<%= entry.getKey() %>"/>
					<span id="label<%= order %>" onclick="display_hide_details(<%= order %>)"><%= entry.getKey() %></span>
					<ul id="partlist<%= order %>" style="display:none">
					  <% int i = 0;
					  for (i=0; i<entry.getValue().size()&&i<10;i++){%>
						  <li><%= entry.getValue().get(i).getMetricName() %></li>
					  <% }
					  if (i<entry.getValue().size()){%>
						  <span style="color:blue; cursor:pointer" onclick="display_completelist(<%= order %>)"> read more </span>
					  <%}%>
					</ul>
					
					<ul id="completelist<%= order %>" style="display:none">
					  <% for (Metric metric:entry.getValue()){%>
						  <li><%= metric.getMetricName() %></li>
					  <%}%>
					</ul>
					
				</div>
			<%if (order%5 == 4 || order>=map.size()-1) {%>
			<div style='clear: both;'></div>
			</div>
			<%} order++;
		} %>
		<input name="sub" type="submit" value="generate describe file">
	</form>
	
	<script>
	function display_hide_details(order)
	{
		if (document.getElementById("partlist"+order).style.display != 'none' || document.getElementById("completelist"+order).style.display != 'none'){
			document.getElementById("partlist"+order).style.display ='none';
			document.getElementById("completelist"+order).style.display ='none';
		}
		else{
			document.getElementById("partlist"+order).style.display ='block';
		}
	}
	
	function display_completelist(order)
	{
		document.getElementById("partlist"+order).style.display ='none';
		document.getElementById("completelist"+order).style.display ='block';
	}
	</script>
</body>
</html>