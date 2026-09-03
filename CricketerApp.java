import java.sql.*;
import java.util.Scanner;

public class CricketerApp {
    private static final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection connection = DBConnection.getConnection()) {
            createTables(connection);
            runMenu(connection);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            System.out.println("Check MySQL, database cricketerdb, DBConnection credentials, and the values entered.");
        }
    }

    private static void createTables(Connection c) throws SQLException {
        String[] sql = {
            "CREATE TABLE IF NOT EXISTS teams (team_id INT AUTO_INCREMENT PRIMARY KEY, team_name VARCHAR(80) NOT NULL UNIQUE)",
            "CREATE TABLE IF NOT EXISTS players (player_id INT AUTO_INCREMENT PRIMARY KEY, player_name VARCHAR(100) NOT NULL, age INT NOT NULL, player_role VARCHAR(30) NOT NULL, team_id INT NOT NULL, FOREIGN KEY (team_id) REFERENCES teams(team_id))",
            "CREATE TABLE IF NOT EXISTS batting_details (player_id INT PRIMARY KEY, runs INT NOT NULL DEFAULT 0, innings INT NOT NULL DEFAULT 0, balls_faced INT NOT NULL DEFAULT 0, FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS bowling_details (player_id INT PRIMARY KEY, balls INT NOT NULL DEFAULT 0, wickets INT NOT NULL DEFAULT 0, runs_conceded INT NOT NULL DEFAULT 0, FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS matches (match_id INT AUTO_INCREMENT PRIMARY KEY, match_date DATE NOT NULL, venue VARCHAR(120) NOT NULL, opponent VARCHAR(80) NOT NULL)",
            "CREATE TABLE IF NOT EXISTS performances (performance_id INT AUTO_INCREMENT PRIMARY KEY, match_id INT NOT NULL, player_id INT NOT NULL, runs INT NOT NULL DEFAULT 0, wickets INT NOT NULL DEFAULT 0, FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE, FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE)"
        };
        try (Statement s = c.createStatement()) { for (String query : sql) s.executeUpdate(query); }
    }

    private static void runMenu(Connection c) throws SQLException {
        while (true) {
            printMenu();
            switch (readInt("Enter your choice: ")) {
                case 1: addTeam(c); break; case 2: addPlayer(c); break; case 3: addBatting(c); break;
                case 4: addBowling(c); break; case 5: addMatch(c); break; case 6: addPerformance(c); break;
                case 7: viewPlayers(c); break; case 8: viewBatting(c); break; case 9: viewBowling(c); break;
                case 10: battingAverage(c); break; case 11: bowlingStrikeRate(c); break; case 12: searchPlayer(c); break;
                case 13: updatePlayer(c); break; case 14: deletePlayer(c); break; case 15: playersWithTeam(c); break;
                case 16: leader(c, "TOP RUN SCORER", "runs", "batting_details"); break;
                case 17: leader(c, "TOP WICKET TAKER", "wickets", "bowling_details"); break;
                case 18: System.out.println("Thank you for using Cricketer Management App."); return;
                default: System.out.println("Please choose a number from 1 to 18.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n========================================");
        System.out.println("          CRICKETER MANAGEMENT APP");
        System.out.println("========================================");
        System.out.println("1. Add Team\n2. Add Player\n3. Add Batting Details");
        System.out.println("4. Add Bowling Details\n5. Add Match\n6. Add Player Performance\n");
        System.out.println("7. View All Players\n8. View Batting Statistics\n9. View Bowling Statistics");
        System.out.println("10. Calculate Batting Average\n11. Calculate Bowling Strike Rate\n");
        System.out.println("12. Search Player\n13. Update Player\n14. Delete Player\n");
        System.out.println("15. Display Players with Team\n16. Top Run Scorer\n17. Top Wicket Taker\n");
        System.out.println("18. Exit\n");
    }

    private static void addTeam(Connection c) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("INSERT INTO teams(team_name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, readText("Team name: "));
            s.executeUpdate();
            try (ResultSet keys = s.getGeneratedKeys()) {
                if (keys.next()) {
                    System.out.println("Team added successfully. Team ID: " + keys.getInt(1));
                }
            }
        }
    }
    private static void addPlayer(Connection c) throws SQLException {
        String playerName = readText("Player name: ");
        int age = readInt("Age: ");
        String role = readText("Role: ");
        int teamId = readInt("Team ID: ");
        if (!teamExists(c, teamId)) {
            System.out.println("No team exists with ID " + teamId + ". Add a team first or use its generated Team ID.");
            return;
        }
        String sql = "INSERT INTO players(player_name,age,player_role,team_id) VALUES (?,?,?,?)";
        try (PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, playerName);
            s.setInt(2, age);
            s.setString(3, role);
            s.setInt(4, teamId);
            s.executeUpdate();
            try (ResultSet keys = s.getGeneratedKeys()) {
                if (keys.next()) {
                    System.out.println("Player added successfully. Player ID: " + keys.getInt(1));
                }
            }
        }
    }
    private static void addBatting(Connection c) throws SQLException {
        int playerId = readInt("Player ID: ");
        if (!playerExists(c, playerId)) {
            System.out.println("No player exists with ID " + playerId + ". Use View All Players to check the ID.");
            return;
        }
        update(c, "INSERT INTO batting_details VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE runs=VALUES(runs),innings=VALUES(innings),balls_faced=VALUES(balls_faced)", playerId, readInt("Runs: "), readInt("Innings: "), readInt("Balls faced: ")); System.out.println("Batting details saved.");
    }
    private static void addBowling(Connection c) throws SQLException { update(c, "INSERT INTO bowling_details VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE balls=VALUES(balls),wickets=VALUES(wickets),runs_conceded=VALUES(runs_conceded)", readInt("Player ID: "), readInt("Balls bowled: "), readInt("Wickets: "), readInt("Runs conceded: ")); System.out.println("Bowling details saved."); }
    private static void addMatch(Connection c) throws SQLException { update(c, "INSERT INTO matches(match_date,venue,opponent) VALUES (?,?,?)", readText("Match date (YYYY-MM-DD): "), readText("Venue: "), readText("Opponent: ")); System.out.println("Match added successfully."); }
    private static void addPerformance(Connection c) throws SQLException { update(c, "INSERT INTO performances(match_id,player_id,runs,wickets) VALUES (?,?,?,?)", readInt("Match ID: "), readInt("Player ID: "), readInt("Runs: "), readInt("Wickets: ")); System.out.println("Performance added successfully."); }

    private interface Row { void print(ResultSet r) throws SQLException; }
    private static void rows(Connection c, String sql, Row row) throws SQLException { try (Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) { while(r.next()) row.print(r); } }
    private static void viewPlayers(Connection c) throws SQLException { System.out.println("\n------------- PLAYER DETAILS -------------\n"); System.out.printf("%-5s %-22s %-6s %-14s %s%n", "ID", "Player Name", "Age", "Role", "Team"); rows(c,"SELECT p.player_id,p.player_name,p.age,p.player_role,t.team_name FROM players p JOIN teams t ON p.team_id=t.team_id ORDER BY p.player_id",r->System.out.printf("%-5d %-22s %-6d %-14s %s%n",r.getInt(1),r.getString(2),r.getInt(3),r.getString(4),r.getString(5))); }
    private static void viewBatting(Connection c) throws SQLException { System.out.println("\n------------- BATTING STATISTICS -------------"); System.out.printf("%-22s %-10s %-10s %s%n","Player Name","Runs","Innings","Balls Faced"); rows(c,"SELECT p.player_name,b.runs,b.innings,b.balls_faced FROM players p JOIN batting_details b ON p.player_id=b.player_id ORDER BY b.runs DESC",r->System.out.printf("%-22s %-10d %-10d %d%n",r.getString(1),r.getInt(2),r.getInt(3),r.getInt(4))); }
    private static void viewBowling(Connection c) throws SQLException { System.out.println("\n------------- BOWLING STATISTICS -------------"); System.out.printf("%-22s %-10s %-10s %s%n","Player Name","Balls","Wickets","Runs Conceded"); rows(c,"SELECT p.player_name,b.balls,b.wickets,b.runs_conceded FROM players p JOIN bowling_details b ON p.player_id=b.player_id ORDER BY b.wickets DESC",r->System.out.printf("%-22s %-10d %-10d %d%n",r.getString(1),r.getInt(2),r.getInt(3),r.getInt(4))); }
    private static void battingAverage(Connection c) throws SQLException { System.out.println("\n------------- BATTING AVERAGE -------------\n"); System.out.printf("%-22s %-10s %-10s %s%n","Player Name","Runs","Innings","Average"); rows(c,"SELECT p.player_name,b.runs,b.innings,b.runs/NULLIF(b.innings,0) FROM players p JOIN batting_details b ON p.player_id=b.player_id ORDER BY b.runs/NULLIF(b.innings,0) DESC",r->System.out.printf("%-22s %-10d %-10d %.2f%n",r.getString(1),r.getInt(2),r.getInt(3),r.getDouble(4))); System.out.println("\nPlayers scoring above overall batting average:"); rows(c,"SELECT p.player_name FROM players p JOIN batting_details b ON p.player_id=b.player_id WHERE b.runs>(SELECT AVG(runs) FROM batting_details)",r->System.out.println(r.getString(1))); }
    private static void bowlingStrikeRate(Connection c) throws SQLException { System.out.println("\n------------- BOWLING STRIKE RATE -------------\n"); System.out.printf("%-22s %-10s %-10s %s%n","Player Name","Balls","Wickets","Strike Rate"); rows(c,"SELECT p.player_name,b.balls,b.wickets,b.balls/NULLIF(b.wickets,0) FROM players p JOIN bowling_details b ON p.player_id=b.player_id ORDER BY b.balls/NULLIF(b.wickets,0)",r->System.out.printf("%-22s %-10d %-10d %.2f%n",r.getString(1),r.getInt(2),r.getInt(3),r.getDouble(4))); }
    private static void searchPlayer(Connection c) throws SQLException { String sql="SELECT p.player_id,p.player_name,p.age,p.player_role,t.team_name FROM players p JOIN teams t ON p.team_id=t.team_id WHERE p.player_name LIKE ?"; try(PreparedStatement s=c.prepareStatement(sql)){s.setString(1,"%"+readText("Search name: ")+"%");try(ResultSet r=s.executeQuery()){while(r.next())System.out.printf("%d | %s | %d | %s | %s%n",r.getInt(1),r.getString(2),r.getInt(3),r.getString(4),r.getString(5));}} }
    private static void updatePlayer(Connection c) throws SQLException { update(c,"UPDATE players SET player_name=?,age=?,player_role=?,team_id=? WHERE player_id=?",readText("New name: "),readInt("New age: "),readText("New role: "),readInt("New team ID: "),readInt("Player ID: ")); System.out.println("Player updated if the ID existed."); }
    private static void deletePlayer(Connection c) throws SQLException { execute(c,"DELETE FROM players WHERE player_id=?",readInt("Player ID to delete: ")); System.out.println("Player deleted if the ID existed."); }
    private static void playersWithTeam(Connection c) throws SQLException { System.out.println("\n------------- PLAYERS WITH TEAM -------------"); System.out.printf("%-22s %-18s %-10s %s%n","Player Name","Team","Runs","Wickets"); rows(c,"SELECT p.player_name,t.team_name,COALESCE(b.runs,0),COALESCE(w.wickets,0) FROM players p JOIN teams t ON p.team_id=t.team_id LEFT JOIN batting_details b ON p.player_id=b.player_id LEFT JOIN bowling_details w ON p.player_id=w.player_id ORDER BY t.team_name,p.player_name",r->System.out.printf("%-22s %-18s %-10d %d%n",r.getString(1),r.getString(2),r.getInt(3),r.getInt(4))); }
    private static void leader(Connection c,String title,String field,String table) throws SQLException { System.out.println("\n------------- "+title+" -------------"); rows(c,"SELECT p.player_name,d."+field+" FROM players p JOIN "+table+" d ON p.player_id=d.player_id ORDER BY d."+field+" DESC LIMIT 1",r->System.out.println(r.getString(1)+" - "+r.getInt(2)+" "+field)); }

    private static boolean playerExists(Connection c, int playerId) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT 1 FROM players WHERE player_id=?")) {
            s.setInt(1, playerId);
            try (ResultSet r = s.executeQuery()) { return r.next(); }
        }
    }

    private static boolean teamExists(Connection c, int teamId) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT 1 FROM teams WHERE team_id=?")) {
            s.setInt(1, teamId);
            try (ResultSet r = s.executeQuery()) { return r.next(); }
        }
    }

    private static void execute(Connection c,String sql,Object value) throws SQLException { try(PreparedStatement s=c.prepareStatement(sql)){s.setObject(1,value);s.executeUpdate();} }
    private static void update(Connection c,String sql,Object... values) throws SQLException { try(PreparedStatement s=c.prepareStatement(sql)){for(int i=0;i<values.length;i++)s.setObject(i+1,values[i]);s.executeUpdate();} }
    private static String readText(String prompt){System.out.print(prompt);return input.nextLine().trim();}
    private static int readInt(String prompt){while(true){try{return Integer.parseInt(readText(prompt));}catch(NumberFormatException e){System.out.println("Enter a valid number.");}}}
}
