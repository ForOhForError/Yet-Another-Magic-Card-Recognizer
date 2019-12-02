![YamCR Banner](readmeImages/YamCRBanner.png)

## About

YamCR is an open-source Magic The Gathering card recognizer, written in Java using BoofCV.

Currently, the features are based around recognizing a card on a webcam feed and displaying it on screen for usecases such as livestreaming.

It's partially written as a pet project, and partially as a tool with features that other recognizers lack.

## Usage

### Setup

Extract the contents of the zip from the [a release page](/releases/latest) to any directory on your machine.

Run the program by opening the file "YamCR.jar" with any recent java runtime.

The first time the program launches, it will prompt you to select a path to save set and decklist files to. This can be any destination. Keep in mind that the set files can end up being large, so choose a partition with a few gigs to spare.

### Webcam selection

Currently, the YamCR needs to recieve webcam input to function. You will be prompted to choose a webcam device and resolution when the program first launches. The program may
repeat this prompt if the last used camera cannot be found, but it will attempt to respect your previous selection.

The program also supports "IP" webcams. Configuration for these is specified in the ```ip_cams``` section of the programs config.json file.

If features of the program need to be used without a webcam present, the following entry can be inserted into config.json to use a 'Dummy' webcam.

```
"ip_cams":[{"mode":"pull","address":"https:\/\/img.scryfall.com\/cards\/large\/front\/0\/a\/0a426922-5e96-48f3-b696-f5dc99258943.jpg?1562135149","name":"Sample IP Webcam"}]}}
```

### Generating sets and decklists

To recognize cards on the webcam feed, the program must process the features of each card. This process requires an internet connection to [Scryfall's](https://scryfall.com) API, as well as
a fair amount of processing time.

To generate sets, press the "Set Generator" button on the sidebar of the program window. A popup will display, giving a dropdown list of the types of sets
that should be generated. Once the desired option is selected, press the "Generate Sets" button to begin the set generation process. The window will log its progress.

To generate decklists, press the "Deck Generator" button on the sidebar of the program window. A popup will display, giving an area to paste in a list of card names (one per line), as well as a
deck name. The decklist supports many of the plaintext formats exported from deckbuilder sets. Once the desired cards are in the text area, the "Generate Deck" button can be used to generate a decklist file.

### Loading sets and decklists

Any sets and decklists can be seen in the settings pane, in the area below the function buttons.

Sets marked with a red X icon are available to be generated. These can be generated with the 'generate sets' option, or by selecting them in the selection pane and pressing 'generate selected'. 

Sets and decks can be unloaded by pressing the "Unload all" button, unloading all sets at once.

Multiple sets can be loaded at once by pressing the "Load Selected" button. This loads the selected set, as well as any sub-sets. For example, using this feature on the "Sets" folder icon will load all sets recursively. Depending on the amount being loaded, this may take some time. Additionally, since recognition time scales as the number of loaded sets increase, this is not a recommended use case at the moment.

### Recognition

Card recognition begins automatically when any number of sets or decks are loaded. The area within the white border on the webcam feed is processed. If a matching card is found, its name will display
in red text on the upper left of the webcam feed. 

The recognition area can be moved by clicking on the webcam feed. The area can be locked into place by checking the "Lock Recognition Bounds" checkbox.

The sidebar provides some options for refining recognition behavior:
- The "Only trigger recognition manually" checkbox can be used to disable automatic recognition. Instead, recognition will only be attempted when pressing any keyboard key after clicking the webcam area.
- The "Score threshold" bar determines how closely a match must "fit" to be accepted as a result. Turning this down will result in more matches, but may increase false positives.

### Other features

The "Card Preview" button will create a popup that will display the image of any recognized cards. This is a useful feature for livestreaming paper magic.

The "Screen Grab" button allows the user to select an area of their computer screen to attempt recognition on. Click at two opposing corners of a rectangle to set the recognition bounds. This is an experimental feature.
