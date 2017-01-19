<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="java.util.*,java.util.Map.*,com.vmware.metricprocessor.pojo.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
	<title>modify describe.xml page</title>
</head>
<body onload="initial_page()">
	<%
	 	Map<String, Map<String, Map<String, List<Metric>>>> map = ( Map<String, Map<String, Map<String, List<Metric>>>>) request.getAttribute("metricMap");
		List<Adapter> adapterList = (List<Adapter>) request.getAttribute("adapterList");
	%>
	
	<form id="form1" name="form1" method="post" action="generateDescribe" style="margin-left:20%; width:60%;">
		<div style="float:left; margin-left: 0%; margin-top:30px; width:100%">
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
		</div>
		<div style="clear: both; height:20px "></div>
		
		<div style="margin-left: 0%; margin-top:10px; width:100%">
			please select the Adapter Kind: 
			<select id="adapterKind" name="adapterKind" style="margin-left: 10px; width: 20%" onchange="display_version_list(this.value)">
			<%for (Adapter adapter : adapterList){%>
				<option value=<%=adapter.getAdapterKind()%>> <%=adapter.getAdapterKind()%> </option>
			<%}%>			
			</select>
		</div>		
		
		<div style="margin-left: 0%; margin-top:30px; width:100%">
			please select the Adapter Version:
			<%for (Adapter adapter : adapterList){%>
				<select id=<%=adapter.getAdapterKind()%> class="adapterVersion" name="adapterVersion_disabled" onchange="display_tags(document.getElementById('adapterKind').value, this.value)" style="margin-left: 10px; width: 20%; display:none">
				<%for (String version : adapter.getVersionList()) {%>
					<option value=<%=version%>> <%=version%> </option>
				<%} %>
				</select>
			<%}%>
		</div>	
		
		
		<% int numberPerRow = 3;
		int numberInPartList = 10;
		int count = 0;
		int order = 0;
		for(Entry<String, Map<String, Map<String, List<Metric>>>> entry1 : map.entrySet()){
			for (Entry<String, Map<String, List<Metric>>> entry2 : entry1.getValue().entrySet()){%>
				<div id="<%=entry1.getKey()+'_'+entry2.getKey()%>" name="tags_block">
				
				<% Map<String, Double> sortMap = new TreeMap<String, Double>();
				for (Entry<String, List<Metric>> entry : entry2.getValue().entrySet()) {
					sortMap.put(entry.getKey(), entry.getValue().get(0).getTagMap().get(entry.getKey()));
				}
		        //这里将map.entrySet()转换成list
		        List<Map.Entry<String, Double>> sortedList = new ArrayList<Map.Entry<String, Double>>(sortMap.entrySet());
		        //然后通过比较器来实现排序
		        Collections.sort(sortedList ,new Comparator<Map.Entry<String, Double>>() {
		            // 降序排序
		            public int compare(Entry<String, Double> o1,
		                    Entry<String, Double> o2) {
		                return o2.getValue().compareTo(o1.getValue());
		            }	            
		        });
/* 		        for (int i=0; i<sortedList.size(); i++){
		        	System.out.println(sortedList.get(i));
		        }	 */
		        sortedList = sortedList.subList(0, sortedList.size()>=5? 5:sortedList.size());
        
				
				for (Entry<String, List<Metric>> entry : entry2.getValue().entrySet()) {%>
				
					<div class="checkbox" style="margin-top:10px; float:left; width:30%;"> 
						<% boolean checked = false;
						for (Map.Entry<String,Double> mapping : sortedList) {
							if (mapping.getKey().equals(entry.getKey())){
								checked = true;
							}
						}
						if (checked){%>
							<input style="margin-left:10px;" type="checkbox" name="checkbox" value="<%= entry.getKey() %>" checked/>
						<%} else{%>
							<input style="margin-left:10px;" type="checkbox" name="checkbox" value="<%= entry.getKey() %>"/>
						<%}%>
						
						<span style="margin-left:30px; cursor:pointer" id="label<%= order %>" onclick="display_hide_details(<%= count %>)"><%= entry.getKey() %></span>
						<ul id="partlist<%= count %>" style="display:none">
						  <% int i = 0;
						  for (i=0; i<entry.getValue().size() && i<numberInPartList; i++){%>
							  <li><%= entry.getValue().get(i).getMetricName() %></li>
						  <% }
						  if (i<entry.getValue().size()){%>
							  <span style="color:blue; cursor:pointer" onclick="display_completelist(<%= count %>)"> read more </span>
						  <%}%>
						</ul>
						
						<ul id="completelist<%= count %>" style="display:none">
						  <% for (Metric metric:entry.getValue()){%>
							  <li><%= metric.getMetricName() %></li>
						  <%}%>
						</ul>
						
					</div>
					<%if (order%numberPerRow == numberPerRow-1 || order>=entry2.getValue().size()-1) {%>
						<div style='clear: both;'></div>
					<%} 
					order++;
					count++;
				}
				
				order = 0;%>
				</div>
			<%}
		}%>
		

		
		<input class="btn btn-default" style="margin-left: 30%; margin-top: 10px;" name="sub" type="submit" value="generate describe file">
	</form>
	
	<script>
	
	function initial_page()
	{
		document.getElementById(document.getElementById("adapterKind").value).style.display ='inline';
		display_tags(document.getElementById("adapterKind").value, document.getElementById(document.getElementById("adapterKind").value).value);
	}
	
	function display_tags(adapterKind, adapterVersion){
		var all_tags_block = document.getElementsByName("tags_block");
		for (var i=0; i<all_tags_block.length; i++){
			if(all_tags_block[i].id==(adapterKind+'_'+adapterVersion)){
				all_tags_block[i].style.display = 'block';
			} else{
				all_tags_block[i].style.display = 'none';
			}
		}
	}
	
	function display_version_list(val)
	{
		var all_version_selects = document.getElementsByClassName("adapterVersion");
		for (var i=0; i<all_version_selects.length; i++){
			all_version_selects[i].style.display ='none';
			all_version_selects[i].name = 'adapterVersion_disabled';
		}
		document.getElementById(val).style.display ='inline';
		document.getElementById(val).name = 'adapterVersion';
		display_tags(val, document.getElementById(val).value);
	}
	
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