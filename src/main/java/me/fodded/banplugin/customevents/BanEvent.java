package me.fodded.banplugin.customevents;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

@Getter
public class BanEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    public UUID staff_uuid;
    public UUID banned_uuid;
    public String reason;
    public String staff_name;
    public String banned_name;
    public Integer duration;
    public Integer position;
    public Integer rating;

    public BanEvent(final String reason, final String staff_name, final String banned_name, final Integer duration, final Integer position, final Integer rating, final UUID staff_uuid, final UUID banned_uuid) {
        this.reason = reason;
        this.staff_name = staff_name;
        this.banned_name = banned_name;
        this.duration = duration;
        this.position = position;
        this.rating = rating;
        this.banned_uuid = banned_uuid;
        this.staff_uuid = staff_uuid;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
