# Pete Kayer Helper

A RuneLite plugin for Pete Kayer's PvP challenges: attack timers, a prayer panel, auto protection prayer, weapon swaps based on his overhead prayer, tank gear between swings, and a Voidwaker special assist. Everything is sent as menu actions, never mouse clicks. Auto prayer, auto swap, the special assist and the Ice Barrage freeze are off by default.

For local use with the official client. It is not a Plugin Hub plugin.

## Run it

Build the plugin jar and drop it where the loader looks:

    powershell -ExecutionPolicy Bypass -File install-plugin.ps1

Start the official client with the plugin registered (no developer mode):

    powershell -ExecutionPolicy Bypass -File run-runelite-local.ps1

`run-runelite-local.ps1` compiles `loader/`, then starts RuneLite from the client jars the launcher already downloaded in `~/.runelite/repository2`. The loader registers every jar in `~/.runelite/local-plugins` that has a `runelite-plugin.properties` naming its plugin classes. The client logs in with `~/.runelite/credentials.properties`, so the character you last launched with `--insecure-write-credentials` is the one it uses.

## Develop

    .\gradlew.bat test
    .\gradlew.bat run