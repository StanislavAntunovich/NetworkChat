package ru.geekbrains.chat;

public class MessagesPatterns {
    public static final String AUTH_MESSAGE = "/auth %s %s";
    public static final String REGISTRATION_MESSAGE = "/registration %s %s";
    public static final String ADDRESSED_MESSAGE_PATTERN = "/w %s %s";
    public static final String INCOME_MESSAGE = "%s: %s\n";
    public static final String MESSAGE_PATTERN = "%s>%s";
    public static final String USER_OFFLINE_PATTERN = "server>user %s is offline";

    public static final String REQUEST_ONLINE_USERS = "/get_online";

    public static final String USER_CAME_OFLINE_COMMAND = "/user_gone";
    public static final String USER_CAME_OFLINE_PATTERN = USER_CAME_OFLINE_COMMAND + " %s";
    public static final String USER_CAME_ONLINE_COMMAND = "/user_came";
    public static final String USER_CAME_ONLINE_PATTERN = USER_CAME_ONLINE_COMMAND + " %s";

    public static final String USERS_LIST_COMMAND = "/users_list";
    public static final String USERS_LIST_PATTERN = USERS_LIST_COMMAND + " %s";

    public static String HISTORY_FILENAME_PATTERN = "%s_history.txt";


}
