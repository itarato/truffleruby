/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.NeverDefault;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

/** Convert objects to a String by calling #to_str, but leave existing Strings or Symbols as they are. */
@GenerateUncached
@NodeChild(value = "childNode", type = RubyBaseNodeWithExecute.class)
public abstract class ToStringOrSymbolNode extends RubyBaseNodeWithExecute {

    @NeverDefault
    public static ToStringOrSymbolNode create() {
        return ToStringOrSymbolNodeGen.create(null);
    }

    public static ToStringOrSymbolNode create(RubyBaseNodeWithExecute child) {
        return ToStringOrSymbolNodeGen.create(child);
    }

    public abstract Object execute(Object value);

    public abstract RubyBaseNodeWithExecute getChildNode();

    @Specialization
    protected RubySymbol coerceRubySymbol(RubySymbol symbol) {
        return symbol;
    }

    @Specialization
    protected RubyString coerceRubyString(RubyString string) {
        return string;
    }

    @Specialization
    protected ImmutableRubyString coerceRubyString(ImmutableRubyString string) {
        return string;
    }

    @Specialization(guards = { "!isRubySymbol(object)", "isNotRubyString(object)" })
    protected Object coerceObject(Object object,
            @Cached DispatchNode toStrNode) {
        return toStrNode.call(
                coreLibrary().truffleTypeModule,
                "rb_convert_type",
                object,
                coreLibrary().stringClass,
                coreSymbols().TO_STR);
    }

    @Override
    public RubyBaseNodeWithExecute cloneUninitialized() {
        return create(getChildNode().cloneUninitialized());
    }
}
