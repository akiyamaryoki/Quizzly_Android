# Quizzly Android Application

This is a quiz game that is facilitated by a server to up to 4 players. All connections are maintained via Bluetooth. A description of the application can be found below.

## Player Creation Screen

This screen allows players to enter a username and join a game. Note that up to 4 players can join a game at any time and if a game is currently in progress the server will respond with a message to that effect.

TODO: Add a smoother transition between joining the game and the first question. Add a welcome page with the application logo.

## Question and Answer Screen

This screen displays questions received by the application and allows the user to submit answers.

TODO: Add a smoother transition between answering a question and the next score update. Add a timer to show the remaining time to answer the provided question.

## Score Update Screen

This screen displays the current scores as received by the server. Player rankings are ordered from top (first) to bottom (last).

TODO: Add a smoother transition between viewing a score update and the next question. Add a message to indicate to the user when the game has ended.

## Design

The application was created in Android Studio. Please see the Quizzly folder for a detailed layout of the application.

## Authors

* **Josh Kimmel** - *Bluetooth functionality, server interaction, view transitions* - (https://github.com/joshkimmel16)
* **Jayant Mehra** - *UI for score update screen* - (https://github.com/JayantMehra)
* **Kunjan Patel** - *UI for player creation screen* - (https://github.com/coolkp)
* **Peiqi Wu** - *UI for question and answer screen* - (https://github.com/akiyamaryoki)