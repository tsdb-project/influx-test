d3.gantt = function(tasks) {
    var FIT_TIME_DOMAIN_MODE = "fit";
    var FIXED_TIME_DOMAIN_MODE = "fixed";
    
    var margin = {
		top : 20,
		right : 40,
		bottom : 20,
		left : 150
    };
    //var timeDomainStart = d3.time.day.offset(new Date(),-3);
    //var timeDomainEnd = d3.time.hour.offset(new Date(),+3);
    var dataStartDate, dataEndDate, offset;
	var fileName = new Array();

    if(tasks == undefined || tasks.length <= 0){
    	dataStartDate = new Date();
    	offset = 0;
    	console.log("if before init");
    }else{
    	dataStartDate = new Date(tasks[0].arrestTime);
    	offset = tasks[tasks.length - 1].relativeEndTime;
    	for (p in tasks){
    		fileName.push(tasks[p].pid);
		}
    }
    console.log("dataStartDate :" + dataStartDate);
    
    var timeDomainStart = d3.time.second(dataStartDate);
    var timeDomainEnd = d3.time.second.offset(dataStartDate, offset);

    var formatDate = d3.time.format("%m/%d/%Y %H:%M:%S");

    console.log("s time format: " + typeof formatDate(timeDomainStart));
    console.log("e time format: " + typeof formatDate(timeDomainEnd));
    
    console.log("timeDomainStart" + timeDomainStart);
    console.log("timeDomainEnd" + timeDomainEnd);
    var timeDomainMode = FIT_TIME_DOMAIN_MODE;// fixed or fit
    var taskTypes = [];
    var patientFile = [];
    var taskStatus = [];
    var height = document.getElementById("chart-container").clientHeight - margin.top - margin.bottom;
    var width = document.getElementById("chart-container").clientWidth - margin.right - margin.left;
    ///console.log("height before: " + document.getElementById("chart-container").clientHeight);
    ///console.log("width before: " + document.getElementById("chart-container").clientWidth);
    ///console.log("height: " + height);
    ///console.log("width: " + width);

    //var tickFormat = "%H:%M";
    var tickFormat = "%d";

    var keyFunction = function(d) {
		//return d.relativeStartTime + d.arrestTime + d.relativeEndTime + d.uuid + d.filetype;
		//var arrestTime = formatDate(d3.time.second(new Date(d.arrestTime)));
		return d.relativeStartTime + d.arrestTime + d.relativeEndTime + d.pid + d.filetype;
    };

    var rectTransform = function(d) {
    	///console.log(x(d.startDate));
    	///console.log(y(d.taskName));
		// // return "translate(" + x(d3.time.second.offset(timeDomainStart, d.relativeStartTime)) + "," + y(d.filename + '#' + d.arrestTime) + ")";
		// // //return "translate(" + x(d3.time.second.offset(timeDomainStart, d.relativeStartTime)) + "," + y(d.arrestTime) + ")";
		// return "translate(" + x(d3.time.second.offset(timeDomainStart, d.relativeStartTime)) + "," + y(d.uuid + "#" + d.arrestTime + "#" + d.filetype) + ")";
		
		//console.log(typeof formatDate(d3.time.second(new Date(d.arrestTime))));
		//var arrestTime = formatDate(d3.time.second(new Date(d.arrestTime)));
		return "translate(" + x(d3.time.second.offset(timeDomainStart, d.relativeStartTime)) + "," + y(d.pid + "#" + d.arrestTime + "#" + d.filetype) + ")";
    };


	var x = d3.time.scale().domain([ timeDomainStart, timeDomainEnd ]).range([ 0, width ]).clamp(true);
    var y = d3.scale.ordinal().domain(fileName).rangeRoundBands([ 0, height - margin.top - margin.bottom ], .1);

    var xAxis = d3.svg.axis().scale(x).orient("bottom").tickFormat(d3.time.format(tickFormat)).tickSubdivide(true)
	    .tickSize(8).tickPadding(8);

    var yAxis = d3.svg.axis().scale(y).orient("left").tickSize(0);

    var initTimeDomain = function(tasks) {
		if (timeDomainMode === FIT_TIME_DOMAIN_MODE) {
		    /*if (tasks === undefined || tasks.length < 1) {
				timeDomainStart = d3.time.day.offset(new Date(), -3);
				timeDomainEnd = d3.time.hour.offset(new Date(), +3);
				return;
		    }
		    tasks.sort(function(a, b) {
		    	return a.endDate - b.endDate;
		    });
		    timeDomainEnd = tasks[tasks.length - 1].endDate;
		    tasks.sort(function(a, b) {
		    	return a.startDate - b.startDate;
		    });
		    timeDomainStart = tasks[0].startDate;*/
			if (tasks === undefined || tasks.length < 1) {
				timeDomainStart = d3.time.day.offset(new Date(), -3);
				timeDomainEnd = d3.time.hour.offset(new Date(), +3);
				return;
		    }
			let dataStartDate, dataEndDate, offset;
		    if(tasks == undefined || tasks.length <= 0){
		    	dataStartDate = new Date();
		    	offset = 0;
		    }else{
				//dataStartDate = new Date(tasks[0].arrestTime);
		    	dataStartDate = 0;
		    	offset = tasks[tasks.length - 1].relativeEndTime;
		    }
		    timeDomainStart = d3.time.second(dataStartDate);
		    timeDomainEnd = d3.time.second(d3.time.second.offset(dataStartDate, offset) - timeDomainStart);

		    console.log("init: " + timeDomainStart + " - " + timeDomainEnd);
		}
    };

    var initAxis = function() {
		x = d3.time.scale().domain([ timeDomainStart, timeDomainEnd]).range([ 0, width ]).clamp(true);
		y = d3.scale.ordinal().domain(fileName).rangeRoundBands([ 0, height - margin.top - margin.bottom ], .1);

		xAxis = d3.svg.axis().scale(x).orient("bottom").tickFormat(d3.time.format(tickFormat)).tickSubdivide(true)
			.tickSize(8).tickPadding(8);
		yAxis = d3.svg.axis().scale(y).orient("left").tickSize(0);
    };
    
    function gantt(tasks) {
	
		initTimeDomain(tasks);
		initAxis();
		
		var svg = d3.select("#chart-container")
			.append("svg")
			.attr("class", "chart")
			.attr("width", width + margin.left + margin.right)
			.attr("height", height + margin.top + margin.bottom)
			.append("g")
		        .attr("class", "gantt-chart")
			.attr("width", width + margin.left + margin.right)
			.attr("height", height + margin.top + margin.bottom)
			.attr("transform", "translate(" + margin.left + ", " + margin.top + ")");

		// Define the div for the tooltip
		var div = d3.select("body").append("div")	
		    .attr("class", "tooltip")				
		    .style("opacity", 0);
			
		svg.selectAll(".chart")
			.data(tasks, keyFunction).enter()
			.append("rect")
			.attr("rx", 5)
		    .attr("ry", 5)
			.attr("class", function(d){ 
			    if(taskStatus[d.status] == null){ return "bar";}
			    return taskStatus[d.status];
			}) 
			.attr("y", 0)
			.attr("transform", rectTransform)
			.attr("height", function(d) { return y.rangeBand(); })
			.attr("width", function(d) {
				var x = d3.time.scale().domain([ 0, (timeDomainEnd-timeDomainStart)/(1000) ]).range([ 0, width ]).clamp(true);
				return (x(d.relativeEndTime - d.relativeStartTime));
				//return ((d.relativeEndTime - d.relativeStartTime)/((timeDomainEnd-timeDomainStart)/(1000*60)) * width);
			})
			.on("mouseover", function(d) {
				var startTime = new Date(d.arrestTime);
				startTime.setSeconds( startTime.getSeconds() + d.relativeStartTime );
				var endTime = new Date(d.arrestTime);
				endTime.setSeconds( endTime.getSeconds() + d.relativeEndTime );
				div.transition()		
	                .duration(200)		
	                .style("opacity", .9);
	            div.html("f: " + d.fname + "<br>" + "s: " + startTime.toISOString() + "<br>" + "e: " + endTime.toISOString())	
					.style("left", (d3.event.pageX) + "px")
					.style("top", (d3.event.pageY - 28) + "px");
			})
			.on("mouseout", function(d) {
				div.transition()		
	                .duration(500)		
	                .style("opacity", 0);
			});
			 
			 
		svg.append("g")
			.attr("class", "x axis")
			.attr("transform", "translate(0, " + (height - margin.top - margin.bottom) + ")")
			.transition()
			.call(xAxis);
			 
		svg.append("g")
			.attr("class", "y axis")
			.transition()
			.call(yAxis)
			.selectAll('.y .tick text')
			.call(function(t){                
	            t.each(function(d){ // for each one
	            	var self = d3.select(this);
	            	var s = self.text().split('#');  // get the text and split it
	            	self.text(''); // clear it out
	            	self.append("tspan") // insert two tspans
	                	.attr("x", -10)
	                	.attr("dy","-0.4em")
	                	.text(s[0] + " " + s[2]);
	              	self.append("tspan")
	                	.attr("x", -10)
	                	.attr("dy","1.4em")
	                	.text(s[1]);
	            })
            });


		// Get Json data from medication table By Id and redirect
		svg.select(".y").filter(".axis").selectAll(".tick")
			.on("click",function () {
				window.location.href = '/analysis/medInfo/' + $(this).text();
			});
		return gantt;

    };
    
    gantt.redraw = function(tasks) {

		/*initTimeDomain();
		initAxis();
		
	    var svg = d3.select("svg");

	    var ganttChartGroup = svg.select(".gantt-chart");
	    var rect = ganttChartGroup.selectAll("rect").data(tasks, keyFunction);
	    
	    rect.enter()
	    .insert("rect",":first-child")
	    .attr("rx", 5)
	    .attr("ry", 5)
	    .attr("class", function(d){ 
		    if(taskStatus[d.status] == null){ return "bar";}
		    return taskStatus[d.status];
	    }) 
	 	.transition()
	 	.attr("y", 0)
	 	.attr("transform", rectTransform)
	 	.attr("height", function(d) { return y.rangeBand(); })
	 	.attr("width", function(d) { 
	    	return (x(d.relativeEndTime) - x(d.relativeStartTime)); 
	    });

	    rect.transition()
	    .attr("transform", rectTransform)
	 	.attr("height", function(d) { return y.rangeBand(); })
	 	.attr("width", function(d) { 
	     	return (x(d.relativeEndTime) - x(d.relativeStartTime));
	    });
	        
		rect.exit().remove();

		svg.select(".x").transition().call(xAxis);
		svg.select(".y").transition().call(yAxis);
		
		return gantt;*/

		initTimeDomain(tasks);
		initAxis();
		
		var svg = d3.select("#chart-container")
			.append("svg")
			.attr("class", "chart")
			.attr("width", width + margin.left + margin.right)
			.attr("height", height + margin.top + margin.bottom)
			.append("g")
		        .attr("class", "gantt-chart")
			.attr("width", width + margin.left + margin.right)
			.attr("height", height + margin.top + margin.bottom)
			.attr("transform", "translate(" + margin.left + ", " + margin.top + ")");

		// Define the div for the tooltip
		var div = d3.select("body").append("div")	
		    .attr("class", "tooltip")				
		    .style("opacity", 0);
			
		svg.selectAll(".chart")
			.data(tasks, keyFunction).enter()
			.append("rect")
			.attr("rx", 5)
		    .attr("ry", 5)
			.attr("class", function(d){ 
			    if(taskStatus[d.status] == null){ return "bar";}
			    return taskStatus[d.status];
			}) 
			.attr("y", 0)
			.attr("transform", rectTransform)
			.attr("height", function(d) { return y.rangeBand(); })
			.attr("width", function(d) {
				var x = d3.time.scale().domain([ 0, (timeDomainEnd-timeDomainStart)/(1000) ]).range([ 0, width ]).clamp(true);
				return (x(d.relativeEndTime - d.relativeStartTime));
				//return ((d.relativeEndTime - d.relativeStartTime)/((timeDomainEnd-timeDomainStart)/(1000*60)) * width);
			})
			.on("mouseover", function(d) {
				var startTime = new Date(d.arrestTime);
				startTime.setSeconds( startTime.getSeconds() + d.relativeStartTime );
				var endTime = new Date(d.arrestTime);
				endTime.setSeconds( endTime.getSeconds() + d.relativeEndTime );
				div.transition()		
	                .duration(200)		
	                .style("opacity", .9);		
	            div.html("f: " + d.fname + "<br>" + "s: " + startTime.toISOString() + "<br>" + "e: " + endTime.toISOString())	
	            	.style("left", (d3.event.pageX) + "px")
					.style("top", (d3.event.pageY - 28) + "px");
			})
			.on("mouseout", function(d) {
				div.transition()		
	                .duration(500)		
	                .style("opacity", 0);
			});
			 
			 
		svg.append("g")
			.attr("class", "x axis")
			.attr("transform", "translate(0, " + (height - margin.top - margin.bottom) + ")")
			.transition()
			.call(xAxis);
			 
		svg.append("g")
			.attr("class", "y axis")
			.transition()
			.call(yAxis)
			.selectAll('.y .tick text')
			.call(function(t){                
	            t.each(function(d){ // for each one
	            	var self = d3.select(this);
	            	var s = self.text().split('#');  // get the text and split it
	            	self.text(''); // clear it out
	            	self.append("tspan") // insert two tspans
	                	.attr("x", 0)
	                	.attr("dy",".8em")
	                	.text(s[0]);
	              	self.append("tspan")
	                	.attr("x", 0)
	                	.attr("dy",".8em")
	                	.text(s[1]);
	            })
            });
			 
		return gantt;
    };

    gantt.margin = function(value) {
		if (!arguments.length)
		    return margin;
		margin = value;
		return gantt;
    };

    gantt.timeDomain = function(value) {
		if (!arguments.length)
		    return [ timeDomainStart, timeDomainEnd ];
		timeDomainStart = +value[0], timeDomainEnd = +value[1];
		return gantt;
    };

    /**
     * @param {string}
     *                vale The value can be "fit" - the domain fits the data or
     *                "fixed" - fixed domain.
     */
    gantt.timeDomainMode = function(value) {
	if (!arguments.length)
	    return timeDomainMode;
        timeDomainMode = value;
        return gantt;

    };

    gantt.taskTypes = function(value) {
		if (!arguments.length)
		    return taskTypes;
		taskTypes = value;
		return gantt;
    };

    
    gantt.taskStatus = function(value) {
		if (!arguments.length)
		    return taskStatus;
		taskStatus = value;
		return gantt;
    };

    gantt.width = function(value) {
		if (!arguments.length)
		    return width;
		width = +value;
		return gantt;	
    };

    gantt.height = function(value) {
		if (!arguments.length)
		    return height;
		height = +value;
		return gantt;
    };

    gantt.tickFormat = function(value) {
		if (!arguments.length)
		    return tickFormat;
		tickFormat = value;
		return gantt;
    };


    
    return gantt;
};