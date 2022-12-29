package miso.bot.app.core;

public interface Source<T> {
    void onMessage(T message);
}