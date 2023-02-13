package me.fodded.banplugin.customevents;

import org.bukkit.event.*;
import java.util.*;

public class UnbanEvent extends Event
{
    public UUID staff_uuid;
    public UUID unbanned_uuid;
    public String staff_name;
    public String unbanned_name;
    public Integer position;
    public Integer rating;

    public UnbanEvent(final String staff_name, final String unbanned_name, final Integer position, final Integer rating, final UUID staff_uuid, final UUID unbanned_uuid) {
        this.staff_name = staff_name;
        this.unbanned_name = unbanned_name;
        this.position = position;
        this.rating = rating;
        this.staff_uuid = staff_uuid;
        this.unbanned_uuid = unbanned_uuid;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }
}