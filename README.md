LearningBot
===========

A Robocode robot which uses supervised learning (BonzaiBoost)


Learning data system
--------------------

The following instructions has to be done to make learning system work properly.

* Add *robocode.robot.filesystem.quota=2000000000* after all *robocode.options.view* lines in *robocode/config/robocode.properties*.
* Execute *chmod u-w robocode.properties* to avoid Robocode removing the added line.

The collected data will be saved in our robot bin folder (*bin/fr/insarennes/learningbot/controller/LearningBot.data/*).
The two created files will be used to create decision tree with BonzainBoost.


Decision tree creation
----------------------

Execute *bonzaiboost -S learningbot*, you can add the *-jobs n* option, were n is the amount of parallel tasks.