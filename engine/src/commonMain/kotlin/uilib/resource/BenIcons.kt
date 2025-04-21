package uilib.resource

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ui.uilib.resource.Icons

@Immutable
object BenIcons : Icons {

    override val name: String = "BenIcons"

    private var _AppLogo: ImageVector? = null
    override val appLogo: ImageVector
        get() {
            if (_AppLogo != null) {
                return _AppLogo!!
            }
            _AppLogo = ImageVector.Builder(
                name = "AppLogo",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(23.48f, 17.81f)
                            horizontalLineTo(67.23f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 72.9f, 23.48f)
                            verticalLineTo(67.23f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 67.23f, 72.9f)
                            horizontalLineTo(23.48f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17.81f, 67.23f)
                            verticalLineTo(23.48f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.48f, 17.81f)
                            close()
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(31.04f, 28.21f)
                            horizontalLineTo(59.67f)
                            arcTo(2.83f, 2.83f, 0f, isMoreThanHalf = false, isPositiveArc = true, 62.5f, 31.04f)
                            verticalLineTo(59.67f)
                            arcTo(2.83f, 2.83f, 0f, isMoreThanHalf = false, isPositiveArc = true, 59.67f, 62.5f)
                            horizontalLineTo(31.04f)
                            arcTo(2.83f, 2.83f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.21f, 59.67f)
                            verticalLineTo(31.04f)
                            arcTo(2.83f, 2.83f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.04f, 28.21f)
                            close()
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(72.9f, 31.48f)
                            lineTo(84.11f, 31.48f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(72.9f, 44.17f)
                            lineTo(84.11f, 44.17f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(72.9f, 56.85f)
                            lineTo(84.11f, 56.85f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(6.59f, 33.86f)
                            lineTo(17.81f, 33.86f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(6.59f, 46.54f)
                            lineTo(17.81f, 46.54f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(6.59f, 59.23f)
                            lineTo(17.81f, 59.23f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(31.48f, 17.81f)
                            lineTo(31.48f, 6.59f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(44.17f, 17.81f)
                            lineTo(44.17f, 6.59f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(56.85f, 17.81f)
                            lineTo(56.85f, 6.59f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(33.86f, 84.11f)
                            lineTo(33.86f, 72.9f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(46.54f, 84.11f)
                            lineTo(46.54f, 72.9f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(59.23f, 84.11f)
                            lineTo(59.23f, 72.9f)
                        }
                    }
                }
            }.build()
            return _AppLogo!!
        }

    private var _Add: ImageVector? = null
    override val add: ImageVector
        get() {
            if (_Add != null) {
                return _Add!!
            }
            _Add = ImageVector.Builder(
                name = "Add",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(23.41f, 11.41f)
                            horizontalLineTo(67.3f)
                            arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 79.3f, 23.41f)
                            verticalLineTo(67.3f)
                            arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 67.3f, 79.3f)
                            horizontalLineTo(23.41f)
                            arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.41f, 67.3f)
                            verticalLineTo(23.41f)
                            arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.41f, 11.41f)
                            close()
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(45.35f, 26.99f)
                            lineTo(45.35f, 63.72f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(26.99f, 45.35f)
                            lineTo(63.72f, 45.35f)
                        }
                    }
                }
            }.build()
            return _Add!!
        }

    private var _Autoscroll: ImageVector? = null
    override val autoscroll: ImageVector
        get() {
            if (_Autoscroll != null) {
                return _Autoscroll!!
            }
            _Autoscroll = ImageVector.Builder(
                name = "Autoscroll",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(23.36f, 40.71f)
                            lineTo(23.36f, 40.71f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(23.36f, 56.29f)
                            lineTo(23.36f, 56.29f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(23.36f, 71.86f)
                            lineTo(67.35f, 71.86f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(48.75f, 19.13f)
                            lineTo(48.75f, 60.21f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(48.75f, 60.21f)
                            lineTo(63.41f, 45.54f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(34.08f, 45.54f)
                            lineTo(48.75f, 60.21f)
                        }
                    }
                }
            }.build()
            return _Autoscroll!!
        }

    private var _Backwards: ImageVector? = null
    override val backwards: ImageVector
        get() {
            if (_Backwards != null) {
                return _Backwards!!
            }
            _Backwards = ImageVector.Builder(
                name = "Backwards",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(71.74f, 74.38f)
                            verticalLineTo(51f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.67f, -5.67f)
                            horizontalLineTo(19f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(18.97f, 45.35f)
                            lineTo(39.3f, 25.02f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(39.3f, 65.69f)
                            lineTo(18.97f, 45.35f)
                        }
                    }
                }
            }.build()
            return _Backwards!!
        }


    private var _Bars: ImageVector? = null
    override val bars: ImageVector
        get() {
            if (_Bars != null) {
                return _Bars!!
            }
            _Bars = ImageVector.Builder(
                name = "Bars",
                defaultWidth = 800.dp,
                defaultHeight = 800.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 6.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(19f, 12.75f)
                    horizontalLineTo(5f)
                    curveTo(4.8011f, 12.75f, 4.6103f, 12.671f, 4.4697f, 12.5303f)
                    curveTo(4.329f, 12.3897f, 4.25f, 12.1989f, 4.25f, 12f)
                    curveTo(4.25f, 11.8011f, 4.329f, 11.6103f, 4.4697f, 11.4697f)
                    curveTo(4.6103f, 11.329f, 4.8011f, 11.25f, 5f, 11.25f)
                    horizontalLineTo(19f)
                    curveTo(19.1989f, 11.25f, 19.3897f, 11.329f, 19.5303f, 11.4697f)
                    curveTo(19.671f, 11.6103f, 19.75f, 11.8011f, 19.75f, 12f)
                    curveTo(19.75f, 12.1989f, 19.671f, 12.3897f, 19.5303f, 12.5303f)
                    curveTo(19.3897f, 12.671f, 19.1989f, 12.75f, 19f, 12.75f)
                    close()
                }
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 6.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(19f, 8.25f)
                    horizontalLineTo(5f)
                    curveTo(4.8011f, 8.25f, 4.6103f, 8.171f, 4.4697f, 8.0303f)
                    curveTo(4.329f, 7.8897f, 4.25f, 7.6989f, 4.25f, 7.5f)
                    curveTo(4.25f, 7.3011f, 4.329f, 7.1103f, 4.4697f, 6.9697f)
                    curveTo(4.6103f, 6.829f, 4.8011f, 6.75f, 5f, 6.75f)
                    horizontalLineTo(19f)
                    curveTo(19.1989f, 6.75f, 19.3897f, 6.829f, 19.5303f, 6.9697f)
                    curveTo(19.671f, 7.1103f, 19.75f, 7.3011f, 19.75f, 7.5f)
                    curveTo(19.75f, 7.6989f, 19.671f, 7.8897f, 19.5303f, 8.0303f)
                    curveTo(19.3897f, 8.171f, 19.1989f, 8.25f, 19f, 8.25f)
                    close()
                }
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 6.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(19f, 17.25f)
                    horizontalLineTo(5f)
                    curveTo(4.8011f, 17.25f, 4.6103f, 17.171f, 4.4697f, 17.0303f)
                    curveTo(4.329f, 16.8897f, 4.25f, 16.6989f, 4.25f, 16.5f)
                    curveTo(4.25f, 16.3011f, 4.329f, 16.1103f, 4.4697f, 15.9697f)
                    curveTo(4.6103f, 15.829f, 4.8011f, 15.75f, 5f, 15.75f)
                    horizontalLineTo(19f)
                    curveTo(19.1989f, 15.75f, 19.3897f, 15.829f, 19.5303f, 15.9697f)
                    curveTo(19.671f, 16.1103f, 19.75f, 16.3011f, 19.75f, 16.5f)
                    curveTo(19.75f, 16.6989f, 19.671f, 16.8897f, 19.5303f, 17.0303f)
                    curveTo(19.3897f, 17.171f, 19.1989f, 17.25f, 19f, 17.25f)
                    close()
                }
            }.build()
            return _Bars!!
        }

