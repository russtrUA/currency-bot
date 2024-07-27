# Amazing Currency Rate Bot

This repository contains the implementation of a Telegram bot that provides currency exchange rates from various banks.

## Table of Contents

- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Usage](#usage)
- [User Settings](#user-settings)
- [Storage Service](#storage-service)
- [Adding a New Bank](#adding-a-new-bank)
- [Adding a New Currency](#adding-a-new-currency)
- [Adventages of Using Threads](#advantages-of-using-threads)

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- Maven

### Installation

1. Clone the repository:

    ```sh
    git clone https://github.com/yourusername/telegram-currency-rate-bot.git
    cd telegram-currency-rate-bot
    ```

2. Install the dependencies:

    ```sh
    mvn install
    ```

### Running the Bot

1. Set the `BOT_TOKEN` environment variable:

    ```sh
    export BOT_TOKEN=your_telegram_bot_token
    ```

2. Start the bot:


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

### Bot Commands
`/start`: Initiates the bot and displays a welcome message.
Other commands and callback queries are handled to provide current exchange rates, user settings, and notifications.

## User settings

User settings can be stored in a JSON file and updated once the user clicks on a specific button. If the user doesn't choose any settings, default settings will be applied. The `UserSettingsProvider` class handles loading and saving these settings using the `StorageService` interface.

## Storage Service
The `StorageService` interface allows for the implementation of alternative storage options. Currently, the `FileStorageService `class is provided, which stores settings in a JSON file. Alternatives could include:

### Alternative Storage:
Using an SQL or NoSQL database to store user settings.
Cloud Storage: Storing settings in a cloud service like AWS S3 or Google Cloud Storage.
Using Threads and Scheduler
The bot uses threads and a scheduler for handling background tasks:

## Adding a New Bank
To add a new bank to the bot, follow these steps:
1. **Update the Bank Enum**: Add a new entry in the Bank enum in `com/app/telegram/model/Bank.java`
2. **Implement Bank Rate Mapping**: Update the `CurrencyRateThread` class to handle the response from the new bank API. Add a method to parse and map the new bank's response to the `BankRateDto` objects.
3. **Fetch Bank Rates**: In the `initializeBankRateLists` method, fetch the rates from the new bank.
4. **Aggregate Bank Rates**: In the `aggregateBankRates` method, add the new bank rates to the list.

## Adding a New Currency
To add a new currency, update the Currency enum in Currency.java with the new currency's details. For example:

```Java
public enum Currency {
    EUR(978),
    GBP(826),
    USD(840),
    // Add new currency
    JPY(392);

    // Other methods...
}
```

## Advantages of Using Threads
1. **Concurrency**: Allows multiple tasks to run concurrently, improving performance.
2. **Responsiveness**: Keeps the application responsive by offloading time-consuming tasks to separate threads.

### Scheduler for CurrencyRateThread
The scheduler is used to periodically fetch and update currency rates:

```Java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
scheduler.scheduleAtFixedRate(currencyRateThread, 0, 10, TimeUnit.MINUTES);
```

#### Advantages:

1. **Regular Updates**: Ensures the currency rates are updated at fixed intervals (every 10 minutes in this case).
2. **Automated Execution**: Automatically handles the execution of the task without manual intervention.