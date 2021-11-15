/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public class FrameOnStackNode extends RubyContextSourceNode {

    @Child private RubyNode child;
    private final FrameSlot frameOnStackMarkerSlot;

    public FrameOnStackNode(RubyNode child, FrameSlot frameOnStackMarkerSlot) {
        this.child = child;
        this.frameOnStackMarkerSlot = frameOnStackMarkerSlot;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final FrameOnStackMarker marker = new FrameOnStackMarker();

        frame.setObject(frameOnStackMarkerSlot, marker);

        try {
            return child.execute(frame);
        } finally {
            marker.setNoLongerOnStack();
        }
    }

}
