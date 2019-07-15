package com.db.williamchart

import com.db.williamchart.animation.NoAnimation
import com.db.williamchart.data.ChartLabel
import com.db.williamchart.data.ChartSet
import com.db.williamchart.data.Line
import com.db.williamchart.data.Point
import com.db.williamchart.renderer.ChartRenderer
import com.db.williamchart.view.ChartView.Axis
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations


class ChartRendererTest {

    private val defSize = 1

    private val defPadding = 0

    private val defLabels = 0F

    private val defAxis = Axis.XY

    @Mock private lateinit var view: ChartContract.View

    @Mock private lateinit var painter: Painter

    @Captor private lateinit var setCaptor: ArgumentCaptor<ChartSet>

    @Captor private lateinit var labelsCaptor: ArgumentCaptor<List<ChartLabel>>

    private lateinit var renderer: ChartRenderer

    @Before fun setup() {

        MockitoAnnotations.initMocks(this)
        renderer = ChartRenderer(view, painter, NoAnimation())
    }

    @Test(expected = IllegalArgumentException::class)
    fun addDataWithOneEntry_ThrowIlegalArgument() {

        val set = Line()
        set.add(Point("label", 1f))

        renderer.add(set)
        renderer.preDraw(defSize, defSize, defPadding, defPadding, defPadding, defPadding, defAxis, defLabels)
    }

    @Test
    fun noData_SkipPreDraw() {

        renderer.draw()

        verify(view, times(0)).drawData(any(), any(), any(), any(), any())
    }

    @Test
    fun addData_RetrievesProperData() {

        val set = Line()
        set.add(Point("label0", 0f))
        set.add(Point("label1", 1f))

        renderer.add(set)
        renderer.preDraw(defSize, defSize, defPadding, defPadding, defPadding, defPadding, defAxis, defLabels)
        renderer.draw()

        verify(view).drawData(any(), any(), any(), any(), capture(setCaptor))
        assertEquals(1F, setCaptor.value.entries[0].y)
        assertEquals(0F, setCaptor.value.entries[1].y)
    }

    @Test
    fun chartWithAxisXY_XYDisplayed() {

        val set = Line()
        set.add(Point("label0", 0f))
        set.add(Point("label1", 1f))

        renderer.add(set)
        renderer.draw()

        verify(view, times(2)).drawLabels(any())
    }

    @Test
    fun chartWithAxisX_XDisplayed() {

        val set = Line()
        set.add(Point("label0", 0f))
        set.add(Point("label1", 1f))

        renderer.add(set)
        renderer.preDraw(defSize, defSize, defPadding, defPadding, defPadding, defPadding,
                Axis.X, defLabels)
        renderer.draw()

        verify(view, times(1)).drawLabels(any())
    }

    @Test
    fun chartWithAxisY_YDisplayed() {

        val set = Line()
        set.add(Point("label0", 0f))
        set.add(Point("label1", 1f))

        renderer.add(set)
        renderer.preDraw(defSize, defSize, defPadding, defPadding, defPadding, defPadding,
                Axis.Y, defLabels)
        renderer.draw()

        verify(view, times(1)).drawLabels(any())
    }

    @Test
    fun chartWithAxisXY_XLabelsInOrder() {

        val set = Line()
        set.add(Point("label0", 0f))
        set.add(Point("label1", 1f))

        renderer.add(set)
        renderer.preDraw(defSize, defSize, defPadding, defPadding, defPadding, defPadding, defAxis, defLabels)
        renderer.draw()

        verify(view, times(2)).drawLabels(capture(labelsCaptor))

        val labels = labelsCaptor.allValues[0]
        for (i in 0..set.entries.size - 2)
            assertTrue(labels[i].x < labels[i+1].x)
    }

    @Test
    fun chartWithAxisXY_YLabelsInOrder() {

        val set = Line()
        set.add(Point("label0", 0f))
        set.add(Point("label1", 1f))

        renderer.add(set)
        renderer.preDraw(defSize, defSize, defPadding, defPadding, defPadding, defPadding, defAxis, defLabels)
        renderer.draw()

        verify(view, times(2)).drawLabels(capture(labelsCaptor))

        val labels = labelsCaptor.allValues[1]
        for (i in 0..set.entries.size - 2)
            assertTrue(labels[i].y > labels[i+1].y)
    }

    @Test
    fun noXY_XYNotDisplayed() {

        val set = Line()
        set.add(Point("label0", 0f))
        set.add(Point("label1", 1f))

        renderer.add(set)
        renderer.preDraw(defSize, defSize, defPadding, defPadding, defPadding, defPadding,
                Axis.NONE, defLabels)
        renderer.draw()

        verify(view, times(0)).drawLabels(any())
    }

    @Test
    fun noXY_ChartDataFulfilsFrameWidth() {

        val labelWidth = 10F
        `when`(painter.measureLabelWidth(any(), any())).thenReturn(labelWidth)

        val set = Line()
        set.add(Point("label0", 0f))
        set.add(Point("label1", 1f))

        renderer.add(set)
        renderer.preDraw(defSize, defSize, defPadding, defPadding, defPadding, defPadding,
                Axis.NONE, defLabels)
        renderer.draw()

        verify(view).drawData(any(), any(), any(), any(), capture(setCaptor))
        assertEquals(0F, setCaptor.value.entries.first().x)
        assertEquals(defSize.toFloat(), setCaptor.value.entries.last().x)
    }

    @Test
    fun noPadding_LabelsFulfilFrame() {

        val width = 1000
        val height = 1000
        val labelWidth = 10F
        val labelheight = 10F
        `when`(painter.measureLabelWidth(any(), any())).thenReturn(labelWidth)
        `when`(painter.measureLabelHeight(any())).thenReturn(labelheight)

        val set = Line()
        set.add(Point("label0", 0f))
        set.add(Point("label1", 1f))

        renderer.add(set)
        renderer.preDraw(width, height,
                defPadding, defPadding, defPadding, defPadding, defAxis, defLabels)
        renderer.draw()

        verify(view, times(2)).drawLabels(capture(labelsCaptor))

        val xLabels = labelsCaptor.allValues[0]
        assertEquals(width.toFloat(), xLabels.last().x + labelWidth / 2)
        assertEquals(height.toFloat(), xLabels.last().y)

        val yLabels = labelsCaptor.allValues[1]
        assertEquals(0F, yLabels.last().x - labelWidth / 2)
        assertEquals(0F, Math.round(yLabels.last().y - labelheight).toFloat())
    }

    @Test
    fun yStartsAtZero_YLabelIsZero() {

        val set = Line()
        set.add(Point("label0", 1f))
        set.add(Point("label1", 2f))

        renderer.yAtZero = true
        renderer.add(set)
        renderer.preDraw(defSize, defSize, defPadding, defPadding, defPadding, defPadding,
                Axis.Y, defLabels)
        renderer.draw()

        verify(view).drawLabels(capture(labelsCaptor))
        assertEquals("0.0", labelsCaptor.value.first().label)
    }

}

/**
 * Returns ArgumentCaptor.capture() as nullable type to avoid java.lang.IllegalStateException
 * when null is returned.
 */
fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()