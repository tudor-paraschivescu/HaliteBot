# halite-bot
Halite AI Programming Challenge Bot designed by "team.getName(aCoolName)" team during the semester project for the Algorithms Design course (2nd year).

[Halite](http://github.com/HaliteChallenge/Halite) is a programming competition. Contestants write bots to play an original multi-player turn-based strategy game played on a rectangular grid. For more information about the game, visit [the website](http://halite.io).

## Versions:

### v1: 

### v2:
We changed the whole strategy, trying to achieve a more aggressive expansion. We run through the map twice - Complexity O(n^3):
* In the first browse, we identify neutral sites around the edges of our teritory and add them in a heap. The sorting method takes into account the production and strength of the site. After the sort, we extract one by one the sites from the heap and check if they can be conquered with one of our sites or by uniting two or three of our sites in a single turn. If not, try to make moves and unite sites in order to have enough strength to win the site next frame.
* In the second browse, we identify our sites that are at the edge of our teritory and didn't move yet and hold them STILL. Also at this stage we set the movements of the sites inside. The sites inside try to move to the closest edge or to the closest enemy if the distance to the enemy is within a certain range (smaller than a given value).

### finalbot:
We tested for different range values to prioritize the attack of an enemy rather than an empty site. In other words, we tried to find a "fine tune" of the bot aggressiveness. After the 1v1 tests, the winners were ranges [0, 6] and [0, 8]. After more testing between the two, the [0, 8] range was set for the final bot.

"*team.getName(aCoolName)*" team members: Ionuț Baciu, Andrei Bolojan, Tudor Paraschivescu, Alexandru Vlad.
