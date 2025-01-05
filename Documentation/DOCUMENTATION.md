# Documentation

## Design

### First Steps

- The main project structure was based on the `httpserverbase` repository.  
- It was then simplified to use **Services** that map to more Services, which in most cases were just **Controller Methods** with the same signature.  
- Instead of using repositories, **simple DB Access Classes** were implemented.  
  - These classes contain methods solely for executing DB queries, which are invoked in the Controller Methods.  
- The PostgreSQL database was run via **Docker**.  
  - When building the container with the Dockerfile, an SQL script is executed.  
- Maven dependencies used in the project (also listed in the [pom.xml](../pom.xml)) are:  
  - **`jackson`**: For using `ObjectMapper` and annotations to de/serialize JSON data.  
  - **`jbcrypt`**: For hashing users' passwords before storing them in the database.  
  - **`json`**: For loading database data from a JSON file.  
  - **`postgresql`**: Database drivers.  
  - **`lombok`**: Simplifies the creation of getters and setters.  
  - **`junit-jupiter`**: JUnit 5 for unit testing.  
  - **`mockito`**: For mocking database interactions in unit tests.  

---

### Database Design

- **Initial Design**:  
  The main tables created were: `users`, `cards`, `deck`, `stack`, and `packages`.  

- **Later Improvements**:  
  - Additional tables like `deck_cards`, `stack_cards`, and `package_cards` were added to hold card IDs for their respective tables.  
  - A `transactions` table was created to record package purchases, linking to the `packages` table to track whether a package was bought.  
  - A `trading_deals` table was added to store trading deals.  
  - The `stack_cards` table was updated to indicate whether a card is in the deck or part of a deal.  

- **Future Changes to Consider**:  
  Instead of separate `deck` and `stack` tables, their IDs could be moved into the `users` table.  

---

### Structure

- **Directories**:  
  - `src/main/java`: Contains the main source code.  
  - `src/test/java`: Contains the test source code.  

- **Packages**:  
  - `org/mtcg`: Main package.  
    - `Main`: Starts the server and configures the router.  
    - `httpserver`: Contains most of the server-side code.  
    - `utils`: Contains enums, the router, and miscellaneous battle utilities.  
    - `models`: Contains model classes like `User` and `Card`.  
    - `controllers`: Contains all controller classes that hold the business logic for the application.  
    - `db`: Contains the database connection class and various access classes for executing database queries.  

- **References**:  
  - [Specification](./MTCG_Specification.pdf)  
  - [API Endpoints (mtcg-api.yaml)](./mtcg-api.yaml):  
    Almost all endpoints are implemented, each with a new service mapping to corresponding Controller Methods.  

---

## Further Development

### Server Workflow

- The server is started with the port `10001`.  
- It waits to accept a client and creates a `Socket` object for it.  
- The `Socket` object is passed to the `RequestHandler` object, which uses the main `Router` instance.  
- The `RequestHandler` is submitted to the `ExecutorService` to run asynchronously.  
- The `run()` method:  
  1. Constructs the `Request` object from the client's input stream.  
  2. Uses the `Router` to determine the correct Service based on the request path.  
  3. Maps the service to the appropriate Controller Method.  
  4. Executes the Controller Method and returns the response.  
  5. Writes the response to the client's output stream.  

---

### Endpoints

#### Users

- **`UserService`** maps the following methods in the `UserController`:  
  - `addUser`: Builds a `User` object from the request body and performs an `INSERT` SQL query.  
  - `listUser`: Retrieves user data by username and returns it as JSON.  
  - `updateUser`: Updates user data.  
- Authentication by token is handled in the `SessionService`.  

#### Packages

- The `PackageController` contains:  
  - `addPackage`:  
    - Reads card data from the request body.  
    - Generates a random package UUID and inserts it into the database.  
    - Uses the `stack_cards` table to hold the card IDs for the package.  
- Authentication was moved into middleware after a presentation.  

#### Transactions

- Authentication was moved to the request object construction phase.  
- Refactored unit tests after adding a `transactions` table to track package purchases.  
- Issues faced:  
  - Initially failed to make package purchases and user money updates a single database transaction.  
  - Selecting a random package from the database didn’t work with the given test curl script.  

#### Cards

- Responds with the cards a user (authenticated via token) has in their stack, returned as JSON.  

#### Deck

- Deck and stack table initialization was moved to user creation.  
- The `PUT` method should have been a `POST` since it also allows for conflicts, but the API specifies it as `PUT`.  

#### User Data & Stats

- New classes were created to serialize data for `/users/{username}`, `/stats`, and `/scoreboard`.  
- Implementation was smooth.  

#### Battle

- A static class `BattleLobby` was created:  
  - Holds a `List<Pair<User>>`, where `Pair` contains two users (first and second).  
  - Pairs users for battles, submits them to a `BattleExecutor`, and waits for a `Future<String>` containing the battle log.  
  - Cards from the losing user’s deck are moved to the winner’s stack.  
  - The battle log is returned after processing.  

#### Trading

- Initial struggles with validation during trades, but creation, listing, and deletion went smoothly.  
- Refactored to add the deck ID and trading deal ID to each card in the user's stack.  

---

## Unit Tests

The Libraries JUNIT 5 and Mockito were used to write the Unit Tests.

- Started with testing the server’s core functionality:  
  - Tested the server, router, and `UserService`.  
- Focused on Controller Methods as Services had similar structures.  
- Tested `Package` Model Class as the Constructor only allowed `Card[]` of certain size.
- Specialized tests:  
  - `PackageControllerTest`:  
    - Used Mockito to mock database interactions.  
    - Tested error handling and response correctness.  
  - Battle scenarios:  
    - Tested all scenarios, including random outcomes, for the special features.  
- Challenges:  
  - Refactoring often broke unit tests, slowing development.  
  - Post-refactor, tests for controllers were revisited to validate complex and simple methods.  

----

## Time tracking

| Feature                           | Time spent |
| --------------------------------- | ---------- |
| Maven project & Git Repo          | 1h         |
| Adapting httpserverbase           | 5h         |
| Implementing Models               | 3h         |
| Creating first Method Flow        | 3h         |
| Making the Database               | 4h         |
| Running the Database in Docker    | 4h         |
| Implementing DB Methods           | 6h         |
| Learning about Maven Dependencies | 2h         |
| Features for Intermediate Hand-In | 6h         |
| Packages                          | 5h         |
| Transactions                      | 8h         |
| First Time using Mockito          | 6h         |
| Cards & Decks                     | 6h         |
| User Data & Stats                 | 5h         |
| Trading                           | 8h         |
| Battle                            | 12h        |
| Debugging and Refactoring         | 20h        |
| Total Time                        | 104h       |
