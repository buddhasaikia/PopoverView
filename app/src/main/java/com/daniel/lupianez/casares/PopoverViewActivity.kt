/**
 * Popover View
 *
 * Copyright 2012 Daniel Lupia–ez Casares <lupidan></lupidan>@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */

package com.daniel.lupianez.casares

import android.app.Activity
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.main.*

class PopoverViewActivity : Activity(), OnClickListener, PopoverViewDelegate {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        button1.setOnClickListener(this)
        button2.setOnClickListener(this)
        button3.setOnClickListener(this)
        button4.setOnClickListener(this)
        button5.setOnClickListener(this)
        button6.setOnClickListener(this)
        button7.setOnClickListener(this)
        button8.setOnClickListener(this)
        button9.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val rootView = findViewById<RelativeLayout>(R.id.rootLayout)
        val popoverView = PopoverView(this, R.layout.popover_showed_view)
        popoverView.contentSizeForViewInPopover = Point(800, 640)
        popoverView.delegate = this
        popoverView.showPopoverFromRectInViewGroup(rootView, PopoverView.getFrameForView(v), PopoverView.PopoverArrowDirectionAny, false)

    }


    override fun popoverViewWillShow(view: PopoverView) {
        Log.i("POPOVER", "Will show")
    }

    override fun popoverViewDidShow(view: PopoverView) {
        Log.i("POPOVER", "Did show")
    }

    override fun popoverViewWillDismiss(view: PopoverView) {
        Log.i("POPOVER", "Will dismiss")
    }

    override fun popoverViewDidDismiss(view: PopoverView) {
        Log.i("POPOVER", "Did dismiss")
    }
}