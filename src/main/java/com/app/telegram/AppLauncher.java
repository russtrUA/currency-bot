package com.app.telegram;

import com.app.telegram.features.bot.CurrencyRateBot;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import static com.app.telegram.constants.Constants.BOT_TOKEN;

public class AppLauncher {
    public static void main(String[] args) {
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(BOT_TOKEN, new CurrencyRateBot(BOT_TOKEN));
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
