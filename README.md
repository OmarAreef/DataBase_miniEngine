# Database Mini-engine
Summary: A Mini Database engine without the support of joins 

Detailed: 

A Mini Database engine with the support of the following functions :
1) Creating a Table
2) Insert 
3) Update
4) Delete 
5) Creating a grid index 
6) Select 

all the database files for tables and indexes are paged according to the constraints in the DBApp.config file 

pseudo code: 

1) Creating table: 
    tables are created using Hashtables for each column type , min , max values. 
    tables are given a clustering key which is also considered primary key.
    tables information are stored in metadata.csv for later use.
    
    
