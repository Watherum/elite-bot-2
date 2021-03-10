CREATE database elitebot;

CREATE ROLE elitebotadmin login;
ALTER ROLE elitebotadmin WITH PASSWORD 'changme';

CREATE SCHEMA IF NOT EXISTS stats AUTHORIZATION elitebotadmin;

CREATE TABLE IF NOT EXISTS stats.leaderboard_dates
(id SERIAL PRIMARY KEY, datetime varchar (50));

CREATE TABLE IF NOT EXISTS stats.competitors
(id SERIAL PRIMARY KEY, competitor varchar (50));


CREATE TABLE IF NOT EXISTS stats.arena_streak_leaderboards
(
    id SERIAL PRIMARY KEY, --not super useful
    competitor_id integer REFERENCES stats.competitors(id),
    streak integer,
    datetime_id integer REFERENCES stats.leaderboard_dates(id)
);


CREATE TABLE IF NOT EXISTS stats.arena_wins_leaderboards
(
    id SERIAL PRIMARY KEY, --not super useful
    competitor_id integer REFERENCES stats.competitors(id),
    wins integer,
    datetime_id integer REFERENCES stats.leaderboard_dates(id)
);

CREATE TABLE IF NOT EXISTS stats.season_leaderboards
(season_number integer PRIMARY KEY,
 competitor_id integer REFERENCES stats.competitors(id),
 points integer
);
--wins + highest streak x 2 (in game name will be used)

INSERT INTO elitebot.stats.competitors(competitor) values ('Watherum');