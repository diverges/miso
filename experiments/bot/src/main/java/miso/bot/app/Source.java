package miso.bot.app;

public interface Source<T> {
    void onMessage(T message);
}