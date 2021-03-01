# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

# This spec can also run on CRuby, for comparison
describe "Always-inlined core methods" do
  describe "are part of the backtrace if the error happens inside that core method" do
    it "for #public_send" do
      -> {
        public_send()
      }.should raise_error(ArgumentError) { |e| e.backtrace_locations[0].label.should == 'public_send' }
      -> {
        public_send(Object.new)
      }.should raise_error(TypeError) { |e| e.backtrace_locations[0].label.should == 'public_send' }
    end

    guard -> { RUBY_ENGINE != "ruby" } do
      it "for #send" do
        -> {
          send()
        }.should raise_error(ArgumentError) { |e| e.backtrace_locations[0].label.should == '__send__' }
        -> {
          send(Object.new)
        }.should raise_error(TypeError) { |e| e.backtrace_locations[0].label.should == '__send__' }
      end

      it "for #__send__" do
        -> {
          __send__()
        }.should raise_error(ArgumentError) { |e| e.backtrace_locations[0].label.should == '__send__' }
        -> {
          __send__(Object.new)
        }.should raise_error(TypeError) { |e| e.backtrace_locations[0].label.should == '__send__' }
      end

      it "for a generated attr_reader" do
        obj = Class.new { attr_reader :foo }.new
        -> {
          obj.foo(:too, :many, :args)
        }.should raise_error(ArgumentError) { |e| e.backtrace_locations[0].label.should == 'foo' }
      end

      it "for a generated attr_writer" do
        obj = Class.new do
          attr_writer :foo
          alias_method :writer, :foo= # so it can be called without send and a different number of arguments
        end.new
        -> {
          obj.send(:foo=, :too, :many, :args)
        }.should raise_error(ArgumentError) { |e| e.backtrace_locations[0].label.should == 'foo=' }
        -> {
          obj.writer(:too, :many, :args)
        }.should raise_error(ArgumentError) { |e| e.backtrace_locations[0].label.should == 'foo=' }

        obj.freeze
        -> {
          obj.foo = 42
        }.should raise_error(FrozenError) { |e| e.backtrace_locations[0].label.should == 'foo=' }
      end
    end

    it "for main.using" do
      -> do
        eval('using "foo"', TOPLEVEL_BINDING)
      end.should raise_error(TypeError) { |e| e.backtrace_locations[0].label.should == 'using' }
    end
  end

  describe "are not part of the backtrace if the error happens in a different method" do
    it "for #public_send" do
      -> {
        public_send(:yield_self) { raise "foo" }
      }.should raise_error(RuntimeError, "foo") { |e|
        e.backtrace_locations[0].label.should.start_with?('block (5 levels)')
        e.backtrace_locations[1].label.should == 'yield_self'
        if RUBY_ENGINE == 'ruby'
          e.backtrace_locations[2].label.should == 'public_send'
        else
          e.backtrace_locations[2].label.should.start_with?('block (4 levels)')
        end
      }
    end

    it "for #send" do
      -> {
        send(:yield_self) { raise "foo" }
      }.should raise_error(RuntimeError, "foo") { |e|
        e.backtrace_locations[0].label.should.start_with?('block (5 levels)')
        e.backtrace_locations[1].label.should == 'yield_self'
        e.backtrace_locations[2].label.should.start_with?('block (4 levels)')
      }
    end

    it "for #__send__" do
      -> {
        __send__(:yield_self) { raise "foo" }
      }.should raise_error(RuntimeError, "foo") { |e|
        e.backtrace_locations[0].label.should.start_with?('block (5 levels)')
        e.backtrace_locations[1].label.should == 'yield_self'
        e.backtrace_locations[2].label.should.start_with?('block (4 levels)')
      }
    end

    it "for Proc#call" do
      line = 0
      -> {
        line = __LINE__
        proc = -> { raise "foo" }
        proc.call
      }.should raise_error(RuntimeError, "foo") { |e|
        e.backtrace_locations[0].label.should.start_with?('block (5 levels)')
        e.backtrace_locations[0].lineno.should == line + 1
        e.backtrace_locations[1].label.should.start_with?('block (4 levels)')
        e.backtrace_locations[1].lineno.should == line + 2
      }
    end
  end

  it "go uncached if seeing too many different always-inlined methods at a call site" do
    names = (1..10).map { |i| :"attr#{i}" }
    obj = Class.new { attr_reader(*names) }.new
    names.each { |name| obj.send(name).should == nil }
  end

  it "work with each(&method(:always_inlined_method))" do
    obj = Class.new do
      [:foo].each(&method(:attr_accessor))
    end.new
    obj.foo = 42
    obj.foo.should == 42
  end
end
