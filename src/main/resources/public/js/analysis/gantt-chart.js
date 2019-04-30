d3.gantt = function(tasks) {
    
    var bar = null;
    var FIT_TIME_DOMAIN_MODE = "fit";
    var FIXED_TIME_DOMAIN_MODE = "fixed";
    
    //var margin = {top : 20, right : 40, bottom : 20, left : 150};
    var margin = {top : 20, right : 40, bottom : 20, left : 200},
    margin2 = {top: 20, right: 1050, bottom: 20, left: 150};
    
    var dataStartDate, dataEndDate, offset;
    var fileName = new Array();
    
    if(tasks == undefined || tasks.length <= 0){
        dataStartDate = new Date();
        offset = 0;
    }else{
        dataStartDate = new Date(tasks[0].arrestTime);
        offset = tasks[tasks.length - 1].relativeEndTime;
        for (p in tasks){
            fileName.push(tasks[p].pid);
        }
    }
    
    var timeDomainStart = d3.timeSecond(dataStartDate);
    var timeDomainEnd = d3.timeSecond.offset(dataStartDate, offset);
    
    var formatDate = d3.timeFormat("%m/%d/%Y %H:%M:%S");
    
    var timeDomainMode = FIT_TIME_DOMAIN_MODE;// fixed or fit
    var taskTypes = tasks.map(a => a.pid + '#' + a.arrestTime + '#' + a.filetype);
    var patientFile = [];
    var taskStatus = [];
    
    var height = document.getElementById("chart-container").clientHeight - margin.top - margin.bottom,
    width = document.getElementById("chart-container").clientWidth - margin.right - margin.left,
    width2 = + document.getElementById("chart-container").clientWidth - margin2.left - margin2.right,
    height2 = + document.getElementById("chart-container").clientHeight - margin2.top - margin2.bottom;
    var tickFormat = "%j";
    
    var keyFunction = function(d) {
        //console.log(d.relativeStartTime + d.arrestTime + d.relativeEndTime + d.pid + d.filetype);
        return d.relativeStartTime + d.arrestTime + d.relativeEndTime + d.pid + d.filetype;
    };
    
    var rectTransform = function(d) {
        //console.log("Rect x: " + d3.timeSecond.offset(timeDomainStart, d.relativeStartTime) + " y: " + d.pid + "#" + d.arrestTime + "#" + d.filetype);
        //console.log("y dom: " + y.domain());
        //console.log("xTrans x: " + x(d3.timeSecond.offset(timeDomainStart, d.relativeStartTime)) + " y: " + y(d.pid + "#" + d.arrestTime + "#" + d.filetype));
        return "translate(" + x(d3.timeSecond.offset(timeDomainStart, d.relativeStartTime)) + "," + y(d.pid + "#" + d.arrestTime + "#" + d.filetype) + ")";
    };
    
    
    var x = d3.scaleTime().domain([ timeDomainStart, timeDomainEnd ]).range([ 0, width ]).clamp(true),
    y = d3.scaleBand().domain(taskTypes).rangeRound([ 0, height - margin.top - margin.bottom ], .1),
    x2 = d3.scaleTime().domain([ timeDomainStart, timeDomainEnd ]).range([ 0, width2 ]).clamp(true),
    y2 = d3.scaleBand().domain(taskTypes).rangeRound([ 0, height2 - margin2.top - margin2.bottom ], .1);
    //console.log("domainy2: " + taskTypes);
    
   	// custom invert function
    y2.invertCust = (function(){
                     console.log("in invertCust");
                     var domain = y2.domain();
                     var range = y2.range();
                     //console.log("d: " + domain + " r: " + range);
                     var scale = d3.scaleQuantize().domain(range).range(domain);
                     
                     return function(val){
                     return scale(val);
                     }
                     })()
    
    var xAxisConfig = d3.axisBottom(x).tickFormat(d3.timeFormat(tickFormat))
    .tickSize(8).tickPadding(8),
    yAxisConfig = d3.axisLeft(y).tickSize(5).tickPadding(10),
    xAxisConfig2 = d3.axisBottom(x2).tickFormat(d3.timeFormat(tickFormat))
    .tickSize(8).tickPadding(8),
    yAxisConfig2 = d3.axisLeft(y2).tickSize(5).tickPadding(10);
    
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
        }
    };
    
    var initAxis = function() {
        x = d3.scaleTime().domain([ timeDomainStart, timeDomainEnd ]).range([ 0, width ]).clamp(true);
        // console.log("init");
        // console.log(timeDomainEnd);
        // console.log(timeDomainEnd - timeDomainStart);
        y = d3.scaleBand().domain(taskTypes).rangeRound([ 0, height - margin.top - margin.bottom ], .1);
        x2 = d3.scaleTime().domain([ timeDomainStart, timeDomainEnd ]).range([ 0, width2 ]).clamp(true);
        y2 = d3.scaleBand().domain(taskTypes).rangeRound([ 0, height2 - margin2.top - margin2.bottom ], .1);
        
        var xAxisConfig = d3.axisBottom(x).tickFormat(d3.timeFormat(tickFormat))
        .tickSize(8).tickPadding(8),
        yAxisConfig = d3.axisLeft(y).tickSize(5).tickPadding(10),
        xAxisConfig2 = d3.axisBottom(x2).tickFormat(d3.timeFormat(tickFormat))
        .tickSize(8).tickPadding(8),
        yAxisConfig2 = d3.axisLeft(y2).tickSize(5).tickPadding(10);
    };
    
    var wrapLabel = function(text, width){
        //console.log(width);
        //console.log(text);
        text.each(function(){ // for each label
                  var self = d3.select(this);
                  var s = self.text().split('#');  // get the text and split it
                  //console.log(self);
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
        // .append("g")
        //        .attr("class", "gantt-chart")
        // .attr("width", width + margin.left + margin.right)
        // .attr("height", height + margin.top + margin.bottom)
        // .attr("transform", "translate(" + margin.left + ", " + margin.top + ")");
        
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
        
        // Create brush along x axis
        var brushX = d3.brushX()                   // Add the brush feature using the d3.brush function
        .extent( [ [0,0], [width,height] ] )  // initialise the brush area: start at 0,0 and finishes at width,height: it means I select the whole graph area
        .on("end", updateChart)              // Each time the brush selection changes, trigger the 'updateChart' function
        
        // Create brush along y axis
        var brushY = d3.brushY()
        .extent([[0, 0], [width2, height]])
        .on("brush end", brushed);
        
        var zoom = d3.zoom()
        .scaleExtent([1, Infinity])
        .translateExtent([[0, 0], [width, height]])
        .extent([[0, 0], [width, height]])
        .on("zoom", zoomed);
        
        // Create the line variable: where both the line and the brush take place
        var focus = svg.append('g')
        .attr("clip-path", "url(#clip)")
        .attr("class", "focus")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
        
        var context = svg.append('g')
        .attr("class", "context")
        .attr("transform", "translate(" + margin2.left + "," + margin2.top + ")");
        
        //var area = focus.selectAll(".chart")
        // BACKUP
        /*var bar = focus.selectAll(".chart")
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
              }
              return x(d.relativeEndTime - d.relativeStartTime);
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
            });*/
        
        // Add the brushing
        focus
        .append("g")
        .attr("class", "brushX")
        .call(brushX);
        
        xAxis = focus.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0, " + (height - margin.top - margin.bottom) + ")")
        .call(xAxisConfig);
        
        yAxis = focus.append("g")
        .attr("class", "y axis one")
        .call(yAxisConfig)
        
        console.log("Y AXIS DEBUG");
        console.log(focus.selectAll('.y .tick text'));
        focus.selectAll('.one .tick text')
        .remove()
        
        // xAxis2 = context.append("g")
        // 	.attr("class", "x axis two")
        // 	.attr("transform", "translate(0, " + (height2 - margin2.top - margin2.bottom) + ")")
        // 	.call(xAxisConfig2);
        
        context.append("g")
        .attr("class", "brushY")
        .call(brushY)
        .call(brushY.move, y.range());
        
        /* BACKUP
         yAxis2 = context.append("g")
         .attr("class", "y axis two")
         .transition()
         .call(yAxisConfig2)
         .selectAll('.y .tick text')
         .call(wrapLabel, y2.bandwidth());*/
        
        yAxis2 = context.append("g")
        .attr("class", "y axis two")
        .call(yAxisConfig2)
        
        /* BACKUP
         context.selectAll('.y .tick text')
         .call(wrapLabel, y2.bandwidth());*/
        
        context.selectAll('.two .tick text')
        .text(function(d){
              var s = d.split("#");
              return s[0]+" "+s[2];
              });
        
        // A function that set idleTimeOut to null
        var idleTimeout
        function idled() { idleTimeout = null; }
        
        
        // Get Json data from medication table By Id and redirect
        svg.select(".two").selectAll(".tick")
        .on("click",function () {
            console.log("IN CLICK");
            window.location.href = '/analysis/medInfo/' + $(this).text().split(' ')[0];
            });
        
        /*svg.append("rect")
	        .attr("class", "zoom")
	        .attr("width", width)
	        .attr("height", height)
	        .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
	        .call(zoom);*/
        
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
                updatedTimestamp = x.invert(extent[1]);
                x.domain([ x.invert(0), updatedTimestamp ])
                focus.select(".brush").call(brushX.move, null) // This remove the grey brush area as soon as the selection has been done
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
                         return x(d.relativeEndTime - d.relativeStartTime);
                         })
                   });
        }
        
        /*	    BACKUP
         function brushed() {
         console.log("340");
         if (d3.event.sourceEvent && d3.event.sourceEvent.type === "zoom") return; // ignore brush-by-zoom
         var s = d3.event.selection || y2.range();
         
         console.log(s);
         y2.invertCust = (function(){
         //console.log("brushed invert");
         var domain = y2.domain();
         var range = y2.range();
         //console.log("cust d: " + domain + " r: " + range);
         var scale = d3.scaleQuantize().domain(range).range(domain);
         
         return function(val){
         //console.log(val);
         //console.log(scale(val));
         return scale(val);
         }
         })()
         console.log("s1 " + s[1] + " s0 " +  s[0]);
         updatedS = Array(s[1] - s[0] + 1).fill().map((item, index) => s[0] + index);
         //console.log(updatedS);
         
         y.domain(updatedS.map(y2.invertCust, y2));
         //console.log(y.domain());
         
         /* BACKUP
         bar
         .transition()
         .duration(1000)
         .attr("transform", rectTransform)
         .attr("height", function(d) {
         console.log("ybw: " + y.bandwidth());
         return y.bandwidth();
         })*/
        /*		  console.log("373");
         yAxis.transition().duration(1000).call(yAxisConfig);
         console.log("375");
         bar
         .transition()
         .duration(1000)
         .attr("transform", rectTransform)
         .attr("height", function(d) {
         console.log("ybw: " + y.bandwidth());
         return y.bandwidth();
         })
         
         
         //focus.select(".area").attr("d", area);
         //focus.select(".y").call(yAxis);
         console.log(yAxis);
         
         //xAxis.transition().duration(1000).call(d3.axisBottom(x))
         console.log("418");
         /*svg.select(".zoom").call(zoom.transform, d3.zoomIdentity
         .scale(height / (s[1] - s[0]))
         .translate(0, -s[0]));*/
        /*		  console.log("422");
         
         }
         */
        
        function brushed() {
            console.log("340");
            if (d3.event.sourceEvent && d3.event.sourceEvent.type === "zoom") return; // ignore brush-by-zoom
            var s = d3.event.selection || y2.range();
            
            console.log(s);
            y2.invertCust = (function(){
                             //console.log("brushed invert");
                             var domain = y2.domain();
                             var range = y2.range();
                             //console.log("cust d: " + domain + " r: " + range);
                             var scale = d3.scaleQuantize().domain(range).range(domain);
                             
                             return function(val){
                             //console.log(val);
                             //console.log(scale(val));
                             return scale(val);
                             }
                             })()
            console.log("s1 " + s[1] + " s0 " +  s[0]);
            updatedS = Array(s[1] - s[0] + 1).fill().map((item, index) => s[0] + index);
            //console.log(updatedS);
            
            y.domain(updatedS.map(y2.invertCust, y2));
            //console.log(y.domain());
            
            /* BACKUP
             bar
             .transition()
             .duration(1000)
             .attr("transform", rectTransform)
             .attr("height", function(d) {
             console.log("ybw: " + y.bandwidth());
             return y.bandwidth();
             })*/
            console.log("373");
            xAxis.transition().duration(1000).call(d3.axisBottom(x).tickFormat(d3.timeFormat(tickFormat)))
            yAxis.transition().duration(1000).call(yAxisConfig);
            console.log("375");
            
            var filteredTasks = tasks.splice(tasks.length);
            if (tasks.length != y.domain().length){
                filteredTasks = tasks.filter(function(value, index, arr){
                    var str = value.pid + "#" + value.arrestTime + "#" + value.filetype;
                    console.log(str);
                    return (y.domain().indexOf(str) > -1);
                });
            }
            focus.selectAll("rect.databar")
                .remove()
            bar = focus.selectAll(".chart")
                .data(filteredTasks, keyFunction)
                .enter()
                .append("rect")
                .attr("class", "alek")
                .attr("rx", 5)
                .attr("ry", 5)
                .attr("class", function(d){
                      if(taskStatus[d.status] == null)
                        { return "databar " + "bar";}
                      return "databar " + taskStatus[d.status];
                      })
                .attr("y", 0)
                .attr("transform", rectTransform)
                .attr("height", function(d) { return y.bandwidth(); })
                .attr("width", function(d) {
                      var x = d3.scaleTime().domain([ 0, (timeDomainEnd-timeDomainStart)/1000 ]).range([ 0, width ]).clamp(true);
                      if(d.fname === 'PUH-2017-292_01noar_b.csv') {
                      }
                      return x(d.relativeEndTime - d.relativeStartTime);
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
            
            
            //focus.select(".area").attr("d", area);
            //focus.select(".y").call(yAxis);
            console.log(yAxis);
            
            //xAxis.transition().duration(1000).call(d3.axisBottom(x))
            console.log("418");
            /*svg.select(".zoom").call(zoom.transform, d3.zoomIdentity
             .scale(height / (s[1] - s[0]))
             .translate(0, -s[0]));*/
            console.log("422");
            
        }
        
        
        function zoomed() {
            console.log("426");
            if (d3.event.sourceEvent && d3.event.sourceEvent.type === "brushY") return; // ignore zoom-by-brush
            var t = d3.event.transform;
            //y.domain(t.rescaleY(y2).domain());
            //focus.select(".area").attr("d", area);
            //focus.select(".y").call(yAxis);
            //context.select(".brushY").call(brushY.move, y.range().map(t.invertCust, t));
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