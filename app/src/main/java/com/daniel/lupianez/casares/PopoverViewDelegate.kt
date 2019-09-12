package com.daniel.lupianez.casares
/**
 * Interface to get information from the popover view. Use setDelegate to have access to this methods
 */
interface PopoverViewDelegate {
    /**
     * Called when the popover is going to show
     *
     * @param view The whole popover view
     */
    fun popoverViewWillShow(view: PopoverView)

    /**
     * Called when the popover did show
     *
     * @param view The whole popover view
     */
    fun popoverViewDidShow(view: PopoverView)

    /**
     * Called when the popover is going to be dismissed
     *
     * @param view The whole popover view
     */
    fun popoverViewWillDismiss(view: PopoverView)

    /**
     * Called when the popover was dismissed
     *
     * @param view The whole popover view
     */
    fun popoverViewDidDismiss(view: PopoverView)
}