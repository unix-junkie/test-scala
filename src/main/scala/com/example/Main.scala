/*-
 * $Id$
 */
package com.example

import java.io.File
import java.util.Date

import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtilities
import org.jfree.data.time.Day
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection

/**
 * @author ashcheglov
 *
 */
object Main {
	def main(args: Array[String]): Unit = {
		val series = new TimeSeries("", classOf[Day])
	        series.add(new Day(new Date("2009/01/01")), 123)
	        series.add(new Day(new Date("2009/01/02")), 133)
	        series.add(new Day(new Date("2009/01/03")), 99)
	        series.add(new Day(new Date("2009/01/04")), 113)
	        series.add(new Day(new Date("2009/01/05")), 159)
	        series.add(new Day(new Date("2009/01/06")), 124)
	        series.add(new Day(new Date("2009/01/07")), 120)
		val dataset=new TimeSeriesCollection()
		dataset.addSeries(series)
 
		val chart = ChartFactory.createTimeSeriesChart("User Participation Chart", "Day", "Number Of Users", dataset, true, true, false)
		ChartUtilities.saveChartAsJPEG(new File("test.jpeg"), chart, 800, 600);
	}
}