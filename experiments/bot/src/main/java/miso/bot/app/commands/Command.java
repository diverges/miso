package miso.bot.app.commands;

import org.javacord.api.event.interaction.SlashCommandCreateEvent;

public abstract class Command {

    public abstract String getId();

    public abstract void build();

    public abstract void handle(SlashCommandCreateEvent event);
}
