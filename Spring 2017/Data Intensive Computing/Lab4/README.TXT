###############
TASK 1 
###############
Compilation Steps:
1. hdfs dfs -put ~/inputTweets.txt/ ~/input
2. hadoop com.sun.tools.javac.Main *.java
3. jar cf wc.jar *.class
4. hadoop jar wc.jar WordCount ~/input/books ~/output1
5. hdfs dfs -get ~/output1

###############
TASK 2 
###############
Compilation Steps:

1. hdfs dfs -put ~/inputTweets.txt/ ~/input
2. hadoop com.sun.tools.javac.Main *.java
3. jar cf PairsandStripes.jar *.class
4. hadoop jar PairsandStripes.jar CoOccurrenceDriver pairs ~/input/inputTweets.txt ~/pairsOutput
5. hadoop jar PairsandStripes.jar CoOccurrenceDriver stripes ~/input/inputTweets.txt ~/stripesOutput
6. hdfs dfs -get ~/pairsOutput
7. hdfs dfs -get ~/stripesOutput

References:
https://github.com/bbejeck/hadoop-algorithms

###############
ACTIVITY 1 
###############
1. hdfs dfs -put ~/texts/ ~/input
2. hadoop com.sun.tools.javac.Main *.java
3. jar cf WordCountActivity1.jar *.class
4. hadoop jar WordCountActivity1.jar WordCountActivity1 ~/input/texts ~/outputFolder1 ~/la.lexicon.csv
