package com.simats.weekend.models;

/**
 * A sealed interface is a modern Java feature that restricts which classes can implement it.
 * This makes our list adapter safer because it can only contain types defined in this file.
 */
public sealed interface NotificationListItem {

    /**S
     * A record is a concise class for holding unchangeable data.
     * This represents a header in the list, like "Today" or "Yesterday".
     * @param title The text to display in the header.
     */
    record NotificationHeader(String title) implements NotificationListItem {}

    /**
     * This record represents a single, detailed notification item.
     * @param title The main text of the notification.
     * @param subtitle The secondary text below the title.
     * @param time The timestamp for the notification.
     * @param iconResId The resource ID for the icon drawable (e.g., R.drawable.ic_location).
     * @param iconBgColor The resource ID for the icon's background color (e.g., R.color.icon_green).
     */
    record NotificationItem(
            String title,
            String subtitle,
            String time,
            int iconResId,
            int iconBgColor
    ) implements NotificationListItem {}
}