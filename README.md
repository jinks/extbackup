# ExtBackup
Minimal Minecraft backup mod, relying on external backup tools.

## Why?
The regular well know backup mods lack the sophistication of dedicated backup tools.

ExtBackup does not care about storage, retention, rotating old backups, freeing up space or any of that stuff.
All it does is save the world to disk and then call a script or program on a configurabe schedule. The external
program can be as simple or as complicated as you want it to be. Connect your miecraft to borg, restic, Veeam or
whatever you like and backup your worlds however you like.

## Target audience
ExtBackup is aimed at server owners. It's so far only tested on Linux and does not offer much configuarion. It just runs
the program with the Minecraft directory as current working directory. It may well work on Windows, or it may not.

## Acknowledgements
* [LatvianModder](https://github.com/LatvianModder) for his work on [FTB-Backups](https://github.com/FTBTeam/FTB-Backups)
  where I stole a lot of the code for this.
* [alexbobp](https://github.com/alexbobp) and the [elytra](https://github.com/elytra) group for
  [BTFU](https://github.com/elytra/BTFU), which gave me the idea but didn't go quite far enough.
