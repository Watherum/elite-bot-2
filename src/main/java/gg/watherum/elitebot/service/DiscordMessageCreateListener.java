package gg.watherum.elitebot.service;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.MessageData;
import gg.watherum.elitebot.configuration.DiscordEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DiscordMessageCreateListener implements DiscordEventListener<MessageCreateEvent> {

    Logger LOG = LoggerFactory.getLogger(DiscordEventListener.class);

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

        try {
            Message eventMessage = event.getMessage();
//            String content = eventMessage.getContent();

            if (!eventMessage.getChannelId().equals(Snowflake.of(discordCommandChannelID))) {
                return Mono.empty();
            }

            if (eventMessage.getContent().isEmpty() || eventMessage.getContent().charAt(0) != '!') {
                return Mono.empty();
            }

            if (eventMessage.getAuthor().get().isBot()) {
                return Mono.empty();
            }


            elitebot.discordCommandHandler(eventMessage);
        } catch (Exception e) {
            LOG.error("An error was captured and hopefully the bot still works :)", e);
        }


        return Mono.empty();
    }
}
