package gg.watherum.elitebot.service;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import gg.watherum.elitebot.configuration.DiscordEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DiscordMessageCreateListener implements DiscordEventListener<MessageCreateEvent> {

    @Value("${DISCORD_COMMAND_CHANNEL_ID}")
    private String discordCommandChannelID;

    @Autowired
    private Elitebot elitebot;

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {

        Message eventMessage = event.getMessage();

        if (!eventMessage.getChannelId().equals(Snowflake.of( discordCommandChannelID ))) {
            return Mono.empty();
        }

        if (eventMessage.getContent().charAt(0) != '!') {
            return Mono.empty();
        }

        if (eventMessage.getAuthor().get().isBot()) {
            return Mono.empty();
        }

        elitebot.discordCommandHandler(eventMessage);

        return Mono.empty();
    }
}
