The java file AprioriItemSet.java takes in the following parameters in the given order:
1. filepath - For eg: "F:\\java workspace\\DM2\\src\\edu\\buffalo\\dm\\associationruletestdata.txt"
2. minimum support  - For eg: 0.3
3. genRules For eg: true/ false in case you wish to generate rules or not
If genRules is true , then the following arguments are reuired as well :
4.   The rule query in one of the mentioned formats:
		// TEMPLATE 1 sample query --- "RULE ANY ['G82_Down','G59_Up']"
		// TEMPLATE 2 sample query --- "SIZEOF RULE 4"
		// TEMPLATE 3 sample query --- "1AND1 RULE ANY ['G10_Down'] RULE 1 ['G59_Up']"
		// TEMPLATE 3 sample query --- "1AND2 RULE 1 ['G82_Down'] HEAD 1"
		// TEMPLATE 3 sample query --- "2OR1 HEAD 1 RULE 1 ['G82_Down']"
		// TEMPLATE 3 sample query --- "2OR2 HEAD 3 RULE 3"   
5. minimum confidence  - For eg: 0.7
6. displayRules  For eg: true/ false in case you wish to display the rules and total number of rules that have qualified for the given query

Thus, the argument for the java file is similar to the following :
"F:\\java workspace\\DM2\\src\\edu\\buffalo\\dm\\associationruletestdata.txt" 0.5 true "2or2 BODY 1 HEAD 2" 0.7 true

Sample command to run from Command Prompt :
java -cp DataMining_Project1Apriori.jar edu.buffalo.dm.AprioriItemSet C:\\Users\\RIMI\\Desktop\\associationruletestdata.txt 0.5 true "2or2 BODY 1 HEAD 2" 0.7 true