    private var _Build: ImageVector? = null

    override val build: ImageVector
        get() {
            if (_Build != null) {
                return _Build!!
            }
            _Build = ImageVector.Builder(
                name = "Build",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(20.37f, 60.76f)
                            horizontalLineToRelative(28.8f)
                            arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
                            verticalLineToRelative(15.6f)
                            arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
                            horizontalLineTo(26f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.67f, -5.67f)
                            verticalLineTo(60.76f)
                            arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(49.17f, 60.76f)
                            horizontalLineTo(78f)
                            arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
                            verticalLineToRelative(9.93f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.67f, 5.67f)
                            horizontalLineTo(49.17f)
                            arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
                            verticalLineTo(60.76f)
                            arcTo(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 49.17f, 60.76f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(34.77f, 45.16f)
                            horizontalLineTo(63.57f)
                            verticalLineTo(60.76f)
                            horizontalLineTo(34.77f)
                            verticalLineTo(45.16f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(54.84f, 29.56f)
                            horizontalLineTo(72.3f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 78f, 35.23f)
                            verticalLineToRelative(9.93f)
                            arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
                            horizontalLineTo(49.17f)
                            arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
                            verticalLineTo(35.23f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 54.84f, 29.56f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(15.99f, 18.87f)
                            horizontalLineTo(33.45f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 39.12f, 24.54f)
                            verticalLineTo(28.8f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 33.45f, 34.47f)
                            horizontalLineTo(15.99f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10.32f, 28.8f)
                            verticalLineTo(24.54f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15.99f, 18.87f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(63.57f, 45.16f)
                            horizontalLineTo(77.97f)
                            verticalLineTo(60.76f)
                            horizontalLineTo(63.57f)
                            verticalLineTo(45.16f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(26f, 45.16f)
                            horizontalLineToRelative(8.73f)
                            arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
                            verticalLineToRelative(15.6f)
                            arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
                            horizontalLineTo(20.37f)
                            arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
                            verticalLineTo(50.83f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 26f, 45.16f)
                            close()
                        }
                    }
                }
            }.build()
            return _Build!!
        }


    private var _Cancel: ImageVector? = null

    override val cancel: ImageVector
        get() {
            if (_Cancel != null) {
                return _Cancel!!
            }
            _Cancel = ImageVector.Builder(
                name = "Cancel",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(19.38f, 19.38f)
                            lineTo(71.33f, 71.33f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(19.38f, 71.33f)
                            lineTo(71.33f, 19.38f)
                        }
                    }
                }
            }.build()
            return _Cancel!!
        }

    private var _ClearStorage: ImageVector? = null

