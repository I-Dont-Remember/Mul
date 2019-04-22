package com.example.mul;

public class API_Comm {
    private static String base_url;

    public static void set_base_url(String url){
        // set the base url
        base_url = url;
    }

    // used by client and provider
    public static int get_curr_status(String append_url){
        // TODO: return value extracted from json

        return 0;
    }

    // used by provider
    public static boolean post_limit(String limit){
        // TODO: return success code based on HTP
        String[] data_values = limit.split(" ");
        int data_limit;

        if(data_values[1].equals("MB"))
            data_limit = Integer.parseInt(data_values[0]) * 1024;
        else
            data_limit = Integer.parseInt(data_values[0]) * 1024 * 1024;

        return true;
    }

    // used by client
    public static boolean post_balance(int balance){
        // TODO: set curr payment balance

        return true;
    }

    // used by client
    public static int post_mulchunk(String append_url, String IMEI_Client, String IMEI_Provider){
        // TODO: return value extracted from json

        return 0;
    }
}
