package miso.bot.app;

import org.javacord.api.event.message.MessageCreateEvent;

import com.vdurmont.emoji.EmojiParser;

import miso.bot.app.core.BatchProcessor;
import miso.bot.app.core.Batcher;

public class MessageCreateEventProcessor extends BatchProcessor<MessageCreateEvent> {

    public MessageCreateEventProcessor(Batcher<MessageCreateEvent> batcher) {
        super(batcher);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void process(Batcher<MessageCreateEvent>.Batch batch) {
        for (MessageCreateEvent event : batch.getItems()) {
            if (!event.isServerMessage()) {
                continue;
            }

            var message = event.getMessage();

            if (message.getEmbeds().isEmpty() && message.getAttachments().isEmpty()) {
                continue;
            }

            try {
                event.addReactionToMessage(EmojiParser.parseToUnicode(":fire:")).join();
                event.addReactionToMessage(EmojiParser.parseToUnicode(":eggplant:")).join();
            } catch (Exception e) {
            }
        }
    }

}
