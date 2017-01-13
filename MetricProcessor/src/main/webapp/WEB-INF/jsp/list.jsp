<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="java.util.*,java.util.Map.*,com.vmware.metricprocessor.pojo.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
	<title>modify describe.xml page</title>
</head>
<body>
	<%
		Map<String, List<Metric>> map = (Map<String, List<Metric>>) request.getAttribute("metricMap");
		map.size();
	%>
	
	<form id="form1" name="form1" method="post" action="generateDescribe" style="margin-left:20%; width:60%;">
		<div style="float:left; margin-left: 10px; margin-top:30px; width:100%">
			please select your job: 
			<select name="userJob" style="margin-left: 10px; width: 20%">
				<option value=""> </option>
				<option value="education"> Education </option>
				<option value="software"> Software </option>
				<option value="accounting"> Accounting </option>
				<option value="finance"> Finance </option>
				<option value="machinery"> Machinery </option>
				<option value="government "> Government  </option>
				<option value="other"> Other </option>
			</select>
			<input class="btn btn-default" style="margin-left: 20%" name="sub" type="submit" value="generate describe file">
		</div>
		
		<div style="clear: both; height:20px "></div>
		
		<% int numberPerRow = 3;
		int numberInPartList = 10;
		int order = 0;
		for (Entry<String, List<Metric>> entry : map.entrySet()) {%>
				<div class="checkbox" style="margin-top:10px; float:left; width:30%;"> 
					<input style="margin-left:10px;" type="checkbox" name="checkbox" value="<%= entry.getKey() %>"/>
					<span style="margin-left:30px; cursor:pointer" id="label<%= order %>" onclick="display_hide_details(<%= order %>)"><%= entry.getKey() %></span>
					<ul id="partlist<%= order %>" style="display:none">
					  <% int i = 0;
					  for (i=0; i<entry.getValue().size() && i<numberInPartList; i++){%>
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
			<%if (order%numberPerRow == numberPerRow-1 || order>=map.size()-1) {%>
			<div style='clear: both;'></div>
			<%} order++;
		} %>
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