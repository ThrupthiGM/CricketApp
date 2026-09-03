CREATE DATABASE IF NOT EXISTS cricket_db;
USE cricket_db;

CREATE TABLE IF NOT EXISTS players (
    player_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    role VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS batting_statistics (
    player_id INT PRIMARY KEY,
    matches INT NOT NULL DEFAULT 0,
    runs INT NOT NULL DEFAULT 0,
    highest_score INT NOT NULL DEFAULT 0,
    batting_average DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_batting_player FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bowling_statistics (
    player_id INT PRIMARY KEY,
    wickets INT NOT NULL DEFAULT 0,
    bowling_average DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_bowling_player FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE
);

INSERT IGNORE INTO players (player_id,name,country,age,role)
VALUES
(101,'Virat Kohli','India',37,'Batsman'),(102,'Rohit Sharma','India',39,'Batsman'),(103,'Jasprit Bumrah','India',32,'Bowler'),(104,'Joe Root','England',35,'Batsman'),(105,'Steve Smith','Australia',37,'Batsman'),(106,'Kane Williamson','New Zealand',36,'Batsman'),(107,'Ravindra Jadeja','India',37,'All Rounder'),(108,'Ben Stokes','England',35,'All Rounder'),(109,'Babar Azam','Pakistan',31,'Batsman'),(110,'Pat Cummins','Australia',33,'Bowler');

INSERT IGNORE INTO batting_statistics (player_id,matches,runs,highest_score,batting_average) VALUES
(101,292,13848,254,58.67),(102,499,19400,264,48.96),(103,196,1000,43,15.00),(104,350,13000,262,50.20),(105,330,10500,239,49.50),(106,320,9500,251,48.75),(107,300,6500,175,35.50),(108,260,7000,258,38.50),(109,280,12000,196,52.40),(110,250,2500,95,20.50);

INSERT IGNORE INTO bowling_statistics (player_id,wickets,bowling_average) VALUES
(101,5,45.00),(102,47,36.00),(103,450,22.50),(104,20,50.00),(105,28,60.00),(106,37,42.00),(107,550,29.00),(108,300,32.00),(109,12,55.00),(110,500,25.40);
