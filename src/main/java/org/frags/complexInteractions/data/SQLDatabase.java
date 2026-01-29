package org.frags.complexInteractions.data;

import io.lumine.mythic.bukkit.utils.storage.sql.hikari.HikariDataSource;
import org.frags.complexInteractions.objects.DataStorage;
import org.frags.complexInteractions.objects.missions.ActiveQuest;
import org.frags.complexInteractions.objects.missions.MobProgress;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SQLDatabase implements DataStorage {

    private final HikariDataSource dataSource;
    private final boolean isMySQL;

    public SQLDatabase(HikariDataSource dataSource, boolean isMySQL) {
        this.dataSource = dataSource;
        this.isMySQL = isMySQL;
    }

    @Override
    public void init() {
        String cooldownsTable = "CREATE TABLE IF NOT EXISTS player_cooldowns (" +
                "uuid CHAR(36) NOT NULL, " +
                "npc_id VARCHAR(64) NOT NULL, " +
                "expiration BIGINT NOT NULL, " +
                "PRIMARY KEY (uuid, npc_id));";

        String completedTable = "CREATE TABLE IF NOT EXISTS player_completed_conversations (" +
                "uuid CHAR(36) NOT NULL, " +
                "conversation_id VARCHAR(64) NOT NULL, " +
                "PRIMARY KEY (uuid, conversation_id));";

        String questsTable = "CREATE TABLE IF NOT EXISTS player_quest_progress (" +
                "uuid CHAR(36) NOT NULL, " +
                "quest_id VARCHAR(64) NOT NULL, " +
                "npc_id VARCHAR(64) NOT NULL, " +
                "mob_type VARCHAR(64) NOT NULL, " +
                "current_amount INT NOT NULL, " +
                "required_amount INT NOT NULL, " +
                "PRIMARY KEY (uuid, quest_id, mob_type));";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps1 = conn.prepareStatement(cooldownsTable);
             PreparedStatement ps2 = conn.prepareStatement(completedTable);
             PreparedStatement ps3 = conn.prepareStatement(questsTable)) {

            ps1.execute();
            ps2.execute();
            ps3.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public void setCooldown(UUID player, String npcId, long cooldownTime) {
        String sql = "REPLACE INTO player_cooldowns (uuid, npc_id, expiration) VALUES (?, ?, ?);";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            ps.setString(2, npcId);
            ps.setLong(3, cooldownTime);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getCooldown(UUID player, String npcId) {
        String sql = "SELECT expiration FROM player_cooldowns WHERE uuid = ? AND npc_id = ?;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            ps.setString(2, npcId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("expiration");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void removeCooldown(UUID player, String npcId) {
        String sql = "DELETE FROM player_cooldowns WHERE uuid = ? AND npc_id = ?;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            ps.setString(2, npcId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Long> getCooldowns(UUID player) {
        Map<String, Long> cooldowns = new HashMap<>();
        String sql = "SELECT npc_id, expiration FROM player_cooldowns WHERE uuid = ?;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String npcId = rs.getString("npc_id");
                    long expiration = rs.getLong("expiration");
                    if (expiration > System.currentTimeMillis()) {
                        cooldowns.put(npcId, expiration);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cooldowns;
    }

    @Override
    public void addCompletedConversation(UUID player, String conversationId) {
        String sql = "REPLACE INTO player_completed_conversations (uuid, conversation_id) VALUES (?, ?);";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            ps.setString(2, conversationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeCompletedConversation(UUID player, String conversationId) {
        String sql = "DELETE FROM player_completed_conversations WHERE uuid = ? AND conversation_id = ?;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            ps.setString(2, conversationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> getCompletedConversations(UUID player) {
        Set<String> completed = new HashSet<>();
        String sql = "SELECT conversation_id FROM player_completed_conversations WHERE uuid = ?;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    completed.add(rs.getString("conversation_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return completed;
    }

    @Override
    public Map<String, ActiveQuest> loadActiveQuests(UUID player) {
        Map<String, ActiveQuest> activeQuests = new HashMap<>();

        Map<String, Map<String, MobProgress>> tempMobs = new HashMap<>();
        Map<String, String> tempNpcIds = new HashMap<>();

        String sql = "SELECT quest_id, npc_id, mob_type, current_amount, required_amount FROM player_quest_progress WHERE uuid = ?;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String questId = rs.getString("quest_id");
                    String npcId = rs.getString("npc_id");
                    String mobType = rs.getString("mob_type");
                    int current = rs.getInt("current_amount");
                    int required = rs.getInt("required_amount");

                    MobProgress progress = new MobProgress(mobType, required);
                    progress.setCurrentAmount(current);

                    tempMobs.putIfAbsent(questId, new HashMap<>());
                    tempMobs.get(questId).put(mobType, progress);

                    tempNpcIds.put(questId, npcId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, Map<String, MobProgress>> entry : tempMobs.entrySet()) {
            String questId = entry.getKey();
            Map<String, MobProgress> mobs = entry.getValue();
            String npcId = tempNpcIds.get(questId);

            ActiveQuest quest = new ActiveQuest(npcId, questId, mobs);
            activeQuests.put(questId, quest);
        }

        return activeQuests;
    }

    @Override
    public void saveActiveQuest(UUID player, ActiveQuest quest) {
        String sql = "REPLACE INTO player_quest_progress (uuid, quest_id, npc_id, mob_type, current_amount, required_amount) VALUES (?, ?, ?, ?, ?, ?);";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (MobProgress mob : quest.getMobList().values()) {
                ps.setString(1, player.toString());
                ps.setString(2, quest.getQuestId());
                ps.setString(3, quest.getNpcId());
                ps.setString(4, mob.getMobType());
                ps.setInt(5, mob.getCurrentAmount());
                ps.setInt(6, mob.getRequiredAmount());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteActiveQuest(UUID player, String questId) {
        String sql = "DELETE FROM player_quest_progress WHERE uuid = ? AND quest_id = ?;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            ps.setString(2, questId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

