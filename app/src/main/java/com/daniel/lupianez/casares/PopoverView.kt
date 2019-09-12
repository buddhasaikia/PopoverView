package com.daniel.lupianez.casares

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.widget.ImageView
import android.widget.RelativeLayout

import java.util.HashMap

class PopoverView : RelativeLayout, OnTouchListener {

    var delegate: PopoverViewDelegate? = null
    /**
     * The main popover containing the view we want to show
     */
    private var popoverView: RelativeLayout? = null
    /**
     * The view group storing this popover. We need this so, when we dismiss the popover, we remove it from the view group
     */
    private var superview: ViewGroup? = null
    /**
     * Sets the content size for the view in a popover, if point is (0,0) the popover will full the screen
     * @param contentSizeForViewInPopover
     */
    var contentSizeForViewInPopover = Point(0, 0)
        set(contentSizeForViewInPopover) {
            field = contentSizeForViewInPopover
            realContentSize = Point(contentSizeForViewInPopover)
            if (popoverView != null) {
                realContentSize.x += popoverView!!.paddingLeft + popoverView!!.paddingRight
                realContentSize.y += popoverView!!.paddingTop + popoverView!!.paddingBottom
            }
        }
    /**
     * The real content size we will use (it considers the padding)
     */
    private var realContentSize = Point(0, 0)
    /**
     * A hash containing
     */
    private var possibleRects: MutableMap<Int, Rect>? = null
    /**
     * Whether the view is animating or not
     */
    private var isAnimating = false
    /**
     * The fade animation time in milliseconds
     */
    var fadeAnimationTime = 300
    /**
     * The layout Rect, is the same as the superview rect
     */
    private var popoverLayoutRect: Rect? = null
    /**
     * The popover background drawable
     */
    var popoverBackgroundDrawable: Int = 0
    /**
     * The popover arrow up drawable
     */
    var popoverArrowUpDrawable: Int = 0
    /**
     * The popover arrow down drawable
     */
    var popoverArrowDownDrawable: Int = 0
    /**
     * The popover arrow left drawable
     */
    var popoverArrowLeftDrawable: Int = 0
    /**
     * The popover arrow down drawable
     */
    var popoverArrowRightDrawable: Int = 0

    /**
     * Get the best available rect (bigger area)
     * @return The Integer key to get the Rect from posibleRects (PopoverArrowDirectionUp,PopoverArrowDirectionDown,PopoverArrowDirectionRight or PopoverArrowDirectionLeft)
     */
    private val bestRect: Int?
        get() {
            var best: Int? = null
            for (arrowDir in possibleRects!!.keys) {
                if (best == null) {
                    best = arrowDir
                } else {
                    val bestRect = possibleRects!![best]
                    val checkRect = possibleRects!![arrowDir]
                    if (bestRect!!.width() * bestRect.height() < checkRect!!.width() * checkRect.height())
                        best = arrowDir
                }
            }
            return best
        }

    /**
     * Constructor to create a popover with a popover view
     * @param context The context where we should create the popover view
     * @param layoutId The ID of the layout we want to put inside the popover
     */
    constructor(context: Context, layoutId: Int) : super(context) {
        val view = View.inflate(context, layoutId, null)
        initPopoverView(view)
    }

    /**
     * Constructor to create a popover with a popover view
     * @param context The context where we should create the popover view
     * @param attrs Attribute set to init the view
     * @param layoutId The ID of the layout we want to put inside the popover
     */
    constructor(context: Context, attrs: AttributeSet, layoutId: Int) : super(context, attrs) {
        initPopoverView(View.inflate(context, layoutId, null))
    }

    /**
     * Constructor to create a popover with a popover view
     * @param context The context where we should create the popover view
     * @param attrs Attribute set to init the view
     * @param defStyle The default style for this view
     * @param layoutId The ID of the layout we want to put inside the popover
     */
    constructor(context: Context, attrs: AttributeSet, defStyle: Int, layoutId: Int) : super(context, attrs, defStyle) {
        initPopoverView(View.inflate(context, layoutId, null))
    }

    /**
     * Constructor to create a popover with a popover view
     * @param context The context where we should create the popover view
     * @param popoverView The inner view we want to show in a popover
     */
    constructor(context: Context, popoverView: View) : super(context) {
        initPopoverView(popoverView)
    }

