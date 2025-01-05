# Documentation

## Design

### First Steps

The main Project Structure was of the httpserverbase Repo.\
It was then simplified to just use Services to map to more Services.\
Which in most Cases were just Controller Methods with the same Signature.\
Also Instead of Repositories just simple DB Access Classes where used.\
These just have Methods to only run DB Queries which get used in Controller Methods.\
The Postgres Database was run via Docker.\
When building the container with the DockerFile an SQL script gets executed.\
And finally all the maven dependencies which are used and are also in the [pom.xml](../pom.xml) file are: \
- `jackson` : To use the ObjectMapper and annotations to De-/Serialize JSON Data.
- `jbcrypt` : To hash the users password to store in the Database
- `json` : For loading the Database Data from a json file 
- `postgresql` : Drivers for the Database adapter itself
- `lombok` : For easier Getters and Setters
- `junit-jupiter` : JUNIT5 for Unit Testing
- `mockito` : To mock Databases in Unit Tests

### Database Design

The main Tables were made (users, cards, deck, stack, packages).\
Later on deck/stack/package_cards tables were made to hold the card Ids of those Tables.\
For Package purchaes the transactions table got referenced by the package table,\
to determine if a Package has been bought.\
Then I made a rading_deals table which hold all the reading deals.\
This made me need to change the stack_cards table to show if a card is in deck or in a deal.\
Finally things I would change:
- Instead of separate deck and stacks tables just put their ids into the users table.



### Structure

- `src/main/java` : Has the main SourceCode
- `src/test/java` : Has the main Testing SourceCode
  - `src/main/java/org/mtcg` : Main Package
      - `Main` : The Main class which starts the Server and configures the Router
      - `httpserver` : Holds most of the Server Side Code
      - `utils` : Has utils like Enums the Router and misc Battle Utilities
      - `models` : Contains the Model Classes like user and Card
      - `controllers` : This folder contains all the controller Classes which hold the main Business Logic for whole app
      - `db` : In here is the Database Connection Class and various Access Classes which execute Database Queries

[Specification](./MTCG_Specification.pdf)

[Here](./mtcg-api.yaml) you can find the API Endpoints which are implemented almost each with a new Service which maps the Methods to Controller Methods which themselves are there own Services

### Further Development

With the few tweaks made to the httpserverbase repo the server worked the following :
- Server instance gets started with the setup port 10001
- It waits until it accepts a client and mackes a Socket Object with it 
- This Socket Object then is put in the Request Handler Object with the main Router instance
- Afterwards it gets submitted to the ExecuterService to be run asynchrounosly 
- The run Method then constructs the request object from the clients Inputstream
- From the constructed Request object the correct Service is determined by the requests path and the Router
- The router then points to the correct Service which then maps the correct controller method which then returns the response
- This response then finally gets written to the clients output Stream

Now onto the different Endpoints and how I manage them :

- **Users**: The `UserService` Class maps the following `UserController` methods :
  - `addUser` : builds a User Object from the Request body and does an INSERT SQL query
  - `listUser` : gets a User by username and selects the user data and returns it as JSON
  - `updateUser` : updates this exact Data from the User 
  
  Further Methods are specialized Database Queries for Authentication by Token as seen in the `SessionService`.\

- **Packages**: 
  - Here I use the `PackageController` which's `addPackage` Method gets invoked by the Service.
  - It reads the Card Data from the Body of the Request, then generates a random Package UUID and inserts it into the Database.
  - Here I first realized that a in between table `stack_cards` was needed to hold the card Id's of the Package.
  - After a Presentation was asked to move the Authentication into a Middleware.

- **Transactions** :
  - Moved the Authentication into the building of a Request Object.
  - Needed to change Unit Test due to refactor.
  - Made a `transactions` table to hold a record of the purchase of the package the package then holds a Foreign Key to this table and it one to the package which has been bought.

  - Failures :
    - At First I had not made the update of the users money purchase into a single Database Transactions.
    - Later on realized that taking a random Package from the Databases does not work with the given Test curl script.

- **Cards**
  - Just responds with the cards the given User from the Token has in his stack in JSON.
- **Deck**
  - Moved the initialization of Deck and Stack table to User creation.
  - Found out that the PUT Method for the Request actually should be a POST since it also allows CONFLICT to happen. But that is how the API specifies it.

- **UserData&Stats**
  - for the Endpoints `/users/{username}`, `/stats` and `/scoreboard` new Classes were made to only Serialize this Data for ease of use.
  - Implementation went smoothly.

- **Battle**
  - Made a static Class the `BattleLobby` which holds a `List<Pair<User>>` the `Pair` Class simply contains 2 members of the same Type and are called first/second.
  - It puts a User into a Pair and if 2 Users are found int puts the Pair into the List and clears it current one.
  - Afterwards its submits a `BattleExecutor` Object with the 2 Users and waits for the `Future<String>` to finish this String then becomes the BattleLog
  - In the battle the Decks of the Users gets copied and then put through various methods and calculations and card object movements until one of the copies are empty. The old Deck object then are used to move the Cards of the looser to the winner Stack and then the accumulated Battle Log then gets returned. 


- **Trading**
  - Struggled with a lot of validation before completing the trade the creation, listing and deletion went well.
  - Refactor added the Deck ID and Trading Deal ID to each card of the users stack.


### Unit Tests




