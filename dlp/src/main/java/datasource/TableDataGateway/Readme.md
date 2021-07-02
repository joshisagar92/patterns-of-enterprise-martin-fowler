- An Object that act as a gateway to database table. One instance handle all row in table.
- Table Data Gateway is probably the simplest database interface pattern to use,
as it maps so nicely onto a database table or record type. 
- It also makes a natural point to encapsulate the precise access logic
of the data source.
- I use it least with Domain Model (116) because I find that 
Data Mapper (165) gives a better isolation between the Domain Model (116) and the database.
- I prefer Table Data Gateway when the result set representation is convenient for the Transaction Script (110) to work with.