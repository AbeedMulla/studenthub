package com.studenthub.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for date and time formatting operations.
 */
public class DateTimeUtils {
    
    private static final SimpleDateFormat TIME_FORMAT_12H = 
        new SimpleDateFormat("h:mm a", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT_24H = 
        new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT_SHORT = 
        new SimpleDateFormat("MMM d", Locale.getDefault());
    private static final SimpleDateFormat DATETIME_FORMAT = 
        new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
    private static final SimpleDateFormat DAY_FORMAT = 
        new SimpleDateFormat("EEEE", Locale.getDefault());
    
    /**
     * Format time in 12-hour format (e.g., "9:30 AM")
     */
    public static String formatTime(long timestamp) {
        return TIME_FORMAT_12H.format(new Date(timestamp));
    }
    
    /**
     * Format time from hour and minute (e.g., "9:30 AM")
     */
    public static String formatTime(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return TIME_FORMAT_12H.format(cal.getTime());
    }
    
    /**
     * Format date (e.g., "Jan 15, 2024")
     */
    public static String formatDate(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }
    
    /**
     * Format date short (e.g., "Jan 15")
     */
    public static String formatDateShort(long timestamp) {
        return DATE_FORMAT_SHORT.format(new Date(timestamp));
    }
    
    /**
     * Format date and time (e.g., "Jan 15, 2024 9:30 AM")
     */
    public static String formatDateTime(long timestamp) {
        return DATETIME_FORMAT.format(new Date(timestamp));
    }
    
    /**
     * Get day name (e.g., "Monday")
     */
    public static String getDayName(long timestamp) {
        return DAY_FORMAT.format(new Date(timestamp));
    }
    
    /**
     * Get current day of week (Calendar.SUNDAY = 1, Calendar.SATURDAY = 7)
     */
    public static int getCurrentDayOfWeek() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }
    
    /**
     * Check if timestamp is today
     */
    public static boolean isToday(long timestamp) {
        Calendar today = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(timestamp);
        
        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
    }
    
    /**
     * Check if timestamp is tomorrow
     */
    public static boolean isTomorrow(long timestamp) {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(timestamp);
        
        return tomorrow.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
               tomorrow.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
    }
    
    /**
     * Check if timestamp is within this week
     */
    public static boolean isThisWeek(long timestamp) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(timestamp);
        
        // Get end of week (Saturday)
        Calendar endOfWeek = Calendar.getInstance();
        endOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        endOfWeek.set(Calendar.HOUR_OF_DAY, 23);
        endOfWeek.set(Calendar.MINUTE, 59);
        endOfWeek.set(Calendar.SECOND, 59);
        
        return target.after(now) && target.before(endOfWeek);
    }
    
    /**
     * Check if timestamp is in the past
     */
    public static boolean isPast(long timestamp) {
        return timestamp < System.currentTimeMillis();
    }
    
    /**
     * Get relative time string (e.g., "In 30 min", "In 2 hours", "Tomorrow")
     */
    public static String getRelativeTimeString(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = timestamp - now;
        
        if (diff < 0) {
            return "Overdue";
        }
        
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        
        if (minutes < 60) {
            return "In " + minutes + " min";
        } else if (hours < 24) {
            return "In " + hours + " hr" + (hours > 1 ? "s" : "");
        } else if (days == 1) {
            return "Tomorrow";
        } else if (days < 7) {
            return "In " + days + " days";
        } else {
            return formatDateShort(timestamp);
        }
    }
    
    /**
     * Get countdown string for timer (e.g., "25:00")
     */
    public static String formatCountdown(long milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
    
    /**
     * Get start of day timestamp
     */
    public static long getStartOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    /**
     * Get end of day timestamp
     */
    public static long getEndOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }
    
    /**
     * Get greeting based on time of day
     */
    public static String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        
        if (hour >= 5 && hour < 12) {
            return "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            return "Good Afternoon";
        } else if (hour >= 17 && hour < 21) {
            return "Good Evening";
        } else {
            return "Good Night";
        }
    }
    
    /**
     * Convert hour and minute to timestamp for today
     */
    public static long getTimestampForToday(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    /**
     * Check if current time is within quiet hours
     */
    public static boolean isWithinQuietHours(int startMinutes, int endMinutes) {
        Calendar now = Calendar.getInstance();
        int currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        
        if (startMinutes < endMinutes) {
            // Same day range (e.g., 9:00 - 17:00)
            return currentMinutes >= startMinutes && currentMinutes < endMinutes;
        } else {
            // Overnight range (e.g., 22:00 - 7:00)
            return currentMinutes >= startMinutes || currentMinutes < endMinutes;
        }
    }
}
