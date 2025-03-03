package dev.hez.meowsense.event.bus;

@FunctionalInterface
public interface Listener<Event> { void call(Event event); }