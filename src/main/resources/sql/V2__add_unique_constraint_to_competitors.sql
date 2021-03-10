Alter table stats.competitors
add constraint unique_competitor_name
UNIQUE (competitor);

ALTER TABLE elitebot.stats.season_leaderboards DROP CONSTRAINT season_leaderboards_pkey;
ALTER TABLE elitebot.stats.season_leaderboards ADD PRIMARY KEY (season_number, competitor_id);

ALTER USER elitebotadmin WITH SUPERUSER;