    override val clearStorage: ImageVector
        get() {
            if (_ClearStorage != null) {
                return _ClearStorage!!
            }
            _ClearStorage = ImageVector.Builder(
                name = "ClearStorage",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(48.38f, 78.16f)
                            lineTo(75.21f, 78.16f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(31.25f, 12.4f)
                            horizontalLineToRelative(17f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.67f, 5.67f)
                            verticalLineTo(69.18f)
                            arcTo(11.34f, 11.34f, 0f, isMoreThanHalf = false, isPositiveArc = true, 42.58f, 80.52f)
                            horizontalLineTo(36.92f)
                            arcTo(11.34f, 11.34f, 0f, isMoreThanHalf = false, isPositiveArc = true, 25.58f, 69.18f)
                            verticalLineTo(18.07f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.67f, -5.67f)
                            close()
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(24.27f, 44.02f)
                            lineTo(47.71f, 59.96f)
                        }
                    }
                }
            }.build()
            return _ClearStorage!!
        }

    private var _CombineCells: ImageVector? = null

    override val combineCells: ImageVector
        get() {
            if (_CombineCells != null) {
                return _CombineCells!!
            }
            _CombineCells = ImageVector.Builder(
                name = "CombineCells",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(23.41f, 11.41f)
                            horizontalLineTo(67.3f)
                            arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 79.3f, 23.41f)
                            verticalLineTo(67.3f)
                            arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 67.3f, 79.3f)
                            horizontalLineTo(23.41f)
                            arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.41f, 67.3f)
                            verticalLineTo(23.41f)
                            arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.41f, 11.41f)
                            close()
                        }
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(64.79f, 25.92f)
                            verticalLineTo(64.79f)
                            horizontalLineTo(25.92f)
                            verticalLineTo(25.92f)
                            horizontalLineTo(64.79f)
                            moveToRelative(2.51f, -6f)
                            horizontalLineTo(23.41f)
                            arcToRelative(3.49f, 3.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.49f, 3.49f)
                            verticalLineTo(67.3f)
                            arcToRelative(3.49f, 3.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.49f, 3.49f)
                            horizontalLineTo(67.3f)
                            arcToRelative(3.49f, 3.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.49f, -3.49f)
                            verticalLineTo(23.41f)
                            arcToRelative(3.49f, 3.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.49f, -3.49f)
                            close()
                        }
                    }
                }
            }.build()
            return _CombineCells!!
        }

    private var _ContinuousExe: ImageVector? = null

    override val continuousExe: ImageVector
        get() {
            if (_ContinuousExe != null) {
                return _ContinuousExe!!
            }
            _ContinuousExe = ImageVector.Builder(
                name = "ContinuousExe",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(12.24f, 45.35f)
                            lineTo(78.47f, 45.35f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(63.53f, 45.35f)
                            lineTo(48.87f, 30.69f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(48.87f, 60.02f)
                            lineTo(63.53f, 45.35f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(78.47f, 45.35f)
                            lineTo(63.8f, 30.69f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(63.8f, 60.02f)
                            lineTo(78.47f, 45.35f)
                        }
                    }
                }
            }.build()
            return _ContinuousExe!!
        }

    private var _Darkmode: ImageVector? = null

    override val darkmode: ImageVector
        get() {
            if (_Darkmode != null) {
                return _Darkmode!!
            }
            _Darkmode = ImageVector.Builder(
                name = "Darkmode",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(40.91f, 48.86f)
                            curveToRelative(-7.51f, -10.21f, -8.58f, -23.63f, -5.38f, -32.79f)
                            arcTo(31f, 31f, 0f, isMoreThanHalf = true, isPositiveArc = false, 74.12f, 62.5f)
                            curveTo(63.56f, 64.84f, 49.5f, 60.51f, 40.91f, 48.86f)
                            close()
                        }
                    }
                }
            }.build()
            return _Darkmode!!
        }

    private var _DeleteBlack: ImageVector? = null

    override val deleteBlack: ImageVector
        get() {
            if (_DeleteBlack != null) {
                return _DeleteBlack!!
            }
            _DeleteBlack = ImageVector.Builder(
                name = "DeleteBlack",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(31.76f, 26.07f)
                            horizontalLineTo(58.94f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 64.61f, 31.74f)
                            verticalLineTo(68.49f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 58.94f, 74.16f)
                            horizontalLineTo(31.76f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 26.09f, 68.49f)
                            verticalLineTo(31.74f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.76f, 26.07f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(34.65f, 35.24f)
                            lineTo(34.65f, 64.99f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(45.35f, 35.24f)
                            lineTo(45.35f, 64.99f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(56.06f, 35.24f)
                            lineTo(56.06f, 64.99f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(29.72f, 16.55f)
                            lineTo(60.99f, 16.55f)
                        }
                    }
                }
            }.build()
            return _DeleteBlack!!
        }
    private var _DeleteRed: ImageVector? = null

    override val deleteRed: ImageVector
        get() {
            if (_DeleteRed != null) {
                return _DeleteRed!!
            }
            _DeleteRed = ImageVector.Builder(
                name = "DeleteRed",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color(0xFFEE2222)),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(31.76f, 26.07f)
                            horizontalLineTo(58.94f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 64.61f, 31.74f)
                            verticalLineTo(68.49f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 58.94f, 74.16f)
                            horizontalLineTo(31.76f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 26.09f, 68.49f)
                            verticalLineTo(31.74f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.76f, 26.07f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color(0xFFEE2222)),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(34.65f, 35.24f)
                            lineTo(34.65f, 64.99f)
                        }
                        path(

                            stroke = SolidColor(Color(0xFFEE2222)),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(45.35f, 35.24f)
                            lineTo(45.35f, 64.99f)
                        }
                        path(

                            stroke = SolidColor(Color(0xFFEE2222)),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(56.06f, 35.24f)
                            lineTo(56.06f, 64.99f)
                        }
                        path(

                            stroke = SolidColor(Color(0xFFEE2222)),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(29.72f, 16.55f)
                            lineTo(60.99f, 16.55f)
                        }
                    }
                }
            }.build()
            return _DeleteRed!!
        }
    private var _Disassembler: ImageVector? = null

    override val disassembler: ImageVector
        get() {
            if (_Disassembler != null) {
                return _Disassembler!!
            }
            _Disassembler = ImageVector.Builder(
                name = "Disassembler",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(72.08f, 55.29f)
                            verticalLineTo(68.61f)
                            arcToRelative(6.24f, 6.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6.24f, 6.24f)
                            horizontalLineTo(23.1f)
                            arcToRelative(6.23f, 6.23f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6.23f, -6.24f)
                            verticalLineTo(25.86f)
                            arcToRelative(6.23f, 6.23f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6.23f, -6.23f)
                            horizontalLineTo(36.42f)
                            arcToRelative(6.23f, 6.23f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6.23f, 6.23f)
                            verticalLineToRelative(17f)
                            arcToRelative(6.23f, 6.23f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.24f, 6.23f)
                            horizontalLineToRelative(17f)
                            arcTo(6.24f, 6.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 72.08f, 55.29f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(58.82f, 13.86f)
                            horizontalLineTo(71.6f)
                            arcTo(6.24f, 6.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 77.84f, 20.1f)
                            verticalLineTo(32.88f)
                            arcTo(6.24f, 6.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 71.6f, 39.12f)
                            horizontalLineTo(58.82f)
                            arcTo(6.24f, 6.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 52.58f, 32.88f)
                            verticalLineTo(20.1f)
                            arcTo(6.24f, 6.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 58.82f, 13.86f)
                            close()
                        }
                    }
                }
            }.build()
            return _Disassembler!!
        }
    private var _Edit: ImageVector? = null

    override val edit: ImageVector
        get() {
            if (_Edit != null) {
                return _Edit!!
            }
            _Edit = ImageVector.Builder(
                name = "Edit",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(35.95f, 25.18f)
                            horizontalLineTo(52.58f)
                            verticalLineTo(67.7f)
                            horizontalLineTo(35.95f)
                            verticalLineTo(25.18f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(35.11f, 67.35f)
                            lineTo(18.65f, 75.93f)
                            arcToRelative(2.87f, 2.87f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.87f, -3.88f)
                            lineTo(23.36f, 55.6f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(66.34f, 14.41f)
                            horizontalLineToRelative(0f)
                            arcToRelative(9.86f, 9.86f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.86f, 9.86f)
                            verticalLineTo(34.33f)
                            arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
                            horizontalLineTo(56.49f)
                            arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
                            verticalLineTo(24.26f)
                            arcToRelative(9.86f, 9.86f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.86f, -9.86f)
                            close()
                        }
                    }
                }
            }.build()
            return _Edit!!
        }
    private var _Export: ImageVector? = null

    override val export: ImageVector
        get() {
            if (_Export != null) {
                return _Export!!
            }
            _Export = ImageVector.Builder(
                name = "Export",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(75.67f, 48.5f)
                            verticalLineTo(63.81f)
                            arcToRelative(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, -12f, 12f)
                            horizontalLineTo(27f)
                            arcToRelative(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, -12f, -12f)
                            verticalLineTo(27.19f)
                            arcToRelative(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, -12f)
                            horizontalLineTo(43.17f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(45.35f, 45.5f)
                            lineTo(75.67f, 15.19f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(75.67f, 15.19f)
                            lineTo(54.93f, 15.19f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(75.67f, 35.92f)
                            lineTo(75.67f, 15.19f)
                        }
                    }
                }
            }.build()
            return _Export!!
        }
    private var _FileCompiled: ImageVector? = null

    override val fileCompiled: ImageVector
        get() {
            if (_FileCompiled != null) {
                return _FileCompiled!!
            }
            _FileCompiled = ImageVector.Builder(
                name = "FileCompiled",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(65f, 47.91f)
                            verticalLineTo(20.35f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.67f, -5.67f)
                            horizontalLineTo(42f)
                            arcToRelative(5.66f, 5.66f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4f, 1.66f)
                            lineTo(24.61f, 29.68f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.66f, 4f)
                            verticalLineTo(69.36f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, 28.62f, 75f)
                            horizontalLineToRelative(9.2f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(53.55f, 29.28f)
                            lineTo(40.32f, 29.28f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(53.55f, 43.39f)
                            lineTo(34.34f, 43.39f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(37.78f, 57.51f)
                            lineTo(34.34f, 57.51f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(76.44f, 53.6f)
                            lineTo(58.78f, 78.03f)
                            lineTo(48.83f, 66.27f)
                        }
                    }
                }
            }.build()
            return _FileCompiled!!
        }

    private var _FileNotCompiled: ImageVector? = null

    override val fileNotCompiled: ImageVector
        get() {
            if (_FileNotCompiled != null) {
                return _FileNotCompiled!!
            }
            _FileNotCompiled = ImageVector.Builder(
                name = "FileNotCompiled",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(47.26f, 55.43f)
                            lineTo(71.93f, 80.1f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(47.26f, 80.1f)
                            lineTo(71.93f, 55.43f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(64.78f, 45.84f)
                            verticalLineTo(18.28f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.67f, -5.67f)
                            horizontalLineTo(41.79f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4f, 1.66f)
                            lineTo(24.44f, 27.61f)
                            arcToRelative(5.66f, 5.66f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.66f, 4f)
                            verticalLineTo(67.28f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, 28.45f, 73f)
                            horizontalLineToRelative(9.2f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(53.39f, 27.21f)
                            lineTo(40.15f, 27.21f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(53.39f, 41.32f)
                            lineTo(34.17f, 41.32f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(37.61f, 55.43f)
                            lineTo(34.17f, 55.43f)
                        }
                    }
                }
            }.build()
            return _FileNotCompiled!!
        }

    private var _Forwards: ImageVector? = null

    override val forwards: ImageVector
        get() {
            if (_Forwards != null) {
                return _Forwards!!
            }
            _Forwards = ImageVector.Builder(
                name = "Forwards",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(19f, 74.52f)
                            verticalLineTo(51.17f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.67f, -5.67f)
                            horizontalLineTo(71.69f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(71.74f, 45.5f)
                            lineTo(51.41f, 25.16f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(51.41f, 65.84f)
                            lineTo(71.74f, 45.5f)
                        }
                    }
                }
            }.build()
            return _Forwards!!
        }
    private var _Home: ImageVector? = null

    override val home: ImageVector
        get() {
            if (_Home != null) {
                return _Home!!
            }
            _Home = ImageVector.Builder(
                name = "Home",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(76.68f, 36.75f)
                            verticalLineTo(74.83f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 71f, 80.5f)
                            horizontalLineTo(61f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.67f, -5.67f)
                            verticalLineTo(56.32f)
                            arcToRelative(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = false, -10f, -10f)
                            horizontalLineToRelative(0f)
                            arcToRelative(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = false, -10f, 10f)
                            verticalLineTo(74.83f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 29.7f, 80.5f)
                            horizontalLineToRelative(-10f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14f, 74.83f)
                            verticalLineTo(36.74f)
                            arcToRelative(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1f, -2.14f)
                            lineToRelative(26.63f, -23f)
                            arcToRelative(5.68f, 5.68f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7.42f, 0f)
                            lineToRelative(26.64f, 23f)
                            arcTo(2.85f, 2.85f, 0f, isMoreThanHalf = false, isPositiveArc = true, 76.68f, 36.75f)
                            close()
                        }
                    }
                }
            }.build()
            return _Home!!
        }
    private var _Import: ImageVector? = null

    override val import: ImageVector
        get() {
            if (_Import != null) {
                return _Import!!
            }
            _Import = ImageVector.Builder(
                name = "Import",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(75.67f, 48.5f)
                            verticalLineTo(63.81f)
                            arcToRelative(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, -12f, 12f)
                            horizontalLineTo(27f)
                            arcToRelative(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, -12f, -12f)
                            verticalLineTo(27.19f)
                            arcToRelative(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, -12f)
                            horizontalLineTo(43.17f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(66.09f, 24.76f)
                            lineTo(35.78f, 55.07f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(35.78f, 55.07f)
                            lineTo(56.52f, 55.07f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(35.78f, 34.34f)
                            lineTo(35.78f, 55.07f)
                        }
                    }
                }
            }.build()
            return _Import!!
        }
    private var _Info: ImageVector? = null

    override val info: ImageVector
        get() {
            if (_Info != null) {
                return _Info!!
            }
            _Info = ImageVector.Builder(
                name = "Info",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(45.35f, 73.29f)
                            lineTo(45.35f, 41.67f)
                        }
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 0.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(50.19f, 28.53f)
                            arcTo(4.84f, 4.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 45.35f, 33.37f)
                            arcTo(4.84f, 4.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 40.51f, 28.53f)
                            arcTo(4.84f, 4.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 50.19f, 28.53f)
                            close()
                        }
                    }
                }
            }.build()
            return _Info!!
        }
    private var _Lightmode: ImageVector? = null

    override val lightmode: ImageVector
        get() {
            if (_Lightmode != null) {
                return _Lightmode!!
            }
            _Lightmode = ImageVector.Builder(
                name = "Lightmode",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(68.67f, 45.5f)
                            arcTo(23.32f, 23.32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 45.35f, 68.82f)
                            arcTo(23.32f, 23.32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.03f, 45.5f)
                            arcTo(23.32f, 23.32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 68.67f, 45.5f)
                            close()
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(45.19f, 13.07f)
                            lineTo(45.17f, 9.99f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(22.3f, 22.69f)
                            lineTo(20.12f, 20.52f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(12.92f, 45.67f)
                            lineTo(9.85f, 45.68f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(22.54f, 68.55f)
                            lineTo(20.38f, 70.73f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(45.52f, 77.93f)
                            lineTo(45.54f, 81f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(68.41f, 68.31f)
                            lineTo(70.59f, 70.47f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(77.78f, 45.33f)
                            lineTo(80.86f, 45.31f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(68.17f, 22.45f)
                            lineTo(70.33f, 20.26f)
                        }
                    }
                }
            }.build()
            return _Lightmode!!
        }
    private var _Logo: ImageVector? = null

    override val logo: ImageVector
        get() {
            if (_Logo != null) {
                return _Logo!!
            }
            _Logo = ImageVector.Builder(
                name = "Logo",
                defaultWidth = 100.dp,
                defaultHeight = 100.dp,
                viewportWidth = 100f,
                viewportHeight = 100f
            ).apply {
                group {
                    path(
                        stroke = SolidColor(Color.Black),
                        strokeAlpha = 1.0f,
                        strokeLineWidth = 6.0f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Miter,
                        strokeLineMiter = 1.0f,
                        pathFillType = PathFillType.NonZero
                    ) {
                        moveTo(10f, 90f)
                        lineTo(90f, 10f)
                    }
                }
            }.build()
            return _Logo!!
        }
    private var _Pin: ImageVector? = null

    override val pin: ImageVector
        get() {
            if (_Pin != null) {
                return _Pin!!
            }
            _Pin = ImageVector.Builder(
                name = "Pin",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(56.47f, 56.13f)
                            lineTo(34.24f, 56.13f)
                            lineTo(37.67f, 21.86f)
                            lineTo(53.04f, 21.86f)
                            lineTo(56.47f, 56.13f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(64.57f, 56.13f)
                            lineTo(26.14f, 56.13f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(45.35f, 77.14f)
                            lineTo(45.35f, 56.13f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(58.45f, 21.86f)
                            lineTo(32.26f, 21.86f)
                        }
                    }
                }
            }.build()
            return _Pin!!
        }
    private var _Processor: ImageVector? = null

    override val processor: ImageVector
        get() {
            if (_Processor != null) {
                return _Processor!!
            }
            _Processor = ImageVector.Builder(
                name = "Processor",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(23.48f, 17.81f)
                            horizontalLineTo(67.23f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 72.9f, 23.48f)
                            verticalLineTo(67.23f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 67.23f, 72.9f)
                            horizontalLineTo(23.48f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17.81f, 67.23f)
                            verticalLineTo(23.48f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.48f, 17.81f)
                            close()
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(31.04f, 28.21f)
                            horizontalLineTo(59.67f)
                            arcTo(2.83f, 2.83f, 0f, isMoreThanHalf = false, isPositiveArc = true, 62.5f, 31.04f)
                            verticalLineTo(59.67f)
                            arcTo(2.83f, 2.83f, 0f, isMoreThanHalf = false, isPositiveArc = true, 59.67f, 62.5f)
                            horizontalLineTo(31.04f)
                            arcTo(2.83f, 2.83f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.21f, 59.67f)
                            verticalLineTo(31.04f)
                            arcTo(2.83f, 2.83f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.04f, 28.21f)
                            close()
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(72.9f, 31.48f)
                            lineTo(84.11f, 31.48f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(72.9f, 44.17f)
                            lineTo(84.11f, 44.17f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(72.9f, 56.85f)
                            lineTo(84.11f, 56.85f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(6.59f, 33.86f)
                            lineTo(17.81f, 33.86f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(6.59f, 46.54f)
                            lineTo(17.81f, 46.54f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(6.59f, 59.23f)
                            lineTo(17.81f, 59.23f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(31.48f, 17.81f)
                            lineTo(31.48f, 6.59f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(44.17f, 17.81f)
                            lineTo(44.17f, 6.59f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(56.85f, 17.81f)
                            lineTo(56.85f, 6.59f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(33.86f, 84.11f)
                            lineTo(33.86f, 72.9f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(46.54f, 84.11f)
                            lineTo(46.54f, 72.9f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(59.23f, 84.11f)
                            lineTo(59.23f, 72.9f)
                        }
                    }
                }
            }.build()
            return _Processor!!
        }
    private var _ProcessorBold: ImageVector? = null

    override val processorBold: ImageVector
        get() {
            if (_ProcessorBold != null) {
                return _ProcessorBold!!
            }
            _ProcessorBold = ImageVector.Builder(
                name = "ProcessorBold",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(67.23f, 17.81f)
                            horizontalLineTo(23.48f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.67f, 5.67f)
                            verticalLineTo(67.23f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.67f, 5.67f)
                            horizontalLineTo(67.23f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.67f, -5.67f)
                            verticalLineTo(23.48f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, 67.23f, 17.81f)
                            close()
                            moveTo(62.5f, 59.66f)
                            arcToRelative(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.84f, 2.84f)
                            horizontalLineTo(31.05f)
                            arcToRelative(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.84f, -2.84f)
                            verticalLineTo(31.05f)
                            arcToRelative(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.84f, -2.84f)
                            horizontalLineTo(59.66f)
                            arcToRelative(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.84f, 2.84f)
                            close()
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(31.05f, 28.21f)
                            horizontalLineTo(59.66f)
                            arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 62.5f, 31.05f)
                            verticalLineTo(59.66f)
                            arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 59.66f, 62.5f)
                            horizontalLineTo(31.05f)
                            arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.21f, 59.66f)
                            verticalLineTo(31.05f)
                            arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.05f, 28.21f)
                            close()
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(31.05f, 28.21f)
                            horizontalLineTo(59.66f)
                            arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 62.5f, 31.05f)
                            verticalLineTo(59.66f)
                            arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 59.66f, 62.5f)
                            horizontalLineTo(31.05f)
                            arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.21f, 59.66f)
                            verticalLineTo(31.05f)
                            arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.05f, 28.21f)
                            close()
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(72.9f, 31.48f)
                            lineTo(84.11f, 31.48f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(72.9f, 44.17f)
                            lineTo(84.11f, 44.17f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(72.9f, 56.85f)
                            lineTo(84.11f, 56.85f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(6.59f, 33.86f)
                            lineTo(17.81f, 33.86f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(6.59f, 46.54f)
                            lineTo(17.81f, 46.54f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(6.59f, 59.23f)
                            lineTo(17.81f, 59.23f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(31.48f, 17.81f)
                            lineTo(31.48f, 6.59f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(44.17f, 17.81f)
                            lineTo(44.17f, 6.59f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(56.85f, 17.81f)
                            lineTo(56.85f, 6.59f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(33.86f, 84.11f)
                            lineTo(33.86f, 72.9f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(46.54f, 84.11f)
                            lineTo(46.54f, 72.9f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(59.23f, 84.11f)
                            lineTo(59.23f, 72.9f)
                        }
                    }
                }
            }.build()
            return _ProcessorBold!!
        }
    private var _ProcessorLight: ImageVector? = null

    override val processorLight: ImageVector
        get() {
            if (_ProcessorLight != null) {
                return _ProcessorLight!!
            }
            _ProcessorLight = ImageVector.Builder(
                name = "ProcessorLight",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4.87f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(23.48f, 17.81f)
                            horizontalLineTo(67.23f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 72.9f, 23.48f)
                            verticalLineTo(67.23f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 67.23f, 72.9f)
                            horizontalLineTo(23.48f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17.81f, 67.23f)
                            verticalLineTo(23.48f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.48f, 17.81f)
                            close()
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4.87f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(31.04f, 28.21f)
                            horizontalLineTo(59.67f)
                            arcTo(2.83f, 2.83f, 0f, isMoreThanHalf = false, isPositiveArc = true, 62.5f, 31.04f)
                            verticalLineTo(59.67f)
                            arcTo(2.83f, 2.83f, 0f, isMoreThanHalf = false, isPositiveArc = true, 59.67f, 62.5f)
                            horizontalLineTo(31.04f)
                            arcTo(2.83f, 2.83f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.21f, 59.67f)
                            verticalLineTo(31.04f)
                            arcTo(2.83f, 2.83f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.04f, 28.21f)
                            close()
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(72.9f, 31.48f)
                            lineTo(84.11f, 31.48f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(72.9f, 44.17f)
                            lineTo(84.11f, 44.17f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(72.9f, 56.85f)
                            lineTo(84.11f, 56.85f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(6.59f, 33.86f)
                            lineTo(17.81f, 33.86f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(6.59f, 46.54f)
                            lineTo(17.81f, 46.54f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(6.59f, 59.23f)
                            lineTo(17.81f, 59.23f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(31.48f, 17.81f)
                            lineTo(31.48f, 6.59f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(44.17f, 17.81f)
                            lineTo(44.17f, 6.59f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(56.85f, 17.81f)
                            lineTo(56.85f, 6.59f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(33.86f, 84.11f)
                            lineTo(33.86f, 72.9f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(46.54f, 84.11f)
                            lineTo(46.54f, 72.9f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 4f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(59.23f, 84.11f)
                            lineTo(59.23f, 72.9f)
                        }
                    }
                }
            }.build()
            return _ProcessorLight!!
        }
    private var _Recompile: ImageVector? = null

    override val recompile: ImageVector
        get() {
            if (_Recompile != null) {
                return _Recompile!!
            }
            _Recompile = ImageVector.Builder(
                name = "Recompile",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(54.23f, 70.71f)
                            arcToRelative(28f, 28f, 0f, isMoreThanHalf = true, isPositiveArc = false, -28f, -28f)
                            verticalLineToRelative(6f)
                            horizontalLineToRelative(0f)
                            lineToRelative(-12f, -12f)
                            lineToRelative(12f, 12f)
                            lineToRelative(12f, -12f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(12.44f, 57.46f)
                            lineTo(39.7f, 57.46f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(12.7f, 66.77f)
                            lineTo(39.95f, 66.77f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(12.7f, 76.07f)
                            lineTo(39.95f, 76.07f)
                        }
                    }
                }
            }.build()
            return _Recompile!!
        }
    private var _Refresh: ImageVector? = null

    override val refresh: ImageVector
        get() {
            if (_Refresh != null) {
                return _Refresh!!
            }
            _Refresh = ImageVector.Builder(
                name = "Refresh",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(19.96f, 28.47f)
                            lineTo(19.95f, 28.47f)
                            lineTo(19.95f, 11.44f)
                            lineTo(19.95f, 28.47f)
                            lineTo(36.98f, 28.47f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(75.25f, 45.35f)
                            arcToRelative(29.9f, 29.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, -51f, -21.14f)
                            lineTo(20f, 28.47f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(70.75f, 62.25f)
                            lineTo(70.75f, 62.24f)
                            lineTo(70.75f, 62.24f)
                            lineTo(70.75f, 79.27f)
                            lineTo(70.75f, 62.24f)
                            lineTo(53.73f, 62.24f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(15.45f, 45.35f)
                            arcTo(29.91f, 29.91f, 0f, isMoreThanHalf = false, isPositiveArc = false, 66.5f, 66.5f)
                            lineToRelative(4.25f, -4.25f)
                        }
                    }
                }
            }.build()
            return _Refresh!!
        }
    private var _ReportBug: ImageVector? = null

    override val reportBug: ImageVector
        get() {
            if (_ReportBug != null) {
                return _ReportBug!!
            }
            _ReportBug = ImageVector.Builder(
                name = "ReportBug",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(22.73f, 41.76f)
                            lineTo(8.7f, 41.76f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(22.73f, 56.65f)
                            lineTo(9f, 58.64f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(24.17f, 69.62f)
                            lineTo(14.66f, 79.13f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(67.98f, 41.76f)
                            lineTo(82.01f, 41.76f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(67.98f, 56.65f)
                            lineTo(81.71f, 58.64f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(66.43f, 69.51f)
                            lineTo(76.05f, 79.13f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(57.3f, 18.94f)
                            lineTo(64.62f, 11.61f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(33.84f, 19.36f)
                            lineTo(26.09f, 11.61f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(59.43f, 28f)
                            verticalLineToRelative(5.64f)
                            horizontalLineTo(31.28f)
                            verticalLineTo(28f)
                            arcToRelative(14.08f, 14.08f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.15f, 0f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(45.35f, 35.52f)
                            lineTo(45.35f, 54.56f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(68f, 39.32f)
                            verticalLineTo(61.11f)
                            curveToRelative(0f, 12.27f, -9.54f, 22.71f, -21.8f, 23.15f)
                            arcTo(22.62f, 22.62f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.73f, 61.65f)
                            verticalLineTo(39.32f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.67f, -5.67f)
                            horizontalLineTo(62.31f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 68f, 39.32f)
                            close()
                        }
                    }
                }
            }.build()
            return _ReportBug!!
        }
    private var _ReturnSubroutine: ImageVector? = null

    override val returnSubroutine: ImageVector
        get() {
            if (_ReturnSubroutine != null) {
                return _ReturnSubroutine!!
            }
            _ReturnSubroutine = ImageVector.Builder(
                name = "ReturnSubroutine",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(29.86f, 45.35f)
                            lineTo(81.15f, 45.35f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(81.15f, 45.35f)
                            lineTo(66.48f, 30.69f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(66.48f, 60.02f)
                            lineTo(81.15f, 45.35f)
                        }
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 0.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(22.15f, 45.35f)
                            arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15.86f, 51.64f)
                            arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.57f, 45.35f)
                            arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.15f, 45.35f)
                            close()
                        }
                    }
                }
            }.build()
            return _ReturnSubroutine!!
        }
    private var _Reverse: ImageVector? = null

    override val reverse: ImageVector
        get() {
            if (_Reverse != null) {
                return _Reverse!!
            }
            _Reverse = ImageVector.Builder(
                name = "Reverse",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(57.4f, 29.97f)
                            lineTo(45.35f, 17.93f)
                            lineTo(33.31f, 29.97f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(33.31f, 60.74f)
                            lineTo(45.35f, 72.78f)
                            lineTo(57.4f, 60.74f)
                        }
                    }
                }
            }.build()
            return _Reverse!!
        }
    private var _Settings: ImageVector? = null

    override val settings: ImageVector
        get() {
            if (_Settings != null) {
                return _Settings!!
            }
            _Settings = ImageVector.Builder(
                name = "Settings",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(79.3f, 51.65f)
                            arcToRelative(34.35f, 34.35f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -12.3f)
                            lineTo(72.68f, 37.9f)
                            arcToRelative(28.07f, 28.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.63f, -6.34f)
                            lineToRelative(3.66f, -5.72f)
                            arcToRelative(34.75f, 34.75f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8.7f, -8.7f)
                            lineTo(59.3f, 20.8f)
                            arcTo(28.07f, 28.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 53f, 18.17f)
                            lineToRelative(-1.45f, -6.62f)
                            arcToRelative(34.81f, 34.81f, 0f, isMoreThanHalf = false, isPositiveArc = false, -12.3f, 0f)
                            lineToRelative(-1.45f, 6.62f)
                            arcToRelative(27.65f, 27.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6.34f, 2.64f)
                            lineTo(25.7f, 17.14f)
                            arcToRelative(34.75f, 34.75f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8.7f, 8.7f)
                            lineToRelative(3.67f, 5.72f)
                            arcTo(27.19f, 27.19f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18f, 37.9f)
                            lineToRelative(-6.63f, 1.45f)
                            arcToRelative(34.81f, 34.81f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 12.3f)
                            lineTo(18f, 53.1f)
                            arcToRelative(26.94f, 26.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.63f, 6.33f)
                            lineTo(17f, 65.15f)
                            arcToRelative(34.58f, 34.58f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.7f, 8.7f)
                            lineToRelative(5.71f, -3.66f)
                            arcToRelative(27.73f, 27.73f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.35f, 2.64f)
                            lineToRelative(1.45f, 6.62f)
                            arcToRelative(34.81f, 34.81f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12.3f, 0f)
                            lineTo(53f, 72.83f)
                            arcToRelative(27.65f, 27.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.34f, -2.64f)
                            lineTo(65f, 73.85f)
                            arcToRelative(34.58f, 34.58f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.7f, -8.7f)
                            lineToRelative(-3.66f, -5.71f)
                            arcToRelative(28.07f, 28.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.63f, -6.34f)
                            close()
                            moveTo(58.61f, 45.51f)
                            arcToRelative(13.18f, 13.18f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.88f, 9.36f)
                            horizontalLineToRelative(0f)
                            arcToRelative(13.21f, 13.21f, 0f, isMoreThanHalf = false, isPositiveArc = true, -9.35f, 3.87f)
                            horizontalLineToRelative(0f)
                            arcTo(13.23f, 13.23f, 0f, isMoreThanHalf = false, isPositiveArc = true, 36f, 54.88f)
                            horizontalLineToRelative(0f)
                            arcToRelative(13.25f, 13.25f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -18.74f)
                            horizontalLineToRelative(0f)
                            arcToRelative(13.23f, 13.23f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.36f, -3.87f)
                            horizontalLineToRelative(0f)
                            arcToRelative(13.21f, 13.21f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.35f, 3.87f)
                            horizontalLineToRelative(0f)
                            arcToRelative(13.18f, 13.18f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.88f, 9.36f)
                            horizontalLineToRelative(0f)
                        }
                    }
                }
            }.build()
            return _Settings!!
        }
    private var _SingleExe: ImageVector? = null

    override val singleExe: ImageVector
        get() {
            if (_SingleExe != null) {
                return _SingleExe!!
            }
            _SingleExe = ImageVector.Builder(
                name = "SingleExe",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(9.56f, 45.35f)
                            lineTo(60.85f, 45.35f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(60.85f, 45.35f)
                            lineTo(46.19f, 30.69f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(46.19f, 60.02f)
                            lineTo(60.85f, 45.35f)
                        }
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 0.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(81.14f, 45.35f)
                            arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 74.85f, 51.64f)
                            arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 68.56f, 45.35f)
                            arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 81.14f, 45.35f)
                            close()
                        }
                    }
                }
            }.build()
            return _SingleExe!!
        }
    private var _SplitCells: ImageVector? = null

    override val splitCells: ImageVector
        get() {
            if (_SplitCells != null) {
                return _SplitCells!!
            }
            _SplitCells = ImageVector.Builder(
                name = "SplitCells",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(23.41f, 11.41f)
                            horizontalLineTo(67.3f)
                            arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 79.3f, 23.41f)
                            verticalLineTo(67.3f)
                            arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 67.3f, 79.3f)
                            horizontalLineTo(23.41f)
                            arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.41f, 67.3f)
                            verticalLineTo(23.41f)
                            arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.41f, 11.41f)
                            close()
                        }
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 0.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(36.6f, 25.91f)
                            verticalLineTo(64.79f)
                            horizontalLineTo(25.91f)
                            verticalLineTo(25.91f)
                            horizontalLineTo(36.6f)
                            moveToRelative(2.51f, -6f)
                            horizontalLineTo(23.41f)
                            arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.5f, 3.5f)
                            verticalLineTo(67.3f)
                            arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.5f, 3.49f)
                            horizontalLineToRelative(15.7f)
                            arcTo(3.49f, 3.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, 42.6f, 67.3f)
                            verticalLineTo(23.41f)
                            arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.49f, -3.5f)
                            close()
                        }
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 0.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(64.79f, 25.91f)
                            verticalLineTo(64.79f)
                            horizontalLineTo(54.1f)
                            verticalLineTo(25.91f)
                            horizontalLineTo(64.79f)
                            moveToRelative(2.51f, -6f)
                            horizontalLineTo(51.6f)
                            arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.5f, 3.5f)
                            verticalLineTo(67.3f)
                            arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.5f, 3.49f)
                            horizontalLineTo(67.3f)
                            arcToRelative(3.49f, 3.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.49f, -3.49f)
                            verticalLineTo(23.41f)
                            arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.49f, -3.5f)
                            close()
                        }
                    }
                }
            }.build()
            return _SplitCells!!
        }
    private var _StatusError: ImageVector? = null

    override val statusError: ImageVector
        get() {
            if (_StatusError != null) {
                return _StatusError!!
            }
            _StatusError = ImageVector.Builder(
                name = "StatusError",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 0.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(50.19f, 71.3f)
                            arcTo(4.84f, 4.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 45.35f, 76.14f)
                            arcTo(4.84f, 4.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 40.51f, 71.3f)
                            arcTo(4.84f, 4.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 50.19f, 71.3f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(45.35f, 17.12f)
                            lineTo(45.35f, 57.48f)
                        }
                    }
                }
            }.build()
            return _StatusError!!
        }
    private var _StatusFine: ImageVector? = null

    override val statusFine: ImageVector
        get() {
            if (_StatusFine != null) {
                return _StatusFine!!
            }
            _StatusFine = ImageVector.Builder(
                name = "StatusFine",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(71.23f, 25.79f)
                            lineTo(41.03f, 69.81f)
                            lineTo(24.71f, 55.05f)
                        }
                    }
                }
            }.build()
            return _StatusFine!!
        }
    private var _StatusLoading: ImageVector? = null

    override val statusLoading: ImageVector
        get() {
            if (_StatusLoading != null) {
                return _StatusLoading!!
            }
            _StatusLoading = ImageVector.Builder(
                name = "StatusLoading",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(71.25f, 57.17f)
                            arcTo(28.46f, 28.46f, 0f, isMoreThanHalf = true, isPositiveArc = true, 62f, 22.25f)
                        }
                    }
                }
            }.build()
            return _StatusLoading!!
        }
    private var _StepInto: ImageVector? = null

    override val stepInto: ImageVector
        get() {
            if (_StepInto != null) {
                return _StepInto!!
            }
            _StepInto = ImageVector.Builder(
                name = "StepInto",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(21f, 16.13f)
                            horizontalLineTo(41.09f)
                            curveToRelative(2.35f, 0f, 4.26f, 2.95f, 4.26f, 6.6f)
                            verticalLineTo(53.79f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(45.35f, 53.79f)
                            lineTo(60.02f, 39.13f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(30.69f, 39.13f)
                            lineTo(45.35f, 53.79f)
                        }
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 0.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(51.64f, 67.79f)
                            arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 45.35f, 74.08f)
                            arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 39.06f, 67.79f)
                            arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 51.64f, 67.79f)
                            close()
                        }
                    }
                }
            }.build()
            return _StepInto!!
        }
    private var _StepMultiple: ImageVector? = null

    override val stepMultiple: ImageVector
        get() {
            if (_StepMultiple != null) {
                return _StepMultiple!!
            }
            _StepMultiple = ImageVector.Builder(
                name = "StepMultiple",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(11.13f, 45.35f)
                            lineTo(65.94f, 45.35f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(51f, 45.35f)
                            lineTo(36.34f, 30.69f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(36.34f, 60.02f)
                            lineTo(51f, 45.35f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(65.94f, 45.35f)
                            lineTo(51.28f, 30.69f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(51.28f, 60.02f)
                            lineTo(65.94f, 45.35f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(77.58f, 66.72f)
                            lineTo(77.58f, 23.99f)
                        }
                    }
                }
            }.build()
            return _StepMultiple!!
        }
    private var _StepOut: ImageVector? = null

    override val stepOut: ImageVector
        get() {
            if (_StepOut != null) {
                return _StepOut!!
            }
            _StepOut = ImageVector.Builder(
                name = "StepOut",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(33.47f, 51.07f)
                            verticalLineTo(31f)
                            curveToRelative(0f, -2.35f, 3f, -4.26f, 6.66f, -4.26f)
                            horizontalLineTo(71.48f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(71.48f, 26.75f)
                            lineTo(56.82f, 12.09f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(56.82f, 41.41f)
                            lineTo(71.48f, 26.75f)
                        }
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 0.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(39.76f, 69.15f)
                            arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 33.47f, 75.44f)
                            arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 27.18f, 69.15f)
                            arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 39.76f, 69.15f)
                            close()
                        }
                    }
                }
            }.build()
            return _StepOut!!
        }
    private var _StepOver: ImageVector? = null

    override val stepOver: ImageVector
        get() {
            if (_StepOver != null) {
                return _StepOver!!
            }
            _StepOver = ImageVector.Builder(
                name = "StepOver",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 0.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(50.31f, 57.66f)
                            arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 44.02f, 63.95f)
                            arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 37.73f, 57.66f)
                            arcTo(6.29f, 6.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 50.31f, 57.66f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(19.71f, 57.66f)
                            verticalLineTo(32.42f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.67f, -5.67f)
                            horizontalLineTo(62.67f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.67f, 5.67f)
                            verticalLineTo(57.66f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(68.34f, 57.66f)
                            lineTo(83f, 43f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(53.68f, 43f)
                            lineTo(68.34f, 57.66f)
                        }
                    }
                }
            }.build()
            return _StepOver!!
        }
    private var _Tag: ImageVector? = null

    override val tag: ImageVector
        get() {
            if (_Tag != null) {
                return _Tag!!
            }
            _Tag = ImageVector.Builder(
                name = "Tag",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(74.38f, 41f)
                            lineTo(39f, 76.44f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8f, 0f)
                            lineTo(15.21f, 60.69f)
                            arcToRelative(5.7f, 5.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -8f)
                            lineToRelative(35.4f, -35.4f)
                            horizontalLineTo(66.42f)
                            lineToRelative(8f, 8f)
                            close()
                        }
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 0.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(64.6f, 31.9f)
                            arcTo(4.84f, 4.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 59.76f, 36.74f)
                            arcTo(4.84f, 4.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 54.92f, 31.9f)
                            arcTo(4.84f, 4.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 64.6f, 31.9f)
                            close()
                        }
                    }
                }
            }.build()
            return _Tag!!
        }

    private var _Decrease: ImageVector? = null

    override val decrease: ImageVector
        get() {
            if (_Decrease != null) {
                return _Decrease!!
            }
            _Decrease = ImageVector.Builder(
                name = "Decrease",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(57.99f, 31.54f)
                            lineTo(32.72f, 31.54f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(32.48f, 46.54f)
                            lineTo(45.11f, 59.17f)
                            lineTo(57.75f, 46.54f)
                        }
                    }
                }
            }.build()
            return _Decrease!!
        }
    private var _Increase: ImageVector? = null

    override val increase: ImageVector
        get() {
            if (_Increase != null) {
                return _Increase!!
            }
            _Increase = ImageVector.Builder(
                name = "Increase",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(32.72f, 52.85f)
                            lineTo(57.99f, 52.85f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(45.35f, 65.49f)
                            lineTo(45.35f, 40.22f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(57.99f, 37.85f)
                            lineTo(45.35f, 25.22f)
                            lineTo(32.72f, 37.85f)
                        }
                    }
                }
            }.build()
            return _Increase!!
        }
    private var _Close: ImageVector? = null

    override val close: ImageVector
        get() {
            if (_Close != null) {
                return _Close!!
            }
            _Close = ImageVector.Builder(
                name = "Close",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(29.95f, 29.95f)
                            lineTo(60.76f, 60.76f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(29.95f, 60.76f)
                            lineTo(60.76f, 29.95f)
                        }
                    }
                }
            }.build()
            return _Close!!
        }

    private var _Folder: ImageVector? = null

    override val folder: ImageVector
        get() {
            if (_Folder != null) {
                return _Folder!!
            }
            _Folder = ImageVector.Builder(
                name = "Folder",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(20.25f, 29.34f)
                            horizontalLineToRelative(52.2f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 78.13f, 35f)
                            verticalLineTo(75.53f)
                            arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
                            horizontalLineTo(20.25f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.67f, -5.67f)
                            verticalLineTo(35f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.67f, -5.67f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(14.58f, 15.18f)
                            horizontalLineTo(45.41f)
                            arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
                            verticalLineToRelative(9.27f)
                            arcToRelative(4.89f, 4.89f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4.89f, 4.89f)
                            horizontalLineToRelative(-21f)
                            arcToRelative(4.89f, 4.89f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4.89f, -4.89f)
                            verticalLineTo(15.18f)
                            arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 0f)
                            close()
                        }
                    }
                }
            }.build()
            return _Folder!!
        }
    private var _File: ImageVector? = null

    override val file: ImageVector
        get() {
            if (_File != null) {
                return _File!!
            }
            _File = ImageVector.Builder(
                name = "File",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(66.35f, 20.85f)
                            verticalLineToRelative(49f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.67f, 5.67f)
                            horizontalLineTo(30f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.67f, -5.67f)
                            verticalLineTo(34.19f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.66f, -4f)
                            lineTo(39.35f, 16.84f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, -1.66f)
                            horizontalLineTo(60.68f)
                            arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 66.35f, 20.85f)
                            close()
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(54.96f, 29.78f)
                            lineTo(41.73f, 29.78f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(54.96f, 43.89f)
                            lineTo(35.75f, 43.89f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 10f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(54.96f, 58.01f)
                            lineTo(35.75f, 58.01f)
                        }
                    }
                }
            }.build()
            return _File!!
        }

    private var _Asmfile: ImageVector? = null
    override val asmFile: ImageVector
        get() {
            if (_Asmfile != null) {
                return _Asmfile!!
            }
            _Asmfile = ImageVector.Builder(
                name = "Asmfile",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(29.35f, 23.85f)
                            lineToRelative(-5f, 9.74f)
                            horizontalLineTo(44.43f)
                            lineToRelative(-5f, -9.74f)
                            arcTo(5.66f, 5.66f, 0f, isMoreThanHalf = false, isPositiveArc = false, 29.35f, 23.85f)
                            close()
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(24.35f, 33.59f)
                            lineTo(24.35f, 40.87f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(44.43f, 33.59f)
                            lineTo(44.43f, 40.87f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(47.21f, 71.31f)
                            horizontalLineTo(61.33f)
                            arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5f, -5f)
                            horizontalLineToRelative(0f)
                            arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5f, -5f)
                            horizontalLineToRelative(-10f)
                            arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5f, -5f)
                            horizontalLineToRelative(0f)
                            arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5f, -5f)
                            horizontalLineTo(65.42f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(50.29f, 20.79f)
                            horizontalLineTo(60.68f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.67f, 5.67f)
                            verticalLineTo(40.87f)
                        }
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6.0f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(36.93f, 71.31f)
                            horizontalLineTo(30f)
                            arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.67f, -5.67f)
                            verticalLineTo(51.23f)
                        }
                    }
                }
            }.build()
            return _Asmfile!!
        }

    private var _ChevronDown: ImageVector? = null

    override val chevronDown: ImageVector
        get() {
            if (_ChevronDown != null) {
                return _ChevronDown!!
            }
            _ChevronDown = ImageVector.Builder(
                name = "ChevronDown",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(32.72f, 40.04f)
                            lineTo(45.35f, 52.67f)
                            lineTo(57.99f, 40.04f)
                        }
                    }
                }
            }.build()
            return _ChevronDown!!
        }
    private var _ChevronRight: ImageVector? = null

    override val chevronRight: ImageVector
        get() {
            if (_ChevronRight != null) {
                return _ChevronRight!!
            }
            _ChevronRight = ImageVector.Builder(
                name = "ChevronRight",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(41.04f, 57.99f)
                            lineTo(53.67f, 45.35f)
                            lineTo(41.04f, 32.72f)
                        }
                    }
                }
            }.build()
            return _ChevronRight!!
        }

    private var _Console: ImageVector? = null

    override val console: ImageVector
        get() {
            if (_Console != null) {
                return _Console!!
            }
            _Console = ImageVector.Builder(
                name = "Console",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
                group {
                    group {
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(22.4f, 53.96f)
                            lineTo(35.03f, 41.33f)
                            lineTo(22.4f, 28.69f)
                        }
                        path(

                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 6f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero
                        ) {
                            moveTo(37.09f, 66.02f)
                            lineTo(68.31f, 66.02f)
                        }
                    }
                }
            }.build()
            return _Console!!
        }

}