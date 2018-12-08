package se.wiktoreriksson.minecraft.ban;

import org.bukkit.OfflinePlayer;

public class BanData {
    private long timestampexpire;
    private String reason;
    private OfflinePlayer player;

    public BanData(long timestampexpire, String reason, OfflinePlayer player) {
        this.timestampexpire = timestampexpire;
        this.reason = reason;
        this.player = player;
    }

    public long getTimestampexpire() {
        return timestampexpire;
    }

    public String getReason() {
        return reason;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public BanData setTimestampexpire(long timestampexpire) {
        this.timestampexpire = timestampexpire;
        return this;
    }

    public BanData setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public BanData setPlayer(OfflinePlayer player) {
        this.player = player;
        return this;
    }
}
