package gg.watherum.elitebot.service;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.rest.entity.RestChannel;
import gg.watherum.elitebot.dao.competitor.SqlCompetitorDao;
import gg.watherum.elitebot.dao.leaderboards.SqlLeaderboardDao;
import gg.watherum.elitebot.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class Elitebot {

    @Value("${OUTPUT_DIR}")
    private String outputDir;

    @Value("${DISCORD_CLIENT_TOKEN}")
    private String discordClientToken;

    @Value("${DISCORD_COMMAND_CHANNEL_ID}")
    private String discordCommandChannelID;

    @Value("${DISCORD_LEADERBOARD_STREAK_CHANNEL_ID}")
    private String discordStreakLeaderboardChannelID;

    @Value("${DISCORD_LEADERBOARD_WINS_CHANNEL_ID}")
    private String discordWinsLeaderboardChannelID;

    @Value("${DISCORD_SEASON_RANKINGS_CHANNEL_ID}")
    private String discordSeasonRankingsChannelID;

    @Value("${TWITCH_CLIENT_ID}")
    private String twitchClientID;

    @Value("${TWITCH_CLIENT_SECRET}")
    private String twitchClientSecret;

    @Value("${CHANNEL_NAME}")
    private String twitchChannelName;

    @Value("${SEASON_NUMBER}")
    private Integer seasonNumber;

    @Value("${SEASON_ENABLED}")
    private boolean seasonEnabled;

    @Value("${WIN_MODIFIER}")
    private Integer winModifier;

    @Value("${STREAK_MODIFIER}")
    private Integer streakModifier;

    private TwitchClient twitchClient;
    private EventManager eventManager;
    private GatewayDiscordClient discordClient;
    private RestChannel discordCommandChannel;
    private RestChannel discordWinsLeaderboardChannel;
    private RestChannel discordStreakLeaderboardChannel;
    private RestChannel discordSeasonRankingsChannel;

    private SqlCompetitorDao sqlCompetitorDao;
    private SqlLeaderboardDao sqlLeaderboardDao;

    Logger LOG = LoggerFactory.getLogger(Elitebot.class);

    private String arena = "";
    private String passcode = "";

    private Queue<String> competitorQueue = new LinkedList<String>();
    private boolean addCompetitorsToQueue = true;

    private Queue<String> levelQueue = new LinkedList<String>();
    private boolean addLevelsToQueue = true;

    private Count count = new Count(" ", 0);

    private CompetitiveSet competitiveSet = new CompetitiveSet();

    private Streak streak = new Streak();

    private HashMap<String, Competitor> competitorMap = new HashMap<>();

    private ArrayList<Streak> streakLog = new ArrayList<>();

    private LocalDate leaderboardDate = LocalDate.now();


    @Autowired
    public Elitebot(GatewayDiscordClient discordClient, TwitchClient twitchClient,
                    SqlCompetitorDao sqlCompetitorDao, SqlLeaderboardDao sqlLeaderboardDao) {
        this.discordClient = discordClient;
        LOG.info("Successfully Connected to Discord");
        this.twitchClient = twitchClient;
        LOG.info("Successfully Connected to Twitch");
        this.sqlCompetitorDao = sqlCompetitorDao;
        this.sqlLeaderboardDao = sqlLeaderboardDao;
    }

    @PostConstruct
    public void setUpChannels() {
        this.discordCommandChannel = discordClient.getChannelById(
                Snowflake.of(discordCommandChannelID)).block().getRestChannel();
        this.discordWinsLeaderboardChannel = discordClient.getChannelById(
                Snowflake.of(discordWinsLeaderboardChannelID)).block().getRestChannel();
        this.discordStreakLeaderboardChannel = discordClient.getChannelById(
                Snowflake.of(discordStreakLeaderboardChannelID)).block().getRestChannel();
        ;
        this.discordSeasonRankingsChannel = discordClient.getChannelById(
                Snowflake.of(discordSeasonRankingsChannelID)).block().getRestChannel();
        this.twitchClient.getChat().joinChannel(twitchChannelName);
        this.eventManager = twitchClient.getChat().getEventManager();
        this.eventManager.onEvent(IRCMessageEvent.class, this::twitchCommandHandler);

        writeStreak();
        writeCompetitiveSet();
        writeCount();
        writeCompetitor(new Competitor());
    }

    @PreDestroy
    public void closeConnections() {
        this.twitchClient.close();
        this.discordClient.onDisconnect();
    }

    private void sendMessageToTwitchChat(String messageToSendToTwitchChat) {
        this.twitchClient.getChat().sendMessage(twitchChannelName, messageToSendToTwitchChat);
    }

    public void twitchCommandHandler(IRCMessageEvent event) {

        if (event == null) {
            return;
        }

        if (event.getCommandType().toLowerCase().equals("join")) {
            return;
        }

        if (event.getCommandType().toLowerCase().equals("userstate")) {
            return;
        }

        if (event.getCommandType().toLowerCase().equals("roomstate")) {
            return;
        }

        try {
//            LOG.info( event.getUser().getName() );


            String message = event.getMessage().get().trim();
            if (message.charAt(0) != '!') {
                return;
            }

            String[] splitMessage = message.split(" ");

            if (message.equals("!help")) {
                String help =
                        "-------------------\n" +
                        "ELITEBOT COMMANDS \n" +
                        "-------------------\n" +
                        "!arena | Get the ArenaID \n" +
                        "!elitejoin | Join the singles set queue, get the arenaID and passcode (!elitejoin Your_Name)\n" +
                        "!competitorqueue | See the status of the player queue\n" +
                        "!points | Get your points for this season (!points Your_Name)\n" +
                        "!addlevel | add a level to the level queue \n" +
                        "!levelqueue | see the status of the level queue\n";
                sendMessageToTwitchChat(help);
            }

            if (splitMessage[0].equals("!elitejoin")) {

                if (addCompetitorsToQueue) {
                    competitorQueue.add(event.getUser().getName());
                    String response = "I've added you to the queue @" + event.getUser().getName() +
                            "! The Arena ID is " + this.arena + " and the passcode is " + this.passcode;
                    sendMessageToTwitchChat(response);
                }
            }

            if (splitMessage[0].equals("!points")) {
                Competitor competitor;
                if (competitorMap.containsKey(splitMessage[1].trim())) {
                    competitor = competitorMap.get(splitMessage[1].trim());
                } else {
                    competitor = getOrCreateCompetitor(splitMessage[1].trim());
                }
                sendMessageToTwitchChat(competitor.getName()
                        + " has " + competitor.getSeasonPoints() + " point(s)");
            }

            if (splitMessage[0].equals("!addlevel")) {

                if (addLevelsToQueue) {
                    levelQueue.add(splitMessage[1]);
                    String response = "I've added your level to the queue @" + event.getUser().getName();
                    sendMessageToTwitchChat(response);
                }
            }

            if (splitMessage[0].equals("!competitorqueue")) {
                String response = "";
                if (competitorQueue.isEmpty()) {
                    sendMessageToTwitchChat("The competitor queue is empty!");
                } else {
                    response = getCompetitorQueuePreview();
                    sendMessageToTwitchChat(response);
                }
            }

            if (splitMessage[0].equals("!levelqueue")) {
                String response = "";
                if (levelQueue.isEmpty()) {
                    sendMessageToTwitchChat("The level queue is empty!");
                } else {
                    response = getLevelQueuePreview();
                    sendMessageToTwitchChat(response);
                }
            }

            if (message.equals("!arena")) {
                if (arena.equals("nj")) {
                    sendMessageToTwitchChat("The arena is currently not joinable");
                } else {
                    sendMessageToTwitchChat("The arena id is " + this.arena + " @" + event.getUserName());
                }
            }
            LOG.info("* Executed " + message + " command");
        } catch (NoSuchElementException e) {
            //Catch Errors related to empty chat on boot
        }


    }

    private void sendMessageToADiscordChannel(RestChannel discordChannel, String message) {
        discordChannel.createMessage(message).block();
    }

    public void discordCommandHandler(Message message) {

        try {
            String[] splitMessage = message.getContent().split(" ");

            if (splitMessage[0].equals("!editarena")) {
                this.arena = splitMessage[1];
                sendMessageToADiscordChannel(this.discordCommandChannel, "The Arena has been updated");
                sendMessageToTwitchChat("The arena code has been updated. Use !arena for the ID");
            }

            if (splitMessage[0].equals("!editpass")) {
                this.passcode = splitMessage[1];
                sendMessageToADiscordChannel(this.discordCommandChannel, "The passcode has been updated");
                sendMessageToTwitchChat("The passcode has been updated. Use !pass for the passcode");
            }

            if (splitMessage[0].equals("!elitemjoin")) {
                this.competitorQueue.add(splitMessage[1]);
                String response = "I've added " + splitMessage[1] + " to the queue!";
                sendMessageToADiscordChannel(this.discordCommandChannel, response);
                sendMessageToTwitchChat(response);
            }

            if (splitMessage[0].equals("!togglecompqueue")) {
                this.addCompetitorsToQueue = !this.addCompetitorsToQueue;
            }

            if (splitMessage[0].equals("!togglelevelqueue")) {
                this.addLevelsToQueue = !this.addLevelsToQueue;
            }

            if (splitMessage[0].equals("!competitorqueue")) {
                sendMessageToADiscordChannel(this.discordCommandChannel, getCompetitorQueuePreview());
            }

            if (splitMessage[0].equals("!levelqueue")) {
                sendMessageToADiscordChannel(this.discordCommandChannel, getLevelQueuePreview());
            }

            if (splitMessage[0].equals("!setcount")) {
                this.count.setInformation(splitMessage[1].trim());
                this.count.setCount(Integer.valueOf(splitMessage[2].trim()));
                sendMessageToADiscordChannel(this.discordCommandChannel, "I have updated the count");
                writeCount();
            }

            if (splitMessage[0].equals("!ic")) {
                this.count.incrementCount();
                updateCountNumber();
            }

            if (splitMessage[0].equals("!dc")) {
                this.count.decrementCount();
                updateCountNumber();
            }

            if (splitMessage[0].equals("!nextlevel")) {
                sendMessageToADiscordChannel(this.discordCommandChannel, "The level is " + this.levelQueue.poll());
            }

            if (splitMessage[0].equals("!nextcomp")) {
                String response = "The next competitor is " + this.competitorQueue.poll();
                sendMessageToTwitchChat(response);
                sendMessageToADiscordChannel(this.discordCommandChannel, response);
            }

            if (splitMessage[0].equals("!logstreak")) {
                this.streakLog.add(this.streak);
                this.streak = new Streak();
                writeStreak();
            }

            if (splitMessage[0].equals("!initstreak")) {
                //update loses of previous competitor
                //tell chat and cmd channel if they should leave

                if (!this.streak.getVictor().equals("No Victor")) {
                    Competitor losingCompetitor = this.competitorMap.get(this.streak.getVictor());
                    boolean doTheyLeave = losingCompetitor.incrementLoses();
                    if (doTheyLeave) {
                        sendMessageToTwitchChat(losingCompetitor.getName() + losingCompetitor.getLeaveMessage());
                        sendMessageToADiscordChannel(this.discordCommandChannel, losingCompetitor.getName() + losingCompetitor.getLeaveMessage());
                    }
                }
                this.streakLog.add(this.streak);
                String name = splitMessage[1].trim();
                this.streak = new Streak(name, Integer.valueOf(splitMessage[2]));
                boolean resetLosses = Boolean.valueOf( splitMessage[3] );
                Competitor newCompetitor = getOrCreateCompetitor(name);//Added to the competitorMap
                if (resetLosses) {
                    newCompetitor.setLosses(0);
                    this.competitorMap.replace(newCompetitor.getName(), newCompetitor);
                }
                writeStreak();
                writeCompetitor(newCompetitor);
                sendMessageToTwitchChat(name + " is now on a streak!");
            }

            if (splitMessage[0].equals("!sw")) {
                this.streak.incrementWins();
                Competitor competitor = this.competitorMap.get(this.streak.getVictor());
                competitor.setEstimatedPoints(estimateCompetitorsNewPoints(competitor, this.streak));
                updateStreakWins();
                updateCompetitorsEstimatedPoints(competitor);
            }

            if (splitMessage[0].equals("!sl")) {
                this.streak.decrementWins();
                Competitor competitor = this.competitorMap.get(this.streak.getVictor());
                competitor.setEstimatedPoints(estimateCompetitorsNewPoints(competitor, this.streak));
                updateStreakWins();
                updateCompetitorsEstimatedPoints(competitor);
            }

            if (splitMessage[0].equals("!updatesub")) {
                Competitor competitor = this.competitorMap.get(splitMessage[1].trim());
                competitor.setSubscriber(!competitor.isSubscriber());
                this.sqlCompetitorDao.updateSubscriptionStatus(competitor.getId(), competitor.isSubscriber());
            }

            if (splitMessage[0].equals("!initbestof")) {
                this.competitiveSet.setUpBestOf(Integer.valueOf(splitMessage[1]));
                this.competitiveSet.setCompetitorOneName(splitMessage[2]);
                this.competitiveSet.setCompetitorTwoName(splitMessage[3]);

                String response = "This is a Best of " + this.competitiveSet.getBestOf()
                        + ". Competitors need to get " +
                        this.competitiveSet.getWinCondition() + " wins! "
                        + this.competitiveSet.getCompetitorOneName() + " & " +
                        this.competitiveSet.getCompetitorTwoName() + " good luck!";

                sendMessageToTwitchChat(response);
                writeCompetitiveSet();
            }

            if (splitMessage[0].equals("!initfirst2")) {
                this.competitiveSet.setUpFirstTo(Integer.valueOf(splitMessage[1]));
                this.competitiveSet.setCompetitorOneName(splitMessage[2]);
                this.competitiveSet.setCompetitorTwoName(splitMessage[3]);

                String response = "This is a First to " + this.competitiveSet.getFirstTo()
                        + ". Competitors need to get " +
                        this.competitiveSet.getWinCondition() + " wins! "
                        + this.competitiveSet.getCompetitorOneName() + " & " +
                        this.competitiveSet.getCompetitorTwoName() + " good luck!";

                sendMessageToTwitchChat(response);
                writeCompetitiveSet();
            }

            if (splitMessage[0].equals("!bestofpopwath")) {
                this.competitiveSet.setUpBestOf(Integer.valueOf(splitMessage[1]));
                this.competitiveSet.setCompetitorOneName("Watherum");
                this.competitiveSet.setCompetitorTwoName(competitorQueue.poll());

                String response = "This is a Best of " + this.competitiveSet.getBestOf()
                        + ". Competitors need to get " +
                        this.competitiveSet.getWinCondition() + " wins! "
                        + this.competitiveSet.getCompetitorOneName() + " & " +
                        this.competitiveSet.getCompetitorTwoName() + " good luck!";

                sendMessageToTwitchChat(response);
                writeCompetitiveSet();
            }

            if (splitMessage[0].equals("!c1w")) {
                boolean setCompleted = this.competitiveSet.incrementCompOneWins();
                String response = this.competitiveSet.getCompetitorOneName() +
                        " now has " + this.competitiveSet.getCompetitorOneWins() + " win(s)!";
                if (setCompleted) {
                    updateWinner();
                    response = "The set is over! The winner is " + this.competitiveSet.getCompetitorOneName() + " !";
                }
                updateCompOneWins();
                updateGameNumber();
                sendMessageToTwitchChat(response);
            }

            if (splitMessage[0].equals("!c1l")) {
                boolean setCompleted = this.competitiveSet.decrementCompOneWins();
                String response = this.competitiveSet.getCompetitorOneName() +
                        " now has " + this.competitiveSet.getCompetitorOneWins() + " win(s)!";
                if (setCompleted) {
                    updateWinner();
                    response = "The set is over! The winner is " + this.competitiveSet.getCompetitorOneName() + " !";
                }
                updateCompOneWins();
                updateGameNumber();
                sendMessageToTwitchChat(response);
            }

            if (splitMessage[0].equals("!c2w")) {
                boolean setCompleted = this.competitiveSet.incrementCompTwoWins();
                String response = this.competitiveSet.getCompetitorTwoName() +
                        " now has " + this.competitiveSet.getCompetitorTwoWins() + " win(s)!";
                if (setCompleted) {
                    updateWinner();
                    response = "The set is over! The winner is " + this.competitiveSet.getCompetitorTwoName() + " !";
                }
                updateCompTwoWins();
                sendMessageToTwitchChat(response);
            }

            if (splitMessage[0].equals("!c2l")) {
                boolean setCompleted = this.competitiveSet.decrementCompTwoWins();
                String response = this.competitiveSet.getCompetitorTwoName() +
                        " now has " + this.competitiveSet.getCompetitorTwoWins() + " win(s)!";
                if (setCompleted) {
                    updateWinner();
                    response = "The set is over! The winner is " + this.competitiveSet.getCompetitorTwoName() + " !";
                }
                updateCompTwoWins();
                sendMessageToTwitchChat(response);
            }

            if (splitMessage[0].equals("!clearset")) {
                this.competitiveSet = new CompetitiveSet();
                sendMessageToADiscordChannel(this.discordCommandChannel, "The set has been cleared");
                writeCompetitiveSet();
            }

            if (splitMessage[0].equals("!setcomplosses")) {
                if (this.competitorMap.containsKey(splitMessage[1].trim())) {
                    Competitor competitor = this.competitorMap.get(splitMessage[1]);
                    competitor.setLosses(Integer.valueOf(splitMessage[2]));
                    if (splitMessage[1].trim().equals(this.streak.getVictor())) {
                        updateLosses(competitor);
                    }
                }
            }

            if (splitMessage[0].equals("!warning")) {
                String minutes = splitMessage[1].trim();
                LocalDateTime endTime = LocalDateTime.now().withNano(0);
                endTime = endTime.plusMinutes(Long.parseLong(minutes));
                String response = "The stream will be ending in roughly " + minutes
                        + " minutes from now. That is approximately " +
                        endTime.toString();
                sendMessageToTwitchChat(response);
            }

            if (splitMessage[0].equals("!joinPWVB")) {
                sendMessageToTwitchChat("!join");
            }

            if (splitMessage[0].equals("!next")) {
                sendMessageToTwitchChat("!next");
            }

            if (splitMessage[0].equals("!calcstats")) {
                streakLog.add(streak);
                this.streak = new Streak();
                Map<String, Integer> streakLeaderboard = calculateStreakLeaderboard();
                Map<String, Integer> winsLeaderboard = calculateWinsLeaderboard();
                writeLeaderboardToDiscord(
                        generateLeaderBoardChatResponse(streakLeaderboard, true),
                        this.discordStreakLeaderboardChannel);
                writeLeaderboardToDiscord(
                        generateLeaderBoardChatResponse(winsLeaderboard, false),
                        this.discordWinsLeaderboardChannel);
                saveLeaderboardsToTheDatabase(streakLeaderboard, winsLeaderboard);

                if (this.seasonEnabled) {
                    calculateAndUpdateSeasonRecords(streakLeaderboard, winsLeaderboard);
                    writeLeaderboardToDiscord(
                            generateSeasonLeaderBoardChatResponse(sqlLeaderboardDao.getSeasonRecordData(this.seasonNumber)),
                            this.discordSeasonRankingsChannel
                    );
                }

            }

            if (splitMessage[0].equals("!elitehelp")) {
                String generalCommands =
                                "-----------------\n" +
                                "GENERAL COMMANDS \n" +
                                "-----------------\n" +
                                "!elitehelp | Return a list of commands. No arguments to this command\n" +
                                "!editarena | Sets the arena for people to join e.g.(!editarena ABCDE). the arugment nj will let people know they cant join\n" +
                                "!warning | Sends a message detailing a time in minutes in the future when the stream ends " +
                                "e.g.(!warning 30)\n" +
                                "!editpass | sets the passcode / password for the arena or lobby\n\n";

                String streakCommands =
                                "----------------\n" +
                                "STREAK COMMANDS \n" +
                                "----------------\n" +
                                "!initstreak | Writes a file used on stream. Sets the name and wins of the player boolean resets losses e.g.(!initstreak Watherum 1 false)\n" +
                                "!sw | Increment the wins of the victor. No arguments to this command\n" +
                                "!sl | Decrement the wins of the victor. No arguments to this command\n" +
                                "!updatesub | update the subcription status of a competitor e.g (!updatesub Watherum)" +
                                "!calcstats | Generates the leaderboard from the streak log and write it to discord" +
                                "!logstreak | logs the current streak to the streak log" +
                                "!clearstreak | clears the streak and the files on stream. No arguments to this command\n\n";

                String setCommands =
                                "-------------\n" +
                                "SET COMMANDS \n" +
                                "-------------\n" +
                                "!initbestof | Initialize the set files for a best of e.g.(!initbestof 5 c1name c2name)\n" +
                                "!initfirst2 | Initialize the set files for a first to e.g.(!initfirst2 10 c1name c2name)\n" +
                                "!c1w | Increment the wins for the 1st competitor. No arguments to this command\n" +
                                "!c2w | Increment the wins for the 2nd competitor. No arguments to this command\n" +
                                "!clearset | Clears the set files. No arguments to this command\n\n" +
                                "!c1l | Decrements the wins of the 1st competitor. No arguments to this command\n" +
                                "!c2l | Decrements the wins of the 2nd competitor. No arguments to this command\n\n";

                String queueCommands =
                                "------------------------------\n" +
                                "QUEUE COMMANDS \n" +
                                "------------------------------\n" +
                                "!togglecompqueue | Opens or closes the competitor queue \n" +
                                "!togglelevelqueue | Open or closes the level queue \n" +
                                "!bestofpopwath | Pops a person from the queue and creates a new set e.g.(!bestofpopwath 5)\n" +
                                "!elitemjoin | manually add a user to the singles set queue\n" +
                                "!levelqueue | prints the current levels in the queue\n" +
                                "!nextlevel | pops the next level in the level queue\n" +
                                "!nextcomp | pops the competitor level in the level queue\n" +
                                "!competitorqueue | prints the current order of people in the queue\n\n";

                String countCommands =
                                "-------------------\n" +
                                "COUNT COMMANDS \n" +
                                "-------------------\n" +
                                "!ic | Increments the number of the count \n" +
                                "!dc | Decrements the number of the count \n" +
                                "!setcount | Sets the number of the count \n";

                String twitchCommands =
                                "-------------------\n" +
                                "TWITCH COMMANDS \n" +
                                "-------------------\n" +
                                "!arena | Get the ArenaID \n" +
                                "!elitejoin | Join the singles set queue, get the arenaID and passcode (!elitejoin Your_Name)\n" +
                                "!competitorqueue | See the status of the player queue\n" +
                                "!points | Get your points for this season (!points Your_Name)\n" +
                                "!addlevel | add a level to the level queue \n" +
                                "!levelqueue | see the status of the level queue\n";

                sendMessageToADiscordChannel(this.discordCommandChannel, generalCommands);
                sendMessageToADiscordChannel(this.discordCommandChannel, streakCommands);
                sendMessageToADiscordChannel(this.discordCommandChannel, setCommands);
                sendMessageToADiscordChannel(this.discordCommandChannel, queueCommands);
                sendMessageToADiscordChannel(this.discordCommandChannel, countCommands);
                sendMessageToADiscordChannel(this.discordCommandChannel, twitchCommands);
            }

            LOG.info("* Executed " + message.getContent() + " command");
        } catch (Exception e) {
            sendMessageToADiscordChannel(
                    this.discordCommandChannel,
                    "@" + message.getAuthor().get().getUsername()
                            + " there was a problem executing " + message.getContent());
        }
    }

    private Competitor getOrCreateCompetitor(String name) {

        if (this.competitorMap.containsKey(name)) {
            Competitor competitor = this.competitorMap.get(name);
            setCompetitorsPoints(competitor);
            return competitor;
        }

        Competitor competitor = this.sqlCompetitorDao.getCompetitor(name);
        if (competitor == null) {
            competitor = this.sqlCompetitorDao.insertCompetitor(new Competitor(name, false));
        }

        setCompetitorsPoints(competitor);

        this.competitorMap.put(name, competitor);
        return competitor;
    }

    private void setCompetitorsPoints(Competitor competitor) {
        Integer seasonPoints = this.sqlLeaderboardDao.getCompetitorSeasonRecord(this.seasonNumber, competitor.getId());
        if (seasonPoints != null) {
            competitor.setSeasonPoints(seasonPoints);
        }
        competitor.setEstimatedPoints(estimateCompetitorsNewPoints(competitor, this.streak));
    }

    private void calculateAndUpdateSeasonRecords(Map<String, Integer> streakLeaderboard,
                                                 Map<String, Integer> winsLeaderboard) {
        for (Map.Entry<String, Integer> entry : streakLeaderboard.entrySet()) {
            Competitor competitor = this.competitorMap.get(entry.getKey());
            Integer streak = entry.getValue();
            Integer wins = winsLeaderboard.get(entry.getKey());
            Integer newPoints = (wins * this.winModifier) + (streak * this.streakModifier);
            Integer currentPoints = competitor.getSeasonPoints();
            if (currentPoints == 0) {
                sqlLeaderboardDao.insertSeasonRecord(newPoints, this.seasonNumber, competitor.getId());
            } else {
                sqlLeaderboardDao.updateSeasonRecord(
                        newPoints + currentPoints, this.seasonNumber, competitor.getId()
                );
            }

        }
    }

    private ArrayList<String> generateSeasonLeaderBoardChatResponse(List<SeasonRecord> seasonStandings) {
        ArrayList<String> response = new ArrayList<>();
        Collections.reverse(seasonStandings);

        response.add("-----------------------------------------------");
        response.add("|                    Season " + this.seasonNumber + " Standings                 |");
        response.add("-----------------------------------------------");

        for (SeasonRecord seasonRecord : seasonStandings) {

            response.add(seasonRecord.getCompetitorName() + " - Points: " + seasonRecord.getPoints());
        }
        response.add("-----------------------------------------------");

        return response;
    }

    private void saveLeaderboardsToTheDatabase(Map<String, Integer> streakLeaderboard,
                                               Map<String, Integer> winsLeaderboard) {
        try {
            Integer leaderboardDateID = sqlLeaderboardDao.insertLeaderboardDate(this.leaderboardDate);

            for (Map.Entry<String, Integer> entry : streakLeaderboard.entrySet()) {
                Competitor competitor = competitorMap.get(entry.getKey());
                sqlLeaderboardDao.insertStreakRecord(competitor.getId(), entry.getValue(), leaderboardDateID);
            }

            for (Map.Entry<String, Integer> entry : winsLeaderboard.entrySet()) {
                Competitor competitor = competitorMap.get(entry.getKey());
                sqlLeaderboardDao.insertWinRecord(competitor.getId(), entry.getValue(), leaderboardDateID);
            }
        } catch (Exception e) {
            LOG.error("Error trying to insert leaderboard data" + e);
        }
    }

    private void writeLeaderboardToDiscord(ArrayList<String> leaderboardText, RestChannel discordChannel) {
        String finalMessage = "";
        for (String line : leaderboardText) {
            finalMessage = finalMessage + line + "\n";
        }
        sendMessageToADiscordChannel(discordChannel, finalMessage);
    }

    //if streak is false then it means wins
    private ArrayList<String> generateLeaderBoardChatResponse(Map<String, Integer> leaderboard, boolean streak) {
        ArrayList<String> response = new ArrayList<>();

        response.add("-----------------------------------------------");
        response.add("|         Leaderbaord for " + this.leaderboardDate + "         |");
        response.add("-----------------------------------------------");

        for (Map.Entry<String, Integer> entry : leaderboard.entrySet()) {
            if (streak) {
                response.add(entry.getKey() + " had a max streak of " + entry.getValue());
            } else {
                response.add(entry.getKey() + " won " + entry.getValue() + " time(s)");
            }
        }
        response.add("-----------------------------------------------");

        return response;
    }

    //Comp to highest Streak number
    private Map<String, Integer> calculateStreakLeaderboard() {
        Map<String, Integer> streakLeaderboard = new LinkedHashMap<>();
        for (Streak streak : this.streakLog) {
            if (streakLeaderboard.containsKey(streak.getVictor())) {
                if (streakLeaderboard.get(streak.getVictor()) < streak.getConsecutiveWins()) {
                    streakLeaderboard.replace(streak.getVictor(), streak.getConsecutiveWins());
                }
            } else {
                streakLeaderboard.put(streak.getVictor(), streak.getConsecutiveWins());
            }
        }
        streakLeaderboard.remove("No Victor");
        return sortByValue(streakLeaderboard);

    }

    //Comp to highest Win number
    private Map<String, Integer> calculateWinsLeaderboard() {
        Map<String, Integer> winsLeaderboard = new LinkedHashMap<>();
        for (Streak streak : this.streakLog) {
            if (winsLeaderboard.containsKey(streak.getVictor())) {
                Integer currentWins = winsLeaderboard.get(streak.getVictor());
                winsLeaderboard.replace(streak.getVictor(), streak.getConsecutiveWins() + currentWins);
            } else {
                winsLeaderboard.put(streak.getVictor(), streak.getConsecutiveWins());
            }
        }
        winsLeaderboard.remove("No Victor");
        return sortByValue(winsLeaderboard);
    }

    private static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    private Integer estimateCompetitorsNewPoints(Competitor competitor, Streak streak) {
        Map<String, Integer> winsLeaderboard = calculateWinsLeaderboard();
        Map<String, Integer> streakLeaderboard = calculateStreakLeaderboard();

        Integer wins = 1;
        if (winsLeaderboard.containsKey(competitor.getName())) {
            wins = winsLeaderboard.get(competitor.getName());
        }
        Integer highestStreak = 0;
        if (streakLeaderboard.containsKey(competitor.getName())) {
            highestStreak = streakLeaderboard.get(competitor.getName());
        }
        if (streak.getVictor().equals(competitor.getName()) && streak.getConsecutiveWins() > highestStreak) {
            highestStreak = streak.getConsecutiveWins();
        }

        return (wins * this.winModifier) + (highestStreak * this.streakModifier);
    }

    private void updateCompetitorMap(Competitor competitor) {
        this.competitorMap.replace(competitor.getName(), competitor);
    }

    private String getCompetitorQueuePreview() {
        String response = "";
        if (this.competitorQueue.isEmpty()) {
            return response;
        }
        for (String competitor : this.competitorQueue) {
            response = response + competitor + ", ";
        }
        return response;
    }

    private String getLevelQueuePreview() {
        String response = "";
        if (this.competitorQueue.isEmpty()) {
            return response;
        }
        for (String level : this.levelQueue) {
            response = response + level + ", ";
        }
        return response;
    }

    private void writeStreak() {
        writeToFile(this.streak.getVictor(), outputDir + "streak/victor.txt");
        writeToFile("Steak = " + this.streak.getConsecutiveWins().toString(), outputDir + "streak/wins.txt");
    }

    private void updateStreakWins() {
        writeToFile("Steak = " + this.streak.getConsecutiveWins().toString(), outputDir + "streak/wins.txt");
    }

    private void writeCount() {
        writeToFile(this.count.getInformation() + " = ", outputDir + "count/info.txt");
        writeToFile(this.count.getCount().toString(), outputDir + "count/number.txt");
    }

    private void updateCountNumber() {
        writeToFile(this.count.getCount().toString(), outputDir + "count/number.txt");
    }

    private void writeCompetitor(Competitor competitor) {
        Integer lossLimit = 3;
        if (competitor.isSubscriber()) {
            lossLimit = 5;
        }
        writeToFile(competitor.getName(), outputDir + "competitor/name.txt");
        writeToFile("L = " + competitor.getLosses().toString() + "/" + lossLimit.toString(), outputDir + "competitor/losses.txt");
        writeToFile("EP = " + competitor.getEstimatedPoints().toString(), outputDir + "competitor/estimated_points.txt");
        writeToFile("SP = " + competitor.getSeasonPoints().toString(), outputDir + "competitor/seasonal_points.txt");
    }

    private void updateCompetitorsEstimatedPoints(Competitor competitor) {
        writeToFile("EP = " + competitor.getEstimatedPoints().toString(), outputDir + "competitor/estimated_points.txt");
    }

    private void updateLosses(Competitor competitor) {
        Integer lossLimit = 3;
        if (competitor.isSubscriber()) {
            lossLimit = 5;
        }
        writeToFile("L = " + competitor.getLosses().toString() + "/" + lossLimit.toString(), outputDir + "competitor/losses.txt");
    }

    private void writeCompetitiveSet() {
        //Things that do not update
        writeToFile(this.competitiveSet.getCompetitorOneName(), outputDir + "set/competitor_one_name.txt");
        writeToFile(this.competitiveSet.getCompetitorTwoName(), outputDir + "set/competitor_two_name.txt");
        writeToFile(this.competitiveSet.getBestOf().toString(), outputDir + "set/best_of_number.txt");
        writeToFile(this.competitiveSet.getFirstTo().toString(), outputDir + "set/first_to_number.txt");
        writeToFile(this.competitiveSet.getWinCondition().toString(), outputDir + "set/win_condition.txt");
        //Things that do update
        writeToFile(this.competitiveSet.getGameNumber().toString(), outputDir + "set/game_number.txt");
        writeToFile(this.competitiveSet.getCompetitorOneWins().toString(), outputDir + "set/competitor_one_wins.txt");
        writeToFile(this.competitiveSet.getCompetitorTwoWins().toString(), outputDir + "set/competitor_two_wins.txt");
        writeToFile(this.competitiveSet.getWinner().toString(), outputDir + "set/winner.txt");
    }

    private void updateCompOneWins() {
        writeToFile(this.competitiveSet.getCompetitorOneWins().toString(), outputDir + "set/competitor_one_wins.txt");
    }

    private void updateCompTwoWins() {
        writeToFile(this.competitiveSet.getCompetitorTwoWins().toString(), outputDir + "set/competitor_two_wins.txt");
    }

    private void updateGameNumber() {
        writeToFile(this.competitiveSet.getGameNumber().toString(), outputDir + "set/game_number.txt");
    }

    private void updateWinner() {
        writeToFile(this.competitiveSet.getWinner(), outputDir + "set/winner.txt");
    }

    private void writeToFile(String text, String file) {
        try {
            BufferedWriter outStream = new BufferedWriter(new FileWriter(file));
            outStream.write(text);
            outStream.close();
        } catch (IOException e) {
            LOG.error("Failed to write to file " + file + " " + e);
        }
    }

    private void appendToFile(String text, String file) {
        try {
            BufferedWriter outStream = new BufferedWriter(new FileWriter(file, true));
            outStream.newLine();
            outStream.write(text);
            outStream.close();
        } catch (IOException e) {
            LOG.error("Failed to append to file " + file + " " + e);
        }
    }

}