d3.gantt = function(tasks) {
	// test comment

	var FIT_TIME_DOMAIN_MODE = "fit";
	var FIXED_TIME_DOMAIN_MODE = "fixed";

	var margin = {
		top : 20,
		right : 40,
		bottom : 20,
		left : 150
	};

	var dataStartDate, dataEndDate, offset;
	var fileName = new Array();

	if(tasks == undefined || tasks.length <= 0){
		dataStartDate = new Date();
		offset = 0;
		// console.log("if before init");
	}else{
		dataStartDate = new Date(tasks[0].arrestTime);
		offset = tasks[tasks.length - 1].relativeEndTime;
		for (p in tasks){
			fileName.push(tasks[p].pid);
		}
	}
	console.log("dataStartDate");
	console.log("Date : " + dataStartDate.toString("MMMM yyyy"));

	var timeDomainStart = d3.timeSecond(dataStartDate);
	var timeDomainEnd = d3.timeSecond.offset(dataStartDate, offset);

	var formatDate = d3.timeFormat("%m/%d/%Y %H:%M:%S");

	// console.log("s time format: " + typeof formatDate(timeDomainStart));
	// console.log("e time format: " + typeof formatDate(timeDomainEnd));

	// console.log("timeDomainStart" + timeDomainStart);
	// console.log("timeDomainEnd" + timeDomainEnd);
	var timeDomainMode = FIT_TIME_DOMAIN_MODE;// fixed or fit
	var taskTypes = [];
	var patientFile = [];
	var taskStatus = [];
	var height = document.getElementById("chart-container").clientHeight - margin.top - margin.bottom;
	var width = document.getElementById("chart-container").clientWidth - margin.right - margin.left;
	var tickFormat = "%j";

	var keyFunction = function(d) {
		//console.log(d);
		return d.relativeStartTime + d.arrestTime + d.relativeEndTime + d.pid + d.filetype;
	};

	var rectTransform = function(d) {
		//console.log(d);
		return "translate(" + x(d3.timeSecond.offset(timeDomainStart, d.relativeStartTime)) + "," + y(d.pid + "#" + d.arrestTime + "#" + d.filetype) + ")";
	};


	var x = d3.scaleTime().domain([ timeDomainStart, timeDomainEnd ]).range([ 0, width ]).clamp(true);
	var y = d3.scaleBand().domain(taskTypes).rangeRound([ 0, height - margin.top - margin.bottom ], .1);

	var xAxisConfig = d3.axisBottom(x).tickFormat(d3.timeFormat(tickFormat))
		.tickSize(8).tickPadding(8);
	var yAxisConfig = d3.axisLeft(y);//.tickSize(0);

	var initTimeDomain = function(tasks) {
		if (timeDomainMode === FIT_TIME_DOMAIN_MODE) {
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
				dataStartDate = 0;
				offset = tasks[tasks.length - 1].relativeEndTime;
			}
			timeDomainStart = d3.timeSecond(dataStartDate);
			timeDomainEnd = d3.timeSecond(d3.timeSecond.offset(dataStartDate, offset) - timeDomainStart);

			// console.log("init: " + timeDomainStart + " - " + timeDomainEnd);
		}
	};

	var initAxis = function() {
		x = d3.scaleTime().domain([ timeDomainStart, timeDomainEnd ]).range([ 0, width ]).clamp(true);
		//x = d3.scaleTime().domain([ 0, (timeDomainEnd-timeDomainStart) ]).range([ 0, width ]).clamp(true);
		console.log("init");
		console.log(timeDomainEnd);
		console.log(timeDomainEnd - timeDomainStart);
		y = d3.scaleBand().domain(taskTypes).rangeRound([ 0, height - margin.top - margin.bottom ], .1);

		xAxisConfig = d3.axisBottom(x).tickFormat(d3.timeFormat(tickFormat))
			.tickSize(8).tickPadding(8);
		yAxisConfig = d3.axisLeft(y).tickSize(0);
	};

	var wrapLabel = function(text, width){
		//console.log(width);
		//console.log(text);
		text.each(function(){ // for each label
			var self = d3.select(this);
			var s = self.text().split('#');  // get the text and split it
			console.log(self);
			//console.log(self.text());
			/*self.text(''); // clear it out
            self.append("tspan") // insert two tspans
                .attr("x", -10)
                .attr("dy","-0.4em")
                .text(s[0] + " " + s[2]);
              self.append("tspan")
                .attr("x", -10)
                .attr("dy","1.4em")
                .text(s[1]);*/
		})
	};

	function gantt(tasks) {

		d3.select("#PatientTimeLine").remove();

		initTimeDomain(tasks);
		initAxis();

		var svg = d3.select("#chart-container")
			.append("svg")
			.attr("id", "PatientTimeLine")
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

		// Add a clipPath: everything out of this area won't be drawn.
		var clip = svg.append("defs").append("svg:clipPath")
			.attr("id", "clip")
			.append("svg:rect")
			.attr("width", width )
			.attr("height", height )
			.attr("x", 0)
			.attr("y", 0);

		// Add brushing
		var brush = d3.brushX()                   // Add the brush feature using the d3.brush function
			.extent( [ [0,0], [width,height] ] )  // initialise the brush area: start at 0,0 and finishes at width,height: it means I select the whole graph area
			.on("end", updateChart)              // Each time the brush selection changes, trigger the 'updateChart' function

		// Create the line variable: where both the line and the brush take place
		var line = svg.append('g')
			.attr("clip-path", "url(#clip)")

		var bar = line.selectAll(".chart")
			.data(tasks, keyFunction)
			.enter()
			.append("rect")
			.attr("rx", 5)
			.attr("ry", 5)
			.attr("class", function(d){
				if(taskStatus[d.status] == null){ return "bar";}
				return taskStatus[d.status];
			})
			.attr("y", 0)
			.attr("transform", rectTransform)
			.attr("height", function(d) { return y.bandwidth(); })
			.attr("width", function(d) {
				var x = d3.scaleTime().domain([ 0, (timeDomainEnd-timeDomainStart)/1000 ]).range([ 0, width ]).clamp(true);
				if(d.fname === 'PUH-2017-292_01noar_b.csv') {
					// console.log(d.fname);
					// console.log(x.domain());
					// console.log(d.relativeEndTime - d.relativeStartTime);
					// console.log(x(d.relativeEndTime - d.relativeStartTime));
				}
				return x(d.relativeEndTime - d.relativeStartTime);
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

		// Add the brushing
		line
			.append("g")
			.attr("class", "brush")
			.call(brush);


		xAxis = svg.append("g")
			.attr("class", "x axis")
			.attr("transform", "translate(0, " + (height - margin.top - margin.bottom) + ")")
			.call(xAxisConfig);
		//.transition()
		//.call(xAxisConfig);

		// console.log(xAxis);

		yAxis = svg.append("g")
			.attr("class", "y axis")
			.transition()
			.call(yAxisConfig)
			.selectAll('.y .tick text')
			.call(wrapLabel, y.bandwidth());

		// A function that set idleTimeOut to null
		var idleTimeout
		function idled() { idleTimeout = null; }


		// Get Json data from medication table By Id and redirect
		svg.select(".y").filter(".axis").selectAll(".tick")
			.on("click",function () {
				window.location.href = '/analysis/medInfo/' + $(this).text().split('#')[0];
			});

		// A function that update the chart for given boundaries
		function updateChart() {

			// What are the selected boundaries?
			extent = d3.event.selection
			var updatedTimestamp = null;

			// If no selection, back to initial coordinate. Otherwise, update X axis domain
			if(!extent){
				if (!idleTimeout) return idleTimeout = setTimeout(idled, 350); // This allows to wait a little bit
				x.domain([ 0,8])
			}else{
				// console.log("invert");
				// console.log(x.invert(extent[1]));
				updatedTimestamp = x.invert(extent[1]);
				x.domain([ x.invert(0), updatedTimestamp ])
				line.select(".brush").call(brush.move, null) // This remove the grey brush area as soon as the selection has been done
			}

			// Update axis and line position
			// console.log(xAxis);
			xAxis.transition().duration(1000).call(d3.axisBottom(x).tickFormat(d3.timeFormat(tickFormat)))
			bar
				.transition()
				.duration(1000)
				.attr("transform", rectTransform)
				.attr("width", function(d) {
					var x = d3.scaleTime().domain([ 0, (updatedTimestamp-timeDomainStart)/(1000) ]).range([ 0, width ]).clamp(true);
					if(d.fname === 'PUH-2017-292_01noar_b.csv') {
						// console.log(d.fname);
						// console.log(x.domain());
						// console.log(d.relativeEndTime - d.relativeStartTime);
						// console.log(x(d.relativeEndTime - d.relativeStartTime));
					}
					return x(d.relativeEndTime - d.relativeStartTime);
				})

			// If user double click, reinitialize the chart
			svg.on("dblclick",function(){
				x.domain([ timeDomainStart, timeDomainEnd ])
				xAxis.transition().call(d3.axisBottom(x).tickFormat(d3.timeFormat(tickFormat)))
				bar
					.transition()
					.attr("transform", rectTransform)
					.attr("width", function(d) {
						var x = d3.scaleTime().domain([ 0, (timeDomainEnd-timeDomainStart)/(1000) ]).range([ 0, width ]).clamp(true);
						if(d.fname === 'PUH-2017-292_01noar_b.csv') {
							// console.log(d.fname);
							// console.log(x.domain());
							// console.log(d.relativeEndTime - d.relativeStartTime);
							// console.log(x(d.relativeEndTime - d.relativeStartTime));
						}
						return x(d.relativeEndTime - d.relativeStartTime);
					})
			});
		}

		return gantt;

	};

	//   gantt.redraw = function(tasks) {

	// /*initTimeDomain();
	// initAxis();

	//    var svg = d3.select("svg");

	//    var ganttChartGroup = svg.select(".gantt-chart");
	//    var rect = ganttChartGroup.selectAll("rect").data(tasks, keyFunction);

	//    rect.enter()
	//    .insert("rect",":first-child")
	//    .attr("rx", 5)
	//    .attr("ry", 5)
	//    .attr("class", function(d){
	//     if(taskStatus[d.status] == null){ return "bar";}
	//     return taskStatus[d.status];
	//    })
	// 	.transition()
	// 	.attr("y", 0)
	// 	.attr("transform", rectTransform)
	// 	.attr("height", function(d) { return y.rangeBand(); })
	// 	.attr("width", function(d) {
	//    	return (x(d.relativeEndTime) - x(d.relativeStartTime));
	//    });

	//    rect.transition()
	//    .attr("transform", rectTransform)
	// 	.attr("height", function(d) { return y.rangeBand(); })
	// 	.attr("width", function(d) {
	//     	return (x(d.relativeEndTime) - x(d.relativeStartTime));
	//    });

	// rect.exit().remove();

	// svg.select(".x").transition().call(xAxis);
	// svg.select(".y").transition().call(yAxis);

	// return gantt;*/

	// initTimeDomain(tasks);
	// initAxis();

	// var svg = d3.select("#chart-container")
	// 	.append("svg")
	// 	.attr("class", "chart")
	// 	.attr("width", width + margin.left + margin.right)
	// 	.attr("height", height + margin.top + margin.bottom)
	// 	.append("g")
	//         .attr("class", "gantt-chart")
	// 	.attr("width", width + margin.left + margin.right)
	// 	.attr("height", height + margin.top + margin.bottom)
	// 	.attr("transform", "translate(" + margin.left + ", " + margin.top + ")");

	// // Define the div for the tooltip
	// var div = d3.select("body").append("div")
	//     .attr("class", "tooltip")
	//     .style("opacity", 0);

	// svg.selectAll(".chart")
	// 	.data(tasks, keyFunction).enter()
	// 	.append("rect")
	// 	.attr("rx", 5)
	//     .attr("ry", 5)
	// 	.attr("class", function(d){
	// 	    if(taskStatus[d.status] == null){ return "bar";}
	// 	    return taskStatus[d.status];
	// 	})
	// 	.attr("y", 0)
	// 	.attr("transform", rectTransform)
	// 	.attr("height", function(d) { return y.rangeBand(); })
	// 	.attr("width", function(d) {
	// 		var x = d3.time.scale().domain([ 0, (timeDomainEnd-timeDomainStart)/(1000) ]).range([ 0, width ]).clamp(true);
	// 		return (x(d.relativeEndTime - d.relativeStartTime));
	// 		//return ((d.relativeEndTime - d.relativeStartTime)/((timeDomainEnd-timeDomainStart)/(1000*60)) * width);
	// 	})
	// 	.on("mouseover", function(d) {
	// 		var startTime = new Date(d.arrestTime);
	// 		startTime.setSeconds( startTime.getSeconds() + d.relativeStartTime );
	// 		var endTime = new Date(d.arrestTime);
	// 		endTime.setSeconds( endTime.getSeconds() + d.relativeEndTime );
	// 		div.transition()
	//                .duration(200)
	//                .style("opacity", .9);
	//            div.html("f: " + d.fname + "<br>" + "s: " + startTime.toISOString() + "<br>" + "e: " + endTime.toISOString())
	//            	.style("left", (d3.event.pageX) + "px")
	// 			.style("top", (d3.event.pageY - 28) + "px");
	// 	})
	// 	.on("mouseout", function(d) {
	// 		div.transition()
	//                .duration(500)
	//                .style("opacity", 0);
	// 	});


	// svg.append("g")
	// 	.attr("class", "x axis")
	// 	.attr("transform", "translate(0, " + (height - margin.top - margin.bottom) + ")")
	// 	.transition()
	// 	.call(xAxisConfig);

	// svg.append("g")
	// 	.attr("class", "y axis")
	// 	.transition()
	// 	.call(yAxisConfig)
	// 	.selectAll('.y .tick text')
	// 	.call(function(t){
	//            t.each(function(d){ // for each one
	//            	var self = d3.select(this);
	//            	var s = self.text().split('#');  // get the text and split it
	//            	self.text(''); // clear it out
	//            	self.append("tspan") // insert two tspans
	//                	.attr("x", 0)
	//                	.attr("dy",".8em")
	//                	.text(s[0]);
	//              	self.append("tspan")
	//                	.attr("x", 0)
	//                	.attr("dy",".8em")
	//                	.text(s[1]);
	//            })
	//           });

	// return gantt;
	//   };

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