    /**
     * Constructor to create a popover with a popover view
     * @param context The context where we should create the popover view
     * @param attrs Attribute set to init the view
     * @param popoverView The inner view we want to show in a popover
     */
    constructor(context: Context, attrs: AttributeSet, popoverView: View) : super(context, attrs) {
        initPopoverView(popoverView)
    }

    /**
     * Constructor to create a popover with a popover view
     * @param context The context where we should create the popover view
     * @param attrs Attribute set to init the view
     * @param defStyle The default style for this view
     * @param popoverView The inner view we want to show in a popover
     */
    constructor(context: Context, attrs: AttributeSet, defStyle: Int, popoverView: View) : super(context, attrs, defStyle) {
        initPopoverView(popoverView)
    }

    /**
     * Init the popover view
     * @param viewToEnclose The view we wan to insert inside the popover
     */
    private fun initPopoverView(viewToEnclose: View) {
        setBackgroundColor(0x00000000)
        setOnTouchListener(this)

        //Set initial drawables
        popoverBackgroundDrawable = defaultPopoverBackgroundDrawable
        popoverArrowUpDrawable = defaultPopoverArrowUpDrawable
        popoverArrowDownDrawable = defaultPopoverArrowDownDrawable
        popoverArrowLeftDrawable = defaultPopoverArrowLeftDrawable
        popoverArrowRightDrawable = defaultPopoverArrowRightDrawable

        //Init the relative layout
        popoverView = RelativeLayout(context)
        popoverView!!.setBackgroundDrawable(resources.getDrawable(popoverBackgroundDrawable, null))
        popoverView!!.addView(viewToEnclose, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

    }

    /**
     * Add the popover to the view with a defined rect inside the popover
     * @param insertRect The rect we want to insert the view
     */
    private fun addPopoverInRect(insertRect: Rect) {
        //Set layout params
        val insertParams = LayoutParams(insertRect.width(), insertRect.height())
        insertParams.leftMargin = insertRect.left
        insertParams.topMargin = insertRect.top
        //Add the view
        addView(popoverView, insertParams)
    }


    private fun addArrow(originRect: Rect, arrowDirection: Int?) {
        //Add arrow drawable
        val arrowImageView = ImageView(context)
        var arrowDrawable: Drawable? = null
        var xPos = 0
        var arrowWidth = 0
        var yPos = 0
        var arrowHeight = 0
        //Get correct drawable, and get Width, Height, Xpos and yPos depending on the selected arrow direction
        if (arrowDirection == PopoverView.PopoverArrowDirectionUp) {
            arrowDrawable = resources.getDrawable(popoverArrowUpDrawable)
            arrowWidth = arrowDrawable!!.intrinsicWidth
            arrowHeight = arrowDrawable.intrinsicHeight
            xPos = originRect.centerX() - arrowWidth / 2 - popoverLayoutRect!!.left
            yPos = originRect.bottom - popoverLayoutRect!!.top
        } else if (arrowDirection == PopoverView.PopoverArrowDirectionDown) {
            arrowDrawable = resources.getDrawable(popoverArrowDownDrawable)
            arrowWidth = arrowDrawable!!.intrinsicWidth
            arrowHeight = arrowDrawable.intrinsicHeight
            xPos = originRect.centerX() - arrowWidth / 2 - popoverLayoutRect!!.left
            yPos = originRect.top - arrowHeight - popoverLayoutRect!!.top
        } else if (arrowDirection == PopoverView.PopoverArrowDirectionLeft) {
            arrowDrawable = resources.getDrawable(popoverArrowLeftDrawable)
            arrowWidth = arrowDrawable!!.intrinsicWidth
            arrowHeight = arrowDrawable.intrinsicHeight
            xPos = originRect.right - popoverLayoutRect!!.left
            yPos = originRect.centerY() - arrowHeight / 2 - popoverLayoutRect!!.top
        } else if (arrowDirection == PopoverView.PopoverArrowDirectionRight) {
            arrowDrawable = resources.getDrawable(popoverArrowRightDrawable)
            arrowWidth = arrowDrawable!!.intrinsicWidth
            arrowHeight = arrowDrawable.intrinsicHeight
            xPos = originRect.left - arrowWidth - popoverLayoutRect!!.left
            yPos = originRect.centerY() - arrowHeight / 2 - popoverLayoutRect!!.top
        }
        //Set drawable
        arrowImageView.setImageDrawable(arrowDrawable)
        //Init layout params
        val arrowParams = LayoutParams(arrowWidth, arrowHeight)
        arrowParams.leftMargin = xPos
        arrowParams.topMargin = yPos
        //add view :)
        addView(arrowImageView, arrowParams)
    }


    /**
     * Calculates the rect for showing the view with Arrow Up
     * @param originRect The origin rect
     * @return The calculated rect to show the view
     */
    private fun getRectForArrowUp(originRect: Rect): Rect {

        //Get available space
        var xAvailable = popoverLayoutRect!!.width()
        if (xAvailable < 0)
            xAvailable = 0
        var yAvailable = popoverLayoutRect!!.height() - (originRect.bottom - popoverLayoutRect!!.top)
        if (yAvailable < 0)
            yAvailable = 0

        //Get final width and height
        var finalX = xAvailable
        if (realContentSize.x > 0 && realContentSize.x < finalX)
            finalX = realContentSize.x
        var finalY = yAvailable
        if (realContentSize.y > 0 && realContentSize.y < finalY)
            finalY = realContentSize.y

        //Get final origin X and Y
        var originX = originRect.centerX() - popoverLayoutRect!!.left - finalX / 2
        if (originX < 0)
            originX = 0
        else if (originX + finalX > popoverLayoutRect!!.width())
            originX = popoverLayoutRect!!.width() - finalX
        val originY = originRect.bottom - popoverLayoutRect!!.top

        //Create rect
        //And return
        return Rect(originX, originY, originX + finalX, originY + finalY)

    }

    /**
     * Calculates the rect for showing the view with Arrow Down
     * @param originRect The origin rect
     * @return The calculated rect to show the view
     */
    private fun getRectForArrowDown(originRect: Rect): Rect {

        //Get available space
        var xAvailable = popoverLayoutRect!!.width()
        if (xAvailable < 0)
            xAvailable = 0
        var yAvailable = originRect.top - popoverLayoutRect!!.top
        if (yAvailable < 0)
            yAvailable = 0

        //Get final width and height
        var finalX = xAvailable
        if (realContentSize.x > 0 && realContentSize.x < finalX)
            finalX = realContentSize.x
        var finalY = yAvailable
        if (realContentSize.y > 0 && realContentSize.y < finalY)
            finalY = realContentSize.y

        //Get final origin X and Y
        var originX = originRect.centerX() - popoverLayoutRect!!.left - finalX / 2
        if (originX < 0)
            originX = 0
        else if (originX + finalX > popoverLayoutRect!!.width())
            originX = popoverLayoutRect!!.width() - finalX
        val originY = originRect.top - popoverLayoutRect!!.top - finalY

        //Create rect
        //And return
        return Rect(originX, originY, originX + finalX, originY + finalY)

    }


    /**
     * Calculates the rect for showing the view with Arrow Right
     * @param originRect The origin rect
     * @return The calculated rect to show the view
     */
    private fun getRectForArrowRight(originRect: Rect): Rect {
        //Get available space
        var xAvailable = originRect.left - popoverLayoutRect!!.left
        if (xAvailable < 0)
            xAvailable = 0
        var yAvailable = popoverLayoutRect!!.height()
        if (yAvailable < 0)
            yAvailable = 0

        //Get final width and height
        var finalX = xAvailable
        if (realContentSize.x > 0 && realContentSize.x < finalX)
            finalX = realContentSize.x
        var finalY = yAvailable
        if (realContentSize.y > 0 && realContentSize.y < finalY)
            finalY = realContentSize.y

        //Get final origin X and Y
        val originX = originRect.left - popoverLayoutRect!!.left - finalX
        var originY = originRect.centerY() - popoverLayoutRect!!.top - finalY / 2
        if (originY < 0)
            originY = 0
        else if (originY + finalY > popoverLayoutRect!!.height())
            originY = popoverLayoutRect!!.height() - finalY

        //Create rect
        //And return
        return Rect(originX, originY, originX + finalX, originY + finalY)
    }

    /**
     * Calculates the rect for showing the view with Arrow Left
     * @param originRect The origin rect
     * @return The calculated rect to show the view
     */
    private fun getRectForArrowLeft(originRect: Rect): Rect {
        //Get available space
        var xAvailable = popoverLayoutRect!!.width() - (originRect.right - popoverLayoutRect!!.left)
        if (xAvailable < 0)
            xAvailable = 0
        var yAvailable = popoverLayoutRect!!.height()
        if (yAvailable < 0)
            yAvailable = 0

        //Get final width and height
        var finalX = xAvailable
        if (realContentSize.x > 0 && realContentSize.x < finalX)
            finalX = realContentSize.x
        var finalY = yAvailable
        if (realContentSize.y > 0 && realContentSize.y < finalY)
            finalY = realContentSize.y

        //Get final origin X and Y
        val originX = originRect.right - popoverLayoutRect!!.left
        var originY = originRect.centerY() - popoverLayoutRect!!.top - finalY / 2
        if (originY < 0)
            originY = 0
        else if (originY + finalY > popoverLayoutRect!!.height())
            originY = popoverLayoutRect!!.height() - finalY

        //Create rect
        //And return
        return Rect(originX, originY, originX + finalX, originY + finalY)
    }


    /**
     * Add available rects for each selected arrow direction
     * @param originRect The rect where the popover will appear from
     * @param arrowDirections The bit mask for the possible arrow directions
     */
    private fun addAvailableRects(originRect: Rect, arrowDirections: Int) {
        //Get popover rects for the available directions
        possibleRects = HashMap()
        if (arrowDirections and PopoverView.PopoverArrowDirectionUp != 0) {
            possibleRects!![PopoverView.PopoverArrowDirectionUp] = getRectForArrowUp(originRect)
        }
        if (arrowDirections and PopoverView.PopoverArrowDirectionDown != 0) {
            possibleRects!![PopoverView.PopoverArrowDirectionDown] = getRectForArrowDown(originRect)
        }
        if (arrowDirections and PopoverView.PopoverArrowDirectionRight != 0) {
            possibleRects!![PopoverView.PopoverArrowDirectionRight] = getRectForArrowRight(originRect)
        }
        if (arrowDirections and PopoverView.PopoverArrowDirectionLeft != 0) {
            possibleRects!![PopoverView.PopoverArrowDirectionLeft] = getRectForArrowLeft(originRect)
        }

    }

    /**
     * This method shows a popover in a ViewGroup, from an origin rect (relative to the Application Window)
     * @param group The group we want to insert the popup. Normally a Relative Layout so it can stand on top of everything
     * @param originRect The rect we want the popup to appear from (relative to the Application Window!)
     * @param arrowDirections The mask of bits to tell in which directions we want the popover to be shown
     * @param animated Whether is animated, or not
     */
    fun showPopoverFromRectInViewGroup(group: ViewGroup, originRect: Rect, arrowDirections: Int, animated: Boolean) {

        //First, tell delegate we will show
        delegate?.popoverViewWillShow(this)

        //Save superview
        superview = group

        //First, add the view to the view group. The popover will cover the whole area
        val insertParams = android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT)
        group.addView(this, insertParams)

        //Now, save rect for the layout (is the same as the superview)
        popoverLayoutRect = getFrameForView(superview!!)

        //Add available rects
        addAvailableRects(originRect, arrowDirections)
        //Get best rect
        val best = bestRect

        //Add popover
        val bestRect = possibleRects!![best]
        addPopoverInRect(bestRect!!)
        //Add arrow image
        addArrow(originRect, best)


        //If we don't want animation, just tell the delegate
        if (!animated) {
            //Tell delegate we did show
            delegate?.popoverViewDidShow(this)
        } else {
            //Continue only if we are not animating
            if (!isAnimating) {

                //Create alpha animation, with its listener
                val animation = AlphaAnimation(0.0f, 1.0f)
                animation.duration = fadeAnimationTime.toLong()
                animation.setAnimationListener(object : AnimationListener {
                    override fun onAnimationStart(animation: Animation) {
                        //Nothing to do here
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                        //Nothing to do here
                    }

                    override fun onAnimationEnd(animation: Animation) {
                        //End animation
                        isAnimating = false
                        //Tell delegate we did show
                        delegate?.popoverViewDidShow(this@PopoverView)
                    }
                })

                //Start animation
                isAnimating = true
                startAnimation(animation)
            }
        }//If we want animation, animate it!
    }

