package com.app.telegram;

import com.app.telegram.features.bot.CurrencyRateBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import static com.app.telegram.constants.Constants.BOT_TOKEN;

public class AppLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppLauncher.class);

    public static void main(String[] args) {
        LOGGER.info("Starting the Telegram bot application...");

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(BOT_TOKEN, new CurrencyRateBot(BOT_TOKEN));
            LOGGER.info("Bot successfully registered");
            Thread.currentThread().join();
        } catch (Exception e) {
            LOGGER.error("Failed to register the bot", e);
        }
    }
}