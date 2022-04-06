package miso.bot.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

public class Main {
    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // TODO: Load from config
        String token = "MzUwMTAyNTAzMDU4MTc4MDU5.WZ44XQ.uTHWMa_hkMvTJ_SyGEDM6BEkhys";

        DiscordApi api = new DiscordApiBuilder()
                .setToken(token)
                .login()
                .join();
        Batcher<MessageCreateEvent> batcher = new Batcher<>();

        api.addMessageCreateListener(event -> {
            if (!event.getMessageAuthor().isBotUser()) {
                batcher.handleMessage(event);
            }
        });

        // Print the invite url of your bot
        logger.log(Level.INFO, "You can invite the bot by using the following url: {0}", api.createBotInvite());
    }
}