    /**
     * Dismiss the current shown popover
     * @param animated Whether it should be dismissed animated or not
     */
    private fun dissmissPopover(animated: Boolean) {

        //Tell delegate we will dismiss
        delegate?.popoverViewWillDismiss(this@PopoverView)

        //If we don't want animation
        if (!animated) {
            //Just remove views
            popoverView?.removeAllViews()
            removeAllViews()
            superview?.removeView(this)
            //Tell delegate we did dismiss
            delegate?.popoverViewDidDismiss(this@PopoverView)
        } else {
            //Continue only if there is not an animation in progress
            if (!isAnimating) {
                //Create alpha animation, with its listener
                val animation = AlphaAnimation(1.0f, 0.0f)
                animation.duration = fadeAnimationTime.toLong()
                animation.setAnimationListener(object : AnimationListener {
                    override fun onAnimationStart(animation: Animation) {
                        //Nothing to do here
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                        //Nothing to do here
                    }

                    override fun onAnimationEnd(animation: Animation) {
                        //Remove the view
                        popoverView?.removeAllViews()
                        removeAllViews()
                        this@PopoverView.superview?.removeView(this@PopoverView)
                        //End animation
                        isAnimating = false
                        //Tell delegate we did dismiss
                        delegate?.popoverViewDidDismiss(this@PopoverView)
                    }
                })

                //Start animation
                isAnimating = true
                startAnimation(animation)
            }

        }

    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        //If we touched over the background popover view (this)
        if (!isAnimating && v === this) {
            dissmissPopover(true)
        }
        return true
    }

