/*-
 * $Id: 0c6dc50c803de1ab7d4177d5c5fd0c9eb41a78c6 $
 */
package com.example

import java.awt.Color.BLACK
import java.awt.Color.GREEN
import java.awt.Rectangle
import java.io.File.separatorChar
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.Comparable

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
import org.jzy3d.chart.ChartLauncher.instructions
import org.jzy3d.chart.ChartLauncher.openChart
import org.jzy3d.chart.ChartLauncher.screenshot
import org.jzy3d.chart.Chart
import org.jzy3d.colors.colormaps.ColorMapRainbow
import org.jzy3d.colors.ColorMapper
import org.jzy3d.maths.Range
import org.jzy3d.plot3d.builder.Builder.buildOrthonormal
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid
import org.jzy3d.plot3d.builder.Mapper
import org.jzy3d.plot3d.rendering.canvas.Quality.Advanced

/**
 * @author Andrew ``Bass'' Shcheglov (andrewbass@gmail.com)
 * @todo Pseudo-3D charts can be implemented using XYPlot and XYBlockRenderer
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

		val signalStart = -3.0
		val signalEnd = 3.0

		val samples = 400
		
		val width = 1440
		val height2d = 300
		val height3d = 900

		val title = "Signal"

		/**
		 * @param x
		 */
		def function2d(x: Double): Double = {
			def noise(t: Double, amplitude: Double): Double = amplitude * (1 + sin(t))
			def signal(t: Double): Double = 1 + .1 * sin(1 / t)
			noise(100 * x, 0.02) + signal(.1 * x) * Π((x - (signalEnd + signalStart) / 2) / (signalEnd - signalStart))
		}

		/**
		 * @param x
		 * @param y
		 */
		def function3d(x: Double, y: Double): Double = {
			function2d(x) * Π((y - (signalEnd + signalStart) / 2) / (signalEnd - signalStart))
		}
	
		val minArgument = signalStart - 1.0
		val maxArgument = signalEnd + 1.0

		plot2d(function2d, minArgument, maxArgument, samples, width, height2d, "test", title)
		plot3d(function3d, minArgument, maxArgument, samples, width, height3d, "." + separatorChar + "test3d.png", title)
	}

	/**
	 * @param function2d
	 * @param minArgument
	 * @param maxArgument
	 * @param samples
	 * @param screenshotWidth
	 * @param screenshotHeight
	 * @param screenshotBasename
	 * @param chartTitle
	 */
	private def plot2d(function2d: Double => Double,
			minArgument: Double,
			maxArgument: Double,
			samples: Int,
			screenshotWidth: Int,
			screenshotHeight: Int,
			screenshotBasename: String,
			chartTitle: String): Unit = {
		val legend = "f(x)"
		val displayLegend = false
		val dataset = sampleFunction(function2d, minArgument, maxArgument, samples, "f(x)")

		val fill = true
		val chart = fill match {
			case true => createXYAreaChart(chartTitle, "X", "Y", dataset, VERTICAL, displayLegend, true, false)
			case _ => createXYLineChart(chartTitle, "X", "Y", dataset, VERTICAL, displayLegend, true, false)
		}
		val plot = chart.getPlot()
		plot.setBackgroundPaint(BLACK)
		plot match {
			case xyPlot: XYPlot => xyPlot.getRenderer().setSeriesPaint(0, GREEN)
		}

		savePng(chart, screenshotBasename + ".png", screenshotWidth, screenshotHeight)
		saveSvg(chart, screenshotBasename + ".svg", screenshotWidth, screenshotHeight)
	}

	/**
	 * @param f the function to plot
	 * @param minArgument
	 * @param maxArgument
	 * @param samples
	 * @param screenshotWidth
	 * @param screenshotHeight
	 * @param screenshotFilename filename with a mandatory parent dir (otherwise an NPE is thrown).
	 * @param windowTitle 
	 */
	private def plot3d(function3d: (Double, Double) => Double,
			minArgument: Double,
			maxArgument: Double,
			samples: Int,
			screenshotWidth: Int,
			screenshotHeight: Int,
			screenshotFilename: String,
			windowTitle: String): Unit = {
		val mapper = new Mapper() {
			override def f(x: Double, y: Double): Double = function3d(x, y)
		}

		val range = new Range(minArgument, maxArgument)

		val surface = buildOrthonormal(new OrthonormalGrid(range, samples, range, samples), mapper)
		val bounds = surface.getBounds()
		surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), bounds.getZmin(), bounds.getZmax(), new org.jzy3d.colors.Color(1, 1, 1, .5f)))
		surface.setFaceDisplayed(true)
		surface.setWireframeDisplayed(false)
//		surface.setWireframeColor(org.jzy3d.colors.Color.WHITE)
		surface.setWireframeColor(org.jzy3d.colors.Color.BLACK)

		val chartType = "awt"
//		val chartType = "swing"
//		val chartType = "newt"
//		val chartType = "offscreen," + screenshotWidth + "," + screenshotHeight
		val chart2 = new Chart(Advanced, chartType)
		chart2.getScene().getGraph().add(surface)
//		chart2.getView().setBackgroundColor(org.jzy3d.colors.Color.BLACK)

		val offscreen = chartType.startsWith("offscreen")
		if (!offscreen) {
			instructions()
			openChart(chart2, new Rectangle(screenshotWidth, screenshotHeight), windowTitle)
		}
		screenshot(chart2, screenshotFilename)
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