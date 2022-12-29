package miso.bot.app;

import java.util.logging.Level;
import java.util.logging.Logger;
import miso.bot.app.core.BatchProcessor;
import miso.bot.app.core.Batcher;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

public class Main {
    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // TODO: Load from config
        String token = "MzUwMTAyNTAzMDU4MTc4MDU5.WZ44XQ.uTHWMa_hkMvTJ_SyGEDM6BEkhys";

        Batcher<MessageCreateEvent> batcher = new Batcher<>();
        BatchProcessor<MessageCreateEvent> processor = new MessageCreateEventProcessor(batcher);


        DiscordApi api = new DiscordApiBuilder()
            .setToken(token).login().join();
        api.addSlashCommandCreateListener(event -> {
            var interaction = event.getSlashCommandInteraction();
        });
        api.addMessageCreateListener(event -> {
            if (!event.getMessageAuthor().isBotUser()) {
                while(!batcher.handleMessage(event)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });

        // Print the invite url of your bot
        logger.log(Level.INFO, "You can invite the bot by using the following url: {0}", api.createBotInvite());

        processor.start();
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                logger.info("Application shutdown started...");
                api.disconnect();
                processor.interrupt();
                try {
                    processor.join(30000);
                } catch (InterruptedException ex) { interrupt(); 
                } finally { logger.info("Application shutdown ended."); }
            }
        });
    }
}