    companion object {

        /**
         * Popover arrow points up. Integer to use with bit operators to tell the popover where the arrow should appear and from where the popover should appear
         */
        val PopoverArrowDirectionUp = 0x00000001
        /**
         * Popover arrow points down. Integer to use with bit operators to tell the popover where the arrow should appear and from where the popover should appear
         */
        val PopoverArrowDirectionDown = 0x00000002
        /**
         * Popover arrow points left. Integer to use with bit operators to tell the popover where the arrow should appear and from where the popover should appear
         */
        val PopoverArrowDirectionLeft = 0x00000004
        /**
         * Popover arrow points right. Integer to use with bit operators to tell the popover where the arrow should appear and from where the popover should appear
         */
        val PopoverArrowDirectionRight = 0x00000008
        /**
         * Popover arrow points any direction. Integer to use with bit operators to tell the popover where the arrow should appear and from where the popover should appear
         */
        val PopoverArrowDirectionAny = PopoverArrowDirectionUp or PopoverArrowDirectionDown or PopoverArrowDirectionLeft or PopoverArrowDirectionRight
        /**
         * The default popover background drawable for all the popovers
         */
        var defaultPopoverBackgroundDrawable = R.drawable.background_popover
        /**
         * The default popover arrow up drawable for all the popovers
         */
        var defaultPopoverArrowUpDrawable = R.drawable.icon_popover_arrow_up
        /**
         * The default popover arrow down drawable for all the popovers
         */
        var defaultPopoverArrowDownDrawable = R.drawable.icon_popover_arrow_down
        /**
         * The default popover arrow left drawable for all the popovers
         */
        var defaultPopoverArrowLeftDrawable = R.drawable.icon_popover_arrow_left
        /**
         * The default popover arrow down drawable for all the popovers
         */
        var defaultPopoverArrowRightDrawable = R.drawable.icon_popover_arrow_right

        /**
         * Get the Rect frame for a view (relative to the Window of the application)
         * @param v The view to get the rect from
         * @return The rect of the view, relative to the application window
         */
        fun getFrameForView(v: View): Rect {
            val location = IntArray(2)
            v.getLocationOnScreen(location)
            return Rect(location[0], location[1], location[0] + v.width, location[1] + v.height)
        }
    }
}
