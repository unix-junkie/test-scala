/*-
 * $Id$
 */
package com.example

import java.awt.Color.BLACK
import java.awt.Color.GREEN
import java.awt.Rectangle
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.Comparable

import scala.annotation.implicitNotFound
import scala.math.signum
import scala.math.sin

import org.apache.batik.dom.GenericDOMImplementation
import org.apache.batik.svggen.SVGGraphics2D
import org.jfree.chart.ChartFactory.createXYAreaChart
import org.jfree.chart.ChartFactory.createXYLineChart
import org.jfree.chart.ChartUtilities.saveChartAsPNG
import org.jfree.chart.plot.PlotOrientation.VERTICAL
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.JFreeChart
import org.jfree.data.function.Function2D
import org.jfree.data.general.DatasetUtilities.sampleFunction2D
import org.jfree.data.xy.XYDataset

/**
 * @author Andrew ``Bass'' Shcheglov (andrewbass@gmail.com)
 */
object Main {
	def main(args: Array[String]): Unit = {
		/**
		 * Normalized rectangular function.
		 *
		 * @param x
		 * @see https://en.wikipedia.org/wiki/Rectangular_function
		 */
		def Π(x: Double): Double = (signum(x + 0.5) - signum(x - 0.5)) / 2

		/**
		 * @param x
		 */
		def function(x: Double): Double = {
			val left = -3.0
			var right = 3.0
			def noise(t: Double, amplitude: Double): Double = amplitude * (1 + sin(t))
			def signal(t: Double): Double = 1 + .1 * sin(1 / t)
			noise(100 * x, 0.02) + signal(.1 * x) * Π((x - (right + left) / 2) / (right - left))
		}
		
		val legend = "f(x)"
		val displayLegend = false
		val dataset = sampleFunction(function, -4.0, 4.0, 400, "f(x)")

		val title = "Басс неебически крут"
		val fill = true
		val chart = fill match {
			case true => createXYAreaChart(title, "X", "Y", dataset, VERTICAL, displayLegend, true, false)
			case _ => createXYLineChart(title, "X", "Y", dataset, VERTICAL, displayLegend, true, false)
		}
		val plot = chart.getPlot()
		plot.setBackgroundPaint(BLACK)
		plot match {
			case xyPlot: XYPlot => xyPlot.getRenderer().setSeriesPaint(0, GREEN)
		}

		val width = 1440
		val height = 300
		savePng(chart, "test.png", width, height)
		saveSvg(chart, "test.svg", width, height)
	}

	/**
	 * @param f
	 * @param start
	 * @param end
	 * @param samples
	 * @param seriesKey
	 */
	private def sampleFunction(f: Double => Double,
			start: Double,
			end: Double,
			samples: Int,
			seriesKey: Comparable[_]): XYDataset = {
		/**
		 * @param f
		 */
		def toFunction2D(f: Double => Double): Function2D = new Function2D {
			def getValue(x: Double): Double = f(x)
		}
		sampleFunction2D(toFunction2D(f), start, end, samples, seriesKey)
	}
	
	/**
	 * @param chart
	 * @param filename
	 * @param width
	 * @param height
	 */
	private def savePng(chart: JFreeChart, filename: String, width: Int, height: Int): Unit = {
		saveChartAsPNG(new File(filename), chart, width, height);
	}
	
	/**
	 * @param chart
	 * @param filename
	 * @param width
	 * @param height
	 */
	private def saveSvg(chart: JFreeChart, filename: String, width: Int, height: Int): Unit = {
		val domImpl = GenericDOMImplementation.getDOMImplementation()
		val document = domImpl.createDocument(null, "svg", null)
		val svgGraphics = new SVGGraphics2D(document)
		chart.draw(svgGraphics, new Rectangle(width, height))
		
		val out = new OutputStreamWriter(new FileOutputStream(filename), "UTF-8")
		svgGraphics.stream(out, true)
		out.flush()
		out.close()
	}
}