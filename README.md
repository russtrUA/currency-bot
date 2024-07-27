# Amazing Currency Rate Bot

This repository contains the implementation of a Telegram bot that provides currency exchange rates from various banks.

## Table of Contents

- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Design of a Bot](#design-of-a-bot)
- [User Settings](#user-settings)
- [Currency Rate Provider](#currency-rate-provider)
- [Notification Service](#notification-service)
- [Additional information](#additional-information)

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- Maven

### Installation

1. Clone the repository:

    ```sh  
    git clone git@github.com:russtrUA/currency-bot.git
    cd currency-bot
    ```  
2. Install the dependencies:

    ```sh  
    mvn install  
    ```  
   
3. [Create Your Own Bot](#create-your-own-bot)

## Configuration

The bot token must be set as an environment variable. This is a security measure to prevent exposing your bot token in the code.

### Setting the `BOT_TOKEN`

On Linux or macOS, use:

```sh  
export BOT_TOKEN=your_telegram_bot_token
```  
  
On Windows, use:  
  
```shell  
setx BOT_TOKEN your_telegram_bot_token
```  
  
## Usage  
### Starting the Bot  

To start the bot, simply run the `main` method in `AppLauncher`.  
### Telegram Bot Commands  
- `/start`: Initiates the bot and displays a welcome message.  
- Other commands and callback queries are handled to provide current exchange rates, user settings, and notifications.  

## Project Structure

The project is organized into the following packages:

- **`com.app.telegram.features.bot`**: Contains the bot implementation and command handling.
- **`com.app.telegram.features.user`**: Contains user settings management.
- **`com.app.telegram.features.rate`**: Contains the currency rate fetching logic.
- **`com.app.telegram.features.notification`**: Contains the notification service.
- **`com.app.telegram.model`**: Contains the data models for banks and currencies.

## Design of a Bot
Currency Rate Bot provides users with up-to-date currency rates and various customization options via a conversational interface. The design chosen for the Bot gives following advantages:
- All callback queries are managed through the `handleCallback` method, simplifying the extension and maintenance of callback logic.
- `CallbackHandler` utilizes helper methods for updating settings and sending keyboards, promoting code reuse and modularity. 
- By fetching and updating user settings dynamically, the handler ensures that the user's preferences are always considered. 
- By using a `ScheduledExecutorService`, the bot efficiently handles periodic tasks like fetching currency rates and sending notifications without blocking the main thread.
- The bot delegates specific tasks to specialized classes (`CurrencyRateThread` for rate fetching and `NotificationService` for notifications), enhancing maintainability.
- The `KeyboardFactory` class provides methods to create various inline keyboards used for user interactions. These keyboards are used to navigate settings, choose banks, select currencies, and manage notification settings.

## User settings  
  
User settings can be stored in a JSON file and updated once the user clicks on a specific button. If the user doesn't choose any settings, default settings will be applied.
- The `UserSettingsProvider` class handles loading and saving these settings using the `StorageService` interface. 
- Settings are loaded from storage during initialization, allowing the bot to resume with the same settings as before a shutdown.
- `UserSettingsProvider` provides default settings for new users and also allows users to customize their settings, which are dynamically fetched and updated.
- Implementation of singleton pattern ensures that only one instance of `UserSettingsProvider` exists, preventing issues related to multiple instances and providing a consistent state across the application.
- `UserSettingsProvider` uses a `ConcurrentHashMap` to manage user settings, enabling efficient and thread-safe access and modifications.
- User settings are automatically saved to a persistent storage (`FileStorageService`) whenever they are updated, ensuring that settings are not lost between application restarts. 
  
### Storage Service  

The `StorageService` interface allows for the implementation of alternative storage options for user settings. Currently, the `FileStorageService `class is provided, which stores settings in a JSON file. Alternatives could include:  
- **Database Storage**: Using an SQL or NoSQL database to store user settings.
- **Cloud Storage**: Storing settings in a cloud service like AWS S3 or Google Cloud Storage.

## Currency Rate Provider
The `CurrencyRateProvider` class leverages the Singleton pattern to provide a consistent and easy-to-access mechanism for managing currency rates across the application. The design ensures that all components that need currency rate data are synchronized and working with the latest information. The main components of `CurrencyRateProvider`: 
- The list `bankRateDtoList` holds the exchange rates from different banks.
- The map `bankResponseStatuses` holds the response statuses of different banks, which is useful for error handling and status checking.
- The method `getPrettyRatesByChatId` retrieves user settings, filters the bank rates based on the user's chosen banks and currencies, and formats the rates into a user-friendly string.
- The `CurrencyRateThread` class updates the `CurrencyRateProvider` with new rates. This class interacts with the Singleton instance of `CurrencyRateProvider`. 
- The run method of `CurrencyRateThread` updates the `bankRateDtoList` of the Singleton instance of `CurrencyRateProvider` with aggregated bank rates. This ensures that the latest rates are always available globally.

## Notification Service

The `NotificationService` class handles sending notifications to users at specified times. Users can set their preferred notification times in the settings. Here are some advantages of the chosen implementation:
- By using a `ScheduledExecutorService` with a thread pool, the notification service can handle multiple user notifications concurrently without performance degradation. 
- The method `calcDelay` ensures that notifications are scheduled accurately based on the user's preferred time, allowing for efficient and timely delivery of messages. 
- The use of a thread pool allows the application to manage multiple notification tasks simultaneously, ensuring that each user receives their notifications without delays.

## Additional information:

### Adding a New Bank

To add a new bank to the bot, follow these steps:
1. **Update the Bank Enum**: Add a new entry in the Bank enum in `com/app/telegram/model/Bank.java`
2. **Implement Bank Rate Mapping**: Update the `CurrencyRateThread` class to handle the response from the new bank API. Add a method to parse and map the new bank's response to the `BankRateDto` objects.
3. **Fetch Bank Rates**: In the `initializeBankRateLists` method, fetch the rates from the new bank.
4. **Aggregate Bank Rates**: In the `aggregateBankRates` method, add the new bank rates to the list.

### Adding a New Currency
To add a new currency, update the `Currency` enum in `Currency.java` with the new currency's details. For example:

```Java  
public enum Currency {  
    EUR(978),
    GBP(826),
    USD(840),
    // Add new currency
    JPY(392)  
    // Other methods...
}  
```  

### Create Your Own Bot

To create your own Telegram bot, follow these steps:

1. **Create a Bot on Telegram**:

   - Open Telegram and search for "BotFather".
   - Start a chat with BotFather and follow the instructions to create a new bot. You will receive a bot token.
2. **Set Up Your Bot**:

   - Set the `BOT_TOKEN` environment variable with the token you received from BotFather.
   - Customize the bot's functionality as needed.

For more detailed instructions, refer to the Telegram Bot [API documentation](https://core.telegram.org/bots/api).