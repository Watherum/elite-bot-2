package gg.watherum.elitebot.configuration;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BotConfiguration {

    @Value("${DISCORD_CLIENT_TOKEN}")
    private String discordClientToken;

    @Value("${TWITCH_CLIENT_ID}")
    private String twitchClientID;

    @Value("${TWITCH_CLIENT_SECRET}")
    private String twitchClientSecret;

    @Value("${OAUTH_TOKEN}")
    private String twitchOAUTHToken;

    @Bean
    public <T extends Event> GatewayDiscordClient gatewayDiscordClient(List< DiscordEventListener<T> > eventListeners) {
        GatewayDiscordClient client = DiscordClientBuilder.create(discordClientToken)
                .build()
                .login()
                .block();

        for(DiscordEventListener<T> listener : eventListeners) {
            client.on(listener.getEventType())
                    .flatMap(listener::execute)
                    .onErrorResume(listener::handleError)
                    .subscribe();
        }

        return client;
    }

    @Bean
    public TwitchClient twitchClient() {
        OAuth2Credential credential = new OAuth2Credential("twitch", twitchOAUTHToken);
        TwitchClient twitchClient = TwitchClientBuilder.builder()
                .withEnableHelix(true)
                .withEnableChat(true)
                .withChatAccount(credential)
                .withClientId(twitchClientID)
                .withClientSecret(twitchClientSecret)
                .build();

        return twitchClient;
    }
}
