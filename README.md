# World Preview (1.8.9)
[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/voidxwalker)

Minecraft mod that draws a preview of the world on the loading screen during chunk generation.
- **Legal** for the minecraft java edition leaderboards
- **Compatible** with all allowed mods
- World preview **doesn't modify world generation** in any way. It simply takes already completed chunks, converts them to client chunks (Chunks minecraft can render) and then draws these chunks on the loading screen.
- There are **no server side modifications** besides minecraft getting the random value for the player spawn earlier (Random is Random so it doesn't change anything)
- You will not be able to reset after the worldgen reaches 100%. This is intentional

![F3 + Esc example](https://github.com/Minecraft-Java-Edition-Speedrunning/mcsr-worldpreview-1.8.9/blob/main/preview-example.png?raw=true)
## Usage
The preview will always render when you create a world.

There are 2 ways the player can interact with the preview:

**Direct Inputs**
- Leave World Generation and Reset: Press the "Save and Quit to Title" button to instantly leave the World Generation to the title screen

--------
**Hotkeys**
(These can be changed in the Controls Screen)
- Leave World Generation and Reset with Hotkey, default button "k" (English keyboard): Press the hotkey button to instantly leave the World Generation to the title screen
- Freeze Preview, default button "j" (English keyboard): Freezes the preview (helps with CPU performance)

## Config file 

A config file, `worldpreview.properties`, is created automatically when the game is first loaded. The file is stored in .minecraft/config/

The following config options are available: 

***loading_screen_fps*** : 
FPS during the preview. Chunks are only loaded during active frames, so this also affects clientside worldgen speed. Minimum: 5fps

***loading_screen_polling_rate*** : 
Input detections per second during preview. Lowers GPU usage, but inputs may not be detected if it's too low. 
Note that inputs will also be checked every frame, so this value should be greater than or equal to `loading_screen_fps`.

***worldgen_log_interval*** : 
Time in milliseconds between each world generation percentage log. 
Smaller values allow worldgen_freeze_percentage to update more accurately, but also use slightly more CPU. Minimum: 50, Maximum: 1000, Default: 100

***worldgen_freeze_percentage*** : 
Worldgen percentage to freeze at. 
Frozen previews are still rendered at loading_screen_fps, but no additional terrain is loaded. Saves CPU usage. Minimum: 50, Maximum: 100, Default: 70 (since previews start at 50%)


## For Macro Makers
The mod prints 3 different log lines:
- "Starting preview" at the start of the preview (Reset buttons unlocked)
- "Leaving world generation" when leaving world generation (Reset buttons locked)
- "Preview frozen with hotkey" when preview is frozen manually.
- "Preview at X%, freezing automatically" when preview is frozen automatically.

## Authors

- [@Void_X_Walker](https://www.github.com/voidxwalker) (https://ko-fi.com/voidxwalker)
- [@pixfumy](https://www.github.com/pixfumy)
- [@tildejustin](https://www.github.com/tildejustin)

